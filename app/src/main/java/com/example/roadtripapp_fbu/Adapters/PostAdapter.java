package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Location;
import com.example.roadtripapp_fbu.PlaceDetailsActivity;
import com.example.roadtripapp_fbu.Post;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.ShowTripActivity;
import com.example.roadtripapp_fbu.Trip;
import com.google.android.libraries.places.api.model.Place;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

/** Adapter class for FeedFragment*/
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public static final String KEY_PROFILE = "profilePic";
    private Context context;
    private List<Post> posts;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /** ViewHolder class, that sets up posts for the FeedFragment recycler view.
     * Binds Posts using Parse to get the data, and Glide to bind it.*/
    //find and store references to the Text and Image views for the post
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        ImageView ivImage;
        TextView tvTime;
        ImageView ivProfile;
        TextView tvCaption;
        TextView tvTripNamePost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivPostImage);
            tvTime = itemView.findViewById(R.id.tvTimeStamp);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvTripNamePost = itemView.findViewById(R.id.tvTripNamePost);
            //when the name of the trip is clicked, bring up details
            //If the location is cliked, show the location details page
            tvTripNamePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Post post = posts.get(getAdapterPosition());
                    if (post.getLocation() != null) {
                        Location location = (Location) post.getLocation();
                        Intent intent = new Intent(context, PlaceDetailsActivity.class);
                        intent.putExtra(Location.class.getSimpleName(), Parcels.wrap(location));
                        context.startActivity(intent);
                    }
                    else {
                        Trip trip = (Trip) posts.get(getAdapterPosition()).getTripId();
                        Intent intent = new Intent(context, ShowTripActivity.class);
                        intent.putExtra("trip", Parcels.wrap(trip));
                        context.startActivity(intent);
                    }
                }
            });
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images. */
        public void bind(Post post) {
            // Bind the post data to the view elements
            tvCaption.setText(post.getCaption());
            tvUsername.setText(post.getUser().getUsername());
            Trip trip = (Trip) post.getTripId();
            //If there is a location associated to the post, show the location instead of the trip
            if (post.getLocation() != null) {
                tvTripNamePost.setText(post.getLocation().getString("locationName"));
            }
            else {
                tvTripNamePost.setText(trip.getTripName());
            }
            //bind time since the post was posted
            Date createdAt = post.getCreatedAt();
            String timeAgo = post.calculateTimeAgo(createdAt);
            tvTime.setText(timeAgo);
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
            //set profile image for the signed in user
            ParseFile profileImage = post.getUser().getParseFile(KEY_PROFILE);
            Glide.with(context)
                    .load(profileImage.getUrl())
                    .circleCrop()
                    .into(ivProfile);
        }
    }

    // Clean all elements of the recycler for endless scrolling
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

}
