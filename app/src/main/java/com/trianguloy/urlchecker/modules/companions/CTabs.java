package com.trianguloy.urlchecker.modules.companions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;

/**
 * Static elements related to CTabs
 * Maybe move here all the other CTabs logic?
 */
public class CTabs {

    /**
     * Ctabs extra intent
     */
    private static final String EXTRA = "android.support.customtabs.extra.SESSION";

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


    /* ------------------- state ------------------- */

    private final GenericPref.Enumeration<OnOffConfig> pref;
    private boolean state = false;

    public CTabs(Context cntx) {
        pref = PREF(cntx);
    }

    /**
     * Initialization from a given intent and a button to toggle
     */
    public void initFrom(Intent intent, ImageButton button) {
        boolean visible;
        if (CTabs.isAvailable()) {
            // configure
            switch (pref.get()) {
                case AUTO:
                default:
                    // If auto we get it from the intent
                    state = intent.hasExtra(CTabs.EXTRA);
                    visible = true;
                    break;
                case HIDDEN:
                    // If hidden we also get it from the intent
                    state = intent.hasExtra(CTabs.EXTRA);
                    visible = false;
                    break;
                case DEFAULT_ON:
                    state = true;
                    visible = true;
                    break;
                case DEFAULT_OFF:
                    state = false;
                    visible = true;
                    break;
                case ALWAYS_ON:
                    state = true;
                    visible = false;
                    break;
                case ALWAYS_OFF:
                    state = false;
                    visible = false;
                    break;
            }
        } else {
            // not available, just ignore
            visible = false;
        }

        // set
        if (visible) {
            // show
            AndroidUtils.longTapForDescription(button);
            AndroidUtils.toggleableListener(button,
                    o -> state = !state,
                    view -> view.setImageResource(state ? R.drawable.ctabs_on : R.drawable.ctabs_off)
            );
            button.setVisibility(View.VISIBLE);
        } else {
            // hide
            button.setVisibility(View.GONE);
        }
    }

    /**
     * applies the setting to a given intent
     */
    public void apply(Intent intent) {
        if (!CTabs.isAvailable()) return;
        if (state && !intent.hasExtra(CTabs.EXTRA)) {
            // enable Custom tabs
            Bundle extras = new Bundle();
            extras.putBinder(CTabs.EXTRA, null); //  Set to null for no session
            intent.putExtras(extras);
        }

        if (!state && intent.hasExtra(CTabs.EXTRA)) {
            // disable Custom tabs
            intent.removeExtra(CTabs.EXTRA);
        }
    }
}
