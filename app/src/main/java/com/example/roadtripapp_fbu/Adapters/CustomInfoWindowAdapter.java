package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.roadtripapp_fbu.Fragments.MapsFragment;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.List;
/**
 * Custom Info Window for map markers. Uses Glide to put in images, and ParseQuery to find the photos for the locations.
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    Context context;
    private final View mContents;
    List<Location> locations;
    ParseFile locationImage;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
        mContents = LayoutInflater.from(this.context).inflate(R.layout.custom_info_contents, null);
        locations = new ArrayList<>();
        locations.addAll(Location.getTripLocations(Collaborator.getCurrentTrip()));
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker, mContents);
        return mContents;
    }

    /**
     * Renders in the information for the custom Info Window for the specific location
     */
    private void render(Marker marker, View view) {
        ImageView ivPlacePhoto = view.findViewById(R.id.ivPlacePhoto);
        getLocationImage(marker.getTitle());
        if (locationImage != null){
            Glide.with(context)
                    .load(locationImage.getUrl())
                    .centerCrop()
                    .into(ivPlacePhoto);
        }
        String title = marker.getTitle();
        TextView tvLocationName = ((TextView) view.findViewById(R.id.tvLocationName));
        if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleText.length(), 0);
            tvLocationName.setText(titleText);
        } else {
            tvLocationName.setText("");
        }
    }

    /**
     * Gets the image for the current location from the Location Object
     */
    private void getLocationImage(String title) {
        for (int i = 0; i < locations.size(); i ++ ) {
            String locationName = locations.get(i).getLocationName();
            if (title.equals(locationName)) {
                locationImage = locations.get(i).getImage();
            }
        }
    }


}
