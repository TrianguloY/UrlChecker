package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.PackageUtilities;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        AndroidUtils.setActionBarColor(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_blog:
                // Open the blog
                PackageUtilities.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://triangularapps.blogspot.com/")), R.string.toast_noBrowser, this);
                break;
            case R.id.btn_openStore:
                // Open the Play Store
                PackageUtilities.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())), R.string.toast_noBrowser, this);
                break;
            case R.id.btn_shareStore:
                // Share the play store
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                // Add data to the intent, the receiving app will decide what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());

                PackageUtilities.startActivity(Intent.createChooser(share, getString(R.string.btn_shareStore)), R.string.toast_error, this);
                break;
        }
    }
}