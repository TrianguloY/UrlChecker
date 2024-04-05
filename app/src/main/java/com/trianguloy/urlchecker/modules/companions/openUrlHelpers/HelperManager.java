package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import android.content.Context;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.ManualBubble;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.AutoBackground;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.SemiautoBubble;
import com.trianguloy.urlchecker.utilities.Enums;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

public class HelperManager {
    public static final int timerSeconds = 10;

    // TODO: add string resource
    public enum Helper implements Enums.IdEnum, Enums.StringEnum {
        autoBackground(1, R.string.auto, new AutoBackground()),
        manualBubble(2, R.string.auto, new ManualBubble()),
        semiAutoBubble(3, R.string.auto, new SemiautoBubble());

        // -----

        private final int id;
        private final int stringResource;
        private final JavaUtils.BiConsumer<Context, String> function;

        Helper(int id, int stringResource, JavaUtils.BiConsumer<Context, String> function) {
            this.id = id;
            this.stringResource = stringResource;
            this.function = function;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getStringResource() {
            return stringResource;
        }

        public JavaUtils.BiConsumer<Context, String> getFunction() {
            return function;
        }


    }

    public enum Compatibility {
        notCompatible,
        urlNeedsHelp,
        compatible

    }
}
