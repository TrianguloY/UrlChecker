package com.trianguloy.urlchecker.modules;

import android.app.Activity;

import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.utilities.Fragment;

/**
 * Base class for a module.
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
     * On this callback you can't call {@link #setUrl(String)}
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
     * Changes the current url
     *
     * @param url new url
     */
    protected final void setUrl(String url) {
        dialog.setUrl(url, this);
    }

}
