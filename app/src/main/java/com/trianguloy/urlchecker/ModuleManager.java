package com.trianguloy.urlchecker;

import android.content.Context;
import android.content.SharedPreferences;

import com.trianguloy.urlchecker.modules.AsciiModule;
import com.trianguloy.urlchecker.modules.BaseModule;
import com.trianguloy.urlchecker.modules.OpenModule;
import com.trianguloy.urlchecker.modules.RedirectModule;
import com.trianguloy.urlchecker.modules.TextInputModule;
import com.trianguloy.urlchecker.modules.VirusTotalModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager of all the modules
 */
public class ModuleManager {

    // ------------------- configuration -------------------

    /**
     * list of registered modules
     */
    private static final Map<String, Class<? extends BaseModule>> modules = new HashMap<>();

    static {
        // TODO: auto-load with reflection?
        modules.put("ascii", AsciiModule.class);
        modules.put("redirect", RedirectModule.class);
        modules.put("virustotal", VirusTotalModule.class);
    }

    // ------------------- class -------------------

    private static final String PREF_NAME = "MM";
    private static final String PREF_SUFFIX = "_en";

    /**
     * @return the uninitialized top module
     */
    static BaseModule getTopModule() {
        return new TextInputModule();
    }

    /**
     * Returns the uninitialized middle modules
     *
     * @param cntx base context (for the sharedpref)
     * @return the list, may be empty
     */
    public static List<BaseModule> getMiddleModules(Context cntx) {
        List<BaseModule> enabled = new ArrayList<>();
        SharedPreferences prefs = cntx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // for each module
        for (Map.Entry<String, Class<? extends BaseModule>> module : modules.entrySet()) {
            // check if enabled
            if (prefs.getBoolean(module.getKey() + PREF_SUFFIX, true)) {
                // and return
                try {
                    enabled.add(module.getValue().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return enabled;
    }

    /**
     * @return the uninitialized bottom module
     */
    static BaseModule getBottomModule() {
        return new OpenModule();
    }
}
