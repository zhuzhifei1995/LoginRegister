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

import com.test.chat.R;
import com.test.chat.adapter.ComicListViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.view.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ComicDetailFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private static final String PARAM = "param";
    private String param;
    private View comicDetailFragmentView;
    private Context context;
    private View comic_loading_layout;
    private PullToRefreshListView comic_PullToRefreshListView;
    private final Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            comic_loading_layout.setVisibility(View.GONE);
            comic_PullToRefreshListView.setVisibility(View.VISIBLE);
        }
    };
    private List<JSONObject> comicJSONObjectList;
    private ComicListViewAdapter comicListViewAdapter;
    private final Handler getComicsByKindHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    JSONArray jsonArray = jsonObject.getJSONArray("comics");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        comicJSONObjectList.add(jsonArray.getJSONObject(i));
                    }
                    comicListViewAdapter = new ComicListViewAdapter(context, R.layout.comic_list_view, comicJSONObjectList);
                    comic_PullToRefreshListView.setAdapter(comicListViewAdapter);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                waitHandler.sendEmptyMessage(0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (JSONException e) {
                    comic_loading_layout.setVisibility(View.VISIBLE);
                    comic_PullToRefreshListView.setVisibility(View.GONE);
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "handleMessage: 异常");
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "handleMessage: 异常");
            }
            super.handleMessage(message);
        }
    };

    public ComicDetailFragment() {
    }

    public static ComicDetailFragment newInstance(String param) {
        ComicDetailFragment comicDetailFragment = new ComicDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, param);
        comicDetailFragment.setArguments(bundle);
        return comicDetailFragment;
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
        comicDetailFragmentView = inflater.inflate(R.layout.fragment_comic_detail, container, false);
        initView();
        return comicDetailFragmentView;
    }

    private void initView() {
        comic_loading_layout = comicDetailFragmentView.findViewById(R.id.comic_loading_layout);
        comic_PullToRefreshListView = comicDetailFragmentView.findViewById(R.id.comic_PullToRefreshListView);
        comicJSONObjectList = new ArrayList<>();
        comic_loading_layout.setVisibility(View.VISIBLE);
        comic_PullToRefreshListView.setVisibility(View.GONE);
        try {
            JSONObject comicKindJSONObject = new JSONObject(param);
            String kindUrl = comicKindJSONObject.getString("kind_link");
            String kindName = comicKindJSONObject.getString("kind_name");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("kind_url", kindUrl);
                    parameter.put("page_num", String.valueOf(1));
                    parameter.put("kind_name", kindName);
                    parameter.put("order_type", ActivityUtil.ORDER_TYPE_POST);
                    Message message = new Message();
                    try {
                        message.obj = new HttpUtil(context, 5).postRequest(ActivityUtil.NET_URL + "/get_comics_by_kind", parameter);
                        message.what = 1;
                    } catch (IOException e) {
                        e.printStackTrace();
                        message.what = 0;
                    }
                    getComicsByKindHandler.sendMessage(message);
                }
            }).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}