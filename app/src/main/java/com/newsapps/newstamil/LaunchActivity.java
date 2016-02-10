package com.newsapps.newstamil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;

import com.newsapps.newstamil.tasker.LoadingTask;

public class LaunchActivity extends Activity implements LoadingTask.LoadingTaskFinishedListener {

    private static final String TAG = "NewsTamil";
    private SharedPreferences m_prefs;
    private ProgressBar progressBar;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        // https://code.google.com/p/android/issues/detail?id=26658
        if (!isTaskRoot()) {
            finish();
            return;
        }

        m_prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        if (!m_prefs.getBoolean("first_launch", false)) {
            // run one time code
            initializeDefaults();
        }


        appEula.show();

    }


    private void initializeDefaults() {
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putBoolean("first_launch", true);
        editor.putString("server_url", "http://server_url.com");
        editor.putBoolean("ssl_trust_any", true);
        editor.putBoolean("ssl_trust_any_host", true);
        editor.putString("http_login", "");
        editor.putString("http_password", "");
        editor.putBoolean("enable_cats", true);
        editor.putBoolean("headlines_mark_read_scroll", true);
        editor.putString("headline_mode", "HL_COMPACT");
        editor.commit();
    }

    @Override
    public void onTaskFinished(boolean success) {
        moveNext();
    }

    AppEULA appEula = new AppEULA(this) {
        @Override
        protected void onEulaAccepted() {

            progressBar = (ProgressBar) findViewById(R.id.launch_progress);
            progressBar.setProgress(5);
            if (m_prefs.getString("password", null) == null) {
                //TODO register
            }
            else {
                new LoadingTask(LaunchActivity.this, progressBar, LaunchActivity.this).execute();
            }
        }

        @Override
        protected void onEulaDeclined() {
            finish();
        }
    };

    private void moveNext()
    {
        //Intent main = new Intent(LaunchActivity.this, OnlineActivity.class);
        Intent main = new Intent(LaunchActivity.this, SubscriptionListActivity.class);
        //Intent main = new Intent(LaunchActivity.this, ExpandableLayoutList.class);
        startActivity(main);

        finish();
    }
}
