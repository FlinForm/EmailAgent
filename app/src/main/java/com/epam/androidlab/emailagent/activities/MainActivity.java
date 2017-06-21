package com.epam.androidlab.emailagent.activities;

import com.epam.androidlab.emailagent.Mailbox;
import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.epam.androidlab.emailagent.fragments.DraftsFragment;
import com.epam.androidlab.emailagent.fragments.InboxFragment;
import com.epam.androidlab.emailagent.fragments.NewEmailFragment;
import com.epam.androidlab.emailagent.fragments.OutboxFragment;
import com.epam.androidlab.emailagent.fragments.RecycleFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import com.google.api.services.gmail.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,
        NavigationView.OnNavigationItemSelectedListener {

    private final String INBOX_FRAGMENT_TAG = "InboxFragment";
    private final String OUTBOX_FRAGMENT_TAG = "OutboxFragment";
    private final String DRAFTS_FRAGMENT_TAG = "DraftsFragment";
    private final String RECYCLE_FRAGMENT_TAG = "RecycleFragment";
    public final static String NEW_EMAIL_TAG = "NewEmailFragment";

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM};

    private FragmentTransaction transaction;
    private DrawerLayout drawerLayout;
    private static com.google.api.services.gmail.Gmail gmailService = null;
    private static GoogleAccountCredential credential;
    private TextView textView;
    private ProgressDialog mProgress;
    public Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transaction  = getSupportFragmentManager().beginTransaction();

        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle("");
        toolBar.inflateMenu(R.menu.new_email_tmenu);
        setSupportActionBar(toolBar);

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        initGmailService();

        textView = (TextView) findViewById(R.id.textView);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.progress_dialog_message));

        getResultsFromApi();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        View fab = findViewById(R.id.fab);
        fab.setOnClickListener(event -> getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentLayout, new NewEmailFragment(), NEW_EMAIL_TAG)
                .commit());
    }

    // Sometimes replacing fragments don't work.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        transaction = getSupportFragmentManager().beginTransaction();
        switch (item.getItemId()) {
            case R.id.inbox_messages:
                    transaction.replace(R.id.fragmentLayout, new InboxFragment(), INBOX_FRAGMENT_TAG);
                break;
            case R.id.outbox_messages:
                    transaction.replace(R.id.fragmentLayout, new OutboxFragment(), OUTBOX_FRAGMENT_TAG);
                break;
            case R.id.drafts:
                    transaction.replace(R.id.fragmentLayout, new DraftsFragment(), DRAFTS_FRAGMENT_TAG);

                break;
            case R.id.recycle:
                    transaction.replace(R.id.fragmentLayout, new RecycleFragment(), RECYCLE_FRAGMENT_TAG);
                break;
        }
        drawerLayout.closeDrawer(Gravity.START);
        transaction.commit();
        return true;
    }

    private void checkDrawer() {
        if (!drawerLayout.isDrawerOpen(Gravity.START)) {
            transaction.commit();
        } else {
            checkDrawer();
        }
    }

    private boolean findAndReplaceFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            System.out.println("t");
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentLayout, fragment);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation:
                if (drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.closeDrawer(Gravity.START);
                } else {
                    drawerLayout.openDrawer(Gravity.START);
                }
                break;
            case R.id.move_back:
                getSupportFragmentManager().beginTransaction()
                        .detach(getSupportFragmentManager().findFragmentByTag(NEW_EMAIL_TAG))
                        .commit();
                break;
        }
        return true;
    }

    public static Gmail getGmailService() {
        return gmailService;
    }

    public static void setGmailService(Gmail gmailService) {
        MainActivity.gmailService = gmailService;
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            textView.setText(R.string.no_connection);
        } else {
            //new MakeRequestTask().execute();

                new RequestHandler(new GmailApiRequests(), null, mProgress, null, null, null)
                        .execute(RequestType.GET_ALL_REFERENCES);

        }
    }

    private void initGmailService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        gmailService = new Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(getString(R.string.quickstart_api))
                .build();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                startActivityForResult(
                        credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    textView.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        credential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                cancel(true);
                return null;
            }
        }

        private List<String> getDataFromApi() throws IOException {
            String user = "me";
            List<String> labels = new ArrayList<>();
            ListLabelsResponse listResponse =
                    gmailService
                            .users()
                            .labels()
                            .list(user)
                            .execute();
            ListMessagesResponse response = gmailService.users()
                    .messages()
                    .list(user)
                    .execute();
            for (Label label : listResponse.getLabels()) {
                labels.add(label.getName());
            }
            return labels;
        }


        @Override
        protected void onPreExecute() {
            textView.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                textView.setText(R.string.no_results);
            } else {
                output.add(0, getString(R.string.retrieve_data));
                textView.setText(TextUtils.join("\n", output));
            }
        }
    }

    public static GoogleAccountCredential getCredential() {
        return credential;
    }
}
