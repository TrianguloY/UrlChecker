package com.trianguloy.urlchecker.modules.list;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.Inflater;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This module allows flag edition
 */
public class FlagsModule extends AModuleData {

    public static GenericPref.Str DEFAULTFLAGS_PREF(Context cntx) {
        return new GenericPref.Str("flagsEditor_defaultFlags", null, cntx);
    }

    @Override
    public String getId() {
        return "flagsEditor";
    }

    @Override
    public int getName() {
        return R.string.mFlags_name;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new FlagsDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new FlagsConfig(cntx);
    }
}

class FlagsDialog extends AModuleDialog {

    public static final String DATA_FLAGS = "flagsEditor.flags";

    // https://github.com/MuntashirAkon/AppManager/blob/19782da4c8556c817ba5795554a1cc21f38af13a/app/src/main/java/io/github/muntashirakon/AppManager/intercept/ActivityInterceptor.java#L92
    private static final List<String> ALL_FLAGS = List.of(
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "FLAG_FROM_BACKGROUND",
            "FLAG_DEBUG_LOG_RESOLUTION",
            "FLAG_EXCLUDE_STOPPED_PACKAGES",
            "FLAG_INCLUDE_STOPPED_PACKAGES",
            "FLAG_GRANT_PERSISTABLE_URI_PERMISSION",
            "FLAG_GRANT_PREFIX_URI_PERMISSION",
            "FLAG_DIRECT_BOOT_AUTO",
            "FLAG_IGNORE_EPHEMERAL",
            "FLAG_ACTIVITY_NO_HISTORY",
            "FLAG_ACTIVITY_SINGLE_TOP",
            "FLAG_ACTIVITY_NEW_TASK",
            "FLAG_ACTIVITY_MULTIPLE_TASK",
            "FLAG_ACTIVITY_CLEAR_TOP",
            "FLAG_ACTIVITY_FORWARD_RESULT",
            "FLAG_ACTIVITY_PREVIOUS_IS_TOP",
            "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS",
            "FLAG_ACTIVITY_BROUGHT_TO_FRONT",
            "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED",
            "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY",
            "FLAG_ACTIVITY_NEW_DOCUMENT",
            "FLAG_ACTIVITY_NO_USER_ACTION",
            "FLAG_ACTIVITY_REORDER_TO_FRONT",
            "FLAG_ACTIVITY_NO_ANIMATION",
            "FLAG_ACTIVITY_CLEAR_TASK",
            "FLAG_ACTIVITY_TASK_ON_HOME",
            "FLAG_ACTIVITY_RETAIN_IN_RECENTS",
            "FLAG_ACTIVITY_LAUNCH_ADJACENT",
            "FLAG_ACTIVITY_MATCH_EXTERNAL",
            "FLAG_ACTIVITY_REQUIRE_NON_BROWSER",
            "FLAG_ACTIVITY_REQUIRE_DEFAULT"
    );
    private final Map<String, Integer> flagMap = new TreeMap<>(); // TreeMap to have the entries sorted by key

    private final GenericPref.Str defaultFlagsPref;

    private EditText flagsHexText;
    private AutoCompleteTextView flagNameText;
    private ImageButton more;
    private LinearLayout box;

    public FlagsDialog(MainDialog dialog) {
        super(dialog);

        defaultFlagsPref = FlagsModule.DEFAULTFLAGS_PREF(dialog);

        try {
            // Only get flags that are present in the current Android version
            for (var field : Intent.class.getFields()) {
                if (ALL_FLAGS.contains(field.getName())) {
                    flagMap.put(field.getName(), (Integer) field.get(null));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_editflags;
    }

    @Override
    public void onInitialize(View views) {
        box = views.findViewById(R.id.box);
        flagsHexText = views.findViewById(R.id.flagsHexText);

        // set the flags to the adapter of the input text
        flagNameText = views.findViewById(R.id.flagText);
        flagNameText.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(
                flagMap.keySet()
        )));
        // so the dropdown gets the maximum width possible
        flagNameText.setDropDownAnchor(R.id.addFlagLayout);
        // FIXME better search, currently it is an autofill, not a search
        // FIXME sometimes its hidden behind keyboard

        // get initial flags
        var defaultFlagsStr = defaultFlagsPref.get();
        if (defaultFlagsStr != null) {
            setFlags(toInteger(defaultFlagsStr));
        }

        // press add to add a flag
        views.<Button>findViewById(R.id.add).setOnClickListener(v -> {
            var flag = flagMap.get(flagNameText.getText().toString());
            if (flag != null) {
                setFlags(getFlagsNonNull() | flag);
            } else {
                Toast.makeText(getActivity(), R.string.mFlags_invalid, Toast.LENGTH_LONG).show();
            }
            // Update views
            updateLayout();
        });

        // press 'more' to expand/collapse the box
        more = views.findViewById(R.id.more);
        more.setOnClickListener(v -> {
            box.setVisibility(box.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            // Update views
            updateLayout();
        });
        box.setVisibility(View.GONE); // start collapsed

        // press edit to start editing, press again to save
        var edit = views.<Button>findViewById(R.id.edit);
        edit.setOnClickListener(v -> {
            if (flagsHexText.isEnabled()) {
                // requested to save
                var flags = toInteger(flagsHexText.getText().toString());
                if (flags != null) {
                    // Extract flags
                    setFlags(flags);
                }
            }
            flagsHexText.setEnabled(!flagsHexText.isEnabled());
            // Update views
            updateLayout();
        });
        // long press to reset
        edit.setOnLongClickListener(v -> {
            // Resets the flags
            setFlags(null);
            // Update views
            updateLayout();
            return true;
        });

    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        updateLayout();
    }

    private void updateLayout() {

        // set text
        var flags = getFlagsNonNull();
        flagsHexText.setText(toHexString(flags));

        // set current flags list
        var decodedFlags = decodeFlags(flags);
        box.removeAllViews();
        if (decodedFlags.size() == 0) {
            // no flags, disable
            AndroidUtils.setEnabled(more, false);
            more.setImageResource(R.drawable.arrow_right);
        } else {
            // flags, enable
            AndroidUtils.setEnabled(more, true);
            more.setImageResource(box.getVisibility() == View.VISIBLE ? R.drawable.arrow_down : R.drawable.arrow_right);
            // For each flag, create a button+text
            for (var flag : decodedFlags) {
                var button_text = Inflater.inflate(R.layout.button_text, box, getActivity());

                // Button that removes the flag
                var button = button_text.<Button>findViewById(R.id.button);
                button.setText(R.string.remove);
                // Apply mask to remove flag
                button.setOnClickListener(v -> {
                    setFlags(getFlagsNonNull() & ~flagMap.get(flag));
                    // Update views
                    updateLayout();
                });

                var text = button_text.<TextView>findViewById(R.id.text);
                text.setText(flag);
            }
        }
    }

    // ------------------- utils -------------------

    /**
     * Decode an int as flags
     */
    private List<String> decodeFlags(int hex) {
        var foundFlags = new ArrayList<String>();
        for (var flag : flagMap.entrySet()) {
            // check if flag is present
            if ((hex & flag.getValue()) != 0) {
                foundFlags.add(flag.getKey());
            }
        }
        return foundFlags;
    }

    // ------------------- store/load flags -------------------
    // this handles the store and load of the flags, if something wants to get the flags
    // it should always use these methods.

    private static final int BASE = 16;
    protected static final String REGEX = "(0x)?[a-fA-F\\d]{1,8}";

    /**
     * parses a text as an hexadecimal flags string.
     * Returns null if invalid
     */
    public static Integer toInteger(String text) {
        if (text != null && text.matches(REGEX)) {
            return Integer.parseInt(text.replaceAll("^0x", ""), BASE);
        } else {
            return null;
        }
    }

    /**
     * Converts an int flags to string
     */
    public static String toHexString(int flags) {
        return "0x" + Integer.toString(flags, BASE);
    }

    /**
     * Retrieves the flags from GlobalData, if it is not defined it will return null
     * Intended for use in other modules
     */
    public static Integer getFlagsNullable(AModuleDialog instance) {
        return toInteger(instance.getData(DATA_FLAGS));
    }

    /**
     * Loads the flags from GlobalData, if none were found it gets the flags from the intent that
     * started this activity
     */
    private int getFlagsNonNull() {
        return getFlagsOrDefault(this, getActivity().getIntent().getFlags());
    }

    /**
     * Loads the flags from GlobalData, if none were found it gets the flags from default
     * Can be used by other modules
     */
    public static int getFlagsOrDefault(AModuleDialog instance, int defaultFlags) {
        var flags = toInteger(instance.getData(DATA_FLAGS));
        return flags == null ? defaultFlags : flags;
    }

    /**
     * Stores the flags in GlobalData
     */
    private void setFlags(Integer flags) {
        putData(DATA_FLAGS, flags == null ? null : toHexString(flags));
    }

}

class FlagsConfig extends AModuleConfig {
    private final GenericPref.Str defaultFlagsPref;

    public FlagsConfig(ModulesActivity activity) {
        super(activity);
        defaultFlagsPref = FlagsModule.DEFAULTFLAGS_PREF(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_flagseditor;
    }

    @Override
    public void onInitialize(View views) {
        defaultFlagsPref.attachToEditText(
                views.findViewById(R.id.flags),
                str -> str,
                str -> str.matches(FlagsDialog.REGEX) ? str : defaultFlagsPref.defaultValue
        );
    }
}