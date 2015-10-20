package com.locname.distribution;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.locname.distribution.model.TaskItem;

import java.util.List;

/**
 * Created by Mostafa on 9/26/2015.
 */
public class CustomTask extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<TaskItem> mTasks;

    public CustomTask(Context context, List<TaskItem> tasks) {
        mInflater = LayoutInflater.from(context);
        mTasks = tasks;
    }

    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public Object getItem(int position) {
        return mTasks.get(position);
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
            holder.task_name = (TextView)view.findViewById(R.id.name);
            holder.details = (TextView)view.findViewById(R.id.latest_message);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        TaskItem task = mTasks.get(position);
        holder.task_name.setText(task.getTask_name());
        holder.details.setText(task.getTask_details());

        return view;
    }

    private class ViewHolder {

        public TextView task_name, details;
    }
}