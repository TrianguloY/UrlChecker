package com.trianguloy.urlchecker.modules.list;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.GenericPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This module clears the url using the ClearUrl database (an asset copy)
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

    static GenericPref.Str DATABASE_URL() {
        return new GenericPref.Str("clearurl_databaseURL", "https://rules2.clearurls.xyz/data.minify.json");
    }
    static GenericPref.Str HASH_URL() {
        return new GenericPref.Str("clearurl_hashURL", "https://rules2.clearurls.xyz/rules.minify.hash");
    }

    public static GenericPref.Bool HASH_PREF() {
        return new GenericPref.Bool("clearurl_hash", true);
    }

    public static GenericPref.Bool CUSTOM_PREF() {
        return new GenericPref.Bool("clearurl_custom", false);
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
    final GenericPref.Str databaseURL = ClearUrlModule.DATABASE_URL();
    final GenericPref.Str hashURL = ClearUrlModule.HASH_URL();
    private final GenericPref.Bool hashPref = ClearUrlModule.HASH_PREF();
    private final GenericPref.Bool customPref = ClearUrlModule.CUSTOM_PREF();
    private Button update;
    private volatile boolean downloading = false;

    public ClearUrlConfig(ConfigActivity activity) {
        super(activity);
        referralPref.init(activity);
        verbosePref.init(activity);
        autoPref.init(activity);
        hashPref.init(activity);
        databaseURL.init(activity);
        hashURL.init(activity);
        customPref.init(activity);
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
        attach(views, R.id.referral, referralPref);
        attach(views, R.id.verbose, verbosePref);
        attach(views, R.id.auto, autoPref);
        attach(views, R.id.checkHash, hashPref);
        attach(views, R.id.customDB, customPref);

        textEditor(R.id.database_URL, databaseURL, views);
        textEditor(R.id.hash_URL, hashURL, views);

        update = views.findViewById(R.id.update);
        update.setOnClickListener(v -> {
            if (!downloading) {
                downloading = true;
                new Thread(() -> {
                    replaceDatabase(views.getContext().getString(R.string.mClear_database),
                            databaseURL.get(), hashURL.get(),  hashPref.get(), views.getContext());
                }).start();
            }
        });
    }

    /**
     * Replaces the database with a new one
     */
    private void replaceDatabase(String fileName, String databaseSource, String hashSource, boolean checkHash, Context context){
        // In case something fails, which can be: file writing, url reading, file download
        int retries = 5;
        int seconds = 3;
        for (int i = 1; i <= retries; i++) {
            try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
                // download json
                String jsonString = readFromUrl(databaseSource);
                JSONObject sourceJson = new JSONObject(jsonString);
                // sha256 checking
                if (checkHash) {
                    // TODO ? if the hash is the same as the downloaded file, there is no need to download the database
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(jsonString.getBytes(Charset.forName("UTF-8")));
                    String encoded = encodeHex(md.digest());
                    // Hash from ClearURLs has a newline character at the end, we trim it
                    String hash = readFromUrl(hashSource).trim();
                    if (!hash.equals(encoded)){
                        throw new Exception("Hash does not match");
                    }
                }

                // FIXME If the json is faulty the retry loop will keep trying in vain until all retries have finished
                // This checks that the database structure is correct, specification:
                // https://docs.clearurls.xyz/1.23.0/specs/rules/#dataminjson-catalog
                JSONObject data = sourceJson.getJSONObject("providers");
                Iterator<String> providers = data.keys();
                while (providers.hasNext()) {
                    // evaluate each provider
                    String provider = providers.next();
                    JSONObject providerData = data.getJSONObject(provider);
                    providerData.getString("urlPattern");
                    // At the time of writing the docs state that 'completeProvider' is required,
                    // however if we check the ClearURLs database we can see that it is missing
                    // in almost all providers (11/177), so we don't check it
                }
                // store json
                fos.write(jsonString.getBytes(Charset.forName("UTF-8")));
                break;
            } catch (Exception e){
                if (i == retries) {
                    e.printStackTrace();
                }
            }
            // delay between retries
            try {
                TimeUnit.SECONDS.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // TODO inform user if it succeeded or not and why
        downloading = false;
    }

    /**
     * from https://gist.github.com/avilches/750151
     */
    private static String encodeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte byt : bytes) sb.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return sb.toString();
    }

    /**
     * from https://stackoverflow.com/a/4308662
     */
    public static String readFromUrl(String url) throws IOException {

        try (InputStream is = new URL(url).openStream();) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            return readAll(rd);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * Initializes a config from a checkbox view
     */
    private void attach(View views, int viewId, GenericPref.Bool config) {
        CheckBox chxbx = views.findViewById(viewId);
        chxbx.setChecked(config.get());
        chxbx.setOnCheckedChangeListener((buttonView, isChecked) -> config.set(isChecked));
    }

    /**
     * Initializes a string from a EditText view, also watches for changes
     */
    private void textEditor(int id, GenericPref.Str strPref, View views){
        EditText txt = (EditText) views.findViewById(id);
        txt.setText(strPref.get());
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                strPref.set(s.toString());
                if (!canBeEnabled()) disable();
            }
        };
        txt.addTextChangedListener(textWatcher);
    }
}

class ClearUrlDialog extends AModuleDialog implements View.OnClickListener {

    public static final String CLEARED = "cleared";

    private final GenericPref.Bool allowReferral = ClearUrlModule.REFERRAL_PREF();
    private final GenericPref.Bool verbose = ClearUrlModule.VERBOSE_PREF();
    private final GenericPref.Bool auto = ClearUrlModule.AUTO_PREF();
    private final GenericPref.Bool custom = ClearUrlModule.CUSTOM_PREF();

    private JSONObject data = null;
    private TextView info;
    private Button fix;

    private String cleared = null;

    public ClearUrlDialog(MainDialog dialog) {
        super(dialog);
        allowReferral.init(dialog);
        verbose.init(dialog);
        auto.init(dialog);
        custom.init(dialog);

        if (custom.get()) {
            // use custom database
            try {
                // TODO fall back if file is downloading?
                data = new JSONObject(getJsonFromStorage(dialog, getActivity().getString(R.string.mClear_database))).getJSONObject("providers");
            } catch (Exception ignore) {
                // custom database load failed, falling back to built-in database
                Toast.makeText(dialog, dialog.getString(R.string.mClear_customDBLoadError), Toast.LENGTH_LONG).show();
                try {
                    data = new JSONObject(getJsonFromAssets(dialog, getActivity().getString(R.string.mClear_database))).getJSONObject("providers");
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            try {
                data = new JSONObject(getJsonFromAssets(dialog, getActivity().getString(R.string.mClear_database))).getJSONObject("providers");
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

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

        try {
            Iterator<String> providers = data.keys();

            whileProvider:
            while (providers.hasNext()) {
                // evaluate each provider
                String provider = providers.next();
                JSONObject providerData = data.getJSONObject(provider);
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

                // fix empty elements
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

            }
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            append(R.string.mClear_error);
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
     * Matches cas insnsitive regexp into an input
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
        if (info.getTag() != null && info.getTag().equals(R.color.bad) && color == R.color.warning) return; // keep bad instead of replacing with warning
        info.setTag(color);
        AndroidUtils.setRoundedColor(color, info, getActivity());
    }

    /**
     * Reads a file from assets and returns its content
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

    /**
     * Reads a file from internal storage and returns its content
     */
    static String getJsonFromStorage(Context context, String fileName) throws IOException {
        String jsonString;
        FileInputStream fis = context.openFileInput(fileName);

        int size = fis.available();
        byte[] buffer = new byte[size];
        fis.read(buffer);
        fis.close();

        jsonString = new String(buffer, "UTF-8");

        return jsonString;
    }

}