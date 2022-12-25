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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.test.chat.R;
import com.test.chat.activity.LoginActivity;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.view.TitleAdapterView;

import java.util.ArrayList;
import java.util.List;

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

    private void initTitleView() {
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
    }

    private void initFragmentView() {
        ActivityUtil.setLinearLayoutBackground(netDiskFragmentView.findViewById(R.id.net_disk_main_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        TextView top_title_TextView = netDiskFragmentView.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("我的网盘");
        initTitleView();
        TabLayout net_disk_title_TabLayout = netDiskFragmentView.findViewById(R.id.net_disk_title_TabLayout);
        ViewPager2 net_disk_content_ViewPager2 = netDiskFragmentView.findViewById(R.id.net_disk_content_ViewPager2);
        List<String> netDiskTitle = new ArrayList<>();
        netDiskTitle.add("网盘文件");
        netDiskTitle.add("本地已下载文件");
        netDiskTitle.add("在线视频");
        netDiskTitle.add("我的漫画");
        netDiskTitle.add("上传文件");
        List<Fragment> fragmentList = new ArrayList<>();
        NetDiskFileFragment netDiskFileFragment = NetDiskFileFragment.newInstance(netDiskTitle.get(0));
        fragmentList.add(netDiskFileFragment);
        BlankFragment blankFragment = BlankFragment.newInstance(netDiskTitle.get(1));
        fragmentList.add(blankFragment);
        NetVideoFragment netVideoFragment = NetVideoFragment.newInstance(netDiskTitle.get(2));
        fragmentList.add(netVideoFragment);
        ComicFragment comicFragment = ComicFragment.newInstance(netDiskTitle.get(3));
        fragmentList.add(comicFragment);
        FileUploadFragment fileUploadFragment = FileUploadFragment.newInstance(netDiskTitle.get(4));
        fragmentList.add(fileUploadFragment);
        TitleAdapterView netDiskTitleAdapterView = new TitleAdapterView(requireActivity(), fragmentList);
        net_disk_content_ViewPager2.setAdapter(netDiskTitleAdapterView);
        net_disk_content_ViewPager2.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                net_disk_content_ViewPager2.setUserInputEnabled(false);
            }
        });
        new TabLayoutMediator(net_disk_title_TabLayout, net_disk_content_ViewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(netDiskTitle.get(position));
            }
        }).attach();
        for (int i = 0; i < net_disk_title_TabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = net_disk_title_TabLayout.getTabAt(i);
            if (tab != null) {
                tab.view.setLongClickable(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tab.view.setTooltipText(null);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        activity = getActivity();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        ActivityUtil.setLinearLayoutBackground(netDiskFragmentView.findViewById(R.id.net_disk_main_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        initTitleView();
        super.onHiddenChanged(hidden);
    }
}