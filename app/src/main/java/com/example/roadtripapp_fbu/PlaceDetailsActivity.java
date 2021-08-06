package com.example.roadtripapp_fbu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Objects.BucketListLocation;
import com.example.roadtripapp_fbu.Objects.Location;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static com.example.roadtripapp_fbu.Objects.BucketListLocation.KEY_LOCATION;
import static com.example.roadtripapp_fbu.Objects.BucketListLocation.KEY_USER;
import static com.parse.ParseObject.KEY_CREATED_AT;

public class PlaceDetailsActivity extends AppCompatActivity {
    Location selectedLocation;
    ImageView ivPlaceImageDetails;
    TextView tvPlaceName;
    TextView tvPlaceAddress;
    Button btnAddBucketList;
    TextView tvOpeningHours;
    TextView tvPhoneNumber;
    TextView tvWebsite;
    ImageButton btnBackDetails;
    List<String> BucketLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        ivPlaceImageDetails = findViewById(R.id.ivPlaceImageDetails);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvPlaceAddress = findViewById(R.id.tvPlaceAddress);
        btnAddBucketList = findViewById(R.id.btnAddBucketList);
        tvOpeningHours = findViewById(R.id.tvOpeningHours);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvWebsite = findViewById(R.id.tvWebsite);
        btnBackDetails = findViewById(R.id.btnBackDetails);

        //when created get the place that was clicked, and use parcel to unwrap
        selectedLocation = (Location) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Location.class.getSimpleName()));

        BucketLocations = new ArrayList<>();
        getLocations(getBucketListLocations(ParseUser.getCurrentUser()));

        //put data into fields
        tvPlaceName.setText(selectedLocation.getLocationName());
        tvPlaceAddress.setText(selectedLocation.getAddress());
        tvOpeningHours.setText(selectedLocation.getHours());
        tvPhoneNumber.setText(selectedLocation.getPhone());
        Linkify.addLinks(tvPhoneNumber, Linkify.ALL);
        tvWebsite.setText(selectedLocation.getWebsite());
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

        //set up back button for details page
        btnBackDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });

        //set up the add to bucketlist button
        addBucketListButton();
    }

    private void getLocations(List<BucketListLocation> bucketListLocations) {
        for (int i = 0; i < bucketListLocations.size(); i++) {
            BucketLocations.add(bucketListLocations.get(i).getLocation().getLocationName());
        }
    }

    /**
     * Uses ParseQuery to get the BucketList of Locations for the user.
     */
    private List<BucketListLocation> getBucketListLocations(ParseUser currentUser) {
        // specify what type of data we want to query - Location.class
        ParseQuery<BucketListLocation> query = ParseQuery.getQuery(BucketListLocation.class);
        //only query BucketList of the current user
        query.whereEqualTo(KEY_USER, UserProfileActivity.user);
        //include data about the locations
        query.include(KEY_LOCATION);
        //finds the newest created trip
        query.addAscendingOrder(KEY_CREATED_AT);
        try {
            return query.find();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Only add onclick listener for bucketList if the user doesn't already have it in their list*/
    private void addBucketListButton() {
        if(BucketLocations.contains(selectedLocation.getLocationName())){
            btnAddBucketList.setVisibility(View.INVISIBLE);
        }
    }
}