package com.trianguloy.urlchecker;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        initializeModule(ModuleManager.getTopModule(), findViewById(R.id.top_module));

        // middle modules
        final List<BaseModule> middleModules = ModuleManager.getMiddleModules(this);
        LinearLayout ll_mods = findViewById(R.id.middle_modules);
        for (BaseModule module : middleModules) {

            // set title
            String name = module.getName();
            if (name != null) {
                final TextView title = new TextView(this);
                title.setText(name + ":");
                ll_mods.addView(title);
            }

            // set content
            initializeModule(module,
                    getLayoutInflater().inflate(module.getLayoutBase(), ll_mods)
            );
        }

        // bottom module
        initializeModule(ModuleManager.getBottomModule(), findViewById(R.id.bottom_module));
    }

    /**
     * Initializes a module by registering with this dialog and adding to the list
     *
     * @param module which module to initialize
     * @param views  the views of that module
     */
    private void initializeModule(BaseModule module, View views) {
        module.registerDialog(this);
        module.initialize(views);
        modules.add(module);
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