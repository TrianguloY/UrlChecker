package com.trianguloy.urlchecker.activities;

import static com.trianguloy.urlchecker.utilities.methods.StreamUtils.UTF_8;

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
import com.trianguloy.urlchecker.utilities.methods.StreamUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class BackupActivity extends Activity {

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
        var input = new EditText(this);
        input.setText(getOutputFile());
        new AlertDialog.Builder(this)
                .setTitle("Choose backup name")
                .setMessage("Name of the backup.\nThe file will be created in the " + getOutputFolder() + " directory.")
                .setView(input)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.btn_bckp, (dialog, which) -> {
                    backup(input.getText().toString());
                })
                .show();
    }

    private void backup(String fileName) {
        var file = new File(getOutputFolder(), fileName);
        file.delete();

        try (var zipStream = new ZipOutputStream(new FileOutputStream(file))) {
            zipStream.setComment(getString(R.string.app_name) + " backup");

            // version
            zipStream.putNextEntry(new ZipEntry("version"));
            zipStream.write(BuildConfig.VERSION_NAME.getBytes(UTF_8));

            // preferences
            zipStream.putNextEntry(new ZipEntry("preferences"));
            var jsonPrefs = new JSONObject();
            for (var entry : prefs.getAll().entrySet()) {
                jsonPrefs.put(entry.getKey(), new JSONObject()
                        .put("value", entry.getValue())
                        .put("type", entry.getValue().getClass().getSimpleName()));
            }
            zipStream.write(jsonPrefs.toString().getBytes(UTF_8));

            // other files
            for (var otherFile : fileList()) {
                zipStream.putNextEntry(new ZipEntry("files/" + otherFile));

                try (var in = openFileInput(otherFile)) {
                    StreamUtils.inputStream2OutputStream(in, zipStream);
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "Can't create backup", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Backup created", Toast.LENGTH_SHORT).show();
    }

    /* ------------------- restore ------------------- */

    public void restore(View view) {
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

    private void restore(Uri inputFile) {
        // copy to temporal file to allow using ZipFile
        var zipFile = new File(getCacheDir(), "backup");
        zipFile.delete();
        try (var in = getContentResolver().openInputStream(inputFile)) {
            StreamUtils.inputStream2File(in, zipFile);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to load the backup", Toast.LENGTH_SHORT).show();
            zipFile.delete();
            return;
        }

        try (var zip = new ZipFile(zipFile)) {
            // get preferences
            var preferences = zip.getEntry("preferences");
            if (preferences != null) {
                var jsonPrefs = new JSONObject(StreamUtils.inputStream2String(zip.getInputStream(preferences)));
                var editor = prefs.edit().clear();
                for (var key : JavaUtils.toList(jsonPrefs.keys())) {
                    var ent = jsonPrefs.getJSONObject(key);
                    switch (ent.getString("type")) {
                        case "String" -> editor.putString(key, ent.getString("value"));
                        case "Integer" -> editor.putInt(key, ent.getInt("value"));
                        case "Long" -> editor.putLong(key, ent.getLong("value"));
                        case "Boolean" -> editor.putBoolean(key, ent.getBoolean("value"));
                        default -> throw new RuntimeException("Unknown type: " + ent.getString("type"));
                    }
                    editor.apply();
                }
            }

            // internal files
            var entries = zip.entries();
            for (var file : fileList()) deleteFile(file);
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var name = entry.getName();
                if (!name.startsWith("files/")) continue;
                name = name.substring(6);
                try (var in = zip.getInputStream(entry)) {
                    try (var out = openFileOutput(name, MODE_PRIVATE)) {
                        StreamUtils.inputStream2OutputStream(in, out);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to parse the backup file", Toast.LENGTH_SHORT).show();
            return;
        } finally {
            zipFile.delete();
        }

        Toast.makeText(this, "Restored backup", Toast.LENGTH_SHORT).show();
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
