package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import android.content.ClipData;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;

public class ClipboardBorrower {
    private volatile static ClipData previous;
    private volatile static ClipData borrowerData;
    private static final String LABEL = "clipboard_borrower_text";

    private static void clear() {
        previous = null;
        borrowerData = null;
    }

    // TODO: maybe notifications allows the app to read the clipboard even in background

    /**
     * {@link #borrow(Context, ClipData)} but we provide the label {@value LABEL}
     *
     * @param text The text which we will put into the clipboard
     */
    public static boolean borrow(Context context, String text) {
        return borrow(context, ClipData.newPlainText(LABEL, text));
    }

    /**
     * Puts content into the clipboard for the user to use. Can be called multiple times before
     * {@link #release}.
     *
     * @param borrowerData The content which we will put into the clipboard
     */
    public synchronized static boolean borrow(Context context, ClipData borrowerData) {
        if (store(context)) {
            ClipboardBorrower.borrowerData = borrowerData;
            AndroidUtils.setPrimaryClip(context, R.string.borrow_borrowSet, ClipboardBorrower.borrowerData);
            return true;
        } else {
            // FIXME: Not needed, androidutils already shows a toast, delete or move borrow_borrowError
            //Toast.makeText(context, R.string.borrow_borrowError, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * Restores the content of the clipboard. If the user has copied something to the clipboard
     * between the first call to {@link #borrow} and this, we will restore that content instead.
     * If the user has copied multiple things in between calls we will restore the last one.
     */
    public synchronized static boolean release(Context context) {
        // XXX: As a side effect, this makes technically possible to call release without borrowing,
        // as the method also initializes things, in practise it doesn't make sense and it achieves
        // nothing
        boolean success = store(context);
        if (success) {
            AndroidUtils.setPrimaryClip(context, R.string.borrow_releaseSet, previous);
        } else {
            // FIXME: Not needed, androidutils already shows a toast, delete or move borrow_releaseError
            //Toast.makeText(context, R.string.borrow_releaseError, Toast.LENGTH_LONG).show();
        }
        clear();
        return success;
    }

    /**
     * A way to use the {@link #release(Context)} method from the background in higher android
     * versions (10+).
     * <p>
     * Relies on window drawing, so it requires the SYSTEM_ALERT_WINDOW permission. Because it will
     * be run as {@link android.view.ViewTreeObserver.OnPreDrawListener}, it won't be done
     * immediately.
     * <p>
     * Has some problems, i.e. if the user is scrolling we will get focus for an instant, so we
     * steal the scrolling from the user.
     */
    public static void releaseFromBubble(Context context) {
        var windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        var floatView = LayoutInflater.from(context).inflate(R.layout.bubble, null);
        var layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                0,
                0,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        LayoutParams.TYPE_APPLICATION_OVERLAY :
                        LayoutParams.TYPE_TOAST,
                LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        floatView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                release(floatView.getContext());
                floatView.getViewTreeObserver().removeOnPreDrawListener(this);
                windowManager.removeView(floatView);
                return false;
            }
        });

        AndroidUtils.runOnUiThread(() -> {
            windowManager.addView(floatView, layoutParams);
        });
    }

    /**
     * Will choose between {@link #release(Context)} and its wrappers so that it selects the best
     * for the current moment, avoiding unnecessary compatibility layers. What this means is that it
     * won't call a background wrapper when in focus and things like that.
     *
     * @return {@link AndroidUtils#canUseClip(Context)}
     */
    public synchronized static boolean releaseSmart(Context context) {
        var canUseClip = AndroidUtils.canUseClip(context);
        if (canUseClip) {
            release(context);
        } else {
            releaseFromBubble(context);
        }
        return canUseClip;
    }

    /**
     * To keep the last thing the user copied to the clipboard
     *
     * @return if the clipboard is accessible
     */
    private synchronized static boolean store(Context context) {
        // Retrieve clip data
        ClipData current = AndroidUtils.getPrimaryClip(context, R.string.borrow_storeGet);

        // Clipboard not accessible
        if (current == null) {
            return false;
        }

        // If it is the first time this method is called in this lifecycle
        if (previous == null) {
            // Store clip data
            previous = current;
        } else {
            // XXX: For debugging purposes: emulator shared clipboard may trip this up, as it is
            //  constantly syncing and messes up the label.
            //  It is recommended to disable it.
            //      - Android studio: File -> Settings -> Tools -> Emulator -> Enable clipboard sharing
            //
            //  Still it didn't work for me and it kept changing the label

            // Use case where the label is relevant:
            //   User opens URL in incognito, pastes URL, then remembers it wants to copy it to the
            //   clipboard so it copies it. Now the label is different, but the text is not.
            if (BuildConfig.DEBUG) {
                // In debug we DO NOT check the label
                if (current.getDescription().getMimeType(0).equals("text/plain")) {
                    var currentText = current.getItemAt(0).coerceToText(context);
                    var borrowerText = ClipboardBorrower.borrowerData.getItemAt(0).coerceToText(context);
                    if (!currentText.equals(borrowerText)) {
                        previous = current;
                    }
                }
            } else {
                // On release we DO check the label
                if (!current.equals(ClipboardBorrower.borrowerData)) {
                    previous = current;
                }
            }
        }
        return true;
    }
}
