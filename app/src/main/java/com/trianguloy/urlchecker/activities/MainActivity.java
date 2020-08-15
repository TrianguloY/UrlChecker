package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.trianguloy.urlchecker.R;

/**
 * The activity to show when clicking the desktop shortcut (when 'opening' the app)
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    /**
     * ButtonClick
     *
     * @param view which button
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_debug:
                openGoogle();
                break;
            case R.id.btn_config:
                startActivity(new Intent(this, ConfigActivity.class));
                break;
        }
    }

    private void openGoogle() {
        //startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")), "Open"));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
    }
}
