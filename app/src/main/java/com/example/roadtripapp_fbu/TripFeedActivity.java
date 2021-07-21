package com.example.roadtripapp_fbu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.roadtripapp_fbu.Adapters.TripFeedAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the Feed for the current trip you have selected from Profile feed.
 * Sets onclick listener to start NewPost Actvity.
 */
public class TripFeedActivity extends AppCompatActivity {
    public static final String TAG = "TripFeedActivity";
    public static final int REQUEST_CODE_POST = 20;
    public static final int REQUEST_CODE_JOURNAL = 40;
    RecyclerView rvTripPosts;
    List<FeedObjects> feedObjects;
    TripFeedAdapter adapter;
    Trip selectedTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_feed);

        //use Parcels to unwrap trip selected
        //unwrap post's data from the pass
        selectedTrip = (Trip) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Trip.class.getSimpleName()));

        rvTripPosts = findViewById(R.id.rvPosts);
        //Set up the adapter for the trip recycler view
        feedObjects = new ArrayList<>();
        //create the adapter
        adapter = new TripFeedAdapter(this, feedObjects);
        //set the adapter on the recycler view
        rvTripPosts.setAdapter(adapter);
        rvTripPosts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTripPosts.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryPosts();
        queryJournals();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_trip_feed, menu);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(selectedTrip.getTripName());
        return true;
    }

    /**
     * When the Delete button is clicked, delete the current trip. When the new post is selected, start intent to NewPostActivity
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //if the selected menu item is new post, take to new post activity
        if (item.getItemId() == R.id.itemNewPost){
            //NewPost icon is tapped, start new post activity
            Intent i = new Intent(TripFeedActivity.this, NewPostActivity.class);
            // serialize the post using parceler, use its short name as a key
            i.putExtra(Trip.class.getSimpleName(), Parcels.wrap(selectedTrip));
            //startActivity(i);
            startActivityForResult(i, REQUEST_CODE_POST);
            return true;
        }
        //if the New Journal Entry Button is selected, take to journal entry activity
        if (item.getItemId() == R.id.itemNewJournal){
            //NewPost icon is tapped, start new post activity
            Intent i = new Intent(TripFeedActivity.this, NewJournalActivity.class);
            // serialize the post using parceler, use its short name as a key
            i.putExtra(Trip.class.getSimpleName(), Parcels.wrap(selectedTrip));
            //startActivity(i);
            startActivityForResult(i, REQUEST_CODE_JOURNAL);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                    feedObjects.addAll(posts);
                    adapter.notifyDataSetChanged();
                }

                // for debugging purposes let's print every post description to logcat
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getCaption() + ", trip: " + post.getTripId());
                }
            }
        });
    }

    /** Begins a Parse Query in a background thread, getting all posts for this trip. */
    /**The posts are added to a list, and the adapter is notified of the data change.*/
    protected void queryJournals() {
        // specify what type of data we want to query - Post.class
        ParseQuery<JournalEntry> query = ParseQuery.getQuery(JournalEntry.class);
        // include data referred by user key
        query.include(JournalEntry.KEY_USER);
        // include data referred by user key
        query.include(JournalEntry.KEY_TRIP);
        //only show the trip was selected
        query.whereEqualTo("tripId", selectedTrip);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<JournalEntry>() {
            @Override
            public void done(List<JournalEntry> journals, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                else {
                    // save received posts to list and notify adapter of new data
                    feedObjects.addAll(journals);
                    adapter.notifyDataSetChanged();
                }

                // for debugging purposes let's print every post description to logcat
                for (JournalEntry journal : journals) {
                    Log.i(TAG, "journal: " + journal.getText() + ", trip: " + journal.getTripId());
                }
            }
        });
    }

    /** Unwraps the posted updated, and populates the Trip Feed without a query.*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Make sure it is returning the same request we made earlier for post, and the result is ok
        if (requestCode == REQUEST_CODE_POST && resultCode == RESULT_OK){
            //get data from the intent and unwrap parcel
            Post post = Parcels.unwrap(data.getParcelableExtra("post"));
            //update the recycler view with the new post
            //modify data source
            feedObjects.add(0, post);
            //update the adapter
            adapter.notifyItemInserted(0);
            //scroll to the top of the recycler view
            rvTripPosts.smoothScrollToPosition(0);
        }
        //Make sure it is returning the same request we made earlier for journal, and the result is ok
        if (requestCode == REQUEST_CODE_JOURNAL && resultCode == RESULT_OK){
            //get data from the intent and unwrap parcel
            JournalEntry journal = Parcels.unwrap(data.getParcelableExtra("journal"));
            //update the recycler view with the new post
            //modify data source
            feedObjects.add(0, journal);
            //update the adapter
            adapter.notifyItemInserted(0);
            //scroll to the top of the recycler view
            rvTripPosts.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}