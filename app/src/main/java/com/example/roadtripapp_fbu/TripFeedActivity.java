package com.example.roadtripapp_fbu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.roadtripapp_fbu.Adapters.TripFeedAdapter;
import com.example.roadtripapp_fbu.Fragments.AddFriendsFragment;
import com.example.roadtripapp_fbu.Fragments.EditTripNameFragment;
import com.example.roadtripapp_fbu.Fragments.ProfileFragment;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.Objects.FeedObjects;
import com.example.roadtripapp_fbu.Objects.JournalEntry;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Displays the Feed for the current trip you have selected from Profile feed.
 * Sets onclick listener to start NewPost Activity.
 */
public class TripFeedActivity extends AppCompatActivity {
    public static final String TAG = "TripFeedActivity";
    public static final int REQUEST_CODE_POST = 20;
    public static final int REQUEST_CODE_JOURNAL = 40;
    RecyclerView rvTripPosts;
    List<FeedObjects> feedObjects;
    TripFeedAdapter adapter;
    public static Trip selectedTrip;
    int ready = 0;

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
        //StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTripPosts.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryPosts();
        queryJournals();
    }

    /** Inflates the menu for trip feed, and sets up for a search view to add friends*/
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
        //if add user is selected, start dialogue to find friends to add
        if (item.getItemId() == R.id.itemAddFriend) {
            showAddFriendsDialogue();
        }
        return super.onOptionsItemSelected(item);
    }

    // Call this method to launch the edit dialog
    private void showAddFriendsDialogue() {
        DialogFragment dialog = new AddFriendsFragment();
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "NEW ADD FRIENDS");
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

    /** Unwraps the posted updated, and populates the Trip Feed without a query.*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Make sure it is returning the same request we made earlier for post, and the result is ok
        if (requestCode == REQUEST_CODE_POST && resultCode == RESULT_OK){
            //get data from the intent and unwrap parcel
            Post post = Parcels.unwrap(data.getParcelableExtra("post"));
            //update the recycler view with the new post
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
            feedObjects.add(0, journal);
            //update the adapter
            adapter.notifyItemInserted(0);
            //scroll to the top of the recycler view
            rvTripPosts.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
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