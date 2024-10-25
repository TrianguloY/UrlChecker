package com.trianguloy.urlchecker.modules.list;

import static java.util.Objects.requireNonNullElse;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.flavors.IncognitoDimension;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.services.CustomTabs;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.UrlUtils;
import com.trianguloy.urlchecker.utilities.wrappers.IntentApp;

import java.util.List;

/**
 * This modules marks the insertion point of new modules
 * If enabled, shows a textview with debug info.
 * Allows also to enable/disable ctabs toasts
 */
public class DebugModule extends AModuleData {
    public static String ID = "debug";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getName() {
        return R.string.mD_name;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new DebugDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new DebugConfig(cntx);
    }
}

class DebugDialog extends AModuleDialog {

    public static final String SEPARATOR = "";
    private TextView data;

    public DebugDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_debug;
    }

    @Override
    public void onInitialize(View views) {
        data = views.findViewById(R.id.data);
        views.findViewById(R.id.showData).setOnClickListener(v -> showData(v.getContext()));
    }

    private void showData(Context context) {
        data.setVisibility(View.VISIBLE);

        // data to display
        data.setText(String.join("\n", List.of(
                "Intent:",
                getActivity().getIntent().toUri(0),

                SEPARATOR,

                "queryIntentActivities:",
                IntentApp.getOtherPackages(UrlUtils.getViewIntent(getUrl(), null), getActivity()).toString(),

                SEPARATOR,

                "queryIntentActivityOptions:",
                getActivity().getPackageManager().queryIntentActivityOptions(
                        new ComponentName(getActivity(), MainDialog.class.getName()),
                        null,
                        UrlUtils.getViewIntent(getUrl(), null),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : 0
                ).toString(),

                SEPARATOR,

                "UrlData:",
                getUrlData().toString(),

                SEPARATOR,

                "GlobalData:",
                getGlobalData().toString(),

                SEPARATOR,

                "Referrer:",
                requireNonNullElse(AndroidUtils.getReferrer(getActivity()), "null")
        )));

        if (BuildConfig.IS_INCOGNITO) {
            data.setText(String.join("\n", List.of(
                    data.getText(),

                    SEPARATOR,

                    "Incognito: ",
                    "AccessibilityServiceConnected: " + IncognitoDimension.isServiceConnected(),
                    "AccessibilityServiceEnabled: " + IncognitoDimension.isServiceEnabled(context)
            )));
        }
    }

    @Override
    public void onPrepareUrl(UrlData urlData) {
        data.setVisibility(View.GONE);
    }
}

class DebugConfig extends AModuleConfig {


    public DebugConfig(ModulesActivity activity) {
        super(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_debug;
    }

    @Override
    public void onInitialize(View views) {
        CustomTabs.SHOWTOAST_PREF(getActivity())
                .attachToSwitch(views.findViewById(R.id.chk_ctabs));
    }
}
