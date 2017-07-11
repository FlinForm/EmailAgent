package com.epam.androidlab.emailagent.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.epam.androidlab.emailagent.model.Mailbox;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * This class provides methods to initialize gmail API in application.
 */

public class GmailApiHelper {
    public static com.google.api.services.gmail.Gmail gmailService;
    public static GoogleAccountCredential credential;

    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM};

    private static final String APPLICATION_NAME = "Email Agent";

    //Initializes users credential.
    public static void initCredential(Context context) {
        GmailApiHelper.credential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    // Initializes gmail API service.
    public static void initGmailService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        gmailService = new Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    //Checks is android device is online.
    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    // Checks if google play services are available on device.
    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    //Creates MimeMessage with given parts.
    public static MimeMessage createNewEmailMessage(String to,
                                                    String subject,
                                                    String messageText,
                                                    File file) {
        if (credential.getSelectedAccountName() != null) {
            Properties properties = new Properties();
            Session session = Session.getDefaultInstance(properties);
            MimeMessage email = new MimeMessage(session);
            try {
                email.setFrom(new InternetAddress(credential.getSelectedAccountName()));
                email.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
                email.setSubject(subject);

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(messageText, "text/plain");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                if (file != null) {
                    mimeBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(file);

                    mimeBodyPart.setDataHandler(new DataHandler(source));
                    mimeBodyPart.setFileName(file.getName());
                    multipart.addBodyPart(mimeBodyPart);
                }
                email.setContent(multipart);

                return email;
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //Gets message part.
    public static String getMessagePart(String part, Message message) {
        if (message == null) {
            message = Mailbox.getMessage();
        }
        for (MessagePartHeader partHeader : message.getPayload().getHeaders()) {
            if (part.equals(partHeader.getName())) {
                return partHeader.getValue();
            }
        }
        return null;
    }
}
