package com.trianguloy.urlchecker.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import com.trianguloy.urlchecker.R;

public interface AndroidSettings {

    /* ------------------- day/light theme ------------------- */

    /**
     * The theme setting
     */
    enum Theme implements TranslatableEnum {
        DEFAULT(0, R.string.spin_defaultTheme),
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
    static void setTheme(Activity activity, boolean dialog) {
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

    static void reload(Activity cntx) {
        Log.d("SETTINGS", "reloading");
        cntx.recreate();
    }
}
