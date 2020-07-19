package com.trianguloy.urlchecker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_debug:
                openGoogle();
                break;
        }
    }

    private void openGoogle() {
        //startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")), "Open"));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
    }
}
