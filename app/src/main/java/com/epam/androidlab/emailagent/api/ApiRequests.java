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


    Message getRawMesage(Gmail service, String userId, Message message) throws IOException;

    void deleteMessage(Gmail service, String userId, String messageId) throws IOException;

    Message sendEmail(Gmail service, String userId, MimeMessage email)
            throws MessagingException, IOException;
}
