package com.example.roadtripapp_fbu.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.roadtripapp_fbu.Post;
import com.example.roadtripapp_fbu.PostAdapter;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Trip;
import com.example.roadtripapp_fbu.TripAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for bottom navigational view. ParseQuery to get the feed of the user, and display in a recycler view.
 */
public class FeedFragment extends Fragment {
    public static final String TAG = "FeedFragment";
    private List<Post> posts;
    private RecyclerView rvPosts;
    protected PostAdapter adapter;


    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvHomeFeed);

        //Set up the adapter for the trip recycler view
        posts = new ArrayList<>();
        //create the adapter
        adapter = new PostAdapter(getContext(), posts);
        //set the adapter on the recycler view
        rvPosts.setAdapter(adapter);
        rvPosts.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext() ,LinearLayoutManager.VERTICAL, false);
        rvPosts.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryPosts();
    }

    /** Begins a Parse Query in a background thread, getting all posts. */
    /**The posts are added to a list, and the adapter is notified of the data change.*/
    protected void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
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

                // for debugging purposes let's print every post description to logcat
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getCaption() + ", trip: " + post.getTripId());
                }

                // save received posts to list and notify adapter of new data
                posts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
}