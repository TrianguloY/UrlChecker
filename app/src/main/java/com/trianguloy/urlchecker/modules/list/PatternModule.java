package com.trianguloy.urlchecker.modules.list;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.companions.PatternCatalog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.Inflater;
import com.trianguloy.urlchecker.utilities.JavaUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new PatternConfig(cntx);
    }
}

class PatternConfig extends AModuleConfig {

    private final PatternCatalog catalog;

    public PatternConfig(ModulesActivity cntx) {
        super(cntx);
        catalog = new PatternCatalog(cntx);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_patterns;
    }

    @Override
    public void onInitialize(View views) {
        views.findViewById(R.id.edit).setOnClickListener(v -> catalog.showEditor());
        views.<TextView>findViewById(R.id.user_content)
                .setText(getActivity().getString(
                        R.string.mPttrn_userContent,
                        "https://github.com/TrianguloY/UrlChecker/wiki/Custom-patterns"
                ));
    }

}

class PatternDialog extends AModuleDialog {
    public static final String APPLIED = "pattern.applied";

    private TextView txt_noPatterns;
    private LinearLayout box;

    private final PatternCatalog catalog;

    private final List<Message> messages = new ArrayList<>();

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
    public void onModifyUrl(UrlData urlData, JavaUtils.Function<UrlData, Boolean> setNewUrl) {
        // init
        messages.clear();
        String url = urlData.url;

        // check each pattern
        JSONObject patterns = catalog.getCatalog();
        for (String pattern : JavaUtils.toList(patterns.keys())) {
            try {
                JSONObject data = patterns.optJSONObject(pattern);
                Message message = new Message(pattern);

                // enabled?
                if (data == null) continue;
                if (!data.optBoolean("enabled", true)) continue;

                // get regex (must exists)
                if (!data.has("regex")) continue;
                Pattern regex = Pattern.compile(data.getString("regex"));

                // applied?
                message.applied = urlData.getData(APPLIED + pattern) != null;

                // check matches
                // if 'regexp' matches, the pattern can match
                // if 'regexp' doesn't match, the patter doesn't match
                var matches = regex.matcher(url).find();
                if (matches && data.has("excludeRegex")) {
                    // if 'excludeRegex' doesn't exist, the pattern can match
                    // if 'excludeRegex' matches, the pattern doesn't matches
                    // if 'excludeRegex' doesn't match, the pattern can match
                    matches = !Pattern.compile(data.getString("excludeRegex")).matcher(url).find();
                }
                if (matches) {
                    message.matches = true;

                    // check replacements
                    String replacement = null;

                    Object replacements = data.opt("replacement");
                    if (replacements != null) {
                        // data exists
                        if (replacements instanceof JSONArray) {
                            // array, get random
                            JSONArray replacementsArray = (JSONArray) replacements;
                            replacement = replacementsArray.getString(new Random().nextInt(replacementsArray.length()));
                        } else {
                            // single data, get that one
                            replacement = replacements.toString();
                        }
                    }

                    if (replacement != null) {
                        // replace url
                        message.newUrl = replaceAll(url, regex, replacement);

                        // automatic? apply
                        if (data.optBoolean("automatic")) {
                            if (setNewUrl.apply(new UrlData(message.newUrl).putData(APPLIED + pattern, APPLIED))) return;
                        }
                    }
                }


                // add
                if (message.applied || message.matches) messages.add(message);

            } catch (Exception e) {
                // invalid pattern? ignore
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        // visualize
        box.removeAllViews();
        if (messages.isEmpty()) {
            // no messages, all good
            txt_noPatterns.setVisibility(View.VISIBLE);
            setVisibility(false);
        } else {
            // messages to show, set them
            txt_noPatterns.setVisibility(View.GONE);
            setVisibility(true);

            for (Message message : messages) {
                // either matches and/or applied is true
                View row = Inflater.inflate(R.layout.button_text, box);

                // text
                TextView text = row.findViewById(R.id.text);
                text.setText(message.applied
                        ? getActivity().getString(R.string.mPttrn_fixed, message.pattern)
                        : message.pattern
                );
                AndroidUtils.setRoundedColor(message.matches ? R.color.warning : R.color.good, text);

                // button
                Button fix = row.findViewById(R.id.button);
                fix.setText(R.string.mPttrn_fix);
                fix.setEnabled(message.newUrl != null);
                if (message.newUrl != null) fix.setOnClickListener(v -> setUrl(new UrlData(message.newUrl).putData(APPLIED + message.pattern, APPLIED)));
            }
        }
    }

    /**
     * DataClass for pattern messages
     */
    private static class Message {
        final String pattern;
        boolean applied;
        public boolean matches;
        String newUrl;

        public Message(String pattern) {
            this.pattern = pattern;
        }
    }

    /**
     * On Android 10 and under, optional groups may yield a "null" in the replacement output instead of an empty string.
     * Therefore, we just copy the implementation from newer versions
     * https://github.com/TrianguloY/UrlChecker/issues/237
     */
    private static String replaceAll(String text, Pattern pattern, String replacement) {
        // Copied from https://android.googlesource.com/platform/libcore/+/refs/heads/android13-release/ojluni/src/main/java/java/util/regex/Matcher.java#837
        Matcher matcher = pattern.matcher(text);
        boolean result = matcher.find();
        if (result) {
            StringBuffer sb = new StringBuffer();
            int appendPos = 0;
            do {
                appendPos = appendReplacement(text, matcher, sb, replacement, appendPos);
                result = matcher.find();
            } while (result);
            appendTail(text, sb, appendPos);
            return sb.toString();
        }
        return text.toString();
    }

    private static int appendReplacement(String text, Matcher matcher, StringBuffer sb, String replacement, int appendPos) {
        // Copied from https://android.googlesource.com/platform/libcore/+/refs/heads/android13-release/ojluni/src/main/java/java/util/regex/Matcher.java#714
        sb.append(text.substring(appendPos, matcher.start()));
        appendEvaluated(matcher, sb, replacement);
        appendPos = matcher.end();

        return appendPos;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void appendEvaluated(Matcher matcher, StringBuffer buffer, String s) {
        // Copied from https://android.googlesource.com/platform/libcore/+/refs/heads/android13-release/ojluni/src/main/java/java/util/regex/Matcher.java#731
        boolean escape = false;
        boolean dollar = false;
        boolean escapeNamedGroup = false;
        int escapeNamedGroupStart = -1;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
            } else if (c == '$' && !escape) {
                dollar = true;
            } else if (c >= '0' && c <= '9' && dollar && !escapeNamedGroup) {
                String groupValue = matcher.group(c - '0');
                if (groupValue != null) {
                    buffer.append(groupValue);
                }
                dollar = false;
            } else if (c == '{' && dollar) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    throw new IllegalArgumentException("your android version does not support named-capturing groups");
                }
                escapeNamedGroup = true;
                escapeNamedGroupStart = i;
            } else if (c == '}' && dollar && escapeNamedGroup) {
                String groupValue = matcher.group(s.substring(escapeNamedGroupStart + 1, i));
                if (groupValue != null) {
                    buffer.append(groupValue);
                }
                dollar = false;
                escapeNamedGroup = false;
            } else if (c != '}' && dollar && escapeNamedGroup) {
                continue;
            } else {
                buffer.append(c);
                dollar = false;
                escape = false;
                escapeNamedGroup = false;
            }
        }

        if (escape) {
            throw new IllegalArgumentException("character to be escaped is missing");
        }

        if (dollar) {
            throw new IllegalArgumentException("Illegal group reference: group index is missing");
        }

        if (escapeNamedGroup) {
            throw new IllegalArgumentException("Missing ending brace '}' from replacement string");
        }
    }

    private static StringBuffer appendTail(String text, StringBuffer sb, int appendPos) {
        // Copied from https://android.googlesource.com/platform/libcore/+/refs/heads/android13-release/ojluni/src/main/java/java/util/regex/Matcher.java#796
        int to = text.length();
        if (appendPos < to) {
            sb.append(text.substring(appendPos, to));
        }
        return sb;
    }
}
