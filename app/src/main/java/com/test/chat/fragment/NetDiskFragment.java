package com.test.chat.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.test.chat.R;
import com.test.chat.activity.LoginActivity;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.view.TitleFragmentPagerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class NetDiskFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private View netDiskFragmentView;
    private Context context;
    private Activity activity;

    public NetDiskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        netDiskFragmentView = layoutInflater.inflate(R.layout.fragment_net_disk, viewGroup, false);
        initFragmentView();
        return netDiskFragmentView;
    }

    private void initFragmentView() {
        ActivityUtil.setLinearLayoutBackground(netDiskFragmentView.findViewById(R.id.net_disk_main_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        TextView top_title_TextView = netDiskFragmentView.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("我的网盘");
        ImageView title_left_ImageView = netDiskFragmentView.findViewById(R.id.title_left_ImageView);
        Bitmap bitmap = ImageUtil.getBitmapFromFile(ActivityUtil.TMP_USER_FILE_PATH, "photo.png.cache");
        if (bitmap != null) {
            Log.e(TAG, "头像图片加载正常");
            title_left_ImageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "图片加载失败,图片为空");
            Toast.makeText(context, "登录信息失效，请重新登录！", Toast.LENGTH_SHORT).show();
            title_left_ImageView.setImageResource(R.drawable.user_default_photo);
            SharedPreferencesUtils.putBoolean(context, "status", false, "user");
            startActivity(new Intent(context, LoginActivity.class));
            activity.finish();
        }
        TabLayout net_disk_title_TabLayout = netDiskFragmentView.findViewById(R.id.net_disk_title_TabLayout);
        ViewPager net_disk_content_ViewPager = netDiskFragmentView.findViewById(R.id.net_disk_content_ViewPager);
        List<Fragment> fragments = new ArrayList<>();
        NetDiskFileFragment netDiskFileFragment = new NetDiskFileFragment();
        BlankFragment blankFragment = new BlankFragment();
        fragments.add(netDiskFileFragment);
        fragments.add(blankFragment);
        TitleFragmentPagerView titleFragmentPagerView = new TitleFragmentPagerView(getChildFragmentManager(), fragments);
        net_disk_content_ViewPager.setAdapter(titleFragmentPagerView);
        net_disk_title_TabLayout.setupWithViewPager(net_disk_content_ViewPager);
        Objects.requireNonNull(net_disk_title_TabLayout.getTabAt(0)).setText("在线网盘");
        Objects.requireNonNull(net_disk_title_TabLayout.getTabAt(1)).setText("本地文件");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getActivity();
        activity = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        ActivityUtil.setLinearLayoutBackground(netDiskFragmentView.findViewById(R.id.net_disk_main_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        super.onHiddenChanged(hidden);
    }
}