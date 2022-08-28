package com.trianguloy.urlchecker.utilities;

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
}
