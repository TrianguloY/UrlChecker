package com.trianguloy.urlchecker.modules.list;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.DoubleEvent;

/**
 * This module shows the current url and allows manual editing
 */
public class TextInputModule extends AModuleData {

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
        return new DescriptionConfig(R.string.mInput_desc);
    }
}

class TextInputDialog extends AModuleDialog implements TextWatcher {

    private final DoubleEvent doubleEdit = new DoubleEvent(1000); // if two updates happens in less than this milliseconds, they are considered as the same

    private EditText edtxt_url;

    public TextInputDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_text;
    }

    @Override
    public void onInitialize(View views) {
        edtxt_url = views.findViewById(R.id.url);
        edtxt_url.addTextChangedListener(this);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        // setText fires the afterTextChanged listener, so we need to remove it
        edtxt_url.removeTextChangedListener(this);
        edtxt_url.setText(urlData.url);
        edtxt_url.addTextChangedListener(this);
        doubleEdit.reset(); // next user update, even if immediately after, will be considered new
    }

    // ------------------- TextWatcher -------------------

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        // new url by the user
        var newUrlData = new UrlData(s.toString())
                .dontTriggerOwn()
                .disableUpdates();

        // mark as minor if too quick
        if (doubleEdit.checkAndTrigger()) newUrlData.asMinorUpdate();

        // set
        setUrl(newUrlData);
    }
}
