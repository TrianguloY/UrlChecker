package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.PackageUtils;

import java.util.Objects;

/**
 * The activity to show when clicking the desktop shortcut (when 'opening' the app)
 */
public class MainActivity extends Activity {

    private AndroidSettings.Theme previousTheme;
    private String previousLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        AndroidSettings.setLocale(this);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check if the theme was changed, if so reload to apply
        var currentTheme = AndroidSettings.THEME_PREF(this).get();
        if (previousTheme == null) previousTheme = currentTheme;
        if (previousTheme != currentTheme) AndroidSettings.reload(this);
        // check if the locale was changed, if so reload to apply
        var currentLocale = AndroidSettings.LOCALE_PREF(this).get();
        if (previousLocale == null) previousLocale = currentLocale;
        if (!Objects.equals(previousLocale, currentLocale)) AndroidSettings.reload(this);
    }

    /* ------------------- button clicks ------------------- */

    public void openModulesActivity(View view) {
        PackageUtils.startActivity(new Intent(this, ModulesActivity.class), R.string.toast_noApp, this);
    }

    public void openSettings(View view) {
        PackageUtils.startActivity(new Intent(this, SettingsActivity.class), R.string.toast_noApp, this);
    }

    public void openAbout(View view) {
        PackageUtils.startActivity(new Intent(this, AboutActivity.class), R.string.toast_noApp, this);
    }

    public void openSample(View view) {
        PackageUtils.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.sample_url))
        ).setPackage(getPackageName()), R.string.toast_noApp, this);
    }

    public void aboutToast(View view) {
        Toast.makeText(this, getString(R.string.app_name) + " - TrianguloY", Toast.LENGTH_SHORT).show();
    }

}
