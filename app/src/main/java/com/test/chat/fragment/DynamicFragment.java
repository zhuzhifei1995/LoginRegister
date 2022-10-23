package com.test.chat.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.chat.R;
import com.test.chat.adapter.FileRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DynamicFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ActivityUtil.TAG;
    private View dynamicFragmentView;
    private Context context;
    private RecyclerView dynamic_RecyclerView;
    private List<JSONObject> fileJSONObjectList;
    private SwipeRefreshLayout dynamic_SwipeRefreshLayout;


    private final Handler getDownloadFilesHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            try {
                JSONObject jsonObject = new JSONObject((String) message.obj);
                JSONArray jsonArray = jsonObject.getJSONArray("message");
                fileJSONObjectList = new ArrayList<JSONObject>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject fileJSONObject = jsonArray.getJSONObject(i);
                    fileJSONObjectList.add(fileJSONObject);
                }
            } catch (JSONException e) {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                dynamic_SwipeRefreshLayout.setRefreshing(false);
                e.printStackTrace();
            }
            super.handleMessage(message);
            if (fileJSONObjectList != null) {
                FileRecyclerViewAdapter fileRecyclerViewAdapter = new FileRecyclerViewAdapter(fileJSONObjectList);
                dynamic_RecyclerView.setLayoutManager(new LinearLayoutManager(context));
                dynamic_RecyclerView.setAdapter(fileRecyclerViewAdapter);
                dynamic_SwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, "加载成功！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        dynamicFragmentView = layoutInflater.inflate(R.layout.fragment_dynamic, viewGroup, false);
        initFragmentView();
        return dynamicFragmentView;
    }

    private void initFragmentView() {
        dynamic_RecyclerView = dynamicFragmentView.findViewById(R.id.dynamic_RecyclerView);
        dynamic_SwipeRefreshLayout = dynamicFragmentView.findViewById(R.id.dynamic_SwipeRefreshLayout);
        dynamic_SwipeRefreshLayout.setOnRefreshListener(this);
        dynamic_SwipeRefreshLayout.setRefreshing(true);
        dynamic_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.obj = new HttpUtil(context).getRequest(ActivityUtil.NET_URL + "/get_download_files");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getDownloadFilesHandler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onRefresh() {
        dynamic_SwipeRefreshLayout.setRefreshing(true);
        Toast.makeText(context, "刷新中，请稍后......", Toast.LENGTH_LONG).show();
        initFragmentView();
    }
}