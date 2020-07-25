package com.trianguloy.urlchecker.modules;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.trianguloy.urlchecker.R;

public class TextInputModule extends BaseModule implements TextWatcher {

    private EditText edtxt_url;
    private boolean editByCode = false;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getLayoutBase() {
        return R.layout.module_text;
    }

    @Override
    public void initialize(View views) {
        edtxt_url = views.findViewById(R.id.url);
        edtxt_url.addTextChangedListener(this);
    }

    @Override
    public void onNewUrl(String url) {
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
        setUrl(s.toString());
    }
}
