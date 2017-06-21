package com.epam.androidlab.emailagent.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.epam.androidlab.emailagent.Mailbox;
import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.RecycleViewAdapter;
import com.epam.androidlab.emailagent.activities.MainActivity;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.gmail.model.Message;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class RecycleFragment extends Fragment {
    private List<Message> trash;
    private GoogleAccountCredential credential;
    private RecyclerView recyclerView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycle_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        credential = MainActivity.getCredential();
        trash = Mailbox.getTrash();

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.progress_dialog_message));

        if (GmailApiHelper.isDeviceOnline(getContext())) {
                new RequestHandler(new GmailApiRequests(),
                        trash,
                        progressDialog,
                        recyclerView,
                        null,
                        null).execute(RequestType.GET_TRASH);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            RecycleViewAdapter adapter = new RecycleViewAdapter(trash);

            recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);

            View fab = view.findViewById(R.id.recycleFab);
            fab.setOnClickListener(event -> getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentLayout, new NewEmailFragment())
                    .commit());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void removeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).commit();
    }
}
