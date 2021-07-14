package com.example.roadtripapp_fbu;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Parse class, setting up Trip object.
 */
@ParseClassName("Trip")
public class Trip extends ParseObject {
    public static final String KEY_USER = "author";
    public static final String KEY_TRIP_LENGTH = "tripLength";
    public static final String KEY_COST = "cost";
    public static final String KEY_TRIP_NAME = "tripName";

    // Ensure that your subclass has a public default constructor
    public Trip() {
        super();
    }

    //returns Trips's cost
    public Number getCost() {
        return getNumber(KEY_COST);
    }

    //updates the cost for the Trip
    public void updateCost(Number cost) {
        put(KEY_COST, cost);
    }

    //returns the time for the Trip
    public Number getTime() {
        return getNumber(KEY_TRIP_LENGTH);
    }

    //updates the time for the Trip
    public void updateTime(Number time) {
        put(KEY_TRIP_LENGTH, time);
    }

    //returns the user that created the Trip
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    //set the user that created the Trip
    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    //returns the TripName
    public String getTripName() {
        return getString(KEY_TRIP_NAME);
    }

    //set the tripName
    public void setTripName(String tripName) {
        put(KEY_TRIP_NAME, tripName);
    }

    //get the newest trip the user has made
    /** Get the newest trip the current user has made*/
    public static Trip getCurrentTrip() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        // include data referred by user key
        query.include(Trip.KEY_USER);
        //only query posts of the currently signed in user
        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        //finds the newest created trip
        try {
            return query.find().get(0);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}