package com.trianguloy.urlchecker.modules.list;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * This module checks for non-ascii characters in the url
 */
public class AsciiModule extends AModuleData {

    @Override
    public String getId() {
        return "ascii";
    }

    @Override
    public int getName() {
        return R.string.mAscii_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new AsciiDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.mAscii_desc);
    }
}

class AsciiDialog extends AModuleDialog {

    private TextView txt_ascii;

    public AsciiDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_ascii;
    }

    @Override
    public void onInitialize(View views) {
        txt_ascii = views.findViewById(R.id.ascii);
    }

    @Override
    public void onNewUrl(String url) {
        List<String> messages = new ArrayList<>();

        // check for non-ascii characters
        if (!url.matches("\\A\\p{ASCII}*\\z")) {
            messages.add(getActivity().getString(R.string.mAscii_warning));
        }

        // TODO: other checks?

        if (messages.isEmpty()) {
            // no messages, all good
            txt_ascii.setText(R.string.mAscii_ok);
            txt_ascii.setBackgroundColor(Color.TRANSPARENT);
        } else {
            // messages to show, concatenate them
            txt_ascii.setText("");
            boolean newline = false;
            for (String message : messages) {
                if (newline) txt_ascii.append("\n");
                newline = true;
                txt_ascii.append(message);
            }
            txt_ascii.setBackgroundColor(getActivity().getResources().getColor(R.color.warning));
        }
    }
}
