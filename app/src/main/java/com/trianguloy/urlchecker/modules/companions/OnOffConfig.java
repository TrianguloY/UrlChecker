package com.trianguloy.urlchecker.modules.companions;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.Enums;

/**
 * Enum for a toggle with on/off x default/always and auto states
 */
public enum OnOffConfig implements Enums.IdEnum, Enums.StringEnum {
    AUTO(0, R.string.auto),
    ON(1, R.string.defaultOn),
    OFF(2, R.string.defaultOff),
    ENABLED(3, R.string.alwaysOn),
    DISABLED(4, R.string.alwaysOff),
    ;

    // -----

    private final int id;
    private final int stringResource;

    OnOffConfig(int id, int stringResource) {
        this.id = id;
        this.stringResource = stringResource;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getStringResource() {
        return stringResource;
    }
}
