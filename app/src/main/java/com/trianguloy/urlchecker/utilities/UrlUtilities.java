package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class UrlUtilities {

    static public void openUrlRemoveThis(String url, Context cntx) {
        Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PackageManager packageManager = cntx.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(baseIntent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : 0);
        List<Intent> intents = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!resolveInfo.activityInfo.packageName.equals(cntx.getPackageName())) {
                Intent intent = new Intent(baseIntent);
                intent.setPackage(resolveInfo.activityInfo.packageName);
                intents.add(intent);
            }
        }

        Intent chooserIntent = Intent.createChooser(intents.remove(0), "Choose app");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));

        cntx.startActivity(chooserIntent);

        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
