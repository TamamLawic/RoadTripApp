package com.example.roadtripapp_fbu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;
import java.util.List;
/**
 * Creates a new ParseObject Post, and allows user input to put into fields.
 * Sets onclick listeners for
 */
//TODO: Need to add in the user's current trip, and pass that when creating a new post
public class NewPostActivity extends AppCompatActivity {
    public String photoFileName = "photo.jpg";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public static final String TAG  = "NewPostActivity";
    EditText etCaption;
    ImageView ivPostImage;
    EditText etMoneySpent;
    ImageButton btnTakePicture;
    ImageButton btnPostUpdate;
    File photoFile;
    Trip clicked_trip;

    /** Sets up on click listeners for buttons to take photo and post update **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        etCaption = findViewById(R.id.etCaption);
        ivPostImage = findViewById(R.id.ivPostImage);
        etMoneySpent = findViewById(R.id.etMoneySpent);
        btnTakePicture = findViewById(R.id.btnTakePicture);
        btnPostUpdate = findViewById(R.id.btnPostUpdate);

        //use Parcels to unwrap trip selected
        //unwrap post's data from the pass
        clicked_trip = (Trip) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Trip.class.getSimpleName()));

        //when post picture button is clicked, post to current trip
        btnPostUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etCaption.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(NewPostActivity.this, "Caption can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Toast.makeText(NewPostActivity.this, "There is no image!", Toast.LENGTH_SHORT).show();
                }
                savePost(description, photoFile);
            }
        });

        //when take picture is clicked start camera to take a photo
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  { onLaunchCamera(); }
        });
    }

    /** Save post in a background thread, sets the parameters to the current fields of the View.*/
    private void savePost(String description, File photoFile) {
        Post post = new Post();
        post.setCaption(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(ParseUser.getCurrentUser());
        post.setTripId(clicked_trip);
        //TODO: need to be able to set the tripID
        //TODO: add in the users current location to make the post
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e("NewPostActivity", "error while saving", e);
                    Toast.makeText(NewPostActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i("NewPostActivity", "Post was saved successfully!");
                //if post saved successfully, remove input fields
                etCaption.setText("");
                ivPostImage.setImageResource(0);
                etMoneySpent.setText("");
                Intent intent = new Intent();
                intent.putExtra("post", Parcels.wrap(post));//set result code and bundle response
                setResult(RESULT_OK, intent);//return results okay, and intent
                finish();//end and return user back to timeline
            }
        });
    }

    /** External Launch of camera application on phone and takes an image.
     * Image file wrapped into a content provider to access later. */
    public void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileproviderx", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    /** Returns the File for a photo stored on disk given the fileName */
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    /** Runs when a picture is taken and saved. Adds the Image to the ImageView resource for composing the post.*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}