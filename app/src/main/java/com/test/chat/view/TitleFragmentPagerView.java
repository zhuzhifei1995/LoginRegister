package com.test.chat.view;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class TitleFragmentPagerView extends FragmentPagerAdapter {

    private final List<Fragment> fragments;
    private final FragmentManager fragmentManager;

    public TitleFragmentPagerView(@NonNull FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup viewGroup, int position) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i < getCount(); i++) {
            Fragment fragment = fragmentManager.findFragmentByTag(makeFragmentName(viewGroup.getId(), getItemId(i)));
            if (fragment != null) {
                fragmentTransaction.remove(fragment);
            }
        }
        fragmentTransaction.add(viewGroup.getId(), getItem(position)).attach(getItem(position)).commitAllowingStateLoss();
        return getItem(position);
    }

    private String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }

}
