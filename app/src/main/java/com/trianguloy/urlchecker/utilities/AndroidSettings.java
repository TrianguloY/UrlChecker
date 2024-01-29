package com.trianguloy.urlchecker.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.fragments.ActivityResultInjector;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface AndroidSettings {

    /* ------------------- locale ------------------- */

    /**
     * The locale pref
     */
    static GenericPref.Str LOCALE_PREF(Context cntx) {
        return new GenericPref.Str("locale", "", cntx);
    }

    /**
     * Sets the locale to an activity
     */
    static void setLocale(Activity cntx) {
        cntx.getResources().updateConfiguration(
                getConfig(parseLocale(LOCALE_PREF(cntx).get())),
                cntx.getResources().getDisplayMetrics()
        );
    }

    /**
     * Container for a locale with a given name
     */
    class AvailableLocale {
        public final String tag;
        public final Locale locale;
        public final String name;

        public AvailableLocale(String tag, Locale locale, String name) {
            this.tag = tag;
            this.locale = locale;
            this.name = name;
        }

        public AvailableLocale(String name) {
            this.tag = "";
            this.locale = null;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + (tag.isEmpty() ? "" : " (" + tag + ")");
        }
    }

    /**
     * Returns the list of available/installed locales (plus a 'default' at the top)
     */
    static List<AvailableLocale> getLocales(Context cntx) {
        // check each locale
        var available = new ArrayList<AvailableLocale>();
        for (var tag : BuildConfig.LOCALES) {
            var locale = parseLocale(tag);

            // check if available on this device (with split apks the device may not have the translation downloaded)
            var localeName = getStringForLocale(R.string.locale, locale, cntx);
            if (available.isEmpty() || !available.get(0).name.equals(localeName)) {
                // either english (first one) or a translation exists
                // note that translations may not exists because PlayStore only installs locales configured by the user
                available.add(new AvailableLocale(tag, locale, localeName));
            } else {
                if (BuildConfig.DEBUG) Log.d("LOCALE", "Locale " + tag + " is not present");
            }
        }
        Collections.sort(available, (a, b) -> a.toString().compareTo(b.toString()));
        available.add(0, new AvailableLocale(cntx.getString(R.string.deviceDefault)));
        return available;
    }

    /**
     * returns a specific string in a specific locale
     */
    static String getStringForLocale(int id, Locale locale, Context cntx) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return cntx.createConfigurationContext(getConfig(locale)).getString(id);
        } else {
            return new Resources(cntx.getAssets(), cntx.getResources().getDisplayMetrics(), getConfig(locale)).getString(id);
        }
    }

    /**
     * returns a configuration object for the given locale
     */
    static Configuration getConfig(Locale locale) {
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        return config;
    }

    /**
     * returns a locale for the given tag (language[-country[-variant]])
     */
    static Locale parseLocale(String locale) {
        if (locale.isEmpty()) return null;
        try {
            var parts = locale.split("-");
            return new Locale(parts[0], parts.length > 1 ? parts[1] : "", parts.length > 2 ? parts[2] : "");
        } catch (Exception e) {
            return null;
        }
    }

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
    static int registerForReloading(ActivityResultInjector activityResultInjector, Activity cntx) {
        return activityResultInjector.register((resultCode, data) -> {
            if (resultCode == RELOAD_RESULT_CODE) {
                AndroidSettings.reload(cntx);
            }
        });
    }

    /**
     * Makes the activity that launched this one to reload, if registered with {@link AndroidSettings#registerForReloading(ActivityResultInjector, Activity)}
     */
    static void markForReloading(Activity cntx) {
        cntx.setResult(RELOAD_RESULT_CODE);
    }

}
