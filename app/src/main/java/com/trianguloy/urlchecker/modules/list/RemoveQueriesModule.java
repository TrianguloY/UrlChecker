package com.trianguloy.urlchecker.modules.list;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private TextView more;
    private LinearLayout box;

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
        more = views.findViewById(R.id.more);
        box = views.findViewById(R.id.box);

        // expand queries
        more.setOnClickListener(v -> {
            boolean checked = box.getVisibility() == View.GONE;
            box.setVisibility(checked ? View.VISIBLE : View.GONE);
            more.setCompoundDrawablesWithIntrinsicBounds(checked ? R.drawable.expanded : R.drawable.collapsed, 0, 0, 0);
        });
        more.performClick(); // initial hide

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

        // remove previously generated buttons
        box.removeAllViews();

        if (!cleared.equals(url)) {
            // query present, notify
            remove.setEnabled(true);
            more.setEnabled(true);
            info.setText(R.string.mRemove_found);
            info.setBackgroundColor(getActivity().getResources().getColor(R.color.warning));

            // extract queries
            String[] queriesArray = extractQueries(url);

            // create a button for each query
            // if multiple query keys are equal, multiple buttons will be created, however these
            // buttons will individually address each one in the order they are found
            for (int i = 0; i < queriesArray.length; i++) {
                Button queryRemover = new Button(getActivity());
                // when clicked, the query will be removed
                queryRemover.setTag(i);
                queryRemover.setOnClickListener(v -> removeQuery((Integer) v.getTag()));
                // text will be the query key (and on one line only)
                queryRemover.setSingleLine(true);
                queryRemover.setEllipsize(TextUtils.TruncateAt.END);
                queryRemover.setText(
                        getActivity().getString(R.string.mRemove_one, queriesArray[i].split("=")[0])
                );
                box.addView(queryRemover);
            }
        } else {
            // no query present, nothing to notify
            remove.setEnabled(false);

            // not pretty, but this sets 'more' on collapsed
            box.setVisibility(View.VISIBLE);
            more.performClick();

            more.setEnabled(false);
            info.setText(R.string.mRemove_noQueries);
            info.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
        }
    }

    @Override
    public void onClick(View v) {
        // pressed the apply button
        if (cleared != null) setUrl(cleared);
    }

    /**
     * Removes the query at index n
     */
    private void removeQuery(int n) {
        if (cleared == null) return;

        String oldUrl = getUrl();
        String[] queriesArray = extractQueries(oldUrl);
        // copy everything from previous url, except queries and fragment
        StringBuilder newUrl = new StringBuilder(oldUrl.substring(0, oldUrl.indexOf("?")));

        // to later check if we need to use '?' or '&'
        boolean firstQuery = true;

        for (int i = 0; i < queriesArray.length; i++) {
            // skip query to remove
            if (i != n) {
                if (firstQuery) {
                    newUrl.append('?');
                    firstQuery = false;
                } else {
                    newUrl.append('&');
                }
                newUrl.append(queriesArray[i]);
            }
        }

        // add fragment
        if (oldUrl.contains("#")) {
            newUrl.append(oldUrl.substring(oldUrl.indexOf("#")));
        }
        setUrl(newUrl.toString());
    }

    /**
     * Returns a list of all queries from a given url
     */
    private String[] extractQueries(String url) {
        int start = url.indexOf("?") + 1;
        int end = url.indexOf("#");
        end = end == -1 ? url.length() : end;

        // failsafe in case the url has a '#' before a '?'
        if (start > end) return new String[0];

        String queriesString = url.substring(start, end);
        return queriesString.split("&");
    }
}
