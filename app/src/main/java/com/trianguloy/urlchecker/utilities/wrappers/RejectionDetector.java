package com.trianguloy.urlchecker.utilities.wrappers;

import android.content.Context;

import com.trianguloy.urlchecker.utilities.generics.GenericPref;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Saves the package and time of the last opened url.
 * If it is requested again in a short amount of time, that package is considered to 'reject' the url and is hidden.
 */
public class RejectionDetector {

    private static final int TIMEFRAME = 5000;
    private final GenericPref.LstStr rejectLast; // [openedTimeMillis, package, url]

    public RejectionDetector(Context cntx) {
        rejectLast = new GenericPref.LstStr("reject_last", "\n", 3, Collections.emptyList(), cntx);
    }

    /**
     * Marks a url as opened to a package (at this moment)
     */
    public void markAsOpen(String url, String packageName) {
        rejectLast.set(List.of(Long.toString(System.currentTimeMillis()), packageName, url));
    }

    /**
     * returns the last package that opened the url if it happened in a short amount of time, null otherwise
     */
    public String getPrevious(String url) {
        try {
            var data = rejectLast.get();

            // return the saved package if the time is less than the timeframe and the url is the same
            return !data.isEmpty()
                    && System.currentTimeMillis() - Long.parseLong(data.get(0)) < TIMEFRAME
                    && Objects.equals(data.get(2), url)
                    ? data.get(1)
                    : null;
        } catch (Exception ignore) {
            // just ignore errors while retrieving the data
            return null;
        }
    }
}
