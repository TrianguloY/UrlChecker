package com.trianguloy.urlchecker.modules.companions;

import android.content.Context;
import android.os.Build;

import com.trianguloy.urlchecker.utilities.GenericPref;

/**
 * Static elements related to CTabs
 * Maybe move here all the other CTabs logic?
 */
public class CTabs {

    /**
     * Ctabs extra intent
     */
    public static final String EXTRA = "android.support.customtabs.extra.SESSION";

    /**
     * CTabs preference
     */
    public static GenericPref.Enumeration<OnOffConfig> PREF(Context cntx) {
        return new GenericPref.Enumeration<>("open_ctabs", OnOffConfig.AUTO, OnOffConfig.class, cntx);
    }

    /**
     * Returns true iff the CTabs feature is available on the device
     */
    public static boolean isAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

}
