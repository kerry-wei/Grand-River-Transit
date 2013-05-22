package com.kerrywei.GrandRiverTransit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NewSchedule extends Activity {

    Button nearByStops;
    Button pickARoute;

    final int NEW_SCHEDULE_FROM_NEARBY_STOPS = 0;
    final int NEW_SCHEDULE_FROM_ROUTE_LIST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_schedule);

        // init buttons:
        nearByStops = (Button) findViewById(R.id.newScheduleScreen_nearByStops);
        nearByStops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showNearbyStops = new Intent(NewSchedule.this, NearbyStops.class);
                startActivityForResult(showNearbyStops, NEW_SCHEDULE_FROM_NEARBY_STOPS);

            }
        });

        pickARoute = (Button) findViewById(R.id.newScheduleScreen_pickARoute);
        pickARoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickARoute = new Intent(getApplicationContext(), RouteList.class);
                startActivityForResult(pickARoute, NEW_SCHEDULE_FROM_ROUTE_LIST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case NEW_SCHEDULE_FROM_NEARBY_STOPS:
        case NEW_SCHEDULE_FROM_ROUTE_LIST:
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
            break;
        default:
            break;
        }
    }
}
