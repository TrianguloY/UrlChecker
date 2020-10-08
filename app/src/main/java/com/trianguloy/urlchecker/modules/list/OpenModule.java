package com.trianguloy.urlchecker.modules.list;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.utilities.LastOpened;
import com.trianguloy.urlchecker.utilities.PackageUtilities;
import com.trianguloy.urlchecker.utilities.UrlUtilities;

import java.util.List;

/**
 * This module contains an open and share buttons
 */
public class OpenModule extends AModuleData {

    @Override
    public String getId() {
        return "open";
    }

    @Override
    public int getName() {
        return R.string.mOpen_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new OpenDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.mOpen_desc);
    }
}

class OpenDialog extends AModuleDialog implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, View.OnLongClickListener {

    private LastOpened lastOpened;

    private List<String> packages;
    private Button btn_open;
    private ImageButton btn_openWith;
    private Menu menu;
    private PopupMenu popup;

    public OpenDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_open;
    }

    @Override
    public void onInitialize(View views) {
        btn_open = views.findViewById(R.id.open);
        btn_open.setOnClickListener(this);
        btn_openWith = views.findViewById(R.id.open_with);
        btn_openWith.setOnClickListener(this);
        View btn_share = views.findViewById(R.id.share);
        btn_share.setOnClickListener(this);
        btn_share.setOnLongClickListener(this);


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

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.share:
                copyToClipboard();
                break;
            default:
                return false;
        }
        return true;
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
            btn_open.setText(R.string.mOpen_noapps);
            btn_open.setEnabled(false);
            btn_openWith.setVisibility(View.GONE);
            return;
        }

        // sort
        lastOpened.sort(packages);

        // set
        btn_open.setText(getActivity().getString(R.string.mOpen_with, PackageUtilities.getPackageName(packages.get(0), getActivity())));
        btn_open.setEnabled(true);
        menu.clear();
        if (packages.size() == 1) {
            btn_openWith.setVisibility(View.GONE);
        } else {
            btn_openWith.setVisibility(View.VISIBLE);
            for (int i = 1; i < packages.size(); i++) {
                menu.add(Menu.NONE, i, i, getActivity().getString(R.string.mOpen_with, PackageUtilities.getPackageName(packages.get(i), getActivity())));
            }
        }

    }

    // ------------------- Buttons -------------------

    /**
     * Open url in a specific app
     *
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
        Intent shareIntent = Intent.createChooser(sendIntent, getActivity().getString(R.string.mOpen_share));
        getActivity().startActivity(shareIntent);
    }

    /**
     * Copy the url to the clipboard
     */
    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", getUrl());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), R.string.mOpen_clipboard, Toast.LENGTH_LONG).show();
        }
    }
}
