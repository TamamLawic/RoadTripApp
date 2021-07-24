package com.example.roadtripapp_fbu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Objects.BucketListLocation;
import com.example.roadtripapp_fbu.Objects.Location;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

public class PlaceDetailsActivity extends AppCompatActivity {
    Location selectedLocation;
    ImageView ivPlaceImageDetails;
    TextView tvPlaceName;
    TextView tvPlaceAddress;
    Button btnAddBucketList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        ivPlaceImageDetails = findViewById(R.id.ivPlaceImageDetails);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvPlaceAddress = findViewById(R.id.tvPlaceAddress);
        btnAddBucketList = findViewById(R.id.btnAddBucketList);

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

        //set onclick listener for adding to BucketList
        btnAddBucketList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BucketListLocation bucketLocation = new BucketListLocation();
                bucketLocation.setUser(ParseUser.getCurrentUser());
                bucketLocation.setLocation(selectedLocation);
                bucketLocation.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(PlaceDetailsActivity.this, "Error while adding!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(PlaceDetailsActivity.this, "Added to List!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}