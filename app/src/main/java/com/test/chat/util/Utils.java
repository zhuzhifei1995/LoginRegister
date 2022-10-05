package com.test.chat.util;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.M)
public class Utils {

    //    连接电脑正常网络时
    public static String NET_URL = "http://192.168.1.4:8080";
    //    连接手机的热点时
//    public static String NET_URL = "http://192.168.229.139:8080";
    public static String TAG = "com.test.chat.zzf";

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

}
