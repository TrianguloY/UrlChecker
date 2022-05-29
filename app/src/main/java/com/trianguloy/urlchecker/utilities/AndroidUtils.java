package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;

/**
 * Generic Android utilities
 */
public class AndroidUtils {

    /**
     * In debug mode, throws an AssertionError, in production just logs it and continues.
     */
    public static void assertError(String detailMessage) {
        Log.d("ASSERT_ERROR", detailMessage);
        if (BuildConfig.DEBUG) {
            // in debug mode, throw exception
            throw new AssertionError(detailMessage);
        }
        // non-debug, just discard
    }

    /**
     * Sets the background color of a view using a rounded box drawable
     */
    public static void setRoundedColor(int color, View view, Context cntx) {
        Drawable drawable = cntx.getResources().getDrawable(R.drawable.round_box);
        drawable.setColorFilter(cntx.getResources().getColor(color), PorterDuff.Mode.SRC);
        view.setBackgroundDrawable(drawable);
    }

    /**
     * Clears the background color of a view
     */
    public static void clearRoundedColor(View view) {
        view.setBackgroundDrawable(null);
    }
}
