package com.trianguloy.urlchecker.utilities.methods;

import org.json.JSONArray;
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

    /** returns list.map{ mapper(it) } but using <24 api android java */
    static <I, O> List<O> mapEach(List<I> list, Function<I, O> mapper) {
        var result = new ArrayList<O>();
        for (var i : list) {
            result.add(mapper.apply(i));
        }
        return result;
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
     * Given an {@link Object} gotten from {@link JSONObject#get(String)} and a {@link Class},
     * it will check if the object is either a value or an array, of said class and return it
     * as a list.
     *
     * @throws ClassCastException if the values are not from type {@code T}
     */
    static <T> List<T> getArrayOrElement(Object object, Class<T> clazz) throws ClassCastException, JSONException {
        List<T> result = new ArrayList<T>();

        if (object instanceof JSONArray array) {
            // List
            for (int i = 0; i < array.length(); i++) {
                result.add(getAsOrCrash(array.get(i), clazz));
            }
        } else {
            // Value
            result.add(getAsOrCrash(object, clazz));
        }

        return result;
    }

    // I don't like it but it wasn't throwing an exception when wrongly casting,
    // probably due to type casting
    /**
     * Returns the value as type {@code T} if possible, if not, it throws an exception
     *
     * @throws ClassCastException if the values are not from type {@code T}
     */
    static <T> T getAsOrCrash(Object object, Class<T> clazz) throws ClassCastException{
        if (clazz.isInstance(object)) {
            return (T) object;
        } else {
            throw new ClassCastException("Not of class " + clazz.getName());
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
     * Takes the [limit] last lines of a [text], separated by [delimiter].
     * This is equivalent to text.split(delimiter).takeLast(limit).join(delimiter) but using java sdk 14 (and hopefully more efficient)
     */
    static String limitLines(String text, char delimiter, int limit) {
        // early exit
        if (limit <= 0 || text.isEmpty()) return "";

        // find the first character of the last (first) line required
        var i = text.length() - 1; // character pointer
        var lines = 0; // count full lines (delimiters encountered)
        while (i >= 0 && lines < limit) {
            if (text.charAt(i) == delimiter) lines++; // delimiter found
            i--; // continue
        }
        // split if necessary
        if (lines >= limit) text = text.substring(i + 2); // found the lines, split
        return text;
    }

    /** Removes elements from a collection matching a predicate */
    static <E> void removeIf(Collection<E> collection, Function<E, Boolean> predicate) {
        var iterator = collection.iterator();
        while (iterator.hasNext()) if (predicate.apply(iterator.next())) iterator.remove();
    }

    /**
     * Returns the object, or default if null
     * java.util.Optional requires api 24
     */
    static <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Calls the function if obj is not null
     * java.util.Optional requires api 24
     */
    static <T> void ifPresent(T obj, Runnable func){
        if (obj != null){
            func.run();
        }
    }

    /**
     * if the element is present in the list, removes it
     * if not, adds it
     */
    static <E> void toggleContains(Collection<E> list, E element) {
        if (list.contains(element)) list.remove(element);
        else list.add(element);
    }

    /** java.util.function.Consumer requires api 24 */
    @FunctionalInterface
    interface Consumer<T> {
        void accept(T t);
    }

    /** java.util.function.Supplier requires api 24 */
    @FunctionalInterface
    interface Supplier<T> {
        T get();
    }

    /** java.util.function.Function requires api 24 */
    @FunctionalInterface
    interface Function<T, R> {
        R apply(T t);
    }

    /** Negates a boolean Function */
    static <T> Function<T, Boolean> negate(Function<T, Boolean> function) {
        return t -> !function.apply(t);
    }

    /**
     * java.util.function.UnaryOperator requires api 24
     */
    @FunctionalInterface
    interface UnaryOperator<T> extends Function<T, T> {
    }

    /**
     * java.time.Duration requires api 26
     */
    static int weeksToMillis(int weeks){
        return weeks*daysToMillis(7);
    }

    /**
     * java.time.Duration requires api 26
     */
    static int daysToMillis(int days){
        return days*hoursToMillis(24);
    }

    /**
     * java.time.Duration requires api 26
     */
    static int hoursToMillis(int hours){
        return hours*minutesToMillis(60);
    }

    /**
     * java.time.Duration requires api 26
     */
    static int minutesToMillis(int minutes){
        return minutes*secondsToMillis(60);
    }

    /**
     * java.time.Duration requires api 26
     */
    static int secondsToMillis(int seconds){
        return seconds*1000;
    }
}
