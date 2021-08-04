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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.MeasureUnit;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.roadtripapp_fbu.Adapters.TripFeedAdapter;
import com.example.roadtripapp_fbu.Fragments.AddFriendsFragment;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.Objects.FeedObjects;
import com.example.roadtripapp_fbu.Objects.JournalEntry;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static androidx.core.app.FrameMetricsAggregator.DELAY_DURATION;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * Displays the Feed for the current trip you have selected from Profile feed.
 * Sets onclick listener to start NewPost Activity.
 */
public class TripFeedActivity extends AppCompatActivity {
    public static final String TAG = "TripFeedActivity";
    public static final int REQUEST_CODE_POST = 20;
    public static final int REQUEST_CODE_JOURNAL = 40;
    RecyclerView rvTripPosts;
    FloatingActionButton fabAdd;
    FloatingActionButton fabAddJournal;
    FloatingActionButton fabAddPost;
    FloatingActionButton fabAddCollaborators;
    TextView tvTripNameFeed;
    ImageButton btnBackTrip;
    ImageView ivTripImageFeed;
    List<FeedObjects> feedObjects;
    TripFeedAdapter adapter;
    TextView tvDurationFeed;
    TextView tvStopsFeed;
    TextView tvMilesFeed;

    public static Trip selectedTrip;
    int ready = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_feed);

        btnBackTrip = findViewById(R.id.btnBackTrip);
        tvTripNameFeed = findViewById(R.id.tvTripNameFeed);
        ivTripImageFeed = findViewById(R.id.ivTripOverviewImage);
        rvTripPosts = findViewById(R.id.rvPosts);
        tvDurationFeed = findViewById(R.id.tvDurationFeed);
        tvStopsFeed = findViewById(R.id.tvStopsFeed);
        tvMilesFeed = findViewById(R.id.tvMilesFeed);

        //use Parcels to unwrap trip selected
        selectedTrip = (Trip) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Trip.class.getSimpleName()));

        tvMilesFeed.setText(String.valueOf(selectedTrip.getLength()).concat(" Mi"));
        tvDurationFeed.setText(String.valueOf(selectedTrip.getTime()).concat(" Hr"));
        List<Location> locations = Location.getTripLocations(selectedTrip);
        tvStopsFeed.setText(String.valueOf(locations.size()));

        //set up recycler view for list of posts
        feedObjects = new ArrayList<>();
        adapter = new TripFeedAdapter(this, feedObjects);
        rvTripPosts.setAdapter(adapter);
        rvTripPosts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTripPosts.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryPosts();
        queryJournals();

        //set up floating action buttons
        setUpFloatingActionButtons();

        tvTripNameFeed.setText(selectedTrip.getTripName());
        ParseFile lastImage = locations.get(locations.size() - 2).getImage();
        setBlurImageToBackground(this, lastImage, ivTripImageFeed);

        //set up Back Button
        btnBackTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setUpFloatingActionButtons() {
        fabAdd = findViewById(R.id.fabAdd);
        fabAddJournal = findViewById(R.id.fabAddJournal);
        fabAddPost = findViewById(R.id.fabAddPost);
        fabAddCollaborators = findViewById(R.id.fabAddCollaborators);

        //show all of the buttons when the add button is clicked
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabAddCollaborators.getVisibility() == View.VISIBLE){
                    fabAddCollaborators.setVisibility(View.INVISIBLE);
                    fabAddJournal.setVisibility(View.INVISIBLE);
                    fabAddPost.setVisibility(View.INVISIBLE);
                }
                else {
                    fabAddCollaborators.setVisibility(View.VISIBLE);
                    fabAddJournal.setVisibility(View.VISIBLE);
                    fabAddPost.setVisibility(View.VISIBLE);
                }
            }
        });

        //when add journal is clicked, show journal
        fabAddJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NewJournal icon is tapped, start new post activity
                Intent i = new Intent(TripFeedActivity.this, NewJournalActivity.class);
                // serialize the post using parceler, use its short name as a key
                i.putExtra(Trip.class.getSimpleName(), Parcels.wrap(selectedTrip));
                //startActivity(i);
                startActivityForResult(i, REQUEST_CODE_JOURNAL);
            }
        });

        //when add post is clicked, launch on new post
        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NewPost icon is tapped, start new post activity
                Intent i = new Intent(TripFeedActivity.this, NewPostActivity.class);
                // serialize the post using parceler, use its short name as a key
                i.putExtra(Trip.class.getSimpleName(), Parcels.wrap(selectedTrip));
                //startActivity(i);
                startActivityForResult(i, REQUEST_CODE_POST);
            }
        });

        fabAddCollaborators.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFriendsDialogue();
            }
        });

    }

    /** Inflates the menu for trip feed, and sets up for a search view to add friends*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(selectedTrip.getTripName());
        return true;
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


    public static void setBlurImageToBackground(final Context context, final ParseFile path, final ImageView imageView) {
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new BlurTransformation(25));
                Glide.with(context).load(path.getUrl())
                        .apply(requestOptions)
                        .into(imageView);
            }
        }, DELAY_DURATION);
    }
}