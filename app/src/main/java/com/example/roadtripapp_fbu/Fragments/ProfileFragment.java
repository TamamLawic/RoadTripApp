package com.example.roadtripapp_fbu.Fragments;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.LoginActivity;
import com.example.roadtripapp_fbu.Adapters.PagerAdapter;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.UserProfileActivity;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.util.List;


/**
 * Fragment for bottom navigational view. Shows currently logged in User's information.
 * Sets up Tab View to show user's Trips and BucketList items
 */
public class ProfileFragment extends Fragment implements EditTripNameFragment.EditNameDialogListener {
    public static final String TAG = "ProfileFragment";
    public static final String KEY_PROFILE = "profilePic";
    Button btnNewTrip;
    ImageView ivProfilePic;
    TextView tvName;
    EditText etTripName;
    TextView tvStopsProfile;
    TextView tvDurationProfile;
    TextView tvMilesProfile;
    ImageButton btnLogOut;

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
     * Sets up the TabView to show the current user's trip and bucketlist
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        UserProfileActivity.user = ParseUser.getCurrentUser();
        btnNewTrip = view.findViewById(R.id.btnNewTrip);
        ivProfilePic = view.findViewById(R.id.ivProfileImage);
        tvName = view.findViewById(R.id.tvName);
        etTripName = view.findViewById(R.id.etTripName);
        tvStopsProfile = view.findViewById(R.id.tvStopsProfile);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        tvDurationProfile = view.findViewById(R.id.tvDurationProfile);
        tvMilesProfile = view.findViewById(R.id.tvMilesProfile);

        tvName.setText(ParseUser.getCurrentUser().getUsername());
        tvStopsProfile.setText(String.valueOf(ParseUser.getCurrentUser().getNumber("totalStops")));
        tvDurationProfile.setText(ParseUser.getCurrentUser().getNumber("totalTime") + " Hrs");
        tvMilesProfile.setText(ParseUser.getCurrentUser().getNumber("totalDistance") + " Miles");
        //Put profile picture into the ImageView
        // query posts from Instagram App
        if (ParseUser.getCurrentUser().getParseFile(KEY_PROFILE) != null) {
            ParseFile profileImage = ParseUser.getCurrentUser().getParseFile(KEY_PROFILE);
            Glide.with(getContext())
                    .load(profileImage.getUrl())
                    .circleCrop()
                    .into(ivProfilePic);
        }
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

        //setup log out button
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
                //send user back to the log in page
                Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
            }
        });
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
     * makes a new trip object in parse for the logged in user, Makes the current user a collaborator to the Trip
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
                //after the trip is saved successfully, add the user as a collaborator
                //make the current user a collaborator to the Trip
                Collaborator collaborator = new Collaborator();
                collaborator.setTrip(trip);
                collaborator.setUser(ParseUser.getCurrentUser());
                collaborator.saveInBackground();
                //if post saved successfully, send user to the create mapView with a new map
                Fragment fragment = new MapsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContainer, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
    }
}