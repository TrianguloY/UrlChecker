package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.PackageUtilities;

public class AboutActivity extends Activity {

    // ------------------- constants -------------------

    public static final String BLOG = "https://triangularapps.blogspot.com/";
    public static final String PLAY_STORE_PREFIX = "https://play.google.com/store/apps/details?id=";
    public static final String F_DROID_PREFIX = "https://f-droid.org/packages/";

    // ------------------- listeners -------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // append version to the action bar title
        setTitle(getTitle() + " (V" + BuildConfig.VERSION_NAME + ")");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // press the 'back' button in the action bar to go back
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Button clicked
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_openBlog:
                // Open the blog
                open(BLOG);
                break;
            case R.id.btn_openPlay:
                // Open the Play Store
                open(PLAY_STORE_PREFIX + getPackageName());
                break;
            case R.id.btn_openDroid:
                // Open F-Droid
                open(F_DROID_PREFIX + getPackageName());
                break;

            case R.id.btn_sharePlay:
                // Share play store
                share(PLAY_STORE_PREFIX + getPackageName());
                break;
            case R.id.btn_shareDroid:
                // Share F-droid
                share(F_DROID_PREFIX + getPackageName());
                break;
        }
    }

    // ------------------- actions -------------------

    /**
     * Open an url in the browser
     */
    private void open(String url) {
        PackageUtilities.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), R.string.toast_noBrowser, this);
    }

    /**
     * Share an url as plain text
     * (App name as subject)
     */
    private void share(String url) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide what to do with it.
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, url);

        PackageUtilities.startActivity(Intent.createChooser(share, getString(R.string.btn_shareStore)), R.string.toast_noApp, this);
    }
}