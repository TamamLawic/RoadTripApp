package com.example.roadtripapp_fbu;

import android.app.Application;

import com.example.roadtripapp_fbu.Objects.BucketListLocation;
import com.example.roadtripapp_fbu.Objects.JournalEntry;
import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.example.roadtripapp_fbu.Objects.Collaborator;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Parse initialization, registers Parse models based on client and server keys.
 */
public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //register Parse Models
        ParseObject.registerSubclass(Trip.class);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Location.class);
        ParseObject.registerSubclass(JournalEntry.class);
        ParseObject.registerSubclass(BucketListLocation.class);
        ParseObject.registerSubclass(Collaborator.class);

        //set applicationId, and server server based on the values
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        );
    }
}
