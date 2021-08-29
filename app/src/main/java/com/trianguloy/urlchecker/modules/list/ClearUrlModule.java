package com.trianguloy.urlchecker.modules.list;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.utilities.GenericPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClearUrlModule extends AModuleData {

    public static GenericPref.Bool REFERRAL_PREF() {
        return new GenericPref.Bool("clearurl_referral", false);
    }

    public static GenericPref.Bool VERBOSE_PREF() {
        return new GenericPref.Bool("clearurl_verbose", false);
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

    public ClearUrlConfig(ConfigActivity activity) {
        super(activity);
        referralPref.init(activity);
        verbosePref.init(activity);
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
        CheckBox referral = views.findViewById(R.id.referral);
        referral.setChecked(referralPref.get());
        referral.setOnCheckedChangeListener((buttonView, isChecked) -> referralPref.set(isChecked));

        CheckBox verbose = views.findViewById(R.id.verbose);
        verbose.setChecked(verbosePref.get());
        verbose.setOnCheckedChangeListener((buttonView, isChecked) -> verbosePref.set(isChecked));
    }
}

class ClearUrlDialog extends AModuleDialog implements View.OnClickListener {

    private final GenericPref.Bool allowReferral = ClearUrlModule.REFERRAL_PREF();
    private final GenericPref.Bool verbose = ClearUrlModule.VERBOSE_PREF();

    private JSONObject data = null;
    private TextView info;
    private Button fix;

    private String cleared = null;

    public ClearUrlDialog(MainDialog dialog) {
        super(dialog);
        try {
            data = new JSONObject(getJsonFromAssets(dialog, "data.minify.json")).getJSONObject("providers");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        allowReferral.init(dialog);
        verbose.init(dialog);
    }


    @Override
    public int getLayoutId() {
        return R.layout.dialog_clearurl;
    }

    @Override
    public void onInitialize(View views) {
        info = views.findViewById(R.id.text);
        fix = views.findViewById(R.id.fix);
        fix.setOnClickListener(this);
    }

    @Override
    public void onNewUrl(String url) {
        info.setText("");
        cleared = url;
        fix.setEnabled(false);
        setColor(R.color.transparent);

        try {
            Iterator<String> providers = data.keys();

            whileProvider:
            while (providers.hasNext()) {
                String provider = providers.next();
                JSONObject providerData = data.getJSONObject(provider);
                if (!Pattern.compile(providerData.getString("urlPattern")).matcher(cleared).find()) {
                    continue;
                }

                // info
                if (verbose.get())
                    append(R.string.mClear_matches, provider);

                // check blocked completeProvider
                if (providerData.optBoolean("completeProvider", false)) {
                    append(R.string.mClear_blocked);
                    setColor(R.color.bad);
                    continue;
                }

                // check exceptions
                if (providerData.has("exceptions")) {
                    JSONArray exceptions = providerData.getJSONArray("exceptions");
                    for (int i = 0; i < exceptions.length(); i++) {
                        String exception = exceptions.getString(i);
                        if (Pattern.compile(exception).matcher(cleared).find()) {
                            if (verbose.get())
                                append(R.string.mClear_exception);
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
                        if (matcher.find()) {
                            if (providerData.optBoolean("forceRedirection", false)) {
                                // maybe do something special?
                                append(R.string.mClear_forcedRedirection);
                            } else {
                                append(R.string.mClear_redirection);
                            }
                            cleared = URLDecoder.decode(matcher.group(1));
                            setColor(R.color.warning);
                            continue whileProvider;
                        }
                    }
                }

                // apply rules
                if (providerData.has("rules")) {
                    JSONArray rules = providerData.getJSONArray("rules");
                    for (int i = 0; i < rules.length(); i++) {
                        String rule = "(?:&amp;|[/?#&])(?:" + rules.getString(i) + "=[^&]*)";
                        Matcher matcher = Pattern.compile(rule).matcher(cleared);
                        if (matcher.find()) {
                            cleared = matcher.replaceAll("");
                            append(R.string.mClear_rule);
                            setColor(R.color.warning);
                        }
                    }
                }

                // apply rawRules
                if (providerData.has("rawRules")) {
                    JSONArray rawRules = providerData.getJSONArray("rawRules");
                    for (int i = 0; i < rawRules.length(); i++) {
                        String rawRule = rawRules.getString(i);
                        Matcher matcher = Pattern.compile(rawRule).matcher(cleared);
                        if (matcher.find()) {
                            cleared = matcher.replaceAll("");
                            append(R.string.mClear_rawRule);
                            setColor(R.color.warning);
                        }
                    }
                }

                // apply referral rules
                if (!allowReferral.get() && providerData.has("referralMarketing")) {
                    JSONArray referrals = providerData.getJSONArray("referralMarketing");
                    for (int i = 0; i < referrals.length(); i++) {
                        String referral = "(?:&amp;|[/?#&])(?:" + referrals.getString(i) + "=[^&]*)";
                        Matcher matcher = Pattern.compile(referral).matcher(cleared);
                        if (matcher.find()) {
                            cleared = matcher.replaceAll("");
                            append(R.string.mClear_referral);
                            setColor(R.color.warning);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            append(R.string.mClear_error);
        }

        // url changed, enable button
        if (!cleared.equals(url)) {
            fix.setEnabled(true);
        }

        // nothing found
        if (info.getText().length() == 0) {
            info.setText(R.string.mClear_noRules);
        }
    }

    /**
     * Utility to append a line to the info textview, manages linebreaks
     */
    private void append(int line, Object... formatArgs) {
        if (info.getText().length() != 0) info.append("\n");
        info.append(getActivity().getString(line, formatArgs));
    }

    /**
     * Utility to set the info background color. Manages color importance
     */
    private void setColor(int color) {
        if (info.getTag() != null && info.getTag().equals(R.color.bad) && color == R.color.warning) return; // keep bad instead of replacing with warning
        info.setTag(color);
        info.setBackgroundColor(getActivity().getResources().getColor(color));
    }

    @Override
    public void onClick(View v) {
        // pressed the fix button
        if (cleared != null) setUrl(cleared);
    }

    /**
     * Reads a JSON file and returns its content
     * From https://www.bezkoder.com/java-android-read-json-file-assets-gson/
     */
    static String getJsonFromAssets(Context context, String fileName) throws IOException {
        String jsonString;
        InputStream is = context.getAssets().open(fileName);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        jsonString = new String(buffer, "UTF-8");

        return jsonString;
    }

}
