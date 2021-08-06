package com.example.roadtripapp_fbu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Adapters.PagerAdapter;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;
import org.w3c.dom.Text;

/** Shows the selected user's profile. Uses Parse to display user's trips, bucketlist, and basic information*/
public class UserProfileActivity extends AppCompatActivity {
    public static final String KEY_PROFILE = "profilePic";
    public static ParseUser user;
    Button btnNewTrip;
    ImageView ivProfilePic;
    TextView tvName;
    ImageButton btnLogOut;
    TextView tvStopsProfile;
    TextView tvDurationProfile;
    TextView tvMilesProfile;
    ImageButton btnBackOtherProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        btnNewTrip = findViewById(R.id.btnNewTrip);
        ivProfilePic = findViewById(R.id.ivProfileImage);
        tvName = findViewById(R.id.tvName);
        btnLogOut = findViewById(R.id.btnLogOut);
        tvStopsProfile = findViewById(R.id.tvStopsProfile);
        tvDurationProfile = findViewById(R.id.tvDurationProfile);
        tvMilesProfile = findViewById(R.id.tvMilesProfile);
        btnBackOtherProfile = findViewById(R.id.btnBackOtherProfile);
        btnBackOtherProfile.setVisibility(View.VISIBLE);

        //User parse to send user data to access profile
        user = (ParseUser) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra("user"));

        //fill in profile
        tvName.setText(user.getUsername());
        tvStopsProfile.setText(String.valueOf(ParseUser.getCurrentUser().getNumber("totalStops")));
        tvDurationProfile.setText(ParseUser.getCurrentUser().getNumber("totalTime") + " Hrs");
        tvMilesProfile.setText(ParseUser.getCurrentUser().getNumber("totalDistance") + " Miles");
        //Put profile picture into the ImageView
        ParseFile profileImage = user.getParseFile(KEY_PROFILE);
        Glide.with(this)
                .load(profileImage.getUrl())
                .circleCrop()
                .into(ivProfilePic);

        //Adds the user to your friends list
        btnNewTrip.setText("Add friend");
        btnLogOut.setVisibility(View.INVISIBLE);

        //Set up the Tab View for Trips/BucketList
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapterProfile myPagerAdapter = new PagerAdapterProfile(getSupportFragmentManager(), 0);
        viewPager.setAdapter(myPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        btnBackOtherProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBackOtherProfile.setVisibility(View.INVISIBLE);
                finish();
            }
        });
    }
}
