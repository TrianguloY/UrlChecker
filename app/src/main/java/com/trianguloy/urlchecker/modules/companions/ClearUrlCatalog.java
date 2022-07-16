package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Manages the local catalog with the rules
 */
public class ClearUrlCatalog {

    /* ------------------- constants ------------------- */

    private static final String fileName = "data.minify.json";
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

        updateIfNecessary();
    }

    /* ------------------- catalog ------------------- */

    /**
     * Returns the catalog content as text
     */
    public String getRaw() {
        // get the updated file first
        try {
            return StreamUtils.inputStream2String(cntx.openFileInput(fileName));
        } catch (IOException ignored) {
        }

        // no updated file or can't read, use built-in one
        try {
            return StreamUtils.inputStream2String(cntx.getAssets().open(fileName));
        } catch (IOException ignored) {
        }

        // can't read either? panic! return empty
        return "{\"providers\":{}}";
    }

    /**
     * Parses and returns the providers from the catalog
     */
    public static JSONObject getProviders(Activity cntx) {
        try {
            return new JSONObject(new ClearUrlCatalog(cntx).getRaw())
                    .getJSONObject("providers");
        } catch (JSONException e) {
            // invalid catalog, return empty
            AndroidUtils.assertError(e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * Returns the rules formatted
     */
    public String getFormattedRules() {
        try {
            return new JSONObject(getRaw()).toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Saves a new local catalog. Returns true if it was saved correctly, false on error
     */
    public boolean setRules(String content) {
        // the same, already saved
        if (content.equals(getRaw())) return true;

        // first test if it can be parsed
        try {
            new JSONObject(content).getJSONObject("providers");
        } catch (JSONException e) {
            e.printStackTrace();
            // can't be parsed
            return false;
        }

        // store json
        try (FileOutputStream fos = cntx.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StreamUtils.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes the custom catalog, built-in one will be returned afterwards
     */
    public void clear() {
        cntx.deleteFile(fileName);
    }

    // ------------------- dialogs -------------------

    /**
     * Show the rules editor dialog
     */
    public void showEditor() {
        // prepare dialog content
        View views = cntx.getLayoutInflater().inflate(R.layout.config_clearurls_editor, null);

        // init rules
        EditText rules = views.findViewById(R.id.rules);
        rules.setText(getFormattedRules());

        // formatter
        views.findViewById(R.id.format).setOnClickListener(v -> {
            try {
                rules.setText(new JSONObject(rules.getText().toString()).toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(cntx, R.string.toast_invalid, Toast.LENGTH_SHORT).show();
            }
        });

        // prepare dialog
        AlertDialog dialog = new AlertDialog.Builder(cntx)
                .setView(views)
                .setPositiveButton(R.string.save, null) // set below
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.reset, null) // set below
                .setCancelable(false)
                .show();

        // prepare more dialog
        // these are configured here to allow auto-closing the dialog when they are pressed
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (setRules(rules.getText().toString())) {
                // saved data, close dialog
                dialog.dismiss();
            } else {
                // invalid data, keep dialog and show why
                Toast.makeText(cntx, R.string.toast_invalid, Toast.LENGTH_LONG).show();
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            // clear catalog and reload internal
            clear();
            rules.setText(getFormattedRules());
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

        // valid, save and update
        if (setRules(rawRules)) {
            lastUpdate.set(System.currentTimeMillis());
            return R.string.mClear_updated;
        } else {
            return R.string.toast_invalid;
        }

    }
}
