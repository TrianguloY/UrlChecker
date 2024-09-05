package com.trianguloy.forceurl.data;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Meant to be used with {@link Apps}. Has one function, which will be called by the
 * accessibility helper to help open the URL as needed.
 */
public interface AccessibilityFunction {
    /**
     * Method that will help the UrlHelperService put the URL in the search bar or similar,
     * only needed when an url needs help. Will be applied multiple times until the service
     * is closed.
     *
     * @param rootNode
     * @param url
     * @param pckg
     * @return True the moment this method is not needed anymore and the service can be closed.
     */
    boolean putUrl(AccessibilityNodeInfo rootNode, String url, String pckg);
}
