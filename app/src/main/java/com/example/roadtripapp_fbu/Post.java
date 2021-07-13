package com.example.roadtripapp_fbu;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Parse class, setting up Post object.
 */
@ParseClassName("Post")
public class Post extends ParseObject {
    public static final String KEY_DESCRIPTION = "caption";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_TRIP = "tripId";

    /** Returns the time elapsed given the creation date until now */
    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }

    //Getters and setters for the Parse Post Object
    //returns post's description
    public String getCaption() {
        return getString(KEY_DESCRIPTION);
    }

    //sets the description for the post
    public void setCaption(String description) {
        put(KEY_DESCRIPTION, description);
    }

    //returns the image for the post
    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    //sets the image for the post
    public void setImage(ParseFile parseFile) {
        put(KEY_IMAGE, parseFile);
    }

    //returns the user that created the post
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    //set the user that created the post
    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    //get the current tripID for the post
    public ParseObject getTripId() { return getParseObject(KEY_TRIP);}

    //set the post's tripId
    public void setTripId(ParseUser user) {
        put(KEY_TRIP, user);
    }
}
