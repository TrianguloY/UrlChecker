package com.trianguloy.urlchecker.modules.list;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.LastOpened;
import com.trianguloy.urlchecker.utilities.PackageUtilities;
import com.trianguloy.urlchecker.utilities.UrlUtilities;

import java.util.LinkedList;
import java.util.List;

/**
 * This module contains an open and share buttons
 */
public class OpenModule extends AModuleData {

    /**
     * Values for the config
     */
    enum CtabsValues {
        AUTO(Key.AUTO),
        ON(Key.ON),
        OFF(Key.OFF);

        public final int key;

        CtabsValues(int key) {
            this.key = key;
        }

        // Values to be stored
        private static class Key {
            public static final int AUTO = 2;
            public static final int ON = 1;
            public static final int OFF = 0;
        }
    }

    enum PresetFlagsValues {
        AUTO(Key.AUTO),
        EXCLUDE_RECENTS(Key.EXCLUDE_RECENTS),
        INSIDE(Key.INSIDE);

        public final int key;
        static private PresetFlagsValues[] values = null;

        PresetFlagsValues(int key) {
            this.key = key;
        }

        // Combinations of flags are always unique, this way is easier to read and keep track of
        private static class Key {
            public static final int AUTO = 0xFFFFFFFF;  // Will never match
            public static final int EXCLUDE_RECENTS = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
            public static final int INSIDE = Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
        }

        // To get enum out of key
        public static PresetFlagsValues getFlag(int key){
            if (values == null){    // init if necessary
                values = PresetFlagsValues.values();
            }
            for (PresetFlagsValues e: values) {
                if (e.key == key){
                    return e;
                }
            }
            return null;
        }
    }

    public static GenericPref.Int CTABS_PREF() {
        return new GenericPref.Int("open_ctabs", CtabsValues.AUTO.key);
    }

    public static GenericPref.Int PRESETFLAGS_PREF() {
        return new GenericPref.Int("open_presetflags", PresetFlagsValues.AUTO.key);
    }

    @Override
    public String getId() {
        return "open";
    }

    @Override
    public int getName() {
        return R.string.mOpen_name;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new OpenDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new OpenConfig(cntx);
    }
}

class OpenDialog extends AModuleDialog implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, View.OnLongClickListener {

    private static final String CTABS_EXTRA = "android.support.customtabs.extra.SESSION";

    private LastOpened lastOpened;

    private final GenericPref.Int ctabsPref = OpenModule.CTABS_PREF();
    private boolean ctabs = false;

    private final GenericPref.Int presetFlagsPref = OpenModule.PRESETFLAGS_PREF();
    private OpenModule.PresetFlagsValues presetState;
    private OpenModule.PresetFlagsValues presetMatch;
    private final OpenModule.PresetFlagsValues[] presetValues = OpenModule.PresetFlagsValues.values();

    private List<String> packages;
    private Button btn_open;
    private ImageButton btn_openWith;
    private Menu menu;
    private PopupMenu popup;
    private ImageButton btn_ctabs;
    private ImageButton btn_presetflags;
    private ImageButton expand;
    private LinearLayout box;


    public OpenDialog(MainDialog dialog) {
        super(dialog);
        ctabsPref.init(dialog);
        presetFlagsPref.init(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_open;
    }

    @Override
    public void onInitialize(View views) {
        Intent intent = getActivity().getIntent();

        btn_ctabs = views.findViewById(R.id.ctabs);
        btn_ctabs.setOnClickListener(this);
        btn_ctabs.setOnLongClickListener(this);
        // If auto we get it from the intent, if not we only check if it is on, if it is not it means is off
        setCtabs(ctabsPref.get() == OpenModule.CtabsValues.AUTO.key ?
                intent.hasExtra(CTABS_EXTRA) :
                ctabsPref.get() == OpenModule.CtabsValues.ON.key);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            btn_ctabs.setVisibility(View.GONE);
        }

        btn_presetflags = views.findViewById(R.id.presetflags);
        btn_presetflags.setOnClickListener(this);
        btn_presetflags.setOnLongClickListener(this);
        // Checks if intent flags match any of the presets
        final int flags = intent.getFlags();
        presetMatch = OpenModule.PresetFlagsValues.getFlag(flags);
        OpenModule.PresetFlagsValues preference = OpenModule.PresetFlagsValues.getFlag(presetFlagsPref.get());

        setPresetState(presetFlagsPref.get() != OpenModule.PresetFlagsValues.AUTO.key ? // if preference is not auto
                (preference != null ? preference : OpenModule.PresetFlagsValues.AUTO) : // set state as preference if not null
                (presetMatch != null ? presetMatch : OpenModule.PresetFlagsValues.AUTO));

        btn_open = views.findViewById(R.id.open);
        btn_open.setOnClickListener(this);
        btn_open.setOnLongClickListener(this);

        box = views.findViewById(R.id.advanced);
        expand = views.findViewById(R.id.expand);
        expand.setOnClickListener(this);
        box.setVisibility(View.VISIBLE);
        toggleAdvancedOpts();

        btn_openWith = views.findViewById(R.id.open_with);
        btn_openWith.setOnClickListener(this);

        View btn_share = views.findViewById(R.id.share);
        btn_share.setOnClickListener(this);
        btn_share.setOnLongClickListener(this);


        popup = new PopupMenu(getActivity(), btn_open);
        popup.setOnMenuItemClickListener(this);
        menu = popup.getMenu();

        lastOpened = new LastOpened(getActivity());
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        updateSpinner();
    }

    // ------------------- Button listener -------------------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ctabs:
                toggleCtabs();
                break;
            case R.id.presetflags:
                rotatePresetFlags();
                break;
            case R.id.expand:
                toggleAdvancedOpts();
                break;
            case R.id.open:
                openUrl(0);
                break;
            case R.id.share:
                shareUrl();
                break;
            case R.id.open_with:
                showList();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.ctabs:
                Toast.makeText(getActivity(), R.string.mOpen_tabsDesc, Toast.LENGTH_SHORT).show();
                break;
            case R.id.presetflags:
                Toast.makeText(getActivity(), R.string.mOpen_rotatePresetFlags, Toast.LENGTH_SHORT).show();
                break;
            case R.id.share:
                copyToClipboard();
                break;
            default:
                return false;
        }
        return true;
    }

    // ------------------- PopupMenu.OnMenuItemClickListener -------------------

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        openUrl(item.getItemId());
        return false;
    }

    // ------------------- Spinner -------------------

    /**
     * Populates the spinner with the apps that android says that can open it
     */
    private void updateSpinner() {
        packages = PackageUtilities.getOtherPackages(UrlUtilities.getViewIntent(getUrl(), null), getActivity());

        // check no apps
        if (packages.isEmpty()) {
            btn_open.setText(R.string.mOpen_noapps);
            btn_open.setEnabled(false);
            btn_openWith.setVisibility(View.GONE);
            return;
        }

        // sort
        lastOpened.sort(packages, getUrl());

        // set
        btn_open.setText(getActivity().getString(R.string.mOpen_with, PackageUtilities.getPackageName(packages.get(0), getActivity())));
        btn_open.setEnabled(true);
        menu.clear();
        if (packages.size() == 1) {
            btn_openWith.setVisibility(View.GONE);
        } else {
            btn_openWith.setVisibility(View.VISIBLE);
            for (int i = 1; i < packages.size(); i++) {
                menu.add(Menu.NONE, i, i, getActivity().getString(R.string.mOpen_with, PackageUtilities.getPackageName(packages.get(i), getActivity())));
            }
        }

    }

    // ------------------- Buttons -------------------

    /**
     * Open url in a specific app
     *
     * @param index index from the packages list of the app to use
     */
    private void openUrl(int index) {
        if (index < 0 || index >= packages.size()) return;

        // update chosen
        String chosed = packages.get(index);
        lastOpened.usedPackage(chosed, getUrl());

        // open
        Intent intent = new Intent(getActivity().getIntent());
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // preserve original VIEW intent
            intent.setData(Uri.parse(getUrl()));
            intent.setComponent(null);
            intent.setPackage(chosed);


        } else {
            // replace with new VIEW intent
            intent = UrlUtilities.getViewIntent(getUrl(), chosed);
        }

        if (ctabs && !intent.hasExtra(CTABS_EXTRA)) {
            // enable Custom tabs

            // https://developer.chrome.com/multidevice/android/customtabs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Bundle extras = new Bundle();
                extras.putBinder(CTABS_EXTRA, null); //  Set to null for no session
                intent.putExtras(extras);
            }
        }

        if (!ctabs && intent.hasExtra(CTABS_EXTRA)) {
            // disable ctabs
            intent.removeExtra(CTABS_EXTRA);
        }

        if (presetState != OpenModule.PresetFlagsValues.AUTO) {
            intent.setFlags(presetState.key);
        }

        PackageUtilities.startActivity(intent, R.string.toast_noApp, getActivity());
    }

    /**
     * Show the popup with the rest of the apps
     */
    private void showList() {
        popup.show();
    }

    /**
     * Shares the url as text
     */
    private void shareUrl() {
        // create send intent
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getUrl());
        sendIntent.setType("text/plain");

        // share intent
        PackageUtilities.startActivity(
                Intent.createChooser(sendIntent, getActivity().getString(R.string.mOpen_share)),
                R.string.mOpen_noapps,
                getActivity()
        );
    }

    /**
     * Copy the url to the clipboard
     */
    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", getUrl());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), R.string.mOpen_clipboard, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Toggle the custom tabs state
     */
    private void toggleCtabs() {
        setCtabs(!ctabs);
    }

    /**
     * Sets the custom tabs state
     */
    private void setCtabs(boolean state) {
        btn_ctabs.setImageResource(state ? R.drawable.ctabs_on : R.drawable.ctabs_off);
        ctabs = state;
    }

    /**
     * Rotates the preset flags state
     */
    private void rotatePresetFlags() {
        int index = presetState.ordinal() + 1;  // next state
        index = presetValues.length <= index ? 0 : index; // OOB check
        // If a match was found there is no need to iterate through the auto state
        index = presetMatch == null ? index :
                presetValues[index] == OpenModule.PresetFlagsValues.AUTO ? index + 1 : index;
        index = presetValues.length <= index ? 0 : index; // OOB check
        setPresetState(presetValues[index]);
    }

    /**
     * Sets the preset flags state
     * @param state
     */
    private void setPresetState(OpenModule.PresetFlagsValues state) {
        int image;
        switch (state){
            case INSIDE:
                image = R.drawable.mopen_inside;
                break;
            case EXCLUDE_RECENTS:
                image = R.drawable.mopen_exclude_recents;
                break;
            case AUTO:
            default:
                image = R.drawable.mopen_inside;
        }
        btn_presetflags.setImageResource(image);
        presetState = state;
    }

    /**
     * Shows/hides the advanced options
     */
    private void toggleAdvancedOpts(){
        box.setVisibility(box.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        expand.setImageResource(box.getVisibility() == View.VISIBLE ?
                R.drawable.arrow_down :
                R.drawable.arrow_right);
    }
}


class OpenConfig extends AModuleConfig {

    private final GenericPref.Int ctabsPref = OpenModule.CTABS_PREF();
    private final GenericPref.Int presetFlagsPref = OpenModule.PRESETFLAGS_PREF();

    public OpenConfig(ConfigActivity activity) {
        super(activity);
        ctabsPref.init(activity);
        presetFlagsPref.init(activity);
    }

    @Override
    public boolean canBeEnabled() {
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_open;
    }

    @Override
    public void onInitialize(View views) {
        Context cntx = views.getContext();

        List<GenericPref.Int.AdapterPair> ctabsElements = new LinkedList<>();
        ctabsElements.add(new GenericPref.Int.AdapterPair(OpenModule.CtabsValues.AUTO.key,
                cntx.getString(R.string.auto)));
        ctabsElements.add(new GenericPref.Int.AdapterPair(OpenModule.CtabsValues.ON.key,
                cntx.getString(R.string.enabled)));
        ctabsElements.add(new GenericPref.Int.AdapterPair(OpenModule.CtabsValues.OFF.key,
                cntx.getString(R.string.disabled)));
        ctabsPref.attachToSpinner(views.findViewById(R.id.ctabs_pref),
                ctabsElements);

        List<GenericPref.Int.AdapterPair> presetFlagsElements = new LinkedList<>();
        presetFlagsElements.add(new GenericPref.Int.AdapterPair(OpenModule.PresetFlagsValues.AUTO.key,
                cntx.getString(R.string.auto)));
        presetFlagsElements.add(new GenericPref.Int.AdapterPair(OpenModule.PresetFlagsValues.INSIDE.key,
                cntx.getString(R.string.mOpen_optionInside)));
        presetFlagsElements.add(new GenericPref.Int.AdapterPair(OpenModule.PresetFlagsValues.EXCLUDE_RECENTS.key,
                cntx.getString(R.string.mOpen_optionExcludeRecents)));
        presetFlagsPref.attachToSpinner(views.findViewById(R.id.presetflags_pref),
                presetFlagsElements);
    }
}