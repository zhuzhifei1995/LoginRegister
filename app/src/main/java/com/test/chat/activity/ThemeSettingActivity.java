package com.test.chat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.SharedPreferencesUtils;

import org.jetbrains.annotations.NotNull;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ThemeSettingActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private Context context;
    private ProgressDialog progressDialog;
    private final Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message message) {
            super.handleMessage(message);
            Toast.makeText(context, "应用主题修改成功！", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_setting);
        initView();
    }

    private void initView() {
        context = this;
        Button theme_one_Button = findViewById(R.id.theme_one_Button);
        theme_one_Button.setOnClickListener(this);
        Button theme_two_Button = findViewById(R.id.theme_two_Button);
        theme_two_Button.setOnClickListener(this);
        Button theme_three_Button = findViewById(R.id.theme_three_Button);
        theme_three_Button.setOnClickListener(this);
        Button theme_four_Button = findViewById(R.id.theme_four_Button);
        theme_four_Button.setOnClickListener(this);
        int themeId = SharedPreferencesUtils.getInt(context, "themeId", 0, "user");
        switch (themeId) {
            case 1:
                theme_one_Button.setText("已设置");
                theme_two_Button.setText("应用背景");
                theme_three_Button.setText("应用背景");
                theme_four_Button.setText("应用背景");
                break;
            case 2:
                theme_one_Button.setText("应用背景");
                theme_two_Button.setText("已设置");
                theme_three_Button.setText("应用背景");
                theme_four_Button.setText("应用背景");
                break;
            case 3:
                theme_one_Button.setText("应用背景");
                theme_two_Button.setText("应用背景");
                theme_three_Button.setText("已设置");
                theme_four_Button.setText("应用背景");
                break;
            case 4:
                theme_one_Button.setText("应用背景");
                theme_two_Button.setText("应用背景");
                theme_three_Button.setText("应用背景");
                theme_four_Button.setText("已设置");
                break;
            default:
                theme_one_Button.setText("应用背景");
                theme_two_Button.setText("应用背景");
                theme_three_Button.setText("应用背景");
                theme_four_Button.setText("应用背景");
                break;
        }
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("主题设置");
    }

    private void setThemeId(int themeId) {
        int themeOldId = SharedPreferencesUtils.getInt(context, "themeId", 0, "user");
        if (themeOldId == themeId) {
            Toast.makeText(context, "当前已设置该主题！", Toast.LENGTH_LONG).show();
        } else {
            SharedPreferencesUtils.putInt(context, "themeId", themeId, "user");
            Intent intent = new Intent("android.intent.action.CART_BROADCAST");
            intent.putExtra("data", "refresh");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            sendBroadcast(intent);
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
                prompt_TextView.setText("修改应用背景中.......");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        waitHandler.sendMessage(new Message());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.theme_one_Button:
                setThemeId(1);
                break;
            case R.id.theme_two_Button:
                setThemeId(2);
                break;
            case R.id.theme_three_Button:
                setThemeId(3);
                break;
            case R.id.theme_four_Button:
                setThemeId(4);
                break;
            default:
                break;
        }
    }
}