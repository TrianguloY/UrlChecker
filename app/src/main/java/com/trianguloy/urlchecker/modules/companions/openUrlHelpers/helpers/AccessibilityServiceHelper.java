package com.trianguloy.urlchecker.modules.companions.openUrlHelpers.helpers;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.modules.companions.Incognito;
import com.trianguloy.urlchecker.services.UrlHelperService;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

public class AccessibilityServiceHelper implements JavaUtils.TriConsumer<Context, String, String> {
    @Override
    public void accept(Context context, String url, String pckg) {
        var instance = UrlHelperService.getInstance();
        if (instance == null){
            // FIXME: is this enough if it is not online? maybe do the check in open module?
            Toast.makeText(context, R.string.helperService_notConnected, Toast.LENGTH_LONG);
        } else {
            instance.openService(url, pckg,
                    Incognito.getAccessibilityFunction(Incognito.getKey(context,
                            new Intent().setPackage(pckg))));
        }
    }
}
