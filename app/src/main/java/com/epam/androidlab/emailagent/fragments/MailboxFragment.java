package com.epam.androidlab.emailagent.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.activities.NewEmailActivity;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxIdentifiers;
import com.epam.androidlab.emailagent.model.MailboxRecycleViewAdapter;
import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * The instance of this fragment is created, when we
 * select one 5 mailboxes in Navigation view.
 */

public class MailboxFragment extends Fragment
        implements View.OnScrollChangeListener, RequestHandler.OnDataChangedListener {
    private final String PROVIDER_URI = "content://com.epam.androidlab.emailagent.provider/";
    private final String MESSAGE_ID = "messageId";
    private final String MESSAGE_SNIPPET = "snippet";
    private final String SENDER = "sender";
    private final String SUBJECT = "Subject";
    private final String MAILER = "From";

    private final String MAILBOX_IDENTIFIER_TAG = "identifier";

    private Uri MAILBOX_URI;
    private MailboxIdentifiers mailboxIdentifier;
    private List<Message> messages;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private boolean isRecycleViewFilled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mailbox, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mailboxIdentifier =
                    MailboxIdentifiers.valueOf(bundle.getString(MAILBOX_IDENTIFIER_TAG));
        }

        MAILBOX_URI = Uri.parse(PROVIDER_URI + mailboxIdentifier.toString().toLowerCase());

        progressBar = (ProgressBar) view.findViewById(R.id.fragmentProgressBar);

        messages = new ArrayList<>();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> refreshContent());
        linearLayoutManager = new LinearLayoutManager(getContext());

        MailboxRecycleViewAdapter adapter = new MailboxRecycleViewAdapter(getActivity(), messages);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollChangeListener(this);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        if (GmailApiHelper.isDeviceOnline(getContext())) {
            progressBar.setVisibility(View.VISIBLE);
            new RequestHandler(new GmailApiRequests(),
                    messages,
                    null,
                    null,
                    this).execute(RequestType.BATCH_REQUEST, mailboxIdentifier);
        }

        View fab = view.findViewById(R.id.recycleFab);
        fab.setOnClickListener(event -> {
            Intent intent = new Intent(getContext(), NewEmailActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDataChanged() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
            System.out.println("pb is visible");
        }
        while (messages.contains(null)){
            messages.remove(null);
        }
        messages.add(null);

        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String messageId = messages.get(item.getItemId()).getId();
        if (GmailApiHelper.isDeviceOnline(getContext())) {
            if (mailboxIdentifier.equals(MailboxIdentifiers.TRASH)) {
                new RequestHandler(new GmailApiRequests(),
                        messages,
                        null,
                        messageId,
                        this).execute(RequestType.DELETE_MESSAGE, null);
            } else {
                new RequestHandler(new GmailApiRequests(),
                        messages,
                        null,
                        messageId,
                        this).execute(RequestType.DELETE_MESSAGE, MailboxIdentifiers.TRASH);
            }
        }
        messages.remove(item.getItemId());
        recyclerView.getAdapter().notifyDataSetChanged();
        getMailboxByIdentifier().remove(item.getItemId());
        return true;
    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        List<Message> messageReferences = getMailboxByIdentifier();
        if (!isRecycleViewFilled) {
            if (messages.size() < messageReferences.size()) {
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == messages.size() - 1) {
                    if (GmailApiHelper.isDeviceOnline(getContext())) {
                        new RequestHandler(new GmailApiRequests(),
                                messages,
                                null,
                                null,
                                this).execute(RequestType.BATCH_REQUEST, mailboxIdentifier);
                    }
                }
            } else {
                isRecycleViewFilled = true;
                messages.remove(null);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void refreshContent() {
        new RequestHandler(new GmailApiRequests(), messages, null, null, this)
                .execute(RequestType.REFRESH_REFERENCES, mailboxIdentifier);
        messages.clear();
    }

    private List<Message> getMailboxByIdentifier() {
        switch (mailboxIdentifier) {
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

    // Not finished yet.
    private void saveDataToDatabase() {
        for (int i = 0; i < messages.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_ID, messages.get(i).getId());
            cv.put(SENDER, GmailApiHelper.getMessagePart(MAILER, messages.get(i)));
            cv.put(SUBJECT.toLowerCase(), GmailApiHelper.getMessagePart(SUBJECT, messages.get(i)));
            cv.put(MESSAGE_SNIPPET, messages.get(i).getSnippet());
            getActivity().getContentResolver().insert(MAILBOX_URI, cv);
        }
    }
}
