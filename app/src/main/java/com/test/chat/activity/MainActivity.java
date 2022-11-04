package com.test.chat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.test.chat.R;
import com.test.chat.fragment.AppStoreFragment;
import com.test.chat.fragment.FriendFragment;
import com.test.chat.fragment.MessageFragment;
import com.test.chat.fragment.MyFragment;
import com.test.chat.fragment.NetDiskFragment;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private static final int BACK_PRESSED_INTERVAL = 2000;
    private static long CURRENT_BACK_PRESSED_TIME = 0;
    private ImageButton message_bottom_ImageButton;
    private ImageButton friend_bottom_ImageButton;
    private ImageButton dynamic_bottom_ImageButton;
    private ImageButton my_bottom_ImageButton;
    private ImageButton app_store_bottom_ImageButton;
    private TextView message_bottom_TextView;
    private TextView friend_bottom_TextView;
    private TextView dynamic_bottom_TextView;
    private TextView my_bottom_TextView;
    private TextView app_store_bottom_TextView;
    private MessageFragment messageFragment;
    private FriendFragment friendFragment;
    private NetDiskFragment netDiskFragment;
    private MyFragment myFragment;
    private AppStoreFragment appStoreFragment;
    private List<JSONObject> userJSONObjectList;
    private ProgressDialog progressDialog;
    private Context context;

    private final Handler friendShowHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            if (message.what == 1) {
                try {
                    String json = (String) message.obj;
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
                    }
                    Toast.makeText(context, "初始化数据成功！", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
            super.handleMessage(message);
        }
    };
    private final Handler searchFriendHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Toast.makeText(context, "当前查找的用户是自己！", Toast.LENGTH_LONG).show();
            } else if (message.what == 0) {
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
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
            super.handleMessage(message);
        }
    };
    private String ANDROID_ID;
    private final Handler isLoginHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj).getJSONObject("message");
                    if (!ANDROID_ID.equals(jsonObject.getString("android_id"))) {
                        SharedPreferencesUtils.putBoolean(context, "status", false, "user");
                        Toast.makeText(context, "当前账号在其他设备的登录，登录信息失效，请重新登录！", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(context, LoginActivity.class));
                        finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
            }
            super.handleMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TmpFileUtil.deleteDownloadFileCache(new File(ActivityUtil.TMP_DOWNLOAD_PATH));
        TmpFileUtil.deleteDownloadFileCache(new File(ActivityUtil.TMP_APK_FILE_PATH));
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        context = this;
        ANDROID_ID = android.provider.Settings.System.getString(getContentResolver(), "android_id");
        LinearLayout message_bottom_LinearLayout = findViewById(R.id.message_bottom_LinearLayout);
        LinearLayout friend_bottom_LinearLayout = findViewById(R.id.friend_bottom_LinearLayout);
        LinearLayout dynamic_bottom_LinearLayout = findViewById(R.id.net_disk_bottom_LinearLayout);
        LinearLayout my_bottom_LinearLayout = findViewById(R.id.my_bottom_LinearLayout);
        LinearLayout app_store_bottom_LinearLayout = findViewById(R.id.app_store_bottom_LinearLayout);
        message_bottom_TextView = findViewById(R.id.message_bottom_TextView);
        friend_bottom_TextView = findViewById(R.id.friend_bottom_TextView);
        dynamic_bottom_TextView = findViewById(R.id.net_disk_bottom_TextView);
        my_bottom_TextView = findViewById(R.id.my_bottom_TextView);
        app_store_bottom_TextView = findViewById(R.id.app_store_bottom_TextView);
        message_bottom_LinearLayout.setOnClickListener(this);
        friend_bottom_LinearLayout.setOnClickListener(this);
        dynamic_bottom_LinearLayout.setOnClickListener(this);
        my_bottom_LinearLayout.setOnClickListener(this);
        app_store_bottom_LinearLayout.setOnClickListener(this);
        message_bottom_ImageButton = findViewById(R.id.message_bottom_ImageButton);
        friend_bottom_ImageButton = findViewById(R.id.friend_bottom_ImageButton);
        my_bottom_ImageButton = findViewById(R.id.my_bottom_ImageButton);
        dynamic_bottom_ImageButton = findViewById(R.id.net_disk_bottom_ImageButton);
        app_store_bottom_ImageButton = findViewById(R.id.app_store_bottom_ImageButton);
        setSelect(0);
        initFriendFragmentData();
    }

    private void initFriendFragmentData() {
        progressDialog = new ProgressDialog(context);
        Window window = progressDialog.getWindow();
        if (window != null) {
            progressDialog.show();
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            progressDialog.setCancelable(false);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.loading_progress_bar);
            TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
            prompt_TextView.setText("初始化数据中.......");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> parameter = new HashMap<>();
                parameter.put("id", SharedPreferencesUtils.getString(context, "id", "0", "user"));
                Message message = new Message();
                try {
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/query_all_user", parameter);
                    message.what = 1;
                } catch (IOException e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                friendShowHandler.sendMessage(message);
            }
        }).start();
    }

    private void setSelect(int i) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragment(transaction);
        switch (i) {
            case 0:
                if (messageFragment == null) {
                    messageFragment = new MessageFragment();
                    transaction.add(R.id.content_FrameLayout, messageFragment);
                } else {
                    transaction.show(messageFragment);
                }
                message_bottom_ImageButton.setBackgroundResource(R.drawable.message_selected);
                message_bottom_TextView.setTextColor(Color.BLUE);
                break;
            case 1:
                if (friendFragment == null) {
                    friendFragment = new FriendFragment();
                    transaction.add(R.id.content_FrameLayout, friendFragment);
                } else {
                    transaction.show(friendFragment);

                }
                friend_bottom_ImageButton.setBackgroundResource(R.drawable.friend_selected);
                friend_bottom_TextView.setTextColor(Color.BLUE);
                break;
            case 2:
                if (netDiskFragment == null) {
                    netDiskFragment = new NetDiskFragment();
                    transaction.add(R.id.content_FrameLayout, netDiskFragment);
                } else {
                    transaction.show(netDiskFragment);
                }
                dynamic_bottom_ImageButton.setBackgroundResource(R.drawable.dynamic_selected);
                dynamic_bottom_TextView.setTextColor(Color.BLUE);
                break;
            case 3:
                if (myFragment == null) {
                    myFragment = new MyFragment();
                    transaction.add(R.id.content_FrameLayout, myFragment);
                } else {
                    transaction.show(myFragment);
                }
                my_bottom_ImageButton.setBackgroundResource(R.drawable.my_selected);
                my_bottom_TextView.setTextColor(Color.BLUE);
                break;
            case 4:
                if (appStoreFragment == null) {
                    appStoreFragment = new AppStoreFragment();
                    transaction.add(R.id.content_FrameLayout, appStoreFragment);
                } else {
                    transaction.show(appStoreFragment);
                }
                app_store_bottom_ImageButton.setBackgroundResource(R.drawable.app_store_selected);
                app_store_bottom_TextView.setTextColor(Color.BLUE);
                break;
            default:
                break;
        }
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (messageFragment != null) {
            fragmentTransaction.hide(messageFragment);
        }
        if (friendFragment != null) {
            fragmentTransaction.hide(friendFragment);
        }
        if (netDiskFragment != null) {
            fragmentTransaction.hide(netDiskFragment);
        }
        if (myFragment != null) {
            fragmentTransaction.hide(myFragment);
        }
        if (appStoreFragment != null) {
            fragmentTransaction.hide(appStoreFragment);
        }
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        isCurrentDeviceLogin();
        setBottomSelectImageButton();
        switch (view.getId()) {
            case R.id.message_bottom_LinearLayout:
                setSelect(0);
                break;
            case R.id.friend_bottom_LinearLayout:
                setSelect(1);
                break;
            case R.id.net_disk_bottom_LinearLayout:
                setSelect(2);
                break;
            case R.id.my_bottom_LinearLayout:
                setSelect(3);
                break;
            case R.id.app_store_bottom_LinearLayout:
                setSelect(4);
                break;
            default:
                break;
        }
    }

    private void isCurrentDeviceLogin() {
        Log.e(TAG, "isCurrentDeviceLogin: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> parameter = new HashMap<>();
                parameter.put("id", SharedPreferencesUtils.getString(context, "id", "0", "user"));
                Message message = new Message();
                try {
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/query_user_by_id", parameter);
                    message.what = 1;
                } catch (IOException e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                isLoginHandler.sendMessage(message);
            }
        }).start();
    }

    private void setBottomSelectImageButton() {
        message_bottom_ImageButton.setBackgroundResource(R.drawable.message_normal);
        message_bottom_TextView.setTextColor(Color.BLACK);
        friend_bottom_ImageButton.setBackgroundResource(R.drawable.friend_normal);
        friend_bottom_TextView.setTextColor(Color.BLACK);
        dynamic_bottom_ImageButton.setBackgroundResource(R.drawable.dynamic_normal);
        dynamic_bottom_TextView.setTextColor(Color.BLACK);
        my_bottom_ImageButton.setBackgroundResource(R.drawable.my_normal);
        my_bottom_TextView.setTextColor(Color.BLACK);
        app_store_bottom_ImageButton.setBackgroundResource(R.drawable.app_store_normal);
        app_store_bottom_TextView.setTextColor(Color.BLACK);
    }

    @Override
    protected void onDestroy() {
        userJSONObjectList = null;
        NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        notificationManager.cancelAll();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        getSupportFragmentManager().getFragments();
        if (getSupportFragmentManager().getFragments().size() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, intent);
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IntentIntegrator.REQUEST_CODE) {
                IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (intentResult != null && intentResult.getContents() != null) {
                    String result = intentResult.getContents();
                    if (result.startsWith("http")) {
                        Uri uri = Uri.parse(result);
                        Intent urlIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(urlIntent);
                    } else {
                        Log.e(TAG, "onActivityResult: " + result);
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
                                parameter.put("phone", result);
                                Message message = new Message();
                                if (result.equals(SharedPreferencesUtils.getString(context, "phone", "", "user"))) {
                                    message.what = 1;
                                } else {
                                    message.what = 0;
                                    try {
                                        message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/query_user_by_phone", parameter);
                                    } catch (IOException e) {
                                        message.what = 2;
                                        e.printStackTrace();
                                    }
                                }
                                searchFriendHandler.sendMessage(message);
                            }
                        }).start();
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - CURRENT_BACK_PRESSED_TIME > BACK_PRESSED_INTERVAL) {
                CURRENT_BACK_PRESSED_TIME = System.currentTimeMillis();
                Toast.makeText(context, "再按一次返回键退出", Toast.LENGTH_LONG).show();
                return false;
            }
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}