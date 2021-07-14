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
import com.parse.ParseObject;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private Context context;
    private List<Trip> trips;

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
        private TextView tvTripNameProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripNameProfile = itemView.findViewById(R.id.tvTripNameProfile);
            itemView.setOnClickListener(this);
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images.
         * @param trip*/
        public void bind(Trip trip) {
            // Bind the post data to the view elements
            tvTripNameProfile.setText(trip.getString("tripName"));
        }

        /** When the post is clicked, wrap trip data using Parcels and start TripFeedActivity sending it with the wrapped trip. */
        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // makes sure the position exists
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position
                Trip trip = trips.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, TripFeedActivity.class);
                // serialize the post using parceler, use its short name as a key
                intent.putExtra(Trip.class.getSimpleName(), Parcels.wrap(trip));
                // display activity
                context.startActivity(intent);
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
