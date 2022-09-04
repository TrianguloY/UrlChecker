package com.trianguloy.urlchecker.modules;

import android.app.Activity;

import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.url.UrlData;
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
     *
     * @param urlData the new url
     */
    public abstract void onNewUrl(UrlData urlData);

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
     * Changes the current url. (no extra data)
     *
     * @param url new url
     */
    protected final void setUrl(String url) {
        setUrl(new UrlData(url));
    }

    /**
     * Changes the current url.
     *
     * @param urlData new url and data
     */
    protected final void setUrl(UrlData urlData) {
        urlData.trigger = this;
        dialog.onNewUrl(urlData);
    }

}
