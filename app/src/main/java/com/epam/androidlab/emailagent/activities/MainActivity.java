package com.epam.androidlab.emailagent.activities;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.epam.androidlab.emailagent.fragments.EmailLetterFragment;
import com.epam.androidlab.emailagent.fragments.MailboxFragment;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxIdentifiers;
import com.epam.androidlab.emailagent.model.MailboxRecycleViewAdapter;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,
        NavigationView.OnNavigationItemSelectedListener,
        MailboxRecycleViewAdapter.OnMailSelectedListener,
        RequestHandler.OnDataChangedListener {

    private final String MAILBOX_IDENTIFIER_TAG = "identifier";
    public static final String EMAIL_FRAGMENT_TAG = "NewEmailFragment";

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

    private static com.google.api.services.gmail.Gmail gmailService = null;
    private static GoogleAccountCredential credential;

    private Fragment activeFragment;
    private FragmentTransaction transaction;
    private DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private Toolbar toolBar;
    private boolean authCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Marmelad-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        progressBar = (ProgressBar) findViewById(R.id.activityProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle("");
        toolBar.inflateMenu(R.menu.new_email_tmenu);
        setSupportActionBar(toolBar);

        if (!Mailbox.isLinksReceived()) {
            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
            initGmailService();
            getResultsFromApi();
            Mailbox.setLinksReceived(true);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged() {
        progressBar.setVisibility(View.INVISIBLE);
        transaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        Fragment inboxFragment = new MailboxFragment();
        bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.INBOX.toString());
        inboxFragment.setArguments(bundle);
        transaction.add(R.id.fragmentLayout,
                inboxFragment,
                MailboxIdentifiers.INBOX.toString()).commit();
        activeFragment = inboxFragment;
    }

    /*private void init() {
        // Your Google Cloud Platform project ID
        String projectId = ServiceOptions.getDefaultProjectId();

        // Your topic ID
        String topicId = "my-new-topic";

        // Create a new topic
        TopicName topic = TopicName.create(projectId, topicId);
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            topicAdminClient.createTopic(topic);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.printf("Topic %s:%s created.\n", topic.getProject(), topic.getTopic());
    }*/

    // Sometimes replacing fragments don't work.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        transaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.inbox_messages:
                Fragment inboxFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.INBOX.toString());
                inboxFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        inboxFragment,
                        MailboxIdentifiers.INBOX.toString());
                activeFragment = inboxFragment;
                break;
            case R.id.outbox_messages:
                Fragment outboxFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.SENT.toString());
                outboxFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        outboxFragment,
                        MailboxIdentifiers.SENT.toString());
                activeFragment = outboxFragment;
                break;
            case R.id.drafts:
                Fragment draftsFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.DRAFT.toString());
                draftsFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        draftsFragment,
                        MailboxIdentifiers.DRAFT.toString());
                activeFragment = draftsFragment;
                break;
            case R.id.recycle:
                Fragment recycleFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.TRASH.toString());
                recycleFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        recycleFragment,
                        MailboxIdentifiers.TRASH.toString());
                activeFragment = recycleFragment;
                break;
        }
        drawerLayout.closeDrawer(Gravity.START);
        transaction.commit();
        return true;
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
                if (activeFragment == null) {
                    return false;
                }
                getSupportFragmentManager().popBackStack();
                break;
        }
        return true;
    }

    @Override
    public void onLetterSelected() {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(R.id.fragmentLayout, new EmailLetterFragment(), EMAIL_FRAGMENT_TAG)
                .commit();
    }

    public static Gmail getGmailService() {
        return gmailService;
    }

    private void getResultsFromApi() {
        if (!GmailApiHelper.isGooglePlayServicesAvailable(this)) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!GmailApiHelper.isDeviceOnline(this)) {
            Snackbar.make(getCurrentFocus(),
                    getString(R.string.no_connection),
                    BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            new RequestHandler(new GmailApiRequests(), null, null, null, this)
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
                    Snackbar.make(getCurrentFocus(),
                            R.string.no_services_found,
                            BaseTransientBottomBar.LENGTH_LONG).show();
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

    public static GoogleAccountCredential getCredential() {
        return credential;
    }
}
