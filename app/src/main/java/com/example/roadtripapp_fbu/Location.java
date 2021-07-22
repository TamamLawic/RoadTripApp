package com.example.roadtripapp_fbu;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

/**
 * Parse class, setting up Location object.
 */
@ParseClassName("Location")
public class Location extends ParseObject {
    public static final String KEY_LAT = "latitude";
    public static final String KEY_LNG = "longitude";
    public static final String KEY_NAME = "locationName";
    public static final String KEY_TRIP = "tripId";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_IMAGE = "placeImage";
    public static final String KEY_POSITION = "order";
    public static final String KEY_HOURS = "openingHours";
    public static final String KEY_PHONE = "phoneNumber";
    public static final String KEY_WEBSITE = "website";



    //Getters and setters for the Parse Location Object
    //returns location's description
    public Number getLatitude() {
        return getNumber(KEY_LAT);
    }

    //sets the latitude for the location
    public void setLatitude(Number latitude) { put(KEY_LAT, latitude); }

    //returns the latitude for the location
    public Number getLongitude() {
        return getNumber(KEY_LNG);
    }

    //sets the longitude for the location
    public void setLongitude(Number number) {
        put(KEY_LNG, number);
    }

    //returns the longitude for the location
    public String getLocationName() { return getString(KEY_NAME); }

    //set the location name
    public void setLocationName(String name) {
        put(KEY_NAME, name);
    }

    //get the current tripID for the location
    public ParseObject getTripId() { return getParseObject(KEY_TRIP);}

    //set the location's tripId
    public void setTripId(Trip tripId) {
        put(KEY_TRIP, tripId);
    }

    //get the current tripID for the location
    public String getAddress() { return getString(KEY_ADDRESS);}

    //set the location's tripId
    public void setAddress(String address) { put(KEY_ADDRESS, address);}

    //get the current Imaage for the location
    public ParseFile getImage() { return getParseFile(KEY_IMAGE);}

    //set the location's tripId
    public void setImage(ParseFile image) { put(KEY_IMAGE, image);}

    //get the current Imaage for the location
    public int getPosition() { return (int) getNumber(KEY_POSITION);}

    //set the location's tripId
    public void setPosition(int position) { put(KEY_POSITION, position);}

    public String getHours() { return getString(KEY_HOURS);}

    //set the location's tripId
    public void setHours(String hours) { put(KEY_HOURS, hours);}

    public String getPhone() { return getString(KEY_PHONE);}

    //set the location's tripId
    public void setPhone(String phone) { put(KEY_PHONE, phone);}

    public String getWebsite() { return getString(KEY_WEBSITE);}

    //set the location's tripId
    public void setWebsite(String website) { put(KEY_WEBSITE, website);}

    //returns all locations for a trip passed in*/
    public static List<Location> getTripLocations(Trip trip) {
        // specify what type of data we want to query - Location.class
        ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
        //only query posts in the tripID passed in
        query.whereEqualTo(KEY_TRIP, trip);
        //finds the newest created trip
        query.addAscendingOrder(KEY_POSITION);
        try {
            return query.find();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
