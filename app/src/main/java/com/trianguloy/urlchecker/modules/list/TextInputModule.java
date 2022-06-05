package com.trianguloy.urlchecker.modules.list;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;

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
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new TextInputDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.mInput_desc);
    }
}

class TextInputDialog extends AModuleDialog implements TextWatcher {

    private static final int SAME_UPDATE_TIMEOUT = 1000; // if two updates happens in less than this milliseconds, they are considered as the same

    private long lastUpdateTimeMillis = -1; // previous edittext update time
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
    public void onNewUrl(String url, boolean minorUpdate) {
        // setText fires the afterTextChanged listener, so we need to remove it
        edtxt_url.removeTextChangedListener(this);
        edtxt_url.setText(url);
        edtxt_url.addTextChangedListener(this);
        lastUpdateTimeMillis = -1; // next user update, even if immediately after, will be considered new
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
        long currentTime = System.currentTimeMillis();
        setUrl(s.toString(), Flags.DONT_NOTIFY_OWN, Flags.DISABLE_UPDATE,
                // considered minor if the previous update was very recent
                currentTime - lastUpdateTimeMillis < SAME_UPDATE_TIMEOUT ? Flags.MINOR_UPDATE : Flags.NONE);
        lastUpdateTimeMillis = currentTime;
    }
}
