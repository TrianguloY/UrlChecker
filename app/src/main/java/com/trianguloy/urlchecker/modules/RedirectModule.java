package com.trianguloy.urlchecker.modules;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Stack;

public class RedirectModule extends BaseModule implements View.OnClickListener {

    private Button check;
    private Button undo;

    private Stack<String> urls = new Stack<>();

    @Override
    public String getName() {
        return "Redirection";
    }

    @Override
    public int getLayoutBase() {
        return R.layout.module_redirect;
    }

    @Override
    public void initialize(View views) {
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

    //https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
    private void check() {
        check.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {
                String url = getUrl();

                String message = "Unknown error";
                HttpURLConnection conn = null;
                try {
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

                final String finalMessage = message;
                final String finalUrl = url;
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (finalUrl == null) {
                            Toast.makeText(getActivity(), finalMessage, Toast.LENGTH_SHORT).show();
                        } else {
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

    private void undo() {
        if (urls.isEmpty()) return;

        setUrl(urls.pop());
        check.setEnabled(true);
        if (urls.isEmpty()) undo.setEnabled(false);
    }

}
