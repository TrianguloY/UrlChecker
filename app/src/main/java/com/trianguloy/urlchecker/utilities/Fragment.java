package com.trianguloy.urlchecker.utilities;

import android.view.View;

public interface Fragment {

    /**
     * @return the layout resource of this module
     */
    int getLayoutId();

    /**
     * Initializes this module from the given views (generated from {@link #getLayoutId()})
     *
     * @param views the inflated views
     */
    void onInitialize(View views);
}
