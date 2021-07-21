package com.example.roadtripapp_fbu.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.roadtripapp_fbu.BuildConfig;
import com.example.roadtripapp_fbu.Location;
import com.example.roadtripapp_fbu.NewPostActivity;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Trip;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.google.maps.DirectionsApi;


import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for bottom navigational view. Makes Google Map object, and populates with the user's current Trip using ParseQuery.
 */
public class MapsFragment extends Fragment {
    private PlacesClient placesClient;
    public static final String TAG = "MapFragment";
    GoogleMap tripMap;
    List<Location> locations;
    private static final int overview = 0;

    /** Loads Current trip data to display the current trip on a map with markers, and direction polylines connecting*/
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
                LatLng latLng1 = new LatLng(location.getLatitude().doubleValue(), location.getLongitude().doubleValue());

                //If you have two pins connecting to each other, add polyline
                if (i < locations.size() - 1) {
                    Location location2 = locations.get(i + 1);
                    LatLng latLng2 = new LatLng(location2.getLatitude().doubleValue(), location2.getLongitude().doubleValue());
                    //draw the trip with directions
                    String locationStart = location.getAddress();
                    String locationEnd = location2.getAddress();
                    DirectionsResult results = getDirectionsDetails(locationStart,locationEnd,TravelMode.DRIVING);
                    if (results != null) {
                        addPolyline(results, googleMap);
                        addMarkersToMap(results, googleMap);
                    }
                }
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

    /** Sets up places API and listener to adding a new location to the trip. Uses Google's autocomplete fragment to connect to registered places.*/
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
        Places.initialize(getContext(), BuildConfig.GOOGLE_API_KEY);
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
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
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
     * Creates a new Location, and populates with the selected place's data. Shows the current map with the pin added with directions
     */
    private void addPlaceToTrip(Place place) {
        Location location = new Location();
        LatLng latLng = place.getLatLng();
        location.setLatitude(latLng.latitude);
        location.setLocationName(place.getName());
        location.setAddress(place.getAddress());
        location.setLongitude(latLng.longitude);
        location.setTripId(Trip.getCurrentTrip());
        location.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                    locations.add(location);
                }
            }
        });
        //If there is already markers on the map, add a polyline and a marker, otherwise just add the marker
        if (locations.size() > 0) {
            Location location2 = locations.get(locations.size() - 1);
            LatLng latLng2 = new LatLng(location2.getLatitude().doubleValue(), location2.getLongitude().doubleValue());
            //draw the trip with directions
            String locationEnd = location.getAddress();
            String locationStart = location2.getAddress();
            DirectionsResult results = getDirectionsDetails(locationStart, locationEnd, TravelMode.DRIVING);
            if (results != null) {
                addPolyline(results, tripMap);
                addMarkersToMap(results, tripMap);
            }
        }
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

    /** Connects to the DirectionAPI and requests directions from a destinations address string to another address string. Returns DirectionResult Object*/
    private DirectionsResult getDirectionsDetails(String origin, String destination, TravelMode mode) {
        List<String> waypoints = new ArrayList<>();
        DateTime now = new DateTime();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (com.google.maps.errors.ApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Starts the maps context for the app to connect to directions.*/
    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey(BuildConfig.GOOGLE_API_KEY)
                .setConnectTimeout(2, TimeUnit.SECONDS)
                .setReadTimeout(5, TimeUnit.SECONDS)
                .setWriteTimeout(2, TimeUnit.SECONDS);
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].startLocation.lat,results.routes[overview].legs[overview].startLocation.lng)).title(results.routes[overview].legs[overview].startAddress));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].endLocation.lat,results.routes[overview].legs[overview].endLocation.lng)).title(results.routes[overview].legs[overview].startAddress).snippet(getEndLocationTitle(results)));
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12));
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    private String getEndLocationTitle(DirectionsResult results){
        return  "Time :"+ results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }
}