package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.companions.VersionManager;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.PackageUtils;

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

        // mark as seen if required
        VersionManager.check(this);

        // open tutorial if not done yet
        if (!TutorialActivity.DONE(this).get()) {
            PackageUtils.startActivity(new Intent(this, TutorialActivity.class), R.string.toast_noApp, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // option for the open in clipboard shortcut
        menu.add(R.string.shortcut_checkClipboard)
                .setIcon(AndroidUtils.getColoredDrawable(R.drawable.ic_clipboard, android.R.attr.textColorPrimary, this))
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT)
                .setOnMenuItemClickListener(o -> {
                    PackageUtils.startActivity(
                            new Intent(this, ShortcutsActivity.class),
                            R.string.toast_noApp,
                            this
                    );
                    return true;
                });
        return super.onCreateOptionsMenu(menu);
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
        Toast.makeText(this, getString(R.string.app_name) + " - " + getString(R.string.trianguloy), Toast.LENGTH_SHORT).show();
    }

}
