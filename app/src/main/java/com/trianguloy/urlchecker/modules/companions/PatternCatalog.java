package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;
import android.content.Context;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.JsonEditor;
import com.trianguloy.urlchecker.utilities.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

public class PatternCatalog {

    private static final String fileName = "patterns";

    private final Activity cntx;

    public PatternCatalog(Activity cntx) {
        this.cntx = cntx;
    }

    public JSONObject getCatalog() {
        // get the updated file first
        try {
            return new JSONObject(StreamUtils.inputStream2String(cntx.openFileInput(fileName)));
        } catch (IOException | JSONException ignored) {
        }

        // no updated file or can't read, use built-in one
        return getBuiltIn();
    }

    public void clear() {
        cntx.deleteFile(fileName);
    }

    public boolean save(JSONObject content) {

        // the same, already saved
        if (content.equals(getCatalog())) return true;

        // same as builtin (maybe a reset?), clear custom
        if (content.equals(getBuiltIn())) {
            clear();
            return true;
        }

        // store
        try (FileOutputStream fos = cntx.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.toString().getBytes(StreamUtils.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public JSONObject getBuiltIn() {
        try {
            return new JSONObject()
                    .put(cntx.getString(R.string.mPttrn_ascii), new JSONObject()
                            .put("regex", ".*[^\\p{ASCII}].*")
                    )
                    .put(cntx.getString(R.string.mPttrn_http), new JSONObject()
                            .put("regex", "^http://(.*)")
                            .put("replacement", "https://$1")
                    )
                    .put(cntx.getString(R.string.mPttrn_noSchemeHttp), new JSONObject()
                            .put("regex", "^(?!.*:).*")
                            .put("replacement", "http://$0")
                    )
                    .put(cntx.getString(R.string.mPttrn_noSchemeHttps), new JSONObject()
                            .put("regex", "^(?!.*:).*")
                            .put("replacement", "https://$0")
                    )
                    ;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public void showEditor() {
        JsonEditor.show(getCatalog(), getBuiltIn(), R.string.mPttrn_editor, cntx, this::save);
    }

}