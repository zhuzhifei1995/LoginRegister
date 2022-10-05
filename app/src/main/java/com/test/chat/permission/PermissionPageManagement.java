package com.test.chat.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.test.chat.util.ActivityUtil;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PermissionPageManagement {

    private static final String TAG = ActivityUtil.TAG;
    private static final String MANUFACTURER_HUA_WEI = "HUA_WEI";
    private static final String MANUFACTURER_MEI_ZU = "MEI_ZU";
    private static final String MANUFACTURER_XiAO_MI = "XiAO_MI";
    private static final String MANUFACTURER_SONY = "SONY";
    private static final String MANUFACTURER_OP_PO = "OP_PO";
    private static final String MANUFACTURER_LG = "LG";
    private static final String MANUFACTURER_VI_VO = "VI_VO";
    private static final String MANUFACTURER_SAM_SUNG = "SAM_SUNG";
    private static final String MANUFACTURER_ZTE = "ZTE";
    private static final String MANUFACTURER_KU_PAI = "KU_PAI";
    private static final String MANUFACTURER_LEN_OVO = "LEN_OVO";

    public static void goToPermissionSetting(Activity activity) {
        Log.e(TAG, "获取权限的机型名称为: " + Build.MANUFACTURER);
        switch (Build.MANUFACTURER) {
            case MANUFACTURER_HUA_WEI:
                huaWei(activity);
                break;
            case MANUFACTURER_MEI_ZU:
                meiZu(activity);
                break;
            case MANUFACTURER_XiAO_MI:
                xiAoMi(activity);
                break;
            case MANUFACTURER_SONY:
                sony(activity);
                break;
            case MANUFACTURER_OP_PO:
                opPo(activity);
                break;
            case MANUFACTURER_VI_VO:
                viVo(activity);
                break;
            case MANUFACTURER_LG:
                LG(activity);
                break;
            default:
                applicationInfo(activity);
                Log.e(TAG, "目前暂不支持此系统" + Build.MANUFACTURER);
                break;
        }
    }

    public static void huaWei(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getApplicationInfo().packageName);
            ComponentName componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(componentName);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void meiZu(Activity activity) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", activity.getPackageName());
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void xiAoMi(Activity activity) {
        try {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.putExtra("extra_pkgname", activity.getPackageName());
            ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.setComponent(componentName);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void sony(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            ComponentName componentName = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
            intent.setComponent(componentName);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void opPo(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            ComponentName componentName = new ComponentName("com.coloros.securitypermission", "com.coloros.securitypermission.permission.PermissionAppAllPermissionActivity");//R11t 7.1.1 os-v3.2
            intent.setComponent(componentName);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    public static void viVo(Activity activity) {
        Intent localIntent;
        if (((Build.MODEL.contains("Y85")) && (!Build.MODEL.contains("Y85A"))) || (Build.MODEL.contains("vivo Y53L"))) {
            localIntent = new Intent();
            localIntent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity");
            localIntent.putExtra("packagename", activity.getPackageName());
            localIntent.putExtra("tabId", "1");
            activity.startActivity(localIntent);
        } else {
            localIntent = new Intent();
            localIntent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity");
            localIntent.setAction("secure.intent.action.softPermissionDetail");
            localIntent.putExtra("packagename", activity.getPackageName());
            activity.startActivity(localIntent);
        }
    }

    public static void LG(Activity activity) {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", activity.getPackageName());
            ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
            intent.setComponent(componentName);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            goIntentSetting(activity);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public static void applicationInfo(Activity activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        activity.startActivity(localIntent);
    }

    public static void systemConfig(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        activity.startActivity(intent);
    }

    private static void goIntentSetting(Activity pActivity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", pActivity.getPackageName(), null);
        intent.setData(uri);
        try {
            pActivity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}