package com.trianguloy.urlchecker;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.modules.BaseModule;
import com.trianguloy.urlchecker.modules.OpenModule;

import java.util.ArrayList;
import java.util.List;

public class MainDialog extends Activity implements TextWatcher {

    public void setUrl(String url) {
        txt_url.setText(url);
        //onChangedUrl(); // not needed, the textwatcher does it
    }

    public String getUrl() {
        return txt_url.getText().toString();
    }

    // ------------------- data -------------------

    private final List<BaseModule> modules = new ArrayList<>();
    private TextView txt_url;
    private LinearLayout ll_mods;

    // ------------------- initialize -------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main);

        txt_url = findViewById(R.id.url);
        txt_url.addTextChangedListener(this);

        ll_mods = findViewById(R.id.mods);

        initialize();

        setUrl(getOpenUrl());

    }

    private void initialize() {
        modules.addAll(ModuleManager.getEnabled(this));
        for (BaseModule module : modules) {

            // set title
            String name = module.getName();
            if(name != null) {
                final TextView title = new TextView(this);
                title.setText(name + ":");
                ll_mods.addView(title);
            }

            // set content
            View views = getLayoutInflater().inflate(module.getLayoutBase(), ll_mods);
            module.setContext(this);
            module.initialize(views);
        }

        // bottom module (open)
        OpenModule openModule = new OpenModule();
        openModule.setContext(this);
        openModule.initialize(findViewById(R.id.open_module));
        modules.add(openModule);
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

    private void onChangedUrl() {
        for (BaseModule module : modules) {
            module.onNewUrl(getUrl());
        }
    }

    // ------------------- TextWatcher -------------------

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        onChangedUrl();
    }
}