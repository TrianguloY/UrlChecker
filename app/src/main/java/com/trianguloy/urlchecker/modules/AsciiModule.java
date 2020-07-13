package com.trianguloy.urlchecker.modules;

import android.view.View;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;

import java.util.ArrayList;
import java.util.List;

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

        if (!url.matches("\\A\\p{ASCII}*\\z")) {
            messages.add("Warning! Non ascii characters found");
        }


        if(messages.isEmpty()){
            txt_ascii.setText("Good url");
        }else {
            txt_ascii.setText("");
            boolean newline = false;
            for (String message : messages) {
                if(newline) txt_ascii.append("\n");
                newline = true;
                txt_ascii.append(message);
            }
        }
    }
}
