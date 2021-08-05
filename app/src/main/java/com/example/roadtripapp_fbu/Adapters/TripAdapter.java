package com.example.roadtripapp_fbu.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Fragments.ProfileFragment;
import com.example.roadtripapp_fbu.MainActivity;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.TripFeedActivity;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.List;

/**
 * Adapter Class for the TabViews Trip Page, shows all Trips the user is a collaborator on.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private Context context;
    private List<Trip> trips;
    public static int totalMiles = 0;
    public static int totalStops = 0;
    public static int totalDuration = 0;

    public TripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripAdapter.ViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    /** Viewholder class, that sets up posts for the ProfileFragment recycler view.
     * Binds Posts using Parse to get the data.*/
    //find and store references to the Text and Image views for the post
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTripNameProfile;
        ImageView ivDestinationTrip;
        TextView tvStopsTrip;
        TextView tvMilesTrip;
        TextView tvDurationTrip;
        ImageView ivStopsIconTrip;
        ImageView imageViewTrip;
        ImageView imageView2Trip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripNameProfile = itemView.findViewById(R.id.tvTripNameProfile);
            ivDestinationTrip = itemView.findViewById(R.id.ivDestinationTrip);
            tvStopsTrip = itemView.findViewById(R.id.tvStopsTrip);
            tvMilesTrip = itemView.findViewById(R.id.tvMilesTrip);
            tvDurationTrip = itemView.findViewById(R.id.tvDurationTrip);
            ivStopsIconTrip = itemView.findViewById(R.id.ivStopsIconTrip);
            imageViewTrip = itemView.findViewById(R.id.imageViewTrip);
            imageView2Trip = itemView.findViewById(R.id.imageView2Trip);
            itemView.setOnClickListener(this);
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images.
         * @param trip*/
        public void bind(Trip trip) {
            // Bind the post data to the view elements
            tvTripNameProfile.setText(trip.getTripName());
            double miles = trip.getLength();
            totalMiles += miles;
            tvMilesTrip.setText(String.valueOf(miles).concat(" Mi"));
            double time = trip.getTime();
            totalDuration += time;
            tvDurationTrip.setText(String.valueOf(time).concat(" Hr"));
            List<Location> locations = Location.getTripLocations(trip);
            double stops = locations.size();
            totalStops += stops;
            tvStopsTrip.setText(String.valueOf(stops));
            if (locations.size() > 0){
                ParseFile destinationImage = locations.get(locations.size() - 1).getImage();
                Glide.with(context)
                        .load(destinationImage.getUrl())
                        .centerCrop()
                        .into(ivDestinationTrip);
            }
        }

        /** When the post is clicked, wrap trip data using Parcels and start TripFeedActivity sending it with the wrapped trip. */
        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // makes sure the position exists before using intent to start TripFeed for selected Trip
            if (position != RecyclerView.NO_POSITION) {
                Trip trip = trips.get(position);
                Intent intent = new Intent(context, TripFeedActivity.class);
                intent.putExtra(Trip.class.getSimpleName(), Parcels.wrap(trip));
                //shared element transition data
                Pair<View, String> p1 = Pair.create((View)tvTripNameProfile, "tripName");
                Pair<View, String> p2 = Pair.create((View)ivDestinationTrip, "destination");
                Pair<View, String> p3 = Pair.create((View)ivStopsIconTrip, "stopIcon");
                Pair<View, String> p4 = Pair.create((View)imageViewTrip, "timeIcon");
                Pair<View, String> p5 = Pair.create((View)imageView2Trip, "carIcon");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) context, p1, p2, p3, p4, p5);
                context.startActivity(intent, options.toBundle());
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        trips.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Trip> list) {
        trips.addAll(list);
        notifyDataSetChanged();
    }
}
