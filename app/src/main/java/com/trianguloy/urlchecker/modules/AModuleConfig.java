package com.trianguloy.urlchecker.modules;

import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.utilities.Fragment;

public abstract class AModuleConfig implements Fragment {

    // ------------------- private data -------------------

    private final ConfigActivity activity;

    // ------------------- initialization -------------------

    public AModuleConfig() {
        this.activity = null;
    }

    public AModuleConfig(ConfigActivity activity) {
        this.activity = activity;
    }

    // ------------------- abstract functions -------------------

    public abstract boolean canBeEnabled();


    // ------------------- utilities -------------------
    public final void disable() {
        if (activity != null) activity.disableModule(this);
    }

}
