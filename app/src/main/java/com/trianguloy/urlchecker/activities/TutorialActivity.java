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

public class TutorialActivity extends Activity {

    private Button backButton;
    private Button nextButton;
    private GenericPref.Bool tutorialDone;
    private ViewFlipper flipper;
    private TextView pageIndexText;

    public static GenericPref.Bool TUTORIAL(Context cntx) {
        return new GenericPref.Bool("tutorial_done", true, cntx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        AndroidSettings.setLocale(this);
        setContentView(R.layout.activity_tutorial);
        setTitle(R.string.tutorial);

        flipper = findViewById(R.id.flipper);
        
        backButton = findViewById(R.id.bBack);
        nextButton = findViewById(R.id.bNext);

        pageIndexText = findViewById(R.id.pageIndex);

        checkEnabled();

        tutorialDone = TUTORIAL(this);
    }


    // ------------------- buttons -------------------
    public void nextSlide(View view){
        flipper.showNext();
        checkEnabled();
    }

    public void previousSlide(View view){
        flipper.showPrevious();
        checkEnabled();
    }

    /**
     * Checks if the buttons should be enabled depending on the current page
     * If on last page "next" will be disabled, if on first page "back" will be disabled
     * Also sets index text
     */
    private void checkEnabled(){
        int current = flipper.getDisplayedChild();
        int max = flipper.getChildCount();

        backButton.setEnabled(current != 0);
        nextButton.setEnabled(current != max - 1);

        pageIndexText.setText(String.format("%d/%d", current + 1, max));
    }

    public void openModulesActivity(View view) {
        PackageUtils.startActivity(new Intent(this, ModulesActivity.class), R.string.toast_noApp, this);
    }

    public void finishTutorial(View view){
        tutorialDone.set(true);
        this.finish();
    }
}
