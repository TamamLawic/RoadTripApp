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

import com.example.roadtripapp_fbu.Adapters.TripFeedAdapter;
import com.example.roadtripapp_fbu.Objects.FeedObjects;
import com.example.roadtripapp_fbu.Objects.JournalEntry;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.example.roadtripapp_fbu.TripFeedActivity.selectedTrip;

/**
 */
public class TripFeedFragment extends Fragment {
    RecyclerView rvTripPosts;
    List<FeedObjects> feedObjects;
    TripFeedAdapter adapter;
    int ready = 0;

    public TripFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_feed, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvTripPosts = view.findViewById(R.id.rvPosts);

        //set up recycler view for list of posts
        feedObjects = new ArrayList<>();
        adapter = new TripFeedAdapter(getContext(), feedObjects);
        rvTripPosts.setAdapter(adapter);
        rvTripPosts.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvTripPosts.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryPosts();
        queryJournals();

    }

    /** Begins a Parse Query in a background thread, getting all posts for this trip. */
    /**The posts are added to a list, and the adapter is notified of the data change.*/
    protected void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.include(Post.KEY_TRIP);
        query.include(Post.KEY_LOCATION);
        query.whereEqualTo("tripId", selectedTrip);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    return;
                }
                else {
                    // save received posts to list and check if the Journals have loaded
                    feedObjects.addAll(posts);
                    ready += 1;
                    //if both the posts and journals have loaded, sort them by created at date
                    if (ready == 2) {
                        sortFeedObjects();
                    }
                }
            }
        });
    }

    /** Begins a Parse Query in a background thread, getting all posts for this trip. */
    /**The posts are added to a list, and the adapter is notified of the data change.*/
    protected void queryJournals() {
        // specify what type of data we want to query - Post.class
        ParseQuery<JournalEntry> query = ParseQuery.getQuery(JournalEntry.class);
        query.include(JournalEntry.KEY_USER);
        query.include(JournalEntry.KEY_TRIP);
        query.whereEqualTo(JournalEntry.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(JournalEntry.KEY_TRIP, selectedTrip);
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<JournalEntry>() {
            @Override
            public void done(List<JournalEntry> journals, ParseException e) {
                // save received posts to list and check if the posts have also loaded
                feedObjects.addAll(journals);
                ready += 1;
                //If both posts and journals have loaded, sort them based on created date
                if (ready == 2){
                    sortFeedObjects();
                }
            }
        });
    }

    /**
     * When both the posts and the journals have loaded, sort them before sending to the adapter class.
     */
    private void sortFeedObjects() {
        Collections.sort(feedObjects, new Comparator<FeedObjects>() {
            @Override
            public int compare(FeedObjects o1, FeedObjects o2) {
                Date date1 = null;
                Date date2 = null;
                if (o1.getType() == 101){
                    JournalEntry journal = (JournalEntry) o1;
                    date1 = journal.getCreatedAt();
                }
                else if (o1.getType() == 102) {
                    Post post = (Post) o1;
                    date1 = post.getCreatedAt();
                }

                if (o2.getType() == 101){
                    JournalEntry journal = (JournalEntry) o2;
                    date2 = journal.getCreatedAt();
                }
                else if (o2.getType() == 102) {
                    Post post = (Post) o2;
                    date2 = post.getCreatedAt();
                }
                return date2.compareTo(date1);
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        });
        adapter.notifyDataSetChanged();
    }
}