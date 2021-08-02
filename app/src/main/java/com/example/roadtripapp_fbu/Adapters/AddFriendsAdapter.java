package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Fragments.AddFriendsFragment;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.TripFeedActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
/**
 * Adapter class for AddFriendsFragment. Shows basic profile information for all users to add to trip collaborators.
 */
public class AddFriendsAdapter extends RecyclerView.Adapter<AddFriendsAdapter.ViewHolder> implements Filterable {
    Context context;
    private final List<ParseUser> userList;
    private FindUserFilter userFilter;

    public AddFriendsAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.userList = users;
    }

    @Override
    public AddFriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_friend, parent, false);
        return new AddFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendsAdapter.ViewHolder holder, int position) {
        ParseUser user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public Filter getFilter() {
        if(userFilter == null)
            userFilter = new FindUserFilter(this, userList);
        return userFilter;
    }

    /** Filterable, that returns all of the users that start with the string typed in*/
    private static class FindUserFilter extends Filter {
        private final AddFriendsAdapter adapter;
        private final List<ParseUser> originalList;
        private final List<ParseUser> filteredList;

        private FindUserFilter(AddFriendsAdapter adapter, List<ParseUser> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final ParseUser user : originalList) {
                    if (user.getUsername().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.userList.clear();
            adapter.userList.addAll((ArrayList<ParseUser>) results.values);
            adapter.notifyDataSetChanged();
        }
    }

    /** Sets up view for adding friends, also sets onclick listener for adding friend*/
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsernameAdd;
        ImageView ivProfileImageAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsernameAdd = itemView.findViewById(R.id.tvUsernameAdd);
            ivProfileImageAdd = itemView.findViewById(R.id.ivProfileImageAdd);
            //when the name of the trip is clicked, bring up details
            //If the location is clicked, show the location details page
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //When the user is clicked, add them to the trip collaborators
                    Collaborator collaborator = new Collaborator();
                    collaborator.setUser(userList.get(getAdapterPosition()));
                    collaborator.setTrip(TripFeedActivity.selectedTrip);
                    collaborator.saveInBackground();
                    Toast.makeText(itemView.getContext(), "Added to trip : ".concat(userList.get(getAdapterPosition()).getUsername()), Toast.LENGTH_SHORT).show();
                }
            });
        }

        /** Bind the post passed in into the item_add_friend for the recycler view using Glide for images. */
        public void bind(ParseUser user) {
            // Bind the post data to the view elements
            tvUsernameAdd.setText(user.getUsername());
            //set profile image for the user
            ParseFile profileImage = user.getParseFile("profilePic");
            Glide.with(context)
                    .load(profileImage.getUrl())
                    .circleCrop()
                    .into(ivProfileImageAdd);
        }
    }
}
