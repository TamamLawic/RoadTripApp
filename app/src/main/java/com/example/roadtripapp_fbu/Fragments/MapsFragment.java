package com.example.roadtripapp_fbu.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.roadtripapp_fbu.Location;
import com.example.roadtripapp_fbu.NewPostActivity;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Trip;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment for bottom navigational view. Makes Google Map object, and populates with the user's current Trip using ParseQuery.
 */
public class MapsFragment extends Fragment {
    private PlacesClient placesClient;
    public static final String TAG = "MapFragment";
    GoogleMap tripMap;
    List<Location> locations;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //create bounds for start or map, center around the US
            LatLngBounds bounds = new LatLngBounds(
                    new LatLng(25.82, -124.39),
                    new LatLng(49.38, -66.94)
            );
            //when the map is ready, add the markers for the current trip
            tripMap = googleMap;
            locations = Location.getTripLocations(Trip.getCurrentTrip());
            for (int i = 0; i < locations.size(); i++) {
                Location location = locations.get(i);
                LatLng latLng = new LatLng(location.getLatitude().doubleValue(), location.getLongitude().doubleValue());
                tripMap.addMarker(new MarkerOptions().position(latLng).title(location.getLocationName()));
                tripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 3));
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set up map view
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        //initializes Places APi
        // Setup Places Client
        Places.initialize(getContext(), getString(R.string.maps_api_key));
        //get new Places client
        placesClient = Places.createClient(getContext());
        //set up the autocomplete fragment and cast to autocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete);
        //set the type of places you want to autocomplete
        autocompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);
        //set a location bias for completions
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184364),
                new LatLng(-33.858754, 151.229596)));
        autocompleteFragment.setCountries("US");
        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //when location is clicked, add it to the trip
                addPlaceToTrip(place);
            }
            @Override
            public void onError(@NonNull Status status) {
            }
        });
    }

    /**
     * Creates a new Location, and populates with the selected place's data. Shows the current map with the pin added
     */
    private void addPlaceToTrip(Place place) {
        Location location = new Location();
        LatLng latLng = place.getLatLng();
        location.setLatitude(latLng.latitude);
        location.setLocationName(place.getName());
        location.setLongitude(latLng.longitude);
        location.setTripId(Trip.getCurrentTrip());
        location.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tripMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
        tripMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * When the options menu is created, change it to the one for the maps fragment
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_maps_fragment, menu);
        //change the title to show the trip you are editing/viewing
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(Trip.getCurrentTrip().getTripName());
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * When the MenuItem post is selected startActivity : NewPostActivity
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(getContext(), NewPostActivity.class);
        // serialize the post using parceler, use its short name as a key
        i.putExtra(Trip.class.getSimpleName(), Parcels.wrap(Trip.getCurrentTrip()));
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }
}