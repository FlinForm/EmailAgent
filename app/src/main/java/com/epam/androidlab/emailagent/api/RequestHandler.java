package com.epam.androidlab.emailagent.api;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.epam.androidlab.emailagent.activities.MainActivity;
import com.google.api.services.gmail.model.Message;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class RequestHandler extends AsyncTask<RequestType, Void, Void> {
    private final String INBOX_QUERY = "INBOX";
    private final String OUTBOX_QUERY = "SENT";
    private final String DRAFTS_QUERY = "DRAFT";
    private final String TRASH_QUERY = "TRASH";
    private final String UNREAD_QUERY = "UNREAD";

    private final String myId;
    private final MimeMessage mimeMessage;
    private final Message message;
    private com.google.api.services.gmail.Gmail service;
    private ApiRequests apiRequests;
    private List<Message> messages;
    private List<String> queries;
    private RequestType request;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    public RequestHandler(ApiRequests apiRequests,
                          List<Message> messages,
                          ProgressDialog progressDialog,
                          RecyclerView recyclerView,
                          MimeMessage mimeMessage,
                          Message message) {
        this.apiRequests = apiRequests;
        this.messages = messages;
        service = MainActivity.getGmailService();
        this.progressDialog = progressDialog;
        this.recyclerView = recyclerView;
        this.mimeMessage = mimeMessage;
        this.message = message;
        queries = new ArrayList<>();
        myId = "me";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null) {
            progressDialog.hide();
        }
        if (recyclerView != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected Void doInBackground(RequestType... params) {
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
        return null;
    }

    private void getAllReferences() throws IOException {
        queries.add(INBOX_QUERY);
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
        queries.add(DRAFTS_QUERY);
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
        queries.add(OUTBOX_QUERY);
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
        queries.add(TRASH_QUERY);
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
        /*queries.add(INBOX_QUERY);
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();*/
    }


    private void handleRequest() throws IOException, MessagingException {
        queries.clear();
        switch (request) {
            case GET_ALL_REFERENCES:
                getAllReferences();
                break;
            case GET_MESSAGE_IDS:
                queries.add(INBOX_QUERY);
                apiRequests.getMessageReferences(service, myId, queries);
                break;
            case GET_DRAFTS:
                queries.add(DRAFTS_QUERY);
                apiRequests.getMessageReferences(service, myId, queries);
                break;
            case GET_TRASH:
                queries.add(TRASH_QUERY);
                apiRequests.getMessageReferences(service, myId, queries);
                break;
            case SEND_EMAIL:
                if (mimeMessage != null) {
                    apiRequests.sendEmail(service, myId, mimeMessage);
                }
                break;
            case MAKE_BATCH_REQUEST:
                if (messages != null | messages.size() != 0) {
                    apiRequests.batchRequest(service, myId, messages, DRAFTS_QUERY);
                }
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
