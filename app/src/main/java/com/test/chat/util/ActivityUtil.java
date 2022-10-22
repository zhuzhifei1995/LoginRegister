package com.test.chat.util;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.test.chat.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ActivityUtil {
    //    连接手机的热点时
//    public static String NET_URL = "http://192.168.229.139:8080";
    public static final String TAG = "com.test.chat.zzf";
    public static final String QQ_SERVER_URL = "https://www.qq.com/contract.shtml";
    public static final String TMP_FRIEND_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/friend";
    public static final String TMP_USER_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/user";
    public static final String TMP_MESSAGE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/message";
    public static final String TMP_VOICE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/voice";
    public static final String TMP_REGISTER_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/register";
    public static final String TMP_UPDATE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/update";
    public static final String TMP_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp";
    //    连接电脑正常网络时
    public static String NET_URL = "http://192.168.1.4:8080";
//    public static String NET_URL = "http://192.168.137.1:8080/";

    public static boolean isMobileNO(String mobile) {
        Log.e(TAG, "isMobileNO: " + mobile);
        Pattern pattern = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher matcher = pattern.matcher(mobile);
        return matcher.matches();
    }

    public static boolean isEmail(String email) {
        Log.e(TAG, "isEmail: " + email);
        String flag = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern pattern = Pattern.compile(flag);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isPassword(String password) {
        Log.e(TAG, "isPassword: " + password);
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]{8,12}");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    @SuppressLint("ResourceAsColor")
    public static void setLinearLayoutBackground(LinearLayout my_fragment_linearLayout, int themeId) {
        switch (themeId) {
            case 1:
                my_fragment_linearLayout.setBackgroundResource(R.drawable.theme_one);
                break;
            case 2:
                my_fragment_linearLayout.setBackgroundResource(R.drawable.theme_two);
                break;
            case 3:
                my_fragment_linearLayout.setBackgroundResource(R.drawable.theme_three);
                break;
            case 4:
                my_fragment_linearLayout.setBackgroundResource(R.drawable.theme_four);
                break;
            default:
                my_fragment_linearLayout.setBackgroundResource(R.color.no_color);
                break;
        }
    }

}
