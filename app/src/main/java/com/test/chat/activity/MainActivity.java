package com.test.chat.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.test.chat.R;
import com.test.chat.fragment.DynamicFragment;
import com.test.chat.fragment.FriendFragment;
import com.test.chat.fragment.MessageFragment;
import com.test.chat.fragment.MyFragment;
import com.test.chat.util.ActivityUtil;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private static final int BACK_PRESSED_INTERVAL = 2000;
    private static long CURRENT_BACK_PRESSED_TIME = 0;
    private ImageButton message_bottom_ImageButton;
    private ImageButton friend_bottom_ImageButton;
    private ImageButton dynamic_bottom_ImageButton;
    private ImageButton my_bottom_ImageButton;
    private TextView message_bottom_TextView;
    private TextView friend_bottom_TextView;
    private TextView dynamic_bottom_TextView;
    private TextView my_bottom_TextView;
    private MessageFragment messageFragment;
    private FriendFragment friendFragment;
    private DynamicFragment dynamicFragment;
    private MyFragment myFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        LinearLayout message_bottom_LinearLayout = findViewById(R.id.message_bottom_LinearLayout);
        LinearLayout friend_bottom_LinearLayout = findViewById(R.id.friend_bottom_LinearLayout);
        LinearLayout dynamic_bottom_LinearLayout = findViewById(R.id.dynamic_bottom_LinearLayout);
        LinearLayout my_bottom_LinearLayout = findViewById(R.id.my_bottom_LinearLayout);
        message_bottom_TextView = findViewById(R.id.message_bottom_TextView);
        friend_bottom_TextView = findViewById(R.id.friend_bottom_TextView);
        dynamic_bottom_TextView = findViewById(R.id.dynamic_bottom_TextView);
        my_bottom_TextView = findViewById(R.id.my_bottom_TextView);

        message_bottom_LinearLayout.setOnClickListener(this);
        friend_bottom_LinearLayout.setOnClickListener(this);
        dynamic_bottom_LinearLayout.setOnClickListener(this);
        my_bottom_LinearLayout.setOnClickListener(this);

        message_bottom_ImageButton = findViewById(R.id.message_bottom_ImageButton);
        friend_bottom_ImageButton = findViewById(R.id.friend_bottom_ImageButton);
        dynamic_bottom_ImageButton = findViewById(R.id.dynamic_bottom_ImageButton);
        my_bottom_ImageButton = findViewById(R.id.my_bottom_ImageButton);
        setSelect(0);
    }

    @SuppressLint("ResourceAsColor")
    private void setSelect(int i) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragment(transaction);
        switch (i) {
            case 0:
                if (messageFragment == null) {
                    messageFragment = new MessageFragment();
                    transaction.add(R.id.content_FrameLayout, messageFragment);
                } else {
                    transaction.show(messageFragment);
                }
                message_bottom_ImageButton.setBackgroundResource(R.drawable.message_selected);
                message_bottom_TextView.setTextColor(Color.BLUE);
                break;
            case 1:
                if (friendFragment == null) {
                    friendFragment = new FriendFragment();
                    transaction.add(R.id.content_FrameLayout, friendFragment);
                } else {
                    transaction.show(friendFragment);

                }
                friend_bottom_ImageButton.setBackgroundResource(R.drawable.friend_selected);
                friend_bottom_TextView.setTextColor(Color.BLUE);
                break;
            case 2:
                if (dynamicFragment == null) {
                    dynamicFragment = new DynamicFragment();
                    transaction.add(R.id.content_FrameLayout, dynamicFragment);
                } else {
                    transaction.show(dynamicFragment);
                }
                dynamic_bottom_ImageButton.setBackgroundResource(R.drawable.dynamic_selected);
                dynamic_bottom_TextView.setTextColor(Color.BLUE);
                break;
            case 3:
                if (myFragment == null) {
                    myFragment = new MyFragment();
                    transaction.add(R.id.content_FrameLayout, myFragment);
                } else {
                    transaction.show(myFragment);
                }
                my_bottom_ImageButton.setBackgroundResource(R.drawable.my_selected);
                my_bottom_TextView.setTextColor(Color.BLUE);
                break;

            default:
                break;
        }
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (messageFragment != null) {
            fragmentTransaction.hide(messageFragment);
        }
        if (friendFragment != null) {
            fragmentTransaction.hide(friendFragment);
        }
        if (dynamicFragment != null) {
            fragmentTransaction.hide(dynamicFragment);
        }
        if (myFragment != null) {
            fragmentTransaction.hide(myFragment);
        }
    }

    @Override
    public void onClick(View view) {
        setBottomSelectImageButton();
        switch (view.getId()) {
            case R.id.message_bottom_LinearLayout:
                setSelect(0);
                break;
            case R.id.friend_bottom_LinearLayout:
                setSelect(1);
                break;
            case R.id.dynamic_bottom_LinearLayout:
                setSelect(2);
                break;
            case R.id.my_bottom_LinearLayout:
                setSelect(3);
                break;
            default:
                break;
        }
    }

    private void setBottomSelectImageButton() {
        message_bottom_ImageButton.setBackgroundResource(R.drawable.message_normal);
        message_bottom_TextView.setTextColor(Color.BLACK);
        friend_bottom_ImageButton.setBackgroundResource(R.drawable.friend_normal);
        friend_bottom_TextView.setTextColor(Color.BLACK);
        dynamic_bottom_ImageButton.setBackgroundResource(R.drawable.dynamic_normal);
        dynamic_bottom_TextView.setTextColor(Color.BLACK);
        my_bottom_ImageButton.setBackgroundResource(R.drawable.my_normal);
        my_bottom_TextView.setTextColor(Color.BLACK);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - CURRENT_BACK_PRESSED_TIME > BACK_PRESSED_INTERVAL) {
                CURRENT_BACK_PRESSED_TIME = System.currentTimeMillis();
                Toast.makeText(MainActivity.this, "再按一次返回键退出", Toast.LENGTH_LONG).show();
                return false;
            }
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}