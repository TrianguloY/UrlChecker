package com.trianguloy.urlchecker.modules.list;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ConfigActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;

// Importing required classes

import java.net.URL;
import java.net.MalformedURLException;



public class RemoveQueriesModule extends AModuleData {

    @Override
    public String getId() {
        return "removeQueries";
    }

    @Override
    public int getName() {
        return R.string.mRemove_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new RemoveQueriesDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ConfigActivity cntx) {
        return new DescriptionConfig(R.string.mRemove_desc);
    }
}

class RemoveQueriesDialog extends AModuleDialog implements View.OnClickListener {

    private TextView info;
    private Button remove;

    private String cleared = null;

    public RemoveQueriesDialog(MainDialog dialog) { super(dialog); }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_removequeries;
    }

    @Override
    public void onInitialize(View views) {
        info = views.findViewById(R.id.text);
        remove = views.findViewById(R.id.fix);
        remove.setOnClickListener(this);
    }

    @Override
    public void onNewUrl(String url) {
        info.setText("");
        cleared = url;
        remove.setEnabled(false);
        URL urlObject = null;
        try {
            urlObject = new URL(url);

            //retrieve all components
            String protocol = urlObject.getProtocol();
            protocol = protocol + ":";
            String authority = urlObject.getAuthority();
            authority = authority != null ? "//" + authority : "";
            String path = urlObject.getPath();
            String ref = urlObject.getRef();
            ref = ref != null ? "#" + ref : "";

            //create the url but without queries
            cleared = protocol + authority + path + ref;
        } catch (MalformedURLException e) {

        }

        // url changed, enable button
        if (urlObject != null && urlObject.getQuery() != null) {
            remove.setEnabled(true);
            info.setText(R.string.mRemove_found);
            setColor(R.color.warning);
        }

        // nothing found
        if (info.getText().length() == 0) {
            info.setText(R.string.mRemove_noQueries);
            setColor(R.color.transparent);
        }
    }

    @Override
    public void onClick(View v) {
        // pressed the fix button
        if (cleared != null) setUrl(cleared);
    }

    // ------------------- utils -------------------


    /**
     * Utility to set the info background color. Manages color importance
     */
    private void setColor(int color) {
        if (info.getTag() != null && info.getTag().equals(R.color.bad) && color == R.color.warning) return; // keep bad instead of replacing with warning
        info.setTag(color);
        info.setBackgroundColor(getActivity().getResources().getColor(color));
    }

}
