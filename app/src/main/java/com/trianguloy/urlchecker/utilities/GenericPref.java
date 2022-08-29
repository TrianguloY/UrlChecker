package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
     * Sets a [value] for this preference.
     * Note: if the value is the default, it will be cleared instead.
     */
    public void set(T value) {
        if (!Objects.equals(value, defaultValue)) {
            // non-default, save
            save(value);
        } else {
            // default, clear
            clear();
        }
    }

    /**
     * Sets a [value] for this preference.
     */
    protected abstract void save(T value);

    /**
     * Clears this preference value
     */
    public void clear() {
        prefs.edit().remove(prefName).apply();
    }


    @Override
    public String toString() {
        return prefName + " = " + get();
    }

    // ------------------- Implementations -------------------

    /**
     * An Int preference
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
        public void save(Integer value) {
            prefs.edit().putInt(prefName, value).apply();
        }
    }

    /**
     * A Long preference
     */
    static public class Lng extends GenericPref<Long> {
        public Lng(String prefName, Long defaultValue) {
            super(prefName, defaultValue);
        }

        @Override
        public Long get() {
            return prefs.getLong(prefName, defaultValue);
        }

        @Override
        public void save(Long value) {
            prefs.edit().putLong(prefName, value).apply();
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
        public void save(Boolean value) {
            prefs.edit().putBoolean(prefName, value).apply();
        }

        /**
         * This checkbox will be set to the pref value, and when the checkbox changes the value will too
         */
        public void attachToCheckBox(CheckBox checkBox) {
            checkBox.setChecked(get());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> set(isChecked));
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
        public void save(String value) {
            prefs.edit().putString(prefName, value).apply();
        }

        /**
         * Adds the value to the existing content
         */
        public void add(String value) {
            set(get() + value);
        }

        /**
         * This editText will be set to the pref value, and when the editText changes the value will too
         */
        public void attachToEditText(EditText editText) {
            editText.setText(get());
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    set(s.toString());
                }
            });
        }
    }


    /**
     * A list of strings preference
     */
    static public class LstStr extends GenericPref<List<String>> {

        final String separator;

        public LstStr(String prefName, String separator, List<String> defaultValue) {
            super(prefName, defaultValue);
            this.separator = separator;
        }

        @Override
        public List<String> get() {
            return split(prefs.getString(prefName, join(defaultValue)));
        }

        @Override
        public void save(List<String> value) {
            prefs.edit().putString(prefName, join(value)).apply();
        }

        private String join(List<String> value) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < value.size(); i++) {
                if (i != 0) sb.append(separator);
                sb.append(value.get(i));
            }
            return sb.toString();
        }

        private List<String> split(String value) {
            ArrayList<String> list = new ArrayList<>();
            if (value != null) list.addAll(Arrays.asList(value.split(separator)));
            return list;
        }
    }

    /**
     * A list of options (enumeration) preference
     */
    static public class Enumeration<T extends Enum<T> & TranslatableEnum> extends GenericPref<T> {
        private final Class<T> type;

        public Enumeration(String prefName, T defaultValue, Class<T> type) {
            super(prefName, defaultValue);
            this.type = type;
        }

        @Override
        public T get() {
            int value = prefs.getInt(prefName, defaultValue.getId());
            for (T entry : type.getEnumConstants()) {
                if (entry.getId() == value) return entry;
            }
            return defaultValue;
        }

        @Override
        public void save(T value) {
            prefs.edit().putInt(prefName, value.getId()).apply();
        }

        /**
         * Populate a spinner with this preference
         */
        public void attachToSpinner(Spinner spinner) {
            // Put elements in the spinner
            T[] values = type.getEnumConstants();
            List<String> names = new ArrayList<>(values.length);
            for (T value : values) {
                names.add(spinner.getContext().getString(value.getStringResource()));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    spinner.getContext(),
                    android.R.layout.simple_spinner_item,
                    names
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            // select current option
            T selection = get();
            for (int i = 0; i < values.length; i++) {
                if (values[i] == selection) spinner.setSelection(i);
            }

            // add listener to auto-change it
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    set(values[i]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

    }
}
