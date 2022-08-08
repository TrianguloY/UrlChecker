package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.JsonEditor;
import com.trianguloy.urlchecker.utilities.InternalFile;

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
            // build from the translated strings
            return new JSONObject()
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