package com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers;

import android.content.Context;

import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.ClipboardBorrower;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;
import com.trianguloy.urlchecker.utilities.wrappers.Bubble;

public class ManualBubble implements JavaUtils.TriConsumer<Context, String, String> {

    // Only one bubble
    private static Bubble bubble = null;
    private static final Object lock = ClipboardBorrower.class;

    /**
     * Launches a Bubble so we can access the clipboard "from the background", if tapped it
     * restores the clipboard at that moment.
     * When restoring the clipboard the Bubble dissapears.
     */
    @Override
    public void accept(Context context, String url, String pckg) {
        synchronized (lock) {
            ClipboardBorrower.borrow(context, url);

            // The bubble will probably outlive any context
            Context finalContext = context.getApplicationContext();
            if (bubble == null) {
                bubble = new Bubble(finalContext, bubbleContext -> {
                    synchronized (lock) {
                        ClipboardBorrower.releaseSmart(bubbleContext);
                        bubble = null;
                    }
                });
            }
        }
    }
}
