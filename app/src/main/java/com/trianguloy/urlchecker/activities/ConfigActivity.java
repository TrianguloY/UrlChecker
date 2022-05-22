package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity that shows the list of modules that can be enabled/disabled
 */
public class ConfigActivity extends Activity {

    private LinearLayout list;
    private final Map<AModuleConfig, Switch> switches = new HashMap<>();
    private final GenericPref.LstStr order = ModuleManager.ORDER_PREF();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        list = findViewById(R.id.list);
        order.init(this);

        // initialize modules
        initModule(ModuleManager.topModule, false);

        for (AModuleData module : ModuleManager.getMiddleModules(true, this)) {
            initModule(module, true);
        }

        initModule(ModuleManager.bottomModule, false);

        // init buttons
        updateMovableButtons();
    }

    /**
     * Initializes and adds a module to the list
     */
    private void initModule(AModuleData module, boolean enableable) {
        final AModuleConfig config = module.getConfig(this);

        // inflate
        View parent = Inflater.inflate(R.layout.config_module, list, this);
        parent.setTag(module.getId());
        Animations.enableAnimations(parent);

        // configure enable toggle
        Switch toggleEnable = parent.findViewById(R.id.enable);
        if (enableable) {
            final GenericPref.Bool enabled_pref = ModuleManager.getEnabledPrefOfModule(module, this);
            toggleEnable.setChecked(enabled_pref.get());
            toggleEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !config.canBeEnabled()) {
                    Toast.makeText(ConfigActivity.this, R.string.toast_cantEnable, Toast.LENGTH_LONG).show();
                    buttonView.setChecked(false);
                } else {
                    enabled_pref.set(isChecked);
                }
            });
            switches.put(config, toggleEnable);
        } else {
            toggleEnable.setChecked(true);
            toggleEnable.setEnabled(false);
        }

        // configure up/down buttons
        parent.findViewById(R.id.move_up).setOnClickListener(v -> moveModule(parent, -1));
        parent.findViewById(R.id.move_down).setOnClickListener(v -> moveModule(parent, 1));
        // the enable/disable status will be set later with {@link this#updateMovableButtons()}

        // configure label
        final TextView title = parent.findViewById(R.id.label);
        title.setText(getString(R.string.dd, getString(module.getName())));

        // configuration of the module
        final View child = Inflater.inflate(config.getLayoutId(), parent.findViewById(R.id.box), this);
        config.onInitialize(child);

        // configure toggleable description
        title.setOnClickListener(v -> {
            boolean checked = child.getVisibility() == View.GONE;
            child.setVisibility(checked ? View.VISIBLE : View.GONE);
            title.setCompoundDrawablesWithIntrinsicBounds(checked ? R.drawable.expanded : R.drawable.collapsed, 0, 0, 0);
        });
        title.performClick(); // initial hide
    }

    /**
     * Moves a module a specific number of positions in the list
     *
     * @param moduleView
     * @param delta
     */
    private void moveModule(View moduleView, int delta) {
        int position = list.indexOfChild(moduleView);
        if (position == -1) return; // no view? impossible
        int newPosition = Math.min(Math.max(1, position + delta), list.getChildCount() - 1); // clamp
        if (newPosition == position) return; // same position? just ignore

        // swap
        list.removeView(moduleView);
        list.addView(moduleView, newPosition);
        updateMovableButtons();

        // update preferences order
        List<String> modules = new ArrayList<>();
        for (int i = list.getChildCount() - 2; i >= 1; i--) {
            // reversed, because -1 means bottom
            modules.add(list.getChildAt(i).getTag().toString());
        }
        order.set(modules);
    }

    /**
     * Disables a module specified by its config
     */
    public void disableModule(AModuleConfig module) {
        final Switch vswitch = switches.get(module);
        if (vswitch != null) vswitch.setChecked(false);
    }

    /**
     * Updates the enable status of all the movable buttons
     */
    private void updateMovableButtons() {
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            // enable up unless already at the top
            View up = child.findViewById(R.id.move_up);
            up.setEnabled(i != list.getChildCount() - 1 && i >= 2);
            up.setAlpha(i != list.getChildCount() - 1 && i >= 2 ? 1 : 0.5f);
            // enable down unless already at the bottom
            View down = child.findViewById(R.id.move_down);
            down.setEnabled(i != 0 && i <= list.getChildCount() - 2);
            down.setAlpha(i != 0 && i <= list.getChildCount() - 3 ? 1 : 0.5f);
        }
    }

    /**
     * Resets the order of the modules
     */
    public void resetOrder(View button) {
        // updates preference
        order.clear();

        // updates views
        List<View> views = new ArrayList<>();
        while (list.getChildCount() > 2) {
            // get all of them
            views.add(list.getChildAt(1));
            list.removeViewAt(1);
        }

        // sort
        List<String> modules = ModuleManager.getOrderedModulesId(this);
        Collections.sort(views, (a, b) -> modules.indexOf(b.getTag().toString()) - modules.indexOf(a.getTag().toString()));

        // set
        for (View view : views) {
            list.addView(view, 1);
        }
        updateMovableButtons();

    }
}