package com.example.roadtripapp_fbu.Objects;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Parse class, setting up Collaborator object.
 */
@ParseClassName("Collaborator")
public class Collaborator extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_TRIP = "trip";

    // Ensure that your subclass has a public default constructor
    public Collaborator() {
        super();
    }

    //Getters and setter for User Object
    public ParseUser getUser() { return getParseUser(KEY_USER);}

    public void setUser(ParseUser user) { put(KEY_USER, user);}

    public Trip getTrip() {return (Trip) getParseObject(KEY_TRIP);}

    public void setTrip(Trip trip) {put(KEY_TRIP, trip);}

    //get the newest trip the user is collaborating on
    /** Get the newest trip the current user has made*/
    public static Trip getCurrentTrip() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Collaborator> query = ParseQuery.getQuery(Collaborator.class);
        // include data referred by user key
        query.include(Collaborator.KEY_USER);
        // include data referred by user key
        query.include(Collaborator.KEY_TRIP);
        //only query posts of the currently signed in user
        query.whereEqualTo(Collaborator.KEY_USER, ParseUser.getCurrentUser());
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        //finds the newest created trip
        try {
            return query.find().get(0).getTrip();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
