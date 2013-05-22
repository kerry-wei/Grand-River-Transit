package com.kerrywei.GrandRiverTransit;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RouteList extends ListActivity {
    final String DEBUG = "GRT Assistant DEBUG";
    
    final int VIEW_DIRECTION_LIST = 0;
    
    NewDatabaseAdapter databaseAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseAdapter = Utilities.getNewDatabaseAdapter();
        fillData();
        
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
        
        Intent i = new Intent(this, DirectionList.class);
        i.putExtra(NewDatabaseAdapter.ROUTE_ID, routeID);
        startActivityForResult(i, VIEW_DIRECTION_LIST);
        
        
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case VIEW_DIRECTION_LIST:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            default:
                break;
        }
    }
    
    @Override
    public boolean onSearchRequested() {
         Bundle appData = new Bundle();
         //appData.putBoolean(RouteListSearchableActivity.JARGON, true);
         startSearch(null, false, appData, false);
         return true;
     }
    
    private void fillData() {
        Cursor cursor = databaseAdapter.getGRTRoutes();
        startManagingCursor(cursor);
        
        String[] from = new String[] { NewDatabaseAdapter.ROUTES_SHORT_NAME, NewDatabaseAdapter.ROUTES_LONG_NAME };
        int[] to = new int[] { R.id.RouteList_topText, R.id.RouteList_bottomText };
        
        // DISCOURAGE! Use CursorManager and CursorLoader instead
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.route_list,
                cursor,
                from,
                to);
        setListAdapter(cursorAdapter);
    }
}
