package com.trianguloy.urlchecker.utilities;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a list of last opened apps, for priority purposes
 */
public class LastOpened {

    /**
     * How many apps to remember
     */
    private static final int N = 5;

    /**
     * The preferences
     */
    private final List<GenericPref.Str> list = new ArrayList<>(N);
    private static final String PREFIX = "opened";

    /**
     * Initializes this utility
     *
     * @param cntx base context
     */
    public LastOpened(Context cntx) {
        for (int i = 0; i < N; i++) {
            GenericPref.Str gp = new GenericPref.Str(PREFIX + i, null);
            gp.init(cntx);
            list.add(gp);
        }

        // debug
//        System.out.println(list);
    }

    /**
     * Sorts an existing list with the last opened
     *
     * @param packs list to sort
     */
    public void sort(List<String> packs) {
        // check if a priority app is in the list
        for (int i = 0; i < N; i++) {
            final String pack = list.get(i).get();
            // and if it is, move to front
            if (packs.contains(pack)) {
                packs.remove(pack);
                packs.add(0, pack);
            }
        }
    }

    /**
     * Marks a package as used, updating the priority list
     *
     * @param pack packagename of the used app
     */
    public void usedPackage(String pack) {

        // check if already the most used, and move all one below
        if (pack.equals(list.get(N - 1).get())) {
            for (int i = 0; i < N - 3; ++i) {
                list.get(i).set(list.get(i + 1).get());
            }
            list.get(N - 2).set(null);
            return;
        }
        ;

        // check intermediate ones, and swap with previous
        for (int i = N - 2; i >= 0; i--) {
            if (pack.equals(list.get(i).get())) {
                String prev = list.get(i).get();
                list.get(i).set(list.get(i + 1).get());
                list.get(i + 1).set(prev);
                return;
            }
        }

        // if not in list, set as last
        list.get(0).set(pack);
    }
}
