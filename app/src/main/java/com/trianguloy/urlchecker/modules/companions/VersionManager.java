package com.trianguloy.urlchecker.modules.companions;

import android.app.AlertDialog;
import android.content.Context;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.activities.TutorialActivity;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Manages the app version, to notify of updates
 */
public class VersionManager {

    private final GenericPref.Str lastVersion;

    public static GenericPref.Str LASTVERSION_PREF(Context cntx) {
        return new GenericPref.Str("changelog_lastVersion", null, cntx);
    }

    /* ------------------- static ------------------- */

    /**
     * Check if the version must be updated
     */
    public static void check(Context cntx) {
        // just call the constructor, it does the check
        new VersionManager(cntx);
    }

    /**
     * Returns true iff [version] is newer than the current one
     */
    public static boolean isNewerThanCurrent(String version) {
        // shortcut to check own version
        if (BuildConfig.VERSION_NAME.equals(version)) return false;

        var versionSplit = split(version);
        // invalid version, consider new just in case
        if (versionSplit.isEmpty()) return true;
        // compare: "1" < "2", "1" < "1.1"
        var currentSplit = split(BuildConfig.VERSION_NAME);
        var i = 0;
        while (true) {
            // end of version, version is older (or equal)
            if (versionSplit.size() <= i) return false;
            // end of current, version is newer
            if (currentSplit.size() <= i) return true;
            // version is older
            if (versionSplit.get(i) < currentSplit.get(i)) return false;
            // version is newer
            if (versionSplit.get(i) > currentSplit.get(i)) return true;
            i++;
        }
    }

    /* ------------------- instance ------------------- */

    public VersionManager(Context cntx) {
        lastVersion = LASTVERSION_PREF(cntx);
        if (lastVersion.get() == null) {
            // no previous setting, the app is a new install, mark as seen
            // ... or maybe it was updated from an old version (where the setting was not yet implemented, 2.12 or below)
            // we check by testing the tutorial flag (which should be set if the app was used)
            if (TutorialActivity.DONE(cntx).get()) lastVersion.set("<2.12");
            else markSeen();
        }
    }

    /**
     * returns true iff the app was updated since last time it was used
     */
    public boolean wasUpdated() {
        // just check inequality. If the app was downgraded, you probably also want to be notified.
        return !BuildConfig.VERSION_NAME.equals(lastVersion.get());
    }

    /**
     * Marks the current version as seen (wasUpdated will return false until a new update happens)
     */
    public void markSeen() {
        lastVersion.set(BuildConfig.VERSION_NAME);
    }

    /* ------------------- private ------------------- */

    static private final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

    /**
     * Extracts all numbers from the string: "1.2.34d" -> [1, 2, 34]
     */
    private static List<Integer> split(String version) {
        if (version == null) return Collections.emptyList();
        var matcher = INTEGER_PATTERN.matcher(version);
        var parts = new ArrayList<Integer>();
        while (matcher.find()) parts.add(Integer.parseInt(matcher.group()));
        return parts;
    }
}
