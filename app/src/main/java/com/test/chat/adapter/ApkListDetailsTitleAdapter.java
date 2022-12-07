package com.test.chat.adapter;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.test.chat.fragment.ApkHomePageFragment;
import com.test.chat.fragment.ApkListDetailsFragment;

import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ApkListDetailsTitleAdapter extends FragmentStateAdapter {

    private final List<JSONObject> jsonObjectList;

    public ApkListDetailsTitleAdapter(@NonNull FragmentActivity fragmentActivity, List<JSONObject> jsonObjectList) {
        super(fragmentActivity);
        this.jsonObjectList = jsonObjectList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return ApkHomePageFragment.newInstance(jsonObjectList.get(position));
        } else {
            return ApkListDetailsFragment.newInstance(jsonObjectList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

}