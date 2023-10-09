package com.trianguloy.urlchecker.modules.companions;

import static com.trianguloy.urlchecker.utilities.methods.JavaUtils.valueOrDefault;

import android.app.Activity;
import android.content.Intent;

import com.trianguloy.urlchecker.modules.AModuleDialog;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents intent flags
 * Stores all the flags and it states, can be retrieved as list or as int
 * Non-compatible flags are ignored
 */
public class Flags {

    // ------------------- static -------------------

    // https://github.com/MuntashirAkon/AppManager/blob/19782da4c8556c817ba5795554a1cc21f38af13a/app/src/main/java/io/github/muntashirakon/AppManager/intercept/ActivityInterceptor.java#L92
    private static final Set<String> ALL_FLAGS = Set.of(
            // all have a 'FLAG_' prefix
            "GRANT_READ_URI_PERMISSION",
            "GRANT_WRITE_URI_PERMISSION",
            "FROM_BACKGROUND",
            "DEBUG_LOG_RESOLUTION",
            "EXCLUDE_STOPPED_PACKAGES",
            "INCLUDE_STOPPED_PACKAGES",
            "GRANT_PERSISTABLE_URI_PERMISSION",
            "GRANT_PREFIX_URI_PERMISSION",
            "DIRECT_BOOT_AUTO",
            "IGNORE_EPHEMERAL",
            "ACTIVITY_NO_HISTORY",
            "ACTIVITY_SINGLE_TOP",
            "ACTIVITY_NEW_TASK",
            "ACTIVITY_MULTIPLE_TASK",
            "ACTIVITY_CLEAR_TOP",
            "ACTIVITY_FORWARD_RESULT",
            "ACTIVITY_PREVIOUS_IS_TOP",
            "ACTIVITY_EXCLUDE_FROM_RECENTS",
            "ACTIVITY_BROUGHT_TO_FRONT",
            "ACTIVITY_RESET_TASK_IF_NEEDED",
            "ACTIVITY_LAUNCHED_FROM_HISTORY",
            "ACTIVITY_NEW_DOCUMENT",
            "ACTIVITY_NO_USER_ACTION",
            "ACTIVITY_REORDER_TO_FRONT",
            "ACTIVITY_NO_ANIMATION",
            "ACTIVITY_CLEAR_TASK",
            "ACTIVITY_TASK_ON_HOME",
            "ACTIVITY_RETAIN_IN_RECENTS",
            "ACTIVITY_LAUNCH_ADJACENT",
            "ACTIVITY_MATCH_EXTERNAL",
            "ACTIVITY_REQUIRE_NON_BROWSER",
            "ACTIVITY_REQUIRE_DEFAULT"
    );

    private static final Map<String, Integer> compatibleFlags = new TreeMap<>(); // TreeMap to have the entries sorted by key

    static {
        // Only get flags that are present in the current Android version
        for (var flag : ALL_FLAGS) {
            try {
                compatibleFlags.put(flag, (Integer) Intent.class.getField("FLAG_" + flag).get(null));
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Integer> getCompatibleFlags() {
        return new TreeMap<>(compatibleFlags);
    }

    // ------------------- CRUD -------------------

    private Set<String> flags;

    // TODO store non-compatible flags? as String "0x001000", int 0x001000

    public Flags() {
        this(0x00000000);
    }

    public Flags(int hex) {
        setFlags(hex);
    }

    public boolean isSet(String flag) {
        return flags.contains(flag);
    }

    public int getFlagsAsInt() {
        return flagsSetToHex(flags);
    }

    public Set<String> getFlagsAsSet() {
        return new TreeSet<>(flags);    // TreeSet to have the entries sorted
    }

    /**
     * Replaces the stored flags with the received hex
     */
    public void setFlags(int hex) {
        this.flags = hexFlagsToSet(hex);
    }

    /**
     * Replaces the stored flags with the received list
     */
    public void setFlags(Set<String> names) {
        Set<String> res = new HashSet<>(names);
        res.retainAll(compatibleFlags.keySet());
        this.flags = res;
    }

    /**
     * Sets the flag to the received boolean
     */
    public boolean setFlag(String flag, boolean bool) {
        if (compatibleFlags.containsKey(flag)) {
            if (bool) {
                return flags.add(flag);
            } else {
                return flags.remove(flag);
            }
        } else {
            return false;
        }
    }

    /**
     * Add flags by applying a mask
     */
    public void addFlags(int hex) {
        flags.addAll(hexFlagsToSet(hex));
    }

    /**
     * Add a list of flags based on its name
     */
    public void addFlags(Set<String> flags) {
        this.flags.addAll(flags);
    }

    /**
     * Add a flag based on its name
     */
    public boolean addFlag(String name) {
        if (compatibleFlags.containsKey(name)) {
            return flags.add(name);
        } else {
            return false;
        }
    }

    /**
     * Remove flags by applying a mask
     */
    public boolean removeFlags(int hex) {
        return flags.removeAll(hexFlagsToSet(hex));
    }

    /**
     * Remove a list of flags based on its name
     */
    public boolean removeFlags(Set<String> flags) {
        return this.flags.removeAll(flags);
    }

    /**
     * Remove a flag based on its name
     */
    public boolean removeFlag(String name) {
        return flags.remove(name);
    }

    // ------------------- utils -------------------

    /**
     * Decode an int as set
     */
    private static Set<String> hexFlagsToSet(int hex) {
        var foundFlags = new HashSet<String>();
        for (var flag : compatibleFlags.entrySet()) {
            // check if flag is present
            if ((hex & flag.getValue()) != 0) {
                foundFlags.add(flag.getKey());
            }
        }
        return foundFlags;
    }

    /**
     * Decode a set as hex
     */
    private static int flagsSetToHex(Set<String> set) {
        int hex = 0x00000000;
        for (var flag : set) {
            if (compatibleFlags.containsKey(flag)) {
                hex = hex | compatibleFlags.get(flag);
            }
        }
        return hex;
    }


    // ------------------- store/load flags -------------------
    // this handles the store and load of the flags, if something wants to get the flags
    // it should always use these methods.

    private static final String DATA_FLAGS = "flagsEditor.flags";
    private static final String REGEX = "(0x)?[a-fA-F\\d]{1,8}";
    private static final int BASE = 16;

    /**
     * parses a text as an hexadecimal flags string.
     * Returns null if invalid
     */
    public static Integer toInteger(String text) {
        if (text != null && text.matches(REGEX)) {
            return Integer.parseInt(text.replaceAll("^0x", ""), BASE);
        } else {
            return null;
        }
    }

    /**
     * Converts an int flags to string
     */
    public static String toHexString(int flags) {
        return "0x" + Integer.toString(flags, BASE);
    }

    /**
     * Retrieves the flags from GlobalData, if it is not defined it will return null
     */
    public static Integer getGlobalFlagsNullable(AModuleDialog instance) {
        return toInteger(instance.getData(DATA_FLAGS));
    }

    /**
     * Loads the flags from GlobalData, if none were found it gets the flags from the intent that
     * started this activity
     */
    public static int getGlobalFlagsNonNull(AModuleDialog instance, Activity cntx) {
        return getGlobalFlagsOrDefault(instance, cntx.getIntent().getFlags());
    }

    /**
     * Loads the flags from GlobalData, if none were found it gets the flags from default
     * Can be used by other modules
     */
    public static int getGlobalFlagsOrDefault(AModuleDialog instance, int defaultFlags) {
        return valueOrDefault(toInteger(instance.getData(DATA_FLAGS)), defaultFlags);
    }

    /**
     * Stores the flags in GlobalData
     */
    public static void setGlobalFlags(Flags flags, AModuleDialog instance) {
        instance.putData(DATA_FLAGS, flags == null ? null : toHexString(flags.getFlagsAsInt()));
    }
}
