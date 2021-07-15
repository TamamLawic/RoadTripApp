package com.example.roadtripapp_fbu;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;
/** Adapter for the TripFeedActivity, binds selected Trip's posts into the recycler view, using Glide for images. */
public class TripFeedAdapter extends RecyclerView.Adapter<TripFeedAdapter.ViewHolder> {
    public static final String KEY_PROFILE = "profilePic";
    Context context;
    List<Post> posts;

    public TripFeedAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripFeedAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername;
        private ImageView ivImage;
        private TextView tvDescription;
        private TextView tvTime;
        private ImageView ivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivPostImage);
            tvDescription = itemView.findViewById(R.id.tvCaption);
            tvTime = itemView.findViewById(R.id.tvTimeStamp);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images. */
        public void bind(Post post) {
            // Bind the post data to the view elements
            tvDescription.setText(post.getCaption());
            tvUsername.setText(post.getUser().getUsername());
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
}
