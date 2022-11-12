package com.test.chat.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.test.chat.R;
import com.test.chat.adapter.PageRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.view.TitleFragmentPagerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressLint("MissingInflatedId")
public class AppStoreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ActivityUtil.TAG;
    private View appStoreFragment;
    private Context context;
    private TabLayout app_store_title_TabLayout;
    private ViewPager app_store_content_ViewPager;
    private View loading_layout;
    private SwipeRefreshLayout app_store_SwipeRefreshLayout;
    private ProgressDialog progressDialog;
    private final Handler showPageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                JSONObject jsonObject = new JSONObject((String) msg.obj);
                int pageNum = Integer.parseInt(jsonObject.getString("kind_page"));
                progressDialog.setContentView(R.layout.apk_page_bar);
                RecyclerView apk_page_RecyclerView = progressDialog.findViewById(R.id.apk_page_RecyclerView);
                List<Integer> pageNumList = new ArrayList<>();
                for (int i = 1; i <= pageNum; i++) {
                    pageNumList.add(i);
                }
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                apk_page_RecyclerView.setLayoutManager(linearLayoutManager);
                PageRecyclerViewAdapter pageRecyclerViewAdapter = new PageRecyclerViewAdapter(pageNumList);
                pageRecyclerViewAdapter.setOnPageSelectOnItemClickListener(new PageRecyclerViewAdapter.PageSelectOnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Toast.makeText(context, ""+position, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
                apk_page_RecyclerView.setAdapter(pageRecyclerViewAdapter);
                super.handleMessage(msg);
            } catch (JSONException e) {
                progressDialog.dismiss();
                Toast.makeText(context, "当前分类无应用！", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    };
    private Boolean TAB_IS_SELECT;
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
                        for (int j = 0; j < jsonArray.length(); j++) {
                            String kindName = jsonArray.getJSONObject(j).getString("kind_name");
                            View view = LayoutInflater.from(context).inflate(R.layout.apk_list_tab_selected, app_store_title_TabLayout, false);
                            TextView kind_name_TextView = view.findViewById(R.id.kind_name_TextView);
                            kind_name_TextView.setText(kindName);
                            Objects.requireNonNull(app_store_title_TabLayout.getTabAt(j)).setCustomView(view);
                            ImageView kind_ImageView = view.findViewById(R.id.kind_ImageView);
                            kind_ImageView.setVisibility(View.GONE);
                        }
                        TAB_IS_SELECT = false;
                        app_store_title_TabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                TAB_IS_SELECT = false;
                                if (tab != null) {
                                    ImageView kind_ImageView = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.kind_ImageView);
                                    kind_ImageView.setImageResource(R.drawable.message_no_show);
                                    kind_ImageView.setVisibility(View.VISIBLE);
                                    if (tab.getPosition() == 0) {
                                        kind_ImageView.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                                if (tab != null) {
                                    Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.kind_ImageView).setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                                if (tab != null) {
                                    ImageView kind_ImageView = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.kind_ImageView);
                                    if (TAB_IS_SELECT) {
                                        kind_ImageView.setImageResource(R.drawable.message_no_show);
                                        TAB_IS_SELECT = false;
                                    } else {
                                        kind_ImageView.setImageResource(R.drawable.message_show);
                                        TAB_IS_SELECT = true;
                                        int position = tab.getPosition();
                                        if (position != 0) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Message msg = new Message();
                                                    Map<String, String> parameter = new HashMap<>();
                                                    try {
                                                        parameter.put("kind_link", jsonArray.getJSONObject(position).getString("kind_link"));
                                                        parameter.put("kind_name", jsonArray.getJSONObject(position).getString("kind_name"));
                                                    } catch (JSONException e) {
                                                        msg.what = 0;
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        msg.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_page_num_by_kind_link", parameter);
                                                        msg.what = 1;
                                                    } catch (IOException e) {
                                                        msg.what = 0;
                                                        e.printStackTrace();
                                                    }
                                                    showPageHandler.sendMessage(msg);
                                                }
                                            }).start();
                                            progressDialog = new ProgressDialog(context);
                                            Window window = progressDialog.getWindow();
                                            if (window != null) {
                                                WindowManager.LayoutParams params = window.getAttributes();
                                                params.gravity = Gravity.CENTER;
                                                progressDialog.setCancelable(false);
                                                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                progressDialog.show();
                                                progressDialog.setContentView(R.layout.loading_progress_bar);
                                            }
                                            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                                @Override
                                                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                                                    if (keyEvent.getKeyCode() == 4) {
                                                        TAB_IS_SELECT = false;
                                                        kind_ImageView.setImageResource(R.drawable.message_no_show);
                                                        dialogInterface.dismiss();
                                                    }
                                                    return false;
                                                }
                                            });
                                        }
                                    }

                                }
                            }
                        });
                        app_store_SwipeRefreshLayout.setEnabled(false);
                        loading_layout.setVisibility(View.GONE);
                    } else {
                        app_store_SwipeRefreshLayout.setEnabled(true);
                        Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    app_store_SwipeRefreshLayout.setEnabled(true);
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                app_store_SwipeRefreshLayout.setEnabled(true);
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
        initFragmentView();
        initTitleView();
        return appStoreFragment;
    }

    private void initTitleView() {
        TextView top_title_TextView = appStoreFragment.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText(new String("应用商城"));
        ImageView title_left_ImageView = appStoreFragment.findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setVisibility(View.GONE);
        ImageView title_right_ImageView = appStoreFragment.findViewById(R.id.title_right_ImageView);
        title_right_ImageView.setImageResource(R.drawable.down_button);
    }

    private void initFragmentView() {
        loading_layout = appStoreFragment.findViewById(R.id.loading_layout);
        loading_layout.setVisibility(View.VISIBLE);
        app_store_SwipeRefreshLayout = appStoreFragment.findViewById(R.id.app_store_SwipeRefreshLayout);
        app_store_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        app_store_SwipeRefreshLayout.setOnRefreshListener(this);
        app_store_SwipeRefreshLayout.setRefreshing(false);
        LinearLayout app_store_LinearLayout = appStoreFragment.findViewById(R.id.app_store_LinearLayout);
        ActivityUtil.setLinearLayoutBackground(app_store_LinearLayout, SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
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
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        initFragmentView();
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onRefresh() {
        initFragmentView();
    }
}