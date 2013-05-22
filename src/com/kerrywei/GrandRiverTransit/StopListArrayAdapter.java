package com.kerrywei.GrandRiverTransit;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class StopListArrayAdapter extends ArrayAdapter<StopListEntry> {
    final String DEBUG = "GRT Assistant DEBUG";
    
    Activity context;
    private final List<StopListEntry> list;
    boolean checkUncheckAll = false;

    public StopListArrayAdapter(Activity context, List<StopListEntry> list) {
        super(context, R.layout.stops_list, list);
        this.context = context;
        this.list = list;
    }
    
    static class ViewHolder {
        protected TextView stopIdTextView;
        protected TextView stopNameTextView;
        protected ImageView stopIcon;
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) { // no view for reuse
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.stops_list_with_start_and_end, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.stopIdTextView = (TextView) view.findViewById(R.id.StopListWithStartAndEnd_topText);
            viewHolder.stopNameTextView = (TextView) view.findViewById(R.id.StopListWithStartAndEnd_bottomText);
            viewHolder.stopIcon = (ImageView) view.findViewById(R.id.StopListWithStartAndEnd_busIcon);
            view.setTag(viewHolder);
            viewHolder.stopIcon.setTag(list.get(position));
        } else { // the view is available for reuse
            view = convertView;
            ((ViewHolder) view.getTag()).stopIcon.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        
        String text = list.get(position).getStopID();
        holder.stopIdTextView.setText(text);
        text = list.get(position).getStopName();
        holder.stopNameTextView.setText(text);
        
        if (list.get(position).isStartStop()) {
            holder.stopIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.bus_start));
        } else if (list.get(position).isEndStop()) {
            holder.stopIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.bus_end));
        } else {
            holder.stopIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.bus_grey));
        }
        
        
        return view;
    }

}
