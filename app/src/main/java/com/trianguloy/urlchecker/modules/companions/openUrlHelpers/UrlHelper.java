package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import android.content.Context;

import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

public interface UrlHelper {
    boolean isCompatible();
    HelperManager.Type getType();
    HelperManager.Autonomy getAutonomy();
    JavaUtils.BiConsumer<Context, String> getFunction();
}
