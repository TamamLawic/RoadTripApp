package com.example.roadtripapp_fbu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import org.parceler.Parcels;

public class PlaceDetailsActivity extends AppCompatActivity {
    Location selectedLocation;
    ImageView ivPlaceImageDetails;
    TextView tvPlaceName;
    TextView tvPlaceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        ivPlaceImageDetails = findViewById(R.id.ivPlaceImageDetails);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvPlaceAddress = findViewById(R.id.tvPlaceAddress);

        //when created get the place that was clicked, and use parcel to unwrap
        selectedLocation = (Location) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Location.class.getSimpleName()));

        //put data into fields
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(selectedLocation.getLocationName());
        tvPlaceName.setText(selectedLocation.getLocationName());
        tvPlaceAddress.setText(selectedLocation.getAddress());
        ParseFile locationImage = selectedLocation.getImage();
        Glide.with(this)
                .load(locationImage.getUrl())
                .centerCrop()
                .into(ivPlaceImageDetails);
        //set onclick listener for adding to bucketlist, if clicked from the trip change to delete buttom from trip
    }
}