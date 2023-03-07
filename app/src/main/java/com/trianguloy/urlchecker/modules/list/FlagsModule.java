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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final GenericPref.Str defaultFlagsPref;

    public static final String FLAGS = "flagsEditor.flags";

    private EditText flagsHexText;
    private AutoCompleteTextView flagNameText;
    private Button add;
    private ImageButton more;
    private Button edit;
    private LinearLayout box;

    private Map<String, Integer> flagMap;

    public FlagsDialog(MainDialog dialog) {
        super(dialog);

        try {
            flagMap = new HashMap<>();
            Collection<String> manualFlags = getDeclaredFlags();

            // Only get flags that are present
            for (Field field : Intent.class.getFields()) {
                if (manualFlags.contains(field.getName())) {
                    flagMap.put(field.getName(), (Integer) field.get(null));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        defaultFlagsPref = FlagsModule.DEFAULTFLAGS_PREF(dialog);
    }

    private Collection<String> getDeclaredFlags() {
        Collection<String> manualFlags = new ArrayList<>();
        // https://github.com/MuntashirAkon/AppManager/blob/19782da4c8556c817ba5795554a1cc21f38af13a/app/src/main/java/io/github/muntashirakon/AppManager/intercept/ActivityInterceptor.java#L92
        manualFlags.add("FLAG_GRANT_READ_URI_PERMISSION");
        manualFlags.add("FLAG_GRANT_WRITE_URI_PERMISSION");
        manualFlags.add("FLAG_FROM_BACKGROUND");
        manualFlags.add("FLAG_DEBUG_LOG_RESOLUTION");
        manualFlags.add("FLAG_EXCLUDE_STOPPED_PACKAGES");
        manualFlags.add("FLAG_INCLUDE_STOPPED_PACKAGES");
        manualFlags.add("FLAG_GRANT_PERSISTABLE_URI_PERMISSION");
        manualFlags.add("FLAG_GRANT_PREFIX_URI_PERMISSION");
        manualFlags.add("FLAG_DIRECT_BOOT_AUTO");   //
        manualFlags.add("FLAG_IGNORE_EPHEMERAL");   //
        manualFlags.add("FLAG_ACTIVITY_NO_HISTORY");
        manualFlags.add("FLAG_ACTIVITY_SINGLE_TOP");
        manualFlags.add("FLAG_ACTIVITY_NEW_TASK");
        manualFlags.add("FLAG_ACTIVITY_MULTIPLE_TASK");
        manualFlags.add("FLAG_ACTIVITY_CLEAR_TOP");
        manualFlags.add("FLAG_ACTIVITY_FORWARD_RESULT");
        manualFlags.add("FLAG_ACTIVITY_PREVIOUS_IS_TOP");
        manualFlags.add("FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
        manualFlags.add("FLAG_ACTIVITY_BROUGHT_TO_FRONT");
        manualFlags.add("FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
        manualFlags.add("FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
        manualFlags.add("FLAG_ACTIVITY_NEW_DOCUMENT");
        manualFlags.add("FLAG_ACTIVITY_NO_USER_ACTION");
        manualFlags.add("FLAG_ACTIVITY_REORDER_TO_FRONT");
        manualFlags.add("FLAG_ACTIVITY_NO_ANIMATION");
        manualFlags.add("FLAG_ACTIVITY_CLEAR_TASK");
        manualFlags.add("FLAG_ACTIVITY_TASK_ON_HOME");
        manualFlags.add("FLAG_ACTIVITY_RETAIN_IN_RECENTS");
        manualFlags.add("FLAG_ACTIVITY_LAUNCH_ADJACENT");
        manualFlags.add("FLAG_ACTIVITY_MATCH_EXTERNAL");
        manualFlags.add("FLAG_ACTIVITY_REQUIRE_NON_BROWSER");
        manualFlags.add("FLAG_ACTIVITY_REQUIRE_DEFAULT");

        return manualFlags;
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_editflags;
    }

    @Override
    public void onInitialize(View views) {
        flagsHexText = views.findViewById(R.id.flagsHexText);
        flagNameText = views.findViewById(R.id.flagText);
        add = views.findViewById(R.id.add);
        more = views.findViewById(R.id.more);
        edit = views.findViewById(R.id.edit);
        box = views.findViewById(R.id.box);

        List<String> allFlagsNames = new ArrayList<>(flagMap.keySet());
        // Sort by value
        Collections.sort(allFlagsNames, (o1, o2) -> flagMap.get(o1).compareTo(flagMap.get(o2)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, allFlagsNames);
        flagNameText.setAdapter(adapter);
        // so the dropdown gets the maximum width possible
        flagNameText.setDropDownAnchor(R.id.addFlagLayout);
        // FIXME better search, currently it is an autofill, not a search
        // FIXME sometimes its hidden behind keyboard

        String defaultFlagsStr = defaultFlagsPref.get();
        if (defaultFlagsStr != null) {
            setFlags(toInteger(defaultFlagsStr));
        }

        // Listeners
        add.setOnClickListener(v -> {
            Integer flag = flagMap.get(flagNameText.getText().toString());
            if (flag != null) {
                setFlags(getFlagsNonNull() | flag);
            } else {
                Toast.makeText(getActivity(), R.string.mFlags_invalid, Toast.LENGTH_LONG).show();
            }
            // Update views
            setUrl(getUrl());
        });
        // Expands the box
        more.setOnClickListener(v -> {
            box.setVisibility(box.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            // Update views
            setUrl(getUrl());
        });
        edit.setOnClickListener(v -> {
            if (flagsHexText.isEnabled()) {
                Integer flags = toInteger(flagsHexText.getText().toString());
                if (flags != null) {
                    // Extract flags
                    setFlags(flags);
                }
            }
            flagsHexText.setEnabled(!flagsHexText.isEnabled());
            // Update views
            setUrl(getUrl());
        });
        edit.setOnLongClickListener(v -> {
            // Resets the flags
            setFlags(null);
            // Update views
            setUrl(getUrl());
            return true;
        });
        box.setVisibility(View.GONE);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        updateLayout();
    }

    private void updateLayout() {
        box.removeAllViews();
        int flags = getFlagsNonNull();
        flagsHexText.setText(toHexString(flags));

        List<String> decodedFlags = decodeFlags(flags);
        // Sort by value
        Collections.sort(decodedFlags, (o1, o2) -> flagMap.get(o1).compareTo(flagMap.get(o2)));

        if (decodedFlags.size() == 0) {
            AndroidUtils.setEnabled(more, false);
        } else {
            AndroidUtils.setEnabled(more, true);
            // For each flag, create a button
            for (String flag : decodedFlags) {
                var button_text = Inflater.inflate(R.layout.button_text, box, getActivity());

                // Button that removes the flag
                var button = button_text.<Button>findViewById(R.id.button);
                button.setText(R.string.remove);
                // Apply mask to remove flag
                button.setOnClickListener(v -> {
                    setFlags(getFlagsNonNull() & ~flagMap.get(flag));
                    // Update views
                    setUrl(getUrl());
                });

                var text = button_text.<TextView>findViewById(R.id.text);
                text.setText(flag);
            }
        }
        more.setImageResource(box.getChildCount() == 0 ? R.drawable.arrow_right
                : box.getVisibility() == View.VISIBLE ? R.drawable.arrow_down
                : R.drawable.arrow_right);
    }

    // ------------------- utils -------------------
    private List<String> decodeFlags(int hex) {
        List<String> foundFlags = new ArrayList<>();
        for (String flagName : flagMap.keySet()) {
            // check if flag is present
            if ((hex & flagMap.get(flagName)) != 0) {
                foundFlags.add(flagName);
            }
        }
        return foundFlags;
    }

    // ------------------- store/load flags -------------------
    // this handles the store and load of the flags, if something wants to get the flags
    // it should always use these methods.

    private static final int BASE = 16;
    protected static final String REGEX = "0x[a-fA-F\\d]{1,8}";

    public static Integer toInteger(String text) {
        if (text != null && text.matches(REGEX)) {
            return Integer.parseInt(text.substring(2), BASE);
        } else {
            return null;
        }
    }

    public static String toHexString(int flags) {
        return "0x" + Integer.toHexString(flags);
    }

    /**
     * Retrieves the flags from GlobalData, if it is not defined it will return null
     * Intended for use in other modules
     */
    public static Integer getFlagsNullable(AModuleDialog instance) {
        return toInteger(instance.getData(FlagsDialog.FLAGS));
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
        Integer flags = toInteger(instance.getData(FLAGS));
        return flags == null ?
                defaultFlags :
                flags;
    }

    /**
     * Stores the flags in GlobalData
     */
    private void setFlags(Integer flags) {
        putData(FLAGS, flags == null ? null : toHexString(flags));
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
        defaultFlagsPref.attachToEditText(views.findViewById(R.id.flags), str -> str, str -> str.matches(FlagsDialog.REGEX) ? str : defaultFlagsPref.defaultValue);
    }
}