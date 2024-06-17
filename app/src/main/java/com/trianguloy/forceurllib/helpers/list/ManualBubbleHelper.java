package com.trianguloy.forceurllib.helpers.list;

import android.content.Context;

import com.trianguloy.forceurllib.utilities.ClipboardBorrower;
import com.trianguloy.forceurllib.helpers.AHelper;
import com.trianguloy.forceurllib.utilities.Bubble;

public class ManualBubbleHelper implements AHelper {

    // Only one bubble
    private static Bubble bubble = null;
    private static final Object lock = ClipboardBorrower.class;

    /**
     * Launches a Bubble so we can access the clipboard "from the background", if tapped it
     * restores the clipboard at that moment.
     * When restoring the clipboard the Bubble dissapears.
     */
    @Override
    public void run(Context context, String url, String pckg, String mode) {
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
