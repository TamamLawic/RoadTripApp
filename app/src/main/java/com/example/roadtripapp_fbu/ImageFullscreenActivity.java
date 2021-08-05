package com.example.roadtripapp_fbu;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.roadtripapp_fbu.Objects.Post;
import com.example.roadtripapp_fbu.Objects.Trip;

import org.parceler.Parcels;

import java.io.IOException;
import java.net.URL;

public class ImageFullscreenActivity extends AppCompatActivity {
    ImageView ivFullScreenImage;
    Post postSelected;
    ImageButton btnBackImageLarge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_fullscreen);
        ivFullScreenImage = findViewById(R.id.ivFullScreenImage);
        btnBackImageLarge = findViewById(R.id.btnBackImageLarge);

        //set image full screen into view
        postSelected = (Post) Parcels.unwrap(getIntent().getParcelableExtra("post"));
        Glide.with(this).load(postSelected.getImage().getUrl()).into(ivFullScreenImage);

        //set up back button
        btnBackImageLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}