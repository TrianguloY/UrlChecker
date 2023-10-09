package com.trianguloy.urlchecker.modules.list;

import android.view.View;
import android.widget.ImageView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;

/**
 * A special module that manages the drawer functionality
 */
public class DrawerModule extends AModuleData {
    @Override
    public String getId() {
        return "drawer";
    }

    @Override
    public int getName() {
        return R.string.mDrawer_name;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new DrawerDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new DescriptionConfig(R.string.mDrawer_desc);
    }
}

class DrawerDialog extends AModuleDialog {
    private ImageView buttonL;
    private ImageView buttonR;
    private final MainDialog dialog;

    public DrawerDialog(MainDialog dialog) {
        super(dialog);
        this.dialog = dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_drawer;
    }

    @Override
    public void onInitialize(View views) {
        buttonL = views.findViewById(R.id.drawerL);
        buttonR = views.findViewById(R.id.drawerR);
        var parent = views.findViewById(R.id.parent);
        parent.getBackground().setAlpha(25);

        AndroidUtils.toggleableListener(parent, v -> dialog.toggleDrawer(), v -> {
            buttonL.setImageResource(dialog.isDrawerVisible() ?
                    R.drawable.arrow_down : R.drawable.arrow_right);
            buttonR.setImageResource(dialog.isDrawerVisible() ?
                    R.drawable.arrow_down : R.drawable.arrow_right);
        });
    }

    @Override
    public void onFinishUrl(UrlData urlData) {
        setVisibility(dialog.anyDrawerChildVisible());
    }

}