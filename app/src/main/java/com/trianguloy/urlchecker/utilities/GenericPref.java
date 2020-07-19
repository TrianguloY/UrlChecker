package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class GenericPref<T> {

    protected SharedPreferences prefs;
    protected final String prefName;
    protected final T defaultValue;

    public GenericPref(String prefName, T defaultValue) {
        this.prefName = prefName;
        this.defaultValue = defaultValue;
    }

    public void init(Context cntx){
        prefs = cntx.getSharedPreferences(cntx.getPackageName(), Context.MODE_PRIVATE);
    }

    public abstract T get();

    public abstract void set(T value);

    @Override
    public String toString() {
        return prefName+" = "+get();
    }

    // ------------------- Implementations -------------------

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
}
