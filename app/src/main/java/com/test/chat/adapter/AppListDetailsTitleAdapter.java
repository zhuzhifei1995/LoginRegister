package com.test.chat.adapter;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.test.chat.fragment.AppListDetailsFragment;
import com.test.chat.fragment.BlankFragment;

import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class AppListDetailsTitleAdapter extends FragmentStateAdapter {

    private final List<JSONObject> jsonObjectList;

    public AppListDetailsTitleAdapter(@NonNull FragmentActivity fragmentActivity, List<JSONObject> jsonObjectList) {
        super(fragmentActivity);
        this.jsonObjectList = jsonObjectList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return BlankFragment.newInstance(jsonObjectList.get(position).toString());
        } else {
            return AppListDetailsFragment.newInstance(jsonObjectList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

}