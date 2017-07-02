package com.epam.androidlab.emailagent.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailApiHelper {

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    //Finished
    public static MimeMessage createNewEmailMessage(String from,
                                                String to,
                                                String subject,
                                                String messageText) {
        if (from != null) {
            Properties properties = new Properties();
            Session session = Session.getDefaultInstance(properties);
            MimeMessage email = new MimeMessage(session);
            try {
                email.setFrom(new InternetAddress(from));
                email.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
                email.setSubject(subject);
                email.setText(messageText);
                return email;
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

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
