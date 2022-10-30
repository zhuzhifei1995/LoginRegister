package com.test.chat.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.chat.R;
import com.test.chat.adapter.ApkRecyclerViewAdapter;
import com.test.chat.adapter.FileRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;

import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class AppListDetailsFragment extends Fragment{

    private static final String TAG = ActivityUtil.TAG;
    private String kindLink;
    private View appListDetailsFragmentView;
    private List<JSONObject> jsonObjectList;
    private Context context;

    public AppListDetailsFragment() {
    }

    public AppListDetailsFragment(String kindLink, List<JSONObject> jsonObjectList) {
        this.kindLink = kindLink;
        this.jsonObjectList = jsonObjectList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        appListDetailsFragmentView = inflater.inflate(R.layout.fragment_app_list_details, container, false);
        initFragmentView();
        return appListDetailsFragmentView;
    }

    private void initFragmentView() {
        RecyclerView apk_file_RecyclerView = appListDetailsFragmentView.findViewById(R.id.apk_file_RecyclerView);
        ApkRecyclerViewAdapter apkRecyclerViewAdapter = new ApkRecyclerViewAdapter(jsonObjectList);
        apk_file_RecyclerView.setLayoutManager(new LinearLayoutManager(context));
        apk_file_RecyclerView.setAdapter(apkRecyclerViewAdapter);
    }
}