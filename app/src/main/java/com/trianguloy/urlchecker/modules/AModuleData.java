package com.trianguloy.urlchecker.modules;

import com.trianguloy.urlchecker.dialogs.MainDialog;

public abstract class AModuleData {
    public abstract String getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract AModuleDialog getDialog(MainDialog dialog);
}
