package com.test.chat.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ImageUtil {

    private static final String TAG = ActivityUtil.TAG;

    public static Bitmap getBitmapFromFile(String dirFile, String name) {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(new File(dirFile, name).getPath(), options);
    }

    public static void saveBitmapToTmpFile(Activity activity, Bitmap bitmap, String dirFile, String name) {
        Log.e(TAG, "保存图片:" + name);
        File file;
        file = new File(dirFile);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("TAG", "文件夹创建失败");
            } else {
                Log.e("TAG", "文件夹创建成功");
            }
        }
        try {
            if (bitmap != null) {
                File photoFile = new File(file, name);
                FileOutputStream fileOutputStream = new FileOutputStream(photoFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                Log.e(TAG, "已经保存");
            } else {
                Log.e(TAG, "保存失败，图片不存在");
            }

        } catch (IOException e) {
            Log.e(TAG, "保存失败");
            e.printStackTrace();
        }
    }

}
