package com.trianguloy.urlchecker.utilities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Generic Android utilities
 */
public interface AndroidUtils {

    /**
     * Sets the start drawable of a textview
     * Wrapped for android compatibility
     */
    static void setStartDrawables(TextView txt, int start) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // we can use the function directly!
            txt.setCompoundDrawablesRelativeWithIntrinsicBounds(start, 0, 0, 0);
        } else {
            // we need to manually adjust
            if ((txt.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK) == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL) {
                // rtl
                txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, start, 0);
            } else {
                // ltr
                txt.setCompoundDrawablesWithIntrinsicBounds(start, 0, 0, 0);
            }
        }
    }

    /**
     * In debug mode, throws an AssertionError, in production just logs it and continues.
     */
    static void assertError(String detailMessage) {
        Log.d("ASSERT_ERROR", detailMessage);
        if (BuildConfig.DEBUG) {
            // in debug mode, throw exception
            throw new AssertionError(detailMessage);
        }
        // non-debug, just discard
    }

    /**
     * Sets the background color (resource id) of a view using a rounded box drawable
     */
    static void setRoundedColor(int color, View view) {
        setRawRoundedColor(view.getContext().getResources().getColor(color), view);
    }

    /**
     * Sets the background color (raw color) of a view using a rounded box drawable
     */
    static void setRawRoundedColor(int color, View view) {
        var drawable = view.getContext().getResources().getDrawable(R.drawable.round_box);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC);
        view.setBackgroundDrawable(drawable);
    }

    /**
     * Clears the background color of a view
     */
    static void clearRoundedColor(View view) {
        view.setBackgroundDrawable(null);
    }

    /**
     * Makes the text of a textview display as a link (which does nothing when clicked)
     */
    static void setAsClickable(TextView textview) {
        SpannableStringBuilder text = new SpannableStringBuilder(textview.getText());
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View ignored) {
                // do nothing
            }
        }, 0, text.length(), 0);
        textview.setText(text);
    }

    /**
     * Returns a formatted date/time from epoch milliseconds according to the context locale.
     * If the date is invalid (negative), "---" is returned instead
     */
    static String formatMillis(long millis, Context cntx) {
        if (millis < 0) return "---";

        return DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        ? cntx.getResources().getConfiguration().getLocales().get(0)
                        : cntx.getResources().getConfiguration().locale
        ).format(new Date(millis));
    }

    /**
     * Copy to the clipboard, retrieves string from id
     */
    static void copyToClipboard(Activity activity, int id, String text) {
        copyToClipboard(activity, activity.getString(id), text);
    }

    /**
     * Copy to the clipboard
     */
    static void copyToClipboard(Activity activity, String toast, String text) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return;

        clipboard.setPrimaryClip(ClipData.newPlainText("", text));

        // show toast to notify it was copied (except on Android 13+, where the device shows a popup itself)
        if (Build.VERSION.SDK_INT < /*Build.VERSION_CODES.TIRAMISU*/33)
            Toast.makeText(activity, toast, Toast.LENGTH_LONG).show();
    }

    /**
     * Get the (possible) referrer activity from an existing one.
     * Null if can't find
     */
    static String getReferrer(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return null;

        Uri referrer = activity.getReferrer();
        if (referrer == null) return null;

        // the scheme must be "android-app"
        if (!"android-app".equals(referrer.getScheme())) return null;
        // the host is the package
        return referrer.getHost();
    }

    /**
     * @see ActionBar#setDisplayHomeAsUpEnabled(boolean)
     * And don't forget to override onOptionsItemSelected!
     */
    static void configureUp(Activity activity) {
        var actionBar = activity.getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
