package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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

import java.util.List;
/**
 * Adapter class for AddFriendsFragment. Shows basic profile information for all users to add to trip collaborators.
 */
public class AddFriendsAdapter extends RecyclerView.Adapter<AddFriendsAdapter.ViewHolder> {
    List<ParseUser> users;
    Context context;

    public AddFriendsAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public AddFriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_friend, parent, false);
        return new AddFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendsAdapter.ViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
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
                    collaborator.setUser(users.get(getAdapterPosition()));
                    collaborator.setTrip(TripFeedActivity.selectedTrip);
                    collaborator.saveInBackground();
                    Toast.makeText(itemView.getContext(), "Added to trip : ".concat(users.get(getAdapterPosition()).getUsername()), Toast.LENGTH_SHORT).show();
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
