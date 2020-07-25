package com.trianguloy.urlchecker.modules;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This module checks for non-ascii characters in the url
 */
public class AsciiModule extends BaseModule {

    private TextView txt_ascii;

    @Override
    public String getName() {
        return "Ascii checker";
    }

    @Override
    public int getLayoutBase() {
        return R.layout.module_ascii;
    }

    @Override
    public void initialize(View views) {
        txt_ascii = views.findViewById(R.id.ascii);
    }

    @Override
    public void onNewUrl(String url) {
        List<String> messages = new ArrayList<>();

        // check for non-ascii characters
        if (!url.matches("\\A\\p{ASCII}*\\z")) {
            messages.add("Warning! Non ascii characters found");
        }

        // TODO: other checks?

        if (messages.isEmpty()) {
            // no messages, all good
            txt_ascii.setText("Good url");
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
