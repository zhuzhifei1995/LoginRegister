package com.test.chat.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.test.chat.R;
import com.test.chat.util.ImageUtil;
import com.test.chat.view.TitleFragmentPagerView;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class AppStoreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = ActivityUtil.TAG;
    private View appStoreFragment;
    private Context context;
    private TabLayout app_store_title_TabLayout;
    private ViewPager app_store_content_ViewPager;
    private SwipeRefreshLayout app_store_SwipeRefreshLayout;
    private List<JSONObject> jsonObjectList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        appStoreFragment = inflater.inflate(R.layout.fragment_app_store, container, false);
        TextView top_title_TextView = appStoreFragment.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText(new String("应用商城"));
        initFragmentView();
        return appStoreFragment;
    }

    private void initFragmentView() {
        app_store_SwipeRefreshLayout = appStoreFragment.findViewById(R.id.app_store_SwipeRefreshLayout);
        app_store_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        app_store_SwipeRefreshLayout.setOnRefreshListener(this);
        app_store_SwipeRefreshLayout.setRefreshing(true);
        app_store_title_TabLayout = appStoreFragment.findViewById(R.id.app_store_title_TabLayout);
        app_store_content_ViewPager = appStoreFragment.findViewById(R.id.app_store_content_ViewPager);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    message.what = 1;
                    message.obj = new HttpUtil(context).getRequest(ActivityUtil.NET_URL+"/get_all_kind");
                } catch (IOException e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                getAllAppStoreKindHandler.sendMessage(message);
            }
        }).start();
    }

    private final Handler getAllAppStoreKindHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1){
                try {
                    JSONArray jsonArray = new JSONObject((String) message.obj).getJSONArray("message");
                    List<Fragment> fragments = new ArrayList<>();
                    for (int i = 0;i<jsonArray.length();i++){
                        String kindLink = jsonArray.getJSONObject(i).getString("kind_link");
                        JSONArray apkJSONArray = jsonArray.getJSONObject(i).getJSONArray("apk_list");
                        jsonObjectList = new ArrayList<>();
                        for (int j = 0;j<apkJSONArray.length();j++){
                            JSONObject apkJSONObject = apkJSONArray.getJSONObject(j);
                            jsonObjectList.add(apkJSONObject);
                            String apkIcon = apkJSONObject.getString("apk_icon");
                            String apkFileName = apkJSONObject.getString("apk_name")+".cache";
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = new HttpUtil(context).getImageBitmap(apkIcon);
                                    ImageUtil.saveBitmapToTmpFile(bitmap, ActivityUtil.TMP_APK_ICON_PATH,apkFileName);
                                }
                            }).start();
                        }
                        AppListDetailsFragment appListDetailsFragment = new AppListDetailsFragment(kindLink, jsonObjectList);
                        fragments.add(appListDetailsFragment);
                    }
                    TitleFragmentPagerView titleFragmentPagerView = new TitleFragmentPagerView(requireActivity().getSupportFragmentManager(), fragments);
                    app_store_content_ViewPager.setAdapter(titleFragmentPagerView);
                    app_store_title_TabLayout.setupWithViewPager(app_store_content_ViewPager);
                    for (int i = 0;i<jsonArray.length();i++){
                        String kindName = jsonArray.getJSONObject(i).getString("kind_name");
                        Objects.requireNonNull(app_store_title_TabLayout.getTabAt(i)).setText(kindName);
                        app_store_SwipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(context, "加载成功！", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    app_store_SwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }else {
                app_store_SwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(message);
        }
    };

    @Override
    public void onRefresh() {
        initFragmentView();
    }
}