package com.trianguloy.urlchecker.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.role.RoleManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;



import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.PackageUtils;

public class BrowserButtonsFragment extends Fragment {
    public static final int REQUEST_CODE = 2;
    private RoleManager roleManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser_buttons, container, false);
        configureBrowserButtons(view);
        return view;
    }

    // adapted from https://stackoverflow.com/a/74108806

    /**
     * hide buttons if not available
     */
    private void configureBrowserButtons(View view) {
        Button b1 = view.findViewById(R.id.b1);
        Button b2 = view.findViewById(R.id.b2);
        Button b3 = view.findViewById(R.id.b3);
        Button b4 = view.findViewById(R.id.b4);

        boolean hide = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager = getActivity().getSystemService(RoleManager.class);
            if (roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                hide = false;
            }
        }

        if (hide) b1.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            b2.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            b3.setVisibility(View.GONE);

        b1.setOnClickListener(v -> chooseBrowserPopup());
        b2.setOnClickListener(v -> openBrowserSettings());
        b3.setOnClickListener(v -> openAppLinks());
        b4.setOnClickListener(v -> openAppDetails());
    }


    /**
     * open a specific dialog to choose the browser
     */
    @TargetApi(Build.VERSION_CODES.Q)
    public void chooseBrowserPopup() {
        startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER), REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(getActivity(), R.string.toast_defaultSet, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), R.string.canceled, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Open android settings about the default browser
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void openBrowserSettings() {
        // open the settings
        Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
        intent.putExtra(
                ":settings:fragment_args_key",
                "default_browser"
        );
        Bundle bundle = new Bundle();
        bundle.putString(":settings:fragment_args_key", "default_browser");
        intent.putExtra(
                ":settings:show_fragment_args",
                bundle
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageUtils.startActivity(intent, R.string.toast_noApp, getActivity());
    }

    // adapted from https://groups.google.com/g/androidscript/c/cLq7eiUVpig/m/RDraxFYQCgAJ

    /**
     * Open the android app settings to open links as default
     */
    @TargetApi(Build.VERSION_CODES.S)
    public void openAppLinks() {
        PackageUtils.startActivity(new Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                Uri.parse("package:" + getActivity().getPackageName())
        ), R.string.toast_noApp, getActivity());
    }

    /**
     * Open the android app settings
     */
    public void openAppDetails() {
        PackageUtils.startActivity(new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getActivity().getPackageName())
        ), R.string.toast_noApp, getActivity());
    }


}
