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
import androidx.fragment.app.FragmentTransaction;

import com.test.chat.R;
import com.test.chat.adapter.ComicKindListViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.view.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ComicFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private static final String PARAM = "param";
    private String param;
    private View comicFragmentView;
    private PullToRefreshListView comic_kind_PullToRefreshListView;
    private Context context;
    private List<JSONObject> comicKindJSONObjectList;
    private ComicKindListViewAdapter comicKindListViewAdapter;
    private View loading_layout;
    private final Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            loading_layout.setVisibility(View.GONE);
            comic_kind_PullToRefreshListView.setVisibility(View.VISIBLE);
        }
    };
    private List<Fragment> comicDetailFragmentList;
    private final Handler getComicKindHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONArray comicKindByPlotJSONArray = new JSONObject((String) message.obj).getJSONArray("comic_kind_by_plot");
                    for (int i = 0; i < comicKindByPlotJSONArray.length(); i++) {
                        comicKindJSONObjectList.add(comicKindByPlotJSONArray.getJSONObject(i));
                        if (i == 0) {
                            comicKindByPlotJSONArray.getJSONObject(i).put("select", 1);
                        } else {
                            comicKindByPlotJSONArray.getJSONObject(i).put("select", 0);
                        }

                    }
                    comicKindListViewAdapter.notifyDataSetChanged();
                    addFragment();
                    switchFragment(0);
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
                    e.printStackTrace();
                }
            } else {
                loading_layout.setVisibility(View.VISIBLE);
                comic_kind_PullToRefreshListView.setVisibility(View.GONE);
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "handleMessage: 异常");
            }
            super.handleMessage(message);
        }
    };

    public ComicFragment() {
    }

    public static ComicFragment newInstance(String param) {
        ComicFragment comicFragment = new ComicFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, param);
        comicFragment.setArguments(bundle);
        return comicFragment;
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
        comicFragmentView = inflater.inflate(R.layout.fragment_comic, container, false);
        initView();
        return comicFragmentView;
    }

    private void initView() {
        Log.e(TAG, "initView: " + param);
        comic_kind_PullToRefreshListView = comicFragmentView.findViewById(R.id.comic_kind_PullToRefreshListView);
        loading_layout = comicFragmentView.findViewById(R.id.loading_layout);
        loading_layout.setVisibility(View.VISIBLE);
        comic_kind_PullToRefreshListView.setVisibility(View.GONE);
        comicKindJSONObjectList = new ArrayList<>();
        comicDetailFragmentList = new ArrayList<>();
        comicKindListViewAdapter = new ComicKindListViewAdapter(context, R.layout.item_listview, comicKindJSONObjectList);
        comic_kind_PullToRefreshListView.setAdapter(comicKindListViewAdapter);
        comicKindListViewAdapter.setOnComicKindTextViewClickListener(new ComicKindListViewAdapter.ComicKindTextViewOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                for (int i = 0; i < comicKindJSONObjectList.size(); i++) {
                    try {
                        if (position == i) {
                            comicKindJSONObjectList.get(i).put("select", 1);
                        } else {
                            comicKindJSONObjectList.get(i).put("select", 0);
                        }
                        switchFragment(position);
                        comicKindListViewAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    message.obj = new HttpUtil(context).getRequest(ActivityUtil.NET_URL + "/get_comic_kind");
                    message.what = 1;
                } catch (IOException e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                getComicKindHandler.sendMessage(message);
            }
        }).start();
    }

    private void addFragment() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        for (int i = 0; i < comicKindJSONObjectList.size(); i++) {
            Fragment fragment = ComicDetailFragment.newInstance(comicKindJSONObjectList.get(i).toString());
            comicDetailFragmentList.add(fragment);
        }
        for (int i = 0; i < comicDetailFragmentList.size(); i++) {
            fragmentTransaction.add(R.id.comic_FrameLayout, comicDetailFragmentList.get(i));
        }
        fragmentTransaction.commit();
    }

    private void switchFragment(int position) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        for (int i = 0; i < comicDetailFragmentList.size(); i++) {
            Fragment fragment = comicDetailFragmentList.get(i);
            fragmentTransaction.hide(fragment);
        }
        fragmentTransaction.show(comicDetailFragmentList.get(position));
        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        comic_kind_PullToRefreshListView.setFocusable(false);
    }
}