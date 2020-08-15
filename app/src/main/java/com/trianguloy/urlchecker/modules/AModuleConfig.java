package com.trianguloy.urlchecker.modules;

import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.utilities.Fragment;

/**
 * Base class for a module configuration fragment
 */
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

    /**
     * @return true iff this module can be enabled (possibly from current settings)
     */
    public abstract boolean canBeEnabled();

    // ------------------- utilities -------------------

    /**
     * Disables this module
     */
    public final void disable() {
        if (activity != null) activity.disableModule(this);
    }

}
