package com.trianguloy.urlchecker.modules.companions;

import android.content.Context;

import com.trianguloy.urlchecker.utilities.generics.GenericPref;
import com.trianguloy.urlchecker.utilities.methods.JavaUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manages a list of last opened apps, for priority purposes
 */
public class LastOpened {

    public static GenericPref.Bool PERDOMAIN_PREF(Context cntx) {
        return new GenericPref.Bool("lastOpen_perDomain", false, cntx);
    }

    /* ------------------- data ------------------- */

    /**
     * Maximum 'preference' between two apps
     */
    private static final int MAX = 3;

    /**
     * The prefix for the savedPrefs
     */
    private static final String PREFIX = "opened %s %s";
    private final GenericPref.Bool perDomainPref;
    private final Context cntx;

    /* ------------------- public ------------------- */

    /**
     * Initializes this utility
     */
    public LastOpened(Context cntx) {
        this.cntx = cntx;
        perDomainPref = PERDOMAIN_PREF(cntx);
    }

    /**
     * Sorts an existing list of [packages] with the preferred order
     */
    public void sort(List<String> packages, String url) {
        Collections.sort(packages, (from, another) -> comparePrefer(from, another, url));
    }

    /**
     * Marks the [prefer] package as preferred over [others].
     */
    public void prefer(String prefer, List<String> others, String url) {
        for (String other : others) {
            prefer(prefer, other, 1, url);
        }
    }

    /* ------------------- private ------------------- */

    /**
     * Marks that [prefer] package is preferred over [other] as much as [amount] more
     */
    private void prefer(String prefer, String other, int amount, String url) {
        // skip prefer over ourselves, it's useless
        if (prefer.equals(other)) return;

        // switch order if not lexicographically sorted
        if (prefer.compareTo(other) > 0) {
            prefer(other, prefer, -amount, url);
            return;
        }

        // update preference (we subtract because negative means preferred)
        GenericPref.Int pref = getPref(prefer, other, url);
        pref.set(JavaUtils.clamp(-MAX, pref.get() - amount, MAX));
    }

    /**
     * Returns the current preference between these two packages.
     * Equivalent result as [from].compareTo([another])
     */
    private int comparePrefer(String from, String another, String url) {
        // switch order if not lexicographically sorted
        if (from.compareTo(another) > 0) {
            return -comparePrefer(another, from, url);
        }

        // get preference
        return getPref(from, another, url).get();
    }

    /**
     * The preference between two packages. ([left] must be lexicographically less than [right])
     */
    private GenericPref.Int getPref(String left, String right, String url) {
        String prefName = String.format(PREFIX, left, right);
        if (perDomainPref.get()) {
            prefName = getDomain(url) + " " + prefName;
        }

        return new GenericPref.Int(prefName, 0, cntx);
    }

    /**
     * Get top level domain and first subdomain (if any) from a given url
     * a.b.c.d => c.d
     * a.b.c => b.c
     * a.b => a.b
     * a => a
     */
    private String getDomain(String url) {
        try {
            List<String> domainParts = Arrays.asList(new URL(url).getHost().split("\\."));
            return String.join(".", domainParts.size() <= 1 ? domainParts : domainParts.subList(domainParts.size() - 2, domainParts.size()));
        } catch (Exception e) {
            // can't get
            e.printStackTrace();
            return "";
        }
    }
}
