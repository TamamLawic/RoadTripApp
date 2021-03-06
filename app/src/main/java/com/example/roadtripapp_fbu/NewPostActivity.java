package com.example.roadtripapp_fbu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.roadtripapp_fbu.Objects.Location;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a new ParseObject Post, and allows user input to put into fields.
 * Sets onclick listeners for adding a photo from yoru gallery, taking a photo and posting it.
 */
public class NewPostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String TAG  = "NewPostActivity";
    public String photoFileName = "photo.jpg";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    Spinner spinnerLocation;
    List<Location> locations;
    List<String> locationOptions;
    EditText etCaption;
    ImageView ivPostImage;
    ImageButton btnTakePicture;
    ImageButton btnPostUpdate;
    ImageButton btnSelectPhoto;
    ImageButton btnBackPost;
    File photoFile;
    Trip clicked_trip;
    Location postLocation;

    /** Sets up on click listeners for buttons to take photo and post update **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        etCaption = findViewById(R.id.etCaption);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnTakePicture = findViewById(R.id.btnTakePicture);
        btnPostUpdate = findViewById(R.id.btnPostUpdate);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        btnBackPost = findViewById(R.id.btnBackPost);

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

        //when the select from gallery button is selected, show gallery and choose photo
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        //set back button listener
        btnBackPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //set click listener for spinner
        locations = Location.getTripLocations(clicked_trip);
        locationOptions = new ArrayList<String>();
        locationOptions.add("None");
        //add all locations in your trip for options
        for (int i = 0; i < locations.size() - 1; i++){
            locationOptions.add(locations.get(i).getLocationName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewPostActivity.this,
                android.R.layout.simple_spinner_item, locationOptions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);
        spinnerLocation.setOnItemSelectedListener(this);
    }

    /** Save post in a background thread, sets the parameters to the current fields of the View.*/
    private void savePost(String description, File photoFile) {
        Post post = new Post();
        post.setCaption(description);
        post.setImage(new ParseFile(photoFile));
        post.setUser(ParseUser.getCurrentUser());
        post.setTripId(clicked_trip);
        if (postLocation != null) {
            post.setLocation(postLocation);
        }
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(NewPostActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                //if post saved successfully, remove input fields
                etCaption.setText("");
                ivPostImage.setImageResource(0);
                Intent intent = new Intent();
                intent.putExtra("post", Parcels.wrap(post));//set result code and bundle response
                setResult(RESULT_OK, intent);//return results okay, and intent
                finish();
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

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    /** Runs when a picture is taken and saved. Adds the Image to the ImageView resource for composing the post.*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if the request is returned from the capture image
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
        //if the return is from selecting a photo
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            ImageView ivPreview = (ImageView) findViewById(R.id.ivPostImage);
            ivPreview.setImageBitmap(selectedImage);

            //set the save file
            //create a file to write bitmap data
            File f = new File(this.getCacheDir(), "photo");
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Convert bitmap to byte array
            Bitmap bitmap = selectedImage;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fos.write(bitmapdata);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            photoFile = f;
        }
    }

    /** Returns image selected from gallery of photos*/
    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /** Starts activity to get the image from gallery selection*/
    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_PHOTO_CODE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //add the location at that position
        if (position > 0) {
            postLocation = locations.get(position - 1);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}