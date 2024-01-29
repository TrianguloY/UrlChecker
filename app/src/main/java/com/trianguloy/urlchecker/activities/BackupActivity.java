package com.trianguloy.urlchecker.activities;

import static com.trianguloy.urlchecker.utilities.methods.JavaUtils.negate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.companions.Hosts;
import com.trianguloy.urlchecker.modules.list.VirusTotalModule;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils.Function;
import com.trianguloy.urlchecker.utilities.wrappers.ProgressDialog;
import com.trianguloy.urlchecker.utilities.wrappers.ZipReader;
import com.trianguloy.urlchecker.utilities.wrappers.ZipWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupActivity extends Activity {

    private Switch chk_prefs;
    private Switch chk_secrets;
    private Switch chk_files;
    private Switch chk_cache;
    private Switch chk_delete;
    private SharedPreferences prefs;

    /* ------------------- activity ------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        AndroidSettings.setLocale(this);
        setContentView(R.layout.activity_backup);
        setTitle(R.string.btn_backup);
        AndroidUtils.configureUp(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        chk_prefs = findViewById(R.id.chk_prefs);
        chk_secrets = findViewById(R.id.chk_secrets);
        chk_files = findViewById(R.id.chk_files);
        chk_cache = findViewById(R.id.chk_cache);
        chk_delete = findViewById(R.id.chk_delete);

        // if this app was reloaded, some settings may have changed, so reload previous one too
        if (AndroidSettings.wasReloaded(this)) AndroidSettings.markForReloading(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_backup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home -> {
                // press the 'back' button in the action bar to go back
                onBackPressed();
                return true;
            }
            case R.id.menu_advanced -> {
                // show advanced
                item.setVisible(false);
                chk_delete.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /* ------------------- backup ------------------- */

    public void backup(View ignored) {
        // choose backup name
        var input = new EditText(this);
        input.setText(getOutputFile());
        var dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.bck_backupTitle)
                .setMessage(getString(R.string.bck_backupMessage, getOutputFolder()))
                .setView(input)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.btn_backup, null)
                .show();

        // positive button: confirm if file exists (run afterwards to avoid auto-dismiss)
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            var file = new File(getOutputFolder(), input.getText().toString());

            if (file.exists()) {
                // confirm
                new AlertDialog.Builder(this)
                        .setTitle(R.string.fileExists)
                        .setMessage(R.string.overrideFile)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.btn_replace, (d, w) -> {
                            dialog.dismiss();
                            backup(file);
                        })
                        .show();
            } else {
                // run directly
                dialog.dismiss();
                backup(file);
            }
        });
    }

    /**
     * Creates a backup and saves it to [file]
     */
    private void backup(File file) {
        ProgressDialog.run(this, R.string.btn_backup, progress -> {

            progress.setMax(7);
            progress.setMessage("Initializing backup");
            try (var zip = new ZipWriter(file, getString(R.string.app_name) + " backup")) {

                // version
                progress.setMessage("Adding version");
                progress.increaseProgress();
                zip.addStringFile("version", BuildConfig.VERSION_NAME);

                // readme
                progress.setMessage("Adding readme");
                progress.increaseProgress();
                try (var readme = getAssets().open("backup_readme.txt")) {
                    zip.addStreamFile("readme.txt", readme);
                }

                // rest of preferences
                progress.setMessage("Adding preferences");
                progress.increaseProgress();
                if (chk_prefs.isChecked()) backupPreferencesMatching(FILE_PREFERENCES, negate(IS_PREF_SECRET), zip);

                // secret preferences
                progress.setMessage("Adding secrets");
                progress.increaseProgress();
                if (chk_secrets.isChecked()) backupPreferencesMatching(FILE_SECRETS, IS_PREF_SECRET, zip);

                // rest of files
                progress.setMessage("Adding files");
                progress.increaseProgress();
                if (chk_files.isChecked()) backupFilesMatching(FILES_FOLDER, negate(IS_FILE_CACHE), zip);

                // cache files
                progress.setMessage("Adding cache");
                progress.increaseProgress();
                if (chk_cache.isChecked()) backupFilesMatching(CACHE_FOLDER, IS_FILE_CACHE, zip);

                runOnUiThread(() -> Toast.makeText(this, R.string.bck_backupOk, Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, R.string.bck_backupError, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void backupPreferencesMatching(String fileName, Function<String, Boolean> predicate, ZipWriter zip) throws IOException, JSONException {
        var jsonPrefs = new JSONObject();
        for (var entry : prefs.getAll().entrySet()) {
            if (!predicate.apply(entry.getKey())) continue;
            jsonPrefs.put(entry.getKey(), new JSONObject()
                    .put(PREF_VALUE, entry.getValue())
                    .put(PREF_TYPE, entry.getValue().getClass().getSimpleName()));
        }
        zip.addStringFile(fileName, jsonPrefs.toString());
    }

    private void backupFilesMatching(String folder, Function<String, Boolean> predicate, ZipWriter zip) throws IOException {
        var empty = true;
        for (var file : fileList()) {
            if (!predicate.apply(file)) continue;
            try (var in = openFileInput(file)) {
                zip.addStreamFile(folder + file, in);
                empty = false;
            }
        }
        if (empty) zip.addStringFile(folder + EMPTY, "");
    }

    /* ------------------- restore ------------------- */

    public void restore(View ignored) {
        // choose backup file
        var intent = new Intent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getOutputFolder());
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, getString(R.string.bck_restoreTitle)), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 0) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(this, R.string.canceled, Toast.LENGTH_SHORT).show();
            return;
        }
        restore(data.getData());
    }

    /**
     * Restores a backup from an uri
     */
    private void restore(Uri inputFile) {
        ProgressDialog.run(this, R.string.btn_restore, progress -> {
            progress.setMax(5);
            progress.setMessage("Loading backup");
            try (var zip = new ZipReader(inputFile, this)) {

                // rest of preferences
                progress.setMessage("Restoring preferences");
                progress.increaseProgress();
                if (chk_prefs.isChecked()) restorePreferencesMatching(FILE_PREFERENCES, negate(IS_PREF_SECRET), zip);

                // secret preferences
                progress.setMessage("Restoring secrets");
                progress.increaseProgress();
                if (chk_secrets.isChecked()) restorePreferencesMatching(FILE_SECRETS, IS_PREF_SECRET, zip);

                // rest of files
                progress.setMessage("Restoring files");
                progress.increaseProgress();
                if (chk_files.isChecked()) restoreFilesMatching(FILES_FOLDER, negate(IS_FILE_CACHE), zip);

                // cache files
                progress.setMessage("Restoring cache");
                progress.increaseProgress();
                if (chk_cache.isChecked()) restoreFilesMatching(CACHE_FOLDER, IS_FILE_CACHE, zip);

                runOnUiThread(() -> Toast.makeText(this, R.string.bck_restoreOk, Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, R.string.bck_restoreError, Toast.LENGTH_LONG).show());
            }

            runOnUiThread(() -> AndroidSettings.reload(this));
        });
    }

    private void restorePreferencesMatching(String fileName, Function<String, Boolean> predicate, ZipReader zip) throws IOException, JSONException {
        var preferences = zip.getFileString(fileName);
        if (preferences == null) return;

        var jsonPrefs = new JSONObject(preferences);
        var editor = prefs.edit();

        // remove
        if (chk_delete.isChecked()) {
            for (var key : prefs.getAll().keySet()) {
                if (predicate.apply(key)) editor.remove(key);
            }
        }

        // add
        for (var key : JavaUtils.toList(jsonPrefs.keys())) {
            var ent = jsonPrefs.getJSONObject(key);
            switch (ent.getString(PREF_TYPE)) {
                case "String" -> editor.putString(key, ent.getString(PREF_VALUE));
                case "Integer" -> editor.putInt(key, ent.getInt(PREF_VALUE));
                case "Long" -> editor.putLong(key, ent.getLong(PREF_VALUE));
                case "Boolean" -> editor.putBoolean(key, ent.getBoolean(PREF_VALUE));
                default -> AndroidUtils.assertError("Unknown type: " + ent.getString(PREF_TYPE));
            }
        }

        editor.apply();
    }

    private void restoreFilesMatching(String folder, Function<String, Boolean> predicate, ZipReader zip) throws IOException {
        var fileNames = zip.fileNames(folder);
        if (fileNames.isEmpty()) return;

        // delete
        if (chk_delete.isChecked()) {
            for (var file : fileList()) {
                if (predicate.apply(file)) deleteFile(file);
            }
        }

        // create
        for (var fileName : fileNames) {
            if ((folder + EMPTY).equals(fileName)) continue; // ignore marker
            try (var out = openFileOutput(fileName.substring(folder.length()), MODE_PRIVATE)) {
                zip.getFileStream(fileName, out);
            }
        }
    }

    /* ------------------- delete ------------------- */

    public void delete(View ignored) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.bck_deleteTitle)
                .setMessage(R.string.bck_deleteMessage)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.btn_delete, (d, w) -> delete())
                .show();
    }

    private void delete() {
        ProgressDialog.run(this, R.string.btn_restore, progress -> {
            progress.setMax(4);
            try {

                // rest of preferences
                progress.setMessage("Deleting preferences");
                if (chk_prefs.isChecked()) deletePreferencesMatching(negate(IS_PREF_SECRET));

                // secret preferences
                progress.setMessage("Deleting secrets");
                progress.increaseProgress();
                if (chk_secrets.isChecked()) deletePreferencesMatching(IS_PREF_SECRET);

                // rest of files
                progress.setMessage("Deleting files");
                progress.increaseProgress();
                if (chk_files.isChecked()) deleteFilesMatching(negate(IS_FILE_CACHE));

                // cache files
                progress.setMessage("Deleting cache");
                progress.increaseProgress();
                if (chk_cache.isChecked()) deleteFilesMatching(IS_FILE_CACHE);

                runOnUiThread(() -> Toast.makeText(this, R.string.bck_deleteOk, Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, R.string.bck_deleteError, Toast.LENGTH_SHORT).show());
            }

            runOnUiThread(() -> AndroidSettings.reload(this));
        });
    }

    private void deletePreferencesMatching(Function<String, Boolean> predicate) {
        var editor = prefs.edit();
        for (var key : prefs.getAll().keySet()) {
            if (predicate.apply(key)) editor.remove(key);
        }
        editor.apply();
    }

    private void deleteFilesMatching(Function<String, Boolean> predicate) {
        for (var file : fileList()) {
            if (predicate.apply(file)) deleteFile(file);
        }
    }

    /* ------------------- common ------------------- */

    private static final String FILE_PREFERENCES = "preferences";
    private static final String FILE_SECRETS = "secrets";
    private static final String FILES_FOLDER = "files/";
    private static final String CACHE_FOLDER = "cache/";
    private static final String PREF_VALUE = "value";
    private static final String PREF_TYPE = "type";
    private static final String EMPTY = ".empty";

    private static final Function<String, Boolean> IS_PREF_SECRET = VirusTotalModule.PREF::equals;
    private static final Function<String, Boolean> IS_FILE_CACHE = s -> s.startsWith(Hosts.PREFIX);

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
