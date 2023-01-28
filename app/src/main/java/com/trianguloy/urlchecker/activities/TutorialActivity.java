package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.GenericPref;
import com.trianguloy.urlchecker.utilities.PackageUtils;

import java.util.Locale;

public class TutorialActivity extends Activity {

    private Button prevButton;
    private Button nextButton;
    private GenericPref.Bool tutorialDone;
    private ViewFlipper flipper;
    private TextView pageIndexText;

    public static GenericPref.Bool DONE(Context cntx) {
        return new GenericPref.Bool("tutorial_done", false, cntx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        AndroidSettings.setLocale(this);
        setContentView(R.layout.activity_tutorial);
        setTitle(R.string.tutorial);

        tutorialDone = DONE(this);

        flipper = findViewById(R.id.flipper);
        prevButton = findViewById(R.id.bBack);
        nextButton = findViewById(R.id.bNext);
        pageIndexText = findViewById(R.id.pageIndex);

        updateButtons();

    }

    /* ------------------- buttons ------------------- */

    @Override
    public void onBackPressed() {
        prev(null);
    }

    public void prev(View view) {
        if (flipper.getDisplayedChild() == 0) {
            // first page, exit
            exit();
        } else {
            // show prev
            flipper.showPrevious();
            updateButtons();
        }
    }

    public void next(View view) {
        if (flipper.getDisplayedChild() == flipper.getChildCount() - 1) {
            // last page, exit
            exit();
        } else {
            // show next
            flipper.showNext();
            updateButtons();
        }
    }

    /* ------------------- actions ------------------- */

    /**
     * Updates the buttons and index texts
     */
    private void updateButtons() {
        int current = flipper.getDisplayedChild();
        int max = flipper.getChildCount();

        prevButton.setText(current == 0 ? R.string.tutorial_button_skip : R.string.back);
        nextButton.setText(current != max - 1 ? R.string.next : R.string.tutorial_button_end);

        pageIndexText.setText(String.format(Locale.getDefault(), "%d/%d", current + 1, max));
    }

    /**
     * Marks the tutorial as completed and exits
     */
    private void exit() {
        tutorialDone.set(true);
        this.finish();
    }


    // TODO: replace with 'fragment' listener
    public void openModulesActivity(View view) {
        PackageUtils.startActivity(new Intent(this, ModulesActivity.class), R.string.toast_noApp, this);
    }
}
