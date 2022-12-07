package com.test.chat.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.test.chat.R;
import com.test.chat.adapter.BannerListViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.view.PullToRefreshListView;
import com.test.chat.view.view.PullToRefreshBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ApkHomePageFragment extends Fragment {

    private static final String PARAM = "param";
    private String param;
    private View apkHomePageFragmentView;
    private Context context;
    private List<JSONObject> bannerJSONObjectList;
    private PullToRefreshListView banner_PullToRefreshListView;
    private View loading_layout;

    private final Handler getApkHomePageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            bannerJSONObjectList.add(jsonArray.getJSONObject(i));
                        }
                    }
                    BannerListViewAdapter bannerListViewAdapter = new BannerListViewAdapter(context, R.layout.banner_list_view, bannerJSONObjectList);
                    banner_PullToRefreshListView.setAdapter(bannerListViewAdapter);
                    loading_layout.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
            }
            banner_PullToRefreshListView.onRefreshComplete();
            super.handleMessage(message);
        }
    };

    public ApkHomePageFragment() {
    }

    public static ApkHomePageFragment newInstance(JSONObject jsonObject) {
        ApkHomePageFragment apkHomePageFragment = new ApkHomePageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, jsonObject.toString());
        apkHomePageFragment.setArguments(bundle);
        return apkHomePageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        if (getArguments() != null) {
            param = getArguments().getString(PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        apkHomePageFragmentView = inflater.inflate(R.layout.fragment_app_home_page, container, false);
        initView();
        return apkHomePageFragmentView;
    }

    private void getBanner() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    JSONObject jsonObject = new JSONObject(param);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("kind_link", jsonObject.getString("kind_link"));
                    parameter.put("kind_name", jsonObject.getString("kind_name"));
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_apk_list_by_kind_link", parameter);
                    message.what = 1;
                } catch (Exception e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                getApkHomePageHandler.sendMessage(message);
            }
        }).start();
    }

    private void initView() {
        banner_PullToRefreshListView = apkHomePageFragmentView.findViewById(R.id.banner_PullToRefreshListView);
        loading_layout = apkHomePageFragmentView.findViewById(R.id.loading_layout);
        loading_layout.setVisibility(View.VISIBLE);
        bannerJSONObjectList = new ArrayList<>();
        getBanner();
        banner_PullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                @SuppressLint("SimpleDateFormat")
                String timeLabel = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(timeLabel);
                if (refreshView.getHeaderLayout().isShown()) {
                    bannerJSONObjectList.clear();
                    getBanner();
                }
            }
        });
    }
}