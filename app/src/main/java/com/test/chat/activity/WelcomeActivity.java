package com.test.chat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.PermissionManager;
import com.test.chat.util.PermissionPageManagement;
import com.test.chat.util.Utils;
import com.yanzhenjie.permission.Permission;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WelcomeActivity extends Activity {

    private static final String TAG = Utils.TAG;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initPermission() {
        PermissionManager.requestPermission(WelcomeActivity.this, new PermissionManager.Callback() {
            @Override
            public void permissionSuccess() {
                PermissionManager.requestPermission(WelcomeActivity.this, new PermissionManager.Callback() {
                    @Override
                    public void permissionSuccess() {
                        PermissionManager.requestPermission(WelcomeActivity.this, new PermissionManager.Callback() {
                            @Override
                            public void permissionSuccess() {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
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
                                Log.e(TAG, "permissionFailed:STORAGE ");
                                PermissionPageManagement.goToPermissionSetting(WelcomeActivity.this);
                            }
                        }, Permission.Group.STORAGE);
                    }

                    @Override
                    public void permissionFailed() {
                        Log.e(TAG, "permissionFailed:MICROPHONE ");
                        PermissionPageManagement.goToPermissionSetting(WelcomeActivity.this);
                    }
                }, Permission.Group.MICROPHONE);
            }

            @Override
            public void permissionFailed() {
                Log.e(TAG, "permissionFailed:CAMERA ");
                PermissionPageManagement.goToPermissionSetting(WelcomeActivity.this);
            }
        }, Permission.Group.CAMERA);
    }

}