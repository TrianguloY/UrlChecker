package com.trianguloy.urlchecker.modules.list;

import static java.util.Objects.requireNonNullElse;

import android.os.Build;
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
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;

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

    public static final String SEPARATOR = "";
    private TextView textView;

    // cached
    private String intentUri;
    private String referrer;

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

        // expand when touched (not only clicked, to avoid a double-click-required bug and because it feels better)
        textView.setOnTouchListener((v, event) -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || textView.getMaxHeight() != Integer.MAX_VALUE) textView.setMaxHeight(Integer.MAX_VALUE);
            return false;
        });

        // cached values
        intentUri = getActivity().getIntent().toUri(0);
        referrer = requireNonNullElse(AndroidUtils.getReferrer(getActivity()), "null");
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        // collapse module to show exactly 5 lines and a half (to indicate there is more data)
        var fontMetrics = textView.getPaint().getFontMetrics();
        textView.setMaxHeight(Math.round((fontMetrics.bottom - fontMetrics.top) * 5.5f));

        // data to display
        textView.setText(String.join("\n", List.of(
                "Intent:",
                intentUri,

                SEPARATOR,

                "UrlData:",
                urlData.toString(),

                SEPARATOR,

                "GlobalData:",
                getGlobalData().toString(),

                SEPARATOR,

                "Referrer:",
                referrer
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
