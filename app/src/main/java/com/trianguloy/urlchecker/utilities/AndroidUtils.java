package com.trianguloy.urlchecker.utilities;

import android.util.Log;

import com.trianguloy.urlchecker.BuildConfig;

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
}
