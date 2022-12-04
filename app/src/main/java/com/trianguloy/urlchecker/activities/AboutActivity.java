package com.trianguloy.urlchecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trianguloy.urlchecker.BuildConfig;
import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.utilities.AndroidSettings;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.Inflater;
import com.trianguloy.urlchecker.utilities.PackageUtils;

import java.util.List;

public class AboutActivity extends Activity {

    // ------------------- links -------------------

    private static final List<Pair<Integer, String>> LINKS = List.of(
            Pair.create(R.string.link_changelog, "https://github.com/TrianguloY/UrlChecker/blob/master/app/src/main/play/release-notes/en-US/default.txt"), // TODO: link to the correct translation
            Pair.create(R.string.link_source, "https://github.com/TrianguloY/UrlChecker"),
            Pair.create(R.string.link_privacy, "https://github.com/TrianguloY/UrlChecker/blob/master/docs/PRIVACY%20POLICY.md"),
            Pair.create(R.string.lnk_fDroid, "https://f-droid.org/packages/{package}"),
            Pair.create(R.string.lnk_playStore, "https://play.google.com/store/apps/details?id={package}"),
            Pair.create(R.string.link_blog, "https://triangularapps.blogspot.com/")
    );

    // ------------------- listeners -------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        setContentView(R.layout.activity_about);
        setTitle(R.string.a_about);
        AndroidUtils.configureUp(this);

        // on release, append version to the action bar title
        if (!BuildConfig.DEBUG) {
            setTitle(getTitle() + " (V" + BuildConfig.VERSION_NAME + ")");
        }

        // fill contributors and translators
        this.<TextView>findViewById(R.id.txt_about).setText(
                getString(R.string.txt_about,
                        getString(R.string.contributors),
                        getString(R.string.translators)
                )
        );

        // create links
        ViewGroup v_links = findViewById(R.id.links);
        for (var link : LINKS) {
            var v_link = Inflater.<TextView>inflate(R.layout.about_link, v_links, this);
            v_link.setText(link.first);
            AndroidUtils.setAsClickable(v_link);
            v_link.setTag(link.second.replace("{package}", getPackageName()));
            v_link.setOnClickListener(this::onClick);
            v_link.setOnLongClickListener(this::onLongClick);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // press the 'back' button in the action bar to go back
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Link clicked
     */
    public void onClick(View view) {
        open(((String) view.getTag()));
    }

    /**
     * Link long-clicked
     */
    public boolean onLongClick(View view) {
        share(((String) view.getTag()));
        return true;
    }

    // ------------------- actions -------------------

    /**
     * Open an url in the browser
     */
    private void open(String url) {
        PackageUtils.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), R.string.toast_noBrowser, this);
    }

    /**
     * Share an url as plain text
     */
    private void share(String url) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide what to do with it.
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url);

        PackageUtils.startActivity(Intent.createChooser(share, getString(R.string.share)), R.string.toast_noApp, this);
    }
}