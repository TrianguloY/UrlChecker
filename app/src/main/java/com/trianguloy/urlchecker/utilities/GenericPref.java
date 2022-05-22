package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A generic type preference
 *
 * @param <T> type of the preference
 */
public abstract class GenericPref<T> {

    /**
     * android sharedprefs
     */
    protected SharedPreferences prefs;

    /**
     * this preference name
     */
    protected final String prefName;

    /**
     * This preference default value
     */
    protected final T defaultValue;

    /**
     * Constructs a generic pref with name and default value, uninitialized
     *
     * @param prefName     this preference name
     * @param defaultValue this preference default value
     */
    public GenericPref(String prefName, T defaultValue) {
        this.prefName = prefName;
        this.defaultValue = defaultValue;
    }

    /**
     * Initializes this preference
     * Returns itself, but as the subtype (I don't know how to return the correct type)
     *
     * @param cntx with this base context
     */
    public GenericPref<T> init(Context cntx) {
        prefs = cntx.getSharedPreferences(cntx.getPackageName(), Context.MODE_PRIVATE);
        return this;
    }

    /**
     * @return the value of this preference
     */
    public abstract T get();

    /**
     * Sets the value of this preference
     *
     * @param value the value to save
     */
    public abstract void set(T value);

    @Override
    public String toString() {
        return prefName + " = " + get();
    }

    /**
     * Clears this preference value
     */
    public void clear() {
        prefs.edit().remove(prefName).apply();
    }

    // ------------------- Implementations -------------------

    /**
     * An integer preference
     */
    static public class Int extends GenericPref<Integer> {
        public Int(String prefName, Integer defaultValue) {
            super(prefName, defaultValue);
        }

        @Override
        public Integer get() {
            return prefs.getInt(prefName, defaultValue);
        }

        @Override
        public void set(Integer value) {
            prefs.edit().putInt(prefName, value).apply();
        }
    }

    /**
     * A boolean preference
     */
    static public class Bool extends GenericPref<Boolean> {
        public Bool(String prefName, Boolean defaultValue) {
            super(prefName, defaultValue);
        }

        @Override
        public Boolean get() {
            return prefs.getBoolean(prefName, defaultValue);
        }

        @Override
        public void set(Boolean value) {
            prefs.edit().putBoolean(prefName, value).apply();
        }
    }

    /**
     * A string preference
     */
    static public class Str extends GenericPref<String> {
        public Str(String prefName, String defaultValue) {
            super(prefName, defaultValue);
        }

        @Override
        public String get() {
            return prefs.getString(prefName, defaultValue);
        }

        @Override
        public void set(String value) {
            prefs.edit().putString(prefName, value).apply();
        }
    }


    /**
     * A list of strings preference
     */
    static public class LstStr extends GenericPref<List<String>> {

        static final String SEPARATOR = ";";

        public LstStr(String prefName, List<String> defaultValue) {
            super(prefName, defaultValue);
        }

        @Override
        public List<String> get() {
            return split(prefs.getString(prefName, join(defaultValue)));
        }

        @Override
        public void set(List<String> value) {
            prefs.edit().putString(prefName, join(value)).apply();
        }

        private static String join(List<String> value) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < value.size(); i++) {
                if (i != 0) sb.append(SEPARATOR);
                sb.append(value.get(i));
            }
            return sb.toString();
        }

        private static List<String> split(String value) {
            ArrayList<String> list = new ArrayList<>();
            if (value != null) list.addAll(Arrays.asList(value.split(SEPARATOR)));
            return list;
        }
    }
}
