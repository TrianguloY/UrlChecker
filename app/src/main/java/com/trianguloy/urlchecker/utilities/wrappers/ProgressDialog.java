package com.trianguloy.urlchecker.utilities.wrappers;

import android.app.Activity;

/**
 * A wrapper around {@link android.app.ProgressDialog} with more useful functions
 */
public class ProgressDialog extends android.app.ProgressDialog {
    private final Activity cntx;

    /**
     * Constructs and shows the dialog
     */
    public ProgressDialog(Activity context, String title) {
        super(context);
        cntx = context;
        setTitle(title); // can't be changed later
        setMessage(""); // otherwise the message view can't be changed later
        setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL); // with spinner, indeterminate mode can't be changed
        setCancelable(false); // disable cancelable by default
        setCanceledOnTouchOutside(false);

        // show immediately
        show();
    }

    /**
     * progress++
     */
    public void increaseProgress() {
        setProgress(getProgress() + 1);
    }

    /**
     * sets max value and resets to 0
     */
    @Override
    public void setMax(int max) {
        super.setMax(max);
        setProgress(0);
    }

    /**
     * Changes the message from any thread
     */
    @Override
    public void setMessage(CharSequence message) {
        cntx.runOnUiThread(() -> super.setMessage(message));
    }
}
