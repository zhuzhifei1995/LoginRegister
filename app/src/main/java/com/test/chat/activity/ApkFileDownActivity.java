package com.test.chat.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.File;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ApkFileDownActivity extends Activity {

    private final String TAG = ActivityUtil.TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_file_down);
        Context context = this;
        File[] fileList = new File(ActivityUtil.TMP_APK_FILE_PATH).listFiles();
        if (fileList != null) {
            if (fileList.length == 0){
                Toast.makeText(context, "没有APK文件", Toast.LENGTH_LONG).show();
            }else {
                for (File file : fileList) {
                    if (!file.isDirectory()) {
                        try {
                            ApkFile apkFile = new ApkFile(file);
                            ApkMeta apkMeta = apkFile.getApkMeta();
                            Toast.makeText(context, apkMeta.toString(), Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        } else {
            Toast.makeText(context, "没有APK文件", Toast.LENGTH_LONG).show();
        }
    }
}