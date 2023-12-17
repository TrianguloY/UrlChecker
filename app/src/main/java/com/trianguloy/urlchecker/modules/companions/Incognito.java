package com.trianguloy.urlchecker.modules.companions;

import static com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelper.compatibility.compatible;
import static com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelper.compatibility.notCompatible;
import static com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelper.compatibility.urlNeedsHelp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelper;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages the incognito feature
 */
public class Incognito {

    /**
     * preference
     */
    public static GenericPref.Enumeration<OnOffConfig> PREF(Context cntx) {
        return new GenericPref.Enumeration<>("open_incognito", OnOffConfig.AUTO, OnOffConfig.class, cntx);
    }

    private final GenericPref.Enumeration<OnOffConfig> pref;
    private boolean state = false;

    public Incognito(Context cntx) {
        this.pref = PREF(cntx);
    }


    private static final Set<String> possibleExtras = new HashSet<>();
    private static final Map<String, JavaUtils.BiFunction<Context, Intent, Boolean>>
            findPackage = new HashMap<>();
    private static final Map<String, JavaUtils.Function<Intent, Boolean>>
            transform = new HashMap<>();

    static {
        // There are 2 functions:
        //      - One checks if the app/fork is the same as the one we want to open in incognito
        //      - The other applies the necessary changes so it opens on incognito, it returns if
        //          the app needs help to input the URL

        // fenix (firefox)
        {
            String key = "fenix";
            // https://bugzilla.mozilla.org/show_bug.cgi?id=1807531
            // https://github.com/search?q=repo%3Amozilla-mobile%2Ffirefox-android+PRIVATE_BROWSING_MODE&type=code
            String extra = "private_browsing_mode";
            possibleExtras.add(extra);

            // all firefox apps share the same home activity
            String activity = "org.mozilla.fenix.HomeActivity";
            // exclude tor browser, as it is always in incognito
            Set<String> exclude = new HashSet<>();
            exclude.add("org.torproject.torbrowser");
            JavaUtils.BiFunction<Context, Intent, Boolean> isThis = (context, intent) -> {
                String pckg = intent.getPackage();
                if (exclude.contains(pckg)) return false;
                Set<String> activities = AndroidUtils.getActivities(context, pckg);
                return activities.contains(activity);
            };

            JavaUtils.Function<Intent, Boolean> setIncognito = (intent) -> {
                intent.putExtra("private_browsing_mode", true);
                return false;
            };

            findPackage.put(key, isThis);
            transform.put(key, setIncognito);
        }

        //chromium (chrome)
        {
            String key = "chromium";
            // https://source.chromium.org/chromium/chromium/src/+/refs/heads/main:chrome/android/java/src/org/chromium/chrome/browser/IntentHandler.java;l=346;drc=fb3fab0be2804a2864783c326518f1acb0402968
            String extra = "com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB";
            String extra2 = "EXTRA_OPEN_NEW_INCOGNITO_TAB"; // just in case, but this shouldn't be needed
            possibleExtras.add(extra);
            possibleExtras.add(extra2);

            // all chromium apps have the same main activity
            String activity = "com.google.android.apps.chrome.Main";
            JavaUtils.BiFunction<Context, Intent, Boolean> isThis = (context, intent) -> {
                String pckg = intent.getPackage();
                String mainActivity = AndroidUtils.getMainActivity(context, pckg);
                return mainActivity.equals(activity);
            };

            // extras do not work in chromium
            // https://source.chromium.org/chromium/chromium/src/+/refs/heads/main:chrome/android/java/src/org/chromium/chrome/browser/IntentHandler.java;drc=e39fffa6900a679961f5992b8f24a084853b811a;l=1036
            // https://source.chromium.org/chromium/chromium/src/+/refs/heads/main:chrome/android/java/src/org/chromium/chrome/browser/IntentHandler.java;l=988;drc=fb3fab0be2804a2864783c326518f1acb0402968
            JavaUtils.Function<Intent, Boolean> setIncognito = (intent) -> {
                intent.setComponent(new ComponentName(intent.getPackage(), "org.chromium.chrome.browser.incognito.IncognitoTabLauncher"));
                return true;
            };

            findPackage.put(key, isThis);
            transform.put(key, setIncognito);
        }
    }

    /**
     * Initialization from a given intent and a button to toggle
     */
    public void initFrom(Intent intent, ImageButton button) {
        // init state
        boolean visible;
        switch (pref.get()) {
            case AUTO:
            default:
                state = isIncognito(intent);
                visible = true;
                break;
            case HIDDEN:
                state = isIncognito(intent);
                visible = false;
                break;
            case DEFAULT_ON:
                state = true;
                visible = true;
                break;
            case DEFAULT_OFF:
                state = false;
                visible = true;
                break;
            case ALWAYS_ON:
                state = true;
                visible = false;
                break;
            case ALWAYS_OFF:
                state = false;
                visible = false;
                break;
        }

        // init button
        if (visible) {
            // show and configure
            button.setVisibility(View.VISIBLE);
            AndroidUtils.longTapForDescription(button);
            AndroidUtils.toggleableListener(button,
                    imageButton -> state = !state,
                    v -> v.setImageResource(state ? R.drawable.incognito : R.drawable.no_incognito)
            );
        } else {
            // hide
            button.setVisibility(View.GONE);
        }
    }

    /**
     * Returns if an intent will launch incognito for a given intent/app, only checks extras
     */
    private boolean isIncognito(Intent intent) {
        boolean incognito = false;
        // Find any extra
        for (String extra : possibleExtras) {
            incognito = incognito | intent.getBooleanExtra(extra, false);
        }

        return incognito;
    }


    /**
     * Cleans the intent before applying incognito
     */
    private void removeIncognito(Intent intent) {
        // Mimics the matching in isIncognito
        for (String extra : possibleExtras) {
            // remove all incognito extras
            intent.removeExtra(extra);
        }
    }


    /**
     * applies the setting to a given intent
     */
    public UrlHelper.compatibility apply(Context context, Intent intent) {
        removeIncognito(intent);
        if (state) {
            for (var entry : findPackage.entrySet()) {
                if (entry.getValue().apply(context, intent)) {
                    if (transform.get(entry.getKey()).apply(intent)) {
                        return urlNeedsHelp;
                    }
                    return compatible;
                }
            }

        }
        return notCompatible;
    }
}
