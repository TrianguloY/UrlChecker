package com.trianguloy.urlchecker.utilities.wrappers;

import android.view.View;
import com.trianguloy.urlchecker.utilities.methods.MovableButtonUpdater;

public class UpButtonUpdater implements MovableButtonUpdater {
    @Override
    public void updateButton(View button, int index, int listSize) {
        button.setEnabled(index > 0);
        button.setAlpha(index > 0 ? 1 : 0.5f);
    }
}