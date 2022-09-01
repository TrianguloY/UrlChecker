package com.trianguloy.urlchecker.modules.list;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.ClickableLinks;
import com.trianguloy.urlchecker.utilities.StreamUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * A module that checks the page status code by using a local browser
 * Allows checking for redirection
 */
public class StatusModule extends AModuleData {

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
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.mStatus_desc);
    }
}

class StatusDialog extends AModuleDialog implements View.OnClickListener, ClickableLinks.OnUrlListener {

    private Button check;
    private TextView info;

    private String redirectionUrl = null;

    public StatusDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.button_text;
    }

    @Override
    public void onInitialize(View views) {
        check = views.findViewById(R.id.button);
        check.setText(R.string.mStatus_check);
        check.setOnClickListener(this);
        info = views.findViewById(R.id.text);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        // reset all
        check.setEnabled(true);
        info.setText("");
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.check:
        check(); // only one button
//                break;
//        }
    }

    /**
     * Starts the checking process
     */
    private void check() {
        // disable button and run in background
        check.setEnabled(false);
        info.setText(R.string.mStatus_checking);

        new Thread(this::_check).start();
    }

    /**
     * Checks a redirect, in background
     * https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
     */
    private void _check() {
        // get url
        String url = getUrl();
        String message;

        HttpURLConnection conn = null;
        try {
            // perform GET to the url
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            conn.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
            int responseCode = conn.getResponseCode();
            Log.d("RESPONSE_CODE", url + ": " + responseCode);

            // prepare message
            message = null;
            String[] codesArray = getActivity().getResources().getStringArray(R.array.mStatus_codes);
            for (String s : codesArray) {
                if (s.startsWith(String.valueOf(responseCode))) {
                    // known message
                    message = s;
                    break;
                }
            }
            if (message == null) {
                // unknown message
                message = getActivity().getString(R.string.mStatus_unknownCode, responseCode);
            }

            // redirection
            String location = conn.getHeaderField("Location");
            if (location != null) {
                location = URLDecoder.decode(location, "UTF-8");
                redirectionUrl = new URL(new URL(url), location).toExternalForm(); // Deal with relative URLs
                message += " - [[" + getActivity().getString(R.string.mStatus_redir) + "]]";
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

        // notify
        final String finalMessage = message;
        getActivity().runOnUiThread(() -> {
            info.setText(finalMessage);
            ClickableLinks.linkify(info, StatusDialog.this);
            check.setEnabled(true);
        });
    }

    @Override
    public void onLinkClick(String tag) {
        // if(tag=="redir") // only redirection tag
        if (redirectionUrl != null) {
            setUrl(redirectionUrl);
        }
    }
}
