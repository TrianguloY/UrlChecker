package com.trianguloy.urlchecker.modules.list;

import android.view.View;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.ClickableLinks;

import java.util.ArrayList;
import java.util.List;

/**
 * This module checks for patterns characters in the url
 */
public class PatternModule extends AModuleData {

    @Override
    public String getId() {
        return "pattern";
    }

    @Override
    public int getName() {
        return R.string.mPttrn_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new PatternDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.mPttrn_desc);
    }
}

class PatternDialog extends AModuleDialog implements ClickableLinks.OnUrlListener {

    private TextView txt_pattern;

    public PatternDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_pattern;
    }

    @Override
    public void onInitialize(View views) {
        txt_pattern = views.findViewById(R.id.pattern);
    }

    @Override
    public void onNewUrl(String url) {
        List<String> messages = new ArrayList<>();

        // check for non-ascii characters
        String strange = url.replaceAll("\\p{ASCII}", "");
        if (!strange.isEmpty()) {
            messages.add(getActivity().getString(R.string.mPttrn_ascii, strange));
        }

        // check for http
        if (url.startsWith("http:")) {
            messages.add(getActivity().getString(R.string.mPttrn_http));
        }

        // TODO: other checks?

        if (messages.isEmpty()) {
            // no messages, all good
            txt_pattern.setText(R.string.mPttrn_ok);
            AndroidUtils.clearRoundedColor(txt_pattern);
        } else {
            // messages to show, concatenate them
            txt_pattern.setText("");
            boolean newline = false;
            for (String message : messages) {
                if (newline) txt_pattern.append("\n");
                newline = true;
                txt_pattern.append(message);
            }
            AndroidUtils.setRoundedColor(R.color.warning, txt_pattern, getActivity());
        }
        ClickableLinks.linkify(txt_pattern, this);
    }

    @Override
    public void onLinkClick(String tag) {
        switch (tag) {
            case "http":
                // replace http with https
                setUrl(getUrl().replaceFirst("^http:", "https:"));
                break;
        }
    }
}
