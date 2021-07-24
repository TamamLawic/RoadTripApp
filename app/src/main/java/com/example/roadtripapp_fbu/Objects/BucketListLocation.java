package com.example.roadtripapp_fbu.Objects;

import com.example.roadtripapp_fbu.Objects.Location;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Parse class, setting up BucketListLocation object.
 */
@ParseClassName("BucketListLocation")
public class BucketListLocation extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_LOCATION = "location";

    //Getters and setters for the Parse BucketListLocations Object
    //returns BucketListLocation's user
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    //sets the user for the BucketListLocation
    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    //gets the user for the BucketListLocation
    public Location getLocation() { return (Location) getParseObject(KEY_LOCATION); }

    //sets the user for the BucketListLocation
    public void setLocation(Location location) {
        put(KEY_LOCATION, location);
    }


}
