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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * A module that allows checking for redirection by using a local browser
 * TODO: show also and maybe react to other codes
 */
public class RedirectModule extends AModuleData {

    @Override
    public String getId() {
        return "redirect";
    }

    @Override
    public int getName() {
        return R.string.mRedir_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new RedirectDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.mRedir_desc);
    }
}

class RedirectDialog extends AModuleDialog implements View.OnClickListener {

    private Button check;
    private TextView info;

    public RedirectDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_redirect;
    }

    @Override
    public void onInitialize(View views) {
        check = views.findViewById(R.id.check);
        check.setOnClickListener(this);
        info = views.findViewById(R.id.info);
    }

    @Override
    public void onNewUrl(String url) {
        // reset all
        check.setEnabled(true);
        info.setText("");
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.check: // only one
        check();
//                break;
//        }
    }

    /**
     * Checks a redirect, in background
     * https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
     */
    private void check() {
        // disable button and run in background
        check.setEnabled(false);
        info.setText(R.string.mRedir_checking);

        new Thread(() -> {

            // get url
            String url = getUrl();

            int message = R.string.mRedir_error;
            HttpURLConnection conn = null;
            try {
                // perform GET to the url
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
                int responseCode = conn.getResponseCode();
                Log.d("RESPONSE_CODE", url + ": " + responseCode);
                switch (responseCode) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        String location = conn.getHeaderField("Location");
                        location = URLDecoder.decode(location, "UTF-8");
                        url = new URL(new URL(url), location).toExternalForm(); // Deal with relative URLs
                        break;
                    default:
                        message = R.string.mRedir_final;
                        url = null;
                }
            } catch (IOException e) {
                // error
                e.printStackTrace();
                url = null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            // notify
            final int finalMessage = message;
            final String finalUrl = url;
            getActivity().runOnUiThread(() -> {
                if (finalUrl == null) {
                    // no redirection, show message (keep button disabled)
                    info.setText(finalMessage);
                } else {
                    // redirection, change url and enable button again
                    info.setText(R.string.mRedir_redir);
                    updateUrl(finalUrl);
                    check.setEnabled(true);
                }
            });
        }).start();
    }

}
