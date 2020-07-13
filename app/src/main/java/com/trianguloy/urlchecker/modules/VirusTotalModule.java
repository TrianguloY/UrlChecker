package com.trianguloy.urlchecker.modules;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.UrlUtilities;
import com.trianguloy.urlchecker.utilities.VirusTotalUtility;

public class VirusTotalModule extends BaseModule implements View.OnClickListener, View.OnLongClickListener {

    private ImageButton btn_scan;
    private TextView txt_result;

    private boolean scanning = false;
    private VirusTotalUtility.InternalReponse result = null;

    @Override
    public String getName() {
        return "VirusTotal";
    }

    @Override
    public int getLayoutBase() {
        return R.layout.module_virustotal;
    }

    @Override
    public void initialize(View views) {
        btn_scan = views.findViewById(R.id.scan);
        txt_result = views.findViewById(R.id.result);

        btn_scan.setOnClickListener(this);
        txt_result.setOnClickListener(this);
        txt_result.setOnLongClickListener(this);
    }

    @Override
    public void onNewUrl(String url) {
        scanning = false;
        result = null;
        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                scan();
                break;
            case R.id.result:
                showInfo(false);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.result:
                showInfo(true);
                return true;
        }
        return false;
    }

    private void scan() {
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
            response = VirusTotalUtility.scanUrl(cntx.getUrl());

            if (response.detectionsTotal > 0) {
                result = response;
                scanning = false;
                cntx.runOnUiThread(new Runnable() {
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
        if (scanning) {
            btn_scan.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            setResult("Scanning...", 0);
            btn_scan.setEnabled(true);
        } else {
            btn_scan.setImageResource(android.R.drawable.ic_menu_search);
            if (result == null) {
                setResult("Press to scan", 0);
                btn_scan.setEnabled(true);
            } else {
                if (result.detectionsTotal <= 0) {
                    setResult("no detections? strange", R.color.warning);
                } else if (result.detectionsPositive > 2) {
                    setResult("Uh oh, " + result.detectionsPositive + "/" + result.detectionsTotal + " engines detected the url (as of date " + result.date + ")", R.color.bad);
                } else if (result.detectionsPositive > 0) {
                    setResult("Uh oh, " + result.detectionsPositive + "/" + result.detectionsTotal + " engines detected the url (as of date " + result.date + ")", R.color.warning);
                } else {
                    setResult("None of the " + result.detectionsTotal + " engines detected the site (as of date " + result.date + ")", R.color.good);
                }
                btn_scan.setEnabled(false);
            }
        }

    }

    private void setResult(String message, int color) {
        txt_result.setBackgroundColor(color == 0 ? Color.TRANSPARENT : cntx.getResources().getColor(color));
        txt_result.setText(message);
    }


    private void showInfo(boolean details) {
        if (result == null) return;

        if (details) {
            UrlUtilities.openUrlRemoveThis(result.scanUrl, cntx);
            cntx.finish();
        } else {
            new AlertDialog.Builder(cntx)
                    .setMessage(result.info)
                    .show();
        }
    }
}
