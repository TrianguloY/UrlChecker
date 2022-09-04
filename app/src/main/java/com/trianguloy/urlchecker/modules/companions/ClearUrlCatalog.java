package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
    private final GenericPref.Lng lastUpdate = new GenericPref.Lng("clearurl_lastUpdate", 1661990400000L); // time when the data.minify.json asset file was updated (floored to previous UTC day)
    private final GenericPref.Lng lastCheck = new GenericPref.Lng("clearurl_lastCheck", -1L);
    private final GenericPref.Bool lastAuto = new GenericPref.Bool("clearurl_lastAuto", false);

    /* ------------------- constructor ------------------- */

    private final Activity cntx;

    public ClearUrlCatalog(Activity cntx) {
        this.cntx = cntx;
        catalogURL.init(cntx);
        hashURL.init(cntx);
        autoUpdate.init(cntx);
        lastUpdate.init(cntx);
        lastCheck.init(cntx);
        lastAuto.init(cntx);
        custom.init(cntx);
        builtIn.init(cntx);

        updateIfNecessary();
    }

    /* ------------------- catalog ------------------- */

    /**
     * Returns the catalog content
     */
    public JSONObject getCatalog() {
        // get the updated file first
        String internal = custom.get();
        if (internal != null) return JavaUtilities.toJson(internal);

        // no updated file or can't read, use built-in one
        return getBuiltIn();
    }

    /**
     * Returns the built-in catalog
     */
    public JSONObject getBuiltIn() {
        // read internal file
        String builtIn = this.builtIn.get();
        if (builtIn != null) return JavaUtilities.toJson(builtIn);

        // can't read either? panic! return empty
        return JavaUtilities.toJson("{\"providers\":{}}");
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
            JSONObject json = clearUrlCatalog.getCatalog();

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
     * For {@link this#setRules(JSONObject, boolean)} return value
     */
    enum Result {
        UP_TO_DATE,
        UPDATED,
        ERROR
    }

    /**
     * Saves a new local catalog. Returns if it was up to date, updated or an error happened.
     * When merge is true, only the top-objects with the same key are replaced.
     */
    public Result setRules(JSONObject rules, boolean merge) {
        // merge rules if required
        if (merge) {
            try {
                // replace only the top objects
                JSONObject merged = getCatalog();
                for (String key : JavaUtilities.toList(rules.keys())) {
                    merged.put(key, rules.getJSONObject(key));
                }
                rules = merged;
            } catch (JSONException e) {
                e.printStackTrace();
                // can't be parsed
                return Result.ERROR;
            }
        }
        // compact
        String content = rules.toString();

        // nothing changed, nothing to update
        if (content.equals(getCatalog().toString())) {
            return Result.UP_TO_DATE;
        }

        // same as builtin (probably a reset), clear custom
        if (content.equals(getBuiltIn().toString())) {
            clear();
            return Result.UPDATED;
        }

        // something new, save
        return custom.set(content) ? Result.UPDATED : Result.ERROR;
    }

    /**
     * Deletes the custom catalog, built-in one will be returned afterwards
     */
    public void clear() {
        custom.delete();
        lastUpdate.clear();
        lastCheck.clear();
        lastAuto.clear();
    }

    // ------------------- dialogs -------------------

    /**
     * Show the rules editor dialog
     */
    public void showEditor() {
        JsonEditor.show(getCatalog(), getBuiltIn(), R.string.mClear_editor, cntx, content -> {
            if (setRules(content, false) != Result.ERROR) {
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

        // info
        TextView txt_check = views.findViewById(R.id.last_check);
        TextView txt_update = views.findViewById(R.id.last_update);
        Runnable update = () -> {
            txt_check.setText(AndroidUtils.formatMillis(lastCheck.get(), cntx)
                    + (lastAuto.get() ? " [" + cntx.getString(R.string.auto) + "]" : "")
            );
            txt_update.setText(AndroidUtils.formatMillis(lastUpdate.get(), cntx));
        };
        update.run();

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
                lastAuto.set(false);
                int toast = _updateCatalog();

                // update
                cntx.runOnUiThread(() -> {
                    updateNow.setEnabled(true);
                    update.run();
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
                lastAuto.set(true);
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
        long now = System.currentTimeMillis();
        lastCheck.set(now);

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
        switch (setRules(json, true)) {
            case UPDATED:
                lastUpdate.set(now);
                return R.string.mClear_updated;
            case UP_TO_DATE:
                return R.string.mClear_upToDate;
            case ERROR:
            default:
                return R.string.toast_invalid;
        }

    }
}
