package com.test.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.permission.PermissionManager;
import com.test.chat.permission.PermissionPageManagement;
import com.test.chat.util.ActivityUtil;
import com.yanzhenjie.permission.Permission;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WelcomeActivity extends Activity {

    private static final String TAG = ActivityUtil.TAG;
    private Context context;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initView();
        initPermission();
    }

    private void initView() {
        context = this;
        activity = this;
        int randomNumber = (int) (Math.random() * 3);
        LinearLayout main_LinearLayout = findViewById(R.id.main_LinearLayout);
        if (randomNumber == 1) {
            main_LinearLayout.setBackgroundResource(R.drawable.layer_splash_chn_bg);
        } else if (randomNumber == 2) {
            main_LinearLayout.setBackgroundResource(R.drawable.layer_splash_math_bg);
        } else {
            main_LinearLayout.setBackgroundResource(R.drawable.layer_splash_eng_bg);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initPermission() {
        Log.e(TAG, "开始请求应用权限");
        PermissionManager.requestPermission(context, new PermissionManager.Callback() {
            @Override
            public void permissionSuccess() {
                PermissionManager.requestPermission(context, new PermissionManager.Callback() {
                    @Override
                    public void permissionSuccess() {
                        PermissionManager.requestPermission(context, new PermissionManager.Callback() {
                            @Override
                            public void permissionSuccess() {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Intent intent = new Intent(context, LoginActivity.class);
                                            Thread.sleep(2000);
                                            startActivity(intent);
                                            finish();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }).start();
                            }

                            @Override
                            public void permissionFailed() {
                                Log.e(TAG, "请求应用文件权限失败！");
                                PermissionPageManagement.goToPermissionSetting(activity);
                            }
                        }, Permission.Group.STORAGE);
                    }

                    @Override
                    public void permissionFailed() {
                        Log.e(TAG, "请求应用录音权限失败！");
                        PermissionPageManagement.goToPermissionSetting(activity);
                    }
                }, Permission.Group.MICROPHONE);
            }

            @Override
            public void permissionFailed() {
                Log.e(TAG, "请求应用相机权限失败！");
                PermissionPageManagement.goToPermissionSetting(activity);
            }
        }, Permission.Group.CAMERA);
    }

}