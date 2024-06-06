package com.trianguloy.urlchecker.modules.companions;

import static com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelperCompanion.Compatibility.compatible;
import static com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelperCompanion.Compatibility.notCompatible;
import static com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelperCompanion.Compatibility.urlNeedsHelp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageButton;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelperCompanion;
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
    private static final Map<String, JavaUtils.TriFunction<AccessibilityNodeInfo, String, String, Boolean>>
            accessibilityFunction = new HashMap<>();

    static {
        // There are 3 functions:
        //      - findPackage/isThis: Checks if the app/fork is the same as the one we want to open
        //          in incognito
        //      - transform/setIncognito: Applies the necessary changes so it opens in incognito.
        //          Returns if the app needs help to input the URL
        //      - accessibilityFunctions/putUrl: Get a function that will help the UrlHelperService
        //          put the URL in the search bar or similar, only needed when an url needs help.
        //          Will be applied multiple times until the service is closed.
        //          Returns true when finished.

        // fenix (firefox)
        {
            String key = "fenix";
            // https://bugzilla.mozilla.org/show_bug.cgi?id=1807531
            // https://github.com/search?q=repo%3Amozilla-mobile%2Ffirefox-android+PRIVATE_BROWSING_MODE&type=code
            String extra = "private_browsing_mode";
            possibleExtras.add(extra);

            // -- CAN BE INCOGNITO --
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

            // -- APPLY INCOGNITO --
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

            // -- CAN BE INCOGNITO --
            // all chromium apps have the same main activity
            String activity = "com.google.android.apps.chrome.Main";
            JavaUtils.BiFunction<Context, Intent, Boolean> isThis = (context, intent) -> {
                String pckg = intent.getPackage();
                String mainActivity = AndroidUtils.getMainActivity(context, pckg);
                return mainActivity.equals(activity);
            };

            // -- APPLY INCOGNITO --
            // extras do not work in chromium
            // https://source.chromium.org/chromium/chromium/src/+/refs/heads/main:chrome/android/java/src/org/chromium/chrome/browser/IntentHandler.java;drc=e39fffa6900a679961f5992b8f24a084853b811a;l=1036
            // https://source.chromium.org/chromium/chromium/src/+/refs/heads/main:chrome/android/java/src/org/chromium/chrome/browser/IntentHandler.java;l=988;drc=fb3fab0be2804a2864783c326518f1acb0402968
            JavaUtils.Function<Intent, Boolean> setIncognito = (intent) -> {
                intent.setComponent(new ComponentName(intent.getPackage(), "org.chromium.chrome.browser.incognito.IncognitoTabLauncher"));
                // I got a case in API 30 where without this flag, the activity wouldn't launch
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
                return true;
            };

            // -- ACCESSIBILITY SERVICE --
            // put URL in search bar, tap it, then tap first result of dropdown
            JavaUtils.TriFunction<AccessibilityNodeInfo, String, String, Boolean> putUrl = (rootNode, pckg, url) -> {
                var searchBar = JavaUtils.getSafe(
                        rootNode.findAccessibilityNodeInfosByViewId(
                                pckg + ":id/url_bar"),
                        0);
                var incognitoBadge = JavaUtils.getSafe(
                        rootNode.findAccessibilityNodeInfosByViewId(
                                pckg + ":id/location_bar_incognito_badge"),
                        0);
                if (incognitoBadge != null && searchBar != null) {
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, url);
                    searchBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    searchBar.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    var enter = JavaUtils.getSafe(
                            rootNode.findAccessibilityNodeInfosByViewId(
                                    pckg + ":id/line_1"),
                            0);
                    if (enter != null) {
                        enter.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return true;
                    }
                }
                return false;
            };

            findPackage.put(key, isThis);
            transform.put(key, setIncognito);
            accessibilityFunction.put(key, putUrl);
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
    public static boolean isIncognito(Intent intent) {
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
     * Applies {@link #apply(Context, Intent)} to a copy of the intent to simulate it.
     * <p>
     * If you want to use {@link #apply(Context, Intent)} based on the return value of this function,
     * it is recommended to, instead, copy the intent yourself and then {@link #apply(Context, Intent)}
     * to it, now you have the original intent, the modified one and the return value, without using
     * {@link #apply(Context, Intent)} twice.
     */
    public UrlHelperCompanion.Compatibility willNeedHelp(Context context, Intent intent) {
        Intent simulation = new Intent(intent);
        return apply(context, simulation);
    }

    /**
     * Applies the setting to a given intent
     */
    public UrlHelperCompanion.Compatibility apply(Context context, Intent intent) {
        // FIXME: ctabs compatibility
        removeIncognito(intent);
        if (state) {
            var key = getKey(context, intent);
            if (key != null) {
                // Package can be opened in incognito

                // Apply transformation, the function also tells us if we will need help
                // to input the URL
                return transform.get(key).apply(intent) ?
                        urlNeedsHelp : compatible;
            }
        }
        return notCompatible;
    }

    /**
     * @return If the app can be opened in incognito, return the key for the dictionaries, null if not
     */
    public static String getKey(Context context, Intent intent) {
        for (var entry : findPackage.entrySet()) {
            if (entry.getValue().apply(context, intent)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static JavaUtils.TriFunction<AccessibilityNodeInfo, String, String, Boolean> getAccessibilityFunction(String key) {
        return accessibilityFunction.get(key);
    }
}
