package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.fragments.ActivityResultInjector;
import com.trianguloy.urlchecker.fragments.BrowserButtonsFragment;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.PackageUtils;

import java.util.Objects;

/**
 * An activity with general app-related settings
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        AndroidSettings.setLocale(this);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.a_settings);
        AndroidUtils.configureUp(this);

        configureBrowserButtons();
        configureDayNight();
        configureLocale();

        // if this app was reloaded, some settings may have changed, so reload previous one too
        if (AndroidSettings.wasReloaded(this)) AndroidSettings.markForReloading(this);
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

    /* ------------------- configure browser ------------------- */

    private final ActivityResultInjector activityResultInjector = new ActivityResultInjector();
    private final BrowserButtonsFragment browserButtons = new BrowserButtonsFragment(this, activityResultInjector);

    private void configureBrowserButtons() {
        browserButtons.onInitialize(findViewById(browserButtons.getLayoutId()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!activityResultInjector.onActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    /* ------------------- day/night ------------------- */

    /**
     * init dayNight spinner
     */
    private void configureDayNight() {
        AndroidSettings.THEME_PREF(this).attachToSpinner(
                this.findViewById(R.id.theme),
                v -> AndroidSettings.reload(SettingsActivity.this)
        );
    }

    /* ------------------- locale ------------------- */

    /**
     * init locale spinner
     */
    private void configureLocale() {
        // init
        var pref = AndroidSettings.LOCALE_PREF(this);
        var spinner = this.<Spinner>findViewById(R.id.locale);

        // populate available
        var locales = AndroidSettings.getLocales(this);
        var adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locales
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // select current option
        for (int i = 0; i < locales.size(); i++) {
            if (Objects.equals(locales.get(i).tag, pref.get())) spinner.setSelection(i);
        }

        // add listener to auto-change it
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // set+notify if changed
                if (!Objects.equals(pref.get(), locales.get(i).tag)) {
                    pref.set(locales.get(i).tag);
                    AndroidSettings.reload(SettingsActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /* ------------------- tutorial ------------------- */

    public void openTutorial(View view) {
        PackageUtils.startActivity(new Intent(this, TutorialActivity.class), R.string.toast_noApp, this);
    }
}
