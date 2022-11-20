package com.test.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ApkDetailActivity extends Activity {

    private static String apkDetailHtml;
    private Context context;
    private final Handler apkDetailHtmlHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            String json = (String) message.obj;
            Toast.makeText(context, json, Toast.LENGTH_SHORT).show();
            super.handleMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_detail);
        apkDetailHtml = getIntent().getStringExtra("apkDetailHtml");
        context = ApkDetailActivity.this;
        initView();
    }

    private void initView() {
        Map<String, String> parameter = new HashMap<>();
        parameter.put("apk_detail_html", apkDetailHtml);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message message = new Message();
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_apk_detail_by_link", parameter);
                    apkDetailHtmlHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}