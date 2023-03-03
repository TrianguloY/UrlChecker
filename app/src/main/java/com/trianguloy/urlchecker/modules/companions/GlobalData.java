package com.trianguloy.urlchecker.modules.companions;

import com.trianguloy.urlchecker.url.UrlData;

import java.util.HashMap;
import java.util.Map;

public class GlobalData {
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
        return super.toString()+"{" + extraData + '}';
    }

}
