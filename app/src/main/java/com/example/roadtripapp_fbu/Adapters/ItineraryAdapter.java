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
import com.example.roadtripapp_fbu.PlaceDetailsActivity;
import com.example.roadtripapp_fbu.R;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.List;

/**
 * Adapter class SlideUpPanel to show the list of waypoints on the trip in order.
 * Sets up onclick listener to take user to the details page for the locations in the trip.
 */
public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ViewHolder> {
    Context context;
    List<Location> locations;

    public ItineraryAdapter(Context context, List<Location> locations) {
        this.context = context;
        this.locations = locations;
    }


    @Override
    public ItineraryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_itinerary, parent, false);
        return new ItineraryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItineraryAdapter.ViewHolder holder, int position) {
        Location location = locations.get(position);
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    /** ViewHolder class, that sets up posts for the FeedFragment recycler view.
     * Binds Posts using Parse to get the data, and Glide to bind it.*/
    //find and store references to the Text and Image views for the post
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvAddress2;
        private TextView tvAddress;
        private ImageView ivPlaceImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress2 = itemView.findViewById(R.id.tvAddress2);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            ivPlaceImage = itemView.findViewById(R.id.ivPlaceImage);
            itemView.setOnClickListener(this);
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images. */
        public void bind(Location location) {
            // Bind the post data to the view elements
            tvAddress.setText(location.getLocationName());
            tvAddress2.setText(location.getAddress());

            ParseFile image = location.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).centerCrop().into(ivPlaceImage);
            }
        }

        @Override
        public void onClick(View v) {
            //when place is clicked, take to details page
            // gets item position
            int position = getAdapterPosition();
            // makes sure the position exists before using intent to start TripFeed for selected Trip
            if (position != RecyclerView.NO_POSITION) {
                Location location = locations.get(position);
                Intent intent = new Intent(context, PlaceDetailsActivity.class);
                intent.putExtra(Location.class.getSimpleName(), Parcels.wrap(location));
                context.startActivity(intent);
            }
        }
    }
}
