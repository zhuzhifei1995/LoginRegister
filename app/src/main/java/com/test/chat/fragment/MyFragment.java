package com.test.chat.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.chat.R;
import com.test.chat.activity.LoginActivity;
import com.test.chat.activity.PhotoShowActivity;
import com.test.chat.activity.ThemeSettingActivity;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    public MyFragment(){}

    private static final String TAG = ActivityUtil.TAG;
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private View myFragmentView;
    private Activity activity;
    private Context context;
    private ProgressDialog progressDialog;
    private final Handler downloadApkFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            String downFileName = (String) message.obj;
            ActivityUtil.showDownloadNotification(context, "", -1, "应用更新下载完成！", 1);
            File downloadFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, downFileName + ".download");
            File cacheFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, downFileName + ".cache");
            TmpFileUtil.copyFile(downloadFile, cacheFile);
            if (downloadFile.delete()) {
                Log.e(TAG, "handleMessage: 临时更新apk文件删除成功");
            } else {
                Log.e(TAG, "handleMessage: 临时更新apk文件删除失败");
            }
            Log.e(TAG, "download success");
            installUpdateApk(cacheFile, downFileName);
            progressDialog.dismiss();
            super.handleMessage(message);
        }
    };
    private final Handler updateApkHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            int versionCode = ActivityUtil.getApkVersionCode(context);
            Log.e(TAG, "当前应用版本号：" + versionCode);
            if (message.what == 1) {
                String apkJson = (String) message.obj;
                try {
                    JSONObject jsonObject = new JSONObject(apkJson);
                    int newVersionCode = jsonObject.getInt("version_code");
                    String apkFileName = jsonObject.getString("apk_file_name");
                    String apkDownloadUrl = jsonObject.getString("apk_download_url");
                    String md5Number = jsonObject.getString("md5_number");
                    if (newVersionCode > versionCode) {
                        Log.e(TAG, "handleMessage: 应用需要更新");
                        File cacheFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, apkFileName + ".cache");
                        if (cacheFile.exists()) {
                            if (!md5Number.equals(TmpFileUtil.getOneFileMD5(cacheFile))) {
                                downloadApkFile(apkDownloadUrl, apkFileName);
                            } else {
                                installUpdateApk(cacheFile, apkFileName);
                            }
                        } else {
                            downloadApkFile(apkDownloadUrl, apkFileName);
                        }
                    } else {
                        Log.e(TAG, "handleMessage: 应用不需要更新");
                        Toast.makeText(context, "当前应用版本已经是最新！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "handleMessage: 应用不需要更新");
                    Toast.makeText(context, "当前应用版本已经是最新！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(message);
        }
    };
    private final Handler loginOutHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message message) {
            Intent intent = new Intent(context, LoginActivity.class);
            SharedPreferencesUtils.removeKey(context, "status", "user");
            File userPhotoFile = new File(ActivityUtil.TMP_USER_FILE_PATH, "photo.png.cache");
            if (userPhotoFile.delete()) {
                Log.e(TAG, "临时头像图片删除成功：" + userPhotoFile.getAbsolutePath());
            } else {
                Log.e(TAG, "无临时头像文件图片：" + userPhotoFile.getAbsolutePath());
            }
            TmpFileUtil.deleteFileCache(new File(ActivityUtil.TMP_FILE_PATH));
            startActivity(intent);
            activity.finish();
            progressDialog.dismiss();
            super.handleMessage(message);
        }
    };
    private final Handler updatePasswordHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message message) {
            try {
                String json = (String) message.obj;
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    SharedPreferencesUtils.putString(context, "password", "", "user");
                    SharedPreferencesUtils.putBoolean(context, "is_remember_password", false, "user");
                    progressDialog.dismiss();
                    Window window = progressDialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.gravity = Gravity.CENTER;
                        progressDialog.setCancelable(false);
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.loading_progress_bar);
                        TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                        prompt_TextView.setText("退出登陆中.......");
                    }
                    Toast.makeText(context, "修改密码成功，登录信息失效，请重新登录！", Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                loginOutHandler.sendMessage(new Message());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(context, "修改密码失败！", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                progressDialog.dismiss();
                Toast.makeText(context, "修改密码失败！", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            super.handleMessage(message);
        }
    };
    private EditText update_verification_code_EditText;
    private final Handler codeHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Log.e(TAG, "handleMessage: " + message.obj);
            try {
                JSONObject jsonObject = new JSONObject((String) message.obj);
                if (jsonObject.getString("code").equals("1")) {
                    String phone = jsonObject.getString("phone");
                    String verificationCode = jsonObject.getString("verification_code");
                    progressDialog.dismiss();
                    Window window = progressDialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.gravity = Gravity.CENTER;
                        progressDialog.setCancelable(false);
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.verification_code_progress_bar);
                        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        update_verification_code_EditText = progressDialog.findViewById(R.id.update_verification_code_EditText);
                        update_verification_code_EditText.requestFocus();
                    }
                    TextView cancel_update_TextView = progressDialog.findViewById(R.id.cancel_update_TextView);
                    cancel_update_TextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            progressDialog.dismiss();
                        }
                    });
                    TextView confirm_update_TextView = progressDialog.findViewById(R.id.confirm_update_TextView);
                    confirm_update_TextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (verificationCode.equals(update_verification_code_EditText.getText().toString())) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Map<String, String> parameter = new HashMap<>();
                                        parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                                        parameter.put("phone", phone);
                                        new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/update_phone_by_id", parameter);
                                    }
                                }).start();
                                SharedPreferencesUtils.putString(context, "phone", phone, "user");
                                Toast.makeText(context, "修改绑定的手机号成功，登录信息失效，请重新登录！", Toast.LENGTH_SHORT).show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(2000);
                                            loginOutHandler.sendMessage(new Message());
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            } else {
                                Toast.makeText(context, "验证码输入错误！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(context, "修改失败，该号码已绑定账号！", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    };
    private EditText update_phone_number_EditText;
    private EditText dialog_old_password_EditText;
    private boolean IS_SHOW_MY_MESSAGE;
    private ImageView photo_my_ImageView;
    private final Handler uploadUpdatePhotoHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    photo_my_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(ActivityUtil.TMP_UPDATE_FILE_PATH, "photo.png.cache"));
                    Toast.makeText(context, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                    TmpFileUtil.copyFile(new File(ActivityUtil.TMP_UPDATE_FILE_PATH, "photo.png.cache"),
                            new File(ActivityUtil.TMP_USER_FILE_PATH, "photo.png.cache"));
                } else {
                    Toast.makeText(context, jsonObject.getString("status"), Toast.LENGTH_LONG).show();
                    SharedPreferencesUtils.removeKey(context, "status", "user");
                    startActivity(new Intent(context, LoginActivity.class));
                    activity.finish();
                }
            } catch (JSONException e) {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_LONG).show();
                photo_my_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(ActivityUtil.TMP_USER_FILE_PATH, "photo.png.cache"));
                e.printStackTrace();
            }
            super.handleMessage(message);
            progressDialog.dismiss();
        }
    };
    private SwipeRefreshLayout my_SwipeRefreshLayout;
    private final Handler updateNikeNameHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message message) {
            try {
                String json = (String) message.obj;
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    SharedPreferencesUtils.putString(context, "nick_name", jsonObject.getString("nick_name"), "user");
                    Toast.makeText(context, jsonObject.getString("status"), Toast.LENGTH_SHORT).show();
                    initMyFragmentView();
                } else {
                    Toast.makeText(context, "修改昵称失败！", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(context, "修改昵称失败！", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            super.handleMessage(message);
            progressDialog.dismiss();
        }
    };
    private final Handler saveUserPhotoHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Bitmap bitmap = (Bitmap) message.obj;
            int type = message.what;
            if (bitmap != null) {
                if (type == 1) {
                    ImageUtil.saveBitmapToTmpFile(bitmap, ActivityUtil.TMP_USER_FILE_PATH, "photo.png.cache");
                } else {
                    ImageUtil.saveBitmapToTmpFile(bitmap, ActivityUtil.TMP_USER_FILE_PATH, "qr_code.png.cache");
                }
                photo_my_ImageView.setImageBitmap(bitmap);
                initMyFragmentView();
                my_SwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, "刷新成功！", Toast.LENGTH_LONG).show();
            }
            super.handleMessage(message);
        }
    };
    private final Handler mySwipeRefreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            try {
                String json = (String) message.obj;
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    JSONObject userJSONObject = jsonObject.getJSONObject("message");
                    SharedPreferencesUtils.putString(context, "create_time", userJSONObject.getString("create_time"), "user");
                    SharedPreferencesUtils.putString(context, "password", userJSONObject.getString("password"), "user");
                    SharedPreferencesUtils.putString(context, "login_number", userJSONObject.getString("login_number"), "user");
                    SharedPreferencesUtils.putString(context, "nick_name", userJSONObject.getString("nick_name"), "user");
                    SharedPreferencesUtils.putString(context, "phone", userJSONObject.getString("phone"), "user");
                    SharedPreferencesUtils.putString(context, "photo", userJSONObject.getString("photo_url"), "user");
                    SharedPreferencesUtils.putString(context, "qr_code_url", userJSONObject.getString("qr_code_url"), "user");
                    saveUserPhoto(userJSONObject.getString("photo_url"), 1);
                    saveUserPhoto(userJSONObject.getString("qr_code_url"), 2);
                } else {
                    Toast.makeText(context, "刷新失败！", Toast.LENGTH_SHORT).show();
                    my_SwipeRefreshLayout.setRefreshing(false);
                }
            } catch (JSONException e) {
                Toast.makeText(context, "刷新失败！", Toast.LENGTH_SHORT).show();
                my_SwipeRefreshLayout.setRefreshing(false);
                e.printStackTrace();
            }
            super.handleMessage(message);
        }
    };
    private EditText dialog_nike_name_EditText;

    private void installUpdateApk(File cacheFile, String downFileName) {
        File apkFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, downFileName);
        TmpFileUtil.copyFile(cacheFile, apkFile);
        ActivityUtil.installApk(context, apkFile);
    }

    private void downloadApkFile(String url, String downFileName) {
        long startTime = System.currentTimeMillis();
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
            prompt_TextView.setText(new String("更新文件下载中...."));
        }
        ActivityUtil.showDownloadNotification(context, "", -1, "应用更新文件下载中......", 0);
        Log.e(TAG, "开始下载时间：startTime=" + startTime);
        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "创建下载文件任务失败！", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "download failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                BufferedSink bufferedSink;
                File downFold = new File(ActivityUtil.TMP_UPDATE_FILE_PATH);
                if (!downFold.exists()) {
                    if (downFold.mkdir()) {
                        Log.e(TAG, "onResponse: 下载文件夹创建成功");
                    } else {
                        Log.e(TAG, "onResponse: 下载文件夹创建失败");
                    }
                }
                File downFile = new File(downFold, downFileName + ".download");
                if (!downFile.exists()) {
                    try {
                        if (downFile.createNewFile()) {
                            Log.e(TAG, "onResponse: 下载文件创建成功");
                        } else {
                            Log.e(TAG, "onResponse: 下载文件创建失败");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Sink sink = Okio.sink(downFile);
                bufferedSink = Okio.buffer(sink);
                bufferedSink.writeAll(Objects.requireNonNull(response.body()).source());
                bufferedSink.close();
                Message message = new Message();
                message.obj = downFileName;
                downloadApkFileHandler.sendMessage(message);
                Log.e(TAG, "totalTime=" + (System.currentTimeMillis() - startTime));
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activity = getActivity();
        context = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        myFragmentView = layoutInflater.inflate(R.layout.fragment_my, viewGroup, false);
        initMyFragmentView();
        return myFragmentView;
    }

    private void initMyFragmentView() {
        ActivityUtil.setLinearLayoutBackground(myFragmentView.findViewById(R.id.my_fragment_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        photo_my_ImageView = myFragmentView.findViewById(R.id.photo_my_ImageView);
        photo_my_ImageView.setOnClickListener(this);
        LinearLayout my_message_LinearLayout = myFragmentView.findViewById(R.id.my_message_LinearLayout);
        my_message_LinearLayout.setOnClickListener(this);
        TextView login_out_TextView = myFragmentView.findViewById(R.id.login_out_TextView);
        login_out_TextView.setOnClickListener(this);
        TextView theme_setting_TextView = myFragmentView.findViewById(R.id.theme_setting_TextView);
        theme_setting_TextView.setOnClickListener(this);
        TextView qr_code_TextView = myFragmentView.findViewById(R.id.qr_code_TextView);
        qr_code_TextView.setOnClickListener(this);
        my_SwipeRefreshLayout = myFragmentView.findViewById(R.id.my_SwipeRefreshLayout);
        my_SwipeRefreshLayout.setOnRefreshListener(this);
        TextView account_and_security_TextView = myFragmentView.findViewById(R.id.account_and_security_TextView);
        account_and_security_TextView.setOnClickListener(this);
        LinearLayout login_number_LinearLayout = myFragmentView.findViewById(R.id.login_number_LinearLayout);
        login_number_LinearLayout.setOnClickListener(this);
        LinearLayout nike_name_LinearLayout = myFragmentView.findViewById(R.id.nike_name_LinearLayout);
        nike_name_LinearLayout.setOnClickListener(this);
        LinearLayout create_time_LinearLayout = myFragmentView.findViewById(R.id.create_time_LinearLayout);
        create_time_LinearLayout.setOnClickListener(this);
        TextView update_apk_TextView = myFragmentView.findViewById(R.id.update_apk_TextView);
        update_apk_TextView.setOnClickListener(this);
        initTitleView();
        initMyMessageView();
    }

    private void initTitleView() {
        my_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        TextView top_title_TextView = myFragmentView.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("我的");
        Button title_right_Button = myFragmentView.findViewById(R.id.title_right_Button);
        title_right_Button.setText("");
        Button title_left_Button = myFragmentView.findViewById(R.id.title_left_Button);
        title_left_Button.setText("");
    }

    private void initMyMessageView() {
        Bitmap bitmap = ImageUtil.getBitmapFromFile(ActivityUtil.TMP_USER_FILE_PATH, "photo.png.cache");
        if (bitmap != null) {
            Log.e(TAG, "图片加载正常");
            photo_my_ImageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "图片为空,加载失败");
            photo_my_ImageView.setImageResource(R.drawable.user_default_photo);
            Toast.makeText(context, "登录信息失效，请重新登录！", Toast.LENGTH_SHORT).show();
            SharedPreferencesUtils.putBoolean(context, "status", false, "user");
            startActivity(new Intent(context, LoginActivity.class));
            activity.finish();
        }
        TextView nike_name_TextView = myFragmentView.findViewById(R.id.nike_name_TextView);
        String nick_name = SharedPreferencesUtils.getString(context, "nick_name", "", "user");
        if (!nick_name.equals("")) {
            nike_name_TextView.setText(nick_name);
        } else {
            nike_name_TextView.setText("未设置昵称");
        }
        TextView login_number_TextView = myFragmentView.findViewById(R.id.login_number_TextView);
        String login_number = SharedPreferencesUtils.getString(context, "login_number", "", "user");
        login_number_TextView.setText(login_number);
        TextView phone_number_TextView = myFragmentView.findViewById(R.id.phone_number_TextView);
        String phone = SharedPreferencesUtils.getString(context, "phone", "", "user");
        phone_number_TextView.setText(phone);
        TextView create_time_TextView = myFragmentView.findViewById(R.id.create_time_TextView);
        String create_time = SharedPreferencesUtils.getString(context, "create_time", "", "user");
        create_time_TextView.setText(create_time);
        TextView password_TextView = myFragmentView.findViewById(R.id.password_TextView);
        String password = SharedPreferencesUtils.getString(context, "password", "", "user");
        password_TextView.setText(password);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_my_ImageView:
                updateMyPhoto();
                break;
            case R.id.my_message_LinearLayout:
                showMyMessage();
                break;
            case R.id.login_out_TextView:
                loginOut();
                break;
            case R.id.account_and_security_TextView:
                showAccountAndSecurity();
                break;
            case R.id.login_number_LinearLayout:
                loginNumberClick();
                break;
            case R.id.nike_name_LinearLayout:
                updateNikeName();
                break;
            case R.id.create_time_LinearLayout:
                createTimeClick();
                break;
            case R.id.theme_setting_TextView:
                settingTheme();
                break;
            case R.id.qr_code_TextView:
                showMyQRCode();
                break;
            case R.id.update_apk_TextView:
                updateApk();
            default:
                break;
        }

    }

    private void updateApk() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    message.obj = new HttpUtil(context).getRequest(ActivityUtil.NET_URL + "/get_apk_update_file");
                    message.what = 1;
                } catch (IOException e) {
                    e.printStackTrace();
                    message.what = 0;
                }
                updateApkHandler.sendMessage(message);
            }
        }).start();
    }

    private void showMyQRCode() {
        Intent intent = new Intent(context, PhotoShowActivity.class);
        intent.putExtra("flag", 4);
        startActivity(intent);
    }

    private void createTimeClick() {
        Toast.makeText(context, "注册时间不能修改！", Toast.LENGTH_SHORT).show();
    }

    private void loginNumberClick() {
        Toast.makeText(context, "登录账号不能修改！", Toast.LENGTH_SHORT).show();
    }

    private void settingTheme() {
        Intent intent = new Intent(activity, ThemeSettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART_BROADCAST");
        BroadcastReceiver mItemViewListClickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("data");
                if ("refresh".equals(message)) {
                    initMyFragmentView();
                }
            }
        };
        broadcastManager.registerReceiver(mItemViewListClickReceiver, intentFilter);
    }

    private void updateNikeName() {
        progressDialog = new ProgressDialog(context);
        Window window = progressDialog.getWindow();
        String oldNickName = SharedPreferencesUtils.getString(context, "nick_name", "", "user");
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            progressDialog.setCancelable(false);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.update_nike_name_progress_bar);
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog_nike_name_EditText = progressDialog.findViewById(R.id.dialog_nike_name_EditText);
            dialog_nike_name_EditText.requestFocus();
            dialog_nike_name_EditText.setText(oldNickName);
            dialog_nike_name_EditText.setSelection(oldNickName.length());
        }
        TextView cancel_update_TextView = progressDialog.findViewById(R.id.cancel_update_TextView);
        cancel_update_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
            }
        });
        TextView confirm_update_TextView = progressDialog.findViewById(R.id.confirm_update_TextView);
        confirm_update_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: " + dialog_nike_name_EditText.getText().toString());
                String newNickName = dialog_nike_name_EditText.getText().toString().trim();
                if (oldNickName.equals(newNickName)) {
                    Toast.makeText(context, "与旧的昵称一致，请重新填写！", Toast.LENGTH_SHORT).show();
                } else {
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    progressDialog.dismiss();
                    progressDialog = new ProgressDialog(context);
                    Window window = progressDialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.gravity = Gravity.CENTER;
                        progressDialog.setCancelable(false);
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.loading_progress_bar);
                        TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                        prompt_TextView.setText("修改昵称信息中.......");
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, String> parameter = new HashMap<>();
                            parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                            parameter.put("nick_name", newNickName);
                            Message message = new Message();
                            message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/update_user_nike_name_by_id", parameter);
                            updateNikeNameHandler.sendMessage(message);
                        }
                    }).start();
                }
            }
        });

    }

    private void showAccountAndSecurity() {
        progressDialog = new ProgressDialog(context);
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            progressDialog.setCancelable(true);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.account_and_security_progress_bar);
        }
        TextView update_password_TextView = progressDialog.findViewById(R.id.update_password_TextView);
        update_password_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                String oldPassword = SharedPreferencesUtils.getString(context, "password", "", "user");
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    progressDialog.setCancelable(false);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.update_password_progress_bar);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    dialog_old_password_EditText = progressDialog.findViewById(R.id.dialog_old_password_EditText);
                    dialog_old_password_EditText.requestFocus();
                }
                TextView cancel_update_TextView = progressDialog.findViewById(R.id.cancel_update_TextView);
                cancel_update_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.dismiss();
                    }
                });
                TextView confirm_update_TextView = progressDialog.findViewById(R.id.confirm_update_TextView);
                confirm_update_TextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText dialog_new_password_EditText = progressDialog.findViewById(R.id.dialog_new_password_EditText);
                        EditText dialog_re_new_password_EditText = progressDialog.findViewById(R.id.dialog_re_new_password_EditText);
                        String inputOldPassword = dialog_old_password_EditText.getText().toString();
                        if (inputOldPassword.equals(oldPassword)) {
                            String newPassword = dialog_new_password_EditText.getText().toString();
                            String reNewPassword = dialog_re_new_password_EditText.getText().toString();
                            if (inputOldPassword.equals(newPassword)) {
                                dialog_new_password_EditText.requestFocus();
                                dialog_new_password_EditText.setSelection(newPassword.length());
                                Toast.makeText(context, "新密码与原始密码一样，请重新输入！", Toast.LENGTH_LONG).show();
                            } else {
                                if (ActivityUtil.isPassword(newPassword)) {
                                    if (newPassword.equals(reNewPassword)) {
                                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                        progressDialog.dismiss();
                                        progressDialog = new ProgressDialog(context);
                                        Window window = progressDialog.getWindow();
                                        if (window != null) {
                                            WindowManager.LayoutParams params = window.getAttributes();
                                            params.gravity = Gravity.CENTER;
                                            progressDialog.setCancelable(false);
                                            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            progressDialog.show();
                                            progressDialog.setContentView(R.layout.loading_progress_bar);
                                            TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                                            prompt_TextView.setText("修改密码中.......");
                                        }
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Map<String, String> parameter = new HashMap<>();
                                                parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                                                parameter.put("password", newPassword);
                                                Message message = new Message();
                                                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/update_password_by_id", parameter);
                                                updatePasswordHandler.sendMessage(message);
                                            }
                                        }).start();
                                    } else {
                                        dialog_re_new_password_EditText.requestFocus();
                                        Toast.makeText(context, "两次输入的修改密码不一致！", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    dialog_new_password_EditText.requestFocus();
                                    Toast.makeText(context, "密码格式错误，请输入8·12位字母和数字组合", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(context, "原始密码输入错误！", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        TextView update_phone_number_TextView = progressDialog.findViewById(R.id.update_phone_number_TextView);
        update_phone_number_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                Window window = progressDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    progressDialog.setCancelable(false);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.update_phone_number_progress_bar);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    update_phone_number_EditText = progressDialog.findViewById(R.id.update_phone_number_EditText);
                    update_phone_number_EditText.requestFocus();
                    TextView update_local_setting_TextView = progressDialog.findViewById(R.id.update_local_setting_TextView);
                    update_local_setting_TextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            final ProgressDialog localPhoneProgressDialog = new ProgressDialog(context);
                            Window window = localPhoneProgressDialog.getWindow();
                            if (window != null) {
                                WindowManager.LayoutParams params = window.getAttributes();
                                params.gravity = Gravity.CENTER;
                                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                progressDialog.setCancelable(true);
                                localPhoneProgressDialog.show();
                                localPhoneProgressDialog.setContentView(R.layout.local_phone_progress_bar);
                            }
                            TextView update_local_TextView = progressDialog.findViewById(R.id.update_local_TextView);
                            TextView _86_TextView = localPhoneProgressDialog.findViewById(R.id._86_TextView);
                            _86_TextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    localPhoneProgressDialog.dismiss();
                                    update_local_TextView.setText(new String("+86中国大陆"));
                                }
                            });

                            TextView _853_TextView = localPhoneProgressDialog.findViewById(R.id._853_TextView);
                            _853_TextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    localPhoneProgressDialog.dismiss();
                                    update_local_TextView.setText(new String("+853中国澳门"));
                                }
                            });

                            TextView _852_TextView = localPhoneProgressDialog.findViewById(R.id._852_TextView);
                            _852_TextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    localPhoneProgressDialog.dismiss();
                                    update_local_TextView.setText(new String("+852中国香港"));
                                }
                            });

                            TextView _886_TextView = localPhoneProgressDialog.findViewById(R.id._886_TextView);
                            _886_TextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    localPhoneProgressDialog.dismiss();
                                    update_local_TextView.setText(new String("+886中国台湾"));
                                }
                            });

                            TextView cancel_TextView = localPhoneProgressDialog.findViewById(R.id.cancel_TextView);
                            cancel_TextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    localPhoneProgressDialog.dismiss();
                                }
                            });
                        }
                    });
                    TextView cancel_update_TextView = progressDialog.findViewById(R.id.cancel_update_TextView);
                    cancel_update_TextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            progressDialog.dismiss();
                        }
                    });
                    TextView confirm_update_TextView = progressDialog.findViewById(R.id.confirm_update_TextView);
                    confirm_update_TextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String newPhone = update_phone_number_EditText.getText().toString();
                            Log.e(TAG, "onClick: " + newPhone);
                            String oldPhone = SharedPreferencesUtils.getString(context, "phone", "", "user");
                            Log.e(TAG, "onClick: " + oldPhone);
                            if (!oldPhone.equals(newPhone)) {
                                if (ActivityUtil.isMobileNO(newPhone)) {
                                    Window window = progressDialog.getWindow();
                                    if (window != null) {
                                        WindowManager.LayoutParams params = window.getAttributes();
                                        params.gravity = Gravity.CENTER;
                                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        progressDialog.setContentView(R.layout.loading_progress_bar);
                                        TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                                        prompt_TextView.setText("获取验证码中.......");
                                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        inputMethodManager.hideSoftInputFromWindow(update_phone_number_EditText.getWindowToken(), 0);
                                    }
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Map<String, String> parameter = new HashMap<>();
                                            parameter.put("phone", newPhone);
                                            Message message = new Message();
                                            message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/phone_is_register_user", parameter);
                                            codeHandler.sendMessage(message);
                                        }
                                    }).start();
                                } else {
                                    Toast.makeText(context, "手机号输入的不对！", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "与上次绑定的手机号一样！", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                        }
                    });
                }
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

    private void loginOut() {
        progressDialog = new ProgressDialog(context);
        Window window = progressDialog.getWindow();
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
                Window window = progressDialog.getWindow();
                if (window != null) {
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.CENTER;
                    progressDialog.setCancelable(false);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.loading_progress_bar);
                    TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
                    prompt_TextView.setText("退出登陆中.......");
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            loginOutHandler.sendMessage(new Message());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
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

    private void showMyMessage() {
        ImageView message_show_ImageView = myFragmentView.findViewById(R.id.message_show_ImageView);
        LinearLayout my_show_message_LinearLayout = myFragmentView.findViewById(R.id.my_show_message_LinearLayout);
        View line_message_View = myFragmentView.findViewById(R.id.line_message_View);
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

    private void updateMyPhoto() {
        progressDialog = new ProgressDialog(context);
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

    private void selectFromAlbum() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
        } else {
            photoFromCapture();
        }
    }

    @Override
    @SuppressLint("QueryPermissionsNeeded")
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        activity.startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(context, "未找到图片查看器", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFromCapture();
                }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectFromPhoto() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                Toast.makeText(context, "未找到图片查看器", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void photoFromCapture() {
        Intent intent;
        Uri pictureUri;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                }
            }
        }
        File pictureFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, "photo.png.cache");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileProvider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void showMyPhoto(ProgressDialog progressDialog) {
        progressDialog.dismiss();
        Intent intent = new Intent(context, PhotoShowActivity.class);
        intent.putExtra("flag", 1);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                my_SwipeRefreshLayout.setRefreshing(true);
                String id = SharedPreferencesUtils.getString(context, "id", "", "user");
                Map<String, String> parameter = new HashMap<>();
                parameter.put("id", id);
                Message message = new Message();
                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/query_user_by_id", parameter);
                mySwipeRefreshHandler.sendMessage(message);
            }
        }).start();
    }

    private void saveUserPhoto(final String photo, int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    message.obj = new HttpUtil(context).getImageBitmap(photo);
                    message.what = type;
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    my_SwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(context, "刷新失败！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                saveUserPhotoHandler.sendMessage(message);
            }
        }).start();
    }

    private void setSmallImageToImageView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photoBitmap = extras.getParcelable("data");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dirFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH);
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                    } else {
                        Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                    }
                }
                File file = new File(dirFile, "photo.png.cache");
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
                    Thread.sleep(1000);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("id", SharedPreferencesUtils.getString(context, "id", "0", "user"));
                    File updatePhotoFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, "photo.png.cache");
                    Message message = new Message();
                    message.obj = new HttpUtil(context).upLoadImageFile(updatePhotoFile,
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
            File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/update");
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                }
            }
            file = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, "photo.png.cache");
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileProvider", file), "image/*");
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
            activity.startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
        } else {
            Toast.makeText(context, "剪切图片失败", Toast.LENGTH_LONG).show();
        }
    }

    private void setBigImageToImageView() {
        File photoFile = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, "photo.png.cache");
        Uri tempPhotoUri = Uri.fromFile(photoFile);
        try {
            Bitmap photoBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(tempPhotoUri));
            photo_my_ImageView.setImageBitmap(photoBitmap);
            uploadUpdatePhoto();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        activity.startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        initMyFragmentView();
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
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
                    File temp = new File(ActivityUtil.TMP_UPDATE_FILE_PATH, "photo.png.cache");
                    startBigPhotoZoom(Uri.fromFile(temp));
                    break;
                default:
                    break;
            }
        }
    }
}