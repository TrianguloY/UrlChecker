package com.trianguloy.urlchecker.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class PackageUtilities {
    static public List<String> getOtherPackages(Intent baseIntent, Context cntx) {
        List<String> packages = new ArrayList<>();

        PackageManager packageManager = cntx.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(baseIntent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!resolveInfo.activityInfo.packageName.equals(cntx.getPackageName())) {
                packages.add(resolveInfo.activityInfo.packageName);
            }
        }
        return packages;
    }


    static public String getPackageName(String pack, Context cntx){
        final PackageManager pm = cntx.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pack,PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "unknown";
        }
    }
}
