package com.test.chat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SharedPreferencesUtils {

    private static final String TAG = ActivityUtil.TAG;
    private static SharedPreferences sharedPreferences;

    public static void putBoolean(Context context, String key, boolean value, String filename) {
        Log.e(TAG, "保存临时数据到"+filename+"文件成功："+key);
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue, String filename) {
        Log.e(TAG, "获取临时数据"+filename+"文件成功："+key);
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static void putString(Context context, String key, String value, String filename) {
        Log.e(TAG, "保存临时数据到"+filename+"文件成功："+key);
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defValue, String filename) {
        Log.e(TAG, "获取临时数据"+filename+"文件成功："+key);
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(key, defValue);
    }

    public static void putInt(Context context, String key, int value, String filename) {
        Log.e(TAG, "保存临时数据到"+filename+"文件成功："+key);
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int defValue, String filename) {
        Log.e(TAG, "获取临时数据"+filename+"文件成功："+key);
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getInt(key, defValue);
    }

    public static void removeKey(Context context, String key, String filename) {
        Log.e(TAG, "删除临时文件数据成功"+filename);
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().remove(key).apply();
    }

}