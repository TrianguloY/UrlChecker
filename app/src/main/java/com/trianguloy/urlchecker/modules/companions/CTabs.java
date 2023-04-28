package com.trianguloy.urlchecker.modules.companions;

import android.content.Context;
import android.os.Build;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.Enums;
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
    public static GenericPref.Enumeration<Config> PREF(Context cntx) {
        return new GenericPref.Enumeration<>("open_ctabs", Config.AUTO, Config.class, cntx);
    }

    /**
     * Returns true iff the CTabs feature is available on the device
     */
    public static boolean isAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * CTabs configuration
     */
    public enum Config implements Enums.IdEnum, Enums.StringEnum {
        AUTO(0, R.string.auto),
        ON(1, R.string.mOpen_ctabsOn),
        OFF(2, R.string.mOpen_ctabsOff),
        ENABLED(3, R.string.mOpen_ctabsEn),
        DISABLED(4, R.string.mOpen_ctabsDis),
        ;

        // -----

        private final int id;
        private final int stringResource;

        Config(int id, int stringResource) {
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
}
