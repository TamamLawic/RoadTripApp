package com.example.roadtripapp_fbu.Fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.example.roadtripapp_fbu.Adapters.AddFriendsAdapter;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.TripFeedActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 *Dialog fragment uses Parse to find users to collaborate on the trip, and display the users who are currently collaborators
 */
public class AddFriendsFragment extends DialogFragment {
    RecyclerView rvAddFriends;
    RecyclerView rvCollaborators;
    AddFriendsAdapter collaboratorAdapter;
    List<ParseUser> collaboratorsUsers;
    List<Collaborator> collaborators;
    AddFriendsAdapter adapter;
    List<ParseUser> allUsers;
    EditText etFindFriends;

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

        etFindFriends = view.findViewById(R.id.etFindFriends);
        etFindFriends.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //set up the collaborators recycler view
        rvCollaborators  = view.findViewById(R.id.rvCollaborators);
        //Set up the adapter for the collaborator recycler view
        collaboratorsUsers = new ArrayList<>();
        //create the adapter
        collaboratorAdapter = new AddFriendsAdapter(getContext(), collaboratorsUsers);
        //set the adapter on the recycler view
        rvCollaborators.setAdapter(collaboratorAdapter);
        rvCollaborators.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCollaborators.setLayoutManager(layoutManager2);
        // query All of the users who are collaborators on the trip
        queryCollaborators();

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
        // queries all of the users
        queryUsers();
    }

    /** Parse Query to get all of the collaborator on a trip*/
    private void queryCollaborators() {
        collaborators = new ArrayList<>();
        // specify what type of data we want to query - User.class
        ParseQuery<Collaborator> query = ParseQuery.getQuery(Collaborator.class);
        // include data referred by user
        query.include(Collaborator.KEY_USER);
        // include data referred by user
        query.whereEqualTo(Collaborator.KEY_TRIP, TripFeedActivity.selectedTrip);
        // order users by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Collaborator>() {
            @Override
            public void done(List<Collaborator> collaboratorsList, ParseException e) {
                // check for errors
                if (e != null) {
                    return;
                }
                else {
                    // save received posts to list and notify adapter of new data
                    collaborators.addAll(collaboratorsList);
                    getUsers();
                }
            }
        });
    }

    /** Gets all of the user Objects from the list of collaborators*/
    private void getUsers() {
        for (int i = 0; i < collaborators.size(); i ++) {
            collaboratorsUsers.add(collaborators.get(i).getUser());
        }
        collaboratorAdapter.notifyDataSetChanged();
    }

    /**
     * Parse Query to find profile information for all not currently a collaborator, to add to recycler view.
     */
    private void queryUsers() {
        // specify what type of data we want to query - User.class
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
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}