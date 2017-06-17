package com.epam.androidlab.emailagent.api;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class RequestHandler extends AsyncTask<RequestType, Void, List> {
    private final String myId;
    private final MimeMessage mimeMessage;
    private final Message message;
    private com.google.api.services.gmail.Gmail service;
    private ApiRequests apiRequests;
    private List list;
    private RequestType request;

    public RequestHandler(ApiRequests apiRequests,
                          GoogleAccountCredential credential,
                          MimeMessage mimeMessage,
                          Message message) {
        this.apiRequests = apiRequests;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Email Agent")
                .build();
        myId = "me";
        this.mimeMessage = mimeMessage;
        this.message = message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List list) {
        super.onPostExecute(list);
    }

    @Override
    protected List doInBackground(RequestType... params) {
        if (params.length == 0) {
            cancel(true);
        }
        request = params[0];
        try {
            handleRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void handleRequest() throws IOException, MessagingException {
        switch (request) {
            case GET_MESSAGE_IDS:
                list = apiRequests.getMessages(service, myId);
                break;
            case GET_DRAFTS:
                apiRequests.getDrafts(service, myId);
                break;
            case SEND_EMAIL:
                if (mimeMessage != null) {
                    apiRequests.sendEmail(service, myId, mimeMessage);
                }
                break;
            case MAKE_DRAFT:
                if (mimeMessage != null) {
                    //apiRequests.makeDraft(service, myId, mimeMessage);
                }
                break;
            case GET_MESSAGE_BY_ID:
                if (message != null) {
                    apiRequests.getMessageById(service, myId, message);
                }
                break;
            case DELETE_MESSAGE:
                if (message != null) {
                    apiRequests.deleteMessage(service, myId, message);
                }
                break;
        }

    }
}
