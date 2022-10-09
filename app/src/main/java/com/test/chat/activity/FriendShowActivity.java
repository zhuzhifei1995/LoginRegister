package com.test.chat.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FriendShowActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private static boolean IS_SHOW_MY_MESSAGE = false;
    private ProgressDialog progressDialog;
    private Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NotNull Message message) {
            super.handleMessage(message);
            progressDialog.dismiss();
            Toast.makeText(FriendShowActivity.this, "获取好友信息成功！", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_show);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        String friendJSON = intent.getStringExtra("friendJSON");
        progressDialog = new ProgressDialog(FriendShowActivity.this);
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setCancelable(false);
            progressDialog.show();
            progressDialog.setContentView(R.layout.loading_progress_bar);
            TextView prompt_TextView = progressDialog.findViewById(R.id.prompt_TextView);
            prompt_TextView.setText("获取好友信息中.......");
        }
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    progressDialog.dismiss();
                    finish();
                    return true;
                }
                return false;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                    waitHandler.sendMessage(new Message());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("好友信息");
        LinearLayout friend_show_LinearLayout = findViewById(R.id.friend_show_LinearLayout);
        friend_show_LinearLayout.setOnClickListener(this);
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(this);
        TextView send_message_TextView = findViewById(R.id.send_message_TextView);
        send_message_TextView.setOnClickListener(this);
        try {
            if (friendJSON != null) {
                JSONObject jsonObject = new JSONObject(friendJSON);
                String id = jsonObject.getString("id");
                String create_time = jsonObject.getString("create_time");
                String login_number = jsonObject.getString("login_number");
                String nick_name = jsonObject.getString("nick_name");
                String password = jsonObject.getString("password");
                String phone = jsonObject.getString("phone");
                String photo = jsonObject.getString("photo");

                SharedPreferencesUtils.putString(FriendShowActivity.this, "create_time_friend", create_time, "user");
                SharedPreferencesUtils.putString(FriendShowActivity.this, "password_friend", password, "user");
                SharedPreferencesUtils.putString(FriendShowActivity.this, "id_friend", id, "user");
                SharedPreferencesUtils.putString(FriendShowActivity.this, "login_number_friend", login_number, "user");
                SharedPreferencesUtils.putString(FriendShowActivity.this, "nick_name_friend", nick_name, "user");
                SharedPreferencesUtils.putString(FriendShowActivity.this, "phone_friend", phone, "user");
                SharedPreferencesUtils.putString(FriendShowActivity.this, "photo_friend", photo, "user");

                String[] photos = jsonObject.getString("photo").split("/");
                String friendPhotoName = photos[photos.length - 1] + ".cache";
                Bitmap bitmap = ImageUtil.getBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/tmp/friend", friendPhotoName);
                ImageView friend_photo_show_ImageView = findViewById(R.id.friend_photo_show_ImageView);
                friend_photo_show_ImageView.setImageBitmap(bitmap);
                friend_photo_show_ImageView.setOnClickListener(this);
                TextView friend_nike_name_TextView = findViewById(R.id.friend_nike_name_TextView);
                if (!nick_name.equals("")) {
                    friend_nike_name_TextView.setText(nick_name);
                } else {
                    friend_nike_name_TextView.setText("未设置昵称");
                }
                TextView friend_login_number_TextView = findViewById(R.id.friend_login_number_TextView);
                friend_login_number_TextView.setText(login_number);
                TextView friend_phone_number_TextView = findViewById(R.id.friend_phone_number_TextView);
                friend_phone_number_TextView.setText(phone);
                TextView friend_create_time_TextView = findViewById(R.id.friend_create_time_TextView);
                friend_create_time_TextView.setText(create_time);
                TextView friend_password_TextView = findViewById(R.id.friend_password_TextView);
                friend_password_TextView.setText(password);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showFriendPhoto() {
        Intent intent = new Intent(FriendShowActivity.this, PhotoShowActivity.class);
        intent.putExtra("flag", 0);
        String[] photos = SharedPreferencesUtils.getString(
                FriendShowActivity.this, "photo_friend", "", "user").split("/");
        String friendPhotoName = photos[photos.length - 1] + ".cache";
        intent.putExtra("photoName", friendPhotoName);
        startActivity(intent);
    }

    private void showMyMessage() {
        ImageView message_show_ImageView = findViewById(R.id.message_show_ImageView);
        LinearLayout friend_show_message_LinearLayout = findViewById(R.id.friend_show_message_LinearLayout);
        View line_message_View = findViewById(R.id.line_message_View);
        if (IS_SHOW_MY_MESSAGE) {
            message_show_ImageView.setImageResource(R.drawable.message_no_show);
            friend_show_message_LinearLayout.setVisibility(View.GONE);
            line_message_View.setVisibility(View.GONE);
            IS_SHOW_MY_MESSAGE = false;
        } else {
            message_show_ImageView.setImageResource(R.drawable.message_show);
            friend_show_message_LinearLayout.setVisibility(View.VISIBLE);
            line_message_View.setVisibility(View.VISIBLE);
            IS_SHOW_MY_MESSAGE = true;
        }
    }

    private void sendMessage() {
        Intent intent = new Intent(FriendShowActivity.this, ChatFriendActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        Log.e(TAG, "好友显示界面的内容被点击：" + view.getId());
        switch (view.getId()) {
            case R.id.title_left_ImageView:
                finish();
                break;
            case R.id.friend_show_LinearLayout:
                showMyMessage();
                break;
            case R.id.send_message_TextView:
                sendMessage();
                break;
            case R.id.friend_photo_show_ImageView:
                showFriendPhoto();
                break;
            default:
                break;
        }
    }

}