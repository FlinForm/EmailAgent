package com.epam.androidlab.emailagent.model;

import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

public class Mailbox {
    private static List<Message> inboxMessages = new ArrayList<>();
    private static List<Message> outboxMessages = new ArrayList<>();
    private static List<Message> drafts = new ArrayList<>();
    private static List<Message> trash = new ArrayList<>();
    private static List<Message> notRead = new ArrayList<>();

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

    public static List<Message> getNotRead() {
        return notRead;
    }

    public static void setInboxMessages(List<Message> inboxMessages) {
        Mailbox.inboxMessages = inboxMessages;
    }

    public static void setOutboxMessages(List<Message> outboxMessages) {
        Mailbox.outboxMessages = outboxMessages;
    }

    public static void setDrafts(List<Message> drafts) {
        Mailbox.drafts = drafts;
    }

    public static void setTrash(List<Message> trash) {
        Mailbox.trash = trash;
    }

    public static void setNotRead(List<Message> notRead) {
        Mailbox.notRead = notRead;
    }
}
