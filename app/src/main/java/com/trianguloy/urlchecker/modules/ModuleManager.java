package com.trianguloy.urlchecker.modules;

import android.content.Context;

import com.trianguloy.urlchecker.modules.list.ClearUrlModule;
import com.trianguloy.urlchecker.modules.list.DebugModule;
import com.trianguloy.urlchecker.modules.list.HistoryModule;
import com.trianguloy.urlchecker.modules.list.LogModule;
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

    private final static List<AModuleData> modules = new ArrayList<>();

    static {
        // TODO: auto-load with reflection?
        modules.add(new LogModule());
        modules.add(new TextInputModule());

        modules.add(new HistoryModule());
        modules.add(new StatusModule());
        modules.add(new VirusTotalModule());
        modules.add(new ClearUrlModule());
        modules.add(new RemoveQueriesModule());
        modules.add(new PatternModule());
        // new modules should preferably be added directly above this line
        modules.add(new DebugModule());

        modules.add(new OpenModule());
    }

    /**
     * Order of the modules
     */
    public static GenericPref.LstStr ORDER_PREF() {
        // default is just the defined order (but in reverse)
        List<String> ids = new ArrayList<>(modules.size());
        for (AModuleData module : modules) {
            ids.add(0, module.getId());
        }
        // return
        return new GenericPref.LstStr("order", ";", ids);
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
     * Returns the uninitialized modules based on the user order.
     * If includeDisabled is false, non-enabled modules will not be returned
     *
     * @param cntx base context (for the sharedpref)
     * @return the list, may be empty
     */
    public static List<AModuleData> getModules(boolean includeDisabled, Context cntx) {
        List<AModuleData> availableModules = new ArrayList<>();

        // check each module
        for (AModuleData module : modules) {
            if (includeDisabled || getEnabledPrefOfModule(module, cntx).get()) {
                // enabled, add
                availableModules.add(module);
            }
        }

        // sort modules
        List<String> order = ORDER_PREF().init(cntx).get();
        int insertion = order.indexOf(DebugModule.ID); // non-present modules will be inserted where the debug module is
        Collections.sort(availableModules, (a, b) -> {
            int posA = order.contains(a.getId()) ? order.indexOf(a.getId()) : insertion;
            int posB = order.contains(b.getId()) ? order.indexOf(b.getId()) : insertion;
            return posB - posA;
        });

        return availableModules;
    }

    /**
     * returns all the modules ids in the order they should be
     */
    public static List<String> getOrderedModulesId(Context cntx) {
        // this is just "return getModules(...).map{it.getId}" but with java 7
        List<AModuleData> modules = getModules(true, cntx);
        List<String> ids = new ArrayList<>();
        for (AModuleData module : modules) {
            ids.add(module.getId());
        }
        return ids;
    }

}
