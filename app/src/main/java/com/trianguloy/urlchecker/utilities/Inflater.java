package com.trianguloy.urlchecker.utilities;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Why the default {@link android.view.LayoutInflater#inflate(int, ViewGroup)} returns the root supplied will always be a mystery, this fixes it.
 */
public class Inflater {
    /**
     * @see {@link android.view.LayoutInflater#inflate(int, ViewGroup)}, but returns the inflated view (not the root view)
     */
    static public <T extends View> T inflate(int resource, ViewGroup root, Activity cntx) {
        final View view = cntx.getLayoutInflater().inflate(resource, root, false);
        root.addView(view);
        return ((T) view);
    }
}
