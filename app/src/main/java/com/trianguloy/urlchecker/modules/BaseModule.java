package com.trianguloy.urlchecker.modules;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.trianguloy.urlchecker.MainDialog;

/**
 * Base class for a module.
 * All modules need to have {@link #registerDialog(MainDialog)} and {@link #initialize(View)} called before use
 */
public abstract class BaseModule {

    // ------------------- initialization -------------------

    /**
     * Initializes this module by registering this dialog
     *
     * @param cntx MainDialog
     */
    public void registerDialog(MainDialog cntx) {
        this.dialog = cntx;
    }

    // ------------------- abstract functions -------------------

    /**
     * @return the name of this module (shown to the user)
     */
    public abstract String getName();

    /**
     * @return the layout resource of this module
     */
    public abstract int getLayoutBase();

    /**
     * Initializes this module from the given views (generated from {@link #getLayoutBase()})
     *
     * @param views
     */
    public abstract void initialize(View views);

    /**
     * Notification of a new url, on this callback you can't call {@link #setUrl(String)}
     *
     * @param url the new url
     */
    public abstract void onNewUrl(String url);

    // ------------------- protected utilities -------------------

    /**
     * @return this activity context
     */
    protected Activity getActivity() {
        return dialog;
    }

    /**
     * @return the current url
     */
    protected String getUrl() {
        return dialog.getUrl();
    }

    /**
     * Changes the current url
     *
     * @param url new url
     */
    protected void setUrl(String url) {
        dialog.setUrl(url, this);
    }

    // ------------------- private data -------------------

    private MainDialog dialog;
}
