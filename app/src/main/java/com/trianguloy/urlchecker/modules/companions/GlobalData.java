package com.trianguloy.urlchecker.modules.companions;

import com.trianguloy.urlchecker.url.UrlData;

import java.util.HashMap;
import java.util.Map;

public class GlobalData {
    // FIXME kill on finish? or even before calling main dialog
    private static GlobalData instance = null;

    private GlobalData(){
    }

    public static GlobalData getInstance(){
        instance = instance == null ? new GlobalData() : instance;
        return instance;
    }


    // FIXME if URLCheck shares to itself it will reset the previous instance
    // maybe create an ID based on the activity object id/memory address
    public static void resetInstance(){
        instance = null;
    }

    // ------------------- extra data -------------------

    /**
     * Any key-value modules can set, will be kept with automatic updates
     */
    private final Map<String, String> extraData = new HashMap<>();

    public GlobalData putData(String key, String value) {
        extraData.put(key, value);
        return this;
    }

    public String getData(String key) {
        return extraData.get(key);
    }

    @Override
    public String toString() {
        return "GlobalData{" + extraData + '}';
    }

}
