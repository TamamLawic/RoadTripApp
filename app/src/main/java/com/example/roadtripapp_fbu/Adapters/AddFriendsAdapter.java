package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.Objects.User;
import com.example.roadtripapp_fbu.PlaceDetailsActivity;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.ShowTripActivity;
import com.example.roadtripapp_fbu.UserProfileActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

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
            //If the location is cliked, show the location details page
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images. */
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
