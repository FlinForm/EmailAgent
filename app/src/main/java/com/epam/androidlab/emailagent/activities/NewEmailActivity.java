package com.epam.androidlab.emailagent.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;

import java.io.File;

import javax.mail.internet.MimeMessage;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * This activity provides content to create new letter.
 * Here we need to fill 3 fields to send letter: email body, receiver and letter subject.
 * Also user can attach file to his letter.
 */

public class NewEmailActivity extends AppCompatActivity {
    private final String DEFAULT_FONTS_PATH = "fonts/Marmelad-Regular.ttf";
    private final int REQUEST_CODE = 13;

    private LinearLayout layout;
    private EditText emailBody, receiver, subject;
    private Toolbar toolBar;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_email);

        layout = (LinearLayout) findViewById(R.id.fileImageLayout);

        toolBar = (Toolbar) findViewById(R.id.new_email_toolbar);
        setSupportActionBar(toolBar);

        receiver = (EditText) findViewById(R.id.receiverName);
        subject = (EditText) findViewById(R.id.subjectNewLetterText);
        emailBody = (EditText) findViewById(R.id.emailBody);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(DEFAULT_FONTS_PATH)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == REQUEST_CODE) {
            attachFile(data);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_email_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.attach_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.send_message:
                sendNewEmail();
                break;
        }
        return  true;
    }

    //This method attaches file to email.
    private void attachFile(Intent data) {
        file = new File(data.getData().getEncodedPath());
        ImageView view = new ImageView(this);
        view.setLayoutParams(new RelativeLayout.LayoutParams(200, 200));
        view.setImageResource(R.drawable.file);
        view.setOnClickListener(event -> {
            file = null;
            layout.removeAllViews();
            layout.invalidate();
        });
        layout.addView(view);
    }

    //Forms new email message and sends it.
    private void sendNewEmail() {
        if ("".equals(receiver.getText().toString())) {
            Snackbar.make(getWindow().getDecorView(),
                    R.string.email_address_warning, BaseTransientBottomBar.LENGTH_LONG).show();
            return;
        }
        if (GmailApiHelper.isDeviceOnline(this)) {
            MimeMessage mimeMessage = GmailApiHelper
                    .createNewEmailMessage(receiver.getText().toString(),
                            subject.getText().toString(),
                            emailBody.getText().toString(),
                            file);
            new RequestHandler(new GmailApiRequests(),
                    null,
                    mimeMessage,
                    null,
                    null)
                    .execute(RequestType.SEND_EMAIL);
        }
        finish();
    }
}
