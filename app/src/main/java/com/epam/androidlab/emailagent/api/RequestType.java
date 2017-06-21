package com.epam.androidlab.emailagent.api;

public enum RequestType {
    GET_MESSAGE_IDS,
    GET_DRAFTS,
    GET_INBOX,
    GET_TRASH,
    GET_OUTBOX,
    GET_UNREAD,
    MAKE_BATCH_REQUEST,
    SEND_EMAIL,
    MAKE_DRAFT,
    GET_MESSAGE_BY_ID,
    DELETE_MESSAGE,
    GET_ALL_REFERENCES
}
