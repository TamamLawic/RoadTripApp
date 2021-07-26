package com.example.roadtripapp_fbu.Fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.roadtripapp_fbu.Adapters.AddFriendsAdapter;
import com.example.roadtripapp_fbu.Adapters.PostAdapter;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.Objects.User;
import com.example.roadtripapp_fbu.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AddFriendsFragment extends DialogFragment {
    RecyclerView rvAddFriends;
    AddFriendsAdapter adapter;
    List<ParseUser> allUsers;

    public AddFriendsFragment() {
        // Required empty public constructor
    }

    public static AddFriendsFragment newInstance(String some_title) {
        AddFriendsFragment fragment = new AddFriendsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvAddFriends  = view.findViewById(R.id.rvAddFriends);
        //Set up the adapter for the trip recycler view
        allUsers = new ArrayList<>();
        //create the adapter
        adapter = new AddFriendsAdapter(getContext(), allUsers);
        //set the adapter on the recycler view
        rvAddFriends.setAdapter(adapter);
        rvAddFriends.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvAddFriends.setLayoutManager(layoutManager);
        // query posts from Instagram App
        queryUsers();
    }

    private void queryUsers() {
        // specify what type of data we want to query - Post.class
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        // include data referred by user
        query.include(Post.KEY_USER);
        // order users by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                // check for errors
                if (e != null) {
                    return;
                }
                else {
                    // save received posts to list and notify adapter of new data
                    allUsers.addAll(users);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}