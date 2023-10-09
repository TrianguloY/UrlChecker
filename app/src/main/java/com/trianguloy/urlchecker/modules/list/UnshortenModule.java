package com.trianguloy.urlchecker.modules.list;

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
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

/**
 * Module to Unshort links by using https://unshorten.me/
 * Consider adding other services, or even allow custom
 */
public class UnshortenModule extends AModuleData {

    @Override
    public String getId() {
        return "unshorten";
    }

    @Override
    public int getName() {
        return R.string.mUnshort_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new UnshortenDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new DescriptionConfig(R.string.mUnshort_desc);
    }
}

class UnshortenDialog extends AModuleDialog {

    private Button unshort;
    private TextView info;

    private Thread thread = null;

    public UnshortenDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.button_text;
    }

    @Override
    public void onInitialize(View views) {
        unshort = views.findViewById(R.id.button);
        unshort.setText(R.string.mUnshort_unshort);
        unshort.setOnClickListener(v -> unshort());

        info = views.findViewById(R.id.text);
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
        unshort.setEnabled(true);
        info.setText("");
        AndroidUtils.clearRoundedColor(info);
    }

    /**
     * Unshorts the current url
     */
    private void unshort() {
        // disable button and run in background
        unshort.setEnabled(false);
        info.setText(R.string.mUnshort_checking);
        AndroidUtils.clearRoundedColor(info);

        thread = new Thread(this::_check);
        thread.start();
    }

    private void _check() {

        try {
            // get response
            var response = new JSONObject(StreamUtils.readFromUrl("https://unshorten.me/json/" + getUrl()));
            var resolved_url = response.getString("resolved_url");
            var usage_count = Integer.parseInt(response.optString("usage_count", "0"));
            var ref = new Object() { // reference object to allow using these inside lambdas
                int usage_limit = 10; // documented but hardcoded
                int remaining_calls = usage_limit - usage_count;
            };
            var error = response.optString("error", "(no reported error)");
            var success = response.getBoolean("success");

            // remaining_calls is not documented, but if it's present use it and replace the hardcoded usage_limit
            try {
                ref.remaining_calls = Integer.parseInt(response.optString("remaining_calls", ""));
                ref.usage_limit = usage_count + ref.remaining_calls;
            } catch (NumberFormatException ignore) {
                // not present, ignore
            }

            // exit if was canceled
            if (Thread.currentThread().isInterrupted()) {
                Log.d("THREAD", "Interrupted");
                return;
            }

            if (!success) {
                // server error, maybe too many checks
                getActivity().runOnUiThread(() -> {
                    info.setText(getActivity().getString(R.string.mUnshort_error, error));
                    AndroidUtils.setRoundedColor(R.color.warning, info);
                    // allow retries
                    unshort.setEnabled(true);
                });
            } else if (Objects.equals(resolved_url, getUrl())) {
                // same, nothing to replace
                getActivity().runOnUiThread(() -> {
                    var pending = ref.remaining_calls <= ref.usage_limit / 2
                            ? " (" + getActivity().getString(R.string.mUnshort_pending, ref.remaining_calls, ref.usage_limit) + ")"
                            : "";
                    info.setText(getActivity().getString(R.string.mUnshort_notFound) + pending);
                    AndroidUtils.clearRoundedColor(info);
                });
            } else {
                // correct, replace
                getActivity().runOnUiThread(() -> {
                    setUrl(new UrlData(resolved_url).dontTriggerOwn());

                    var pending = ref.remaining_calls <= ref.usage_limit / 2
                            ? " (" + getActivity().getString(R.string.mUnshort_pending, ref.remaining_calls, ref.usage_limit) + ")"
                            : "";
                    info.setText(getActivity().getString(R.string.mUnshort_ok) + pending);
                    AndroidUtils.setRoundedColor(R.color.good, info);
                    // a short url can redirect to another short url
                    unshort.setEnabled(true);
                });
            }

        } catch (IOException | JSONException e) {
            // internal error
            e.printStackTrace();

            // exit if was canceled
            if (Thread.currentThread().isInterrupted()) {
                Log.d("THREAD", "Interrupted");
                return;
            }

            getActivity().runOnUiThread(() -> {
                info.setText(getActivity().getString(R.string.mUnshort_internal, e.getMessage()));
                AndroidUtils.setRoundedColor(R.color.bad, info);
                unshort.setEnabled(true);
            });
        }

    }

}
