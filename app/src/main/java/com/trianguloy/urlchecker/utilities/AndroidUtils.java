package com.trianguloy.urlchecker.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
     * Sets the start drawable of a textview
     * Wrapped for android compatibility
     */
    public static void setStartDrawables(TextView txt, int start) {
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

    /**
     * Makes the text of a textview display as a link (which does nothing when clicked)
     */
    public static void setAsClickable(TextView textview) {
        SpannableStringBuilder text = new SpannableStringBuilder(textview.getText());
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View ignored) {
                // do nothing
            }
        }, 0, text.length(), 0);
        textview.setText(text);
    }
}
