package com.trianguloy.urlchecker.modules.companions;

import android.content.Context;

import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.JavaUtilities;

import java.util.Collections;
import java.util.List;

/**
 * Manages a list of last opened apps, for priority purposes
 */
public class LastOpened {

    /* ------------------- data ------------------- */

    /**
     * Maximum 'preference' between two apps
     */
    private static final int MAX = 3;

    /**
     * The prefix for the savedPrefs
     */
    private static final String PREFIX = "opened %s %s";
    private final Context cntx;

    /* ------------------- public ------------------- */

    /**
     * Initializes this utility
     */
    public LastOpened(Context cntx) {
        this.cntx = cntx;
    }

    /**
     * Sorts an existing list of [packages] with the preferred order
     */
    public void sort(List<String> packages) {
        Collections.sort(packages, this::comparePrefer);
    }

    /**
     * Marks the [prefer] package as preferred over [others].
     */
    public void prefer(String prefer, List<String> others) {
        for (String other : others) {
            prefer(prefer, other, 1);
        }
    }

    /* ------------------- private ------------------- */

    /**
     * Marks that [prefer] package is preferred over [other] as much as [amount] more
     */
    private void prefer(String prefer, String other, int amount) {
        // skip prefer over ourselves, it's useless
        if (prefer.equals(other)) return;

        // switch order if not lexicographically sorted
        if (prefer.compareTo(other) > 0) {
            prefer(other, prefer, -amount);
            return;
        }

        // update preference (we subtract because negative means preferred)
        GenericPref<Integer> pref = getPref(prefer, other);
        pref.set(JavaUtilities.clamp(-MAX, pref.get() - amount, MAX));
    }

    /**
     * Returns the current preference between these two packages.
     * Equivalent result as [from].compareTo([another])
     */
    private int comparePrefer(String from, String another) {
        // switch order if not lexicographically sorted
        if (from.compareTo(another) > 0) {
            return -comparePrefer(another, from);
        }

        // get preference
        return getPref(from, another).get();
    }

    /**
     * The preference between two packages. ([left] must be lexicographically less than [right])
     */
    private GenericPref<Integer> getPref(String left, String right) {
        return new GenericPref.Int(String.format(PREFIX, left, right), 0).init(cntx);
    }
}
