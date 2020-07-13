package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class GenericPref<T> {

    protected final SharedPreferences prefs;
    protected final String prefName;
    protected final T defaultValue;

    public GenericPref(Context cntx, String filename, String prefName, T defaultValue) {
        this.prefName = prefName;
        this.defaultValue = defaultValue;
        prefs = cntx.getSharedPreferences(filename, Context.MODE_PRIVATE);
    }

    public abstract T get();

    public abstract void set(T value);

    // ------------------- Implementations -------------------

    static public class Int extends GenericPref<Integer> {
        public Int(Context cntx, String filename, String prefName, Integer defaultValue) {
            super(cntx, filename, prefName, defaultValue);
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

    static public class Str extends GenericPref<String> {
        public Str(Context cntx, String filename, String prefName, String defaultValue) {
            super(cntx, filename, prefName, defaultValue);
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
}
