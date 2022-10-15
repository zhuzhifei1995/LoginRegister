package com.test.chat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class LoginActivity extends Activity implements OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private static final int BACK_PRESSED_INTERVAL = 2000;
    private static long CURRENT_BACK_PRESSED_TIME = 0;
    private static boolean MORE_UP_IS_SHOW = false;
    private LinearLayout menu_more_LinearLayout;
    private ImageView more_up_ImageView;
    private RelativeLayout root_RelativeLayout;
    private ProgressDialog progressDialog;
    private String ANDROID_ID;
    private CheckBox remember_password_CheckBox;
    private Context context;
    private final Handler loginHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.getString("code").equals("1")) {
                    JSONObject user = jsonObject.getJSONObject("message");

                    SharedPreferencesUtils.putString(context, "create_time", user.getString("create_time"), "user");
                    SharedPreferencesUtils.putString(context, "password", user.getString("password"), "user");
                    SharedPreferencesUtils.putString(context, "id", user.getInt("id") + "", "user");
                    SharedPreferencesUtils.putString(context, "login_number", user.getString("login_number"), "user");
                    SharedPreferencesUtils.putString(context, "nick_name", user.getString("nick_name"), "user");
                    SharedPreferencesUtils.putString(context, "phone", user.getString("phone"), "user");
                    SharedPreferencesUtils.putBoolean(context, "status", true, "user");
                    SharedPreferencesUtils.putBoolean(context, "is_remember_password", remember_password_CheckBox.isChecked(), "user");
                    SharedPreferencesUtils.putString(context, "photo", user.getString("photo"), "user");

                    saveUserPhoto(user.getString("photo"));

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("status", jsonObject.getString("status"));
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(context, jsonObject.getString("status"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(context, "网络异常，登录失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }
    };
    private EditText login_account_EditText;
    private EditText login_password_EditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ANDROID_ID = android.provider.Settings.System.getString(getContentResolver(), "android_id");
        Log.e(TAG, "获取设备的 ANDROID_ID：" + ANDROID_ID);
        if (SharedPreferencesUtils.getBoolean(context, "status", false, "user")) {
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_login);
            initView();
        }

    }

    private void initView() {
        context = this;
        menu_more_LinearLayout = findViewById(R.id.menu_more_LinearLayout);
        Button login_register_Button = findViewById(R.id.login_register_Button);
        login_register_Button.setOnClickListener(this);
        more_up_ImageView = findViewById(R.id.more_up_ImageView);
        RelativeLayout view_more_RelativeLayout = findViewById(R.id.view_more_RelativeLayout);
        view_more_RelativeLayout.setOnClickListener(this);
        root_RelativeLayout = findViewById(R.id.root_RelativeLayout);
        Button login_Button = findViewById(R.id.login_Button);
        login_Button.setOnClickListener(this);
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        remember_password_CheckBox = findViewById(R.id.remember_password_CheckBox);
        boolean isRememberPassword = SharedPreferencesUtils.getBoolean(context, "is_remember_password", false, "user");
        remember_password_CheckBox.setChecked(isRememberPassword);

        login_account_EditText = findViewById(R.id.login_account_EditText);
        login_password_EditText = findViewById(R.id.login_password_EditText);

        login_account_EditText.setText(SharedPreferencesUtils.getString(context, "login_number", "", "user"));
        login_account_EditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                login_account_EditText.setSelection(login_account_EditText.getText().length());
                login_account_EditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.showSoftInput(getCurrentFocus(), 0);
                }
            }
        }, 100);

        if (isRememberPassword) {
            login_password_EditText.setText(SharedPreferencesUtils.getString(context, "password", "", "user"));
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - CURRENT_BACK_PRESSED_TIME > BACK_PRESSED_INTERVAL) {
            CURRENT_BACK_PRESSED_TIME = System.currentTimeMillis();
            Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Log.e(TAG, "登录界面的内容被点击：" + view.getId());
        switch (view.getId()) {
            case R.id.view_more_RelativeLayout:
                showMoreMenu(MORE_UP_IS_SHOW);
                break;
            case R.id.login_register_Button:
                register();
                break;
            case R.id.login_Button:
                login();
                break;
        }
    }

    @Override
    protected void onPause() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onPause();
    }

    private void register() {
        Intent intent = new Intent(context, RegisterActivity.class);
        startActivity(intent);
    }

    private void login() {
        // TODO
//        login_account_EditText.setText(new String("20220922234321"));
//        login_password_EditText.setText(new String("123456789"));
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(login_account_EditText.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(login_password_EditText.getWindowToken(), 0);

        final String login_account = login_account_EditText.getText().toString();
        final String login_password = login_password_EditText.getText().toString();

        if (login_account.equals("")) {
            Toast.makeText(this, "登录名不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (login_password.equals("")) {
            Toast.makeText(this, "登录密码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        Window window = progressDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        progressDialog.setContentView(R.layout.loading_progress_bar);
        TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
        prompt_TextView.setText("登陆中.......");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> parameter = new HashMap<>();
                parameter.put("login_number", login_account);
                parameter.put("password", login_password);
                parameter.put("android_id", ANDROID_ID);
                Message message = new Message();
                message.obj = new HttpUtil(context)
                        .postRequest(ActivityUtil.NET_URL + "/login_user", parameter);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loginHandler.sendMessage(message);
            }
        }).start();
    }

    private void saveUserPhoto(final String photo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap photoBitmap = new HttpUtil(context).getImageBitmap(photo);
                if (photoBitmap != null) {
                    ImageUtil.saveBitmapToTmpFile(photoBitmap, Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache");
                }
            }
        }).start();
    }

    private void showMoreMenu(boolean show) {
        if (show) {
            menu_more_LinearLayout.setVisibility(View.GONE);
            more_up_ImageView.setImageResource(R.drawable.login_more_up);
            MORE_UP_IS_SHOW = false;
        } else {
            menu_more_LinearLayout.setVisibility(View.VISIBLE);
            more_up_ImageView.setImageResource(R.drawable.login_more);
            MORE_UP_IS_SHOW = true;
        }
        root_RelativeLayout.invalidate();
    }
}
