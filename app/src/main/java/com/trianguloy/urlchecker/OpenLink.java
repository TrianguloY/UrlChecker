package com.trianguloy.urlchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class OpenLink extends Activity {
    
    private TextView txt_url;
    private TextView txt_result;
    private ImageButton btn_redirect;
    private ImageButton btn_scan;
    
    private CustomAdapter adapter;
    
    private String url = null;
    private VirusTotalUtility.InternalReponse result = null;
    
    private boolean scanning = false;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_open_link);
        
        Uri uri = this.getIntent().getData();
        if (uri == null) {
            Toast.makeText(this, "No url!!!!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        url = uri.toString();
        
        txt_url = findViewById(R.id.txt_url);
        txt_result = findViewById(R.id.txt_result);
        btn_redirect = findViewById(R.id.btn_goRedirect);
        btn_scan = findViewById(R.id.btn_scan);
        
        GridView grdVw_browsers = findViewById(R.id.grdVw_browsers);
        adapter = new CustomAdapter(this);
        
        grdVw_browsers.setAdapter(adapter);
        grdVw_browsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage(adapter.getItem(position));
                startActivity(intent);
            }
        });
        
        
        //setOnLongClick
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                OpenLink.this.onLongClick(view);
                return false;
            }
        };
        txt_url.setOnLongClickListener(onLongClickListener);
        txt_result.setOnLongClickListener(onLongClickListener);
        
        btn_redirect.setOnLongClickListener(onLongClickListener);
        btn_scan.setOnLongClickListener(onLongClickListener);
        
        
        updateUI();
        createBrowsers();
        
    }
    
    private void setResult(String message, int color) {
        txt_result.setBackgroundColor(color);
        txt_result.setText(message);
    }
    
    private void scanUrl() {
        if (scanning) {
            scanning = false;
        } else {
            scanning = true;
            new Thread(new Runnable() {
                public void run() {
                    _scanUrl();
                }
            }).start();
        }
        updateUI();
    }
    
    private void _scanUrl() {
        VirusTotalUtility.InternalReponse response;
        while (scanning) {
            response = VirusTotalUtility.scanUrl(url);
            
            if (response.detectionsTotal > 0) {
                result = response;
                scanning = false;
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
                return;
            }
            
            //retry
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private void updateUI() {
        txt_url.setText(url);
        
        if (scanning) {
            btn_scan.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            setResult("Scanning...", Color.GRAY);
        } else {
            btn_scan.setImageResource(android.R.drawable.ic_menu_search);
            if (result == null) {
                setResult("Press to scan", 0);
            } else {
                if (result.detectionsTotal <= 0) {
                    setResult("no detections? strange", Color.YELLOW);
                } else if (result.detectionsPositive > 0) {
                    setResult("Uh oh, " + result.detectionsPositive + "/" + result.detectionsTotal + " engines detected the url (as of date " + result.date + ")", Color.RED);
                } else {
                    setResult("None of the " + result.detectionsTotal + " engines detected the site (as of date " + result.date + ")", Color.GREEN);
                }
            }
        }
        
    }
    
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                scanUrl();
                break;
            case R.id.btn_goRedirect:
                followRedirect();
                break;
            case R.id.txt_result:
                if (result != null) {
                    openUrlInBrowser(result.scanUrl);
                }
                break;
            
            //DEBUG: just in case
            case R.id.txt_url:
                openUrlInBrowser(url);
                break;
        }
    }
    
    public void onLongClick(View view) {
        switch (view.getId()) {
            case R.id.btn_goRedirect:
            case R.id.btn_scan:
                Toast.makeText(this, view.getContentDescription(), Toast.LENGTH_SHORT).show();
                break;
            
            case R.id.txt_result:
                showDebug();
                break;
        }
    }
    
    private void showDebug() {
        if (result != null) {
            new AlertDialog.Builder(this)
                    .setMessage(result.info)
                    .show();
        }
    }
    
    //https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
    private void followRedirect() {
        
        new Thread(new Runnable() {
            public void run() {
                String message = null;
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
                            result = null;
                            break;
                        default:
                            message = "No redirection, final URL, try to scan now";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    message = "Error when following redirect";
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
                _createBrowsers();
                
                final String finalMessage = message;
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                        if (finalMessage != null) {
                            Toast.makeText(OpenLink.this, finalMessage, Toast.LENGTH_SHORT).show();
                            btn_redirect.setEnabled(false);
                        }
                    }
                });
            }
        }).start();
        
        
    }
    
    private void createBrowsers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                _createBrowsers();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
    
    private void _createBrowsers() {
        adapter.clearAll();
        Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(baseIntent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!resolveInfo.activityInfo.packageName.equals(getPackageName())) {
                adapter.addItem(resolveInfo.activityInfo.packageName);
            }
        }
    }
    
    
    private void openUrlInBrowser(String url) {
        Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(baseIntent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : 0);
        List<Intent> intents = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!resolveInfo.activityInfo.packageName.equals(getPackageName())) {
                Intent intent = new Intent(baseIntent);
                intent.setPackage(resolveInfo.activityInfo.packageName);
                intents.add(intent);
            }
        }
        
        Intent chooserIntent = Intent.createChooser(intents.remove(0), "Choose app");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
        
        startActivity(chooserIntent);
        
        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        finish();
    }
}
