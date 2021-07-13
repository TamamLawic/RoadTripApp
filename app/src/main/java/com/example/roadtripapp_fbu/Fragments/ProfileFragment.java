package com.example.roadtripapp_fbu.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.roadtripapp_fbu.LoginActivity;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Trip;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Fragment for bottom navigational view. ParseQuery to get all of the user's trips and show them in a recycler view.
 */
public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFragment";
    private Button btnLogOut;
    private Button btnNewTrip;

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
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnNewTrip = view.findViewById(R.id.btnNewTrip);

        //when logout button is clicked, log out the current user
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

        //Creates a new trip object, and takes you to the mapFragment to start planning the trip
        btnNewTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                makeNewTrip(currentUser);
            }
        });
    }

    /**
     * makes a new trip object in parse for the logged in user
     */
    private void makeNewTrip(ParseUser currentUser) {
        ParseObject trip = new ParseObject("Trip");

        trip.put("author", ParseUser.getCurrentUser());
        //TODO: make a way to edit the name of the trip
        trip.put("tripName", "A string");

        // Saves the new object.
        trip.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post was saved successfully!");
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