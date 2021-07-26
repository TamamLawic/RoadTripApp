package com.example.roadtripapp_fbu.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.roadtripapp_fbu.Adapters.ItineraryAdapter;
import com.example.roadtripapp_fbu.Objects.BucketListLocation;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.UserProfileActivity;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static com.example.roadtripapp_fbu.Objects.BucketListLocation.KEY_LOCATION;
import static com.example.roadtripapp_fbu.Objects.BucketListLocation.KEY_USER;
import static com.parse.ParseObject.KEY_CREATED_AT;

/**
 * Fragment for Tab View in Profile Fragment to show user's BucketList in a recycler view
 */
public class BucketlistViewFragment extends Fragment {
    RecyclerView rvBucketList;
    ItineraryAdapter adapter;
    List<Location> locations;
    List<BucketListLocation> bucketLocations;

    public BucketlistViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bucketlist_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvBucketList = view.findViewById(R.id.rvBucketList);

        //set up the recycler view for the itinerary
        //Set up the adapter for the trip recycler view
        locations = new ArrayList<>();
        //create the adapter
        adapter = new ItineraryAdapter(getContext(), locations);
        //set the adapter on the recycler view
        rvBucketList.setAdapter(adapter);
        rvBucketList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvBucketList.setLayoutManager(layoutManager);
        bucketLocations = getBucketListLocations(ParseUser.getCurrentUser());
        getLocations(bucketLocations);
        adapter.notifyDataSetChanged();
    }

    private void getLocations(List<BucketListLocation> bucketLocations) {
        for (int i = 0; i < bucketLocations.size(); i++) {
            BucketListLocation bucketLocation = bucketLocations.get(i);
            locations.add(bucketLocation.getLocation());
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

}