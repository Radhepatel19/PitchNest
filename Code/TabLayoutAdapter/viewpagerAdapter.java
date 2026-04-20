package com.example.businessidea.TabLayoutAdapter;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.businessidea.fragment.FollowersFragment;
import com.example.businessidea.fragment.FollowingFragment;

public class viewpagerAdapter extends FragmentPagerAdapter {

    private final String dataToFollowers;
    private final String dataToFollowing;

    public viewpagerAdapter(@NonNull FragmentManager fm, String followersData, String followingData) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.dataToFollowers = followersData;
        this.dataToFollowing = followingData;
    }
    @Override
    public Fragment getItem(int position) {
        if (position==0){
            return FollowersFragment.newInstance(dataToFollowers);
        } else {
            return FollowingFragment.newInstance(dataToFollowing);
        }
    }

    @Override
    public int getCount() {
        return 2; // no. of tabs
    }


    @Override
    public CharSequence getPageTitle(int position) {
        if (position==0){
            return "Followers";
        }else {
            return "Following";
        }
    }
}

