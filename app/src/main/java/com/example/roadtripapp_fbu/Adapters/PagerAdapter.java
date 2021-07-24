package com.example.roadtripapp_fbu.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.roadtripapp_fbu.Fragments.BucketlistViewFragment;
import com.example.roadtripapp_fbu.Fragments.TripViewFragment;

/**
 * Adapter class for the Tab View in the Profile Fragment. Gets new fragments for bucketlistfragment, and trip view fragments.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new TripViewFragment();
        }
        else {
            return new BucketlistViewFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Trips";
        }
        else {
            return "Bucket List";
        }
    }
}
