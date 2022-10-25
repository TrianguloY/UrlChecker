package com.trianguloy.urlchecker.modules.list;

import android.app.AlertDialog;
import android.text.util.Linkify;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.GenericPref;

import java.util.Date;

/**
 * A module that logs all urls that passes through it
 */
public class LogModule extends AModuleData {

    public static GenericPref.Str LOG_DATA() {
        return new GenericPref.Str("log_data", "");
    }

    @Override
    public String getId() {
        return "log";
    }

    @Override
    public int getName() {
        return R.string.mLog_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new LogDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new LogConfig(cntx);
    }
}

class LogDialog extends AModuleDialog {

    private final GenericPref.Str log = LogModule.LOG_DATA();

    public LogDialog(MainDialog dialog) {
        super(dialog);
        log.init(dialog);
    }

    @Override
    public int getLayoutId() {
        return -1;
    }

    @Override
    public void onInitialize(View views) {
        // new instance, log date
        log.add((log.get().isEmpty() ? "" : "\n")
                + "--- " + new Date().toLocaleString() + " ---\n"
        );
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        // new url, log it
        log.add("> " + urlData.url + "\n");
    }
}

class LogConfig extends AModuleConfig {

    private final GenericPref.Str log = LogModule.LOG_DATA();

    public LogConfig(ConfigActivity activity) {
        super(activity);
        log.init(activity);
    }

    @Override
    public boolean canBeEnabled() {
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_log;
    }

    @Override
    public void onInitialize(View views) {
        views.findViewById(R.id.view).setOnClickListener(v -> showLog(false));
        views.findViewById(R.id.edit).setOnClickListener(v -> showLog(true));
    }

    /**
     * Display the log, editable or clickable
     */
    public void showLog(boolean editable) {
        // init textview with content
        // on editable: an editText
        // on non-editable: a textview with links
        TextView textView = editable ? new EditText(getActivity()) : new TextView(getActivity());
        textView.setText(
                !log.get().isEmpty() ? log.get()
                        : editable ? ""
                        : getActivity().getString(R.string.mLog_empty)
        );
        if (!editable) Linkify.addLinks(textView, Linkify.WEB_URLS);

        // wrap into a padded scrollview for nice scrolling
        int pad = getActivity().getResources().getDimensionPixelSize(R.dimen.smallPadding);
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(textView);
        scrollView.setPadding(pad, pad, pad, pad);

        // common dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.mLog_name)
                .setView(scrollView)
                .setNegativeButton(R.string.close, null);

        if (editable) {
            // editable: add save and clear buttons
            builder = builder
                    .setPositiveButton(R.string.save, (dialog, which) ->
                            log.set(textView.getText().toString())
                    )
                    .setNeutralButton(R.string.clear, null); // set below
        }

        // show
        AlertDialog dialog = builder.show();

        // prepare more dialog
        // these are configured here to disable automatic auto-closing when they are pressed
        if (editable) {
            // editable: configure clear button
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                // clear content
                textView.setText("");
            });
        }

    }
}