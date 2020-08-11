package com.trianguloy.urlchecker.modules.list;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;

/**
 * This module shows the current url and allows manual editing
 */
public class TextInputModule extends AModuleData {

    @Override
    public String getId() {
        return "text";
    }

    @Override
    public String getName() {
        return "Input text";
    }

    @Override
    public String getDescription() {
        return "Allows to edit the url manually";
    }

    @Override
    public AModuleDialog getDialog(MainDialog dialog) {
        return new TextInputDialog(dialog);
    }
}

class TextInputDialog extends AModuleDialog implements TextWatcher {

    private EditText edtxt_url;
    private boolean editByCode = false;

    public TextInputDialog(MainDialog dialog) {
        super(dialog);
    }


    @Override
    public int getLayoutDialog() {
        return R.layout.module_text;
    }

    @Override
    public void onInitialize(View views) {
        edtxt_url = views.findViewById(R.id.url);
        edtxt_url.addTextChangedListener(this);
    }

    @Override
    public void onNewUrl(String url) {
        // setText fires the afterTextChanged listener, so we need to manually disable it
        editByCode = true;
        edtxt_url.setText(url);
        editByCode = false;
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
        if(editByCode) return;
        // new url by the user
        setUrl(s.toString());
    }
}
