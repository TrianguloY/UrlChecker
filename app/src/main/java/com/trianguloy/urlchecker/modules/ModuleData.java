package com.trianguloy.urlchecker.modules;

import com.trianguloy.urlchecker.modules.list.AsciiModule;
import com.trianguloy.urlchecker.modules.list.OpenModule;
import com.trianguloy.urlchecker.modules.list.RedirectModule;
import com.trianguloy.urlchecker.modules.list.TextInputModule;
import com.trianguloy.urlchecker.modules.list.VirusTotalModule;

import java.util.ArrayList;
import java.util.List;

/**
 * If instantiated contains the information about a module.
 * Statically contains the available modules
 */
public class ModuleData {

    // ------------------- configuration -------------------

    public final static List<ModuleData> toggleableModules = new ArrayList<>();
    public final static ModuleData bottomModule = new ModuleData(OpenModule.class, "open", "Open & Share", "Allows to open or share the current url.");
    public final static ModuleData topModule = new ModuleData(TextInputModule.class, "text", "Input text", "Allows to edit the url manually");

    // ------------------- initialization -------------------

    static {
        // TODO: auto-load with reflection?
        add(RedirectModule.class, "redirect", "Redirection","Allows to check for redirection");
        add(VirusTotalModule.class, "virustotal", "VirusTotal","Allows to check the url in VirusTotal (an api key is needed)");
        add(AsciiModule.class, "ascii", "Ascii checker", "Checks for non-ascii characters");
    }

    public static void add(Class<? extends BaseModule> dialogClass, String id, String name, String description) {
        toggleableModules.add(new ModuleData(dialogClass, id, name, description));
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

    public final String description;

    /**
     * Full constructor for a module
     *
     * @param dialogClass the class
     * @param id          identifier
     * @param name        user name
     * @param description
     */
    public ModuleData(Class<? extends BaseModule> dialogClass, String id, String name, String description) {
        this.dialogClass = dialogClass;
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
