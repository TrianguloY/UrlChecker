package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;

import com.trianguloy.urlchecker.R;

import org.json.JSONException;
import org.json.JSONObject;

public class PatternCatalog {

    public static JSONObject getBuiltIn(Activity cntx) {
        try {
            return new JSONObject()
                    .put(cntx.getString(R.string.mPttrn_ascii), new JSONObject()
                            .put("regex", ".*[^\\p{ASCII}].*")
                            .put("enabled", true)
                            .put("automatic", false)
                    )
                    .put(cntx.getString(R.string.mPttrn_http), new JSONObject()
                            .put("regex", "^http://(.*)")
                            .put("replacement", "https://$1")
                            .put("enabled", true)
                            .put("automatic", true)
                    )
                    .put(cntx.getString(R.string.mPttrn_noSchemeHttp), new JSONObject()
                            .put("regex", "^(?!.*:).*")
                            .put("replacement", "http://$0")
                            .put("enabled", true)
                            .put("automatic", true)
                    )
                    .put(cntx.getString(R.string.mPttrn_noSchemeHttps), new JSONObject()
                            .put("regex", "^(?!.*:).*")
                            .put("replacement", "https://$0")
                            .put("enabled", true)
                            .put("automatic", false)
                    )
                    ;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

}