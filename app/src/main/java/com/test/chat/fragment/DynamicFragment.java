package com.test.chat.fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DynamicFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup,
                             Bundle savedInstanceState) {
        return layoutInflater.inflate(R.layout.fragment_dynamic, viewGroup, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        super.onHiddenChanged(hidden);
    }
}