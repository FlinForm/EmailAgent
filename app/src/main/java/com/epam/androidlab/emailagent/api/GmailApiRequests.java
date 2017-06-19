package com.epam.androidlab.emailagent.api;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.ListDraftsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class GmailApiRequests implements ApiRequests {
    //Finished
    @Override
    public List<Message> getMessages(Gmail service, String userId, List<String> queries) throws IOException {
        List<Message> messageReferences = new ArrayList<>();
        List<Message> messages = new ArrayList<>();

        ListMessagesResponse response = service.users()
                .messages()
                .list(userId)
                .setLabelIds(queries)
                .execute();

        while (response.getMessages() != null) {
            messageReferences.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users()
                        .messages()
                        .list(userId)
                        .setLabelIds(queries)
                        .setPageToken(pageToken)
                        .execute();
            } else {
                break;
            }
        }
        for (Message message : messageReferences) {
            messages.add(getMessageById(service, userId, message));
        }
        return messages;
    }

    //Finished
    @Override
    public Message getMessageById(Gmail service, String userId, Message message) throws IOException {
        return service.users().messages().get(userId, message.getId()).execute();
    }

    //Finished
    @Override
    public Message getDraftById(Gmail service, String userId, Draft draft) throws IOException {
        return service.users().drafts().get(userId, draft.getId()).execute().getMessage();
    }

    //Finished
    @Override
    public void deleteMessage(Gmail service, String userId, Message message) throws IOException {
        service.users().threads().delete(userId, message.getId()).execute();
    }

    //Finished
    @Override
    public List<Draft> getDrafts(Gmail service, String userId) throws IOException {
        ListDraftsResponse response = service.users().drafts().list(userId).execute();
        List<Draft> drafts = response.getDrafts();
        for (Draft draft : drafts) {
            System.out.println(getDraftById(service, userId, draft).getLabelIds());
        }
        return drafts;
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

    //Finished
    @Override
    public Draft makeAndSendDraft(Gmail service, String userId, MimeMessage email)
            throws IOException, MessagingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        Draft draft = new Draft();
        draft.setMessage(message);
        service.users().drafts().send(userId, draft).execute();
        return draft;
    }
}
