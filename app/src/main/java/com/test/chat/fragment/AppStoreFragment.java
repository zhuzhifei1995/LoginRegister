package com.test.chat.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.view.TitleFragmentPagerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class AppStoreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ActivityUtil.TAG;
    private View appStoreFragment;
    private Context context;
    private TabLayout app_store_title_TabLayout;
    private ViewPager app_store_content_ViewPager;
    private View loading_layout;
    private SwipeRefreshLayout app_store_SwipeRefreshLayout;
    private final Handler getAllAppStoreKindHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                        List<Fragment> fragments = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject kindJSONObject = jsonArray.getJSONObject(i);
                            AppListDetailsFragment appListDetailsFragment = new AppListDetailsFragment(kindJSONObject);
                            fragments.add(appListDetailsFragment);
                        }
                        TitleFragmentPagerView titleFragmentPagerView = new TitleFragmentPagerView(requireActivity().getSupportFragmentManager(), fragments);
                        app_store_content_ViewPager.setAdapter(titleFragmentPagerView);
                        app_store_title_TabLayout.setupWithViewPager(app_store_content_ViewPager);
                        app_store_content_ViewPager.setOffscreenPageLimit(1);
                        for (int j = 0; j < jsonArray.length(); j++) {
                            String kindName = jsonArray.getJSONObject(j).getString("kind_name");
                            Objects.requireNonNull(app_store_title_TabLayout.getTabAt(j)).setText(kindName);
                        }
                        app_store_SwipeRefreshLayout.setEnabled(false);
                        loading_layout.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(message);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        appStoreFragment = inflater.inflate(R.layout.fragment_app_store, container, false);
        initTitleView();
        initFragmentView();
        return appStoreFragment;
    }

    private void initTitleView() {
        TextView top_title_TextView = appStoreFragment.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText(new String("应用商城"));
        ImageView title_left_ImageView = appStoreFragment.findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setVisibility(View.GONE);
        loading_layout = appStoreFragment.findViewById(R.id.loading_layout);
        ImageView title_right_ImageView = appStoreFragment.findViewById(R.id.title_right_ImageView);
        title_right_ImageView.setImageResource(R.drawable.down_button);
    }

    private void initFragmentView() {
        app_store_SwipeRefreshLayout = appStoreFragment.findViewById(R.id.app_store_SwipeRefreshLayout);
        app_store_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        app_store_SwipeRefreshLayout.setOnRefreshListener(this);
        app_store_SwipeRefreshLayout.setRefreshing(false);
        loading_layout.setVisibility(View.VISIBLE);
        app_store_title_TabLayout = appStoreFragment.findViewById(R.id.app_store_title_TabLayout);
        app_store_content_ViewPager = appStoreFragment.findViewById(R.id.app_store_content_ViewPager);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    message.what = 1;
                    message.obj = new HttpUtil(context).getRequest(ActivityUtil.NET_URL + "/get_all_apk_kind");
                } catch (IOException e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                getAllAppStoreKindHandler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onRefresh() {
        initFragmentView();
    }
}