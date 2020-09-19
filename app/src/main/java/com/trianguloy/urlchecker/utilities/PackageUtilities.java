package com.trianguloy.urlchecker.utilities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utilities related to packages
 */
public class PackageUtilities {

    /**
     * Returns a list of packages that can open an intent, removing this app from the list
     *
     * @param baseIntent intent that the packages will be able to open
     * @param cntx       base context (and the app that will be filtered)
     * @return the list of other packages
     */
    static public List<String> getOtherPackages(Intent baseIntent, Context cntx) {
        List<String> packages = new ArrayList<>();

        // get all packages
        PackageManager packageManager = cntx.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(baseIntent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : 0);

        // filter the current app
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!resolveInfo.activityInfo.packageName.equals(cntx.getPackageName())) {
                packages.add(resolveInfo.activityInfo.packageName);
            }
        }

        return packages;
    }

    /**
     * Returns the app name of a package
     *
     * @param pack packagename to search
     * @param cntx base context
     * @return that app name
     */
    static public String getPackageName(String pack, Context cntx) {
        final PackageManager pm = cntx.getPackageManager();
        try {
            // try getting the app label
            return pm.getApplicationLabel(pm.getApplicationInfo(pack, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            // can't get the label
            e.printStackTrace();
            return "unknown";
        }
    }

    /**
     * Wrapper for startActivity to catch ActivityNotFoundException
     *
     * @param intent intent to open
     * @param string toast shown on ActivityNotFoundException
     * @param cntx   base context
     */
    static public void startActivity(Intent intent, int string, Context cntx) {
        try {
            cntx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(cntx, string, Toast.LENGTH_SHORT).show();
        }
    }
}
