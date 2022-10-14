package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.JsonEditor;
import com.trianguloy.urlchecker.utilities.InternalFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the catalog of the Pattern module
 */
public class PatternCatalog {

    private final InternalFile custom = new InternalFile("patterns");

    private final Activity cntx;

    public PatternCatalog(Activity cntx) {
        this.cntx = cntx;
        custom.init(cntx);
    }

    /**
     * Returns the current catalog
     */
    public JSONObject getCatalog() {
        // get the updated file first
        try {
            String content = custom.get();
            if (content != null) return new JSONObject(content);
        } catch (JSONException ignored) {
        }

        // no updated file or can't read, use built-in one
        return getBuiltIn();
    }

    /**
     * Gets the builtin patterns
     */
    public JSONObject getBuiltIn() {
        try {
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
                    ;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    /**
     * Saves a json as new catalog
     */
    public boolean save(JSONObject content) {

        // same as builtin (maybe a reset?), delete custom
        if (content.equals(getBuiltIn())) {
            custom.delete();
            return true;
        }

        // store
        return custom.set(content.toString());
    }

    /**
     * Shows a dialog to manually edit the catalog
     */
    public void showEditor() {
        JsonEditor.show(getCatalog(), getBuiltIn(), R.string.mPttrn_editor, cntx, this::save);
    }

}