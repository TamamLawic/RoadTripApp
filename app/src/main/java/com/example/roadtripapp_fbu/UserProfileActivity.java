package com.example.roadtripapp_fbu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class UserProfileActivity extends AppCompatActivity {
    public static final String KEY_PROFILE = "profilePic";
    public static ParseUser user;
    Button btnNewTrip;
    ImageView ivProfilePic;
    TextView tvName;
    //public ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        btnNewTrip = findViewById(R.id.btnNewTrip);
        ivProfilePic = findViewById(R.id.ivProfileImage);
        tvName = findViewById(R.id.tvName);

        //User parse to send user data to access profile
        user = (ParseUser) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra("user"));

        //fill in profile
        tvName.setText(user.getUsername());
        //Put profile picture into the ImageView
        // query posts from Instagram App
        ParseFile profileImage = user.getParseFile(KEY_PROFILE);
        Glide.with(this)
                .load(profileImage.getUrl())
                .circleCrop()
                .into(ivProfilePic);

        //Adds the user to your friends list
        btnNewTrip.setText("Add friend");

        //Set up the Tab View for Trips/BucketList
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter myPagerAdapter = new PagerAdapter(getSupportFragmentManager(), 0);
        viewPager.setAdapter(myPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

}
