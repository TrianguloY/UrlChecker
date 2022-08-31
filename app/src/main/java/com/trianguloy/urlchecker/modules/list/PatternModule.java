package com.trianguloy.urlchecker.modules.list;

import android.app.Activity;
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
        return new PatternConfig(cntx);
    }
}

class PatternConfig extends AModuleConfig implements View.OnClickListener {

    private final PatternCatalog catalog;

    public PatternConfig(Activity cntx) {
        catalog = new PatternCatalog(cntx);
    }

    @Override
    public boolean canBeEnabled() {
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_patterns;
    }

    @Override
    public void onInitialize(View views) {
        views.findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        catalog.showEditor();
    }
}

class PatternDialog extends AModuleDialog implements View.OnClickListener {
    public static final String APPLIED = "pattern.applied";

    private TextView txt_noPatterns;
    private LinearLayout box;

    private final PatternCatalog catalog;

    public PatternDialog(MainDialog dialog) {
        super(dialog);
        catalog = new PatternCatalog(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_pattern;
    }

    @Override
    public void onInitialize(View views) {
        txt_noPatterns = views.findViewById(R.id.pattern);
        box = views.findViewById(R.id.box);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        // init
        List<Message> messages = new ArrayList<>();
        String url = urlData.url;

        // check each pattern
        JSONObject patterns = catalog.getCatalog();
        for (String pattern : JavaUtilities.toList(patterns.keys())) {
            try {
                JSONObject data = patterns.optJSONObject(pattern);
                Message message = new Message(pattern);

                // enabled?
                if (data == null) continue;
                if (!data.optBoolean("enabled", true)) continue;

                // applied?
                message.applied = urlData.getData(APPLIED + pattern) != null;

                // check regexp
                String regex = data.optString("regex", "(?!)");
                if (url.matches(".*" + regex + ".*")) {
                    message.matches = true;
                    // check replacements
                    String replacement = data.has("replacement") ? data.optString("replacement") : null;
                    if (replacement != null) {
                        message.newUrl = url.replaceAll(regex, replacement);
                    }
                }

                // automatic?
                message.automatic = data.optBoolean("automatic");

                // add
                if (message.applied || message.matches) messages.add(message);

            } catch (Exception e) {
                // invalid pattern? ignore
                e.printStackTrace();
            }
        }

        // visualize
        box.removeAllViews();
        if (messages.isEmpty()) {
            // no messages, all good
            txt_noPatterns.setVisibility(View.VISIBLE);
        } else {
            // messages to show, set them
            txt_noPatterns.setVisibility(View.GONE);

            for (Message message : messages) {
                // either matches and/or applied is true
                View row = Inflater.inflate(R.layout.button_text, box, getActivity());

                // text
                TextView text = row.findViewById(R.id.text);
                text.setText(message.applied
                        ? getActivity().getString(R.string.mPttrn_fixed, message.pattern)
                        : message.pattern
                );
                AndroidUtils.setRoundedColor(message.matches ? R.color.warning : R.color.good, text, getActivity());

                // button
                Button fix = row.findViewById(R.id.button);
                fix.setText(R.string.mPttrn_fix);
                fix.setEnabled(message.newUrl != null);
                fix.setTag(new String[]{message.pattern, message.newUrl}); // data for the onCLick
                fix.setOnClickListener(this);

                // autoclick
                if (message.automatic) onClick(message.pattern, message.newUrl);
            }
        }
    }

    @Override
    public void onClick(View view) {
        String[] tag = (String[]) view.getTag();
        if (tag != null) onClick(tag[0], tag[1]);
    }

    /**
     * applies a pattern
     */
    private void onClick(String pattern, String url) {
        if (url != null) setUrl(new UrlData(url).putData(APPLIED + pattern, APPLIED));
    }

    /**
     * DataClass for pattern messages
     */
    private static class Message {
        final String pattern;
        public boolean automatic;
        boolean applied;
        public boolean matches;
        String newUrl;

        public Message(String pattern) {
            this.pattern = pattern;
        }
    }
}
