package com.kerrywei.GrandRiverTransit;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RouteListSearchableActivity extends ListActivity {
    NewDatabaseAdapter databaseAdapter;
    final int VIEW_STOP_LIST = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);

        databaseAdapter = Utilities.getNewDatabaseAdapter();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            performSearch(query);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // get routeID based on _id:
        String routeID = null;
        Cursor c = databaseAdapter.getGRTRouteIDFromRoutesBasedOnRowID(id);
        if (c.moveToFirst()) {
            routeID = c.getString(c.getColumnIndexOrThrow(NewDatabaseAdapter.ROUTES_ID));
        }
        c.close();

        Intent i = new Intent(this, RouteMaker.class);
        i.putExtra(NewDatabaseAdapter.ROUTE_ID, routeID);
        i.putExtra(RouteMaker.ROUTEID_SRC, RouteMaker.ROUTEID_FROM_ROUTES);
        startActivityForResult(i, VIEW_STOP_LIST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
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

    private void performSearch(String query) {
        Cursor c = databaseAdapter.searchGRTRoutes(query);
        fillData(c);
    }

    private void fillData(Cursor cursor) {
        // Cursor cursor = databaseAdapter.getGRTRoutes();
        startManagingCursor(cursor);

        String[] from = new String[] { NewDatabaseAdapter.ROUTES_SHORT_NAME, NewDatabaseAdapter.ROUTES_LONG_NAME };
        int[] to = new int[] { R.id.RouteList_topText, R.id.RouteList_bottomText };

        // Discouraged. Use CursorManager and CursorLoader instead
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.route_list, cursor, from, to);
        setListAdapter(cursorAdapter);
    }
}
