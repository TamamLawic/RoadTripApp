package com.example.roadtripapp_fbu.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.LoginActivity;
import com.example.roadtripapp_fbu.PagerAdapter;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Trip;
import com.example.roadtripapp_fbu.Adapters.TripAdapter;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for bottom navigational view. Sets up Tab View to show user's Trips and BucketList items
 */
public class ProfileFragment extends Fragment implements EditTripNameFragment.EditNameDialogListener {
    public static final String TAG = "ProfileFragment";
    public static final String KEY_PROFILE = "profilePic";
    Button btnNewTrip;
    ImageView ivProfilePic;
    TextView tvName;
    EditText etTripName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * When the fragment is created, set an onclick listener for logging out the user.
     * Sets up the recyclerview and displays all of the user's trips
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        btnNewTrip = view.findViewById(R.id.btnNewTrip);
        ivProfilePic = view.findViewById(R.id.ivProfileImage);
        tvName = view.findViewById(R.id.tvName);
        etTripName = view.findViewById(R.id.etTripName);

        //fill in profile
        tvName.setText(ParseUser.getCurrentUser().getUsername());
        //Put profile picture into the ImageView
        // query posts from Instagram App
        ParseFile profileImage = ParseUser.getCurrentUser().getParseFile(KEY_PROFILE);
        Glide.with(getContext())
                .load(profileImage.getUrl())
                .circleCrop()
                .into(ivProfilePic);
        //Creates a new trip object, and takes you to the mapFragment to start planning the trip
        btnNewTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start the fragment to namee the trip, set the imput to the result
                showEditDialog();
            }
        });

        //Set up the Tab View for Trips/BucketList
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        PagerAdapter myPagerAdapter = new PagerAdapter(getActivity().getSupportFragmentManager(), 0);
        viewPager.setAdapter(myPagerAdapter);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    // Call this method to launch the edit dialog
    private void showEditDialog() {
        FragmentManager fm = getFragmentManager();
        EditTripNameFragment editNameDialogFragment = EditTripNameFragment.newInstance("Some Title");
        // SETS the target fragment for use later when sending results
        editNameDialogFragment.setTargetFragment(ProfileFragment.this, 300);
        editNameDialogFragment.show(fm, "fragment_edit_name");
    }

    // This is called when the dialog is completed and the results have been passed
    @Override
    public void onFinishEditDialog(String inputText) {
        Toast.makeText(getContext(), "New trip: " + inputText, Toast.LENGTH_SHORT).show();
        ParseUser currentUser = ParseUser.getCurrentUser();
        makeNewTrip(currentUser, inputText);
    }

    /**
     * When the options menu is created, change it to the one for the maps fragment
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_fragment, menu);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle("Profile");
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * When the MenuItem post is selected startActivity : NewPostActivity
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
        //send user back to the log in page
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }

    /**
     * makes a new trip object in parse for the logged in user
     */
    private void makeNewTrip(ParseUser currentUser, String tripName) {
        Trip trip = new Trip();

        trip.put("author", ParseUser.getCurrentUser());
        trip.put("tripName", tripName);

        // Saves the new object.
        trip.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                //if post saved successfully, send user to the create mapView with a new map
                Fragment fragment = new MapsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContainer, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                //Add a new trip item for the profile recycler view
            }
        });
    }
}