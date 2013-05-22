package com.kerrywei.GrandRiverTransit;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CustomizedOverlay extends ItemizedOverlay {
    
    private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();
    Context context;
    OverlayItem overlayItem;
    Dialog dialog;

    public CustomizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        this.context = context;
    }
    
    @Override
    public int size() {
        return overlayItemList.size();
    }
    
    @Override
    protected OverlayItem createItem(int i) {
      return overlayItemList.get(i);
    }
    
    @Override
    protected boolean onTap(int index) {
        overlayItem = overlayItemList.get(index);
        
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.show_route_from_stop);
        dialog.setTitle(overlayItem.getSnippet() + ": " + overlayItem.getTitle());
        
        Button addToFavoriteButton = (Button) dialog.findViewById(R.id.MapView_showRoutesOnThisStop);
        addToFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PickARoute.class);
                i.putExtra(NewDatabaseAdapter.STOPS_ID, overlayItem.getSnippet());
                context.startActivity(i);
                dialog.dismiss();
            }
        });
        dialog.show();
      
        return true;
    }
    
    public void updateOverlay(int i, OverlayItem item) {
        overlayItemList.set(i, item);
    }
    
    public void addOverlay(OverlayItem overlay) {
        overlayItemList.add(overlay);
        populate();
    }

}
