package com.trianguloy.forceurllib.helpers;

import com.trianguloy.forceurllib.helpers.list.AccessibilityServiceHelper;
import com.trianguloy.forceurllib.helpers.list.AutoBackgroundHelper;
import com.trianguloy.forceurllib.helpers.list.ManualBubbleHelper;
import com.trianguloy.forceurllib.helpers.list.NoneHelper;
import com.trianguloy.forceurllib.helpers.list.SemiautoBubbleHelper;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.Enums;

public enum Helpers implements Enums.IdEnum, Enums.StringEnum {
    none(0, R.string.cHelper_helperNone, new NoneHelper()),
    autoBackground(1, R.string.cHelper_helperAuto, new AutoBackgroundHelper()),
    manualBubble(2, R.string.cHelper_helperManual, new ManualBubbleHelper()),
    semiAutoBubble(3, R.string.cHelper_helperSemiauto, new SemiautoBubbleHelper()),
    accessibilityService(4, R.string.cHelper_helperAccessibility, new AccessibilityServiceHelper());

    // -----

    private final int id;
    private final int stringResource;
    private final AHelper helper;

    Helpers(int id, int stringResource, AHelper helper) {
        this.id = id;
        this.stringResource = stringResource;
        this.helper = helper;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getStringResource() {
        return stringResource;
    }

    public AHelper getHelper() {
        return helper;
    }
}
