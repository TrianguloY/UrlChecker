package com.trianguloy.urlchecker.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.PackageUtils;

/**
 * An activity with general app-related settings
 */
public class SettingsActivity extends Activity {

    public static final int REQUEST_CODE = 2;
    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.a_settings);
        AndroidUtils.configureUp(this);

        configureBrowserButtons();
        configureDayNight();
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

    /* ------------------- default browser ------------------- */

    // adapted from https://stackoverflow.com/a/74108806

    /**
     * hide buttons if not available
     */
    private void configureBrowserButtons() {
        boolean hide = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager = getSystemService(RoleManager.class);
            if (roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                hide = false;
            }
        }
        if (hide) findViewById(R.id.b1).setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            findViewById(R.id.b2).setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            findViewById(R.id.b3).setVisibility(View.GONE);
    }

    /**
     * open a specific dialog to choose the browser
     */
    @TargetApi(Build.VERSION_CODES.Q)
    public void chooseBrowserPopup(View view) {
        PackageUtils.startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER), REQUEST_CODE, R.string.toast_noApp, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, R.string.toast_defaultSet, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.canceled, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open android settings about the default browser
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void openBrowserSettings(View view) {
        // open the settings
        Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
        intent.putExtra(
                ":settings:fragment_args_key",
                "default_browser"
        );
        Bundle bundle = new Bundle();
        bundle.putString(":settings:fragment_args_key", "default_browser");
        intent.putExtra(
                ":settings:show_fragment_args",
                bundle
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageUtils.startActivity(intent, R.string.toast_noApp, this);
    }

    // adapted from https://groups.google.com/g/androidscript/c/cLq7eiUVpig/m/RDraxFYQCgAJ

    /**
     * Open the android app settings to open links as default
     */
    @TargetApi(Build.VERSION_CODES.S)
    public void openAppLinks(View view) {
        PackageUtils.startActivity(new Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                Uri.parse("package:" + getPackageName())
        ), R.string.toast_noApp, this);
    }

    /**
     * Open the android app settings
     */
    public void openAppDetails(View view) {
        PackageUtils.startActivity(new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName())
        ), R.string.toast_noApp, this);
    }

    /* ------------------- day/night ------------------- */

    /**
     * init dayNight spinner
     */
    private void configureDayNight() {
        AndroidSettings.THEME_PREF(this).attachToSpinner(
                this.findViewById(R.id.theme),
                o -> AndroidSettings.reload(SettingsActivity.this)
        );
    }
}
