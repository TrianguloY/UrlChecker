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

    // ------------------- instantiated struct -------------------

    /**
     * The module class
     */
    public final Class<? extends BaseModule> dialogClass;

    /**
     * Identifier of the module
     */
    public final String id;

    /**
     * Visible name of this module
     */
    public final String name;

    /**
     * Full constructor for a module
     *
     * @param dialogClass the class
     * @param id          identifier
     * @param name        user name
     */
    public ModuleData(Class<? extends BaseModule> dialogClass, String id, String name) {
        this.dialogClass = dialogClass;
        this.id = id;
        this.name = name;
    }

    /**
     * Simplified constructor (for fixed modules)
     *
     * @param dialogClass the class of the module
     */
    public ModuleData(Class<? extends BaseModule> dialogClass) {
        this.dialogClass = dialogClass;
        id = name = null;
    }
}
