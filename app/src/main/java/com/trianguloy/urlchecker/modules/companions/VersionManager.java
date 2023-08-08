package com.trianguloy.urlchecker.modules.companions;

import android.content.Context;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.activities.TutorialActivity;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.JavaUtils;

/**
 * Manages the app version, to notify of updates
 */
public class VersionManager {

    private final GenericPref.Str lastVersion;

    public static GenericPref.Str LASTVERSION_PREF(Context cntx) {
        return new GenericPref.Str("changelog_lastVersion", null, cntx);
    }

    /**
     * Check if the version must be updated
     */
    public static void check(Context cntx) {
        // just call the constructor, it does the check
        new VersionManager(cntx);
    }

    public VersionManager(Context cntx) {
        lastVersion = LASTVERSION_PREF(cntx);
        if (lastVersion.get() == null) {
            // no previous setting, the app is a new install, mark as seen
            // ... or maybe it was updated from an old version (where the setting was not yet implemented)
            // In that case fake a 'previous' version
            // we check by testing the tutorial flag (which should be set if the app was used)
            if (TutorialActivity.DONE(cntx).get()) lastVersion.set("0");
            else markSeen();
        }
    }

    /**
     * returns true iff the app was updated since last time it was used
     */
    public boolean wasUpdated() {
        var last = lastVersion.get();
        var current = BuildConfig.VERSION_NAME;
        if (last.equals(current)) return false; // early exit: same version = no update

        // check
        return JavaUtils.compareArrays(parseVersion(last), parseVersion(current)) < 0;
    }

    /**
     * Marks the current version as seen (wasUpdated will return false until a new update happens)
     */
    public void markSeen() {
        lastVersion.set(BuildConfig.VERSION_NAME);
    }

    /**
     * extracts the version numbers:
     * 1.2.3-alpha -> [1, 2, 3]
     */
    private int[] parseVersion(String version) {
        var parts = version.split("\\.");
        var parsed = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                parsed[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                parsed[i] = 0; // failsafe
            }
        }
        return parsed;
    }
}
