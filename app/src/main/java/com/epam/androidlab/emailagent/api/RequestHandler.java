package com.epam.androidlab.emailagent.api;

import android.os.AsyncTask;

import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxIdentifiers;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Handles requests addressed to gmail API in parallel thread.
 */

public class RequestHandler extends AsyncTask<Object, Void, Void> {
    private final String INBOX_QUERY = "INBOX";
    private final String myId;
    private final MimeMessage mimeMessage;
    private final String messageId;

    private final com.google.api.services.gmail.Gmail service;
    private final OnDataChangedListener onDataChangedListener;
    private final ApiRequests apiRequests;
    private final List<Message> messages;
    private final List<String> query;
    private RequestType request;

    public RequestHandler(ApiRequests apiRequests,
                          List<Message> messages,
                          MimeMessage mimeMessage,
                          String messageId,
                          OnDataChangedListener onDataChangedListener) {
        this.onDataChangedListener = onDataChangedListener;
        this.apiRequests = apiRequests;
        this.messages = messages;
        this.mimeMessage = mimeMessage;
        this.messageId = messageId;
        query = new ArrayList<>();
        myId = "me";
        service = GmailApiHelper.gmailService;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mimeMessage == null) {
            if (onDataChangedListener != null) {
                onDataChangedListener.onDataChanged();
            }
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
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // The main method, that handles requests.
    private void handleRequest(Object... params) throws IOException, MessagingException {
        query.clear();
        switch (request) {
            case GET_ALL_REFERENCES:
                getAllReferences();
                break;
            case GET_MESSAGE_IDS:
                query.add(INBOX_QUERY);
                apiRequests.getMessageReferences(service, myId, query);
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
            case MODIFY_MESSAGE:
                Message message = Mailbox.getMessage();
                if (message.getLabelIds().contains(MailboxIdentifiers.UNREAD.toString())) {
                    apiRequests.modifyMessage(service, message.getId(), myId);
                    for (Message m : Mailbox.getUnRead()) {
                        if (m.getId().equals(message.getId())) {
                            message = m;
                            break;
                        }
                    }
                    Mailbox.getUnRead().remove(message);
                }
                break;
            case REFRESH_REFERENCES:
                if (params.length != 0) {
                    query.clear();
                    query.add(params[1].toString());
                    apiRequests.getMessageReferences(service, myId, query);
                    apiRequests.batchRequest(service, myId, messages, params[1].toString());
                    query.clear();
                }
        }
    }

    //Gets messages of all mailboxes with getMessageReferences() method.
    private void getAllReferences() throws IOException {
        query.add(MailboxIdentifiers.INBOX.toString());
        apiRequests.getMessageReferences(service, myId, query);
        query.clear();
        query.add(MailboxIdentifiers.DRAFT.toString());
        apiRequests.getMessageReferences(service, myId, query);
        query.clear();
        query.add(MailboxIdentifiers.SENT.toString());
        apiRequests.getMessageReferences(service, myId, query);
        query.clear();
        query.add(MailboxIdentifiers.TRASH.toString());
        apiRequests.getMessageReferences(service, myId, query);
        query.clear();
        query.add(MailboxIdentifiers.UNREAD.toString());
        apiRequests.getMessageReferences(service, myId, query);
        query.clear();
    }

    // Updates gmail trash when user deletes message.
    private void updateTrash() throws IOException {
        Mailbox.getTrash().clear();
        query.add(MailboxIdentifiers.TRASH.toString());
        apiRequests.getMessageReferences(service, myId, query);
        query.clear();
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }
}
