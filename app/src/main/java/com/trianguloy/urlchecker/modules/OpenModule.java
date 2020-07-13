package com.trianguloy.urlchecker.modules;

import android.content.Intent;
import android.view.View;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.UrlUtilities;

public class OpenModule extends BaseModule implements View.OnClickListener {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getLayoutBase() {
        return R.layout.module_share;
    }

    @Override
    public void initialize(View views) {
        views.findViewById(R.id.open).setOnClickListener(this);
        views.findViewById(R.id.share).setOnClickListener(this);
    }

    @Override
    public void onNewUrl(String url) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                openUrl();
                break;
            case R.id.share:
                shareUrl();
                break;
        }
    }

    private void openUrl() {
        UrlUtilities.openUrlRemoveThis(cntx.getUrl(), cntx);
        cntx.finish();
    }

    private void shareUrl() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, cntx.getUrl());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share");
        cntx.startActivity(shareIntent);
    }
}
