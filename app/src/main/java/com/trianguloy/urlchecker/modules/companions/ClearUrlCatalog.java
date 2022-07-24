package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.JsonEditor;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.AssetFile;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.InternalFile;
import com.trianguloy.urlchecker.utilities.JavaUtilities;
import com.trianguloy.urlchecker.utilities.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the local catalog with the rules
 */
public class ClearUrlCatalog {

    /* ------------------- constants ------------------- */

    private final InternalFile custom = new InternalFile("clearUrlCatalog");
    private final AssetFile builtIn = new AssetFile("data.minify.json");
    private static final int AUTOUPDATE_PERIOD = /* 1 week (in milliseconds) */
            7/*days*/ * 24/*hours*/ * 60/*minutes*/ * 60/*seconds*/ * 1000/*milliseconds*/;

    /* ------------------- prefs ------------------- */

    private final GenericPref.Str catalogURL = new GenericPref.Str("clearurl_catalogURL", "https://rules2.clearurls.xyz/data.minify.json");
    private final GenericPref.Str hashURL = new GenericPref.Str("clearurl_hashURL", "https://rules2.clearurls.xyz/rules.minify.hash");
    private final GenericPref.Bool autoUpdate = new GenericPref.Bool("clearurl_autoUpdate", false);
    private final GenericPref.Lng lastUpdate = new GenericPref.Lng("clearurl_lastUpdate", 0L);

    /* ------------------- constructor ------------------- */

    private final Activity cntx;

    public ClearUrlCatalog(Activity cntx) {
        this.cntx = cntx;
        catalogURL.init(cntx);
        hashURL.init(cntx);
        autoUpdate.init(cntx);
        lastUpdate.init(cntx);
        custom.init(cntx);
        builtIn.init(cntx);

        updateIfNecessary();
    }

    /* ------------------- catalog ------------------- */

    /**
     * Returns the catalog content as text
     */
    public String getCatalog() {
        // get the updated file first
        String internal = custom.get();
        if (internal != null) return internal;

        // no updated file or can't read, use built-in one
        return getBuiltIn();
    }

    /**
     * Returns the built-in catalog
     */
    public String getBuiltIn() {
        // read internal file
        String builtIn = this.builtIn.get();
        if (builtIn != null) return builtIn;

        // can't read either? panic! return empty
        return "{\"providers\":{}}";
    }

    /**
     * Parses and returns the providers from the catalog
     * Returns a list of pairs: [(rule,data),...]
     */
    public static List<Pair<String, JSONObject>> getRules(Activity cntx) {
        try {
            // prepare
            List<Pair<String, JSONObject>> rules = new ArrayList<>();
            ClearUrlCatalog clearUrlCatalog = new ClearUrlCatalog(cntx);
            JSONObject json = JavaUtilities.toJson(clearUrlCatalog.getCatalog());

            // extract and merge each provider
            for (String provider : JavaUtilities.toList(json.keys())) {
                JSONObject providerData = json.getJSONObject(provider);
                for (String rule : JavaUtilities.toList(providerData.keys())) {
                    rules.add(Pair.create(rule, providerData.getJSONObject(rule)));
                }
            }
            return rules;
        } catch (JSONException e) {
            // invalid catalog, return empty
            AndroidUtils.assertError(e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Saves a new local catalog. Returns true if it was saved correctly, false on error.
     * When merge is true, only the top-objects with the same key are replaced.
     */
    public boolean setRules(JSONObject rules, boolean merge) {
        // merge rules if required
        if (merge) {
            try {
                // replace only the top objects
                JSONObject merged = JavaUtilities.toJson(getCatalog());
                for (String key : JavaUtilities.toList(rules.keys())) {
                    merged.put(key, rules.getJSONObject(key));
                }
                rules = merged;
            } catch (JSONException e) {
                e.printStackTrace();
                // can't be parsed
                return false;
            }
        }
        // compact
        String content = rules.toString();

        // same as builtin (maybe a reset?), clear custom
        if (content.equals(getBuiltIn())) {
            clear();
            return true;
        }

        // store
        return custom.set(content);
    }

    /**
     * Deletes the custom catalog, built-in one will be returned afterwards
     */
    public void clear() {
        custom.delete();
        lastUpdate.clear();
    }

    // ------------------- dialogs -------------------

    /**
     * Show the rules editor dialog
     */
    public void showEditor() {
        JsonEditor.show(JavaUtilities.toJson(getCatalog()), JavaUtilities.toJson(getBuiltIn()), R.string.mClear_editor, cntx, content -> {
            if (setRules(content, false)) {
                // saved data, close dialog
                return true;
            } else {
                // invalid data, keep dialog and show why
                Toast.makeText(cntx, R.string.toast_invalid, Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    /**
     * Show the updater dialog
     */
    public void showUpdater() {
        // prepare dialog content
        View views = cntx.getLayoutInflater().inflate(R.layout.config_clearurls_updater, null);

        // configure
        catalogURL.attachToEditText(views.findViewById(R.id.catalog_URL));
        hashURL.attachToEditText(views.findViewById(R.id.hash_URL));
        autoUpdate.attachToCheckBox(views.findViewById(R.id.autoUpdate));

        // prepare dialog
        AlertDialog dialog = new AlertDialog.Builder(cntx)
                .setView(views)
                .setPositiveButton(R.string.mClear_updateNow, null) // set below
                .setNeutralButton(R.string.mClear_restore, null) // set below
                .setCancelable(true)
                .show();

        // prepare more dialog
        // these are configured here to allow auto-closing the dialog when they are pressed
        Button updateNow = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        updateNow.setOnClickListener(v -> {
            // updates the rules
            updateNow.setEnabled(false);
            new Thread(() -> {
                // run
                int toast = _updateCatalog();

                // update
                cntx.runOnUiThread(() -> {
                    updateNow.setEnabled(true);
                    Toast.makeText(cntx, toast, Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            // clear and reload
            catalogURL.clear();
            hashURL.clear();
            dialog.dismiss();
            showUpdater();
        });
    }

    // ------------------- internal -------------------

    /**
     * If the catalog is old, updates it in background. Otherwise does nothing.
     */
    private void updateIfNecessary() {
        if (autoUpdate.get() && lastUpdate.get() + AUTOUPDATE_PERIOD < System.currentTimeMillis()) {
            new Thread(() -> {
                // run
                int toast = _updateCatalog();

                // don't show message to user, but log it
                Log.d("UPDATE", cntx.getString(toast));
            }).start();
        }
    }

    /**
     * Replaces the catalog with a new one.
     * Network operation, designed to be run in a background thread.
     * Returns the message to display to the user about the result
     */
    private int _updateCatalog() {
        // read content
        String rawRules;
        try {
            rawRules = StreamUtils.readFromUrl(catalogURL.get());
        } catch (IOException e) {
            e.printStackTrace();
            return R.string.mClear_urlError;
        }

        // check hash if provided
        if (!hashURL.get().trim().isEmpty()) {

            // read hash
            String hash;
            try {
                hash = StreamUtils.readFromUrl(hashURL.get()).trim();
            } catch (IOException e) {
                e.printStackTrace();
                return R.string.mClear_hashError;
            }

            // if different, notify
            if (!StreamUtils.sha256(rawRules).equalsIgnoreCase(hash)) {
                return R.string.mClear_hashMismatch;
            }
        }

        // parse json
        JSONObject json;
        try {
            json = new JSONObject(rawRules);
        } catch (JSONException e) {
            e.printStackTrace();
            return R.string.toast_invalid;
        }

        // valid, save and update
        if (setRules(json, true)) {
            lastUpdate.set(System.currentTimeMillis());
            return R.string.mClear_updated;
        } else {
            return R.string.toast_invalid;
        }

    }
}
