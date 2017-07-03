package com.epam.androidlab.emailagent.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.UnsupportedEncodingException;

public class EmailLetterFragment extends Fragment {
    private final String MAILER = "From";
    private final String SUBJECT = "Subject";
    private TextView mailer, subject, emailBody;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.email_letter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mailer = (TextView) view.findViewById(R.id.mailerContent);
        subject = (TextView) view.findViewById(R.id.subjectContent);
        emailBody = (TextView) view.findViewById(R.id.letterContent);
        emailBody.setMovementMethod(new ScrollingMovementMethod());
        fillTextViews();
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

    private void fillTextViews() {
        mailer.setText(GmailApiHelper.getMessagePart(MAILER, null));
        subject.setText(GmailApiHelper.getMessagePart(SUBJECT, null));
        byte[] bodyBytes;
        if (Mailbox.getMessage().getPayload().getParts() == null) {
            bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                    .getPayload()
                    .getBody()
                    .getData());
        } else {
            bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                    .getPayload()
                    .getParts()
                    .get(0)
                    .getBody()
                    .getData());
        }
        try {
            if (bodyBytes == null) {
                emailBody.setText(getString(R.string.problems_with_content));
                return;
            } else {
                String body = new String(bodyBytes, "UTF-8");
                emailBody.setText(body);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
