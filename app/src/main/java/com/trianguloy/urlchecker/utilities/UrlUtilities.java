package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UrlUtilities {

    static public Intent getViewIntent(String url, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (packageName != null) intent.setPackage(packageName);
        return intent;
    }

    static public void openUrlRemoveThis(String url, Context cntx) {

        List<Intent> intents = new ArrayList<>();
        for (String pack : PackageUtilities.getOtherPackages(getViewIntent(url, null), cntx)) {
            intents.add(getViewIntent(url, pack));
        }

        if (intents.isEmpty()) {
            Toast.makeText(cntx, "No apps can open this url", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent chooserIntent = Intent.createChooser(intents.remove(0), "Choose app");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));

        cntx.startActivity(chooserIntent);

        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
