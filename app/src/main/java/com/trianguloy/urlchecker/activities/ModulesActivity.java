package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.utilities.GenericPref;

/**
 * An activity that shows the list of modules that can be enabled/disabled
 */
public class ModulesActivity extends Activity {

    private LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);
        list = findViewById(R.id.list);

        initialize();
    }

    private void initialize() {

        initModule(ModuleManager.topModule, false);

        for (AModuleData module : ModuleManager.toggleableModules) {
            initModule(module, true);
        }

        initModule(ModuleManager.bottomModule, false);
    }

    private void initModule(AModuleData module, boolean enableable) {
        // inflate
        View views = getLayoutInflater().inflate(R.layout.conf_module, list, false);
        list.addView(views); // separated to return the inflated view instead of the parent

        // configure enable toggle
        Switch toggleEnable = views.findViewById(R.id.enable);
        if (enableable) {
            final GenericPref.Bool enabled_pref = ModuleManager.getEnabledPrefOfModule(module, this);
            toggleEnable.setChecked(enabled_pref.get());
            toggleEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    enabled_pref.set(isChecked);
                }
            });
        } else {
            toggleEnable.setChecked(true);
            toggleEnable.setEnabled(false);
        }

        // configure info
        ((TextView) views.findViewById(R.id.label)).setText(module.getName());
        ((TextView) views.findViewById(R.id.desc)).setText(module.getDescription());

        // configure toggleable description
        final View details_cont = views.findViewById(R.id.details);
        View toggle = views.findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = v.getTag() != null;
                v.setTag(checked ? null : new Object());
                details_cont.setVisibility(checked ? View.VISIBLE : View.GONE);
                ((ImageView) v).setImageResource(checked ? R.drawable.expanded : R.drawable.collapsed);
            }
        });
        toggle.performClick();

        // add configurations
    }
}