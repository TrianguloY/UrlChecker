package com.trianguloy.urlchecker.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generic Java utils.
 * I prefer smaller and more available apps, even if they require an older API and not using Kotlin
 */
public class JavaUtilities {

    /**
     * Converts an iterator to a list
     */
    public static <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}
