package com.trianguloy.urlchecker.modules.list;

import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.modules.companions.PatternCatalog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.Inflater;
import com.trianguloy.urlchecker.utilities.JavaUtilities;

import org.json.JSONObject;

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

class PatternDialog extends AModuleDialog implements View.OnClickListener {

    private TextView txt_pattern;
    private LinearLayout box;

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
        box = views.findViewById(R.id.box);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        List<Pair<String, String>> messages = new ArrayList<>();
        String url = urlData.url;

        // for each pattern
        JSONObject patterns = PatternCatalog.getBuiltIn(getActivity());
        for (String pattern : JavaUtilities.toList(patterns.keys())) {
            JSONObject data = patterns.optJSONObject(pattern);
            if (data == null) continue;
            if (!data.optBoolean("enabled")) continue;
            String regex = data.optString("regex", "(?!)");
            if (url.matches(regex)) {
                String replacement = data.has("replacement") ? data.optString("replacement") : null;
                if (replacement != null) {
                    replacement = url.replaceAll(regex, replacement);
                    if (data.optBoolean("automatic")) {
                        setUrl(replacement);
                        return;
                    }
                }
                messages.add(Pair.create(pattern, replacement));
            }
        }

        box.removeAllViews();
        if (messages.isEmpty()) {
            // no messages, all good
            txt_pattern.setVisibility(View.VISIBLE);
        } else {
            // messages to show, set them
            txt_pattern.setVisibility(View.GONE);

            for (Pair<String, String> pair : messages) {
                String label = pair.first;
                String newUrl = pair.second;
                View row = Inflater.inflate(R.layout.button_text, box, getActivity());

                // text
                TextView text = row.findViewById(R.id.text);
                text.setText(label);
                AndroidUtils.setRoundedColor(R.color.warning, text, getActivity());

                // button
                Button fix = row.findViewById(R.id.button);
                fix.setText(R.string.mPttrn_fix);
                fix.setEnabled(newUrl != null);
                fix.setTag(newUrl); // will set this when clicked
                fix.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag != null) setUrl(tag.toString());
    }
}
