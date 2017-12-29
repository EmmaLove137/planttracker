package com.nonsense.planttracker.tracker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.tracker.impl.GenericRecord;
import com.nonsense.planttracker.tracker.impl.Plant;
import com.nonsense.planttracker.tracker.impl.Recordable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantRecordableTileArrayAdapter extends ArrayAdapter<GenericRecord> {

    private int viewResourceId;
    private Plant currentPlant;

    public PlantRecordableTileArrayAdapter(Context context, int textViewResourceId, Plant plant) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        currentPlant = plant;
    }

    public PlantRecordableTileArrayAdapter(Context context, int resource, List<GenericRecord> items,
                                           Plant plant) {
        super(context, resource, items);
        viewResourceId = resource;
        currentPlant = plant;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(viewResourceId, null);
        }

        Calendar plantStartDate = currentPlant.getPlantStartDate();
        //Calendar flowerStartDate = currentPlant.getFlowerStartDate();

        GenericRecord p = getItem(position);
        long growWeekCount = 96969696;//FIXME

        String weekDisplay = "";
        weekDisplay = "(W" + growWeekCount + ") ";

        if (p != null) {
            TextView eventTypeTextView = (TextView)v.findViewById(R.id.observEventTypeTextView);
            eventTypeTextView.setText(p.displayName);

            TextView recordableSummaryTextView = (TextView)v.findViewById(
                    R.id.recordableSummaryTextView);
            recordableSummaryTextView.setText(p.getSummary());

            TextView dateTextView = (TextView)v.findViewById(R.id.dateTextView);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
            dateTextView.setText(sdf.format(p.time.getTime()) + " [Wk.3/6]");
        }

        return v;
    }


}
