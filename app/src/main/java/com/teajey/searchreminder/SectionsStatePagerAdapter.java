package com.teajey.searchreminder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private LinkedHashMap<String, Fragment> fragments = new LinkedHashMap<>();

    SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment f, String title) {
        fragments.put(title, f);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        ArrayList<Fragment> fragmentArrayList = new ArrayList<>(fragments.values());
        return fragmentArrayList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
