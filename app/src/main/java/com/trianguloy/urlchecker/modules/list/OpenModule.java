package com.trianguloy.urlchecker.modules.list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.flavors.IncognitoDimension;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.companions.CTabs;
import com.trianguloy.urlchecker.modules.companions.Flags;
import com.trianguloy.urlchecker.modules.companions.Incognito;
import com.trianguloy.urlchecker.modules.companions.LastOpened;
import com.trianguloy.urlchecker.modules.companions.OnOffConfig;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;
import com.trianguloy.urlchecker.utilities.methods.PackageUtils;
import com.trianguloy.urlchecker.utilities.methods.UrlUtils;
import com.trianguloy.urlchecker.utilities.wrappers.RejectionDetector;

import java.util.List;

/**
 * This module contains an open and share buttons
 */
public class OpenModule extends AModuleData {

    public static GenericPref.Bool CLOSEOPEN_PREF(Context cntx) {
        return new GenericPref.Bool("open_closeopen", true, cntx);
    }

    public static GenericPref.Bool CLOSESHARE_PREF(Context cntx) {
        return new GenericPref.Bool("open_closeshare", true, cntx);
    }

    public static GenericPref.Bool CLOSECOPY_PREF(Context cntx) {
        return new GenericPref.Bool("open_closecopy", false, cntx);
    }

    public static GenericPref.Bool NOREFERRER_PREF(Context cntx) {
        return new GenericPref.Bool("open_noReferrer", false, cntx);
    }

    public static GenericPref.Bool MERGECOPY_PREF(Context cntx) {
        return new GenericPref.Bool("open_mergeCopy", false, cntx);
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
    public AModuleDialog getDialog(MainDialog cntx) {
        return new OpenDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new OpenConfig(cntx);
    }
}

class OpenDialog extends AModuleDialog {

    private final GenericPref.Bool closeOpenPref;
    private final GenericPref.Bool closeSharePref;
    private final GenericPref.Bool closeCopyPref;
    private final GenericPref.Bool noReferrerPref;
    private final GenericPref.Bool mergeCopyPref;

    private final LastOpened lastOpened;
    private final CTabs cTabs;
    private final Incognito incognito;
    private final RejectionDetector rejectionDetector;

    private List<String> packages;
    private Button btn_open;
    private ImageButton btn_openWith;
    private View openParent;
    private Menu menu;
    private PopupMenu popup;

    public OpenDialog(MainDialog dialog) {
        super(dialog);
        lastOpened = new LastOpened(dialog);
        cTabs = new CTabs(dialog);
        incognito = new Incognito(dialog);
        rejectionDetector = new RejectionDetector(dialog);
        closeOpenPref = OpenModule.CLOSEOPEN_PREF(dialog);
        closeSharePref = OpenModule.CLOSESHARE_PREF(dialog);
        closeCopyPref = OpenModule.CLOSECOPY_PREF(dialog);
        noReferrerPref = OpenModule.NOREFERRER_PREF(dialog);
        mergeCopyPref = OpenModule.MERGECOPY_PREF(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_open;
    }

    @Override
    public void onInitialize(View views) {
        Intent intent = getActivity().getIntent();

        // ctabs
        cTabs.initFrom(intent, views.findViewById(R.id.ctabs));

        // incognito
        incognito.initFrom(intent, views.findViewById(R.id.mode_incognito));

        // init open
        openParent = views.findViewById(R.id.open_parent);
        btn_open = views.findViewById(R.id.open);
        btn_open.setOnClickListener(v -> openUrl(0));

        // init openWith
        btn_openWith = views.findViewById(R.id.open_with);
        btn_openWith.setOnClickListener(v -> showList());

        // init copy & share
        var btn_copy = views.findViewById(R.id.copyUrl);
        var btn_share = views.findViewById(R.id.share);
        btn_share.setOnClickListener(v -> shareUrl());
        if (mergeCopyPref.get()) {
            // merge mode (single button)
            btn_copy.setVisibility(View.GONE);
            btn_share.setOnLongClickListener(v -> {
                copyUrl();
                return true;
            });
        } else {
            // split mode (two buttons)
            btn_copy.setOnClickListener(v -> copyUrl());
            AndroidUtils.longTapForDescription(btn_share);
            AndroidUtils.longTapForDescription(btn_copy);
        }

        // init openWith popup
        popup = new PopupMenu(getActivity(), btn_open);
        popup.setOnMenuItemClickListener(item -> {
            openUrl(item.getItemId());
            return false;
        });
        menu = popup.getMenu();
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        updateSpinner(urlData.url);
    }

    // ------------------- Spinner -------------------

    /**
     * Populates the spinner with the apps that can open it, in preference order
     */
    private void updateSpinner(String url) {
        packages = PackageUtils.getOtherPackages(UrlUtils.getViewIntent(url, null), getActivity());

        // remove referrer
        if (noReferrerPref.get()) {
            packages.remove(AndroidUtils.getReferrer(getActivity()));
        }

        // remove rejected
        // note: this will be called each time, so a rejected package will not be rejected again if the user changes the url and goes back. This is expected
        packages.remove(rejectionDetector.getPrevious(url));

        // check no apps
        if (packages.isEmpty()) {
            btn_open.setText(R.string.mOpen_noapps);
            AndroidUtils.setEnabled(openParent, false);
            btn_open.setEnabled(false);
            btn_openWith.setVisibility(View.GONE);
            return;
        }

        // sort
        lastOpened.sort(packages, getUrl());

        // set
        btn_open.setText(getActivity().getString(R.string.mOpen_with, PackageUtils.getPackageName(packages.get(0), getActivity())));
        AndroidUtils.setEnabled(openParent, true);
        btn_open.setEnabled(true);
        menu.clear();
        if (packages.size() == 1) {
            btn_openWith.setVisibility(View.GONE);
        } else {
            btn_openWith.setVisibility(View.VISIBLE);
            for (int i = 1; i < packages.size(); i++) {
                menu.add(Menu.NONE, i, i, getActivity().getString(R.string.mOpen_with, PackageUtils.getPackageName(packages.get(i), getActivity())));
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
        // get
        if (index < 0 || index >= packages.size()) return;
        var chosen = packages.get(index);

        // update as preferred over the rest
        lastOpened.prefer(chosen, packages, getUrl());

        // open
        var intent = new Intent(getActivity().getIntent());
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // preserve original VIEW intent
            intent.setData(Uri.parse(getUrl()));
            intent.setComponent(null);
            intent.setPackage(chosen);
        } else {
            // replace with new VIEW intent
            intent = UrlUtils.getViewIntent(getUrl(), chosen);
        }

        // ctabs
        cTabs.apply(intent);

        // Get flags from global data (probably set by flags module, if active)
        var flags = Flags.getGlobalFlagsNullable(this);
        if (flags != null) {
            intent.setFlags(flags);
        }

        // incognito
        incognito.apply(getActivity(), intent, getUrl());

        // rejection detector: mark as open
        rejectionDetector.markAsOpen(getUrl(), chosen);

        // open
        PackageUtils.startActivity(intent, R.string.toast_noApp, getActivity());

        // FIXME: We borrow the clipboard before launching the activity, if the activity does not
        //  start, it doesn't make sense that we borrowed the clipboard

        // finish activity
        if (closeOpenPref.get()) {
            this.getActivity().finish();
        }
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
        var sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getUrl());
        sendIntent.setType("text/plain");

        // share intent
        var chooser = Intent.createChooser(sendIntent, getActivity().getString(R.string.mOpen_share));
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // to still show after finishAndRemoveTask
        PackageUtils.startActivity(
                chooser,
                R.string.mOpen_noapps,
                getActivity()
        );
        if (closeSharePref.get()) {
            getActivity().finish();
        }
    }

    /**
     * Copy the url
     */
    private void copyUrl() {
        AndroidUtils.copyToClipboard(getActivity(), R.string.mOpen_clipboard, getUrl());
        if (closeCopyPref.get()) {
            getActivity().finish();
        }
    }

}

class OpenConfig extends AModuleConfig {

    public OpenConfig(ModulesActivity activity) {
        super(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_open;
    }

    @Override
    public void onInitialize(View views) {
        if (CTabs.isAvailable()) {
            CTabs.PREF(getActivity()).attachToSpinner(views.findViewById(R.id.ctabs_pref), null);
        } else {
            views.findViewById(R.id.ctabs_parent).setVisibility(View.GONE);
        }
        configureIncognito(views);
        OpenModule.CLOSEOPEN_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.closeopen_pref));
        OpenModule.CLOSESHARE_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.closeshare_pref));
        OpenModule.CLOSECOPY_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.closecopy_pref));
        OpenModule.NOREFERRER_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.noReferrer));
        LastOpened.PERDOMAIN_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.perDomain));
        OpenModule.MERGECOPY_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.mergeCopy_pref));
    }

    // ------------------- incognito dimension -------------------
    private void configureIncognito(View views) {
        var incognitoButton = (Button) views.findViewById(R.id.urlHelper_settings);
        JavaUtils.Consumer<OnOffConfig> buttonEnabled = null;
        if (BuildConfig.IS_INCOGNITO) {
            incognitoButton.setOnClickListener(v -> {
                IncognitoDimension.showSettings(getActivity());
            });
            buttonEnabled = onOffConfig -> {
                incognitoButton.setEnabled(OnOffConfig.ALWAYS_OFF != onOffConfig);
            };
            buttonEnabled.accept(Incognito.PREF(getActivity()).get());
        } else {
            incognitoButton.setVisibility(View.GONE);
        }
        Incognito.PREF(getActivity()).attachToSpinner(views.findViewById(R.id.incognito_pref), buttonEnabled);
    }
}

