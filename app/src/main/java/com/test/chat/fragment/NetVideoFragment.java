package com.test.chat.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.test.chat.R;
import com.test.chat.activity.NetVideoPlayActivity;
import com.test.chat.adapter.NetVideoAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.KeyboardStateObserver;
import com.test.chat.view.PullToRefreshListView;
import com.test.chat.view.view.PullToRefreshBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class NetVideoFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private static final String PARAM = "param";
    private final int LOAD_MORE = 1;
    private final int LOAD_ERROR = 0;
    private View loading_layout;
    private TextView no_video_TextView;
    private PullToRefreshListView video_PullToRefreshListView;
    private LinearLayout search_LinearLayout;
    private List<JSONObject> netVideoJSONObjectList;
    private NetVideoAdapter netVideoAdapter;
    private final Handler searchVideoHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("videos");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            netVideoJSONObjectList.add(jsonArray.getJSONObject(i));
                        }
                    }
                    if (netVideoJSONObjectList.size() == 0) {
                        video_PullToRefreshListView.setVisibility(View.GONE);
                        no_video_TextView.setVisibility(View.VISIBLE);
                        no_video_TextView.setText(new String("没有符合条件的视频！"));
                    } else {
                        video_PullToRefreshListView.setAdapter(netVideoAdapter);
                        netVideoAdapter.notifyDataSetChanged();
                        video_PullToRefreshListView.setVisibility(View.VISIBLE);
                        no_video_TextView.setVisibility(View.GONE);
                    }
                    loading_layout.setVisibility(View.GONE);
                    search_LinearLayout.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                video_PullToRefreshListView.setVisibility(View.GONE);
                no_video_TextView.setVisibility(View.VISIBLE);
                no_video_TextView.setText(R.string.please_input_keyword);
                loading_layout.setVisibility(View.GONE);
                search_LinearLayout.setVisibility(View.VISIBLE);
            }
            super.handleMessage(message);
        }
    };
    private String keyword;
    private String param;
    private View netVideoFragmentView;
    private Context context;
    private Activity activity;
    private int PAGE_NUM;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == LOAD_MORE) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("videos");
                        if (jsonArray.length() == 0) {
                            PAGE_NUM = PAGE_NUM - 1;
                            Toast.makeText(context, "没有更多视频了！", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject videoJSONObject = jsonArray.getJSONObject(i);
                                netVideoJSONObjectList.add(videoJSONObject);
                            }
                            Toast.makeText(context, "加载更多成功！", Toast.LENGTH_SHORT).show();
                            netVideoAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
            }
            video_PullToRefreshListView.onRefreshComplete();
            super.handleMessage(message);
        }
    };

    public NetVideoFragment() {
    }

    public static NetVideoFragment newInstance(String param) {
        NetVideoFragment fragment = new NetVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, param);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();
        Log.e(TAG, "onCreate: " + param);
        if (getArguments() != null) {
            param = getArguments().getString(PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        netVideoFragmentView = inflater.inflate(R.layout.fragment_net_video, container, false);
        initView();
        return netVideoFragmentView;
    }

    private void initView() {
        PAGE_NUM = 1;
        EditText search_video_EditText = netVideoFragmentView.findViewById(R.id.search_video_EditText);
        ActivityUtil.showKeyboard(activity, search_video_EditText);
        search_video_EditText.requestFocus();
        Button search_video_Button = netVideoFragmentView.findViewById(R.id.search_video_Button);
        loading_layout = netVideoFragmentView.findViewById(R.id.loading_layout);
        no_video_TextView = netVideoFragmentView.findViewById(R.id.no_video_TextView);
        video_PullToRefreshListView = netVideoFragmentView.findViewById(R.id.video_PullToRefreshListView);
        netVideoJSONObjectList = new ArrayList<>();
        netVideoAdapter = new NetVideoAdapter(context, R.layout.net_video_list_view, netVideoJSONObjectList);
        netVideoAdapter.setOnNetVideoLinearLayoutClickListener(new NetVideoAdapter.NetVideoLinearLayoutOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                try {
                    Intent intent = new Intent(context, NetVideoPlayActivity.class);
                    String fileDownloadUrl = netVideoJSONObjectList.get(position).getString("video_source");
                    String fileName = netVideoJSONObjectList.get(position).getString("video_name") + ".mp4";
                    intent.putExtra("fileDownloadUrl", fileDownloadUrl);
                    intent.putExtra("fileName", fileName);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        search_LinearLayout = netVideoFragmentView.findViewById(R.id.search_LinearLayout);
        initKeyboardStateObserver();
        search_video_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PAGE_NUM = 1;
                netVideoJSONObjectList.clear();
                keyword = search_video_EditText.getText().toString();
                ActivityUtil.hideKeyboard(activity);
                if (!keyword.trim().equals("")) {
                    loading_layout.setVisibility(View.VISIBLE);
                    video_PullToRefreshListView.setVisibility(View.GONE);
                    no_video_TextView.setVisibility(View.GONE);
                    search_LinearLayout.setVisibility(View.GONE);
                    Message message = new Message();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Map<String, String> parameter = new HashMap<>();
                                parameter.put("keyword", keyword);
                                parameter.put("page_num", "" + 1);
                                message.obj = new HttpUtil(context, 5).postRequest(ActivityUtil.NET_URL + "/get_net_video", parameter);
                                message.what = 1;
                            } catch (IOException e) {
                                message.what = 0;
                                e.printStackTrace();
                            }
                            searchVideoHandler.sendMessage(message);
                        }
                    }).start();
                } else {
                    video_PullToRefreshListView.setVisibility(View.GONE);
                    no_video_TextView.setVisibility(View.VISIBLE);
                    no_video_TextView.setText(R.string.please_input_keyword);
                    loading_layout.setVisibility(View.GONE);
                    search_LinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        video_PullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                @SuppressLint("SimpleDateFormat")
                String timeLabel = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(timeLabel);
                if (refreshView.getFooterLayout().isShown()) {
                    new Thread() {
                        public void run() {
                            PAGE_NUM = PAGE_NUM + 1;
                            Message message = new Message();
                            try {
                                Map<String, String> parameter = new HashMap<>();
                                parameter.put("keyword", keyword);
                                parameter.put("page_num", "" + PAGE_NUM);
                                message.obj = new HttpUtil(context, 5).postRequest(ActivityUtil.NET_URL + "/get_net_video", parameter);
                                message.what = LOAD_MORE;
                            } catch (Exception e) {
                                message.what = LOAD_ERROR;
                                e.printStackTrace();
                            }
                            refreshHandler.sendMessage(message);
                        }
                    }.start();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        video_PullToRefreshListView.setFocusable(false);
    }

    private void initKeyboardStateObserver() {
        KeyboardStateObserver.getKeyboardStateObserver(activity).
                setKeyboardVisibilityListener(new KeyboardStateObserver.OnKeyboardVisibilityListener() {
                    @Override
                    public void onKeyboardShow() {
                        Log.e(TAG, "键盘弹出");
                        video_PullToRefreshListView.setVisibility(View.GONE);
                        no_video_TextView.setVisibility(View.GONE);
                        loading_layout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onKeyboardHide() {
                        Log.e(TAG, "键盘收回");
                    }
                });
    }
}