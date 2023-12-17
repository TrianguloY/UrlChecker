package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import android.content.ClipData;
import android.content.Context;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;

public class ClipboardBorrower {
    // There is only one clipboard, so there is only one borrower
    private static ClipboardBorrower instance = null;

    private ClipboardBorrower() {
    }

    public static ClipboardBorrower getClipboardBorrower() {
        if (instance == null) {
            instance = new ClipboardBorrower();
        }
        return instance;
    }

    // Class
    private ClipData previous;
    private ClipData borrowerData;
    private boolean init = false;

    // XXX: For debugging purposes: emulator shared clipboard may trip this up, as it is constantly
    //  syncing and messes up the label.
    //  It is recommended to disable it.
    //      - Android studio: File -> Settings -> Tools -> Emulator -> Enable clipboard sharing
    //
    //  Still it didn't work for me and it kept changing the label
    private static final String label = "clipboard_borrower_text";

    // TODO: Bubbles and maybe notifications allows the app to read the clipboard even in background,
    //  some checks for android versions or the possibility to force compatibility with this would be
    //  great, don't know yet if this is a good place to put these things

    /**
     * Puts content into the clipboard for the user to use. Can be called multiple times before
     * releaseClipboard.
     *
     * @param context
     * @param text    The text which we will put into the clipboard
     */
    public boolean borrowClipboard(Context context, String text) {
        return borrowClipboard(context, ClipData.newPlainText(label, text));
    }

    /**
     * Puts content into the clipboard for the user to use. Can be called multiple times before
     * releaseClipboard.
     *
     * @param context
     * @param borrowerData The content which we will put into the clipboard
     */
    private boolean borrowClipboard(Context context, ClipData borrowerData) {
        if (storeClipboard(context)) {
            this.borrowerData = borrowerData;
            AndroidUtils.setPrimaryClip(context, R.string.borrow_borrowSet, this.borrowerData);
            return true;
        } else {
            Toast.makeText(context, R.string.borrow_borrowError, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * Restores the content of the clipboard. If the user has copied something to the clipboard
     * between the first call to borrowClipboard and this, we will restore that content instead.
     * If the user has copied multiple things in between calls we will restore the last content
     * the user copied.
     */
    public boolean releaseClipboard(Context context) {
        // XXX: As a side effect, this makes technically possible to call release without borrowing,
        // as the method also initializes things, in practise it doesn't make sense and it achieves
        // nothing
        boolean success = storeClipboard(context);
        if (success) {
            AndroidUtils.setPrimaryClip(context, R.string.borrow_releaseSet, previous);
        } else {
            Toast.makeText(context, R.string.borrow_releaseError, Toast.LENGTH_LONG).show();
            // TODO: warn user in description that if they see this message too much (always) they
            //  should not use this borrower setting as it doesn't work in their device
        }
        instance = null; // Kill the borrower
        return success;
    }

    /**
     * To keep the last thing the user copied to the clipboard
     *
     * @return if the clipboard is accessible
     */
    private boolean storeClipboard(Context context) {
        // Retrieve clip data
        ClipData current = AndroidUtils.getPrimaryClip(context, R.string.borrow_storeGet);

        // Clipboard not accessible
        if (current == null) {
            return false;
        }

        // If it is the first time this method is called in this lifecycle
        if (!init) {
            // Store clip data
            previous = current;
            init = true;
        } else {
            final int index = 0;
            if (current.getDescription().getMimeType(index).equals("text/plain")) {
                var currentText = current.getItemAt(index).coerceToText(context);
                var borrowerText = this.borrowerData.getItemAt(index).coerceToText(context);
                if (!currentText.equals(borrowerText)) {
                    previous = current;
                }
            }
        }

        return true;
    }
}
