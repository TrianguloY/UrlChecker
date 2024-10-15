package com.trianguloy.urlchecker.modules.companions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.PackageUtils;

/**
 * The share functionality.
 * Currently in the OpenDialog, but may be moved to an independent dialog in the future.
 */
public interface ShareUtility {
    static GenericPref.Bool CLOSESHARE_PREF(Context cntx) {
        return new GenericPref.Bool("open_closeshare", true, cntx);
    }

    static GenericPref.Bool CLOSECOPY_PREF(Context cntx) {
        return new GenericPref.Bool("open_closecopy", false, cntx);
    }

    static GenericPref.Bool MERGECOPY_PREF(Context cntx) {
        return new GenericPref.Bool("open_mergeCopy", false, cntx);
    }

    /** The onInitialize of an AModuleConfig */
    static void onInitializeConfig(View views, Activity activity) {
        CLOSESHARE_PREF(activity).attachToSwitch(views.findViewById(R.id.closeshare_pref));
        CLOSECOPY_PREF(activity).attachToSwitch(views.findViewById(R.id.closecopy_pref));
        MERGECOPY_PREF(activity).attachToSwitch(views.findViewById(R.id.mergeCopy_pref));
    }

    /** The equivalent of an AModuleDialog */
    class Dialog {

        private final MainDialog mainDialog;
        private final GenericPref.Bool closeSharePref;
        private final GenericPref.Bool closeCopyPref;
        private final GenericPref.Bool mergeCopyPref;

        public Dialog(MainDialog mainDialog) {
            this.mainDialog = mainDialog;
            closeSharePref = ShareUtility.CLOSESHARE_PREF(mainDialog);
            closeCopyPref = ShareUtility.CLOSECOPY_PREF(mainDialog);
            mergeCopyPref = ShareUtility.MERGECOPY_PREF(mainDialog);
        }

        public void onInitialize(View views) {

            // init copy & share
            var btn_copy = views.findViewById(R.id.copyUrl);
            var btn_share = views.findViewById(R.id.share);
            btn_share.setOnClickListener(v -> shareUrl());
            if (mergeCopyPref.get()) {
                // merge mode (single button)
                btn_copy.setVisibility(View.GONE);
                btn_share.setOnLongClickListener(v -> {
                    copyUrl();
                    return true;
                });
            } else {
                // split mode (two buttons)
                btn_copy.setOnClickListener(v -> copyUrl());
                AndroidUtils.longTapForDescription(btn_share);
                AndroidUtils.longTapForDescription(btn_copy);
            }
        }

        /* ------------------- Buttons ------------------- */

        /** Shares the url as text */
        public void shareUrl() {
            // create send intent
            var sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mainDialog.getUrlData().url);
            sendIntent.setType("text/plain");

            // share intent
            var chooser = Intent.createChooser(sendIntent, mainDialog.getString(R.string.mOpen_share));
            PackageUtils.startActivity(
                    chooser,
                    R.string.mOpen_noapps,
                    mainDialog
            );
            if (closeSharePref.get()) {
                mainDialog.finish();
            }
        }

        /** Copy the url */
        public void copyUrl() {
            AndroidUtils.copyToClipboard(mainDialog, R.string.mOpen_clipboard, mainDialog.getUrlData().url);
            if (closeCopyPref.get()) {
                mainDialog.finish();
            }
        }
    }

}
