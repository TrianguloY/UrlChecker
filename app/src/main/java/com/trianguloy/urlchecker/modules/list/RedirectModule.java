package com.trianguloy.urlchecker.modules.list;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.Stack;

/**
 * A module that allows checking for redirection by using a local browser
 */
public class RedirectModule extends AModuleData {

    @Override
    public String getId() {
        return "redirect";
    }

    @Override
    public String getName() {
        return "Redirection";
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new RedirectDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig("By pressing the redirect button a petition will be made to retrieve that url. If the result is a redirection, the new url will be replaced (can be undo). The url is fetched, but not evaluated, so redirection based on javascript won't be detected.");
    }
}

class RedirectDialog extends AModuleDialog implements View.OnClickListener {

    private Button check;
    private Button undo;

    /**
     * The redirected urls, for undoing
     */
    private Stack<String> urls = new Stack<>();

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
        undo = views.findViewById(R.id.undo);
        undo.setOnClickListener(this);
    }

    @Override
    public void onNewUrl(String url) {
        urls.clear();
        check.setEnabled(true);
        undo.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check:
                check();
                break;
            case R.id.undo:
                undo();
                break;
        }
    }

    /**
     * Checks a redirect, in background
     * https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
     */
    private void check() {
        // disable button and run in background
        check.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {

                // get url
                String url = getUrl();

                String message = "Unknown error";
                HttpURLConnection conn = null;
                try {
                    // perform GET to the url
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
                    switch (conn.getResponseCode()) {
                        case HttpURLConnection.HTTP_MOVED_PERM:
                        case HttpURLConnection.HTTP_MOVED_TEMP:
                            String location = conn.getHeaderField("Location");
                            location = URLDecoder.decode(location, "UTF-8");
                            url = new URL(new URL(url), location).toExternalForm(); // Deal with relative URLs
                            break;
                        default:
                            message = "No redirection, final URL, try to scan now";
                            url = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    message = "Error when following redirect";
                    url = null;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

                // notify
                final String finalMessage = message;
                final String finalUrl = url;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (finalUrl == null) {
                            // no redirection, show message (keep buton disabled)
                            Toast.makeText(getActivity(), finalMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            // redirection, change url and enable button again
                            urls.push(getUrl());
                            setUrl(finalUrl);
                            undo.setEnabled(true);
                            check.setEnabled(true);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * Undo one redirection
     */
    private void undo() {
        if (urls.isEmpty()) return;

        // set previous url and enable/disable buttons if needed
        setUrl(urls.pop());
        check.setEnabled(true);
        if (urls.isEmpty()) undo.setEnabled(false);
    }

}
