package com.trianguloy.urlchecker.utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Generic Java utils.
 * I prefer smaller and more available apps, even if they require an older API and not using Kotlin
 */
public interface JavaUtils {

    /**
     * Converts an iterator to a list
     */
    static <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Converts a string into a json object, returns empty on failure
     */
    static JSONObject toJson(String content) {
        try {
            return new JSONObject(content);
        } catch (JSONException e) {
            // invalid catalog, return empty
            return new JSONObject();
        }
    }

    /**
     * Clamps a value between two other values.
     */
    static int clamp(int min, int value, int max) {
        return min <= max ? Math.max(min, Math.min(value, max))
                // just in case
                : clamp(max, value, min);
    }

    /**
     * Applies a filter to both strings to check if all words of keywords are in body.
     * The order does not matter.
     */
    static boolean containsWords(String body, String keywords) {
        JavaUtils.Function<String, String> filter = s -> s.toUpperCase().replaceAll("[\\s-_]+", " ");
        // Match all words
        String[] words = filter.apply(keywords).split(" ");
        body = filter.apply(body);
        boolean match = true;
        for (String str : words) {
            if (!body.contains(str)) {
                match = false;
                break;
            }
        }
        return match;
    }

    /**
     * Returns the object, or default if null
     * java.util.Optional requires api 24
     */
    static <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * if the element is present in the list, removes it
     * if not, adds it
     */
    static <E> void toggleContains(Collection<E> list, E element) {
        if (list.contains(element)) list.remove(element);
        else list.add(element);
    }

    /**
     * java.util.function.Consumer requires api 24
     */
    @FunctionalInterface
    interface Consumer<T> {
        void accept(T t);
    }

    /**
     * java.util.function.Function requires api 24
     */
    @FunctionalInterface
    interface Function<T, R> {
        R apply(T t);
    }

    /**
     * java.util.function.UnaryOperator requires api 24
     */
    @FunctionalInterface
    interface UnaryOperator<T> extends Function<T, T> {
    }
}
