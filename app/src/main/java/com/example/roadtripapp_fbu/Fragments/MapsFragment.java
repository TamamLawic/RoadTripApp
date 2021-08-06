package com.example.roadtripapp_fbu.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roadtripapp_fbu.Adapters.CustomInfoWindowAdapter;
import com.example.roadtripapp_fbu.Adapters.ItineraryAdapter;
import com.example.roadtripapp_fbu.Adapters.SuggestionsAdapter;
import com.example.roadtripapp_fbu.BuildConfig;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.NewPostActivity;
import com.example.roadtripapp_fbu.R;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.google.maps.DirectionsApi;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for bottom navigational view. Makes Google Map object, and populates with the user's current Trip using ParseQuery.
 */
public class MapsFragment extends Fragment implements SuggestionsAdapter.EventListener, CustomInfoWindowAdapter.EventListener {
    public static final String TAG = "MapFragment";
    Trip currentTrip = Collaborator.getCurrentTrip();
    private SlidingUpPanelLayout slidingPane;
    protected ItineraryAdapter adapter;
    protected SuggestionsAdapter suggestionsAdapter;
    private static final int overview = 0;
    private PlacesClient placesClient;
    RecyclerView rvItinerary;
    RecyclerView rvSuggestedStops;
    List<Location> locations;
    List<JSONObject> suggestedLocations;
    List<JSONObject> addedLocations;
    List<Bitmap> placesImages;
    GoogleMap tripMap;
    ImageButton btnResfreshMap;
    TextView tvStops;
    TextView tvDuration;
    TextView tvMiles;
    long miles = 0L;
    long duration = 0L;
    int targetLocationLength = 0;
    double latN = 25; //highest Latitude
    double lngN = -130; //highest Longitude
    double latS = 50; //Lowest Latitude
    double lngS = -70; //lowest Longitude
    ProgressBar progressBar;

    /** Loads Current trip data to display the current trip on a map with markers, and direction poly lines connecting*/
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //when the map is ready, add the markers for the current trip
            tripMap = googleMap;
            tripMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getContext(), MapsFragment.this));
            locations.addAll(Location.getTripLocations(currentTrip));
            adapter.notifyDataSetChanged();
            //register data observer for list of locations, triggered when the location has finished loading.
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    if (suggestedLocations != null){
                        //Only reload the map when all of the locations have been added in Parse
                        if (locations.size() ==  targetLocationLength) {
                            FragmentManager manager = getParentFragmentManager();
                            manager.beginTransaction().replace(R.id.flContainer, new MapsFragment()).commit();
                        }
                    }
                }
            });
            if (locations.size() > 0) {
                addAllLocationsMap();
            }
            tvStops.setText(String.valueOf(locations.size()));
            tvDuration.setText(String.valueOf(duration).concat(" Hours"));
            tvMiles.setText(String.valueOf(miles).concat(" Miles"));

            //Onclick listener for finding things to do near a stopping point
            tripMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    // Construct a CameraPosition focusing on Marker position.
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(marker.getPosition() )      // Sets the center of the map to the Marker
                            .zoom(12)
                            .build();
                    tripMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    //find suggestions for places in this area
                    showMapStops(marker);
                }
            });

            //Onclick listener for clicking on Points of interest on the map
            tripMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
                @Override
                public void onPoiClick(PointOfInterest pointOfInterest) {
                    Toast.makeText(getContext(), "Clicked: " +
                                    pointOfInterest.name + "\nPlace ID:" + pointOfInterest.placeId +
                                    "\nLatitude:" + pointOfInterest.latLng.latitude +
                                    " Longitude:" + pointOfInterest.latLng.longitude, Toast.LENGTH_SHORT).show();
                }
            });

            //set on click listener for user done adding locations
            btnResfreshMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //clear the adapter for the recycler view when moving out of the city view
                    suggestedLocations.clear();
                    suggestionsAdapter.notifyDataSetChanged();
                    if (addedLocations.size() > 0){
                        //add locations to the current trip
                        targetLocationLength = locations.size() + addedLocations.size();
                        addLocationsToTrip();
                    }
                    else {
                        LatLngBounds bounds = new LatLngBounds(
                                new LatLng(latS, lngS),
                                new LatLng(latN, lngN)
                        );
                        tripMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                    }
                }
            });
            //get rid of progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    /** Sets up places API and listener to adding a new location to the trip. Uses Google's autocomplete fragment to connect to registered places.*/
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMiles = view.findViewById(R.id.tvMilesProfile);
        tvDuration = view.findViewById(R.id.tvDurationProfile);
        tvStops = view.findViewById(R.id.tvStopsProfile);
        rvItinerary = view.findViewById(R.id.rvItinerary);
        rvSuggestedStops = view.findViewById(R.id.rvSuggestedStops);
        slidingPane = view.findViewById(R.id.slidingPaneItinerary);
        btnResfreshMap = view.findViewById(R.id.btnResfreshMap);
        progressBar = view.findViewById(R.id.progressBar);

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
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete);
        //set the type of places you want to autocomplete
        autocompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);
        //set a location bias for completions
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(new LatLng(-33.880490, 151.184364), new LatLng(-33.858754, 151.229596)));
        autocompleteFragment.setCountries("US");
        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.VIEWPORT));
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

        //Set up Slide Listener for pull up view
        slidingPane.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {}

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                //if the panel is closed, push changes to Parse for order
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    reorderTrip();
                    //reloads the map to show changes in directions
                    FragmentManager manager = getParentFragmentManager();
                    manager.beginTransaction().replace(R.id.flContainer, new MapsFragment()).commit();
                }
            }
        });

        //set up the recycler view for the itinerary
        //Set up the adapter for the trip recycler view
        locations = new ArrayList<>();
        //create the adapter
        adapter = new ItineraryAdapter(getContext(), locations);
        //set the adapter on the recycler view
        rvItinerary.setAdapter(adapter);
        rvItinerary.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvItinerary.setLayoutManager(layoutManager);
        // set up reorder itemTouch Helper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvItinerary);

        //set up recycler view for suggested stops
        suggestedLocations = new ArrayList<JSONObject>();
        //create the new adapter
        suggestionsAdapter = new SuggestionsAdapter(getContext(), suggestedLocations, this);
        //set the adapter to the recycler view
        rvSuggestedStops.setAdapter(suggestionsAdapter);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvSuggestedStops.setLayoutManager(layoutManager1);
        addedLocations = new ArrayList<>();
        placesImages = new ArrayList<>();
    }

    /**
     * When the options menu is created, change it to the one for the maps fragment
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //change the title to show the trip you are editing/viewing
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(currentTrip.getTripName());
        inflater.inflate(R.menu.menu_maps_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * When the MenuItem post is selected startActivity : NewPostActivity
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(getContext(), NewPostActivity.class);
        // serialize the post using parceler, use its short name as a key
        i.putExtra(Trip.class.getSimpleName(), Parcels.wrap(currentTrip));
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEvent(List<JSONObject> added_stops, List<Bitmap> images) {
        addedLocations = added_stops;
        placesImages = images;
    }

    /** Sets up touch helper for reordering the trip*/
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPostion = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(locations, fromPostion, toPosition);
            adapter.notifyItemMoved(fromPostion, toPosition);
            return false;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    } ;

    /**
     * Creates a new Location, and populates with the selected place's data. Shows the current map with the pin added with directions.
     * Uses places query to find photo metadata, and convert into a photo
     */
    private void addPlaceToTrip(Place place) {
        Location location = new Location();
        LatLng latLng = place.getLatLng();
        //check if this changes the bounds of the map
        double latLocation = latLng.latitude;
        double lngLocation = latLng.longitude;
        // check bounds to find the camera position
        if (latLocation > latN) {
            latN = latLocation;
        }
        if (latLocation < latS) {
            latS = latLocation;
        }
        if (lngLocation > lngN) {
            lngN = lngLocation;
        }
        if (lngLocation < lngS) {
            lngS = lngLocation;
        }
        location.setLatitude(latLng.latitude);
        location.setLocationName(place.getName());
        location.setAddress(place.getAddress());
        try{
            location.setHours(place.getOpeningHours().getWeekdayText().toString());
            location.setPhone(place.getPhoneNumber());
            location.setWebsite(place.getWebsiteUri().toString());
        } catch (Exception e) {
            location.setHours("");
            location.setPhone("");
            location.setWebsite("");        }
        location.setLongitude(latLng.longitude);
        //get the image data from google places
        final PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

        // Create a FetchPhotoRequest.
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500)
                .setMaxHeight(300)
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            //create a file to write bitmap data
            //set the save file
            //create a file to write bitmap data
            File f = new File(getContext().getCacheDir(), "photo");
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            location.setImage(new ParseFile(f));
            location.setTripId(currentTrip);
            location.setPosition(locations.size());
            try {
                location.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            locations.add(location);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
            }
        });

        //If there is already markers on the map, add a polyline and a marker, otherwise just add the marker
        if (locations.size() > 0) {
            Location location2 = locations.get(locations.size() - 1);
            //draw the trip with directions
            String locationEnd = location.getAddress();
            String locationStart = location2.getAddress();
            DirectionsResult results = getDirectionsDetails(locationStart, locationEnd, TravelMode.DRIVING);
            if (results != null) {
                addPolyline(results, tripMap);
                miles += results.routes[overview].legs[overview].distance.inMeters * 0.000621371;
                duration += results.routes[overview].legs[overview].duration.inSeconds * 0.000277778;
                //after adding another stop, add to the values
                tvStops.setText(String.valueOf(locations.size()));
                tvDuration.setText(String.valueOf(duration).concat(" Hours"));
                tvMiles.setText(String.valueOf(miles).concat(" Miles"));
                //set the TripDistance and time
                currentTrip.setLength((int) miles);
                currentTrip.setTime((int) duration);
                currentTrip.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            LatLngBounds bounds = new LatLngBounds(
                    new LatLng(latS, lngS),
                    new LatLng(latN, lngN)
            );
            tripMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        }
        else {

            tripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.850033, -87.6500523), 15));
        }
        tripMap.addMarker(new MarkerOptions().position(latLng).title(location.getLocationName()));

    }

    /** Connects to the DirectionAPI and requests directions from a destinations address string to another address string. Returns DirectionResult Object*/
    private DirectionsResult getDirectionsDetails(String origin, String destination, TravelMode mode) {
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
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .setReadTimeout(8, TimeUnit.SECONDS)
                .setWriteTimeout(5, TimeUnit.SECONDS);
    }

    /** Pushes user's reordered list of places to Parse to store*/
    private void reorderTrip() {
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            location.setPosition(i);
            location.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /** Adds all locations to the current map*/
    private void addAllLocationsMap() {
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            double latLocation = location.getLatitude().doubleValue();
            double lngLocation = location.getLongitude().doubleValue();
            // check bounds to find the camera position
            if (latLocation > latN) {
                latN = latLocation;
            }
            if (latLocation < latS) {
                latS = latLocation;
            }
            if (lngLocation > lngN) {
                lngN = lngLocation;
            }
            if (lngLocation < lngS) {
                lngS = lngLocation;
            }

            LatLng latLng1 = new LatLng(latLocation, lngLocation);
            tripMap.addMarker(new MarkerOptions().position(latLng1).title(location.getLocationName()));

            //If you have two pins connecting to each other, add polyline
            if (i < locations.size() - 1) {
                Location location2 = locations.get(i + 1);
                //draw the trip with directions
                String locationStart = location.getAddress();
                String locationEnd = location2.getAddress();
                DirectionsResult results = getDirectionsDetails(locationStart,locationEnd,TravelMode.DRIVING);
                if (results != null) {
                    addPolyline(results, tripMap);
                    //add to total time of the trip, total miles for the trip, and stops
                    miles += results.routes[overview].legs[overview].distance.inMeters * 0.000621371;
                    duration += results.routes[overview].legs[overview].duration.inSeconds * 0.000277778;
                }
            }
            //set the TripDistance and time
            currentTrip.setLength((int) miles);
            currentTrip.setTime((int) duration);
            currentTrip.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //create bounds for start or map, center around the US
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(latS, lngS),
                new LatLng(latN, lngN)
        );
        tripMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    /** Gets all of the suggested places to visit in the proximity of the waypoint selected using HTTP request to nearby search in PlaceAPI*/
    private void showMapStops(Marker marker) {
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        suggestedLocations.clear();
        suggestedLocations.addAll(loadNearByPlaces(latitude, longitude));
        suggestionsAdapter.notifyDataSetChanged();
    }

    /** Calls Place Search API and gets data through Http request to get Places in the proximity to the location*/
    private List<JSONObject> loadNearByPlaces(double latitude, double longitude) {
        HttpURLConnection httpURLConnection = null;
        StringBuilder jsonResults = new StringBuilder();
        List<JSONObject> placeSuggestions = null;

        //Make Places URL to get all locations in a 3 miles radius from the selected marker
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        //change the radius for the search
        googlePlacesUrl.append("&radius=").append(5000);
        googlePlacesUrl.append("&types=").append("tourist_attraction");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + BuildConfig.GOOGLE_API_KEY);

        try {
            //Make string into URL and connect.
            URL placeApiURL = new URL(googlePlacesUrl.toString());
            httpURLConnection = (HttpURLConnection) placeApiURL.openConnection();
            InputStreamReader reader = new InputStreamReader(httpURLConnection.getInputStream());
            int read;
            char[] buffer = new char[1050];
            while((read = reader.read(buffer)) != -1){
                jsonResults.append(buffer, 0, read);
            }
            JSONObject jsonObject = new JSONObject(jsonResults.toString());
            JSONArray resultsJsonArray = jsonObject.getJSONArray("results");
            placeSuggestions = new ArrayList<>();
            for (int i = 0; i < resultsJsonArray.length(); i ++){
                JSONObject suggestion = resultsJsonArray.getJSONObject(i);
                placeSuggestions.add(suggestion);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        //Return all of the places in the area around the marker
        return placeSuggestions;
    }

    /** Adds all the clicked suggested locations to the trip. Uses a GooglePlaceRequest to get place object from PlaceID
     * uses Parse to add to the database*/
    public void addLocationsToTrip() {
        for (int i = 0; i < addedLocations.size(); i ++){
            //for each location get the location's details
            List<Place.Field> placeFields = new ArrayList<>();
            placeFields.add(Place.Field.ID);
            placeFields.add(Place.Field.NAME);
            placeFields.add(Place.Field.LAT_LNG);
            placeFields.add(Place.Field.ADDRESS);
            placeFields.add(Place.Field.PHOTO_METADATAS);
            placeFields.add(Place.Field.OPENING_HOURS);
            placeFields.add(Place.Field.PHONE_NUMBER);
            placeFields.add(Place.Field.WEBSITE_URI);

            try {
                String placeID = addedLocations.get(i).getString("place_id");

                // Create a FetchPhotoRequest.
                final FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeID, placeFields).build();
                //request the Place object from MapsSDK
                placesClient.fetchPlace(placeRequest).addOnSuccessListener((fetchPlaceResponse) -> {
                    Place place = fetchPlaceResponse.getPlace();
                    addPlaceToTrip(place);

                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    //Event Listener for CustomInfoReady
    @Override
    public void onEvent(Marker marker) {
        marker.showInfoWindow();
    }
}