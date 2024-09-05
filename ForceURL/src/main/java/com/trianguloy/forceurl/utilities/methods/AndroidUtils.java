package com.trianguloy.forceurl.utilities.methods;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.trianguloy.forceurl.R;

import java.util.HashSet;
import java.util.Set;

public interface AndroidUtils {
    /**
     * Returns the main activity of a package
     */
    static String getMainActivity(Context cntx, String pckg) {
        return cntx.getPackageManager()
                .getLaunchIntentForPackage(pckg)
                .getComponent().getClassName();
    }

    /**
     * Returns a Set with all the activities of a package
     */
    static Set<String> getActivities(Context cntx, String pckg) {
        Set<String> activities = new HashSet<>();
        try {
            ActivityInfo[] activityInfos = (cntx.getPackageManager()
                    .getPackageInfo(pckg, PackageManager.GET_ACTIVITIES).activities);

            for (var act : activityInfos) {
                activities.add(act.name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return activities;
    }

    /**
     * Get primary clip from clipboard, retrieves string from id
     */
    static ClipData getPrimaryClip(Context context, int id) {
        return getPrimaryClip(context, context.getString(id), context.getString(R.string.clipboard_getError));
    }

    /**
     * Get primary clip from clipboard, retrieves string from id
     */
    static ClipData getPrimaryClip(Context context, int id, int failId) {
        return getPrimaryClip(context, context.getString(id), context.getString(failId));
    }

    /**
     * Get primary clip from clipboard
     */
    static ClipData getPrimaryClip(Context context, String toast) {
        return getPrimaryClip(context, toast, context.getString(R.string.clipboard_getError));
    }

    static ClipData getPrimaryClip(Context context, String toast, String failToast) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            safeToast(context, R.string.clipboard_serviceError, Toast.LENGTH_LONG);
            return null;
        }

        // NOTE: according to https://stackoverflow.com/a/38965870
        //  if the clipboard is empty, it will return null, however there is no mention of this in
        //  the documentation https://developer.android.com/reference/android/content/ClipboardManager#getPrimaryClip().
        //  If there is a way to know it is empty for sure we should instead return ClipData.newPlainText("", "")
        //  or similar. We want to keep null for cases in which we cannot access the clipboard
        ClipData res = clipboard.getPrimaryClip();
        if (res != null) {
            // show toast to notify it was read (except on Android 13+, where the device shows a popup itself)
            if (Build.VERSION.SDK_INT < /*Build.VERSION_CODES.TIRAMISU*/33)
                safeToast(context, toast, Toast.LENGTH_LONG);
        } else {
            // FIXME: does android show toast on failed read?
            safeToast(context, failToast, Toast.LENGTH_LONG);
        }
        return res;
    }

    /**
     * Set primary clip from clipboard, retrieves string from id
     */
    static void setPrimaryClip(Context context, int id, ClipData clipData) {
        setPrimaryClip(context, context.getString(id), clipData);
    }

    /**
     * Set primary clip from clipboard
     */
    static void setPrimaryClip(Context context, String toast, ClipData clipData) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            safeToast(context, R.string.clipboard_setError, Toast.LENGTH_LONG);
            return;
        }

        clipboard.setPrimaryClip(clipData);

        // show toast to notify it was read (except on Android 13+, where the device shows a popup itself)
        if (Build.VERSION.SDK_INT < /*Build.VERSION_CODES.TIRAMISU*/33)
            safeToast(context, toast, Toast.LENGTH_LONG);
    }

    /**
     * Checks if the clipboard can be read at this moment. For android 10+ that means the app is
     * focused or has a special permission granted via ADB (READ_CLIPBOARD_IN_BACKGROUND).
     */
    static boolean canReadClip(Context context) {
        // FIXME: find a better way, this creates an unnecessary toast
        // FIXME: confusing toast, if unable to read clipboard we will make a workaround to force it
        return getPrimaryClip(context,
                R.string.clipboard_canReadTrue,
                R.string.clipboard_canReadFalse) != null;
    }

    /**
     * Just {@link #safeToast(Context, int, int)} but retrieves string from id
     */
    static void safeToast(Context context, int resId, int duration) {
        safeToast(context, context.getString(resId), duration);
    }

    /**
     * A way to show {@link Toast#makeText(Context, CharSequence, int)} if unsure wether this is the
     * UI thread
     */
    static void safeToast(Context context, String text, int duration) {
        runOnUiThread(() -> {
            Toast.makeText(context, text, duration).show();
        });
    }

    /**
     * @return Wether it was called from an UI thread
     */
    static boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * To be used when uncertain if this is the UI thread. If it is the UI thread, it runs
     * immediately, if not, it is added to the message queue of the UI thread via
     * {@link Handler#post(Runnable)}.
     *
     * @param run A runnable which will be ran on the UI thread
     * @return Wether it was called from an UI thread
     */
    static boolean runOnUiThread(Runnable run) {
        var isUiThread = isUiThread();
        if (isUiThread) {
            run.run();
        } else {
            new Handler(Looper.getMainLooper()).post(run);
        }
        return isUiThread;
    }
}
