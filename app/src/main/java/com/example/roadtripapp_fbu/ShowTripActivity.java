package com.example.roadtripapp_fbu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.roadtripapp_fbu.Adapters.ItineraryAdapter;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Displays the Trip and itinerary for the trip selected from the Home Feed. Uses Parse to query the Trip selected and passed in using a Parcel.
 */
public class ShowTripActivity extends AppCompatActivity {
    private static final int overview = 0;
    private SupportMapFragment mapFragment;
    Trip clicked_trip;
    TextView tvStopsDetails;
    List<Location> waypoints;
    ItineraryAdapter adapter;
    RecyclerView rvTripDetails;
    TextView tvDurationDetails;
    TextView tvMilesDetails;
    GoogleMap tripMap;
    ImageButton btnBackOtherTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_trip);

        tvStopsDetails = findViewById(R.id.tvStopsProfile);
        rvTripDetails = findViewById(R.id.rvTripDetails);
        tvDurationDetails = findViewById(R.id.tvDurationProfile);
        tvMilesDetails = findViewById(R.id.tvMilesProfile);
        tvStopsDetails = findViewById(R.id.tvStopsProfile);
        btnBackOtherTrip = findViewById(R.id.btnBackOtherTrip);

        //unwrap trip from Parcel and put data into trip
        //use Parcels to unwrap trip selected
        //unwrap post's data from the pass
        clicked_trip = (Trip) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra("trip"));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(clicked_trip.getTripName());
        tvDurationDetails.setText(String.valueOf(clicked_trip.getTime()).concat(" Hours"));
        tvMilesDetails.setText(String.valueOf(clicked_trip.getLength()).concat(" Miles"));

        //set up the recycler view for the itinerary
        //Set up the adapter for the trip recycler view
        waypoints = new ArrayList<>();
        //create the adapter
        adapter = new ItineraryAdapter(this, waypoints);
        //set the adapter on the recycler view
        rvTripDetails.setAdapter(adapter);
        rvTripDetails.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // set the layout manager on the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTripDetails.setLayoutManager(layoutManager);
        //get all locations for the trip and add to the recycler view
        waypoints.addAll(Location.getTripLocations(clicked_trip));
        tvStopsDetails.setText(String.valueOf(waypoints.size()));
        adapter.notifyDataSetChanged();

        //set up the map view of the trip
        //set up map view
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapViewDetails);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        }

        //if the back button is pressed go back to home feed
        btnBackOtherTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /** Loads new google map. Populates the map with the user's current trip, determined by ParseQuery.
     * Moves camera to see the current trip, and notifies the adapter the data has been loaded.
     **/
    protected void loadMap(GoogleMap googleMap) {
        //create bounds for start or map, center around the US
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(25.82, -124.39),
                new LatLng(49.38, -66.94)
        );
        //when the map is ready, add the markers for the current trip
        tripMap = googleMap;
        adapter.notifyDataSetChanged();
        for (int i = 0; i < waypoints.size(); i++) {
            Location location = waypoints.get(i);
            LatLng latLng1 = new LatLng(location.getLatitude().doubleValue(), location.getLongitude().doubleValue());

            //If you have two pins connecting to each other, add polyline
            if (i < waypoints.size() - 1) {
                Location location2 = waypoints.get(i + 1);
                LatLng latLng2 = new LatLng(location2.getLatitude().doubleValue(), location2.getLongitude().doubleValue());
                //draw the trip with directions
                String locationStart = location.getAddress();
                String locationEnd = location2.getAddress();
                DirectionsResult results = getDirectionsDetails(locationStart,locationEnd, TravelMode.DRIVING);
                if (results != null) {
                    addPolyline(results, googleMap);
                    addMarkersToMap(results, googleMap);
                }
                else {
                    tripMap.addMarker(new MarkerOptions().position(latLng1));
                    tripMap.addMarker(new MarkerOptions().position(latLng2));
                }
            }
            tripMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 3));
        }
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

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    private String getEndLocationTitle(DirectionsResult results){
        return  "Time :"+ results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }
}