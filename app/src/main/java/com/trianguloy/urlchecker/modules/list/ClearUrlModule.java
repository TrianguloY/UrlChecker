package com.trianguloy.urlchecker.modules.list;

import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.companions.ClearUrlCatalog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.GenericPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This module clears the url using the ClearUrl catalog
 */
public class ClearUrlModule extends AModuleData {

    public static GenericPref.Bool REFERRAL_PREF() {
        return new GenericPref.Bool("clearurl_referral", false);
    }

    public static GenericPref.Bool VERBOSE_PREF() {
        return new GenericPref.Bool("clearurl_verbose", false);
    }

    public static GenericPref.Bool AUTO_PREF() {
        return new GenericPref.Bool("clearurl_auto", false);
    }

    @Override
    public String getId() {
        return "clearUrl";
    }

    @Override
    public int getName() {
        return R.string.mClear_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new ClearUrlDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new ClearUrlConfig(cntx);
    }
}

class ClearUrlConfig extends AModuleConfig {

    private final GenericPref.Bool referralPref = ClearUrlModule.REFERRAL_PREF();
    private final GenericPref.Bool verbosePref = ClearUrlModule.VERBOSE_PREF();
    private final GenericPref.Bool autoPref = ClearUrlModule.AUTO_PREF();

    private final ClearUrlCatalog catalog;

    public ClearUrlConfig(ConfigActivity activity) {
        super(activity);
        referralPref.init(activity);
        verbosePref.init(activity);
        autoPref.init(activity);
        catalog = new ClearUrlCatalog(activity);
    }

    @Override
    public boolean canBeEnabled() {
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_clearurls;
    }

    @Override
    public void onInitialize(View views) {
        referralPref.attachToCheckBox(views.findViewById(R.id.referral));
        verbosePref.attachToCheckBox(views.findViewById(R.id.verbose));
        autoPref.attachToCheckBox(views.findViewById(R.id.auto));

        views.findViewById(R.id.update).setOnClickListener(v -> catalog.showUpdater());
        views.findViewById(R.id.edit).setOnClickListener(v -> catalog.showEditor());
    }

}

class ClearUrlDialog extends AModuleDialog implements View.OnClickListener {

    public static final String CLEARED = "clearUrl.cleared";

    private final GenericPref.Bool allowReferral = ClearUrlModule.REFERRAL_PREF();
    private final GenericPref.Bool verbose = ClearUrlModule.VERBOSE_PREF();
    private final GenericPref.Bool auto = ClearUrlModule.AUTO_PREF();

    private final List<Pair<String, JSONObject>> data;
    private TextView info;
    private Button fix;

    private String cleared = null;

    public ClearUrlDialog(MainDialog dialog) {
        super(dialog);
        allowReferral.init(dialog);
        verbose.init(dialog);
        auto.init(dialog);

        data = ClearUrlCatalog.getRules(getActivity());
    }

    @Override
    public int getLayoutId() {
        return R.layout.button_text;
    }

    @Override
    public void onInitialize(View views) {
        info = views.findViewById(R.id.text);
        fix = views.findViewById(R.id.button);
        fix.setText(R.string.mClear_clear);
        fix.setOnClickListener(this);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        cleared = urlData.url;
        if (urlData.getData(CLEARED) != null) {
            // was cleared
            info.setText(R.string.mClear_cleared);
            setColor(R.color.good);
        } else {
            // was not cleared
            info.setText("");
            setColor(R.color.transparent);
        }
        fix.setEnabled(false);

        whileProvider:
        for (Pair<String, JSONObject> pair : data) {
            // evaluate each provider
            String provider = pair.first;
            JSONObject providerData = pair.second;
            try {
                if (!matcher(providerData.getString("urlPattern"), cleared).find()) {
                    continue;
                }

                // info
                if (verbose.get()) append(R.string.mClear_matches, provider);

                // check blocked completeProvider
                if (providerData.optBoolean("completeProvider", false)) {
                    // provider blocked
                    append(R.string.mClear_blocked);
                    setColor(R.color.bad);
                    continue;
                }

                // check exceptions
                if (providerData.has("exceptions")) {
                    JSONArray exceptions = providerData.getJSONArray("exceptions");
                    for (int i = 0; i < exceptions.length(); i++) {
                        String exception = exceptions.getString(i);
                        if (matcher(exception, cleared).find()) {
                            // exception matches, ignore provider
                            if (verbose.get()) append(R.string.mClear_exception, exception);
                            continue whileProvider;
                        }
                    }
                }

                // apply redirections
                if (providerData.has("redirections")) {
                    JSONArray redirections = providerData.getJSONArray("redirections");
                    for (int i = 0; i < redirections.length(); i++) {
                        String redirection = redirections.getString(i);
                        Matcher matcher = Pattern.compile(redirection).matcher(cleared);
                        if (matcher.find() && matcher.groupCount() >= 1) {
                            // redirection found
                            if (providerData.optBoolean("forceRedirection", false)) {
                                // maybe do something special?
                                append(R.string.mClear_forcedRedirection);
                            } else {
                                append(R.string.mClear_redirection);
                            }
                            if (verbose.get()) details(redirection);
                            cleared = decodeURIComponent(matcher.group(1)); // can't be null, checked in the if
                            setColor(R.color.warning);
                            continue whileProvider;
                        }
                    }
                }

                // apply rawRules
                if (providerData.has("rawRules")) {
                    JSONArray rawRules = providerData.getJSONArray("rawRules");
                    for (int i = 0; i < rawRules.length(); i++) {
                        String rawRule = rawRules.getString(i);
                        Matcher matcher = matcher(rawRule, cleared);
                        if (matcher.find()) {
                            // rawrule matches, apply
                            cleared = matcher.replaceAll("");
                            append(R.string.mClear_rawRule);
                            if (verbose.get()) details(rawRule);
                            setColor(R.color.warning);
                        }
                    }
                }

                // apply rules
                if (providerData.has("rules")) {
                    JSONArray rules = providerData.getJSONArray("rules");
                    for (int i = 0; i < rules.length(); i++) {
                        String rule = "([?&#])" + rules.getString(i) + "=[^&#]*";
                        Matcher matcher = matcher(rule, cleared);
                        while (matcher.find()) {
                            // rule applies
                            cleared = matcher.replaceFirst("$1");
                            matcher.reset(cleared);
                            append(R.string.mClear_rule);
                            if (verbose.get()) details(rules.getString(i));
                            setColor(R.color.warning);
                        }
                    }
                }

                // apply referral rules
                if (!allowReferral.get() && providerData.has("referralMarketing")) {
                    JSONArray referrals = providerData.getJSONArray("referralMarketing");
                    for (int i = 0; i < referrals.length(); i++) {
                        String referral = "([?&#])" + referrals.getString(i) + "=[^&#]*";
                        Matcher matcher = matcher(referral, cleared);
                        while (matcher.find()) {
                            // raw rule applies
                            cleared = matcher.replaceFirst("$1");
                            matcher.reset(cleared);
                            append(R.string.mClear_referral);
                            if (verbose.get()) details(referrals.getString(i));
                            setColor(R.color.warning);
                        }
                    }
                }

                // if changed, fix cleaning artifacts
                if (!cleared.equals(urlData.url)) {

                    // remove empty elements
                    cleared = cleared
                            .replaceAll("\\?&+", "?")
                            .replaceAll("\\?#", "#")
                            .replaceAll("\\?$", "")
                            .replaceAll("&&+", "&")
                            .replaceAll("&#", "#")
                            .replaceAll("&$", "")
                            .replaceAll("#&+", "#")
                            .replaceAll("#$", "")
                    ;

                    // restore missing domain
                    if (!cleared.matches("^https?://.*")) {
                        cleared = "http://" + cleared;
                    }
                }
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
                if (verbose.get()) {
                    append(R.string.mClear_error);
                    details(provider);
                }
            }
        }

        // url changed, enable button
        if (!cleared.equals(urlData.url)) {
            fix.setEnabled(true);
            if (verbose.get()) info.append("\n\n -> " + cleared);
            // and apply automatically if required
            if (auto.get()) onClick(null);
        }

        // nothing found
        if (info.getText().length() == 0) {
            info.setText(R.string.mClear_noRules);
        }
    }

    @Override
    public void onClick(View v) {
        // pressed the fix button
        if (cleared != null) setUrl(new UrlData(cleared).putData(CLEARED, CLEARED));
    }

    // ------------------- utils -------------------

    /**
     * Hopefully the same as javascript's decodeURIComponent
     * Idea from https://stackoverflow.com/a/6926987, but using own implementation
     */
    private static String decodeURIComponent(String text) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        String[] parts = text.split("\\+");
        for (String part : parts) {
            if (result.length() != 0) result.append('+');
            result.append(URLDecoder.decode(part, "UTF-8"));
        }
        return result.toString();
    }

    /**
     * Matches case insensitive regexp into an input
     *
     * @param regexp regexp to use
     * @param input  input to use
     * @return the matcher object
     */
    private static Matcher matcher(String regexp, String input) {
        return Pattern.compile(regexp, Pattern.CASE_INSENSITIVE).matcher(input);
    }

    /**
     * Utility to append a line to the info textview, manages linebreaks
     */
    private void append(int line, Object... formatArgs) {
        if (info.getText().length() != 0) info.append("\n");
        info.append(getActivity().getString(line, formatArgs));
    }

    /**
     * utility to append data to the info textview, after calling append
     */
    private void details(String data) {
        info.append(": " + data);
    }

    /**
     * Utility to set the info background color. Manages color importance
     */
    private void setColor(int color) {
        if (info.getTag() != null && info.getTag().equals(R.color.bad) && color == R.color.warning)
            return; // keep bad instead of replacing with warning
        info.setTag(color);
        AndroidUtils.setRoundedColor(color, info, getActivity());
    }

}
