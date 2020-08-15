package com.trianguloy.urlchecker.utilities;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class Inflater {
    static public <T extends View> T inflate(int resource, ViewGroup root, Activity cntx) {
        final View view = cntx.getLayoutInflater().inflate(resource, root, false);
        root.addView(view);
        return ((T) view);
    }
}
