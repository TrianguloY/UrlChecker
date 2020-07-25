package com.trianguloy.urlchecker.modules;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.trianguloy.urlchecker.MainDialog;

public abstract class BaseModule {

    // ------------------- data -------------------

    private MainDialog dialog;

    // ------------------- initialization -------------------

    public void registerDialog(MainDialog cntx) {
        this.dialog = cntx;
    }

    // ------------------- abstract functions -------------------

    public abstract String getName();

    public abstract int getLayoutBase();

    public abstract void initialize(View views);

    public abstract void onNewUrl(String url);

    // ------------------- protected utilities -------------------

    protected Activity getActivity() {
        return dialog;
    }

    protected String getUrl() {
        return dialog.getUrl();
    }

    protected void setUrl(String url) {
        dialog.setUrl(url, this);
    }
}
