package com.kerrywei.GrandRiverTransit;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class RouteMaker extends TabActivity {
    public static final String ROUTEID_SRC = "routeIDSrc";
    public static final String ROUTEID_FROM_ROUTES = "from_route_list";
    public static final String ROUTEID_FROM_CUST = "from_routes_from_stop";
    
    final String DEBUG = "GRT Assistant DEBUG";
    
    //Long route_rowID;
    String stopID = null;
    String routeID = null;
    
    //String routeIdOption = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_maker);
        
        // get route_rowID passed from RouteList:
        Bundle extras = getIntent().getExtras();
        
        //route_rowID = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(DatabaseAdapter.ROUTES_ROWID);
        //routeIdOption = (savedInstanceState == null) ? null : (String) savedInstanceState.getSerializable(RouteMaker.ROUTEID_SRC);
        routeID = (savedInstanceState == null) ? null : (String) savedInstanceState.getSerializable(NewDatabaseAdapter.ROUTE_ID);
        stopID = (savedInstanceState == null) ? null : (String) savedInstanceState.getSerializable(NewDatabaseAdapter.STOPS_ID);
        
        if (extras != null) {
            //route_rowID = extras.getLong(DatabaseAdapter.ROUTES_ROWID);
            //routeIdOption = extras.getString(RouteMaker.ROUTEID_SRC);
            routeID = extras.getString(NewDatabaseAdapter.ROUTE_ID);
            stopID = extras.getString(NewDatabaseAdapter.STOPS_ID);
        }
        assert(routeID != null);
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        // direction one trip_id:
        NewDatabaseAdapter databaseAdapter = Utilities.getNewDatabaseAdapter();
        long tripID_a = 0;
        int startStopID = 0;
        // debug:
        int stopCount = 0;
        // end
        Cursor c = databaseAdapter.getOneGRTTripID(routeID, -1);
        if (c.moveToFirst()) {
            // startStopID = c.getInt(c.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_ID));
            tripID_a = c.getLong(c.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_ID));
            stopCount = c.getInt(c.getColumnIndexOrThrow("stopCount"));
        }
        c = databaseAdapter.getFirstStopIDOfATrip(routeID, tripID_a);
        if (c.moveToFirst()) {
            startStopID = c.getInt(c.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_ID));
        }
        
        // debug:
        Log.d(DEBUG, "RouteMaker: direction one: stopCount = " + stopCount);
        // end
        c.close();
        
        
        
        // direction two trip_id (get the trip with the most stops and the first stop is not startStopID):
        long tripID_b = 0;
        c = databaseAdapter.getOneGRTTripID(routeID, startStopID);
        if (c.moveToFirst()) {
            tripID_b = c.getLong(c.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_ID));
            stopCount = c.getInt(c.getColumnIndexOrThrow("stopCount"));
        }
        // debug:
        Log.d(DEBUG, "RouteMaker: direction two: stopCount = " + stopCount);
        // end
        c.close();
        
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, StopsList.class);
        intent.putExtra(NewDatabaseAdapter.ROUTE_ID, routeID);
        // Pass stopID to the stopList:
        // intent.putExtra(NewDatabaseAdapter.STOPS_ID, stopID);
        intent.putExtra(NewDatabaseAdapter.TRIP_ID, tripID_a);
        
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("directionOne").setIndicator("Direction One",
                        res.getDrawable(R.drawable.direction_one))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        
        
        
        // Do the same for the other tab
        intent = new Intent().setClass(this, StopsList.class);
        intent.putExtra(NewDatabaseAdapter.ROUTE_ID, routeID);
        intent.putExtra(NewDatabaseAdapter.TRIP_ID, tripID_b);
        spec = tabHost.newTabSpec("directionTwo").setIndicator("Direction Two",
                          res.getDrawable(R.drawable.direction_two))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
    }
    
}
