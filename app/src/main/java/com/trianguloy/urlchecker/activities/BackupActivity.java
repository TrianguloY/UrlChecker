package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;
import com.trianguloy.urlchecker.utilities.wrappers.ZipReader;
import com.trianguloy.urlchecker.utilities.wrappers.ZipWriter;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupActivity extends Activity {

    public static final String PREF_VALUE = "value";
    public static final String PREF_TYPE = "type";
    public static final String PREFERENCES = "preferences";
    public static final String FILES_FOLDER = "files/";

    private SharedPreferences prefs;

    /* ------------------- activity ------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        AndroidSettings.setLocale(this);
        setContentView(R.layout.activity_backup);
        setTitle(R.string.btn_bckp);
        AndroidUtils.configureUp(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // press the 'back' button in the action bar to go back
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* ------------------- backup ------------------- */

    public void backup(View view) {
        // choose backup name
        var input = new EditText(this);
        input.setText(getOutputFile());
        new AlertDialog.Builder(this)
                .setTitle("Choose backup name")
                .setMessage("Name of the backup.\nThe file will be created in the " + getOutputFolder() + " directory.")
                .setView(input)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.btn_bckp, (dialog, which) -> backup(input.getText().toString()))
                .show();
    }

    /**
     * Creates a backup and saves it to [fileName]
     */
    private void backup(String fileName) {
        try (var file = new ZipWriter(new File(getOutputFolder(), fileName), getString(R.string.app_name) + " backup")) {

            // version
            file.addStringFile("version", BuildConfig.VERSION_NAME);

            // readme
            try (var readme = getAssets().open("backup_readme.txt")) {
                file.addStreamFile("readme.txt", readme);
            }

            // preferences
            var jsonPrefs = new JSONObject();
            for (var entry : prefs.getAll().entrySet()) {
                jsonPrefs.put(entry.getKey(), new JSONObject()
                        .put(PREF_VALUE, entry.getValue())
                        .put(PREF_TYPE, entry.getValue().getClass().getSimpleName()));
            }
            file.addStringFile(PREFERENCES, jsonPrefs.toString());

            // other files
            for (var otherFile : fileList()) {
                try (var in = openFileInput(otherFile)) {
                    file.addStreamFile(FILES_FOLDER + otherFile, in);
                }
            }

            Toast.makeText(this, "Backup created", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Can't create backup", Toast.LENGTH_LONG).show();
        }
    }

    /* ------------------- restore ------------------- */

    public void restore(View view) {
        // choose backup file
        var intent = new Intent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getOutputFolder());
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "Select backup file"), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 0) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show();
            return;
        }
        restore(data.getData());
    }

    /**
     * Restores a backup from an uri
     */
    private void restore(Uri inputFile) {
        try (var zip = new ZipReader(inputFile, this)) {

            // get preferences
            var preferences = zip.getFileString(PREFERENCES);
            if (preferences != null) {
                var jsonPrefs = new JSONObject(preferences);
                var editor = prefs.edit().clear();
                for (var key : JavaUtils.toList(jsonPrefs.keys())) {
                    var ent = jsonPrefs.getJSONObject(key);
                    switch (ent.getString(PREF_TYPE)) {
                        case "String" -> editor.putString(key, ent.getString(PREF_VALUE));
                        case "Integer" -> editor.putInt(key, ent.getInt(PREF_VALUE));
                        case "Long" -> editor.putLong(key, ent.getLong(PREF_VALUE));
                        case "Boolean" -> editor.putBoolean(key, ent.getBoolean(PREF_VALUE));
                        default -> throw new RuntimeException("Unknown type: " + ent.getString(PREF_TYPE));
                    }
                    editor.apply();
                }
            }

            // internal files
            for (var file : fileList()) deleteFile(file);
            for (var fileName : zip.fileNames()) {
                if (!fileName.startsWith(FILES_FOLDER)) continue;
                try (var out = openFileOutput(fileName.substring(6), MODE_PRIVATE)) {
                    zip.getFileStream(fileName, out);
                }
            }

            Toast.makeText(this, "Restored backup", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to restore the backup file", Toast.LENGTH_SHORT).show();
        }

    }

    private File getOutputFolder() {
        File folder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            folder = getSystemService(StorageManager.class).getPrimaryStorageVolume().getDirectory();
        } else {
            folder = Environment.getExternalStorageDirectory();
        }
        folder = new File(folder, Environment.DIRECTORY_DOWNLOADS);
        folder.mkdirs();
        return folder;
    }

    private String getOutputFile() {
        return "UrlChecker_" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + ".backup";
    }
}
