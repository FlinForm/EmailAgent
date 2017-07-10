package com.epam.androidlab.emailagent.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;

import android.widget.TextView;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.epam.androidlab.emailagent.model.AdapterUtils;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;

import java.io.UnsupportedEncodingException;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LetterContentActivity extends AppCompatActivity {
    private final String MULTIPART_ALTERNATIVE = "multipart/alternative";
    private final String TEXT_HTML = "text/html";
    private final String TEXT_PLAIN = "text/plain";
    private final String MULTIPART_MIXED = "multipart/mixed";
    private final String TEXT_CALENDAR = "text/calendar";
    private final String MULTIPART_SIGNED = "multipart/signed";
    private final String MULTIPART_RELATED = "multipart/related";
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_letter);

        new RequestHandler(new GmailApiRequests(), null, null, null, null)
                .execute(RequestType.MODIFY_MESSAGE);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Marmelad-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        Toolbar toolBar = (Toolbar) findViewById(R.id.letter_toolbar);
        toolBar.setTitle("");
        setSupportActionBar(toolBar);

        webView = (WebView) findViewById(R.id.materialWebView);
        webView.setInitialScale(getScale());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadData(getMessageBody(Mailbox.getMessage()),
                "text/html; charset=utf-8",
                null);

        fillCard(Mailbox.getMessage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_email_tmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private String getMessageBody(Message message) {
        byte[] bodyBytes;
        switch (message.getPayload().getMimeType()) {
            case MULTIPART_ALTERNATIVE:
                bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                        .getPayload()
                        .getParts()
                        .get(1)
                        .getBody()
                        .getData());
                break;
            case TEXT_HTML:
                bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                        .getPayload()
                        .getBody()
                        .getData());
                break;
            case TEXT_PLAIN:
                bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                        .getPayload()
                        .getBody()
                        .getData());
                break;
            case MULTIPART_MIXED:
                bodyBytes = null;
                break;
            case TEXT_CALENDAR:
                bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                        .getPayload()
                        .getParts()
                        .get(0)
                        .getParts()
                        .get(1)
                        .getBody()
                        .getData());
                break;
            case MULTIPART_SIGNED:
                bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                        .getPayload()
                        .getParts()
                        .get(0)
                        .getParts()
                        .get(1)
                        .getBody()
                        .getData());
                break;
            case MULTIPART_RELATED:
                bodyBytes = null;
                break;
            default:
                bodyBytes = null;
                break;
        }
        String body = "";
        System.out.println(bodyBytes == null);
        try {
            if (bodyBytes != null) {
                body = new String(bodyBytes, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(body);
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

    private int getScale(){
        Display display =
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(500);
        val = val * 100d;
        return val.intValue();
    }
}
