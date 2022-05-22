package com.trianguloy.urlchecker.modules;

import android.content.Context;

import com.trianguloy.urlchecker.modules.list.ClearUrlModule;
import com.trianguloy.urlchecker.modules.list.DebugModule;
import com.trianguloy.urlchecker.modules.list.HistoryModule;
import com.trianguloy.urlchecker.modules.list.OpenModule;
import com.trianguloy.urlchecker.modules.list.PatternModule;
import com.trianguloy.urlchecker.modules.list.RemoveQueriesModule;
import com.trianguloy.urlchecker.modules.list.StatusModule;
import com.trianguloy.urlchecker.modules.list.TextInputModule;
import com.trianguloy.urlchecker.modules.list.VirusTotalModule;
import com.trianguloy.urlchecker.utilities.GenericPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manager of all the modules
 */
public class ModuleManager {

    public final static AModuleData topModule = new TextInputModule();

    private final static List<AModuleData> toggleableModules = new ArrayList<>();

    static {
        // TODO: auto-load with reflection?
        toggleableModules.add(new HistoryModule());
        toggleableModules.add(new StatusModule());
        toggleableModules.add(new VirusTotalModule());
        toggleableModules.add(new ClearUrlModule());
        toggleableModules.add(new RemoveQueriesModule());
        toggleableModules.add(new PatternModule());
        toggleableModules.add(new DebugModule());
    }

    public final static AModuleData bottomModule = new OpenModule();

    /**
     * User defined order of the toggleable modules
     */
    public static GenericPref.LstStr ORDER_PREF() {
        return new GenericPref.LstStr("order", Collections.emptyList());
    }


    // ------------------- class -------------------

    private static final String PREF_SUFFIX = "_en";

    /**
     * Returns a preference to indicate if a specific module is enabled or not
     */
    public static GenericPref.Bool getEnabledPrefOfModule(AModuleData module, Context cntx) {
        final GenericPref.Bool enabledPref = new GenericPref.Bool(module.getId() + PREF_SUFFIX, module.isEnabledByDefault());
        enabledPref.init(cntx);
        return enabledPref;
    }

    /**
     * Returns the uninitialized middle modules based on the user order.
     * If includeDisabled is false, non-enabled modules will not be returned
     *
     * @param cntx base context (for the sharedpref)
     * @return the list, may be empty
     */
    public static List<AModuleData> getMiddleModules(boolean includeDisabled, Context cntx) {
        List<AModuleData> availableModules = new ArrayList<>();

        // check each module
        for (AModuleData module : toggleableModules) {
            if (includeDisabled || getEnabledPrefOfModule(module, cntx).get()) {
                try {
                    // enabled, add
                    availableModules.add(module);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        List<String> order = ORDER_PREF().init(cntx).get();
        Collections.sort(availableModules, (a, b) -> order.indexOf(b.getId()) - order.indexOf(a.getId()));

        return availableModules;
    }

    /**
     * returns all the modules ids in the order they should be
     */
    public static List<String> getOrderedModulesId(Context cntx) {
        // this is just "return getMiddleModules(...).map{it.getId}" but with java 7
        List<AModuleData> modules = getMiddleModules(true, cntx);
        List<String> ids = new ArrayList<>();
        for (AModuleData module : modules) {
            ids.add(module.getId());
        }
        return ids;
    }

}
