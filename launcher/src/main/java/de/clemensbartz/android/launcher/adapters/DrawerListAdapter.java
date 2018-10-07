/*
 * Copyright (C) 2017  Clemens Bartz
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.clemensbartz.android.launcher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.clemensbartz.android.launcher.R;
import de.clemensbartz.android.launcher.models.ApplicationModel;

/**
 * Array adapter for the drawer. Takes an @{ApplicationModel}.
 *
 * @author Clemens Bartz
 * @since 1.0
 */
public final class DrawerListAdapter extends ArrayAdapter<ApplicationModel> {

    /** The resource id. */
    private final int resource;

    /** The list of all application models. */
    private final List<ApplicationModel> unfilteredList;

    /**
     * Initializes a new adapter.
     * @param context the activity
     * @param objects the list of application models
     */
    public DrawerListAdapter(
            final Context context,
            final List<ApplicationModel> objects) {

        super(context, R.layout.drawer_item);
        this.resource = R.layout.drawer_item;
        unfilteredList = objects;
    }

    @Override
    public View getView(final int position,
                        final View convertView,
                        final ViewGroup parent) {

        ViewHolder viewHolder;
        View v = convertView;

        if (convertView == null) {
            v = LayoutInflater.from(getContext()).inflate(resource, null);

            viewHolder = new ViewHolder();
            viewHolder.icon = v.findViewById(R.id.icon);
            viewHolder.name = v.findViewById(R.id.name);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final ApplicationModel resolveInfo = getItem(position);

        if (resolveInfo != null && viewHolder != null) {
            viewHolder.icon.setContentDescription(resolveInfo.label);
            viewHolder.icon.setImageDrawable(resolveInfo.icon);
            viewHolder.name.setText(resolveInfo.label);
        }

        return v;
    }

    @Override
    public ApplicationModel getItem(final int position) {
        return unfilteredList.get(position);
    }

    @Override
    public int getCount() {
        return unfilteredList.size();
    }

    @Override
    public int getPosition(final ApplicationModel item) {
        return unfilteredList.indexOf(item);
    }

    @Override
    public boolean isEmpty() {
        return unfilteredList.isEmpty();
    }

    /**
     * View holder class.
     */
    private static class ViewHolder {
        /** The view for the icon. */
        private ImageView icon;
        /** The view for the label. */
        private TextView name;
    }
}
