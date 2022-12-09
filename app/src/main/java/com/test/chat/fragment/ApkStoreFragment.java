package com.test.chat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.test.chat.R;
import com.test.chat.activity.ApkFileDownActivity;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.view.PullToRefreshListView;
import com.test.chat.view.TitleAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ApkStoreFragment extends Fragment {

    private List<JSONObject> jsonObjectList;
    private View appStoreFragment;
    private Context context;
    private View app_store_View;
    private ViewPager2 app_list_ViewPager2;
    private final Handler getAppKindJSONObjectHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (message.what == 1) {
                        if (jsonObject.getString("code").equals("1")) {
                            jsonObjectList = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray("message");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject appKindJSONObject = jsonArray.getJSONObject(i);
                                jsonObjectList.add(appKindJSONObject);
                            }
                            initView();
                        }
                    } else {
                        jsonObjectList = null;
                    }
                } catch (JSONException e) {
                    jsonObjectList = null;
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(message);
        }
    };

    public ApkStoreFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup,
                             Bundle savedInstanceState) {
        appStoreFragment = layoutInflater.inflate(R.layout.fragment_app_store, viewGroup, false);
        initApkStoreFragmentView();
        return appStoreFragment;
    }

    private void initApkStoreFragmentView() {
        app_store_View = appStoreFragment.findViewById(R.id.app_store_View);
        app_store_View.setVisibility(View.VISIBLE);
        LinearLayout app_store_LinearLayout = appStoreFragment.findViewById(R.id.app_store_LinearLayout);
        ActivityUtil.setLinearLayoutBackground(app_store_LinearLayout, SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        TextView top_title_TextView = appStoreFragment.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText(R.string.apk_store);
        ImageView title_left_ImageView = appStoreFragment.findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setVisibility(View.GONE);
        ImageView title_right_ImageView = appStoreFragment.findViewById(R.id.title_right_ImageView);
        title_right_ImageView.setVisibility(View.VISIBLE);
        title_right_ImageView.setImageResource(R.drawable.down_button);
        title_right_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ApkFileDownActivity.class));
            }
        });
        ImageView search_button = appStoreFragment.findViewById(R.id.title_right_ImageView2);
        search_button.setImageResource(R.drawable.search_button);
        search_button.setVisibility(View.VISIBLE);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        getAppKindJSONObject();
    }

    private void initView() {
        TabLayout app_list_title_TabLayout = appStoreFragment.findViewById(R.id.app_list_title_TabLayout);
        app_list_ViewPager2 = appStoreFragment.findViewById(R.id.app_list_ViewPager2);
        List<Fragment> fragmentList = new ArrayList<>();
        ApkHomePageFragment apkHomePageFragment = ApkHomePageFragment.newInstance(jsonObjectList.get(0));
        fragmentList.add(apkHomePageFragment);
        for (int i = 1; i < jsonObjectList.size(); i++) {
            ApkListDetailsFragment apkListDetailsFragment = ApkListDetailsFragment.newInstance(jsonObjectList.get(i));
            fragmentList.add(apkListDetailsFragment);
        }
        TitleAdapterView apkStoreTitleAdapterView = new TitleAdapterView(requireActivity(), fragmentList);
        app_list_ViewPager2.setAdapter(apkStoreTitleAdapterView);
        app_list_ViewPager2.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                app_list_ViewPager2.setUserInputEnabled(false);
            }
        });
        new TabLayoutMediator(app_list_title_TabLayout, app_list_ViewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                try {
                    tab.setText(jsonObjectList.get(position).getString("kind_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).attach();
        for (int i = 0; i < app_list_title_TabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = app_list_title_TabLayout.getTabAt(i);
            if (tab != null) {
                tab.view.setLongClickable(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tab.view.setTooltipText(null);
                }
            }
        }
        app_store_View.setVisibility(View.GONE);
    }

    private void getAppKindJSONObject() {
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
                getAppKindJSONObjectHandler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(ActivityUtil.TAG, "onHiddenChanged: " + getClass().getSimpleName());
        ActivityUtil.setLinearLayoutBackground(appStoreFragment.findViewById(R.id.app_store_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        super.onHiddenChanged(hidden);
    }
}