package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.PackageUtilities;

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
        Class cls;
        switch (view.getId()) {
            case R.id.setup:
                cls = ConfigActivity.class;
                break;
            case R.id.about:
                cls = AboutActivity.class;
                break;
            case R.id.txt_sample:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/"));
                i.setPackage(getPackageName());
                PackageUtilities.startActivity(i, R.string.toast_error, this);
                return;
            case R.id.m_img_icon:
                Toast.makeText(this, getString(R.string.app_name) + ", by TrianguloY", Toast.LENGTH_SHORT).show();
                return;
            default:
                Log.d("SWITCH", view.toString());
                if (BuildConfig.DEBUG)
                    Toast.makeText(this, "Unknown view: " + view, Toast.LENGTH_LONG).show();
                return;
        }
        PackageUtilities.startActivity(new Intent(this, cls), R.string.toast_error, this);
    }

}
