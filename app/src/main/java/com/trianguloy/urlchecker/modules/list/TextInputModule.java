package com.trianguloy.urlchecker.modules.list;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.wrappers.DefaultTextWatcher;
import com.trianguloy.urlchecker.utilities.wrappers.DoubleEvent;

/** This module shows the current url and allows manual editing */
public class TextInputModule extends AModuleData {

    public static GenericPref.Bool ALWAYSEDIT_PREF(Context cntx) {
        return new GenericPref.Bool("text_alwaysEdit", false, cntx);
    }

    @Override
    public String getId() {
        return "text";
    }

    @Override
    public int getName() {
        return R.string.mInput_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new TextInputDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new TextInputConfig(cntx);
    }
}

class TextInputConfig extends AModuleConfig {

    public TextInputConfig(ModulesActivity cntx) {
        super(cntx);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_input;
    }

    @Override
    public void onInitialize(View views) {
        TextInputModule.ALWAYSEDIT_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.alwaysEdit));
    }
}

class TextInputDialog extends AModuleDialog {

    private final DoubleEvent doubleEdit = new DoubleEvent(1000); // if two updates happens in less than this milliseconds, they are considered as the same
    private final InputMethodManager inputMethodManager;
    private final GenericPref.Bool alwaysEdit;
    private boolean skipUpdate = false;

    private TextView txt_url;
    private EditText edtxt_url;

    public TextInputDialog(MainDialog dialog) {
        super(dialog);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        alwaysEdit = TextInputModule.ALWAYSEDIT_PREF(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_text;
    }

    @Override
    public void onInitialize(View views) {
        txt_url = views.findViewById(R.id.url);
        edtxt_url = views.findViewById(R.id.urlEdit);
        edtxt_url.addTextChangedListener(new DefaultTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (skipUpdate) return;

                // new url by the user
                var newUrlData = new UrlData(s.toString())
                        .dontTriggerOwn()
                        .disableUpdates();

                // mark as minor if too quick
                if (doubleEdit.checkAndTrigger()) newUrlData.asMinorUpdate();

                // set
                setUrl(newUrlData);
            }

        });

        if (alwaysEdit.get()) {
            // always editable
            txt_url.setVisibility(View.GONE);
            edtxt_url.setVisibility(View.VISIBLE);
        } else {
            // editable when clicked
            AndroidUtils.setOnClickWithPositionListener(txt_url, position -> {
                edtxt_url.setSelection(txt_url.getOffsetForPosition(position.first, position.second));
                txt_url.setVisibility(View.GONE);
                edtxt_url.setVisibility(View.VISIBLE);
                // force open the keyboard
                edtxt_url.requestFocus();
                inputMethodManager.showSoftInput(edtxt_url, 0);
            });
        }

    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        // setText fires the afterTextChanged listener, so we need to skip it
        skipUpdate = true;

        edtxt_url.setText(urlData.url);
        if (!alwaysEdit.get()) {
            // back to non-edit
            txt_url.setText(getSpannableUriText(urlData.url));
            txt_url.setVisibility(View.VISIBLE);
            edtxt_url.setVisibility(View.GONE);

            // force close the keyboard
            inputMethodManager.hideSoftInputFromWindow(edtxt_url.getWindowToken(), 0);
        }

        skipUpdate = false;
        doubleEdit.reset(); // next user update, even if immediately after, will be considered new
    }

    private CharSequence getSpannableUriText(String rawUri) {
        var str = new SpannableStringBuilder(rawUri);

        // bold host
        try {
            var start = rawUri.indexOf("://");
            if (start != -1) {
                start += 3;
                var end = rawUri.indexOf("/", start);

                var userinfo = rawUri.indexOf("@", start);
                if (userinfo != -1 && userinfo < end) start = userinfo + 1;

                var port = rawUri.lastIndexOf(":", end);
                if (port != -1 && port > start) end = port;

                str.setSpan(new StyleSpan(BOLD), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }

        // italic query+fragment
        try {
            var start = rawUri.indexOf("?");
            if (start == -1) start = rawUri.indexOf("#");
            if (start != -1) str.setSpan(new StyleSpan(ITALIC), start, rawUri.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }

        return str;
    }
}
