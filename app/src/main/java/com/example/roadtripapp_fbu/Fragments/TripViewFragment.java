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

import com.example.roadtripapp_fbu.Adapters.TripAdapter;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.UserProfileActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
/**
 * Fragment for the TabView's Trip section, shows the user's Trips. Uses ParseQuery to get all Trips the user is a collaborator on
 */
public class TripViewFragment extends Fragment {
    RecyclerView rvTrips;
    protected TripAdapter adapter;
    protected List<Trip> allTrips;
    List<Collaborator> collaborators;

    public TripViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvTrips = view.findViewById(R.id.rvTrips);
        //Set up the adapter for the trip recycler view
        allTrips = new ArrayList<>();
        collaborators = new ArrayList<>();
        //create the adapter
        adapter = new TripAdapter(getContext(), allTrips);
        //set the adapter on the recycler view
        rvTrips.setAdapter(adapter);
        rvTrips.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext() ,LinearLayoutManager.VERTICAL, false);
        rvTrips.setLayoutManager(layoutManager);
        //query collaborators, to find all trips where the user is the collaborator
        queryPosts();
    }

    /** Begins a Parse Query in a background thread, getting all of the posts the user has authored. */
    /**The posts are added to a list, and the adapter is notified of the data change.*/
    public void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Collaborator> query = ParseQuery.getQuery(Collaborator.class);
        // include data referred by user key
        query.include(Collaborator.KEY_USER);
        // include data referred by trip key
        query.include(Collaborator.KEY_TRIP);
        //only query posts of the currently signed in user
        query.whereEqualTo(Collaborator.KEY_USER, UserProfileActivity.user);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Collaborator>() {
            @Override
            public void done(List<Collaborator> trips, ParseException e) {
                // check for errors
                if (e != null) {
                    return;
                }
                // get all the trips from the Collaborator object
                collaborators.addAll(trips);
                getAllTrips();
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Gets all of the Trips the user is a collaborator on
     */
    private void getAllTrips() {
        for (int i = 0; i < collaborators.size(); i++) {
            allTrips.add(collaborators.get(i).getTrip());
        }
    }
}