package com.trianguloy.urlchecker.modules.list;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.AutomationRules;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.generics.GenericPref;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebhookModule extends AModuleData {

    public static GenericPref.Str WEBHOOK_URL_PREF(Context cntx) {
        return new GenericPref.Str("webhook_url", "", cntx);
    }

    public static GenericPref.Bool AUTO_SEND_PREF(Context cntx) {
        return new GenericPref.Bool("webhook_auto", false, cntx);
    }

    @Override
    public String getId() {
        return "webhook";
    }

    @Override
    public int getName() {
        return R.string.mWebhook_name;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new WebhookDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new WebhookConfig(cntx);
    }

    @Override
    public List<AutomationRules.Automation<AModuleDialog>> getAutomations() {
        return (List<AutomationRules.Automation<AModuleDialog>>) (List<?>) WebhookDialog.AUTOMATIONS;
    }
}

class WebhookDialog extends AModuleDialog {

    private static final Executor executor = Executors.newSingleThreadExecutor();
    
    private final GenericPref.Str webhookUrl;
    private final GenericPref.Bool autoSend;
    private TextView statusText;
    private UrlData currentUrl;

    public static final List<AutomationRules.Automation<WebhookDialog>> AUTOMATIONS = new ArrayList<>();
    static {
        AUTOMATIONS.add(new AutomationRules.Automation<>(
            "webhook_send",
            R.string.mWebhook_auto_send,
            dialog -> dialog.sendToWebhook()
        ));
    }

    public WebhookDialog(MainDialog dialog) {
        super(dialog);
        webhookUrl = WebhookModule.WEBHOOK_URL_PREF(dialog);
        autoSend = WebhookModule.AUTO_SEND_PREF(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_webhook;
    }

    @Override
    public void onInitialize(View views) {
        statusText = views.findViewById(R.id.status_text);
        views.findViewById(R.id.send_button).setOnClickListener(v -> sendToWebhook());
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        currentUrl = urlData;
        if (autoSend.get()) {
            sendToWebhook();
        }
    }

    private void sendToWebhook() {
        if (currentUrl == null) return;
        
        String webhook = webhookUrl.get();
        if (webhook.isEmpty()) {
            showStatus(R.string.mWebhook_no_url, true);
            return;
        }

        showStatus(R.string.mWebhook_sending, false);
        
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("url", currentUrl.url);
                json.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .format(new Date()));
                
                URL url = new URL(webhook);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    showStatus(R.string.mWebhook_success, false);
                } else {
                    showStatus(R.string.mWebhook_error, true);
                }

            } catch (Exception e) {
                showStatus(R.string.mWebhook_error, true);
            }
        });
    }

    private void showStatus(int stringId, boolean isError) {
        getActivity().runOnUiThread(() -> {
            statusText.setText(stringId);
            statusText.setVisibility(View.VISIBLE);
            
            if (!isError) {
                Toast.makeText(getActivity(), stringId, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

class WebhookConfig extends AModuleConfig {

    private final GenericPref.Str webhookUrl;
    private final GenericPref.Bool autoSend;

    public WebhookConfig(ModulesActivity activity) {
        super(activity);
        webhookUrl = WebhookModule.WEBHOOK_URL_PREF(activity);
        autoSend = WebhookModule.AUTO_SEND_PREF(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_webhook;
    }

    @Override
    public void onInitialize(View views) {
        webhookUrl.attachToEditText(views.findViewById(R.id.webhook_url));
        autoSend.attachToSwitch(views.findViewById(R.id.auto_send));
    }
} 