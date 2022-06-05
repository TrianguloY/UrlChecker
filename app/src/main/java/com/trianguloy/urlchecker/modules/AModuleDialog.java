package com.trianguloy.urlchecker.modules;

import android.app.Activity;

import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.utilities.Fragment;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Base class for a module's dialog fragment.
 */
public abstract class AModuleDialog implements Fragment {

    // ------------------- private data -------------------

    private final MainDialog dialog;

    // ------------------- initialization -------------------

    public AModuleDialog(MainDialog dialog) {
        this.dialog = dialog;
    }

    // ------------------- abstract functions -------------------

    /**
     * Notification of a new url.
     * On this callback you can't call {@link #setUrl(String, Flags...)}
     *
     * @param url         the new url
     * @param minorUpdate if true, the new url is considered a minor update
     */
    public abstract void onNewUrl(String url, boolean minorUpdate);

    // ------------------- utilities -------------------

    /**
     * @return this activity context
     */
    protected final Activity getActivity() {
        return dialog;
    }

    /**
     * @return the current url
     */
    protected final String getUrl() {
        return dialog.getUrl();
    }

    /**
     * Updates flags
     */
    public enum Flags {
        /**
         * A flag that means 'no flag', for convenience
         */
        NONE,
        /**
         * If set, the module that triggers the update will NOT be notified
         */
        DONT_NOTIFY_OWN,
        /**
         * If set, the url will not be changed (future setUrl calls will be ignored)
         */
        DISABLE_UPDATE,
        /**
         * If set, this update is considered 'minor' and modules may decide to ignore or merge it with the previous one
         */
        MINOR_UPDATE
    }

    /**
     * Changes the current url.
     *
     * @param url   new url
     * @param flags updating flags
     */
    protected final void setUrl(String url, Flags... flags) {
        dialog.onNewUrl(url, this,
                flags.length == 0 ? EnumSet.noneOf(Flags.class) : EnumSet.copyOf(Arrays.asList(flags))
        );
    }

}
