package com.trianguloy.urlchecker.fragments;

import android.content.Intent;
import android.util.Log;

import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An injector to allow registering for activityResult.
 * Alternative to using the deprecate android fragments (and without using the huge compatibility library)
 */
public class ActivityResultInjector {
    private static final int RESERVED = 123; // just in case, registered listeners will have requestCode >= this
    private final List<Listener> listeners = new ArrayList<>();

    /* ------------------- client use ------------------- */

    public interface Listener {
        /**
         * Called when the event fires for a particular registrar
         */
        void onActivityResult(int resultCode, Intent data);

    }

    /**
     * Call this to register an onActivityResult listener. Returns the requestCode you must use in the startActivityForResult call
     */
    public int register(Listener listener) {
        listeners.add(listener);
        return RESERVED + listeners.size() - 1;
    }

    /* ------------------- activity use ------------------- */

    /**
     * An activity must use this as:
     * <pre>
     *     @Override
     *     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *         if (!activityResultInjector.onActivityResult(requestCode, resultCode, data))
     *             super.onActivityResult(requestCode, resultCode, data);
     *     }
     * </pre>
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        var index = requestCode - RESERVED;
        if (index < 0) {
            // external
            Log.d("ACTIVITY_RESULT", "External request code (" + requestCode + "), consider using ActivityResultInjector");
            return false;
        }
        if (index >= listeners.size()) {
            // external?
            AndroidUtils.assertError("Invalid request code (" + requestCode + "), consider using ActivityResultInjector or a requestCode less than " + RESERVED);
            return false;
        }
        listeners.get(index).onActivityResult(resultCode, data);
        return true;
    }
}
