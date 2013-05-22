package com.kerrywei.GrandRiverTransit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class DirectionList extends ListActivity {
    
    final String DEBUG = "GRT Assistant DEBUG";
    final int VIEW_STOP_LIST = 0;
    String routeID = null;
    NewDatabaseAdapter databaseAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseAdapter = Utilities.getNewDatabaseAdapter();
        
        // get extras:
        Bundle extras = getIntent().getExtras();
        routeID = (savedInstanceState == null) ? null : (String) savedInstanceState.getSerializable(NewDatabaseAdapter.ROUTE_ID);
        if (extras != null) {
            routeID = extras.getString(NewDatabaseAdapter.ROUTE_ID);
        }
        assert(routeID != null);
        
        fillData();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        /*
        // get routeID based on _id:
        String headSign = null;
        Cursor c = databaseAdapter.getTripHeadsignFromTripsBasedOnRowID(id);
        if (c.moveToFirst()) {
            headSign = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_HEADSIGN));
        }
        c.close();
        */
        
        String headSign = null;
        int tripID = 0;
        HashMap<String,String> entry = (HashMap<String,String>) getListView().getItemAtPosition(position);
        headSign = entry.get("headsign");
        tripID = Integer.valueOf(entry.get("tripID"));
        
        Intent i = new Intent(this, StopsList.class);
        i.putExtra(NewDatabaseAdapter.ROUTE_ID, routeID);
        i.putExtra(NewDatabaseAdapter.TRIP_HEADSIGN, headSign);
        i.putExtra(NewDatabaseAdapter.TRIP_ID, tripID);
        
        startActivityForResult(i, VIEW_STOP_LIST);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case VIEW_STOP_LIST:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            default:
                break;
        }
    }
    
    private void fillData() {
        ArrayList<Map<String,String>> stopList = new ArrayList<Map<String,String>>(); 
        
        Cursor cursor = databaseAdapter.getGRTDirections(routeID);
        String headsign = null;
        int tripID = 0;
        if (cursor.moveToFirst()) {
            do {
                HashMap<String,String> stopListEntry = new HashMap<String,String>();
                headsign = cursor.getString(cursor.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_HEADSIGN));
                stopListEntry.put("headsign", headsign);
                
                Cursor c = databaseAdapter.getLongestStopListOfADirection(routeID, headsign);
                if (c.moveToFirst()) {
                    tripID = c.getInt(c.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_ID));
                }
                c.close();
                
                // get names of the first and last stop of the trip:
                String firstStopName = databaseAdapter.getNameOfTheFirstStopOfATrip(tripID, routeID);
                String lastStopName = databaseAdapter.getNameOfTheLastStopOfATrip(tripID, routeID);
                
                stopListEntry.put("firstStopName", firstStopName);
                stopListEntry.put("lastStopName", lastStopName);
                stopListEntry.put("tripID", String.valueOf(tripID));
                
                stopList.add(stopListEntry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                stopList,
                R.layout.direction_list_entry,
                new String[] {"headsign", "firstStopName", "lastStopName"},
                new int[] {R.id.DirectionList_direction, R.id.DirectionList_startStop, R.id.DirectionList_endStop}
            );
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }
}
