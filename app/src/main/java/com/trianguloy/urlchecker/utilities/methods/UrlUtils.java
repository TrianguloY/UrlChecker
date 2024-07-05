package com.trianguloy.urlchecker.utilities.methods;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.wrappers.IntentApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utilities related to urls
 */
public interface UrlUtils {

    /**
     * Returns an intent that will open the given url, with an optional package
     *
     * @param url       the url that will be opened
     * @param intentApp the intentApp that will be opened, null to let android choose
     * @return the converted intent
     */
    static Intent getViewIntent(String url, IntentApp intentApp) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intentApp != null) intent.setComponent(intentApp.getComponent());
        return intent;
    }

}
