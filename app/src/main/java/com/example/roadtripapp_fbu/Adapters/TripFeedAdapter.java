package com.example.roadtripapp_fbu.Adapters;

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
import com.example.roadtripapp_fbu.FeedObjects;
import com.example.roadtripapp_fbu.JournalEntry;
import com.example.roadtripapp_fbu.Post;
import com.example.roadtripapp_fbu.R;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/** Adapter for the TripFeedActivity, binds selected Trip's posts into the recycler view, using Glide for images. */
public class TripFeedAdapter extends RecyclerView.Adapter {
    public static final String KEY_PROFILE = "profilePic";
    private List<FeedObjects> feedObjects;
    Context context;

    public TripFeedAdapter(Context context, List<FeedObjects> feedObjects) {
        this.context = context;
        this.feedObjects = feedObjects;
    }

    /** Seperate ViewHolder and Binder for the Post Objects*/
    class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername;
        private ImageView ivImage;
        private TextView tvDescription;
        private TextView tvTime;
        private ImageView ivProfile;
        TextView tvTripNamePost;

        /** View Holder for the Post Objects*/
        public PostViewHolder(View itemView) {
            super(itemView);
            // get reference to views
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivPostImage);
            tvDescription = itemView.findViewById(R.id.tvCaption);
            tvTime = itemView.findViewById(R.id.tvTimeStamp);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvTripNamePost = itemView.findViewById(R.id.tvTripNamePost);
        }

        void bindView(int position) {
            Post post = (Post) feedObjects.get(position);
            // bind data to the views
            tvDescription.setText(post.getCaption());
            tvUsername.setText(post.getUser().getUsername());
            tvTripNamePost.setText(post.getTripId().getString("tripName"));
            //bind time since the post was posted
            Date createdAt = post.getCreatedAt();
            String timeAgo = post.calculateTimeAgo(createdAt);
            tvTime.setText(timeAgo);
            ParseFile image = post.getImage();
            if (image != null && context!= null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
            //set profile image for the signed in user
            ParseFile profileImage = post.getUser().getParseFile(KEY_PROFILE);
            if (context!= null) {
                Glide.with(context)
                        .load(profileImage.getUrl())
                        .circleCrop()
                        .into(ivProfile);
            }
        }
    }
    /** Seperate View Holder for the Journal Objects in the TripFeed*/
    class JournalViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvCaption;
        TextView tvTimeStampJournal;

        public JournalViewHolder(View itemView) {
            super(itemView);
            // get reference to views
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvTimeStampJournal = itemView.findViewById(R.id.tvTimeStampJournal);
        }

        void bindView(int position) {
            JournalEntry journal = (JournalEntry) feedObjects.get(position);
            // bind data to the views
            tvTitle.setText(journal.getTitle());
            tvCaption.setText(journal.getText());
            Date createdAt = journal.getCreatedAt();
            String timePosted = Post.calculateTimeAgo(createdAt);
            tvTimeStampJournal.setText(timePosted);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return feedObjects.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case FeedObjects.TYPE_JOURNAL:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_journal_entry, parent, false);
                return new JournalViewHolder(itemView);
            default: // TYPE_POST
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_post, parent, false);
                return new PostViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case FeedObjects.TYPE_JOURNAL:
                ((JournalViewHolder) holder).bindView(position);
                break;
            case FeedObjects.TYPE_POST:
                ((PostViewHolder) holder).bindView(position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return feedObjects.size();
    }

    public void setLiteratureList(List<? extends FeedObjects> literatureList) {
        if (feedObjects == null){
            feedObjects = new ArrayList<>();
        }
        feedObjects.clear();
        feedObjects.addAll(literatureList);
        notifyDataSetChanged();
    }

}
