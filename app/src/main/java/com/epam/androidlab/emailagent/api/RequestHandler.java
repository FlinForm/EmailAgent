package com.epam.androidlab.emailagent.api;

import android.os.AsyncTask;

import com.epam.androidlab.emailagent.activities.MainActivity;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxIdentifiers;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class RequestHandler extends AsyncTask<Object, Void, Void> {
    private final String INBOX_QUERY = "INBOX";

    private final String myId;
    private final MimeMessage mimeMessage;
    private final String messageId;
    private com.google.api.services.gmail.Gmail service;
    public OnDataChangedListener listener;
    private ApiRequests apiRequests;
    private List<Message> messages;
    private List<String> queries;
    private RequestType request;

    public RequestHandler(ApiRequests apiRequests,
                          List<Message> messages,
                          MimeMessage mimeMessage,
                          String messageId,
                          OnDataChangedListener listener) {
        this.listener = listener;
        this.apiRequests = apiRequests;
        this.messages = messages;
        this.mimeMessage = mimeMessage;
        this.messageId = messageId;
        queries = new ArrayList<>();
        myId = "me";
        service = GmailApiHelper.gmailService;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mimeMessage == null) {
            listener.onDataChanged();
        }
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (params.length == 0) {
            cancel(true);
        }
        request = (RequestType) params[0];
        try {
            handleRequest(params);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleRequest(Object... params) throws IOException, MessagingException {
        queries.clear();
        switch (request) {
            case GET_ALL_REFERENCES:
                getAllReferences();
                break;
            case GET_MESSAGE_IDS:
                queries.add(INBOX_QUERY);
                apiRequests.getMessageReferences(service, myId, queries);
                break;
            case SEND_EMAIL:
                if (mimeMessage != null) {
                    apiRequests.sendEmail(service, myId, mimeMessage);
                }
                break;
            case BATCH_REQUEST:
                if (messages == null | params.length < 2) {
                    return;
                }
                apiRequests.batchRequest(service, myId, messages, params[1].toString());
                for (Message message : messages) {
                    System.out.println(message.getPayload().getMimeType());
                }
                break;
            case DELETE_MESSAGE:
                if (messageId != null) {
                    if (params[1] != null) {
                        apiRequests.trashMessage(service, myId, messageId);
                        updateTrash();
                    } else {
                        apiRequests.deleteMessage(service, myId, messageId);
                    }
                }
                break;

        }
    }

    private void getAllReferences() throws IOException {
        queries.add(MailboxIdentifiers.INBOX.toString());
        apiRequests.getMessageReferences(service, myId, queries);
        System.out.println(Mailbox.getInboxMessages().get(0).getSnippet());
        queries.clear();
        queries.add(MailboxIdentifiers.DRAFT.toString());
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
        queries.add(MailboxIdentifiers.SENT.toString());
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
        queries.add(MailboxIdentifiers.TRASH.toString());
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
        /*queries.add(RequestType.UNREAD.toString());
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();*/
    }

    private void updateTrash() throws IOException {
        Mailbox.getTrash().clear();
        queries.add(MailboxIdentifiers.TRASH.toString());
        apiRequests.getMessageReferences(service, myId, queries);
        queries.clear();
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }
}
