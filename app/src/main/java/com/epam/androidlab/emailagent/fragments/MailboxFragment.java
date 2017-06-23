package com.epam.androidlab.emailagent.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.activities.MainActivity;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxRecycleViewAdapter;
import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MailboxFragment extends Fragment implements View.OnScrollChangeListener {
    private final String MAILBOX_IDENTIFIER_TAG = "identifier";
    private RequestType mailboxIdentifier;
    private List<Message> messages;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ProgressDialog progressDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
            mailboxIdentifier = RequestType.valueOf(bundle.getString(MAILBOX_IDENTIFIER_TAG));
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.progress_dialog_message));

        messages = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getContext());

        MailboxRecycleViewAdapter adapter = new MailboxRecycleViewAdapter(getActivity(), messages);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollChangeListener(this);

        if (GmailApiHelper.isDeviceOnline(getContext())) {
            new RequestHandler(new GmailApiRequests(),
                    messages,
                    progressDialog,
                    recyclerView,
                    null,
                    null).execute(RequestType.MAKE_BATCH_REQUEST, mailboxIdentifier);
        }

        View fab = view.findViewById(R.id.recycleFab);
        fab.setOnClickListener(event -> getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentLayout, new NewEmailFragment(), MainActivity.NEW_EMAIL_TAG)
                .commit());

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    // !!!!!! FIX SCREEN ROTATION BUG !!!!!!!!!
    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        List<Message> messageReferences = getMailboxByIdentifier();

         if (messages.size() < messageReferences.size()) {
            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == messages.size() - 1) {
                if (GmailApiHelper.isDeviceOnline(getContext())) {
                    new RequestHandler(new GmailApiRequests(),
                            messages,
                            progressDialog,
                            recyclerView,
                            null,
                            null).execute(RequestType.MAKE_BATCH_REQUEST, mailboxIdentifier);
                }
            }
        }
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
}
