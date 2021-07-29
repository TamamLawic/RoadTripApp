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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
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
public class MapsFragment extends Fragment {
    public static final String TAG = "MapFragment";
    Trip currentTrip = Collaborator.getCurrentTrip();
    private SlidingUpPanelLayout slidingPane;
    protected ItineraryAdapter adapter;
    protected SuggestionsAdapter suggestionsAdapter;
    private static final int overview = 0;
    private PlacesClient placesClient;
    GoogleApiClient googleApiClient;
    RecyclerView rvItinerary;
    RecyclerView rvSuggestedStops;
    List<Location> locations;
    List<JSONObject> suggestedLocations;
    GoogleMap tripMap;
    TextView tvStops;
    TextView tvDuration;
    TextView tvMiles;
    long miles = 0L;
    long duration = 0L;

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
            // info window.
            tripMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getContext()));
            locations.addAll(Location.getTripLocations(currentTrip));
            adapter.notifyDataSetChanged();
            addAllLocationsMap();
            tvStops.setText(String.valueOf(locations.size()));
            tvDuration.setText(String.valueOf(duration).concat(" Hours"));
            tvMiles.setText(String.valueOf(miles).concat(" Miles"));

            //Onclick listener for finding things to do near a stopping point
            tripMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(marker.getPosition() )      // Sets the center of the map to Mountain View
                            .zoom(12)                       // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
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
        }
    };

    /** Builds the google API Client to connect to the PlaceSearch API*/
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) getContext())
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) getContext())
                .build();
        googleApiClient.connect();
    }

    /** Gets all of the suggested places to visit in the proximity of the waypoint selected*/
    private void showMapStops(Marker marker) {
        //get the trip for the marker
        //set up the adapter and recycler view for the trip
        //show the recycler view for the trip, with stops
        //user can search for stops near them
        LatLngBounds bounds = tripMap.getProjection().getVisibleRegion().latLngBounds;
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

        //get the data
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        //change the radius for the search
        googlePlacesUrl.append("&radius=").append(1500);
        googlePlacesUrl.append("&types=").append("tourist_attraction");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + BuildConfig.GOOGLE_API_KEY);

        try {
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
                Log.i(TAG, suggestion.toString());
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
        return placeSuggestions;
    }

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
        tvMiles = view.findViewById(R.id.tvMilesDetails);
        tvDuration = view.findViewById(R.id.tvDurationDetails);
        tvStops = view.findViewById(R.id.tvStopsDetails);
        rvItinerary = view.findViewById(R.id.rvItinerary);
        rvSuggestedStops = view.findViewById(R.id.rvSuggestedStops);
        slidingPane = view.findViewById(R.id.slidingPaneItinerary);

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
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI));
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
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                //if the panel is closed, push changes to Parse for order
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    reorderTrip();
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
        suggestionsAdapter = new SuggestionsAdapter(getContext(), suggestedLocations);
        //set the adapter to the recycler view
        rvSuggestedStops.setAdapter(suggestionsAdapter);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvSuggestedStops.setLayoutManager(layoutManager1);

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

        // Get the attribution text.
        final String attributions = photoMetadata.getAttributions();

        // Create a FetchPhotoRequest.
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500) // Optional.
                .setMaxHeight(300) // Optional.
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
            location.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        locations.add(location);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                final int statusCode = apiException.getStatusCode();
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
        }
        tripMap.addMarker(new MarkerOptions().position(latLng).title(location.getLocationName()));
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
        actionBar.setTitle(currentTrip.getTripName());
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
        //create bounds for start or map, center around the US
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(25.82, -124.39),
                new LatLng(49.38, -66.94)
        );

        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            LatLng latLng1 = new LatLng(location.getLatitude().doubleValue(), location.getLongitude().doubleValue());
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
            tripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 3));
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
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }
}