package com.trianguloy.urlchecker.modules.companions;

import android.content.Intent;

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
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "FLAG_FROM_BACKGROUND",
            "FLAG_DEBUG_LOG_RESOLUTION",
            "FLAG_EXCLUDE_STOPPED_PACKAGES",
            "FLAG_INCLUDE_STOPPED_PACKAGES",
            "FLAG_GRANT_PERSISTABLE_URI_PERMISSION",
            "FLAG_GRANT_PREFIX_URI_PERMISSION",
            "FLAG_DIRECT_BOOT_AUTO",
            "FLAG_IGNORE_EPHEMERAL",
            "FLAG_ACTIVITY_NO_HISTORY",
            "FLAG_ACTIVITY_SINGLE_TOP",
            "FLAG_ACTIVITY_NEW_TASK",
            "FLAG_ACTIVITY_MULTIPLE_TASK",
            "FLAG_ACTIVITY_CLEAR_TOP",
            "FLAG_ACTIVITY_FORWARD_RESULT",
            "FLAG_ACTIVITY_PREVIOUS_IS_TOP",
            "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS",
            "FLAG_ACTIVITY_BROUGHT_TO_FRONT",
            "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED",
            "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY",
            "FLAG_ACTIVITY_NEW_DOCUMENT",
            "FLAG_ACTIVITY_NO_USER_ACTION",
            "FLAG_ACTIVITY_REORDER_TO_FRONT",
            "FLAG_ACTIVITY_NO_ANIMATION",
            "FLAG_ACTIVITY_CLEAR_TASK",
            "FLAG_ACTIVITY_TASK_ON_HOME",
            "FLAG_ACTIVITY_RETAIN_IN_RECENTS",
            "FLAG_ACTIVITY_LAUNCH_ADJACENT",
            "FLAG_ACTIVITY_MATCH_EXTERNAL",
            "FLAG_ACTIVITY_REQUIRE_NON_BROWSER",
            "FLAG_ACTIVITY_REQUIRE_DEFAULT"
    );

    private static final Map<String, Integer> compatibleFlags = new TreeMap<>(); // TreeMap to have the entries sorted by key

    static {
        try {
            // Only get flags that are present in the current Android version
            for (var field : Intent.class.getFields()) {
                if (ALL_FLAGS.contains(field.getName())) {
                    compatibleFlags.put(field.getName(), (Integer) field.get(null));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> getCompatibleFlags(){
        return new TreeMap<>(compatibleFlags);
    }

    // ------------------- CRUD -------------------

    private Set<String> flags;

    // TODO store non-compatible flags? as String "0x001000", int 0x001000

    public Flags(){
        this(0x00000000);
    }

    public Flags(int hex){
        setFlags(hex);
    }

    public boolean isSet(String flag){
        return flags.contains(flag);
    }

    public int getFlagsAsInt(){
        return flagsSetToHex(flags);
    }

    public Set<String> getFlagsAsSet() {
        return new TreeSet<>(flags);    // TreeSet to have the entries sorted
    }

    /**
     * Replaces the stored flags with the received hex
     */
    public void setFlags(int hex){
        this.flags = hexFlagsToSet(hex);
    }

    /**
     * Replaces the stored flags with the received list
     */
    public void setFlags(Set<String> names){
        Set<String> res = new HashSet<>(names);
        res.retainAll(compatibleFlags.keySet());
        this.flags = res;
    }

    /**
     * Sets the flag to the received boolean
     */
    public boolean setFlag(String flag, boolean bool){
        if (compatibleFlags.containsKey(flag)){
            if (bool){
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
    public void addFlags(int hex){
        flags.addAll(hexFlagsToSet(hex));
    }

    /**
     * Add a list of flags based on its name
     */
    public void addFlags(Set<String> flags){
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
    public boolean removeFlags(int hex){
        return flags.removeAll(hexFlagsToSet(hex));
    }

    /**
     * Remove a list of flags based on its name
     */
    public boolean removeFlags(Set<String> flags){
        return this.flags.removeAll(flags);
    }

    /**
     * Remove a flag based on its name
     */
    public boolean removeFlag(String name){
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
        for (var flag : set){
            if (compatibleFlags.containsKey(flag)) {
                hex = hex | compatibleFlags.get(flag);
            }
        }
        return hex;
    }
}
