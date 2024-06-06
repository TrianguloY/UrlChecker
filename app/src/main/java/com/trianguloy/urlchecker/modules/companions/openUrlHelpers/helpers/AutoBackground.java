package com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers;

import android.content.Context;

import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.ClipboardBorrower;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelperCompanion;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AutoBackground implements JavaUtils.TriConsumer<Context, String, String> {

    // Only one clipboard, only one thread
    static ScheduledThreadPoolExecutor executor = null;
    static ScheduledFuture<?> task = null;
    private static final Object lock = ClipboardBorrower.class;

    /**
     * Copies URL into clipboard and launches a thread that will restore the clipboard some time
     * later. If called while another thread is waiting to restore the clipboard, that one will
     * be cancelled an a new one launched.
     */
    @Override
    public void accept(Context context, String url, String pckg) {
        // The task will probably outlive any context
        Context finalContext = context.getApplicationContext();

        synchronized (lock) {
            if (executor == null) {
                executor = new ScheduledThreadPoolExecutor(1);
            }
            if (task != null) {
                task.cancel(true);
            }
            ClipboardBorrower.borrow(context, url);
            task = executor.schedule(() -> {
                synchronized (lock) {
                    if (!Thread.interrupted()) {
                        ClipboardBorrower.releaseSmart(finalContext);
                        executor = null;
                        task = null;
                    }
                }
            }, UrlHelperCompanion.TIMER_PREF(context).get(), TimeUnit.SECONDS);
        }
    }
}
