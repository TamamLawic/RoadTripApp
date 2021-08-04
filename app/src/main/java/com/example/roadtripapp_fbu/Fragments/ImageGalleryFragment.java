package com.example.roadtripapp_fbu.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.roadtripapp_fbu.Adapters.ImageGalleryAdapter;
import com.example.roadtripapp_fbu.Adapters.TripFeedAdapter;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.R;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static com.example.roadtripapp_fbu.TripFeedActivity.selectedTrip;

/**
 * Fragment for TabView in TripFeed Activity, Shows all feed information for selected trip
 */
public class ImageGalleryFragment extends Fragment {
    RecyclerView rvImageGallery;
    ImageGalleryAdapter adapter;
    List<Post> allPosts;

    public ImageGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_gallery, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvImageGallery = view.findViewById(R.id.rvImageGallery);

        //set up recycler view for list of posts
        allPosts = new ArrayList<>();
        adapter = new ImageGalleryAdapter(getContext(), allPosts);
        rvImageGallery.setAdapter(adapter);

        //greedy image layout
        GreedoLayoutManager layoutManager = new GreedoLayoutManager(adapter);
        rvImageGallery.setLayoutManager(layoutManager);
        rvImageGallery.setAdapter(adapter);
        layoutManager.setMaxRowHeight(900);
        int spacing = 4;
        rvImageGallery.addItemDecoration(new GreedoSpacingItemDecoration(spacing));
        rvImageGallery.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryPosts();
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
                    allPosts.addAll(posts);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}