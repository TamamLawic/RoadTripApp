package com.example.roadtripapp_fbu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.roadtripapp_fbu.Adapters.PagerAdapterFeed;
import com.example.roadtripapp_fbu.Fragments.AddFriendsFragment;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static androidx.core.app.FrameMetricsAggregator.DELAY_DURATION;

/**
 * Displays the Feed for the current trip you have selected from Profile feed.
 * Sets onclick listener to start NewPost Activity.
 */
public class TripFeedActivity extends AppCompatActivity {
    public static final String TAG = "TripFeedActivity";
    public static final int REQUEST_CODE_POST = 20;
    public static final int REQUEST_CODE_JOURNAL = 40;
    FloatingActionButton fabAdd;
    FloatingActionButton fabAddJournal;
    FloatingActionButton fabAddPost;
    FloatingActionButton fabAddCollaborators;
    TextView tvTripNameFeed;
    ImageButton btnBackTrip;
    ImageView ivTripImageFeed;
    TextView tvDurationFeed;
    TextView tvStopsFeed;
    TextView tvMilesFeed;
    public static Trip selectedTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_feed);

        btnBackTrip = findViewById(R.id.btnBackTrip);
        tvTripNameFeed = findViewById(R.id.tvTripNameFeed);
        ivTripImageFeed = findViewById(R.id.ivTripOverviewImage);
        tvDurationFeed = findViewById(R.id.tvDurationProfile);
        tvStopsFeed = findViewById(R.id.tvStopsProfile);
        tvMilesFeed = findViewById(R.id.tvMilesProfile);

        //use Parcels to unwrap trip selected
        selectedTrip = (Trip) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Trip.class.getSimpleName()));

        tvMilesFeed.setText(String.valueOf(selectedTrip.getLength()).concat(" Mi"));
        tvDurationFeed.setText(String.valueOf(selectedTrip.getTime()).concat(" Hr"));
        List<Location> locations = Location.getTripLocations(selectedTrip);
        tvStopsFeed.setText(String.valueOf(locations.size()));

        //set up floating action buttons
        setUpFloatingActionButtons();

        tvTripNameFeed.setText(selectedTrip.getTripName());
        ParseFile lastImage = locations.get(locations.size() - 1).getImage();
        setBlurImageToBackground(this, lastImage, ivTripImageFeed);

        //set up Back Button
        btnBackTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });

        //Set up the Tab View for Feed/Image Gallery
        ViewPager viewPager = (ViewPager) findViewById(R.id.pageView);
        PagerAdapterFeed myPagerAdapter = new PagerAdapterFeed(getSupportFragmentManager(), 0);
        viewPager.setAdapter(myPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabBar);
        tabLayout.setupWithViewPager(viewPager);
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

//    /** Unwraps the posted updated, and populates the Trip Feed without a query.*/
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        //Make sure it is returning the same request we made earlier for post, and the result is ok
//        if (requestCode == REQUEST_CODE_POST && resultCode == RESULT_OK){
//            //get data from the intent and unwrap parcel
//            Post post = Parcels.unwrap(data.getParcelableExtra("post"));
//            //update the recycler view with the new post
//            feedObjects.add(0, post);
//            //update the adapter
//            adapter.notifyItemInserted(0);
//            //scroll to the top of the recycler view
//            rvTripPosts.smoothScrollToPosition(0);
//        }
//        //Make sure it is returning the same request we made earlier for journal, and the result is ok
//        if (requestCode == REQUEST_CODE_JOURNAL && resultCode == RESULT_OK){
//            //get data from the intent and unwrap parcel
//            JournalEntry journal = Parcels.unwrap(data.getParcelableExtra("journal"));
//            //update the recycler view with the new post
//            feedObjects.add(0, journal);
//            //update the adapter
//            adapter.notifyItemInserted(0);
//            //scroll to the top of the recycler view
//            rvTripPosts.smoothScrollToPosition(0);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

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