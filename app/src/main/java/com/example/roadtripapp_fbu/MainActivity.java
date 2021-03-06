package com.example.roadtripapp_fbu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.roadtripapp_fbu.Fragments.HomeFeedFragment;
import com.example.roadtripapp_fbu.Fragments.MapsFragment;
import com.example.roadtripapp_fbu.Fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Sets up bottom navigational view, and sets fragments for the bottom navigation.
 */
public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    final FragmentManager fragmentManager = getSupportFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment;
                        switch (item.getItemId()) {
                            case R.id.action_feed:
                                fragment = new HomeFeedFragment();
                                break;
                            case R.id.action_create_trip:
                                fragment = new MapsFragment();
                                break;
                            case R.id.action_profile:
                            default:
                                fragment = new ProfileFragment();
                                break;
                        }
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        return true;
                    }
                });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_feed);
    }
}