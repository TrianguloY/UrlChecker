package com.trianguloy.urlchecker.dialogs;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.modules.ModuleData;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.BaseModule;

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
    public void setUrl(String url, BaseModule providerModule) {
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
    private final List<BaseModule> modules = new ArrayList<>();

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
        initializeModule(ModuleData.topModule);

        // middle modules
        final List<ModuleData> middleModules = ModuleManager.getMiddleModules(this);
        for (ModuleData module : middleModules) {

            // set title
            String name = module.name;
            if (name != null) {
                final TextView title = new TextView(this);
                title.setText(name + ":");
                ll_mods.addView(title);
            }

            // set content
            initializeModule(module);
        }

        // bottom module
        initializeModule(ModuleData.bottomModule);
    }

    /**
     * Initializes a module by registering with this dialog and adding to the list
     *
     * @param moduleData which module to initialize
     */
    private void initializeModule(ModuleData moduleData) {
        try {
            // enabled, add
            BaseModule module = moduleData.dialogClass.getDeclaredConstructor(MainDialog.class).newInstance(this);
            View views = getLayoutInflater().inflate(module.getLayoutDialog(), ll_mods,false);
            ll_mods.addView(views); // separated to return the inflated view instead of the parent
            module.onInitialize(views);
            modules.add(module);
        } catch (Exception e) {
            // can't add module
            e.printStackTrace();
        }
    }

    /**
     * @return the url that this activity was opened with (intent uri)
     */
    private String getOpenUrl() {

        // get data
        Uri uri = this.getIntent().getData();

        if (uri == null) {
            // check in case someone opens this without url
            Toast.makeText(this, "No url!!!!", Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }

        // return
        return uri.toString();
    }

    // ------------------- url -------------------

    /**
     * Notifies all modules (except the one that changed it) of a new url
     *
     * @param providerModule the module that provided the new url, won't be called (if null all are called)
     */
    private void onChangedUrl(BaseModule providerModule) {
        for (BaseModule module : modules) {
            if (module != providerModule)
                module.onNewUrl(getUrl());
        }
    }

}