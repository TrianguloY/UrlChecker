package com.trianguloy.urlchecker.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.fragments.ResultCodeInjector;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface AndroidSettings {

    /* ------------------- day/light theme ------------------- */

    /**
     * The theme setting
     */
    enum Theme implements Enums.IdEnum, Enums.StringEnum {
        DEFAULT(0, R.string.deviceDefault),
        DARK(1, R.string.spin_darkTheme),
        LIGHT(2, R.string.spin_lightTheme),
        ;

        private final int id;
        private final int string;

        Theme(int id, int string) {
            this.id = id;
            this.string = string;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getStringResource() {
            return string;
        }
    }

    /**
     * The theme pref
     */
    static GenericPref.Enumeration<Theme> THEME_PREF(Context cntx) {
        return new GenericPref.Enumeration<>("dayNight", Theme.DEFAULT, Theme.class, cntx);
    }

    /**
     * Sets the theme (light/dark mode) to an activity
     */
    static void setTheme(Context activity, boolean dialog) {
        int style;
        switch (THEME_PREF(activity).get()) {
            case DEFAULT:
            default:
                if (!dialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // is dayNight different to manually choosing dark or light? no idea, but it exists so...
                    style = R.style.ActivityThemeDayNight;
                    break;
                }

                style = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO
                        ? (dialog ? R.style.DialogThemeLight : R.style.ActivityThemeLight) // explicit light mode (uiMode=NIGHT_NO)
                        : (dialog ? R.style.DialogThemeDark : R.style.ActivityThemeDark) // dark mode or device default
                ;
                break;
            case DARK:
                style = (dialog ? R.style.DialogThemeDark : R.style.ActivityThemeDark);
                break;
            case LIGHT:
                style = (dialog ? R.style.DialogThemeLight : R.style.ActivityThemeLight);
                break;
        }

        // set
        activity.setTheme(style);
    }

    /* ------------------- reloading ------------------- */

    String RELOAD_EXTRA = "reloaded";
    int RELOAD_RESULT_CODE = Activity.RESULT_FIRST_USER;

    /**
     * destroys and recreates the activity (to apply changes) and marks it
     */
    static void reload(Activity cntx) {
        Log.d("SETTINGS", "reloading");
        cntx.getIntent().putExtra(RELOAD_EXTRA, true); // keep data
        cntx.recreate();
    }

    /**
     * Returns true if the activity was reloaded (with {@link AndroidSettings#reload}) and clears the flag
     */
    static boolean wasReloaded(Activity cntx) {
        var intent = cntx.getIntent();
        var reloaded = intent.getBooleanExtra(RELOAD_EXTRA, false);
        intent.removeExtra(RELOAD_EXTRA);
        return reloaded;
    }

    /**
     * Registers an activity result to reload if the launched activity is marked as reloading using{@link AndroidSettings#markForReloading(Activity)}
     */
    static int registerForReloading(ResultCodeInjector resultCodeInjector, Activity cntx) {
        return resultCodeInjector.registerActivityResult((resultCode, data) -> {
            if (resultCode == RELOAD_RESULT_CODE) {
                AndroidSettings.reload(cntx);
            }
        });
    }

    /**
     * Makes the activity that launched this one to reload, if registered with {@link AndroidSettings#registerForReloading(ResultCodeInjector, Activity)}
     */
    static void markForReloading(Activity cntx) {
        cntx.setResult(RELOAD_RESULT_CODE);
    }

}
