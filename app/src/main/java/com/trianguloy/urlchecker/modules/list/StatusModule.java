package com.trianguloy.urlchecker.modules.list;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.StreamUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A module that checks the page status code by performing a GET request
 * Allows checking for redirection
 */
public class StatusModule extends AModuleData {

    public static GenericPref.Bool AUTOREDIR_PREF(Context cntx) {
        return new GenericPref.Bool("statusCode_autoRedir", false, cntx);
    }

    public static GenericPref.Str AUTOCHECK_PREF(Context cntx) {
        return new GenericPref.Str("statusCode_autoCheck", "", cntx);
    }


    @Override
    public String getId() {
        return "statusCode";
    }

    @Override
    public int getName() {
        return R.string.mStatus_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new StatusDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new StatusConfig(cntx);
    }
}

class StatusConfig extends AModuleConfig {

    public StatusConfig(ModulesActivity cntx) {
        super(cntx);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_status;
    }

    @Override
    public void onInitialize(View views) {
        StatusModule.AUTOREDIR_PREF(getActivity()).attachToSwitch(views.findViewById(R.id.autoredirect));
        StatusModule.AUTOCHECK_PREF(getActivity()).attachToEditText(views.findViewById(R.id.autoCheck));
    }
}

class StatusDialog extends AModuleDialog {
    private static final String PREVIOUS = "redirected.redirected";


    private Button check;
    private TextView previous;
    private TextView info;
    private TextView redirect;

    private String redirectionUrl = null;
    private Thread thread = null;

    private GenericPref.Bool autoRedir;
    private GenericPref.Str autoCheck;

    public StatusDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_status;
    }

    @Override
    public void onInitialize(View views) {
        check = views.findViewById(R.id.check);
        check.setOnClickListener(v -> {
            AndroidUtils.setHideableText(previous, null);
            check();
        });

        previous = views.findViewById(R.id.previous);
        AndroidUtils.setRoundedColor(R.color.good, previous);

        info = views.findViewById(R.id.info);

        redirect = views.findViewById(R.id.redirect);
        redirect.setOnClickListener(v -> {
            // replace url
            if (redirectionUrl != null) {
                setUrl(redirectionUrl);
                redirectionUrl = null;
            }
        });

        autoRedir = StatusModule.AUTOREDIR_PREF(getActivity());
        autoCheck = StatusModule.AUTOCHECK_PREF(getActivity());
    }

    @Override
    public void onPrepareUrl(UrlData urlData) {
        // cancel previous check if pending
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        // reset all
        check.setEnabled(true);
        check.setText(R.string.mStatus_check);
        AndroidUtils.setHideableText(previous, urlData.getData(PREVIOUS));
        AndroidUtils.setHideableText(info, null);
        redirectionUrl = null;
        updateRedirect();

        if (!urlData.disableUpdates && urlData.url.matches(autoCheck.get())) {
            // autocheck
            check();
        }
    }

    /**
     * Starts the checking process
     */
    private void check() {
        // disable button
        check.setEnabled(false);
        check.setText(R.string.mStatus_recheck);
        AndroidUtils.setHideableText(info, getActivity().getString(R.string.mStatus_checking));
        redirectionUrl = null;
        updateRedirect();

        // check in background
        thread = new Thread(this::_check);
        thread.start();
    }

    /**
     * Checks a redirect, in background
     * https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
     */
    private void _check() {
        // get url
        var url = getUrl();
        Log.d("STATUS", "Checking: " + url);
        String message;

        HttpURLConnection conn = null;
        try {
            // perform GET to the url
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            conn.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
            var responseCode = conn.getResponseCode();
            Log.d("RESPONSE_CODE", url + ": " + responseCode);

            // prepare message
            message = null;
            var codesArray = getActivity().getResources().getStringArray(R.array.mStatus_codes);
            for (var s : codesArray) {
                if (s.startsWith(String.valueOf(responseCode))) {
                    // known status code
                    message = s;
                    break;
                }
            }
            if (message == null) {
                // unknown status code
                message = getActivity().getString(R.string.mStatus_unknownCode, responseCode);
            }

            // redirection
            var location = conn.getHeaderField("Location");
            if (location != null) {
                // this should be removed, the uri needs to be kept encoded
                // location = URLDecoder.decode(location, "UTF-8");
                redirectionUrl = new URL(new URL(url), location).toExternalForm(); // Deal with relative URLs
            }
        } catch (IOException e) {
            // io error
            e.printStackTrace();
            message = getActivity().getString(R.string.mStatus_ioerror, e.getMessage());
        } catch (Exception e) {
            // other error
            e.printStackTrace();
            message = getActivity().getString(R.string.mStatus_error, e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        // exit if was canceled
        if (Thread.currentThread().isInterrupted()) {
            Log.d("THREAD", "Interrupted");
            return;
        }

        // notify
        final var finalMessage = message;
        getActivity().runOnUiThread(() -> {
            info.setText(finalMessage);
            check.setEnabled(true);

            if (autoRedir.get() && redirectionUrl != null) {
                // autoredirect, replace url
                var previousMessage = previous.getText().toString() + (previous.length() == 0 ? "" : "\n") + "--> " + finalMessage;
                setUrl(new UrlData(redirectionUrl).putData(PREVIOUS, previousMessage));
                redirectionUrl = null;
            } else {
                updateRedirect();
            }

        });
    }

    /**
     * Updates the redirect textview based on the redirect variable
     */
    private void updateRedirect() {
        if (redirectionUrl == null) {
            AndroidUtils.setHideableText(redirect, null);
            return;
        }

        // this code sets the redirect text to "Redirects to _{redirectionUrl}_" with url underscored.
        // it does so by using a marker to underline exactly the parameter (wherever it is) and later replace it with the final url
        // all underlined looks bad, and auto-underline may not work with some malformed urls

        // "Redirects to %s" -> "Redirects to {marker}"
        var marker = "%S%";
        var string = getActivity().getString(R.string.mStatus_redir, marker);

        // "Redirects to {marker}" -> "Redirects to _{marker}_"
        var start = string.indexOf(marker);
        var end = start + marker.length();
        SpannableStringBuilder text = new SpannableStringBuilder(string);
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View ignored) {
                // do nothing, the whole view is clickable
            }
        }, start, end, 0);

        // "Redirects to _{marker}_" -> "Redirects to _{redirectionUrl}_"
        text.replace(start, end, redirectionUrl);

        AndroidUtils.setHideableText(redirect, text);
    }
}
