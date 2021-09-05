package com.trianguloy.urlchecker.modules.list;

import android.app.AlertDialog;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.UrlUtilities;
import com.trianguloy.urlchecker.utilities.VirusTotalUtility;

/**
 * This module uses the VirusTotal api (https://developers.virustotal.com/reference) for url reports
 */
public class VirusTotalModule extends AModuleData {

    static GenericPref.Str API_PREF() {
        return new GenericPref.Str("api_key", "");
    }

    @Override
    public String getId() {
        return "virustotal";
    }

    @Override
    public int getName() {
        return R.string.mVT_name;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new VirusTotalDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new VirusTotalConfig(cntx);
    }
}

class VirusTotalConfig extends AModuleConfig implements TextWatcher {

    final GenericPref.Str api_key = VirusTotalModule.API_PREF();

    public VirusTotalConfig(ConfigActivity cntx) {
        super(cntx);
        api_key.init(cntx);
    }

    @Override
    public boolean canBeEnabled() {
        final String key = api_key.get();
        return key != null && !key.isEmpty();
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_virustotal;
    }

    @Override
    public void onInitialize(View views) {
        final EditText edit_key = (EditText) views.findViewById(R.id.api_key);
        edit_key.setText(api_key.get());
        edit_key.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        api_key.set(s.toString());
        if (!canBeEnabled()) disable();
    }
}

class VirusTotalDialog extends AModuleDialog implements View.OnClickListener, View.OnLongClickListener {

    private static final int RETRY_TIMEOUT = 5000;
    private Button btn_scan;
    private TextView txt_result;

    private boolean scanning = false;
    private VirusTotalUtility.InternalReponse result = null;

    private final GenericPref.Str api_key;

    public VirusTotalDialog(MainDialog dialog) {
        super(dialog);
        api_key = VirusTotalModule.API_PREF();
        api_key.init(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_virustotal;
    }

    @Override
    public void onInitialize(View views) {
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
                scanOrCancel();
                break;
            case R.id.result:
                showInfo(false);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.result) {
            showInfo(true);
            return true;
        }
        return false;
    }


    /**
     * Performs a scan of the current url, in background.
     * Or cancels current scan
     */
    private void scanOrCancel() {
        if (scanning) {
            // already scanning? cancel
            scanning = false;
        } else {
            // start scan
            scanning = true;
            new Thread(this::_scanUrl).start();
        }
        updateUI();
    }

    /**
     * Manages the scanning in the background
     */
    private void _scanUrl() {
        VirusTotalUtility.InternalReponse response;
        while (scanning) {
            // asks for the report
            response = VirusTotalUtility.scanUrl(getUrl(), api_key.get(), getActivity());

            // check valid report
            if (response.detectionsTotal > 0 || response.error != null) {
                result = response;
                scanning = false;
                getActivity().runOnUiThread(this::updateUI);
                return;
            }

            // retry if still no report
            try {
                Thread.sleep(RETRY_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Updates the ui
     */
    private void updateUI() {
        if (scanning) {
            // scanning in progress, show cancel
            btn_scan.setText(R.string.mVT_cancel);
            setResult(getActivity().getString(R.string.mVT_scanning), 0);
            btn_scan.setEnabled(true);
        } else {
            // not a scanning in progress
            btn_scan.setText(R.string.mVT_scan);
            if (result == null) {
                // no result available, new url
                setResult(getActivity().getString(R.string.mVT_pressScan), 0);
                btn_scan.setEnabled(true);
            } else {
                // result available
                if (result.error != null) {
                    // an error ocurred
                    setResult(result.error, Color.TRANSPARENT);
                    btn_scan.setEnabled(true);
                } else {
                    // valid result
                    btn_scan.setEnabled(false);
                    if (result.detectionsPositive > 2) {
                        // more that two bad detection, bad url
                        setResult(getActivity().getString(R.string.mVT_badUrl, result.detectionsPositive, result.detectionsTotal, result.date), R.color.bad);
                    } else if (result.detectionsPositive > 0) {
                        // 1 or 2 bad detections, warning
                        setResult(getActivity().getString(R.string.mVT_warningUrl, result.detectionsPositive, result.detectionsTotal, result.date), R.color.warning);
                    } else {
                        // no detections, good
                        setResult(getActivity().getString(R.string.mVT_goodUrl, result.detectionsTotal, result.date), R.color.good);
                    }
                }
            }
        }

    }

    /**
     * Utility to update the ui
     *
     * @param message with this message
     * @param color   and this background color
     */
    private void setResult(String message, int color) {
        txt_result.setText(message);
        txt_result.setBackgroundColor(color == 0 ? Color.TRANSPARENT : getActivity().getResources().getColor(color));
    }

    /**
     * Shows the report results
     *
     * @param details if true, the virustotal page is opened, if false just a basic dialog with the json
     */
    private void showInfo(boolean details) {
        if (result == null || result.error != null) return;

        if (details) {
            UrlUtilities.openUrlRemoveThis(result.scanUrl, getActivity());
        } else {
            // TODO: beautify this
            new AlertDialog.Builder(getActivity())
                    .setMessage(result.info)
                    .show();
        }
    }
}
