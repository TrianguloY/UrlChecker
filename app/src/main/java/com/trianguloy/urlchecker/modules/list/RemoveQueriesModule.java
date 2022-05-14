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

/**
 * This module removes queries "?foo=bar" from an url
 * Originally made by PabloOQ
 */
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

    public RemoveQueriesDialog(MainDialog dialog) {
        super(dialog);
    }

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
        // clear
        // an uri is defined as [scheme:][//authority][path][?query][#fragment]
        // in order to remove the query, we need to remove everything between the '?' (included) and the '#' if present (excluded)
        // we need to match a '?' followed by anything except a '#', and remove it
        // this allows us to work with any string, even with non-standard urls
        cleared = url.replaceAll("\\?[^#]*", "");

        if (!cleared.equals(url)) {
            // query present, notify
            remove.setEnabled(true);
            info.setText(R.string.mRemove_found);
            info.setBackgroundColor(getActivity().getResources().getColor(R.color.warning));
        } else {
            // no query present, nothing to notify
            remove.setEnabled(false);
            info.setText(R.string.mRemove_noQueries);
            info.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
        }
    }

    @Override
    public void onClick(View v) {
        // pressed the apply button
        if (cleared != null) setUrl(cleared);
    }

}
