package com.example.roadtripapp_fbu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.roadtripapp_fbu.R;

public class TripFeedActivity extends AppCompatActivity {
    private Button btnNewPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_feed);

        btnNewPost = findViewById(R.id.btnNewPost);
        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TripFeedActivity.this, NewPostActivity.class);
                //TODO: Add the TripID into the post activity
                startActivity(i);
            }
        });
    }
}