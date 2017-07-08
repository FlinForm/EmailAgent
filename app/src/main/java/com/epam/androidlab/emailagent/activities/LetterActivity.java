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

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LetterActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_letter);

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
        if (message.getPayload().getParts() == null) {
            bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                    .getPayload()
                    .getBody()
                    .getData());
        } else {
            bodyBytes = Base64.decodeBase64(Mailbox.getMessage()
                    .getPayload()
                    .getParts()
                    .get(1)
                    .getBody()
                    .getData());
        }
        String body = "";

        try {
            if (bodyBytes != null) {
                body = new String(bodyBytes, "UTF-8");
            }
            System.out.println(body);
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

    private int getScale(){
        Display display =
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(500);
        val = val * 100d;
        return val.intValue();
    }
}
