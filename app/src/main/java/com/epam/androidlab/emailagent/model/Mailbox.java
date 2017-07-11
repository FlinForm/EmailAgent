package com.epam.androidlab.emailagent.model;

import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

public class Mailbox {
    private static final List<Message> inboxMessages = new ArrayList<>();
    private static final List<Message> outboxMessages = new ArrayList<>();
    private static final List<Message> drafts = new ArrayList<>();
    private static final List<Message> trash = new ArrayList<>();
    private static final List<Message> unRead = new ArrayList<>();
    private static Message message;
    private static Message rawMessage;
    private static boolean linksReceived;

    private Mailbox() {
    }

    public static List<Message> getInboxMessages() {
        return inboxMessages;
    }

    public static List<Message> getOutboxMessages() {
        return outboxMessages;
    }

    public static List<Message> getDrafts() {
        return drafts;
    }

    public static List<Message> getTrash() {
        return trash;
    }

    public static List<Message> getUnRead() {
        return unRead;
    }

    public static Message getMessage() {
        return message;
    }

    public static void setMessage(Message message) {
        Mailbox.message = message;
    }

    public static boolean isLinksReceived() {
        return linksReceived;
    }

    public static void setLinksReceived(boolean linksReceived) {
        Mailbox.linksReceived = linksReceived;
    }

    public static void setRawMessage(Message rawMessage) {
        Mailbox.rawMessage = rawMessage;
    }
}
