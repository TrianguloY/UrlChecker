package com.trianguloy.urlchecker.utilities;

import android.view.View;

public class DrawableButtonUtils {
    /**
     * For some reason some drawable buttons are displayed the same when enabled and disabled.
     * This method also sets an alpha as a workaround
     *
     * @param view    view to enable/disable
     * @param enabled new state
     */
    public static void setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.35f);
    }
}
