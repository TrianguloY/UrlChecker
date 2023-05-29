package com.trianguloy.urlchecker.modules.list;

import android.net.Uri;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trianguloy.urlchecker.R;
import com.trianguloy.urlchecker.activities.ModulesActivity;
import com.trianguloy.urlchecker.dialogs.MainDialog;
import com.trianguloy.urlchecker.modules.AModuleConfig;
import com.trianguloy.urlchecker.modules.AModuleData;
import com.trianguloy.urlchecker.modules.AModuleDialog;
import com.trianguloy.urlchecker.modules.DescriptionConfig;
import com.trianguloy.urlchecker.url.UrlData;
import com.trianguloy.urlchecker.utilities.AndroidUtils;
import com.trianguloy.urlchecker.utilities.Inflater;
import com.trianguloy.urlchecker.utilities.JavaUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This module shows all parts of the url decoded
 */
public class UriPartsModule extends AModuleData {

    @Override
    public String getId() {
        return "uriparts";
    }

    @Override
    public int getName() {
        return R.string.mParts_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new UriPartsDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new DescriptionConfig(R.string.mParts_desc);
    }
}

class UriPartsDialog extends AModuleDialog {

    private LinearLayout box;
    private final List<String> expandedGroups = new ArrayList<>();

    public UriPartsDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_parts;
    }

    @Override
    public void onInitialize(View views) {
        box = views.findViewById(R.id.box);
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        // clear
        box.removeAllViews();

        // parse
        var uri = Uri.parse(urlData.url);

        // domain elements
        if (uri.getAuthority() != null || uri.getScheme() != null) {
            var domain = addGroup("Domain", -1, null);
            addPart("scheme", uri.getScheme(), domain, null);
            addPart("user info", uri.getUserInfo(), domain, null);
            addPart("host", uri.getHost(), domain, null);
            addPart("port", uri.getPort() != -1 ? String.valueOf(uri.getPort()) : null, domain, null);
        }

        // paths
        var pathSegments = uri.getPathSegments();
        if (pathSegments.size() > 0) {
            var paths = addGroup("Paths", pathSegments.size(), uri.buildUpon().path(null));
            for (var i = 0; i < pathSegments.size(); i++) {
                var pathSegment = pathSegments.get(i);

                var builder = uri.buildUpon();
                builder.path(null);
                for (int newI = 0; newI < pathSegments.size(); newI++) {
                    if (newI != i) builder.appendPath(pathSegments.get(newI));
                }
                addPart("/", pathSegment, paths, builder);
            }
        }

        // queries
        var queryParts = getQueryParts(uri);
        if (queryParts.size() > 0) {
            var queries = addGroup("Queries", queryParts.size(), uri.buildUpon().query(null));
            for (var queryPart : queryParts) {
                var builder = uri.buildUpon();
                builder.query(null);
                for (var newQuerypart : queryParts) {
                    if (!Objects.equals(newQuerypart, queryPart))
                        builder.appendQueryParameter(newQuerypart.first, newQuerypart.second);

                }
                addPart(queryPart.first, queryPart.second, queries, builder);
            }
        }

        // fragment
        if (uri.getFragment() != null) {
            var fragment = addGroup("Fragment", -1, uri.buildUpon().fragment(null));
            addPart("#", uri.getFragment(), fragment, null);
        }

        setVisibility(box.getChildCount() > 0);
    }

    /**
     * Adds a collapsible group
     */
    private LinearLayout addGroup(String name, int size, Uri.Builder onDelete) {
        var title = Inflater.inflate(R.layout.uri_part, box);
        title.findViewById(R.id.key).setVisibility(View.GONE);

        var name_view = title.<TextView>findViewById(R.id.value);
        name_view.setText(name + (size <= -1 ? "" : " (" + size + ")"));
        AndroidUtils.setAsClickable(name_view);

        var delete_view = title.<Button>findViewById(R.id.delete);
        if (onDelete == null) delete_view.setVisibility(View.GONE);
        else delete_view.setOnClickListener(v -> setUrl(onDelete.build().toString()));

        var group = Inflater.<LinearLayout>inflate(R.layout.dialog_parts, box);
        group.setVisibility(expandedGroups.contains(name) ? View.VISIBLE : View.GONE);
        AndroidUtils.toggleableListener(
                title,
                v -> JavaUtils.toggleContains(expandedGroups, name),
                v -> {
                    group.setVisibility(expandedGroups.contains(name) ? View.VISIBLE : View.GONE);
                    AndroidUtils.setStartDrawables(name_view,
                            expandedGroups.contains(name) ? R.drawable.arrow_down : R.drawable.arrow_right
                    );
                }
        );

        return group;
    }

    /**
     * Adds a part
     */
    private void addPart(String name, String value, LinearLayout container, Uri.Builder onDelete) {
        if (value == null) return;
        var part = Inflater.inflate(R.layout.uri_part, container);

        part.<TextView>findViewById(R.id.key).setText(name);
        var value_view = part.<TextView>findViewById(R.id.value);
        value_view.setText(value);
        AndroidUtils.setAsClickable(value_view);
        value_view.setOnClickListener(v -> setUrl(value));

        var delete_view = part.<Button>findViewById(R.id.delete);
        if (onDelete != null) {
            try {
                var newUrl = onDelete.build().toString();
                delete_view.setOnClickListener(v -> setUrl(newUrl));
            } catch (UnsupportedOperationException ignored) {
                delete_view.setVisibility(View.GONE);
            }
        } else {
            delete_view.setVisibility(View.GONE);
        }
    }

    /**
     * Gets the queries, in order (except for same-name ones)
     */
    private List<Pair<String, String>> getQueryParts(Uri uri) {
        try {
            var queries = new ArrayList<Pair<String, String>>();
            for (var name : uri.getQueryParameterNames()) {
                for (var value : uri.getQueryParameters(name)) {
                    queries.add(Pair.create(name, value));
                }
            }
            return queries;
        } catch (UnsupportedOperationException e) {
            return Collections.emptyList();
        }
    }

}
