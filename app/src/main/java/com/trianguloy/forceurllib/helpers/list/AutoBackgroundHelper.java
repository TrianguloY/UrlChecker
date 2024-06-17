package com.trianguloy.forceurllib.helpers.list;

import android.content.Context;

import com.trianguloy.forceurllib.utilities.ClipboardBorrower;
import com.trianguloy.forceurllib.helpers.AHelper;
import com.trianguloy.forceurllib.lib.Preferences;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AutoBackgroundHelper implements AHelper {

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
    public void run(Context context, String url, String pckg, String mode) {
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
            }, Preferences.TIMER_PREF(context).get(), TimeUnit.SECONDS);
        }
    }
}
