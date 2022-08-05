package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utilities related to urls
 */
public class UrlUtilities {

    /**
     * Returns an intent that will open the given url, with an optional package
     *
     * @param url         the url that will be opened
     * @param packageName the package that will be opened, null to let android choose
     * @return the converted intent
     */
    static public Intent getViewIntent(String url, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (packageName != null) intent.setPackage(packageName);
        return intent;
    }

    /**
     * Opens an url removing this app from the chooser
     *
     * @param url  url to open
     * @param cntx base context
     */
    static public void openUrlRemoveThis(String url, Context cntx) {

        // get packages that can open the url
        List<Intent> intents = new ArrayList<>();
        for (String pack : PackageUtilities.getOtherPackages(getViewIntent(url, null), cntx)) {
            intents.add(getViewIntent(url, pack));
        }

        // check if none
        if (intents.isEmpty()) {
            Toast.makeText(cntx, R.string.toast_noBrowser, Toast.LENGTH_SHORT).show();
            return;
        }

        // create chooser
        Intent chooserIntent = Intent.createChooser(intents.remove(0), cntx.getString(R.string.title_choose));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[0]));

        // open
        PackageUtilities.startActivity(chooserIntent, R.string.toast_noBrowser, cntx);
    }
}
