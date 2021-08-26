package com.trianguloy.urlchecker.modules;

import android.app.Activity;

import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.utilities.Fragment;

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
     * On this callback you can't call {@link #updateUrl(String)}
     *
     * @param url the new url
     */
    public abstract void onNewUrl(String url);

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
     * Changes the current url.
     * All the other modules are notified, BUT NOT THE CALLER
     *
     * @param url new url
     * @see AModuleDialog#setUrl(String)
     */
    protected final void updateUrl(String url) {
        dialog.setUrl(url, this);
    }

    /**
     * Changes the current url.
     * All the modules are notified, INCLUDING THE CALLER
     *
     * @param url new url
     * @see AModuleDialog#updateUrl(String)
     */
    protected final void setUrl(String url) {
        dialog.setUrl(url, null);
    }

}
