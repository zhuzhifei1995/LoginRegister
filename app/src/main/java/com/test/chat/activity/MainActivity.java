package com.test.chat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.chat.R;
import com.test.chat.adapter.FriendRecyclerViewAdapter;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;
import com.test.chat.util.ActivityUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends Activity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ActivityUtil.TAG;
    private static final int BACK_PRESSED_INTERVAL = 2000;
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static long CURRENT_BACK_PRESSED_TIME = 0;
    private static boolean IS_UPDATE_MY_VIEW;
    private static boolean IS_SHOW_MY_MESSAGE = false;
    private static String IMAGE_FILE_NAME = "photo.png.cache";
    private static String TMP_PHOTO_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/update";
    private ImageView dynamic_ImageView;
    private TextView dynamic_TextView;
    private ImageView my_ImageView;
    private TextView my_TextView;
    private ImageView friend_ImageView;
    private TextView friend_TextView;
    private ImageView message_ImageView;
    private TextView message_TextView;
    private ImageView photo_my_ImageView;
    private ProgressDialog progressDialog;
    private List<JSONObject> userJSONObjectList;
    private EditText search_EditText;
    private RecyclerView friend_RecyclerView;
    private FriendRecyclerViewAdapter friendRecyclerViewAdapter;
    private SwipeRefreshLayout friend_SwipeRefreshLayout;
    private SwipeRefreshLayout my_SwipeRefreshLayout;
    private ImageView title_right_ImageView;
    private Button search_Button;
    private List<JSONObject> clickUserList;

    private Handler uploadUpdatePhotoHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    photo_my_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME));
                    Toast.makeText(MainActivity.this, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                    TmpFileUtil.copyFile(new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME), new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache"));
                } else {
                    Toast.makeText(MainActivity.this, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                    SharedPreferencesUtils.removeKey(MainActivity.this, "status", "user");
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "网络异常！", Toast.LENGTH_LONG).show();
                photo_my_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(Environment.getExternalStorageDirectory().getPath()
                        + "/tmp/user", "photo.png.cache"));
                e.printStackTrace();
            }
            super.handleMessage(message);
            progressDialog.dismiss();
        }
    };
    private Handler getMessageHandler = new Handler(Looper.getMainLooper()) {
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
                                Bitmap photoBitmap = new HttpUtil(MainActivity.this).getImageBitmap(messageImageUrl);
                                if (photoBitmap != null) {
                                    ImageUtil.saveBitmapToTmpFile(photoBitmap,Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image", imageName + ".cache");
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
                                    new HttpUtil(MainActivity.this).getSoundFile(messageVoiceUrl, voiceName);
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                TmpFileUtil.writeJSONToFile(messagesJSONArray.toString(), Environment.getExternalStorageDirectory().getPath() + "/tmp/message", "message.json");
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            super.handleMessage(message);
        }
    };
    private Handler friendShowHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            String json = (String) message.obj;
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    Toast.makeText(MainActivity.this, "刷新成功！", Toast.LENGTH_SHORT).show();
                    JSONArray jsonArray = jsonObject.getJSONArray("message");
                    TmpFileUtil.writeJSONToFile(jsonArray.toString(), Environment.getExternalStorageDirectory().getPath() + "/tmp/friend", "friend.json");
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
                                Bitmap bitmap = new HttpUtil(MainActivity.this).getImageBitmap(photo);
                                ImageUtil.saveBitmapToTmpFile(bitmap, Environment.getExternalStorageDirectory().getPath() + "/tmp/friend", tmpBitmapFileName);
                            }
                        }).start();
                    }
                }
            } catch (JSONException e) {
                try {
                    JSONArray jsonArray = new JSONArray(TmpFileUtil.getJSONFileString(Environment.getExternalStorageDirectory().getPath() + "/tmp/friend", "friend.json"));
                    userJSONObjectList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        final JSONObject user = jsonArray.getJSONObject(i);
                        userJSONObjectList.add(user);
                    }
                } catch (JSONException e1) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                e.printStackTrace();
            }
            if (userJSONObjectList != null) {
                initFriendRecyclerView();
            }
            friend_SwipeRefreshLayout.setRefreshing(false);
            title_right_ImageView.setOnClickListener(MainActivity.this);
            friend_RecyclerView.setVisibility(View.VISIBLE);
            super.handleMessage(message);
        }
    };
    private Handler friendSwipeRefreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message message) {
            friend_SwipeRefreshLayout.setRefreshing(false);
            super.handleMessage(message);
        }
    };
    private Handler mySwipeRefreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            try {
                String json = (String) message.obj;
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    Toast.makeText(MainActivity.this, "刷新成功！", Toast.LENGTH_LONG).show();
                    JSONObject userJSONObject = jsonObject.getJSONObject("message");
                    SharedPreferencesUtils.putString(MainActivity.this, "create_time", userJSONObject.getString("create_time"), "user");
                    SharedPreferencesUtils.putString(MainActivity.this, "password", userJSONObject.getString("password"), "user");
                    SharedPreferencesUtils.putString(MainActivity.this, "login_number", userJSONObject.getString("login_number"), "user");
                    SharedPreferencesUtils.putString(MainActivity.this, "nick_name", userJSONObject.getString("nick_name"), "user");
                    SharedPreferencesUtils.putString(MainActivity.this, "phone", userJSONObject.getString("phone"), "user");
                    SharedPreferencesUtils.putString(MainActivity.this, "photo", userJSONObject.getString("photo_url"), "user");
                    saveUserPhoto(userJSONObject.getString("photo_url"));
                } else {
                    Toast.makeText(MainActivity.this, "刷新失败！", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "刷新失败！", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            my_SwipeRefreshLayout.setRefreshing(false);
            initMyView();
            super.handleMessage(message);
        }
    };
    private Handler searchFriendHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 1) {
                initMyView();
                Toast.makeText(MainActivity.this, "当前查找的用户是自己！", Toast.LENGTH_LONG).show();
            } else {
                try {
                    String friendJSON = (String) message.obj;
                    JSONObject jsonObject = new JSONObject(friendJSON);
                    if (jsonObject.getString("code").equals("1")) {
                        Intent intent = new Intent(MainActivity.this, FriendShowActivity.class);
                        intent.putExtra("friendJSON", jsonObject.getString("message"));
                        startActivity(intent);
                    }
                    Toast.makeText(MainActivity.this, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            progressDialog.dismiss();
            super.handleMessage(message);
        }
    };

    private void initFriendRecyclerView() {
        try {
            clickUserList = new ArrayList<>();
            String clickUserJSON = TmpFileUtil.getJSONFileString(Environment.getExternalStorageDirectory().getPath() + "/tmp/message", "chat.json");
            JSONArray jsonArray = new JSONArray(clickUserJSON);
            for (int i = 0; i < jsonArray.length(); i++) {
                clickUserList.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "文件读取失败，未点击过好友："+"/tmp/message/chat.json");
        }
        friendRecyclerViewAdapter = new FriendRecyclerViewAdapter(userJSONObjectList);
        friend_RecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        friendRecyclerViewAdapter.setOnItemClickListener(new FriendRecyclerViewAdapter.FriendRecyclerViewAdapterOnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                Log.e(TAG, "第" + position + "个被短时的点击：" + userJSONObjectList.get(position));
                Intent intent = new Intent(MainActivity.this, FriendShowActivity.class);
                intent.putExtra("friendJSON", userJSONObjectList.get(position).toString());
                if (clickUserList.size() == 0) {
                    clickUserList.add(userJSONObjectList.get(position));
                } else {
                    String clickUserJSON = TmpFileUtil.getJSONFileString(Environment.getExternalStorageDirectory().getPath() + "/tmp/message", "chat.json");
                    if (!clickUserJSON.contains(userJSONObjectList.get(position).toString())) {
                        clickUserList.add(userJSONObjectList.get(position));
                    }
                }
                TmpFileUtil.writeJSONToFile(clickUserList.toString(), Environment.getExternalStorageDirectory().getPath() + "/tmp/message", "chat.json");
                startActivity(intent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("user_id", SharedPreferencesUtils.getString(MainActivity.this, "id", "", "user"));
                        try {
                            parameter.put("friend_id", userJSONObjectList.get(position).getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Message message = new Message();
                        message.obj = new HttpUtil(MainActivity.this).postRequest(ActivityUtil.NET_URL + "/get_messages", parameter);
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
        progressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(MainActivity.this);
        IS_UPDATE_MY_VIEW = true;
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        if (status != null && !status.equals("")) {
            Toast.makeText(MainActivity.this, status, Toast.LENGTH_LONG).show();
            Window window = progressDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                progressDialog.show();
                progressDialog.setCancelable(false);
                progressDialog.setContentView(R.layout.loading_progress_bar);
                TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                prompt_TextView.setText("当前用户信息同步中......");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        progressDialog.dismiss();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
        setContentView(R.layout.activity_main);
        initView();
        initMessageView();
    }

    private void initView() {
        LinearLayout dynamic_LinearLayout = findViewById(R.id.dynamic_LinearLayout);
        dynamic_LinearLayout.setOnClickListener(this);

        LinearLayout my_LinearLayout = findViewById(R.id.my_LinearLayout);
        my_LinearLayout.setOnClickListener(this);

        LinearLayout friend_LinearLayout = findViewById(R.id.friend_LinearLayout);
        friend_LinearLayout.setOnClickListener(this);

        LinearLayout message_LinearLayout = findViewById(R.id.message_LinearLayout);
        message_LinearLayout.setOnClickListener(this);

        dynamic_ImageView = findViewById(R.id.dynamic_ImageView);
        dynamic_ImageView.setOnClickListener(this);
        dynamic_TextView = findViewById(R.id.dynamic_TextView);
        dynamic_TextView.setOnClickListener(this);

        my_ImageView = findViewById(R.id.my_ImageView);
        my_ImageView.setOnClickListener(this);
        my_TextView = findViewById(R.id.my_TextView);
        my_TextView.setOnClickListener(this);

        friend_ImageView = findViewById(R.id.friend_ImageView);
        friend_ImageView.setOnClickListener(this);
        friend_TextView = findViewById(R.id.friend_TextView);
        friend_TextView.setOnClickListener(this);

        message_ImageView = findViewById(R.id.message_ImageView);
        message_ImageView.setOnClickListener(this);
        message_TextView = findViewById(R.id.message_TextView);
        message_TextView.setOnClickListener(this);
    }

    private void initDynamicView() {
        setContentView(R.layout.activity_dynamic);
        initView();

        message_ImageView.setImageResource(R.drawable.message_normal);
        friend_ImageView.setImageResource(R.drawable.friend_normal);
        dynamic_ImageView.setImageResource(R.drawable.dynamic_selected);
        my_ImageView.setImageResource(R.drawable.my_normal);
        message_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        friend_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        my_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        dynamic_TextView.setTextColor(getResources().getColor(R.color.main_select, null));
    }

    private void saveUserPhoto(final String photo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap photoBitmap = new HttpUtil(MainActivity.this).getImageBitmap(photo);
                if (photoBitmap != null) {
                    ImageUtil.saveBitmapToTmpFile(photoBitmap,Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void initMyView() {
        IS_SHOW_MY_MESSAGE = false;
        setContentView(R.layout.activity_my);
        initView();
        message_ImageView.setImageResource(R.drawable.message_normal);
        friend_ImageView.setImageResource(R.drawable.friend_normal);
        dynamic_ImageView.setImageResource(R.drawable.dynamic_normal);
        my_ImageView.setImageResource(R.drawable.my_selected);
        message_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        friend_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        my_TextView.setTextColor(getResources().getColor(R.color.main_select, null));
        dynamic_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("我的");
        Button title_right_Button = findViewById(R.id.title_right_Button);
        title_right_Button.setText("");
        Button title_left_Button = findViewById(R.id.title_left_Button);
        title_left_Button.setText("");
        TextView login_out_TextView = findViewById(R.id.login_out_TextView);
        login_out_TextView.setOnClickListener(this);
        photo_my_ImageView = findViewById(R.id.photo_my_ImageView);
        photo_my_ImageView.setOnClickListener(this);
        LinearLayout my_message_LinearLayout = findViewById(R.id.my_message_LinearLayout);
        my_message_LinearLayout.setOnClickListener(this);

        my_SwipeRefreshLayout = findViewById(R.id.my_SwipeRefreshLayout);
        my_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        my_SwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        my_SwipeRefreshLayout.setRefreshing(true);
                        String id = SharedPreferencesUtils.getString(MainActivity.this, "id", "", "user");
                        Map<String, String> parameter = new HashMap<>();
                        parameter.put("id", id);
                        Message message = new Message();
                        message.obj = new HttpUtil(MainActivity.this).postRequest(ActivityUtil.NET_URL + "/query_user_by_id", parameter);
                        mySwipeRefreshHandler.sendMessage(message);
                    }
                }).start();
            }
        });

        Bitmap bitmap = ImageUtil.getBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache");
        if (bitmap != null) {
            Log.e(TAG, "图片加载正常");
            photo_my_ImageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "图片为空,加载失败");
            photo_my_ImageView.setImageResource(R.drawable.user_default_photo);
        }
        TextView nike_name_TextView = findViewById(R.id.nike_name_TextView);
        String nick_name = SharedPreferencesUtils.getString(MainActivity.this, "nick_name", "", "user");
        if (!nick_name.equals("")) {
            nike_name_TextView.setText(nick_name);
        } else {
            nike_name_TextView.setText("未设置昵称");
        }
        TextView login_number_TextView = findViewById(R.id.login_number_TextView);
        String login_number = SharedPreferencesUtils.getString(MainActivity.this, "login_number", "", "user");
        login_number_TextView.setText(login_number);
        TextView phone_number_TextView = findViewById(R.id.phone_number_TextView);
        String phone = SharedPreferencesUtils.getString(MainActivity.this, "phone", "", "user");
        phone_number_TextView.setText(phone);
        TextView create_time_TextView = findViewById(R.id.create_time_TextView);
        String create_time = SharedPreferencesUtils.getString(MainActivity.this, "create_time", "", "user");
        create_time_TextView.setText(create_time);
        TextView password_TextView = findViewById(R.id.password_TextView);
        String password = SharedPreferencesUtils.getString(MainActivity.this, "password", "", "user");
        password_TextView.setText(password);
    }

    private void initMessageView() {
        setContentView(R.layout.activity_main);
        initView();
        message_ImageView.setImageResource(R.drawable.message_selected);
        friend_ImageView.setImageResource(R.drawable.friend_normal);
        dynamic_ImageView.setImageResource(R.drawable.dynamic_normal);
        my_ImageView.setImageResource(R.drawable.my_normal);
        message_TextView.setTextColor(getResources().getColor(R.color.main_select, null));
        friend_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        my_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        dynamic_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("消息");
        Button title_left_Button = findViewById(R.id.title_left_Button);
        title_left_Button.setText("");
        Button title_right_Button = findViewById(R.id.title_right_Button);
        title_right_Button.setText("");

        final List<JSONObject> chatJSONObjectList = new ArrayList<>();
        String json = TmpFileUtil.getJSONFileString(Environment.getExternalStorageDirectory().getPath()
                + "/tmp/message", "chat.json");
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                chatJSONObjectList.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView no_chat_TextView = findViewById(R.id.no_chat_TextView);
        if (chatJSONObjectList.size() == 0) {
            no_chat_TextView.setVisibility(View.VISIBLE);
        } else {
            no_chat_TextView.setVisibility(View.GONE);
            Collections.reverse(chatJSONObjectList);
            RecyclerView chat_RecyclerView = findViewById(R.id.chat_RecyclerView);
            FriendRecyclerViewAdapter chatRecyclerViewAdapter = new FriendRecyclerViewAdapter(chatJSONObjectList);
            chatRecyclerViewAdapter.setOnItemClickListener(new FriendRecyclerViewAdapter.FriendRecyclerViewAdapterOnItemClickListener() {
                @Override
                public void onItemClick(final int position) {
                    Log.e(TAG, "第" + position + "个被短时的点击：" + chatJSONObjectList.get(position).toString());
                    Intent intent = new Intent(MainActivity.this, FriendShowActivity.class);
                    intent.putExtra("friendJSON", chatJSONObjectList.get(position).toString());
                    startActivity(intent);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, String> parameter = new HashMap<>();
                            parameter.put("user_id", SharedPreferencesUtils.getString(MainActivity.this, "id", "", "user"));
                            try {
                                parameter.put("friend_id", chatJSONObjectList.get(position).getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Message message = new Message();
                            message.obj = new HttpUtil(MainActivity.this).postRequest(ActivityUtil.NET_URL + "/get_messages", parameter);
                            getMessageHandler.sendMessage(message);
                        }
                    }).start();
                }
            });
            chat_RecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            chat_RecyclerView.setAdapter(chatRecyclerViewAdapter);
        }
    }

    private void getNetFriendRecyclerView() {
        Map<String, String> parameter = new HashMap<>();
        parameter.put("id", SharedPreferencesUtils.getString(MainActivity.this, "id", "0", "user"));
        Message message = new Message();
        message.obj = new HttpUtil(MainActivity.this).postRequest(ActivityUtil.NET_URL + "/query_all_user", parameter);
        friendShowHandler.sendMessage(message);
    }

    private void initFriendView() {
        setContentView(R.layout.activity_friend);
        initView();
        message_ImageView.setImageResource(R.drawable.message_normal);
        friend_ImageView.setImageResource(R.drawable.friend_selected);
        dynamic_ImageView.setImageResource(R.drawable.dynamic_normal);
        my_ImageView.setImageResource(R.drawable.my_normal);
        message_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        friend_TextView.setTextColor(getResources().getColor(R.color.main_select, null));
        my_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        dynamic_TextView.setTextColor(getResources().getColor(R.color.main_normal, null));
        friend_RecyclerView = findViewById(R.id.friend_RecyclerView);
        friend_SwipeRefreshLayout = findViewById(R.id.friend_SwipeRefreshLayout);
        friend_SwipeRefreshLayout.setOnRefreshListener(this);
        friend_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        search_EditText = findViewById(R.id.search_EditText);
        title_right_ImageView = findViewById(R.id.title_right_ImageView);
        title_right_ImageView.setImageResource(R.drawable.search_button);
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(this);
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("好友");
        Bitmap bitmap = ImageUtil.getBitmapFromFile(Environment.getExternalStorageDirectory().getPath()
                + "/tmp/user", "photo.png.cache");
        if (bitmap != null) {
            Log.e(TAG, "图片加载正常");
            title_left_ImageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "图片加载失败,图片为空");
            title_left_ImageView.setImageResource(R.drawable.user_default_photo);
        }
        if (IS_UPDATE_MY_VIEW) {
            onRefresh();
            IS_UPDATE_MY_VIEW = false;
            friend_SwipeRefreshLayout.setRefreshing(false);
            Window window = progressDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER;
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                progressDialog.show();
                progressDialog.setCancelable(false);
                progressDialog.setContentView(R.layout.loading_progress_bar);
                TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                prompt_TextView.setText("获取好友列表中.......");
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getKeyCode() == 4) {
                            progressDialog.dismiss();
                        }
                        return false;
                    }
                });
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getNetFriendRecyclerView();
                }
            }).start();
        } else {
            title_right_ImageView.setOnClickListener(MainActivity.this);
            try {
                String json = TmpFileUtil.getJSONFileString(Environment.getExternalStorageDirectory().getPath()
                        + "/tmp/friend", "friend.json");
                JSONArray jsonArray = new JSONArray(json);
                userJSONObjectList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject user = jsonArray.getJSONObject(i);
                    userJSONObjectList.add(user);
                }
                if (userJSONObjectList != null) {
                    initFriendRecyclerView();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final LinearLayout root_LinearLayout = findViewById(R.id.root_LinearLayout);
        root_LinearLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > 0)) {
                    Log.e(TAG, "键盘被收起");
                    View title_search_bar_include = findViewById(R.id.title_search_bar_include);
                    title_search_bar_include.setVisibility(View.VISIBLE);
                    LinearLayout title_search_LinearLayout = findViewById(R.id.title_search_LinearLayout);
                    title_search_LinearLayout.setVisibility(View.GONE);
                    View foot_select_View = findViewById(R.id.foot_select_View);
                    foot_select_View.setVisibility(View.VISIBLE);
                    friend_RecyclerView.setVisibility(View.VISIBLE);
                } else if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > 0)) {
                    Log.e(TAG, "键盘被弹起");
                    View foot_select_View = findViewById(R.id.foot_select_View);
                    foot_select_View.setVisibility(View.GONE);
                    friend_RecyclerView.setVisibility(View.GONE);
                    root_LinearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.e(TAG, "键盘已经失去焦点");
                            search_EditText.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    search_EditText.setSelection(search_EditText.getText().length());
                                    search_EditText.requestFocus();
                                    InputMethodManager manager = ((InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                                    if (manager != null) {
                                        manager.showSoftInput(getCurrentFocus(), 0);
                                    }
                                }
                            }, 100);
                        }
                    });
                }
            }
        });
        search_Button = findViewById(R.id.search_Button);
        search_Button.setOnClickListener(this);
    }

    private void loginOut() {
        final Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            progressDialog.setCancelable(true);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.login_out_progress_bar);
        }
        TextView login_out_confirm_TextView = progressDialog.findViewById(R.id.login_out_confirm_TextView);
        login_out_confirm_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    progressDialog.setCancelable(true);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.loading_progress_bar);
                    TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                    prompt_TextView.setText("退出登陆中.......");
                }
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                SharedPreferencesUtils.removeKey(MainActivity.this, "status", "user");
                File userPhotoFile = new File(Environment.getExternalStorageDirectory().getPath()
                        + "/tmp/user", "photo.png.cache");
                if (userPhotoFile.delete()) {
                    Log.e(TAG, "临时头像图片删除成功："+userPhotoFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "无临时头像文件图片："+userPhotoFile.getAbsolutePath());
                }
                TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/friend"));
                TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/message"));
                TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/friend"));
                TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image"));
                TmpFileUtil.deleteFileCache(new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/voice"));
                startActivity(intent);
                finish();
            }
        });
        TextView login_out_cancel_TextView = progressDialog.findViewById(R.id.login_out_cancel_TextView);
        login_out_cancel_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
            }
        });
    }

    private void updateMyPhoto() {
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            progressDialog.setCancelable(true);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.photo_view_progress_bar);
        }
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == 4) {
                    dialogInterface.dismiss();
                }
                return false;
            }
        });
        TextView photo_show_TextView = progressDialog.findViewById(R.id.photo_show_TextView);
        photo_show_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMyPhoto(progressDialog);
            }
        });
        TextView album_TextView = progressDialog.findViewById(R.id.album_TextView);
        album_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                selectFromPhoto();
            }
        });
        TextView photo_TextView = progressDialog.findViewById(R.id.photo_TextView);
        photo_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                selectFromAlbum();
            }
        });
        TextView cancel_TextView = progressDialog.findViewById(R.id.cancel_TextView);
        cancel_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
            }
        });
    }

    private void showMyPhoto(DialogInterface dialog) {
        dialog.dismiss();
        Intent intent = new Intent(MainActivity.this, PhotoShowActivity.class);
        intent.putExtra("flag", 1);
        startActivity(intent);
    }

    private void selectFromPhoto() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                Toast.makeText(MainActivity.this, "未找到图片查看器", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void selectFromAlbum() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
        } else {
            photoFromCapture();
        }
    }

    private void photoFromCapture() {
        Intent intent;
        Uri pictureUri;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(TMP_PHOTO_FILE_PATH);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败："+dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功："+dirFile.getAbsolutePath());
                }
            }
        }
        File pictureFile = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileProvider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void startSmallPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING);
    }

    private void setSmallImageToImageView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photoBitmap = extras.getParcelable("data");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dirFile = new File(TMP_PHOTO_FILE_PATH);
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.e(TAG, "文件夹创建失败："+dirFile.getAbsolutePath());
                    } else {
                        Log.e(TAG, "文件夹创建成功："+dirFile.getAbsolutePath());
                    }
                }
                File file = new File(dirFile, IMAGE_FILE_NAME);
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(file);
                    if (photoBitmap != null) {
                        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } else {
                        Log.e(TAG, "图片文件不存在！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            photo_my_ImageView.setImageBitmap(photoBitmap);
            uploadUpdatePhoto();
        }
    }

    private void uploadUpdatePhoto() {
        progressDialog.setCancelable(false);
        Window window = progressDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            progressDialog.setCancelable(false);
            progressDialog.show();
            progressDialog.setContentView(R.layout.loading_progress_bar);
            TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
            prompt_TextView.setText("上传更新头像中......");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("id", SharedPreferencesUtils.getString(MainActivity.this,
                            "id", "0", "user"));
                    File updatePhotoFile = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
                    Message message = new Message();
                    message.obj = new HttpUtil(MainActivity.this).upLoadImageFile(updatePhotoFile,
                            ActivityUtil.NET_URL + "/update_user_photo", parameter);
                    uploadUpdatePhotoHandler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startBigPhotoZoom(Uri uri) {
        File file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(TMP_PHOTO_FILE_PATH);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败："+dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功："+dirFile.getAbsolutePath());
                }
            }
            file = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(FileProvider.getUriForFile(MainActivity.this,
                    getPackageName() + ".fileProvider", file), "image/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 500);
            intent.putExtra("outputY", 500);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
        } else {
            Toast.makeText(MainActivity.this, "剪切图片失败", Toast.LENGTH_LONG).show();
        }
    }

    private void setBigImageToImageView() {
        File photoFile = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
        Uri tempPhotoUri = Uri.fromFile(photoFile);
        try {
            Bitmap photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(tempPhotoUri));
            photo_my_ImageView.setImageBitmap(photoBitmap);
            uploadUpdatePhoto();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showMyMessage() {
        ImageView message_show_ImageView = findViewById(R.id.message_show_ImageView);
        LinearLayout my_show_message_LinearLayout = findViewById(R.id.my_show_message_LinearLayout);
        View line_message_View = findViewById(R.id.line_message_View);
        if (IS_SHOW_MY_MESSAGE) {
            message_show_ImageView.setImageResource(R.drawable.message_no_show);
            my_show_message_LinearLayout.setVisibility(View.GONE);
            line_message_View.setVisibility(View.GONE);
            IS_SHOW_MY_MESSAGE = false;
        } else {
            message_show_ImageView.setImageResource(R.drawable.message_show);
            my_show_message_LinearLayout.setVisibility(View.VISIBLE);
            line_message_View.setVisibility(View.VISIBLE);
            IS_SHOW_MY_MESSAGE = true;
        }
    }

    private void showSearchFriend() {
        View title_search_bar_include = findViewById(R.id.title_search_bar_include);
        title_search_bar_include.setVisibility(View.GONE);
        LinearLayout title_search_LinearLayout = findViewById(R.id.title_search_LinearLayout);
        title_search_LinearLayout.setVisibility(View.VISIBLE);
        search_EditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                search_EditText.setSelection(search_EditText.getText().length());
                search_EditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.showSoftInput(getCurrentFocus(), 0);
                }
            }
        }, 100);

        search_EditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
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
                hideSoftInputFromWindow(Objects.requireNonNull(MainActivity.this.getCurrentFocus()).getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        final String phone = search_EditText.getText().toString();
        if (ActivityUtil.isMobileNO(phone)) {
            progressDialog.show();
            Window window = progressDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER;
                progressDialog.setCancelable(true);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            progressDialog.setContentView(R.layout.loading_progress_bar);
            TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
            prompt_TextView.setText("查找中.......");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("phone", phone);
                    Message message = new Message();
                    if (phone.equals(SharedPreferencesUtils.getString(MainActivity.this,
                            "phone", "", "user"))) {
                        message.what = 1;
                    } else {
                        message.what = 0;
                    }
                    message.obj = new HttpUtil(MainActivity.this).postRequest
                            (ActivityUtil.NET_URL + "/query_user_by_phone", parameter);
                    searchFriendHandler.sendMessage(message);
                }
            }).start();
        } else {
            Toast.makeText(MainActivity.this, "请输入正确的手机号！", Toast.LENGTH_LONG).show();
        }
        search_EditText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SMALL_IMAGE_CUTTING:
                    if (data != null) {
                        setSmallImageToImageView(data);
                    }
                    break;
                case REQUEST_BIG_IMAGE_CUTTING:
                    if (data != null) {
                        setBigImageToImageView();
                    }
                    break;
                case REQUEST_IMAGE_GET:
                    try {
                        if (data != null) {
                            startSmallPhotoZoom(data.getData());
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    File temp = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
                    startBigPhotoZoom(Uri.fromFile(temp));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(MainActivity.this, "未找到图片查看器", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFromCapture();
                }
        }
    }

    @Override
    public void onClick(View view) {
        Log.e(TAG, "主界面的内容被点击：" + view.getId());
        switch (view.getId()) {
            case R.id.dynamic_LinearLayout:
            case R.id.dynamic_ImageView:
            case R.id.dynamic_TextView:
                initDynamicView();
                break;
            case R.id.my_LinearLayout:
            case R.id.my_ImageView:
            case R.id.my_TextView:
            case R.id.title_left_ImageView:
                initMyView();
                break;
            case R.id.friend_LinearLayout:
            case R.id.friend_ImageView:
            case R.id.friend_TextView:
                initFriendView();
                break;
            case R.id.message_LinearLayout:
            case R.id.message_ImageView:
            case R.id.message_TextView:
                initMessageView();
                break;
            case R.id.login_out_TextView:
                loginOut();
                break;
            case R.id.photo_my_ImageView:
                updateMyPhoto();
                break;
            case R.id.my_message_LinearLayout:
                showMyMessage();
                break;
            case R.id.title_right_ImageView:
                showSearchFriend();
                break;
            case R.id.search_Button:
                searchFriend();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - CURRENT_BACK_PRESSED_TIME > BACK_PRESSED_INTERVAL) {
                CURRENT_BACK_PRESSED_TIME = System.currentTimeMillis();
                Toast.makeText(MainActivity.this, "再按一次返回键退出", Toast.LENGTH_LONG).show();
                deletePhotoCacheFile();
                return false;
            }
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void deletePhotoCacheFile() {
        File photoCacheFile = new File(TMP_PHOTO_FILE_PATH, IMAGE_FILE_NAME);
        if (photoCacheFile.delete()) {
            Log.e(TAG, "临时图片删除成功："+photoCacheFile.getAbsolutePath());
        } else {
            Log.e(TAG, "无生成的图片"+photoCacheFile.getAbsolutePath());
        }
    }

    @Override
    protected void onDestroy() {
        TmpFileUtil.writeJSONToFile("{}", Environment.getExternalStorageDirectory().getPath()
                + "/tmp/friend", "friend.json");
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        if (friendRecyclerViewAdapter != null) {
            friendRecyclerViewAdapter.setOnItemClickListener(null);
        }
        if (search_Button != null) {
            search_Button.setOnClickListener(null);
        } else {
            friend_SwipeRefreshLayout.setRefreshing(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                getNetFriendRecyclerView();
                friendSwipeRefreshHandler.sendMessage(new Message());
            }
        }).start();
    }

}

