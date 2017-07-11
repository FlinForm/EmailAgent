package com.epam.androidlab.emailagent.api;

import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxIdentifiers;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Its and implementation of ApiRequests, using gmail API.
 */

public class GmailApiRequests implements ApiRequests {

    //This method receives message references when first time connected to mailbox.
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

    //Forms batch request to get 10 messages from mailbox with given references.
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

    //Deletes message from the mailbox.
    @Override
    public void deleteMessage(Gmail service, String userId, String messageId) throws IOException {
        service.users().threads().delete(userId, messageId).execute();
    }

    //Gets Raw content of the message.
    @Override
    public Message getRawMessage(Gmail service, String userId, String messageId) throws IOException {
        return service.users().messages().get(userId, messageId).setFormat("raw").execute();
    }

    //Moves selected message to mailbox trash.
    @Override
    public void trashMessage(Gmail service, String userId, String messageId) throws IOException {
        service.users().messages().trash(userId, messageId).execute();
    }


    //Sends new message.
    @Override
    public Message sendEmail(Gmail service, String userId, MimeMessage email)
            throws MessagingException, IOException {
        Message message = makeMessageFromMimeMessage(email);
        message = service.users().messages().send(userId, message).execute();
        Mailbox.getOutboxMessages().add(message);
        return message;
    }

    //This method removes "UNREAD" label from message, when user opens message first time.
    @Override
    public void modifyMessage(Gmail service, String messageId, String userId) throws IOException {
        ModifyMessageRequest messageRequest = new ModifyMessageRequest();
        messageRequest.setRemoveLabelIds(Arrays.asList("UNREAD"));
        service.users().messages().modify(userId, messageId, messageRequest).execute();
    }

    //Constructs Message from MimeMessage
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

    //Gets list of messages by given tag.
    private List<Message> getListByQuery(String tag) {
        switch (MailboxIdentifiers.valueOf(tag)) {
            case INBOX:
                return Mailbox.getInboxMessages();
            case SENT:
                return Mailbox.getOutboxMessages();
            case DRAFT:
                return Mailbox.getDrafts();
            case TRASH:
                return Mailbox.getTrash();
            case UNREAD:
                return Mailbox.getUnRead();
        }
        return null;
    }
}
