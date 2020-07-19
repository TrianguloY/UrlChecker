package com.trianguloy.urlchecker;

import android.content.Context;
import android.content.SharedPreferences;

import com.trianguloy.urlchecker.modules.AsciiModule;
import com.trianguloy.urlchecker.modules.BaseModule;
import com.trianguloy.urlchecker.modules.OpenModule;
import com.trianguloy.urlchecker.modules.RedirectModule;
import com.trianguloy.urlchecker.modules.VirusTotalModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {

    // ------------------- configuration -------------------

    private static final Map<String, Class<? extends BaseModule>> modules = new HashMap<>();

    static {
        modules.put("ascii", AsciiModule.class);
        modules.put("redirect", RedirectModule.class);
        modules.put("virustotal", VirusTotalModule.class);
    }

    // ------------------- class -------------------

    public static List<BaseModule> getEnabled(Context cntx) {
        List<BaseModule> enabled = new ArrayList<>();
        SharedPreferences prefs = cntx.getSharedPreferences("MM", Context.MODE_PRIVATE);
        for (Map.Entry<String, Class<? extends BaseModule>> module : modules.entrySet()) {
            if(prefs.getBoolean(module.getKey()+"_en", true)){
                try {
                    enabled.add(module.getValue().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return enabled;
    }
}
