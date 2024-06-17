package com.trianguloy.forceurllib.utilities.methods;

import java.util.List;

public interface JavaUtils {
    /**
     * Returns the element at the specified position if there is one, null if there is none
     */
    static <T> T getSafe(List<T> list, int index) {
        if (list != null && 0 <= index && index < list.size()) {
            return list.get(index);
        }
        return null;
    }
}
