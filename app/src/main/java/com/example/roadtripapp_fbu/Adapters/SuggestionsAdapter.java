package com.example.roadtripapp_fbu.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.roadtripapp_fbu.BuildConfig;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.R;
import com.google.android.libraries.places.api.Places;
import com.google.gson.JsonObject;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static androidx.core.app.FrameMetricsAggregator.DELAY_DURATION;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/** Adapter class for the suggested places recycler view in MapFragment.*/
public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
    List<JSONObject> suggestedPlaces;
    List<Bitmap> placesImages;
    Context context;
    EventListener listener;
    List<JSONObject> placesAdded;

    public SuggestionsAdapter(Context context, List<JSONObject> places, EventListener listener) {
        this.context = context;
        this.suggestedPlaces = places;
        this.placesImages = new ArrayList<>();
        this.listener = listener;
        this.placesAdded = new ArrayList<JSONObject>();
    }

    @Override
    public SuggestionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggested_locations, parent, false);
        // Setup Places Client
        Places.initialize(context, BuildConfig.GOOGLE_API_KEY);
        return new SuggestionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionsAdapter.ViewHolder holder, int position) {
        JSONObject place = suggestedPlaces.get(position);
        holder.bind(place);
        holder.btnAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //addLocationTrip(position);
                //if the button is add, add to trip
                if (holder.btnAddLocation.getTag().toString().equals("Add")) {
                    holder.btnAddLocation.setTag("Remove");
                    holder.btnAddLocation.setImageResource(R.drawable. outline_remove_circle_outline_24);
                    placesAdded.add(place);
                }
                else{
                    holder.btnAddLocation.setImageResource(R.drawable. outline_add_24);
                    holder.btnAddLocation.setTag("Add");
                    placesAdded.remove(place);
                }
                //notify map that the data has changed for places to stop
                listener.onEvent(placesAdded, placesImages);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestedPlaces.size();
    }

    /** ViewHolder class, that sets up suggested places for the user to add to their trip*/
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSuggestedImage;
        TextView tvSuggestionName;
        ImageButton btnAddLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSuggestedImage = itemView.findViewById(R.id.ivSuggestedImage);
            tvSuggestionName = itemView.findViewById(R.id.tvSuggestionName);
            btnAddLocation = itemView.findViewById(R.id.btnAddLocation);
        }

        /** Bind the post passed in into the item_post for the recycler view using Glide for images.
         * @param place*/
        public void bind(JSONObject place) {
            String photoMetadata = null;
            btnAddLocation.setVisibility(View.VISIBLE);
            // Bind the post data to the view elements
            try {
                tvSuggestionName.setText(place.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //get the image data from google places
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            try {
                //get the data
                StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
                googlePlacesUrl.append("maxwidth=").append(3000);
                //change the radius for the search
                googlePlacesUrl.append("&photoreference=").append(place.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));
                googlePlacesUrl.append("&types=").append("restaurant");
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + BuildConfig.GOOGLE_API_KEY);

                // Initialize a new ImageRequest
                ImageRequest imageRequest = new ImageRequest(
                        googlePlacesUrl.toString(), // Image URL
                        new Response.Listener<Bitmap>() { // Bitmap listener
                            @Override
                            public void onResponse(Bitmap response) {
                                // Do something with response
                                placesImages.add(response);
                                RequestOptions requestOptions = new RequestOptions();
                                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                                Glide.with(itemView).asBitmap().apply(requestOptions).load(response).into(ivSuggestedImage);
                            }
                        },
                        0, // Image width
                        0, // Image height
                        ImageView.ScaleType.FIT_CENTER, // Image scale type
                        Bitmap.Config.RGB_565, //Image decode configuration
                        new Response.ErrorListener() { // Error listener
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Do something with error response
                                error.printStackTrace();
                            }
                        }
                );
            // Add ImageRequest to the RequestQueue
            requestQueue.add(imageRequest);
        } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /** Event Listener triggered in Map Fragment when a location is clicked.
     * Updates the list of suggested location the user wants to add or remove from their trip.*/
    public interface EventListener {
        void onEvent(List<JSONObject> stops, List<Bitmap> placeImages);
    }
}
