package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
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

    /**
     * ButtonClick
     *
     * @param view which button
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.modules:
                // open setup
                PackageUtils.startActivity(new Intent(this, ModulesActivity.class), R.string.toast_noApp, this);
                break;
            case R.id.settings:
                // open settings
                PackageUtils.startActivity(new Intent(this, SettingsActivity.class), R.string.toast_noApp, this);
                break;
            case R.id.about:
                // open about
                PackageUtils.startActivity(new Intent(this, AboutActivity.class), R.string.toast_noApp, this);
                break;
            case R.id.txt_sample:
                // click on the google url
                String label = ((TextView) view).getText().toString();
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(label));
                i.setPackage(getPackageName());
                PackageUtils.startActivity(i, R.string.toast_noApp, this);
                break;
            case R.id.m_img_icon:
                // click on the app icon
                Toast.makeText(this, getString(R.string.app_name) + " - TrianguloY", Toast.LENGTH_SHORT).show();
                break;
            default:
                AndroidUtils.assertError("Unknown view: " + view);
        }
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

}
