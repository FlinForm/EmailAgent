package com.epam.androidlab.emailagent.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.model.AdapterUtils;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;

import java.io.UnsupportedEncodingException;

public class LetterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_letter);
        fillCard(Mailbox.getMessage());

        WebView myWebView = (WebView) findViewById(R.id.materialWebView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadData(getMessageBody(Mailbox.getMessage()),
                Mailbox.getMessage().getPayload().getMimeType(),
                "UTF-8");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private String getMessageBody(Message message) {
        byte[] bodyBytes;
        if (message.getPayload().getParts() == null) {
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
        String body = "";

        try {
            body = new String(bodyBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return body;
    }

    private void fillCard(Message message) {
        AdapterUtils helper = new AdapterUtils(this);
        TextView mailer = (TextView) findViewById(R.id.mailer);
        TextView subject = (TextView) findViewById(R.id.subject);
        TextView body = (TextView) findViewById(R.id.body);
        TextView date = (TextView) findViewById(R.id.date);
        ImageView imageView = (ImageView) findViewById(R.id.cardImageView);
        TextView imageViewText = (TextView) findViewById(R.id.imageViewText);

        mailer.setText(helper.formatReceiverText(helper.getReceiver(message)));
        subject.setText(helper.getMessageSubject(message));
        if ("".equals(message.getSnippet())) {
            body.setText(R.string.no_content);
        } else {
            body.setText(helper.formatCardText(message.getSnippet()));
        }
        date.setText(helper.parseDate(helper.getDate(message)));
        helper.setImageViewColor(helper.getReceiver(message).substring(0, 1).toLowerCase(),
                imageView);
        helper.setImageViewText(helper.getReceiver(message).substring(0, 2),
                imageViewText);
    }
}
