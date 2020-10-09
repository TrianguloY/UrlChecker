package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.utilities.Animations;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.Inflater;

import java.util.HashMap;
import java.util.Map;

/**
 * An activity that shows the list of modules that can be enabled/disabled
 */
public class ConfigActivity extends Activity {

    private LinearLayout list;
    private Map<AModuleConfig, Switch> switches = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
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
        final AModuleConfig config = module.getConfig(this);

        // inflate
        View parent = Inflater.inflate(R.layout.config_module, list, this);
        Animations.enableAnimations(parent);

        // configure enable toggle
        Switch toggleEnable = parent.findViewById(R.id.enable);
        if (enableable) {
            final GenericPref.Bool enabled_pref = ModuleManager.getEnabledPrefOfModule(module, this);
            toggleEnable.setChecked(enabled_pref.get());
            toggleEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !config.canBeEnabled()) {
                        Toast.makeText(ConfigActivity.this, R.string.toast_cantEnable, Toast.LENGTH_LONG).show();
                        buttonView.setChecked(false);
                    } else {
                        enabled_pref.set(isChecked);
                    }
                }
            });
            switches.put(config, toggleEnable);
        } else {
            toggleEnable.setChecked(true);
            toggleEnable.setEnabled(false);
        }

        // configure label
        final TextView title = parent.findViewById(R.id.label);
        title.setText(getString(R.string.dd, getString(module.getName())));

        // configuration of the module
        final View child = Inflater.inflate(config.getLayoutId(), (ViewGroup) parent.findViewById(R.id.box), this);
        config.onInitialize(child);

        // configure toggleable description
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = child.getVisibility() == View.GONE;
                child.setVisibility(checked ? View.VISIBLE : View.GONE);
                title.setCompoundDrawablesWithIntrinsicBounds(checked ? R.drawable.expanded : R.drawable.collapsed, 0, 0, 0);
            }
        });
        title.performClick(); // initial hide
    }

    public void disableModule(AModuleConfig module) {
        final Switch vswitch = switches.get(module);
        if (vswitch != null) vswitch.setChecked(false);
    }
}