package com.trianguloy.urlchecker.modules.companions.openUrlHelpers;

import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.ManualBubble;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.AutoBackground;
import com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers.SemiautoBubble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HelperManager {
    public static final int timerSeconds = 10;
    static UrlHelper[][] urlHelpers = new UrlHelper[Autonomy.values().length][Type.values().length];

    static {
        java.util.List<UrlHelper> helpersList = new ArrayList<>();
        helpersList.add(new ManualBubble());
        helpersList.add(new AutoBackground());
        helpersList.add(new SemiautoBubble());

        for (UrlHelper urlHelper : helpersList) {
            urlHelpers[urlHelper.getAutonomy().ordinal()][urlHelper.getType().ordinal()] = urlHelper;
        }
    }

    public static UrlHelper getHelper(Autonomy autonomy, Type type) {
        return urlHelpers[autonomy.ordinal()][type.ordinal()];
    }

    public enum Autonomy {
        auto,
        semiauto,
        manual
    }

    public final static Set<Type> clipboardSet = new HashSet<>(Arrays.asList(
            Type.background, Type.bubble, Type.notification));

    public enum Type {
        background,
        bubble,
        notification
    }

    public enum Compatibility {
        notCompatible,
        urlNeedsHelp,
        compatible

    }
}
