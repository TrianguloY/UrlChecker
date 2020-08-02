package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.ModuleData;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.utilities.GenericPref;

import java.util.List;

/**
 * An activity that shows the list of modules that can be enabled/disabled
 */
public class ModulesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);

        initialize();
    }

    private void initialize() {
        LinearLayout ll = findViewById(R.id.list);

        List<ModuleData> modules = ModuleData.toggleableModules;

        for (ModuleData module : modules) {
            // inflate
            View views = getLayoutInflater().inflate(R.layout.conf_module, ll, false);
            ll.addView(views); // separated to return the inflated view instead of the parent

            // configure
            Switch enabled = views.findViewById(R.id.enable);
            final GenericPref.Bool enabled_pref = ModuleManager.getEnabledPrefOfModule(module, this);
            enabled.setChecked(enabled_pref.get());
            enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    enabled_pref.set(isChecked);
                }
            });
            ((TextView) views.findViewById(R.id.label)).setText(module.name);
        }

    }
}