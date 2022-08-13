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

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.Inflater;

import java.util.ArrayList;
import java.util.List;

/**
 * The main dialog, when opening a url
 */
public class MainDialog extends Activity {

    // ------------------- module functions -------------------

    /**
     * Maximum number of updates to avoid loops
     */
    private static final int MAX_UPDATES = 100;

    /**
     * to allow changing url while notifying
     */
    private int updating = 0;

    /**
     * A module (null if first change) want to set a new url. Return true if set, false if not.
     */
    public boolean onNewUrl(UrlData urlData, AModuleDialog providerModule) {
        if (updating > MAX_UPDATES) return false;

        // change url (merge if not the first)
        if (updating != 0) urlData.mergeData(this.urlData);
        this.urlData = urlData;

        // test and mark recursion
        if (urlData.disableUpdates) updating = MAX_UPDATES;
        updating++;
        int updating_current = updating;

        // and notify the other modules
        for (AModuleDialog module : modules) {
            // skip own if required
            if (!urlData.triggerOwn && module == providerModule) continue;
            try {
                module.onNewUrl(urlData);
            } catch (Exception e) {
                e.printStackTrace();
                AndroidUtils.assertError("Exception in onNewUrl for module " + (providerModule == null ? "-none-" : providerModule.getClass().getName()));
            }
            if (updating_current != updating) return true;
        }
        updating = 0;
        return true;
    }

    /**
     * @return the current url
     */
    public String getUrl() {
        return urlData.url;
    }

    // ------------------- data -------------------

    /**
     * All active modules
     */
    private final List<AModuleDialog> modules = new ArrayList<>();

    // the current url
    private UrlData urlData = new UrlData("");

    // ------------------- initialize -------------------

    private LinearLayout ll_mods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidUtils.setTheme(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main);
        setFinishOnTouchOutside(true);

        // get views
        ll_mods = findViewById(R.id.middle_modules);

        // initialize
        initializeModules();

        // load url
        onNewUrl(new UrlData(getOpenUrl()), null);
    }

    /**
     * Initializes the modules
     */
    private void initializeModules() {
        modules.clear();

        // add
        final List<AModuleData> middleModules = ModuleManager.getModules(false, this);
        for (AModuleData module : middleModules) {
            initializeModule(module);
        }
    }

    /**
     * Initializes a module by registering with this dialog and adding to the list
     *
     * @param moduleData which module to initialize
     */
    private void initializeModule(AModuleData moduleData) {
        try {
            // enabled, add
            AModuleDialog module = moduleData.getDialog(this);
            int layoutId = module.getLayoutId();

            View child = null;

            // set content if required
            if (layoutId >= 0) {

                // separator if necessary
                if (ll_mods.getChildCount() != 0) addSeparator();

                ViewGroup parent;
                // set module block
                if (moduleData.canBeDisabled()) {
                    // init decorations
                    View block = Inflater.inflate(R.layout.dialog_module, ll_mods, this);
                    final TextView title = block.findViewById(R.id.title);
                    title.setText(getString(R.string.dd, getString(moduleData.getName())));
                    parent = block.findViewById(R.id.mod);
                } else {
                    // non-disable modules are considered internal and won't show decorations
                    parent = ll_mods;
                }

                // set module content
                child = Inflater.inflate(layoutId, parent, this);
            }

            // init
            module.onInitialize(child);
            modules.add(module);
        } catch (Exception e) {
            // can't add module
            e.printStackTrace();
            AndroidUtils.assertError("Exception in initializeModule for module " + moduleData.getId());
        }
    }

    /**
     * Adds a separator component to the list of mods
     */
    private void addSeparator() {
        Inflater.inflate(R.layout.separator, ll_mods, this);
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
     * shows a toast, finishes the activity and returns null
     */
    private String invalid() {
        // for an invalid parameter
        Toast.makeText(this, R.string.toast_invalid, Toast.LENGTH_SHORT).show();
        finish();
        return null;
    }

}