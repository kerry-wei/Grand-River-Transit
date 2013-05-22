package com.kerrywei.GrandRiverTransit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BusStopTime extends ListActivity {

    final String DEBUG = "GRT Assistant DEBUG";

    NewDatabaseAdapter databaseAdapter;
    String routeID = null;
    String startStopID = null;
    String startStopName = null;
    String endStopID = null;
    String endStopName = null;

    static void putExtra(Intent i, String routeID, String startStopID, String endStopID, String startStopName,
            String endStopName) {
        i.putExtra(NewDatabaseAdapter.ROUTE_ID, routeID);
        i.putExtra(NewDatabaseAdapter.START_STOP_ID, startStopID);
        i.putExtra(NewDatabaseAdapter.START_STOP_NAME, startStopName);
        i.putExtra(NewDatabaseAdapter.END_STOP_ID, endStopID);
        i.putExtra(NewDatabaseAdapter.END_STOP_NAME, endStopName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_stop_time);

        // get data passed from the main activity:
        Bundle extras = getIntent().getExtras();
        routeID = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.ROUTE_ID);
        startStopID = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.START_STOP_ID);
        startStopName = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.START_STOP_NAME);
        endStopID = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.END_STOP_ID);
        endStopName = (savedInstanceState == null) ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.END_STOP_NAME);
        if (extras != null) {
            routeID = extras.getString(NewDatabaseAdapter.ROUTE_ID);
            startStopID = extras.getString(NewDatabaseAdapter.START_STOP_ID);
            startStopName = extras.getString(NewDatabaseAdapter.START_STOP_NAME);
            endStopID = extras.getString(NewDatabaseAdapter.END_STOP_ID);
            endStopName = extras.getString(NewDatabaseAdapter.END_STOP_NAME);
        }

        databaseAdapter = Utilities.getNewDatabaseAdapter();
        fillData();
    }

    // fill the list view with data:
    private void fillData() {
        // update textviews:
        TextView t = (TextView) findViewById(R.id.BusStopTime_startStop);
        t.setText(startStopName);
        t = (TextView) findViewById(R.id.BusStopTime_endStop);
        t.setText(endStopName);

        String[] serviceIDs = getServiceIDs(getDateOfWeek(), getDate());
        int[] tripIDs = getTripIDs(routeID, serviceIDs);

        Cursor c = databaseAdapter
                .getBusStopTimes(tripIDs, routeID, startStopID, endStopID, startStopName, endStopName);

        startManagingCursor(c);

        String[] from = new String[] { NewDatabaseAdapter.STOP_TIMES_DEPARTURE_TIME,
                NewDatabaseAdapter.STOP_TIMES_ARRIVAL_TIME };
        int[] to = new int[] { R.id.BusStopTimeEntry_departureTime, R.id.BusStopTimeEntry_arrivalTime };

        // Now create an array adapter and set it to display using our row
        // Discouraged. Use CursorManager and CursorLoader instead
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.bus_stop_time_entry, c, from, to);
        setListAdapter(cursorAdapter);

    }

    private String getDateOfWeek() {
        Calendar calendar = Calendar.getInstance();
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
        case Calendar.MONTH:
            return "monday";
        case Calendar.TUESDAY:
            return "tuesday";
        case Calendar.WEDNESDAY:
            return "wednesday";
        case Calendar.THURSDAY:
            return "thursday";
        case Calendar.FRIDAY:
            return "friday";
        case Calendar.SATURDAY:
            return "saturday";
        case Calendar.SUNDAY:
            return "sunday";
        default:
            return null;
        }
    }

    private int getDate() {
        int ans = 0;
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String stringDate = dateFormat.format(date);
        try {
            ans = Integer.valueOf(stringDate);
        } catch (NumberFormatException e) {
            Log.d(DEBUG, "ERROR in BusStopTimes-getDate(): NumberFormatException.");
            ans = -1;
        }
        return ans;
    }

    private String[] getServiceIDs(String date_of_week, int date) {
        // !!! change this later: should query the calendar table to get max number of the service_id
        String serviceArr[] = new String[11];
        Cursor cursor = databaseAdapter.getGRTServiceID(date_of_week, date);
        if (cursor.moveToFirst()) {
            String serviceID = null;
            int i = 0;
            do {
                serviceID = cursor.getString(cursor.getColumnIndexOrThrow(NewDatabaseAdapter.CALENDAR_SERVICE_ID));
                serviceArr[i] = serviceID;
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        return serviceArr;
    }

    private int[] getTripIDs(String routeId, String[] serviceIDs) {
        Cursor cursor = databaseAdapter.getGRTTripID(routeId, serviceIDs);
        int count = cursor.getCount();
        int[] trips = new int[count];
        int i = 0;
        int trip_id;
        if (cursor.moveToFirst()) {
            do {
                trip_id = cursor.getInt(cursor.getColumnIndexOrThrow(NewDatabaseAdapter.TRIP_ID));
                trips[i] = trip_id;
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        }

        return trips;
    }

}
