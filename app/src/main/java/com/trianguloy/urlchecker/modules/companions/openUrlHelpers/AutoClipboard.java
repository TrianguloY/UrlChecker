package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import android.content.Context;
import android.os.Looper;

import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// TODO: Only works reliably on Android 9 or less, with bubbles or notifications it
//  might work on other versions too

public class AutoClipboard implements JavaUtils.BiConsumer<Context, String> {
    // Only one clipboard, only one thread
    static ScheduledThreadPoolExecutor executor = null;
    static ScheduledFuture<?> task = null;

    /**
     * Copies URL into clipboard and launches a thread that will restore the clipboard some time
     * later. If called while another thread is waiting to restore the clipboard, that one will
     * be cancelled an a new one launched.
     */
    @Override
    public void accept(Context context, String url) {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        if (task != null) {
            task.cancel(false);
        }
        ClipboardBorrower borrower = ClipboardBorrower.getClipboardBorrower();
        borrower.borrowClipboard(context, url);
        task = executor.schedule(() -> {
            Looper.prepare();
            borrower.releaseClipboard(context);
            executor = null;
            task = null;
            Looper.loop();
        }, UrlHelper.timerSeconds, TimeUnit.SECONDS);
    }
}
