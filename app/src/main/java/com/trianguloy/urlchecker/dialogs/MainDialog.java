package com.trianguloy.urlchecker.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.utilities.Inflater;

import java.util.ArrayList;
import java.util.List;

/**
 * The main dialog, when opening a url
 */
public class MainDialog extends Activity {

    // ------------------- module functions -------------------

    /**
     * to avoid infinite loops when setting urls
     */
    boolean onSettingUrl = false;
    private LinearLayout ll_mods;

    /**
     * A module changed the url
     *
     * @param url            the new url
     * @param providerModule which module changed it (null if first change)
     */
    public void setUrl(String url, AModuleDialog providerModule) {
        if (onSettingUrl) {
            // a recursive call, invalid
            if (BuildConfig.DEBUG) {
                // in debug mode, assert
                throw new AssertionError("Attempting to change an url inside a setUrl call");
            } else {
                // non-debug, just discard
                return;
            }
        }

        // change url
        this.url = url;

        // and notify the other modules
        onSettingUrl = true;
        onChangedUrl(providerModule);
        onSettingUrl = false;
    }

    /**
     * @return the current url
     */
    public String getUrl() {
        return url;
    }

    // ------------------- data -------------------

    /**
     * All active modules
     */
    private final List<AModuleDialog> modules = new ArrayList<>();

    // the current url
    private String url;

    // ------------------- initialize -------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main);

        // get views
        ll_mods = findViewById(R.id.middle_modules);

        // initialize
        initializeModules();

        // load url
        setUrl(getOpenUrl(), null);
    }

    /**
     * Initializes the modules
     */
    private void initializeModules() {
        modules.clear();

        // top module
        initializeModule(ModuleManager.topModule, false);

        // middle modules
        final List<AModuleData> middleModules = ModuleManager.getEnabledMiddleModules(this);
        for (AModuleData module : middleModules) {
            initializeModule(module, true);
        }

        // bottom module
        initializeModule(ModuleManager.bottomModule, false);
    }

    /**
     * Initializes a module by registering with this dialog and adding to the list
     *
     * @param moduleData which module to initialize
     */
    private void initializeModule(AModuleData moduleData, boolean decorations) {
        try {
            // enabled, add
            AModuleDialog module = moduleData.getDialog(this);

            ViewGroup parent;
            // set module block
            if (decorations) {
                View block = Inflater.inflate(R.layout.dialog_module, ll_mods, this);
                final TextView title = block.findViewById(R.id.title);
                title.setText(getString(R.string.dd, getString(moduleData.getName())));
                parent = block.findViewById(R.id.mod);
            } else {
                parent = ll_mods;
            }

            // set module content
            View child = Inflater.inflate(module.getLayoutId(), parent, this);
            module.onInitialize(child);

            modules.add(module);
        } catch (Exception e) {
            // can't add module
            e.printStackTrace();
        }
    }

    /**
     * @return the url that this activity was opened with (intent uri or sent text)
     */
    private String getOpenUrl() {
        // get the intent
        Intent intent = getIntent();
        if (intent == null) return invalid();

        // check the action
        String action = getIntent().getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            // sent text
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText == null) return invalid();
            return sharedText.trim();
        } else if (Intent.ACTION_VIEW.equals(action)) {
            // view url
            Uri uri = intent.getData();
            if (uri == null) return invalid();
            return uri.toString();
        } else {
            // other
            return invalid();
        }
    }

    /**
     * @return null, finishes the activity and shows a toast
     */
    private String invalid() {
        // for an invalid parameter
        Toast.makeText(this, R.string.toast_invalid, Toast.LENGTH_SHORT).show();
        finish();
        return null;
    }

    // ------------------- url -------------------

    /**
     * Notifies all modules (except the one that changed it) of a new url
     *
     * @param providerModule the module that provided the new url, won't be called (if null all are called)
     */
    private void onChangedUrl(AModuleDialog providerModule) {
        for (AModuleDialog module : modules) {
            if (module != providerModule)
                module.onNewUrl(getUrl());
        }
    }

}