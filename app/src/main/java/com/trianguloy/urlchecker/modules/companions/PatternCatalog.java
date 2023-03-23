package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;
import android.content.Context;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.JsonCatalog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the catalog of the Pattern module
 */
public class PatternCatalog extends JsonCatalog {

    public PatternCatalog(Activity cntx) {
        super(cntx, "patterns", R.string.mPttrn_editor);
    }

    @Override
    public JSONObject buildBuiltIn(Context cntx) throws JSONException {
        return new JSONObject()

                // built from the translated strings
                .put(cntx.getString(R.string.mPttrn_ascii), new JSONObject()
                        .put("regex", "[^\\p{ASCII}]")
                )
                .put(cntx.getString(R.string.mPttrn_http), new JSONObject()
                        .put("regex", "^http://")
                        .put("replacement", "https://")
                )
                .put(cntx.getString(R.string.mPttrn_noSchemeHttp), new JSONObject()
                        .put("regex", "^(?!.*:)")
                        .put("replacement", "http://$0")
                )
                .put(cntx.getString(R.string.mPttrn_noSchemeHttps), new JSONObject()
                        .put("regex", "^(?!.*:)")
                        .put("replacement", "https://$0")
                )

                // privacy redirections samples (see https://github.com/TrianguloY/UrlChecker/discussions/122)
                .put("Reddit ➔ Teddit", new JSONObject()
                        .put("regex", "^https?://(?:[a-z0-9-]+\\.)*?reddit.com/(.*)")
                        .put("replacement", "https://teddit.net/$1")
                        .put("enabled", "false")
                )
                .put("Twitter ➔ Nitter", new JSONObject()
                        .put("regex", "^https?://(?:[a-z0-9-]+\\.)*?twitter.com/(.*)")
                        .put("replacement", "https://nitter.net/$1")
                        .put("enabled", "false")
                )
                .put("Youtube ➔ Invidious", new JSONObject()
                        .put("regex", "^https?://(?:[a-z0-9-]+\\.)*?youtube.com/(.*)")
                        .put("replacement", new JSONArray()
                                .put("https://yewtu.be/$1")
                                .put("https://farside.link/invidious/$1")
                        )
                        .put("enabled", "false")
                )
                .put("Fandom ➔ BreezeWiki", new JSONObject()
                        .put("regex", "^https?://([a-z0-9-]+)\\.fandom\\.com/(.*)")
                        .put("excludeRegex", "^https?://www\\.fandom\\.com/")
                        .put("replacement", "https://breezewiki.com/$1/$2")
                        .put("enabled", "false")
                )
                ;
    }

}
