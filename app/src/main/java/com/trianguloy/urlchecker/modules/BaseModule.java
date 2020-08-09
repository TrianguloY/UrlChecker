package com.trianguloy.urlchecker.modules;

import android.app.Activity;
import android.view.View;

import com.trianguloy.urlchecker.dialogs.MainDialog;

/**
 * Base class for a module.
 */
public abstract class BaseModule {

    // ------------------- private data -------------------

    private final MainDialog dialog;

    // ------------------- initialization -------------------

    public BaseModule(MainDialog dialog) {
        this.dialog = dialog;
    }

    // ------------------- abstract functions -------------------

    /**
     * @return the layout resource of this module
     */
    public abstract int getLayoutBase();

    /**
     * Notification of a new url.
     * On this callback you can't call {@link #setUrl(String)}
     *
     * @param url the new url
     */
    public abstract void onNewUrl(String url);

    /**
     * Initializes this module from the given views (generated from {@link #getLayoutBase()})
     *
     * @param views the inflated views
     */
    public abstract void onInitialize(View views);

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
