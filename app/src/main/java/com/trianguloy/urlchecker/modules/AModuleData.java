package com.trianguloy.urlchecker.modules;

import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;

public abstract class AModuleData {

    /**
     * @return the unique identifier of this module
     */
    public abstract String getId();

    /**
     * @return the user visible name of this module
     */
    public abstract String getName();

    /**
     * Returns the dialog fragment of this module
     *
     * @param cntx for the fragment
     * @return the initialized module dialog class
     */
    public abstract AModuleDialog getDialog(MainDialog cntx);

    /**
     * Returns the configuration fragment of this module
     *
     * @param cntx for the fragment
     * @return the initialized module configuration class
     */
    public abstract AModuleConfig getConfig(ConfigActivity cntx);
}
