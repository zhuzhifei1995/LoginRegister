package com.test.chat.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.chat.R;
import com.test.chat.activity.FriendShowActivity;
import com.test.chat.activity.LoginActivity;
import com.test.chat.adapter.FriendRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.KeyboardStateObserver;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FriendFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private View friendFragmentView;
    private Activity activity;
    private Context context;
    private final Handler getMessageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                final JSONArray messagesJSONArray = jsonObject.getJSONArray("message");
                for (int i = 0; i < messagesJSONArray.length(); i++) {
                    if (messagesJSONArray.getJSONObject(i).getString("message_type").equals("2")) {
                        final String messageImageUrl = messagesJSONArray.getJSONObject(i).getString("message_image_url");
                        final String imageName = messagesJSONArray.getJSONObject(i).getString("message");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap photoBitmap = new HttpUtil(context).getImageBitmap(messageImageUrl);
                                if (photoBitmap != null) {
                                    ImageUtil.saveBitmapToTmpFile(photoBitmap, ActivityUtil.TMP_MESSAGE_FILE_PATH, imageName + ".cache");
                                }
                            }
                        }).start();
                    }
                    if (messagesJSONArray.getJSONObject(i).getString("message_type").equals("3")) {
                        final String messageVoiceUrl = messagesJSONArray.getJSONObject(i).getString("message_voice_url");
                        final String voiceName = messagesJSONArray.getJSONObject(i).getString("message");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new HttpUtil(context).getSoundFile(messageVoiceUrl, voiceName);
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                TmpFileUtil.writeJSONToFile(messagesJSONArray.toString(), ActivityUtil.TMP_MESSAGE_FILE_PATH, "message.json");
            } catch (Exception e) {
                Toast.makeText(context, "网络异常", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            super.handleMessage(message);
        }
    };
    private EditText search_EditText;
    private ProgressDialog progressDialog;
    private final Handler searchFriendHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Toast.makeText(context, "当前查找的用户是自己！", Toast.LENGTH_LONG).show();
            } else {
                try {
                    String friendJSON = (String) message.obj;
                    JSONObject jsonObject = new JSONObject(friendJSON);
                    if (jsonObject.getString("code").equals("1")) {
                        Intent intent = new Intent(context, FriendShowActivity.class);
                        intent.putExtra("friendJSON", jsonObject.getString("message"));
                        startActivity(intent);
                    }
                    Toast.makeText(context, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(context, "网络异常", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            progressDialog.dismiss();
            super.handleMessage(message);
        }
    };
    private View title_search_bar_include;
    private LinearLayout title_search_LinearLayout;
    private SwipeRefreshLayout friend_SwipeRefreshLayout;
    private ImageView title_left_ImageView;
    private TextView top_title_TextView;
    private List<JSONObject> userJSONObjectList;
    private RecyclerView friend_RecyclerView;
    private FriendRecyclerViewAdapter friendRecyclerViewAdapter;
    private List<JSONObject> clickUserList;
    private final Handler friendShowHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("message");
                    TmpFileUtil.writeJSONToFile(jsonArray.toString(), ActivityUtil.TMP_FRIEND_FILE_PATH, "friend.json");
                    userJSONObjectList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        final JSONObject user = jsonArray.getJSONObject(i);
                        userJSONObjectList.add(user);
                        final String photo = user.getString("photo");
                        String[] photos = photo.split("/");
                        final String tmpBitmapFileName = photos[photos.length - 1] + ".cache";
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = new HttpUtil(context).getImageBitmap(photo);
                                ImageUtil.saveBitmapToTmpFile(bitmap, ActivityUtil.TMP_FRIEND_FILE_PATH, tmpBitmapFileName);
                            }
                        }).start();
                    }
                    Toast.makeText(context, "刷新成功！", Toast.LENGTH_LONG).show();
                    friend_SwipeRefreshLayout.setRefreshing(false);
                }
            } catch (JSONException e) {
                Toast.makeText(context, "刷新失败，网络异常！", Toast.LENGTH_LONG).show();
                friend_SwipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "获取数据失败！");
                e.printStackTrace();
            }
            initFriendRecyclerView();
            friend_SwipeRefreshLayout.setRefreshing(false);
            super.handleMessage(message);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activity = getActivity();
        context = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        friendFragmentView = layoutInflater.inflate(R.layout.fragment_friend, viewGroup, false);
        initFragmentView();
        return friendFragmentView;
    }

    private void initFragmentView() {
        ActivityUtil.setLinearLayoutBackground(friendFragmentView.findViewById(R.id.friend_fragment_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        progressDialog = new ProgressDialog(context);
        title_left_ImageView = friendFragmentView.findViewById(R.id.title_left_ImageView);
        top_title_TextView = friendFragmentView.findViewById(R.id.top_title_TextView);
        ImageView title_right_ImageView = friendFragmentView.findViewById(R.id.title_right_ImageView);
        title_right_ImageView.setImageResource(R.drawable.search_button);
        title_right_ImageView.setOnClickListener(this);
        Button search_Button = friendFragmentView.findViewById(R.id.search_Button);
        search_Button.setOnClickListener(this);
        title_search_bar_include = friendFragmentView.findViewById(R.id.title_search_bar_include);
        title_search_LinearLayout = friendFragmentView.findViewById(R.id.title_search_LinearLayout);
        friend_SwipeRefreshLayout = friendFragmentView.findViewById(R.id.friend_SwipeRefreshLayout);
        friend_SwipeRefreshLayout.setOnRefreshListener(this);
        friend_RecyclerView = friendFragmentView.findViewById(R.id.friend_RecyclerView);
        LinearLayout fragment_friend_LinearLayout = friendFragmentView.findViewById(R.id.fragment_friend_LinearLayout);
        fragment_friend_LinearLayout.setOnClickListener(this);
        initTitleView();
        initKeyboardStateObserver();
        initFriendRecyclerView();
    }

    private void initTitleView() {
        friend_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        top_title_TextView.setText("好友");
        Bitmap bitmap = ImageUtil.getBitmapFromFile(ActivityUtil.TMP_USER_FILE_PATH, "photo.png.cache");
        if (bitmap != null) {
            Log.e(TAG, "头像图片加载正常");
            title_left_ImageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "图片加载失败,图片为空");
            Toast.makeText(context, "登录信息失效，请重新登录！", Toast.LENGTH_SHORT).show();
            title_left_ImageView.setImageResource(R.drawable.user_default_photo);
            SharedPreferencesUtils.putBoolean(context, "status", false, "user");
            startActivity(new Intent(context, LoginActivity.class));
            activity.finish();
        }
    }

    private void initKeyboardStateObserver() {
        KeyboardStateObserver.getKeyboardStateObserver(activity).
                setKeyboardVisibilityListener(new KeyboardStateObserver.OnKeyboardVisibilityListener() {
                    @Override
                    public void onKeyboardShow() {
                        Log.e(TAG, "键盘弹出");
                        title_search_bar_include.setVisibility(View.GONE);
                        title_search_LinearLayout.setVisibility(View.VISIBLE);
                        friend_SwipeRefreshLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onKeyboardHide() {
                        Log.e(TAG, "键盘收回");
                        title_search_bar_include.setVisibility(View.VISIBLE);
                        title_search_LinearLayout.setVisibility(View.GONE);
                        friend_SwipeRefreshLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onRefresh() {
        if (friendRecyclerViewAdapter != null) {
            friendRecyclerViewAdapter.setOnItemClickListener(null);
            Toast.makeText(context, "刷新中，请稍后......", Toast.LENGTH_LONG).show();
        }
        initNetFriendRecyclerView();
    }

    private void initNetFriendRecyclerView() {
        friend_SwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> parameter = new HashMap<>();
                parameter.put("id", SharedPreferencesUtils.getString(context, "id", "0", "user"));
                Message message = new Message();
                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/query_all_user", parameter);
                friendShowHandler.sendMessage(message);
            }
        }).start();
    }

    private void showSearchFriend() {
        title_search_bar_include.setVisibility(View.GONE);
        title_search_LinearLayout.setVisibility(View.VISIBLE);
        search_EditText = friendFragmentView.findViewById(R.id.search_EditText);
        search_EditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                search_EditText.setSelection(search_EditText.getText().length());
                search_EditText.requestFocus();
                InputMethodManager inputMethodManager = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
                inputMethodManager.showSoftInput(activity.getCurrentFocus(), 0);
            }
        }, 100);

        search_EditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchFriend();
                    return true;
                }
                return false;
            }
        });
    }

    private void searchFriend() {
        ((InputMethodManager) search_EditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).
                hideSoftInputFromWindow(Objects.requireNonNull
                        (activity.getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        final String phone = search_EditText.getText().toString();
        if (ActivityUtil.isMobileNO(phone)) {
            Window window = progressDialog.getWindow();
            if (window != null) {
                progressDialog.show();
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER;
                progressDialog.setCancelable(false);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                progressDialog.setContentView(R.layout.loading_progress_bar);
                TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                prompt_TextView.setText("查找中.......");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("phone", phone);
                    Message message = new Message();
                    if (phone.equals(SharedPreferencesUtils.getString(context, "phone", "", "user"))) {
                        message.what = 1;
                    } else {
                        message.what = 0;
                    }
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/query_user_by_phone", parameter);
                    searchFriendHandler.sendMessage(message);
                }
            }).start();
        } else {
            Toast.makeText(context, "请输入正确的手机号！", Toast.LENGTH_LONG).show();
        }
        search_EditText.setText("");
        title_search_bar_include.setVisibility(View.VISIBLE);
        title_search_LinearLayout.setVisibility(View.GONE);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_right_ImageView:
                showSearchFriend();
                break;
            case R.id.search_Button:
                searchFriend();
                break;
            case R.id.fragment_friend_LinearLayout:
                hideFragmentFriendLinearLayout();
                break;
            default:
                break;
        }
    }

    private void hideFragmentFriendLinearLayout() {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (search_EditText != null) {
            inputMethodManager.hideSoftInputFromWindow(search_EditText.getWindowToken(), 0);
        }
    }

    private void initFriendRecyclerView() {
        try {
            JSONArray jsonArray = new JSONArray(TmpFileUtil.getJSONFileString(ActivityUtil.TMP_FRIEND_FILE_PATH, "friend.json"));
            userJSONObjectList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject user = jsonArray.getJSONObject(i);
                userJSONObjectList.add(user);
            }
        } catch (JSONException e) {
            Log.e(TAG, "文件读取失败，本地没有好友数据");
        }
        if (userJSONObjectList != null) {
            try {
                clickUserList = new ArrayList<>();
                String clickUserJSON = TmpFileUtil.getJSONFileString(ActivityUtil.TMP_CHAT_FILE_PATH, "chat.json");
                JSONArray jsonArray = new JSONArray(clickUserJSON);
                for (int i = 0; i < jsonArray.length(); i++) {
                    clickUserList.add(jsonArray.getJSONObject(i));
                }
            } catch (JSONException e) {
                Log.e(TAG, "文件读取失败，未点击过好友");
            }
            friendRecyclerViewAdapter = new FriendRecyclerViewAdapter(userJSONObjectList);
            friend_RecyclerView.setLayoutManager(new LinearLayoutManager(context));
            friendRecyclerViewAdapter.setOnItemClickListener(new FriendRecyclerViewAdapter.FriendRecyclerViewAdapterOnItemClickListener() {
                @Override
                public void onItemClick(final int position) {
                    Log.e(TAG, "第" + position + "个被短时的点击：" + userJSONObjectList.get(position));
                    Intent intent = new Intent(context, FriendShowActivity.class);
                    intent.putExtra("friendJSON", userJSONObjectList.get(position).toString());
                    if (clickUserList.size() == 0) {
                        clickUserList.add(userJSONObjectList.get(position));
                    } else {
                        String clickUserJSON = TmpFileUtil.getJSONFileString(ActivityUtil.TMP_CHAT_FILE_PATH, "chat.json");
                        if (!clickUserJSON.contains(userJSONObjectList.get(position).toString())) {
                            clickUserList.add(userJSONObjectList.get(position));
                        }
                    }
                    TmpFileUtil.writeJSONToFile(clickUserList.toString(), ActivityUtil.TMP_CHAT_FILE_PATH, "chat.json");
                    startActivity(intent);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, String> parameter = new HashMap<>();
                            parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                            try {
                                parameter.put("friend_id", userJSONObjectList.get(position).getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Message message = new Message();
                            message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_messages", parameter);
                            getMessageHandler.sendMessage(message);
                        }
                    }).start();
                }
            });

            friendRecyclerViewAdapter.setOnItemLongClickListener(new FriendRecyclerViewAdapter.FriendRecyclerViewAdapterOnItemLongClickListener() {
                @Override
                public void onItemLongClick(int position) {
                    Log.e(TAG, "第" + position + "个被长按：" + userJSONObjectList.get(position));
                }
            });
            friend_RecyclerView.setAdapter(friendRecyclerViewAdapter);
        } else {
            Log.e(TAG, "列表为空");
            Toast.makeText(context, "没有查询到好友数据！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        initFragmentView();
        super.onHiddenChanged(hidden);
    }
}