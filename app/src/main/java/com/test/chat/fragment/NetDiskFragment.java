package com.test.chat.fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.test.chat.R;
import com.test.chat.adapter.NetDiskFragmentPagerAdapter;
import com.test.chat.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class NetDiskFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private View netDiskFragmentView;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        netDiskFragmentView = layoutInflater.inflate(R.layout.fragment_net_disk, viewGroup, false);
        initFragmentView();
        return netDiskFragmentView;
    }

    private void initFragmentView() {
        TabLayout title_TabLayout = netDiskFragmentView.findViewById(R.id.title_TabLayout);
        ViewPager content_ViewPager = netDiskFragmentView.findViewById(R.id.content_ViewPager);

        List<Fragment> fragments = new ArrayList<>();
        NetDiskFileFragment netDiskFileFragment = new NetDiskFileFragment();
        BlankFragment blankFragment = new BlankFragment();
        fragments.add(netDiskFileFragment);
        fragments.add(blankFragment);

        NetDiskFragmentPagerAdapter netDiskFragmentPagerAdapter = new NetDiskFragmentPagerAdapter(requireActivity().getSupportFragmentManager(), fragments);
        content_ViewPager.setAdapter(netDiskFragmentPagerAdapter);
        title_TabLayout.setupWithViewPager(content_ViewPager);

        Objects.requireNonNull(title_TabLayout.getTabAt(0)).setText("在线网盘");
        Objects.requireNonNull(title_TabLayout.getTabAt(1)).setText("我的下载");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        super.onHiddenChanged(hidden);
    }
}