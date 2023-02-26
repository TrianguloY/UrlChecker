package com.trianguloy.urlchecker.modules.list;

import android.view.View;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.services.CustomTabs;
import com.trianguloy.urlchecker.url.UrlData;

import java.util.List;

/**
 * This modules marks the insertion point of new modules
 * If enabled, shows a textview with debug info.
 * Currently shows the original intent (as uri)
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

    private TextView textView;
    private String intentUri;

    public DebugDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_debug;
    }

    @Override
    public void onInitialize(View views) {
        textView = views.findViewById(R.id.data);

        // cached
        intentUri = getActivity().getIntent().toUri(0);
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        textView.setText(String.join("\n\n", List.of(
                // show activity uri
                intentUri,
                // show current url data
                urlData.toString(),
                // show global data
                getGlobalData().toString()
        )));
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
