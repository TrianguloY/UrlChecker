package com.trianguloy.forceurl.data;

import android.content.Context;
import android.content.Intent;

import java.util.Set;

/**
 * Represents either a package or a package group (apps), which need additional tweaking
 * to open the intent in a specific way (i.e. incognito). These might need additional
 * help to be opened.
 * <p>
 * Must follow the hierarchy data.mode.group.Apps
 */
public interface Apps {
    /**
     * Returns an id to identify this app/fork
     */
    static String getId(Apps app) {
        var sep = ".";
        var clazz = app.getClass();
        var pckgParts = clazz.getName().split("\\.");
        return pckgParts[pckgParts.length - 3] + sep +
                pckgParts[pckgParts.length - 2] + sep +
                pckgParts[pckgParts.length - 1] + sep;
        // i.e. "incognito.forks.Chromium"
    }

    /**
     * Checks if the app/fork is the same as the one we want to open
     */
    boolean isThis(Context cntx, String pckg);

    /**
     * Applies the necessary changes so it opens as we need.
     *
     * @param intent
     * @return If the app needs help to input the URL
     */
    boolean transform(Intent intent);

    /**
     * @return If the app needs help to input the URL
     */
    boolean needsHelp();

    /**
     * @return A read only set of extras related to opening the app in this mode
     */
    Set<String> getExtras();

}