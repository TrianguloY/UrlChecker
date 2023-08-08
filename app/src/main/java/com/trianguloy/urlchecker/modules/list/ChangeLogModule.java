package com.trianguloy.urlchecker.modules.list;

import android.view.View;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.modules.companions.VersionManager;
import com.trianguloy.urlchecker.utilities.AndroidUtils;

/**
 * This module will show a message if the app was updated.
 * TODO: display the changelog, or a button to open it.
 */
public class ChangeLogModule extends AModuleData {

    @Override
    public String getId() {
        return "changelog";
    }

    @Override
    public int getName() {
        return R.string.mChg_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new ChangeLogModuleDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new DescriptionConfig(R.string.mChg_desc);
    }
}

class ChangeLogModuleDialog extends AModuleDialog {

    public ChangeLogModuleDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_changelog;
    }

    @Override
    public void onInitialize(View views) {
        var versionManager = new VersionManager(getActivity());
        setVisibility(versionManager.wasUpdated());

        AndroidUtils.setRoundedColor(R.color.good, views.findViewById(R.id.updated));

        views.findViewById(R.id.dismiss).setOnClickListener(v -> {
            setVisibility(false);
            versionManager.markSeen();
        });
    }
}