package com.kerrywei.GrandRiverTransit;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class StopsList extends ListActivity {
    final String DEBUG = "GRT Assistant DEBUG";

    // Long rowID;
    String routeID = null;
    String startStopID = null;
    String endStopID = null;

    // trip_id passed from RouteMaker:
    long trip_id_from_RouteMaker;

    String headsign = null;

    long schedule_row_id;

    boolean insertSchedule = false;

    NewDatabaseAdapter databaseAdapter;

    private ArrayAdapter<StopListEntry> arrayAdapter;

    StopListEntry stopListEntryClicked;
    StopListEntry startStop = null;
    StopListEntry endStop = null;

    Dialog dialog;
    
    static void putExtra(Intent i, long id, String routeShortName, String startStopID, String endStopID) {
        i.putExtra(NewDatabaseAdapter.ROUTES_ROWID, id);
        i.putExtra(NewDatabaseAdapter.START_STOP_ID, startStopID);
        i.putExtra(NewDatabaseAdapter.END_STOP_ID, endStopID);
        i.putExtra(NewDatabaseAdapter.ROUTE_ID, routeShortName);
    }

    static void putExtra(Intent i, String routeID, String stopID) {
        i.putExtra(NewDatabaseAdapter.ROUTE_ID, routeID);
        i.putExtra(NewDatabaseAdapter.START_STOP_ID, stopID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);

        databaseAdapter = Utilities.getNewDatabaseAdapter();
        Bundle extras = getIntent().getExtras();
        // rowID = null;
        routeID = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.ROUTE_ID);
        startStopID = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.START_STOP_ID);
        endStopID = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.END_STOP_ID);
        headsign = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.TRIP_HEADSIGN);
        String schedule_row_id_str = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.ROUTES_ROWID);
        String trip_id_str = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.TRIP_ID);

        if (schedule_row_id_str != null) {
            schedule_row_id = Long.valueOf(schedule_row_id_str);
        }
        if (trip_id_str != null) {
            trip_id_from_RouteMaker = Long.valueOf(trip_id_str);
        }

        if (extras != null) {
            routeID = extras.getString(NewDatabaseAdapter.ROUTE_ID);
            startStopID = extras.getString(NewDatabaseAdapter.START_STOP_ID);
            endStopID = extras.getString(NewDatabaseAdapter.END_STOP_ID);

            headsign = extras.getString(NewDatabaseAdapter.TRIP_HEADSIGN);

            schedule_row_id = extras.getLong(NewDatabaseAdapter.ROUTES_ROWID);
            trip_id_from_RouteMaker = extras.getInt(NewDatabaseAdapter.TRIP_ID);
        }

        insertSchedule = endStopID == null ? true : false;

        fillData();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        stopListEntryClicked = (StopListEntry) getListView().getItemAtPosition(position);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.set_start_and_end);
        dialog.setTitle("Set Start or End");

        Button setStart = (Button) dialog.findViewById(R.id.StopListWithStartAndEnd_start);
        setStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(DEBUG, "set start clicked!");

                if (startStop != null) {
                    // clear the existing startStop:
                    startStop.setStartStop(false);
                }
                if (endStop == stopListEntryClicked) {
                    endStop = null;
                }
                startStop = stopListEntryClicked;
                startStop.setStartStop(true);
                startStop.setEndStop(false);
                arrayAdapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        Button setEnd = (Button) dialog.findViewById(R.id.StopListWithStartAndEnd_end);
        setEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG, "set end clicked!");
                if (endStop != null) {
                    endStop.setEndStop(false);
                }
                if (startStop == stopListEntryClicked) {
                    startStop = null;
                }
                endStop = stopListEntryClicked;
                endStop.setStartStop(false);
                endStop.setEndStop(true);
                arrayAdapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (insertSchedule) {
            inflateInsertMenu(menu);
        } else {
            inflateUpdateMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.StopListMenuItem_saveSchedule:
            if (routeID != null && startStop != null && endStop != null && startStop.getStopID() != null
                    && startStop.getStopName() != null && endStop.getStopID() != null && endStop.getStopName() != null) {
                // get route_short_name from table routes:
                Cursor c = databaseAdapter.getGRTRouteShortName(routeID);
                int route_short_name = -1;
                if (c.moveToFirst()) {
                    route_short_name = c.getInt(c.getColumnIndexOrThrow(NewDatabaseAdapter.ROUTES_SHORT_NAME));
                }
                c.close();

                databaseAdapter.insertMySchedulesEntry(routeID, startStop.getStopID(), startStop.getStopName(),
                        endStop.getStopID(), endStop.getStopName(), route_short_name);

                if (getParent() == null) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    getParent().setResult(RESULT_OK);
                    finish();
                }
            }
            return true;
        case R.id.StopListMenuItem_updateSchedule:
            if (routeID != null && startStop != null && endStop != null && startStop.getStopID() != null
                    && startStop.getStopName() != null && endStop.getStopID() != null && endStop.getStopName() != null) {
                // get route_short_name from table routes:
                Cursor c = databaseAdapter.getGRTRouteShortName(routeID);
                int route_short_name = -1;
                if (c.moveToFirst()) {
                    route_short_name = c.getInt(c.getColumnIndexOrThrow(NewDatabaseAdapter.ROUTES_SHORT_NAME));
                }
                c.close();

                databaseAdapter.updateMySchedulesEntry(schedule_row_id, routeID, startStop.getStopID(),
                        startStop.getStopName(), endStop.getStopID(), endStop.getStopName(), route_short_name);

                if (getParent() == null) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    getParent().setResult(RESULT_OK);
                    finish();
                }
            }
            return true;
        default:
            return true;
        }
    }

    private void inflateInsertMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stop_list_menu_insert, menu);
    }

    private void inflateUpdateMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stop_list_menu_update, menu);
    }

    private void fillData() {
        long tripID = -1;
        Cursor cursor;

        if (startStopID != null) { // for NearbyStops
            cursor = databaseAdapter.getGRTTripIDBasedOnRouteAndStopID(routeID, startStopID);
            // get the trip_id of the trip with largest stop_sequence number:
            // however, this may not be the right way... update this query later!!
            if (cursor.moveToFirst()) {
                tripID = cursor.getInt(cursor.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_ID));
            }
            cursor.close();
        } else { // for RouteMaker
            tripID = trip_id_from_RouteMaker;

            /*
             * cursor = databaseAdapter.getLongestStopListOfADirection(routeID, headsign); if (cursor.moveToFirst()) {
             * tripID = cursor.getInt(cursor.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_ID)); } cursor.close();
             */
        }

        cursor = databaseAdapter.getGRTStopList(routeID, tripID);
        // cursor = databaseAdapter.getLongestStopListOfADirection(routeID, headsign);

        // Initialize the array adapter for the conversation thread
        List<StopListEntry> list = new ArrayList<StopListEntry>();
        arrayAdapter = new StopListArrayAdapter(this, list);

        // fill arrayAdapter with entry from the cursor:
        if (cursor.moveToFirst()) {
            String stop_id = null, stop_name = null;
            do {
                stop_id = cursor.getString(cursor.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_ID));
                stop_name = cursor.getString(cursor.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_NAME));
                StopListEntry entry = new StopListEntry(stop_id, stop_name);
                arrayAdapter.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // mark stops as start and end if necessary
        // startStopID != null && endStopID == null means the request is from NearbyStops->PickARoute;
        // startStopID != null && endStopID != null means the request is from GrandRiverTransitActivity;
        // otherwise, the request is from RouteList
        if (startStopID != null && endStopID == null) {
            for (int i = 0; i < arrayAdapter.getCount(); i++) {
                if (arrayAdapter.getItem(i).getStopID().equals(startStopID) && i + 1 < arrayAdapter.getCount()) {
                    startStop = arrayAdapter.getItem(i);
                    endStop = arrayAdapter.getItem(i + 1);
                    startStop.setStartStop(true);
                    startStop.setEndStop(false);
                    endStop.setStartStop(false);
                    endStop.setEndStop(true);
                }
            }
        } else if (startStopID != null && endStopID != null) {
            for (int i = 0; i < arrayAdapter.getCount(); i++) {
                if (arrayAdapter.getItem(i).getStopID().equals(startStopID)) {
                    startStop = arrayAdapter.getItem(i);
                    startStop.setStartStop(true);
                    startStop.setEndStop(false);

                }
                if (arrayAdapter.getItem(i).getStopID().equals(endStopID)) {
                    endStop = arrayAdapter.getItem(i);
                    endStop.setStartStop(false);
                    endStop.setEndStop(true);
                }
            }
        } else { // the request comes from RouteList. mark the first stop as start stop, and the last stop as end stop
            if (arrayAdapter.getCount() > 0) {
                startStop = arrayAdapter.getItem(0);
                endStop = arrayAdapter.getItem(arrayAdapter.getCount() - 1);
                startStop.setStartStop(true);
                startStop.setEndStop(false);
                endStop.setStartStop(false);
                endStop.setEndStop(true);
            }
        }

        arrayAdapter.setNotifyOnChange(true);
        setListAdapter(arrayAdapter);
    }
}
