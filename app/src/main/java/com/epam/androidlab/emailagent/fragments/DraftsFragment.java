package com.epam.androidlab.emailagent.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.RecycleViewAdapter;
import com.epam.androidlab.emailagent.activities.MainActivity;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DraftsFragment extends Fragment {
    private List<Message> drafts;
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
        drafts = new ArrayList<>();

        if (GmailApiHelper.isDeviceOnline(getContext())) {
            try {
                drafts = new RequestHandler(new GmailApiRequests(),
                        credential,
                        recyclerView,
                        null,
                        null).execute(RequestType.GET_DRAFTS).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            RecycleViewAdapter adapter = new RecycleViewAdapter(drafts);

            recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);

            View fab = view.findViewById(R.id.recycleFab);
            fab.setOnClickListener(event -> {
                    removeFragment();
                    getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentLayout, new NewEmailFragment(), MainActivity.NEW_EMAIL_TAG)
                    .commit();
            });
        }
    }

    private void removeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}
