package com.trianguloy.urlchecker.modules;

import java.util.ArrayList;
import java.util.List;

/**
 * If instantiated contains the information about a module.
 * Statically contains the available modules
 */
public class ModuleData {

    // ------------------- configuration -------------------

    public final static List<ModuleData> toggleableModules = new ArrayList<>();
    public final static ModuleData bottomModule = new ModuleData(OpenModule.class);
    public final static ModuleData topModule = new ModuleData(TextInputModule.class);

    // ------------------- initialization -------------------

    static {
        // TODO: auto-load with reflection?
        add(AsciiModule.class, "ascii", "Ascii checker");
        add(RedirectModule.class, "redirect", "Redirection");
        add(VirusTotalModule.class, "virustotal", "VirusTotal");
    }

    public static void add(Class<? extends BaseModule> dialogClass, String id, String name) {
        toggleableModules.add(new ModuleData(dialogClass, id, name));
    }

    // ------------------- struct -------------------

    public final Class<? extends BaseModule> dialogClass;
    public final String id;

    public final String name;

    public ModuleData(Class<? extends BaseModule> dialogClass, String id, String name) {
        this.dialogClass = dialogClass;
        this.id = id;
        this.name = name;
    }

    public ModuleData(Class<? extends BaseModule> dialogClass) {
        this.dialogClass = dialogClass;
        id = name = null;
    }
}
