package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.PackageUtils;

import java.util.Locale;

/**
 * The activity to show when clicking the desktop shortcut (when 'opening' the app)
 */
public class MainActivity extends Activity {

    private AndroidSettings.Theme previousTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
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
                if (BuildConfig.DEBUG) {
                    chooseLocaleDebug();
                    break;
                }
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
    }

    /**
     * Debug-only way to change locale.
     * To be replaced with a proper implementation with issue
     * https://github.com/TrianguloY/UrlChecker/issues/45
     */
    private void chooseLocaleDebug() {
        String[] locales = new String[]{"en", "es", "fr-FR", "iw", "pt-PT", "tr", "uk", "ja"};
        new AlertDialog.Builder(this)
                .setItems(locales, (dialog, which) -> {
                    Configuration config = new Configuration();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        config.setLocale(Locale.forLanguageTag(locales[which]));
                    } else {
                        config.locale = new Locale(locales[which]);
                    }
                    getBaseContext().getResources().updateConfiguration(config, null);
                    recreate();
                })
                .show();
    }

}
