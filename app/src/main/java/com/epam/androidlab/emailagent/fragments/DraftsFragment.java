package com.epam.androidlab.emailagent.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.epam.androidlab.emailagent.Mailbox;
import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.RecycleViewAdapter;
import com.epam.androidlab.emailagent.activities.MainActivity;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

public class DraftsFragment extends Fragment implements View.OnScrollChangeListener {
    private List<Message> drafts;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycle_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.progress_dialog_message));

        drafts = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getContext());

        RecycleViewAdapter adapter = new RecycleViewAdapter(drafts);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollChangeListener(this);

        if (GmailApiHelper.isDeviceOnline(getContext())) {
            new RequestHandler(new GmailApiRequests(),
                    drafts,
                    progressDialog,
                    recyclerView,
                    null,
                    null).execute(RequestType.MAKE_BATCH_REQUEST);
        }

        View fab = view.findViewById(R.id.recycleFab);
        fab.setOnClickListener(event -> {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentLayout, new NewEmailFragment(), MainActivity.NEW_EMAIL_TAG)
                    .commit();
            });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (drafts.size() < Mailbox.getDrafts().size()) {
            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == drafts.size() - 1) {
                if (GmailApiHelper.isDeviceOnline(getContext())) {
                    new RequestHandler(new GmailApiRequests(),
                            drafts,
                            progressDialog,
                            recyclerView,
                            null,
                            null).execute(RequestType.MAKE_BATCH_REQUEST);
                }
            }
        }
    }

    private void removeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).commit();
    }
}
