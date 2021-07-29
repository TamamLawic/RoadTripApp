package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.BuildConfig;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.PlaceDetailsActivity;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.ShowTripActivity;
import com.example.roadtripapp_fbu.UserProfileActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.JsonArray;
import com.google.maps.model.Photo;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
/** Adapter class for the suggested places recycler view in MapFragment.*/
public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
    private PlacesClient placesClient;
    List<JSONObject> suggestedPlaces;
    Context context;

    public SuggestionsAdapter(Context context, List<JSONObject> places) {
        this.context = context;
        this.suggestedPlaces = places;
    }

    @Override
    public SuggestionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggested_locations, parent, false);
        // Setup Places Client
        Places.initialize(context, BuildConfig.GOOGLE_API_KEY);
        //get new Places client
        placesClient = Places.createClient(context);
        return new SuggestionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionsAdapter.ViewHolder holder, int position) {
        JSONObject place = suggestedPlaces.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return suggestedPlaces.size();
    }

    /** ViewHolder class, that sets up suggested places for the user to add to their trip*/
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivSuggestedImage;
        TextView tvSuggestionName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSuggestedImage = itemView.findViewById(R.id.ivSuggestedImage);
            tvSuggestionName = itemView.findViewById(R.id.tvSuggestionName);
            itemView.setOnClickListener(this);
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images.
         * @param place*/
        public void bind(JSONObject place) {
            String photoMetadata = null;
            // Bind the post data to the view elements
            try {
                tvSuggestionName.setText(place.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //get the image data from google places
            try {
                JSONArray photoData = place.getJSONArray("photos");
                Photo photo = (Photo) photoData.get(0);
                photoMetadata = photo.photoReference;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //make request for photo data
        }

        @Override
        public void onClick(View v) {
        }
    }
}
