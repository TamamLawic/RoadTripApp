package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.PlaceDetailsActivity;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.ShowTripActivity;
import com.example.roadtripapp_fbu.UserProfileActivity;
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.*;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder> implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate {
    public static final String KEY_PROFILE = "profilePic";
    private Context context;
    private List<Post> posts;

    public ImageGalleryAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @Override
    public ImageGalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_gallery, parent, false);
        return new ImageGalleryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageGalleryAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public double aspectRatioForIndex(int i) {
        if (i >= posts.size()) {
            return 1;
        } else {
            try {
                File imageFile = posts.get(i).getImage().getFile();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                double imageHeight = options.outHeight;
                double imageWidth = options.outWidth;
                return imageWidth/imageHeight;

            } catch (ParseException e) {
                e.printStackTrace();
                return 1;
            }
        }
    }

    /** ViewHolder class, that sets up posts for the FeedFragment recycler view.
     * Binds Posts using Parse to get the data, and Glide to bind it.*/
    //find and store references to the Text and Image views for the post
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImageGallery;
        ConstraintLayout layoutImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImageGallery = itemView.findViewById(R.id.ivImageGallery);
            layoutImage = itemView.findViewById(R.id.layoutImage);
            //when the name of the trip is clicked, bring up details
            //If the location is clicked, show the location details page
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //when post is clicked, show bigger image

                }
            });
        }

        /**
         * Bind the post passed in into the item_post for the recycler view using Glide for images.
         */
        public void bind(Post post) {
            ParseFile image = post.getImage();
            if (image != null) {
                //load image into the background
                Glide.with(context).load(post.getImage().getUrl()).into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            layoutImage.setBackground(resource);
                        }
                    }
                });
            }
        }
    }}
