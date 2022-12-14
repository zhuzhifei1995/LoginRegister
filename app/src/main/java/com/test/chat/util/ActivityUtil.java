package com.test.chat.util;


import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.test.chat.R;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ActivityUtil {
    //    连接手机的热点时
//    public static String NET_URL = "http://192.168.229.139:8080";
    public static final String TAG = "com.test.chat.zzf";
    public static final String QQ_SERVER_URL = "https://www.qq.com/contract.shtml";

    public static final String ROOT_FILE_PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String TMP_FRIEND_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/friend";
    public static final String TMP_USER_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/user";
    public static final String TMP_MESSAGE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/message";
    public static final String TMP_VOICE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/voice";
    public static final String TMP_REGISTER_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/register";
    public static final String TMP_UPDATE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/update";
    public static final String TMP_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/download";
    public static final String TMP_APK_ICON_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/apk/icon";
    public static final String TMP_APK_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files/apk/apk";
    public static final String TMP_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.test.chat/files";

    public static final String[] MOVIE_TYPE = {"mp4", "flv", "mkv", "webm", "avi", "wmv"};
    public static final String[] MUSIC_TYPE = {"aac", "mp3", "ac3", "ogg", "wav"};
    public static final String[] TXT_TYPE = {"txt", "ini", "log", "xml", "json"};
    public static final String[] IMAGE_TYPE = {"jpg", "png", "bmp", "jpeg", "gif"};

    public static final String ORDER_TYPE_POST = "post";
    public static final String ORDER_TYPE_UPDATE = "update";
    public static final String ORDER_TYPE_CLICK = "click";

    //    连接电脑正常网络时
    public static String NET_URL = "http://192.168.1.3:8080";
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

    public static int getApkVersionCode(Context context) {
        int versionCode = 0;
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
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
                my_fragment_linearLayout.setBackgroundResource(R.color.white);
                break;
        }
    }

    public static void showDownloadNotification(Context context, String downFileName, int position, String notificationShow, int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(context, downFileName)
                    .setContentTitle("应用下载管理")
                    .setContentText(downFileName + " " + notificationShow)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.download_normal)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(downFileName, TAG, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(position, notification);
            if (type == 1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            notificationManager.cancel(position);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } else {
            Log.e(TAG, "showDownloadNotification: 安卓版本？");
        }
    }

    public static void installApk(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity, EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @SuppressLint("PrivateApi")
    public static void silentInstallApk(File apkFile, Context context) {
        PackageManager packageManager = context.getPackageManager();
        Class<? extends PackageManager> packageManagerClass = packageManager.getClass();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Class<?> packageInstallObserverClass = Class.forName("android.app.PackageInstallObserver");
                Constructor<?> constructor = packageInstallObserverClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object installObserver = constructor.newInstance();
                Method method = packageManagerClass.getDeclaredMethod("installPackage",
                        Uri.class, packageInstallObserverClass, int.class, String.class);
                method.setAccessible(true);
                method.invoke(packageManager, Uri.fromFile(apkFile), installObserver, 2, null);
            } else {
                Method method = packageManagerClass.getDeclaredMethod("installPackage", Uri.class,
                        Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class);
                method.setAccessible(true);
                method.invoke(packageManager, Uri.fromFile(apkFile), null, 2, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
