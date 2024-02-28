package com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers;

import android.content.Context;

import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.ClipboardBorrower;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.UrlHelper;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.HelperManager;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;
import com.trianguloy.urlchecker.utilities.wrappers.Bubble;

public class ManualBubble implements UrlHelper {
    @Override
    public boolean isCompatible() {
        return true;
    }

    // Uses clipboard

    @Override
    public HelperManager.Type getType() {
        return HelperManager.Type.bubble;
    }

    @Override
    public HelperManager.Autonomy getAutonomy() {
        return HelperManager.Autonomy.manual;
    }

    @Override
    public JavaUtils.BiConsumer<Context, String> getFunction() {
        return new Function();
    }

    // Only one bubble
    private static Bubble bubble = null;
    private static final Object lock = new Object();

    private class Function implements JavaUtils.BiConsumer<Context, String> {
        /**
         * Launches a Bubble so we can access the clipboard "from the background", if tapped it
         * restores the clipboard at that moment.
         * When restoring the clipboard the Bubble dissapears.
         */
        @Override
        public void accept(Context context, String url) {
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
}
