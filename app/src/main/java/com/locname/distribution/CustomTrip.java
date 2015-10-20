package com.locname.distribution;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.locname.distribution.model.TripItem;

import java.util.List;

public class CustomTrip extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<TripItem> mTrips;

    public CustomTrip(Context context, List<TripItem> trips) {
        mInflater = LayoutInflater.from(context);
        mTrips = trips;
    }

    @Override
    public int getCount() {
        return mTrips.size();
    }

    @Override
    public Object getItem(int position) {
        return mTrips.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.row_layout, parent, false);
            holder = new ViewHolder();
            holder.trip_name = (TextView)view.findViewById(R.id.name);
            holder.description = (TextView)view.findViewById(R.id.latest_message);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        TripItem trip = mTrips.get(position);
        holder.trip_name.setText(trip.getTrip_name());
        holder.description.setText(trip.getTrip_description());

        return view;
    }

    private class ViewHolder {
        public ImageView avatar;
        public TextView trip_name, description;
    }
}