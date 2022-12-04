package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.Animations;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.Inflater;
import com.trianguloy.urlchecker.utilities.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity that shows the list of modules that can be enabled/disabled
 */
public class ModulesActivity extends Activity {

    private LinearLayout list;
    private final Map<AModuleConfig, Switch> switches = new HashMap<>();
    private GenericPref.LstStr order;
    private GenericPref.Bool showDecorations;

    // ------------------- listeners -------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        setContentView(R.layout.activity_modules);
        setTitle(R.string.a_modules);
        AndroidUtils.configureUp(this);

        list = findViewById(R.id.list);
        order = ModuleManager.ORDER_PREF(this);
        showDecorations = ModuleManager.DECORATIONS_PREF(this);

        initConfig();

        // initialize modules
        for (AModuleData module : ModuleManager.getModules(true, this)) {
            initModule(module);
        }

        // init buttons
        updateMovableButtons();
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

    // ------------------- actions -------------------

    private void initConfig() {
        showDecorations.attachToCheckBox(findViewById(R.id.showDecorations));
    }

    /**
     * Initializes and adds a module to the list
     */
    private void initModule(AModuleData module) {
        final AModuleConfig config = module.getConfig(this);

        // inflate
        View parent = Inflater.inflate(R.layout.config_module, list, this);
        parent.setTag(module.getId());
        Animations.enableAnimations(parent);

        // configure enable toggle
        Switch toggleEnable = parent.findViewById(R.id.enable);
        final GenericPref.Bool enabled_pref = ModuleManager.getEnabledPrefOfModule(module, this);
        toggleEnable.setChecked(enabled_pref.get());
        toggleEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !config.canBeEnabled()) {
                Toast.makeText(ModulesActivity.this, R.string.toast_cantEnable, Toast.LENGTH_LONG).show();
                buttonView.setChecked(false);
            } else {
                enabled_pref.set(isChecked);
            }
        });
        switches.put(config, toggleEnable);

        // configure up/down buttons
        parent.findViewById(R.id.move_up).setOnClickListener(v -> moveModule(parent, -1));
        parent.findViewById(R.id.move_down).setOnClickListener(v -> moveModule(parent, 1));
        // the enable/disable status will be set later with {@link this#updateMovableButtons()}

        // configure label
        final TextView title = parent.findViewById(R.id.label);
        title.setText(getString(R.string.dd, getString(module.getName())));
        AndroidUtils.setAsClickable(title);

        // configuration of the module
        var child = Inflater.inflate(config.getLayoutId(), parent.findViewById(R.id.box), this);
        config.onInitialize(child);

        // configure toggleable description
        final var description = parent.findViewById(R.id.details);
        title.setOnClickListener(v -> {
            boolean checked = description.getVisibility() == View.GONE;
            description.setVisibility(checked ? View.VISIBLE : View.GONE);
            AndroidUtils.setStartDrawables(title,
                    checked ? R.drawable.arrow_down : R.drawable.arrow_right);
        });
        title.performClick(); // initial hide
    }

    /**
     * Moves a module a specific number of positions in the list
     */
    private void moveModule(View moduleView, int delta) {
        int position = list.indexOfChild(moduleView);
        if (position == -1) return; // no view? impossible
        int newPosition = JavaUtils.clamp(0, position + delta, list.getChildCount() - 1);
        if (newPosition == position) return; // same position? just ignore

        // swap
        list.removeView(moduleView);
        list.addView(moduleView, newPosition);
        updateMovableButtons();

        // update preferences order
        List<String> modules = new ArrayList<>();
        for (int i = list.getChildCount() - 1; i >= 0; i--) {
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
            up.setEnabled(i > 0);
            up.setAlpha(i > 0 ? 1 : 0.5f);
            // enable down unless already at the bottom
            View down = child.findViewById(R.id.move_down);
            down.setEnabled(i < list.getChildCount() - 1);
            down.setAlpha(i < list.getChildCount() - 1 ? 1 : 0.5f);
        }
    }

    /**
     * Resets the order of the modules
     */
    public void resetOrder(View button) {
        // updates preference
        order.clear();

        // get and remove all views
        List<View> views = new ArrayList<>();
        for (int i = 0; i < list.getChildCount(); i++) {
            views.add(list.getChildAt(i));
        }
        list.removeAllViews();

        // sort views based on the order they should have
        List<String> modules = ModuleManager.getOrderedModulesId(this);
        Collections.sort(views, (a, b) -> modules.indexOf(a.getTag().toString()) - modules.indexOf(b.getTag().toString()));

        // add
        for (View view : views) {
            list.addView(view);
        }
        updateMovableButtons();

    }
}