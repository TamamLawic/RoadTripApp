package com.example.roadtripapp_fbu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.roadtripapp_fbu.Fragments.BucketlistViewFragment;
import com.example.roadtripapp_fbu.Fragments.OtherUserTripViewFragment;

/**
 * Adapter class for the Tab View in a different user's Profile Fragment. Gets new fragments for bucketlistfragment, and trip view fragments.
 */
public class PagerAdapterProfile extends FragmentStatePagerAdapter {

    public PagerAdapterProfile(@NonNull FragmentManager fm, int behavior) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new OtherUserTripViewFragment();
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
