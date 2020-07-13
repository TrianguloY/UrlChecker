package com.trianguloy.urlchecker.modules;

import android.content.Context;
import android.view.View;

import com.trianguloy.urlchecker.MainDialog;

public abstract class BaseModule {

    protected MainDialog cntx;

    public void setContext(MainDialog cntx){
        this.cntx = cntx;
    }

    public abstract String getName();

    public abstract int getLayoutBase();

    public abstract void initialize(View views);

    public abstract void onNewUrl(String url);
}
