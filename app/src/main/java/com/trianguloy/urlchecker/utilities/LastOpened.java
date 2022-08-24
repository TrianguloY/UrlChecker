package com.trianguloy.urlchecker.utilities;

import android.content.Context;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manages a list of last opened apps, for priority purposes
 */
public class LastOpened {

    /**
     * How many apps to remember
     */
    private static final int N = 5;

    private static final String PREFIX = "opened";

    private final Context cntx;

    /**
     * Initializes this utility
     */
    public LastOpened(Context cntx) {
        this.cntx = cntx;

        // debug
//        System.out.println(list);
    }

    /**
     * Sorts an existing list of packages in-place with the last opened for a given url
     */
    public void sort(List<String> packs, String url) {
        List<String> priority = getPriority(url).get();

        // sort list based on priority
        Collections.sort(packs, (p1, p2) -> Integer.compare(priority.indexOf(p2), priority.indexOf(p1)));
    }

    /**
     * Returns the priority list preferences for a given url.
     * The most preferred is the last.
     */
    private GenericPref.LstStr getPriority(String url) {
        // get top level domain and first subdomain (if any)
        String domain;
        try {
            List<String> domainParts = Arrays.asList(new URL(url).getHost().split("\\.", 3));
            domain = String.join(".", domainParts.size() < 2 ? domainParts : domainParts.subList(0, 2));
        } catch (Exception e) {
            e.printStackTrace();
            domain = "";
        }

        // init list for that domain
        GenericPref.LstStr pref = new GenericPref.LstStr(PREFIX + domain, ";", Collections.emptyList());
        pref.init(cntx);
        return pref;
    }

    /**
     * Marks a package as used for a given url, updating the priority list
     */
    public void usedPackage(String pack, String url) {
        GenericPref<List<String>> pref = getPriority(url);
        List<String> priority = pref.get();

        // update
        int i = priority.indexOf(pack);
        if (i >= 0 && i + 1 < priority.size()) {
            // already present and not most prioritized, shift up
            Collections.swap(priority, i, i + 1);
        } else if (i == -1) {
            // not present, add as last (keep to N max)
            while (priority.size() >= N) priority.remove(0);
            priority.add(0, pack);
        }

        // save
        pref.set(priority);
    }
}
