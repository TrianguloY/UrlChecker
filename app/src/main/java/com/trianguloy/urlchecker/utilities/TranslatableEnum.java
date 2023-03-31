package com.trianguloy.urlchecker.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * On java, an enum can't be extended :(
 * This is the best I can do...unless I discover how to create annotations!
 */
public interface TranslatableEnum {
    /**
     * The id of the saved preference. Must never change
     */
    int getId();

    /**
     * This must return the string resourced associated with this enum value
     */
    int getStringResource();

    /**
     * Get an enum from an id
     */
    static <TE extends TranslatableEnum> TE toEnum(Class<TE> te, int id) {
        TE[] enumConstants = te.getEnumConstants();
        for (TE constant : enumConstants) {
            if (constant.getId() == id){
                return constant;
            }
        }
        return null;
    }

    /**
     * Get a map of id and enum
     */
    static <TE extends TranslatableEnum> Map<Integer, TE> toEnumMap(Class<TE> te) {
        Map<Integer, TE> res = new HashMap<>();
        TE[] enumConstants = te.getEnumConstants();
        for (TE constant : enumConstants) {
            res.put(constant.getId(), constant);
        }
        return res;
    }
}
