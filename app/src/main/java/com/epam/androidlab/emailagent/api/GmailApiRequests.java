package com.epam.androidlab.emailagent.api;

import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxIdentifiers;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.ListDraftsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class GmailApiRequests implements ApiRequests {

    //Finished
    @Override
    public void getMessageReferences(Gmail service, String userId, List<String> queries)
            throws IOException {
        List<Message> messages = getListByQuery(queries.get(0));
        ListMessagesResponse response = service.users()
                .messages()
                .list(userId)
                .setLabelIds(queries)
                .execute();

        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users()
                        .messages()
                        .list(userId)
                        .setPageToken(pageToken)
                        .execute();
            } else {
                break;
            }
        }
    }

    @Override
    public void batchRequest(Gmail service, String userId, List<Message> messages, String query)
    throws IOException {
        int startPosition = messages.size();
        int endPosition = startPosition + 10;

        List<Message> messagesLinks = getListByQuery(query);
        if (messagesLinks.size() == 0) {
            return;
        }

        BatchRequest batchRequest = service.batch();
        JsonBatchCallback<Message> callback = new JsonBatchCallback<Message>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {

            }

            @Override
            public void onSuccess(Message message, HttpHeaders responseHeaders) throws IOException {
                messages.add(message);
            }
        };

        for (int i = startPosition; i < endPosition; i++) {
            if (i > messagesLinks.size() - 1) {
                break;
            }
            Message message = messagesLinks.get(i);
            service.users()
                    .messages()
                    .get(userId, message.getId())
                    .setFormat("full")
                    .queue(batchRequest, callback);
        }
        batchRequest.execute();
    }

    //Finished
    @Override
    public void deleteMessage(Gmail service, String userId, String messageId) throws IOException {
        service.users().threads().delete(userId, messageId).execute();
    }

    //Finished
    @Override
    public void trashMessage(Gmail service, String userId, String messageId) throws IOException {
        service.users().messages().trash(userId, messageId).execute();
    }


    //Finished
    @Override
    public Message sendEmail(Gmail service, String userId, MimeMessage email)
            throws MessagingException, IOException {
        Message message = makeMessageFromMimeMessage(email);
        message = service.users().messages().send(userId, message).execute();
        return message;
    }

    public static Message makeMessageFromMimeMessage(MimeMessage email)
            throws IOException, MessagingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private List<Message> getListByQuery(String query) {
        switch (MailboxIdentifiers.valueOf(query)) {
            case INBOX:
                return Mailbox.getInboxMessages();
            case SENT:
                return Mailbox.getOutboxMessages();
            case DRAFT:
                return Mailbox.getDrafts();
            case TRASH:
                return Mailbox.getTrash();
            case UNREAD:
                return null;
        }
        return null;
    }
}
