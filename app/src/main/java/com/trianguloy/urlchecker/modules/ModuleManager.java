package com.trianguloy.urlchecker.modules;

import android.content.Context;

import com.trianguloy.urlchecker.utilities.GenericPref;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager of all the modules
 */
public class ModuleManager {

    // ------------------- class -------------------

    private static final String PREF_SUFFIX = "_en";
    /**
     * Status of modules by default (true=enabled, false=disabled)
     */
    private static final boolean ENABLED_DEFAULT = true;

    public static GenericPref.Bool getEnabledPrefOfModule(ModuleData module, Context cntx) {
        final GenericPref.Bool enabledPref = new GenericPref.Bool(module.id + PREF_SUFFIX, ENABLED_DEFAULT);
        enabledPref.init(cntx);
        return enabledPref;
    }

    /**
     * Returns the uninitialized middle modules
     *
     * @param cntx base context (for the sharedpref)
     * @return the list, may be empty
     */
    public static List<ModuleData> getMiddleModules(Context cntx) {
        List<ModuleData> enabled = new ArrayList<>();

        // check each module
        for (ModuleData module : ModuleData.toggleableModules) {
            if (getEnabledPrefOfModule(module, cntx).get()) {
                try {
                    // enabled, add
                    enabled.add(module);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return enabled;
    }

}
