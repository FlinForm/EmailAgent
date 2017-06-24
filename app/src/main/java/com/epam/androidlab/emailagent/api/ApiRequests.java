package com.epam.androidlab.emailagent.api;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public interface ApiRequests {
    void getMessageReferences(Gmail service, String userId, List<String> queries) throws IOException;

    void batchRequest(Gmail service, String userId, List<Message> messages, String query) throws IOException;

    void trashMessage(Gmail service, String userId, String messageId) throws IOException;

    List<Draft> getDrafts(Gmail service, String userId) throws IOException;

    Message getMessageById(Gmail service, String userId, Message message) throws IOException;

    Message getDraftById(Gmail service, String userId, Draft draft) throws IOException;

    void deleteMessage(Gmail service, String userId, String messageId) throws IOException;

    Draft makeAndSendDraft(Gmail service, String userId, MimeMessage email)
            throws IOException, MessagingException;

    Message sendEmail(Gmail service, String userId, MimeMessage email)
            throws MessagingException, IOException;
}
