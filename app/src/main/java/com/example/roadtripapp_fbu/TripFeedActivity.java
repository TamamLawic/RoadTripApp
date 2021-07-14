package com.example.roadtripapp_fbu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.roadtripapp_fbu.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the Feed for the current trip you have selected from Profile feed.
 * Sets onclick listener to start NewPost Actvity.
 */
public class TripFeedActivity extends AppCompatActivity {
    public static final String TAG = "TripFeedActivity";
    Button btnNewPost;
    RecyclerView rvTripPosts;
    List<Post> tripPosts;
    TripFeedAdapter adapter;
    Trip selectedTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_feed);

        btnNewPost = findViewById(R.id.btnNewPost);
        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TripFeedActivity.this, NewPostActivity.class);
                //TODO: Add the TripID into the post activity
                startActivity(i);
            }
        });

        //use Parcels to unwrap trip selected
        //unwrap post's data from the pass
        selectedTrip = (Trip) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Trip.class.getSimpleName()));

        rvTripPosts = findViewById(R.id.rvPosts);
        //Set up the adapter for the trip recycler view
        tripPosts = new ArrayList<>();
        //create the adapter
        adapter = new TripFeedAdapter(this, tripPosts);
        //set the adapter on the recycler view
        rvTripPosts.setAdapter(adapter);
        rvTripPosts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTripPosts.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryPosts();
    }

    /** Begins a Parse Query in a background thread, getting all posts for this trip. */
    /**The posts are added to a list, and the adapter is notified of the data change.*/
    protected void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_USER);
        // include data referred by user key
        query.include(Post.KEY_TRIP);
        //only show the trip was selected
        query.whereEqualTo("tripId", selectedTrip);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                else {
                    // save received posts to list and notify adapter of new data
                    tripPosts.addAll(posts);
                    adapter.notifyDataSetChanged();
                }

                // for debugging purposes let's print every post description to logcat
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getCaption() + ", trip: " + post.getTripId());
                }
            }
        });
    }
}