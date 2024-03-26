package com.trianguloy.urlchecker.utilities.wrappers;

import android.view.View;
import com.trianguloy.urlchecker.utilities.methods.MovableButtonUpdater;


public class DownButtonUpdater implements MovableButtonUpdater {
    @Override
    public void updateButton(View button, int index, int listSize) {
        button.setEnabled(index < listSize - 1);
        button.setAlpha(index < listSize - 1 ? 1 : 0.5f);
    }
}
