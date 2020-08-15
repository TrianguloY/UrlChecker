package com.trianguloy.urlchecker.modules;

import android.content.Context;

import com.trianguloy.urlchecker.dialogs.MainDialog;

public abstract class AModuleData {
    public abstract String getId();

    public abstract String getName();

    public abstract AModuleDialog getDialog(MainDialog dialog);

    public abstract AModuleConfig getConfig(Context cntx);
}
