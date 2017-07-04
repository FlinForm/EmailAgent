package com.epam.androidlab.emailagent.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.activities.MainActivity;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import javax.mail.internet.MimeMessage;

public class NewEmailFragment extends Fragment {
    private GoogleAccountCredential credential;
    private EditText emailBody, receiver, subject;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.email_new, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        credential = MainActivity.getCredential();

        receiver = (EditText) view.findViewById(R.id.receiverName);
        subject = (EditText) view.findViewById(R.id.subjectNewLetterText);
        emailBody = (EditText) view.findViewById(R.id.emailBody);

        View fab = view.findViewById(R.id.fabEmailFragment);
        fab.setOnClickListener(event -> sendNewEmail(view));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.new_email_tmenu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void sendNewEmail(View view) {
        if ("".equals(receiver.getText().toString())) {
            Snackbar.make(view, R.string.email_address_warning, BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }
        if (GmailApiHelper.isDeviceOnline(getContext())) {
            MimeMessage mimeMessage = GmailApiHelper
                    .createNewEmailMessage(credential.getSelectedAccountName(),
                            receiver.getText().toString(),
                            subject.getText().toString(),
                            emailBody.getText().toString());
            new RequestHandler(new GmailApiRequests(),
                    null,
                    mimeMessage,
                    null, null)
                    .execute(RequestType.SEND_EMAIL);
        }
        getFragmentManager().beginTransaction().remove(this).commit();
    }
}
