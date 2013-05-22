package com.kerrywei.GrandRiverTransit;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class PickARoute extends ListActivity {
    final String DEBUG = "GRT Assistant DEBUG";
    final int VIEW_STOP_LIST = 0;
    String stopID = null;

    NewDatabaseAdapter databaseAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseAdapter = Utilities.getNewDatabaseAdapter();

        // get extra data:
        Bundle extras = getIntent().getExtras();
        stopID = savedInstanceState == null ? null : (String) savedInstanceState
                .getSerializable(NewDatabaseAdapter.STOPS_ID);
        if (extras != null) {
            stopID = extras.getString(NewDatabaseAdapter.STOPS_ID);
        }

        fillData();

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // get routeID from _id:
        String routeID = null;
        Cursor c = databaseAdapter.getGRTRouteIDFromRoutes_From_Stop(id);
        if (c.moveToFirst()) {
            routeID = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.ROUTES_ID));
        }
        c.close();

        Intent i = new Intent(this, StopsList.class);
        StopsList.putExtra(i, routeID, stopID);

        startActivityForResult(i, VIEW_STOP_LIST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        Cursor cursor = databaseAdapter.getGRTRoutesBasedOnStopID(stopID);

        startManagingCursor(cursor);

        String[] from = new String[] { NewDatabaseAdapter.ROUTES_SHORT_NAME, NewDatabaseAdapter.ROUTES_LONG_NAME };
        int[] to = new int[] { R.id.RouteList_topText, R.id.RouteList_bottomText };

        // Now create an array adapter and set it to display using our row

        // DISCOURAGE! Use CursorManager and CursorLoader instead
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.route_list, cursor, from, to);
        setListAdapter(cursorAdapter);
    }
}
