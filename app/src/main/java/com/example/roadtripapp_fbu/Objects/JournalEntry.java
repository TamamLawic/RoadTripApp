package com.example.roadtripapp_fbu.Objects;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Parse class, setting up JournalEntry
 */
@ParseClassName("JournalEntry")
public class JournalEntry extends ParseObject implements FeedObjects {
    public static final String KEY_TEXT = "text";
    public static final String KEY_TRIP = "tripId";
    public static final String KEY_USER = "author";
    public static final String KEY_TITLE = "title";

    //Getters and setters for the Parse Post Object
    //returns journal entry's description
    public String getText() {
        return getString(KEY_TEXT);
    }

    //sets the description for the post
    public void setText(String text) {
        put(KEY_TEXT, text);
    }

    //gets the entry's trip
    public Trip getTripId() {
        return (Trip) getParseObject(KEY_TRIP);
    }

    //sets the trip for the entry
    public void setTripId(Trip trip) {
        put(KEY_TRIP, trip);
    }

    //gets the journal entry's author
    public ParseUser getUser() {return getParseUser(KEY_USER);}

    //sets the journal entry's author
    public void setUser(ParseUser user) {put(KEY_USER, user);}

    //gets the journal entry's title
    public String getTitle() {return getString(KEY_TITLE);}

    //sets the journal entry's title
    public void setTitle(String title) {put(KEY_TITLE, title);}

    @Override
    public int getType() {
        return FeedObjects.TYPE_JOURNAL;
    }
}
