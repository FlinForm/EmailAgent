package com.epam.androidlab.emailagent.activities;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.epam.androidlab.emailagent.fragments.MailboxFragment;
import com.epam.androidlab.emailagent.model.Mailbox;
import com.epam.androidlab.emailagent.model.MailboxIdentifiers;
import com.epam.androidlab.emailagent.model.MailboxRecycleViewAdapter;
import com.epam.androidlab.emailagent.services.MessagingService;
import com.google.android.gms.common.GoogleApiAvailability;

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
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private final String DEFAULT_FONTS_PATH = "fonts/Marmelad-Regular.ttf";
    private final String MAILBOX_IDENTIFIER_TAG = "identifier";

    private String MENU_TITLE;

    private FragmentTransaction transaction;
    private DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(DEFAULT_FONTS_PATH)
                .setFontAttrId(R.attr.fontPath)
                .build());

        //startService(new Intent(this, MessagingService.class));

        progressBar = (ProgressBar) findViewById(R.id.activityProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        MENU_TITLE = getString(R.string.inbox_messages);
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle("");
        setSupportActionBar(toolBar);

        if (!Mailbox.isLinksReceived()) {
            GmailApiHelper.initCredential(this);
            GmailApiHelper.initGmailService();
            getResultsFromApi();
            Mailbox.setLinksReceived(true);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        transaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.inbox_messages:
                MENU_TITLE = getString(R.string.inbox_messages);
                Fragment inboxFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.INBOX.toString());
                inboxFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        inboxFragment,
                        MailboxIdentifiers.INBOX.toString());
                break;
            case R.id.outbox_messages:
                MENU_TITLE = getString(R.string.outbox_messages);
                Fragment outboxFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.SENT.toString());
                outboxFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        outboxFragment,
                        MailboxIdentifiers.SENT.toString());
                break;
            case R.id.drafts:
                MENU_TITLE = getString(R.string.drafts);
                Fragment draftsFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.DRAFT.toString());
                draftsFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        draftsFragment,
                        MailboxIdentifiers.DRAFT.toString());
                break;
            case R.id.recycle:
                MENU_TITLE = getString(R.string.recycle);
                Fragment recycleFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.TRASH.toString());
                recycleFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        recycleFragment,
                        MailboxIdentifiers.TRASH.toString());
                break;
            case R.id.unread_messages:
                MENU_TITLE = getString(R.string.unread_messages);
                invalidateOptionsMenu();
                Fragment unreadFragment = new MailboxFragment();
                bundle.putString(MAILBOX_IDENTIFIER_TAG, MailboxIdentifiers.UNREAD.toString());
                unreadFragment.setArguments(bundle);
                transaction.replace(R.id.fragmentLayout,
                        unreadFragment,
                        MailboxIdentifiers.TRASH.toString());
                break;
        }
        invalidateOptionsMenu();
        drawerLayout.closeDrawer(Gravity.START);
        transaction.commit();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem item = menu.findItem(R.id.toolbarTitle);
        item.setTitle(MENU_TITLE);
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
                getSupportFragmentManager().popBackStack();
                break;
        }
        return true;
    }

    @Override
    public void onLetterSelected() {
        Intent intent = new Intent(this, LetterContentActivity.class);
        startActivity(intent);
    }

    private void getResultsFromApi() {
        if (!GmailApiHelper.isGooglePlayServicesAvailable(this)) {
            acquireGooglePlayServices();
        } else if (GmailApiHelper.credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!GmailApiHelper.isDeviceOnline(this)) {
            Snackbar.make(getWindow().getDecorView(),
                    getString(R.string.no_connection),
                    BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            new RequestHandler(new GmailApiRequests(), null, null, null, this)
                    .execute(RequestType.GET_ALL_REFERENCES);
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                GmailApiHelper.credential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                startActivityForResult(
                        GmailApiHelper.credential.newChooseAccountIntent(),
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
                    Snackbar.make(getWindow().getDecorView(),
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
                        GmailApiHelper.credential.setSelectedAccountName(accountName);
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

    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}
