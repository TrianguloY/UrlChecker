package com.trianguloy.urlchecker.dialogs;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.ModuleManager;
import com.trianguloy.urlchecker.modules.companions.VersionManager;
import com.trianguloy.urlchecker.modules.list.DrawerModule;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.Inflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main dialog, when opening a url
 */
public class MainDialog extends Activity {

    /**
     * Maximum number of updates to avoid loops
     */
    private static final int MAX_UPDATES = 100;

    // ------------------- data -------------------

    /**
     * All active modules
     */
    private final Map<AModuleDialog, List<View>> modules = new HashMap<>();

    /**
     * Global data to keep even if the url changes
     */
    public final Map<String, String> globalData = new HashMap<>();

    /**
     * The current url
     */
    private UrlData urlData = new UrlData("");

    /**
     * Currently in the process of updating.
     */
    private int updating = 0;

    // ------------------- module functions -------------------

    /**
     * Something wants to set a new url.
     */
    public void onNewUrl(UrlData newUrlData) {
        // mark as next if nothing else yet
        if (updating != 0) {
            AndroidUtils.assertError("Don't call onNewUrl while updating, use the onModifyUrl 'setNewUrl' callback");
            return;
        }
        urlData = newUrlData;

        // fire updates loop
        main_loop:
        while (true) {
            updating++;

            // first notify modules
            for (var module : modules.keySet()) {
                // skip own if required
                if (!urlData.triggerOwn && module == urlData.trigger) continue;
                try {
                    module.onPrepareUrl(urlData);
                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidUtils.assertError("Exception in onPrepareUrl for module " + module.getClass().getName());
                }
            }

            // second ask for modifications
            for (var module : modules.keySet()) {
                // skip own if required
                if (!urlData.triggerOwn && module == urlData.trigger) continue;
                try {
                    var modifiedUrlData = new UrlData[]{null};
                    module.onModifyUrl(urlData, newUrl -> {
                        // callback to replace the url. Alternative to throwing an exception and catch it here.
                        // can't use a return value directly because the caller needs to know if it should continue or not.
                        if (!urlData.disableUpdates && updating < MAX_UPDATES) {
                            // new url accepted
                            modifiedUrlData[0] = newUrl;
                            return true;
                        } else {
                            // a new url is not accepted
                            return false;
                        }
                    });
                    if (modifiedUrlData[0] != null) {
                        // modified, restart
                        modifiedUrlData[0].mergeData(urlData);
                        urlData = modifiedUrlData[0];
                        continue main_loop;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidUtils.assertError("Exception in onModifyUrl for module " + module.getClass().getName());
                }
            }

            // third notify for final changes
            for (var module : modules.keySet()) {
                // skip own if required
                if (!urlData.triggerOwn && module == urlData.trigger) continue;
                try {
                    module.onDisplayUrl(urlData);
                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidUtils.assertError("Exception in onDisplayUrl for module " + module.getClass().getName());
                }
            }

            // fourth finish notification
            for (var module : modules.keySet()) {
                // skip own if required
                if (!urlData.triggerOwn && module == urlData.trigger) continue;
                try {
                    module.onFinishUrl(urlData);
                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidUtils.assertError("Exception in onFinishUrl for module " + module.getClass().getName());
                }
            }

            break;
        }

        // end, reset
        updating = 0;
    }

    /**
     * Return the current url
     */
    public String getUrl() {
        return urlData.url;
    }

    /**
     * Changes a module visibility
     */
    public void setModuleVisibility(AModuleDialog module, boolean visible) {
        var views = modules.get(module);
        if (views == null) {
            AndroidUtils.assertError("Module " + module + " is not found in the list.");
            return;
        }
        for (var view : views) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    // ------------------- initialize -------------------

    private LinearLayout ll_main;
    private LinearLayout ll_drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, true);
        AndroidSettings.setLocale(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main);
        setFinishOnTouchOutside(true);

        // mark as seen if required
        VersionManager.check(this);

        // get views
        ll_main = findViewById(R.id.main);
        ll_drawer = findViewById(R.id.drawer);
        ll_drawer.setVisibility(View.GONE);

        // load url (or urls)
        var links = getOpenUrl();
        switch (links.size()) {
            case 0:
                // no links, invalid
                Toast.makeText(this, R.string.toast_invalid, Toast.LENGTH_SHORT).show();
                finish();
                break;
            case 1:
                // 1 link, just that

                // initialize
                initializeModules();

                // show
                onNewUrl(new UrlData(links.iterator().next()));
                break;
            default:
                // multiple links, choose
                var links_array = links.toArray(new String[0]);
                new AlertDialog.Builder(this)
                        .setItems(links_array, (dialog, which) -> {

                            // initialize
                            initializeModules();

                            // show
                            onNewUrl(new UrlData(links_array[which]));
                            dialog.dismiss();
                        })
                        .setOnCancelListener(o -> finish())
                        .show();
        }
    }

    /**
     * Initializes the modules
     */
    private void initializeModules() {
        modules.clear();
        ll_main.removeAllViews();
        ll_drawer.removeAllViews();
        var placeOnDrawer = false;

        // add
        var middleModules = ModuleManager.getModules(false, this);
        for (var module : middleModules) {
            initializeModule(module, placeOnDrawer);

            // If this module is the drawer module, all the remaining modules will be hidden
            if (module instanceof DrawerModule) placeOnDrawer = true;
        }

        // avoid empty
        if (ll_main.getChildCount() == 0) {
            ll_main.addView(egg()); // ;)
        }
    }

    /**
     * Initializes a module by registering with this dialog and adding to the list
     *
     * @param moduleData which module to initialize
     */
    private void initializeModule(AModuleData moduleData, boolean drawer) {
        try {
            // enabled, add
            AModuleDialog module = moduleData.getDialog(this);
            int layoutId = module.getLayoutId();

            View child = null;

            // set content if required
            var views = new ArrayList<View>();
            var ll = drawer ? ll_drawer : ll_main;
            if (layoutId >= 0) {

                // separator if necessary
                if (ll_main.getChildCount() != 0) views.add(addSeparator(ll));

                ViewGroup parent;
                // set module block
                if (ModuleManager.getDecorationsPrefOfModule(moduleData, this).get()) {
                    // init decorations
                    var block = Inflater.inflate(R.layout.dialog_module, ll);
                    var title = block.<TextView>findViewById(R.id.title);
                    title.setText(getString(R.string.dd, getString(moduleData.getName())));
                    parent = block.findViewById(R.id.mod);
                } else {
                    // no decorations
                    parent = ll;
                }

                // set module content
                child = Inflater.inflate(layoutId, parent);
                views.add(child);
            }

            // remove views when decorator pref is enabled, to avoid setting the visibility
            // consider changing to a different flag
            if (ModuleManager.getDecorationsPrefOfModule(moduleData, this).get()) {
                views.clear();
            }

            // init
            modules.put(module, views);
            module.onInitialize(child);
        } catch (Exception e) {
            // can't add module
            e.printStackTrace();
            AndroidUtils.assertError("Exception in initializeModule for module " + moduleData.getId());
        }
    }

    /**
     * Adds a separator component to the list of mods
     */
    private View addSeparator(LinearLayout ll) {
        return Inflater.inflate(R.layout.separator, ll);
    }

    /**
     * Returns the url that this activity was opened with (intent uri or sent text)
     */
    private Set<String> getOpenUrl() {
        // get the intent
        var intent = getIntent();
        if (intent == null) return Collections.emptySet();

        // check the action
        var action = getIntent().getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            // sent text
            var sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText == null) return Collections.emptySet();
            var links = AndroidUtils.getLinksFromText(sharedText);
            if (links.isEmpty()) links.add(sharedText.trim()); // no links? just use the whole text, the user requested the app so...
            return links;
        } else {
            // other, check data
            var uri = intent.getData();
            if (uri == null) return Collections.emptySet();
            return Collections.singleton(uri.toString());
        }
    }

    // ------------------- drawer module -------------------

    /**
     * returns the visibility of the drawer
     */
    public boolean isDrawerVisible() {
        return ll_drawer.getVisibility() != View.GONE;
    }

    /**
     * Toggles the drawer visibility
     */
    public void toggleDrawer() {
        ll_drawer.setVisibility(isDrawerVisible() ? View.GONE : View.VISIBLE);
    }

    /**
     * returns true if the drawer contains at least one visible children
     */
    public boolean anyDrawerChildVisible() {
        int childCount = ll_drawer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (ll_drawer.getChildAt(i).getVisibility() == View.VISIBLE) {
                return true;
            }
        }
        return false;
    }

    /* ------------------- its a secret! ------------------- */

    /**
     * To be set when there is no module displayed
     */
    private View egg() {
        var frame = new FrameLayout(this);

        var contentA = new ImageView(this);
        contentA.setImageResource(R.mipmap.ic_launcher);
        frame.addView(contentA);
        var a1 = ObjectAnimator.ofFloat(contentA, "rotation", 0, 360);
        a1.setDuration((long) (4000 + Math.random() * 2000));
        a1.setInterpolator(null);
        a1.setRepeatCount(ValueAnimator.INFINITE);
        a1.start();
        var a2 = ObjectAnimator.ofFloat(contentA, "alpha", 1, 0);
        a2.setDuration((long) (4000 + Math.random() * 2000));
        a2.setInterpolator(null);
        a2.setRepeatCount(ValueAnimator.INFINITE);
        a2.setRepeatMode(ValueAnimator.REVERSE);
        a2.start();

        var contentB = new ImageView(this);
        contentB.setImageResource(R.drawable.trianguloy);
        frame.addView(contentB);
        var b1 = ObjectAnimator.ofFloat(contentB, "rotation", 360, 0);
        b1.setDuration((long) (4000 + Math.random() * 2000));
        b1.setInterpolator(null);
        b1.setRepeatCount(ValueAnimator.INFINITE);
        b1.start();
        var b2 = ObjectAnimator.ofFloat(contentB, "alpha", 0, 1);
        b2.setDuration(a2.getDuration());
        b2.setInterpolator(null);
        b2.setRepeatCount(ValueAnimator.INFINITE);
        b2.setRepeatMode(ValueAnimator.REVERSE);
        b2.start();

        return frame;
    }


}