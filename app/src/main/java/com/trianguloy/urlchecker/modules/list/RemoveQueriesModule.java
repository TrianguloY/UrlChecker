package com.trianguloy.urlchecker.modules.list;

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
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.Inflater;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private LinearLayout box;

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
        remove = views.findViewById(R.id.button);
        remove.setText(R.string.mRemove_all);
        box = views.findViewById(R.id.box);

        // expand queries
        info.setOnClickListener(v -> {
            box.setVisibility(box.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            updateMoreIndicator();
        });

        remove.setOnClickListener(this);
    }

    @Override
    public void onNewUrl(UrlData urlData) {
        // initialize
        box.removeAllViews();

        // parse
        UrlParts parts = new UrlParts(urlData.url);

        if (parts.getQueries() == 0) {
            // no queries present, nothing to notify
            info.setText(R.string.mRemove_noQueries);
            remove.setEnabled(false); // disable the remove button
        } else {
            // queries present, notify
            info.setText(parts.getQueries() == 1
                    ? getActivity().getString(R.string.mRemove_found1) // 1 query
                    : getActivity().getString(R.string.mRemove_found, parts.getQueries()) // 2+ queries
            );
            AndroidUtils.setAsClickable(info);
            remove.setEnabled(true); // enable the remove all button

            // for each query, create a button
            for (int i = 0; i < parts.getQueries(); i++) {
                View button_text = Inflater.inflate(R.layout.button_text, box, getActivity());
                button_text.setTag(i); // to mark the query this button/text must act on

                // button that removes the query
                Button button = button_text.findViewById(R.id.button);
                String queryName = parts.getQueryName(i);
                button.setText(queryName.isEmpty()
                        // if no name
                        ? getActivity().getString(R.string.mRemove_empty)
                        // with name
                        : getActivity().getString(R.string.mRemove_one, queryName)
                );
                button.setOnClickListener(this);

                // text that displays the query value and sets it
                TextView text = button_text.findViewById(R.id.text);
                text.setText(parts.getQueryValue(i));
                AndroidUtils.setAsClickable(text);
                text.setOnClickListener(this);
            }
        }

        // update
        updateMoreIndicator();
    }

    @Override
    public void onClick(View v) {
        UrlParts parts = new UrlParts(getUrl());
        Integer tag = (Integer) ((View) v.getParent()).getTag();

        if (v instanceof Button) {
            // remove all queries (no tag) or a specific one
            parts.removeQuery(tag == null ? -1 : tag);
            // join and set
            setUrl(parts.getUrl());
        } else if (v instanceof TextView) {
            // replace the query url
            setUrl(parts.getQueryValue(tag));
        } else {
            AndroidUtils.assertError("Invalid view " + v.getClass().getName());
        }


    }

    /**
     * Sets the 'more' indicator.
     */
    private void updateMoreIndicator() {
        AndroidUtils.setStartDrawables(info,
                box.getChildCount() == 0 ? 0
                        : box.getVisibility() == View.VISIBLE ? R.drawable.arrow_down
                        : R.drawable.arrow_right);
    }

    /**
     * Manages the splitting, removing and merging of queries
     */
    private static class UrlParts {
        private final String preQuery; // "http://google.com"
        private final List<String> queries = new ArrayList<>(); // ["ref=foo","bar"]
        private final String postQuery; // "#start"

        /**
         * Prepares a url and extracts its queries
         */
        public UrlParts(String url) {
            // an uri is defined as [scheme:][//authority][path][?query][#fragment]
            // we need to find a '?' followed by anything except a '#'
            // this allows us to work with any string, even with non-standard or malformed uris
            int iStart = url.indexOf("?"); // position of '?' (-1 if not present)
            int iEnd = url.indexOf("#", iStart + 1);
            iEnd = iEnd == -1 ? url.length() : iEnd; // position of '#' (end of string if not present)

            // add part until '?' or until postQuery if not present
            preQuery = url.substring(0, iStart != -1 ? iStart : iEnd);
            // add queries if any
            if (iStart != -1) {
                queries.addAll(splitFix(url.substring(iStart + 1, iEnd), "&"));
            }
            // add part after queries (empty if not present)
            postQuery = url.substring(iEnd);
        }

        /**
         * Joins the url back into a full string
         */
        public String getUrl() {
            StringBuilder sb = new StringBuilder(preQuery);
            // first query after '?', the rest after '&'
            for (int i = 0; i < queries.size(); ++i)
                sb.append(i == 0 ? "?" : "&").append(queries.get(i));
            sb.append(postQuery);
            return sb.toString();
        }

        /**
         * returns the number of queries present
         */
        public int getQueries() {
            return queries.size();
        }

        /**
         * Returns the name of the query
         */
        public String getQueryName(int i) {
            return queries.get(i).split("=")[0];
        }

        /**
         * Returns the decoded value of the query
         */
        public String getQueryValue(int i) {
            String[] split = queries.get(i).split("=");
            if (split.length == 1) return "";
            try {
                return URLDecoder.decode(split[1]);
            } catch (Exception e) {
                // can't decode, return it directly
                return split[1];
            }
        }

        /**
         * Removes a query by its index i, or all if -1
         */
        public void removeQuery(int i) {
            if (i == -1) {
                // remove all queries
                queries.clear();
            } else {
                // remove that query
                queries.remove(i);
            }
        }
    }

    /**
     * {@link String#split(String)} won't return the last element if it's empty.
     * This function does. And it returns a list instead of an array.
     * Everything else is the same.
     * Note: regexp must not match '#', it does change this function
     */
    private static List<String> splitFix(String string, String regexp) {
        // split with an extra char
        String[] parts = (string + "#").split(regexp);
        // remove the extra char
        parts[parts.length - 1] = parts[parts.length - 1].substring(0, parts[parts.length - 1].length() - 1);
        return Arrays.asList(parts);
    }
}
