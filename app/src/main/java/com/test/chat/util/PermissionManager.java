package com.test.chat.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PermissionManager {

    private static final String TAG = Utils.TAG;

    public static void requestPermission(final Context context, final Callback callback, String... permissions) {
        Log.e(TAG, "requestPermission: ");
        AndPermission.with(context)
                .permission(permissions)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (callback != null) {
                            callback.permissionSuccess();

                        }
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (callback != null)
                            callback.permissionFailed();
                        AndPermission.hasAlwaysDeniedPermission(context, permissions);
                    }
                })
                .start();
    }

    public interface Callback {
        void permissionSuccess();

        void permissionFailed();
    }
}
