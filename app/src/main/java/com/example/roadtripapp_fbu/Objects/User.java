package com.example.roadtripapp_fbu.Objects;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Parse class, setting up User object.
 */
@ParseClassName("User")
public class User extends ParseObject {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PROFILE_IMAGE = "username";

    // Ensure that your subclass has a public default constructor
    public User() {
        super();
    }

    //Getters and setter for User Object
    public String getUsername() { return getString(KEY_USERNAME);}

    public void setUsername(String username) { put(KEY_USERNAME, username);}

    public ParseFile getProfilePicture() {return getParseFile(KEY_PROFILE_IMAGE);}

    public void setProfileImage(ParseFile image) {put(KEY_PROFILE_IMAGE, image);}

}
