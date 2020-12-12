package com.trianguloy.urlchecker.modules.list;

import android.view.View;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;

/**
 * A textview with debug info.
 * Currently shows the original intent (as uri)
 */
public class DebugModule extends AModuleData {
    @Override
    public String getId() {
        return "debug";
    }

    @Override
    public int getName() {
        return R.string.dbg_name;
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
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.dbg_desc);
    }
}

class DebugDialog extends AModuleDialog {

    public DebugDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public void onNewUrl(String url) {
        // ignore
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_debug;
    }

    @Override
    public void onInitialize(View views) {
        ((TextView) views.findViewById(R.id.text1)).setText(
                getActivity().getIntent().toUri(0)
        );
    }
}