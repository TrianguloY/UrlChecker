package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import static com.trianguloy.urlchecker.utilities.methods.PackageUtils.startActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.AccessibilityServiceHelper;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.AutoBackground;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.ManualBubble;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.NoneHelper;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.SemiautoBubble;
import com.trianguloy.urlchecker.utilities.Enums;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

public class UrlHelperCompanion {
    public static GenericPref.Enumeration<Helper> CURRENT_PREF(Context cntx) {
        return new GenericPref.Enumeration<>("urlHelper_urlHelper",
                Helper.none,
                Helper.class,
                cntx);
    }

    public static GenericPref.Bool STOREBEFORERELEASE_PREF(Context cntx) {
        return new GenericPref.Bool("urlHelper_storeBeforeRelease",
                true,
                cntx);
    }

    public static GenericPref.Int TIMER_PREF(Context cntx) {
        return new GenericPref.Int("urlHelper_timer",
                10,
                cntx);
    }

    // ---
    public static void showSettings(Activity cntx) {
        new UrlHelperConfig(cntx).showSettings();
    }

    /* ------------------- enums ------------------- */
    public enum Helper implements Enums.IdEnum, Enums.StringEnum {
        none(0, R.string.cHelper_helperNone, new NoneHelper()),
        autoBackground(1, R.string.cHelper_helperAuto, new AutoBackground()),
        manualBubble(2, R.string.cHelper_helperManual, new ManualBubble()),
        semiAutoBubble(3, R.string.cHelper_helperSemiauto, new SemiautoBubble()),
        accessibilityService(4, R.string.cHelper_helperAccessibility, new AccessibilityServiceHelper());

        // -----

        private final int id;
        private final int stringResource;
        private final JavaUtils.TriConsumer<Context, String, String> function;

        Helper(int id, int stringResource, JavaUtils.TriConsumer<Context, String, String> function) {
            this.id = id;
            this.stringResource = stringResource;
            this.function = function;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getStringResource() {
            return stringResource;
        }

        public JavaUtils.TriConsumer<Context, String, String> getFunction() {
            return function;
        }


    }

    public enum Compatibility {
        notCompatible,
        urlNeedsHelp,
        compatible

    }
}

class UrlHelperConfig {

    /* ------------------- prefs ------------------- */
    private final GenericPref.Enumeration<UrlHelperCompanion.Helper> current;
    private final GenericPref.Bool storeBeforeRestore;
    private final GenericPref.Int timer;

    /* ------------------- constructor ------------------- */

    private final Activity cntx;

    public UrlHelperConfig(Activity cntx) {
        this.cntx = cntx;

        current = UrlHelperCompanion.CURRENT_PREF(cntx);
        storeBeforeRestore = UrlHelperCompanion.STOREBEFORERELEASE_PREF(cntx);
        timer = UrlHelperCompanion.TIMER_PREF(cntx);
    }

    /**
     * Show the settings dialog
     */
    public void showSettings() {
        // prepare dialog content
        View views = cntx.getLayoutInflater().inflate(R.layout.config_urlhelper, null);

        // configure
        current.attachToSpinner(views.findViewById(R.id.helperType_pref), null);
        storeBeforeRestore.attachToSwitch(views.findViewById(R.id.storeBeforeRelease_pref));
        timer.attachToEditText(views.findViewById(R.id.seconds_pref), 0);
        views.findViewById(R.id.draw_permissions).setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + cntx.getPackageName()));
            startActivity(intent, 1, cntx);
        });
        views.findViewById(R.id.accessibility_permissions).setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent, 1, cntx);
        });

        // prepare dialog
        AlertDialog dialog = new AlertDialog.Builder(cntx)
                .setView(views)
                .setTitle(R.string.cHelper_title)
                .setCancelable(true)
                .show();

        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
}

