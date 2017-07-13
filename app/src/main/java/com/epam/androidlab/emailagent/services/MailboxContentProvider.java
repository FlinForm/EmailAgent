package com.epam.androidlab.emailagent.services;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class is used to save uploaded messages and
 * to use them in future when user device is offline.
 */

public class MailboxContentProvider extends ContentProvider {
    private static final String DB_NAME = "mailboxes";
    private static final int DB_VERSION = 1;

    //Table names
    private static final String INBOX_TABLE = "inbox";
    private static final String SENT_TABLE = "sent";
    private static final String DRAFT_TABLE = "draft";
    private static final String TRASH_TABLE = "trash";
    private static final String UNREAD_TABLE = "unread";

    //Table columns
    private static final String ID = "id";
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_SNIPPET = "snippet";
    private static final String SENDER = "sender";
    private static final String SUBJECT = "subject";

    //Creating tables
    private static final String INBOX_CREATE = "create table " + INBOX_TABLE + "("
            + ID + " integer primary key autoincrement, "
            + MESSAGE_ID + " text, "
            + SENDER + " text, "
            + SUBJECT + " text, "
            + MESSAGE_SNIPPET + " text " + ");";
    private static final String SENT_CREATE = "create table " + SENT_TABLE + "("
            + ID + " integer primary key autoincrement, "
            + MESSAGE_ID + " text, "
            + SENDER + " text, "
            + SUBJECT + " text, "
            + MESSAGE_SNIPPET + " text " + ");";
    private static final String DRAFT_CREATE = "create table " + DRAFT_TABLE + "("
            + ID + " integer primary key autoincrement, "
            + MESSAGE_ID + " text, "
            + SENDER + " text, "
            + SUBJECT + " text, "
            + MESSAGE_SNIPPET + " text " + ");";
    private static final String TRASH_CREATE = "create table " + TRASH_TABLE + "("
            + ID + " integer primary key autoincrement, "
            + MESSAGE_ID + " text, "
            + SENDER + " text, "
            + SUBJECT + " text, "
            + MESSAGE_SNIPPET + " text " + ");";
    private static final String UNREAD_CREATE = "create table " + UNREAD_TABLE + "("
            + ID + " integer primary key autoincrement, "
            + MESSAGE_ID + " text, "
            + SENDER + " text, "
            + SUBJECT + " text, "
            + MESSAGE_SNIPPET + " text " + ");";

    // Uri's
    private static final String AUTHORITY = "com.epam.androidlab.emailagent.provider";
    private static final String MESSAGES_PATH = "messages";
    private static final Uri MESSAGES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + MESSAGES_PATH);
    private static final String MESSAGES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + MESSAGES_PATH;
    private static final String MESSAGES_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + MESSAGES_PATH;

    private static final int URI_INBOX = 1;
    private static final int URI_SENT = 2;
    private static final int URI_DRAFT = 3;
    private static final int URI_TRASH = 4;
    private static final int URI_UNREAD = 5;
    private static final int URI_MESSAGES_ID = 6;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, INBOX_TABLE, URI_INBOX);
        uriMatcher.addURI(AUTHORITY, SENT_TABLE, URI_SENT);
        uriMatcher.addURI(AUTHORITY, DRAFT_TABLE, URI_DRAFT);
        uriMatcher.addURI(AUTHORITY, TRASH_TABLE, URI_TRASH);
        uriMatcher.addURI(AUTHORITY, UNREAD_TABLE, URI_UNREAD);
        uriMatcher.addURI(AUTHORITY, MESSAGES_PATH + "/#", URI_MESSAGES_ID);
    }

    private DBHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case URI_INBOX:
                cursor = sqLiteDatabase.query(INBOX_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case URI_SENT:
                cursor = sqLiteDatabase.query(SENT_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case URI_DRAFT:
                cursor = sqLiteDatabase.query(DRAFT_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case URI_TRASH:
                cursor = sqLiteDatabase.query(TRASH_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case URI_UNREAD:
                cursor = sqLiteDatabase.query(UNREAD_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),
                MESSAGES_CONTENT_URI);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return uriMatcher.match(uri) == URI_MESSAGES_ID ?
                MESSAGES_CONTENT_ITEM_TYPE : MESSAGES_CONTENT_TYPE;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        long rowID;
        Uri resultUri;
        switch (uriMatcher.match(uri)) {
            case URI_INBOX:
                System.out.println("1");
                rowID = sqLiteDatabase.insert(INBOX_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID);
                break;
            case URI_SENT:
                System.out.println("2");
                rowID = sqLiteDatabase.insert(SENT_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID);
                break;
            case URI_DRAFT:
                System.out.println("3");
                rowID = sqLiteDatabase.insert(DRAFT_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID);
                break;
            case URI_TRASH:
                System.out.println("4");
                rowID = sqLiteDatabase.insert(TRASH_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID);
                break;
            case URI_UNREAD:
                System.out.println("5");
                rowID = sqLiteDatabase.insert(UNREAD_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        int cnt;
        switch (uriMatcher.match(uri)) {
            case URI_INBOX:
                cnt = sqLiteDatabase.delete(INBOX_TABLE, selection, selectionArgs);
                break;
            case URI_SENT:
                cnt = sqLiteDatabase.delete(SENT_TABLE, selection, selectionArgs);
                break;
            case URI_DRAFT:
                cnt = sqLiteDatabase.delete(DRAFT_TABLE, selection, selectionArgs);
                break;
            case URI_TRASH:
                cnt = sqLiteDatabase.delete(TRASH_TABLE, selection, selectionArgs);
                break;
            case URI_UNREAD:
                cnt = sqLiteDatabase.delete(UNREAD_TABLE, selection, selectionArgs);
                break;
            default:
                cnt = sqLiteDatabase.delete(uri.toString().toLowerCase(), null, null);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        int cnt;
        switch (uriMatcher.match(uri)) {
            case URI_INBOX:
                cnt = sqLiteDatabase.update(INBOX_TABLE, values, selection, selectionArgs);
                break;
            case URI_SENT:
                cnt = sqLiteDatabase.update(SENT_TABLE, values, selection, selectionArgs);
                break;
            case URI_DRAFT:
                cnt = sqLiteDatabase.update(DRAFT_TABLE, values, selection, selectionArgs);
                break;
            case URI_TRASH:
                cnt = sqLiteDatabase.update(TRASH_TABLE, values, selection, selectionArgs);
                break;
            case URI_UNREAD:
                cnt = sqLiteDatabase.update(UNREAD_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(INBOX_CREATE);
            db.execSQL(SENT_CREATE);
            db.execSQL(DRAFT_CREATE);
            db.execSQL(TRASH_CREATE);
            db.execSQL(UNREAD_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
