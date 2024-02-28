package com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers;

import android.content.Context;
import android.os.Build;

import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.ClipboardBorrower;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.HelperManager;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelper;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AutoBackground implements UrlHelper {

    @Override
    public boolean isCompatible() {
        // Only works reliably on Android 9 or less
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
    }

    // Uses clipboard

    @Override
    public HelperManager.Type getType() {
        return HelperManager.Type.background;
    }

    @Override
    public HelperManager.Autonomy getAutonomy() {
        return HelperManager.Autonomy.auto;
    }


    @Override
    public JavaUtils.BiConsumer<Context, String> getFunction() {
        return new Function();
    }

    private static class Function implements JavaUtils.BiConsumer<Context, String> {
        // Only one clipboard, only one thread
        static ScheduledThreadPoolExecutor executor = null;
        static ScheduledFuture<?> task = null;
        static final Object lock = new Object();

        /**
         * Copies URL into clipboard and launches a thread that will restore the clipboard some time
         * later. If called while another thread is waiting to restore the clipboard, that one will
         * be cancelled an a new one launched.
         */
        @Override
        public void accept(Context context, String url) {
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
                }, HelperManager.timerSeconds, TimeUnit.SECONDS);
            }
        }
    }
}
