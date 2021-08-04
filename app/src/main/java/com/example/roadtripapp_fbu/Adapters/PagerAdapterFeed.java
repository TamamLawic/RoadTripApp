package com.example.roadtripapp_fbu.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.roadtripapp_fbu.Fragments.BucketlistViewFragment;
import com.example.roadtripapp_fbu.Fragments.ImageGalleryFragment;
import com.example.roadtripapp_fbu.Fragments.TripFeedFragment;
import com.example.roadtripapp_fbu.Fragments.TripViewFragment;

/**
 * Adapter class for the Tab View in the TripFeed Activity, shows both the feed, and the image gallery
 */
public class PagerAdapterFeed extends FragmentStatePagerAdapter {

    public PagerAdapterFeed(@NonNull FragmentManager fm, int behavior) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new TripFeedFragment();
        }
        else {
            return new ImageGalleryFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Trip Feed";
        }
        else {
            return "Images";
        }
    }
}