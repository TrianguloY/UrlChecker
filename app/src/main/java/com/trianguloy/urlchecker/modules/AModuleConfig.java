package com.trianguloy.urlchecker.modules;

import com.trianguloy.urlchecker.utilities.Fragment;

public abstract class AModuleConfig implements Fragment {
    public abstract boolean canBeEnabled();
}
