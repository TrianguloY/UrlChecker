package com.trianguloy.urlchecker.modules;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.utilities.LastOpened;
import com.trianguloy.urlchecker.utilities.PackageUtilities;
import com.trianguloy.urlchecker.utilities.UrlUtilities;

import java.util.List;

/**
 * This module contains an open and share buttons
 */
public class OpenModule extends BaseModule implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private LastOpened lastOpened;

    private List<String> packages;
    private Button btn_open;
    private ImageButton btn_openWith;
    private Menu menu;
    private PopupMenu popup;

    public OpenModule(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutBase() {
        return R.layout.module_open;
    }

    @Override
    public void onInitialize(View views) {
        btn_open = views.findViewById(R.id.open);
        btn_open.setOnClickListener(this);
        btn_openWith = views.findViewById(R.id.open_with);
        btn_openWith.setOnClickListener(this);
        views.findViewById(R.id.share).setOnClickListener(this);


        popup = new PopupMenu(getActivity(), btn_open);
        popup.setOnMenuItemClickListener(this);
        menu = popup.getMenu();

        lastOpened = new LastOpened(getActivity());
    }

    @Override
    public void onNewUrl(String url) {
        updateSpinner();
    }

    // ------------------- Button listener -------------------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                openUrl(0);
                break;
            case R.id.share:
                shareUrl();
                break;
            case R.id.open_with:
                showList();
                break;
        }
    }

    // ------------------- PopupMenu.OnMenuItemClickListener -------------------

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        openUrl(item.getItemId());
        return false;
    }

    // ------------------- Spinner -------------------

    private void updateSpinner() {
        packages = PackageUtilities.getOtherPackages(UrlUtilities.getViewIntent(getUrl(), null), getActivity());

        // check no apps
        if (packages.isEmpty()) {
            btn_open.setText("");
            btn_open.setEnabled(false);
            btn_openWith.setVisibility(View.GONE);
            return;
        }

        // sort
        lastOpened.sort(packages);

        // set
        btn_open.setText("Open with " + PackageUtilities.getPackageName(packages.get(0), getActivity()));
        btn_open.setEnabled(true);
        menu.clear();
        if (packages.size() == 1) {
            btn_openWith.setVisibility(View.GONE);
        } else {
            btn_openWith.setVisibility(View.VISIBLE);
            for (int i = 1; i < packages.size(); i++) {
                menu.add(Menu.NONE, i, i, "Open with " + PackageUtilities.getPackageName(packages.get(i), getActivity()));
            }
        }

    }

    // ------------------- Buttons -------------------

    /**
     * Open url in a specific app
     * @param index index from the packages list of the app to use
     */
    private void openUrl(int index) {
        if (index < 0 || index >= packages.size()) return;

        // open
        String chosed = packages.get(index);
        lastOpened.usedPackage(chosed);
        getActivity().startActivity(UrlUtilities.getViewIntent(getUrl(), chosed));
    }

    /**
     * Show the popup with the rest of the apps
     */
    private void showList() {
        popup.show();
    }

    /**
     * Shares the url as text
     */
    private void shareUrl() {
        // create send intent
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getUrl());
        sendIntent.setType("text/plain");

        // share intent
        Intent shareIntent = Intent.createChooser(sendIntent, "Share");
        getActivity().startActivity(shareIntent);
    }
}
