package com.trianguloy.urlchecker.utilities.methods;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic Android utilities
 */
public interface AndroidUtils {

    /**
     * Sets the start drawable of a textview
     * Wrapped for android compatibility
     */
    static void setStartDrawables(TextView txt, int start) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // we can use the function directly!
            txt.setCompoundDrawablesRelativeWithIntrinsicBounds(start, 0, 0, 0);
        } else {
            // we need to manually adjust
            if ((txt.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK) == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL) {
                // rtl
                txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, start, 0);
            } else {
                // ltr
                txt.setCompoundDrawablesWithIntrinsicBounds(start, 0, 0, 0);
            }
        }
    }

    /**
     * For some reason some drawable buttons are displayed the same when enabled and disabled.
     * This method also sets an alpha as a workaround
     */
    static void setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.35f);
    }

    /**
     * In debug mode, throws an AssertionError, in production just logs it and continues.
     */
    static void assertError(String detailMessage) {
        Log.d("ASSERT_ERROR", detailMessage);
        if (BuildConfig.DEBUG) {
            // in debug mode, throw exception
            throw new AssertionError(detailMessage);
        }
        // non-debug, just discard
    }

    /**
     * Sets the background color (resource id) of a view using a rounded box drawable
     */
    static void setRoundedColor(int color, View view) {
        setRawRoundedColor(view.getContext().getResources().getColor(color), view);
    }

    /**
     * Sets the background color (raw color) of a view using a rounded box drawable
     */
    static void setRawRoundedColor(int color, View view) {
        var drawable = view.getContext().getResources().getDrawable(R.drawable.round_box);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC);
        view.setBackgroundDrawable(drawable);
    }

    /**
     * Clears the background color of a view
     */
    static void clearRoundedColor(View view) {
        view.setBackgroundDrawable(null);
    }

    /**
     * Makes the text of a textview display as a link (which does nothing when clicked)
     */
    static void setAsClickable(TextView textview) {
        SpannableStringBuilder text = new SpannableStringBuilder(textview.getText());
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View ignored) {
                // do nothing
            }
        }, 0, text.length(), 0);
        textview.setText(text);
        // TODO: remove underline (set color only). textview.setTextColor(textview.getResources().getColor(R.color.app)); doesn't work
    }

    /**
     * Returns a formatted date/time from epoch milliseconds according to the context locale.
     * If the date is invalid (negative), "---" is returned instead
     */
    static String formatMillis(long millis, Context cntx) {
        if (millis < 0) return "---";

        return DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        ? cntx.getResources().getConfiguration().getLocales().get(0)
                        : cntx.getResources().getConfiguration().locale
        ).format(new Date(millis));
    }

    /**
     * Copy to the clipboard, retrieves string from id
     */
    static void copyToClipboard(Activity activity, int id, String text) {
        copyToClipboard(activity, activity.getString(id), text);
    }

    /**
     * Copy to the clipboard
     */
    static void copyToClipboard(Activity activity, String toast, String text) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(activity, R.string.clipboard_copyError, Toast.LENGTH_LONG).show();
            return;
        }

        clipboard.setPrimaryClip(ClipData.newPlainText("", text));

        // show toast to notify it was copied (except on Android 13+, where the device shows a popup itself)
        if (Build.VERSION.SDK_INT < /*Build.VERSION_CODES.TIRAMISU*/33)
            Toast.makeText(activity, toast, Toast.LENGTH_LONG).show();
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
        return AndroidUtils.getPrimaryClip(context,
                R.string.clipboard_canReadTrue,
                R.string.clipboard_canReadFalse) != null;
    }

    /**
     * Get the (possible) referrer activity from an existing one.
     * Null if can't find
     */
    static String getReferrer(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return null;

        Uri referrer = activity.getReferrer();
        if (referrer == null) return null;

        // the scheme must be "android-app"
        if (!"android-app".equals(referrer.getScheme())) return null;
        // the host is the package
        return referrer.getHost();
    }

    /**
     * @see ActionBar#setDisplayHomeAsUpEnabled(boolean)
     * And don't forget to override onOptionsItemSelected!
     */
    static void configureUp(Activity activity) {
        var actionBar = activity.getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Sets an onClickListener to a [view] so that:
     * [toggle] will be called when clicked (to change something).
     * [listener] will be called now and when clicked (to update state).
     * If you need to initialize things, do them before calling this.
     */
    static <V extends View> void toggleableListener(V view, JavaUtils.Consumer<V> toggle, JavaUtils.Consumer<V> listener) {
        view.setOnClickListener(v -> {
            toggle.accept(view);
            listener.accept(view);
        });
        listener.accept(view);
    }

    /**
     * Adds an onLongClickListener that will show a toast with the contentdescription
     */
    static void longTapForDescription(View view) {
        view.setOnLongClickListener(v -> {
            var contentDescription = v.getContentDescription();
            if (contentDescription == null)
                AndroidUtils.assertError("No content description for view " + view);
            Toast.makeText(v.getContext(), contentDescription, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    /**
     * Sets the text and the visibility of a textview (visible iff there is text)
     */
    static void setHideableText(TextView view, CharSequence text) {
        view.setText(text);
        view.setVisibility(text == null || text.length() == 0 ? View.GONE : View.VISIBLE);
    }

    /**
     * Returns a drawable with a different color
     */
    static Drawable getColoredDrawable(int drawableId, int colorAttr, Context cntx) {
        // get drawable
        var drawable = cntx.getResources().getDrawable(drawableId).mutate();

        // get color
        var resolvedAttr = new TypedValue();
        cntx.getTheme().resolveAttribute(colorAttr, resolvedAttr, true);

        // tint
        drawable.setColorFilter(cntx.getResources().getColor(resolvedAttr.resourceId), PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    /**
     * Returns all the unique links found on a given text
     */
    static Set<String> getLinksFromText(CharSequence text) {
        var links = new HashSet<String>();
        var matcher = Patterns.WEB_URL.matcher(text);
        while (matcher.find()) links.add(matcher.group());
        return links;
    }

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
