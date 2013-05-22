package com.kerrywei.GrandRiverTransit;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NearbyStops extends MapActivity implements LocationListener {
    final int PICK_A_ROUTE = 0;

    MapView mapView;
    List<Overlay> mapOverlays;

    NewDatabaseAdapter databaseAdapter;
    LocationManager locationManager;
    Location currentLocation;

    boolean locationPinned = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearyby_stops_map);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(17);

        mapOverlays = mapView.getOverlays();

        databaseAdapter = Utilities.getNewDatabaseAdapter();

        startLocationMonitor();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*
         * if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) { webView.goBack(); return true; }
         */

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        int lat = (int) (location.getLatitude() * 1e6);
        int lon = (int) (location.getLongitude() * 1e6);

        Drawable drawable = this.getResources().getDrawable(R.drawable.you_are_here);
        CustomizedOverlay itemizedoverlay = new CustomizedOverlay(drawable, this);
        GeoPoint point = new GeoPoint(lat, lon);
        OverlayItem overlayitem = new OverlayItem(point, "You are here", null);

        if (mapOverlays.size() == 0) {
            itemizedoverlay.addOverlay(overlayitem);
            mapOverlays.add(itemizedoverlay);
        } else {
            ((CustomizedOverlay) mapOverlays.get(0)).updateOverlay(0, overlayitem);
        }

        if (!locationPinned) {
            mapView.getController().setCenter(point);
            locationPinned = true;
        }

        // update the map view once the user's location is updated:
        showNearbyStops();
    }

    @Override
    public void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
        locationPinned = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case PICK_A_ROUTE:
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
            break;
        default:
            break;
        }
    }

    private void startLocationMonitor() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
    }

    private void showNearbyStops() {
        Cursor stopCursor = databaseAdapter.getGRTStops();
        double lat, lon, currentLat, currentLon;
        String stopName = null, stopID = null;
        if (currentLocation == null) {
            return;
        } else {
            currentLat = currentLocation.getLatitude();
            currentLon = currentLocation.getLongitude();
        }
        stopCursor.moveToFirst();
        do {
            lat = stopCursor.getDouble(stopCursor.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_LAT));
            lon = stopCursor.getDouble(stopCursor.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_LON));
            stopName = stopCursor.getString(stopCursor.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_NAME));
            stopID = String.valueOf(stopCursor.getInt(stopCursor.getColumnIndexOrThrow(NewDatabaseAdapter.STOPS_ID)));
            float[] distance = new float[3];
            Location.distanceBetween(lat, lon, currentLat, currentLon, distance);
            if (distance[0] < 1000) {

                int geo_lat = (int) (lat * 1e6);
                int geo_lon = (int) (lon * 1e6);
                Drawable drawable = this.getResources().getDrawable(R.drawable.red_pin);
                CustomizedOverlay itemizedoverlay = new CustomizedOverlay(drawable, this);
                GeoPoint point = new GeoPoint(geo_lat, geo_lon);
                OverlayItem overlayitem = new OverlayItem(point, stopName, stopID);
                itemizedoverlay.addOverlay(overlayitem);
                mapOverlays.add(itemizedoverlay);

            }
        } while (stopCursor.moveToNext());

        stopCursor.close();
    }

    @SuppressWarnings("rawtypes")
    class CustomizedOverlay extends ItemizedOverlay {
        private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
        Context context;
        OverlayItem item;
        Dialog dialog;

        public CustomizedOverlay(Drawable defaultMarker, Context context) {
            super(boundCenterBottom(defaultMarker));
            this.context = context;
        }

        @Override
        public int size() {
            return overlays.size();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return overlays.get(i);
        }

        @Override
        protected boolean onTap(int index) {
            item = overlays.get(index);
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.show_route_from_stop);
            dialog.setTitle(item.getSnippet() + ": " + item.getTitle());
            Button addToFavoriteButton = (Button) dialog.findViewById(R.id.MapView_showRoutesOnThisStop);
            addToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, PickARoute.class);
                    i.putExtra(NewDatabaseAdapter.STOPS_ID, item.getSnippet());
                    ((Activity) context).startActivityForResult(i, PICK_A_ROUTE);
                    dialog.dismiss();
                }
            });
            dialog.show();

            return true;
        }

        public void updateOverlay(int i, OverlayItem item) {
            overlays.set(i, item);
        }

        public void addOverlay(OverlayItem overlay) {
            overlays.add(overlay);
            populate();
        }

    }

}
