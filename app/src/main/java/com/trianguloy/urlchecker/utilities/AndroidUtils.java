package com.trianguloy.urlchecker.utilities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
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
     * Sets the theme (light/dark mode) to an activity
     */
    public static void setTheme(Activity activity) {
        activity.setTheme(
                (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO
                        ? R.style.DialogThemeLight // explicit light mode
                        : R.style.DialogThemeDark // dark mode or device default
        );
    }

    /**
     * Changes the action bar color
     */
    public static void setActionBarColor(Activity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.app)));
    }

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
