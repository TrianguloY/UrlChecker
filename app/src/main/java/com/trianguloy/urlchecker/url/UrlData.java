package com.trianguloy.urlchecker.url;

import com.trianguloy.urlchecker.modules.AModuleDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages an url and extra data associated with it
 * I don't like this approach, but unfortunately I can't think of something better
 */
public class UrlData {

    // ------------------- url -------------------

    public final String url;

    public UrlData(String url) {
        this.url = url == null ? "" : url;
    }

    // ------------------- optional data -------------------

    /**
     * The module that triggered this data (null if internal)
     */
    public AModuleDialog trigger;

    /**
     * If set, the module that triggers the update will be notified
     */
    public boolean triggerOwn = true;

    public UrlData dontTriggerOwn() {
        triggerOwn = false;
        return this;
    }

    /**
     * If set, the url will not be changed (future setUrl calls will be ignored)
     */
    public boolean disableUpdates = false;

    public UrlData disableUpdates() {
        disableUpdates = true;
        return this;
    }

    /**
     * If set, this update is considered 'minor' and modules may decide to ignore or merge it with the previous one
     */
    public boolean minorUpdate = false;

    public UrlData asMinorUpdate() {
        minorUpdate = true;
        return this;
    }

    // ------------------- extra data -------------------

    /**
     * Any key-value modules can set, will be kept with automatic updates
     */
    private final Map<String, String> extraData = new HashMap<>();

    public UrlData putData(String key, String value) {
        extraData.put(key, value);
        return this;
    }

    public String getData(String key) {
        return extraData.get(key);
    }

    public void mergeData(UrlData urlData) {
        extraData.putAll(urlData.extraData);
    }

    @Override
    public String toString() {
        return "UrlData{" + "url='" + url + '\'' +
                ", trigger=" + trigger +
                ", triggerOwn=" + triggerOwn +
                ", disableUpdates=" + disableUpdates +
                ", minorUpdate=" + minorUpdate +
                ", extraData=" + extraData +
                '}';
    }
}
