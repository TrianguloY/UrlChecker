package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import android.content.Context;

import com.trianguloy.urlchecker.utilities.Enums;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;


public enum UrlHelper implements Enums.IdEnum{
    // 0 reserved for auto
    autoClipboard(1, new AutoClipboard()),
    manualClipboard(2, null),
    semiAutoClipboard(3,null);
    // open another app?
    // open another app in incognito?

    public static final int timerSeconds = 10;
    private int id;
    private JavaUtils.BiConsumer<Context, String> helper;

    UrlHelper(int id, JavaUtils.BiConsumer<Context, String> helper) {
        this.id = id;
        this.helper = helper;
    }

    @Override
    public int getId() {
        return id;
    }

    public JavaUtils.BiConsumer<Context, String> getHelper(){
        return helper;
    }

    public enum compatibility {
        notCompatible,
        urlNeedsHelp,
        compatible

    }
}
