package com.trianguloy.urlchecker.modules.companions;

import android.content.Context;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.activities.TutorialActivity;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;

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
}
