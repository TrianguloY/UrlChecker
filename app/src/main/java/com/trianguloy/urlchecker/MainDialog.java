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

public class MainDialog extends Activity {

    boolean onSettingUrl = false;

    public void setUrl(String url, BaseModule providerModule) {
        if (BuildConfig.DEBUG && onSettingUrl) {
            throw new AssertionError("Attempting to change an url inside a setUrl call");
        }
        this.url = url;
        onSettingUrl = true;
        onChangedUrl(providerModule);
        onSettingUrl = false;
    }

    public String getUrl() {
        return url;
    }

    // ------------------- data -------------------

    private final List<BaseModule> modules = new ArrayList<>();
    private String url;
    private LinearLayout ll_mods;

    // ------------------- initialize -------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main);

        ll_mods = findViewById(R.id.middle_modules);

        initialize();

        setUrl(getOpenUrl(), null);

    }

    private void initialize() {
        modules.clear();

        // top module
        initializeModule(ModuleManager.getTopModule(), findViewById(R.id.top_module));

        final List<BaseModule> middleModules = ModuleManager.getMiddleModules(this);
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

    private void initializeModule(BaseModule module, View views){
        module.registerDialog(this);
        module.initialize(views);
        modules.add(module);
    }

    private String getOpenUrl() {
        Uri uri = this.getIntent().getData();
        if (uri == null) {
            Toast.makeText(this, "No url!!!!", Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        return uri.toString();
    }

    // ------------------- url -------------------

    private void onChangedUrl(BaseModule providerModule) {
        for (BaseModule module : modules) {
            if (module != providerModule)
                module.onNewUrl(getUrl());
        }
    }

}