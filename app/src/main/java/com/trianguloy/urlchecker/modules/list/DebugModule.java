package com.trianguloy.urlchecker.modules.list;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.services.CustomTabs;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.GenericPref;

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

class DebugDialog extends AModuleDialog implements View.OnClickListener, View.OnLongClickListener {

    private TextView txt_intent;
    private TextView txt_urlData;

    public DebugDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        txt_urlData.setText(urlData.toString());
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_debug;
    }

    @Override
    public void onInitialize(View views) {
        txt_intent = views.findViewById(R.id.intent);
        txt_intent.setText(getActivity().getIntent().toUri(0));
        txt_intent.setOnClickListener(this);
        txt_intent.setOnLongClickListener(this);

        txt_urlData = views.findViewById(R.id.urlData);
        txt_urlData.setOnClickListener(this);
        txt_urlData.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.intent:
                AndroidUtils.copyToClipboard(getActivity(), R.string.mD_copyIntent, txt_intent.getText().toString());
                break;
            case R.id.urlData:
                AndroidUtils.copyToClipboard(getActivity(), R.string.mD_copyUrlData, txt_urlData.getText().toString());
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.intent:
            case R.id.urlData:
                AndroidUtils.copyToClipboard(getActivity(), R.string.mD_copyAll, txt_intent.getText() + "\n" + txt_urlData.getText());
                break;
            default:
                return false;
        }
        return true;
    }
}

class DebugConfig extends AModuleConfig {

    final GenericPref.Bool show_toasts;

    public DebugConfig(ModulesActivity activity) {
        super(activity);
        show_toasts = CustomTabs.SHOWTOAST_PREF(activity);
    }

    @Override
    public boolean canBeEnabled() {
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_debug;
    }

    @Override
    public void onInitialize(View views) {
        CheckBox chk_ctabs = views.findViewById(R.id.chk_ctabs);
        chk_ctabs.setChecked(show_toasts.get());
        chk_ctabs.setOnCheckedChangeListener((buttonView, isChecked) -> show_toasts.set(isChecked));
    }
}
