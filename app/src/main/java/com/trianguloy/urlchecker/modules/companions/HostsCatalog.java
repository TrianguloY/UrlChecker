package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;
import android.content.Context;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.generics.JsonCatalog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Catalog of hosts configuration. The whole hosts files are not included as ready-to-use copy like ClearURLs, because they are a lot bigger.
 * Instead, the configuration specifies the urls where to download them, and a parsed&optimized version is saved instead.
 * Manual update is required, they are too big and they should be updated in a background process
 */
public class HostsCatalog extends JsonCatalog {

    public HostsCatalog(Activity cntx) {
        super(cntx, "hosts", R.string.mHosts_editor);
    }

    @Override
    public JSONObject buildBuiltIn(Context cntx) throws JSONException {
        return new JSONObject()
                .put(cntx.getString(R.string.mHosts_malware), new JSONObject()
                        .put("color", "#80E91E63")
                        .put("file", "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts")
                )
                .put(cntx.getString(R.string.mHosts_fakenews), new JSONObject()
                        .put("color", "#803C1E1E")
                        .put("file", "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews/hosts")
                        .put("replace", false)
                )
                .put(cntx.getString(R.string.mHosts_gambling), new JSONObject()
                        .put("color", "#80483360")
                        .put("file", "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/gambling/hosts")
                        .put("replace", false)
                )
                .put(cntx.getString(R.string.mHosts_adult), new JSONObject()
                        .put("color", "#80603351")
                        .put("file", "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/porn/hosts")
                        .put("replace", false)
                )
                .put(cntx.getString(R.string.trianguloy), new JSONObject()
                        .put("color", "#FCDABA")
                        .put("hosts", new JSONArray()
                                .put("triangularapps.blogspot.com")
                                .put("trianguloy.github.io"))
                        .put("enabled", "false")
                )
                ;
    }
}
