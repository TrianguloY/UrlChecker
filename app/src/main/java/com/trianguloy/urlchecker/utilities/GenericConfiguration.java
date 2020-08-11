package com.trianguloy.urlchecker.utilities;

public abstract class GenericConfiguration {
    public static class StrPrefConfiguration extends GenericConfiguration {
        public final GenericPref.Str pref;

        public StrPrefConfiguration(String description, GenericPref.Str pref) {
            super(description);
            this.pref = pref;
        }

    }


    public final String description;

    protected GenericConfiguration(String description) {
        this.description = description;
    }

}
