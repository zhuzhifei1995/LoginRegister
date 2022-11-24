package com.test.chat.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.test.chat.fragment.AppListDetailsFragment;

import org.json.JSONObject;

import java.util.List;

public class AppListDetailsTitleAdapter extends FragmentStateAdapter {

    private final List<JSONObject> jsonObjectList;

    public AppListDetailsTitleAdapter(@NonNull FragmentActivity fragmentActivity, List<JSONObject> jsonObjectList) {
        super(fragmentActivity);
        this.jsonObjectList = jsonObjectList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return AppListDetailsFragment.newInstance(jsonObjectList.get(position));
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

}