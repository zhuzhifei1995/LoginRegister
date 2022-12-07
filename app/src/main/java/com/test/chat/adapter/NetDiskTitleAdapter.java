package com.test.chat.adapter;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.test.chat.fragment.BlankFragment;
import com.test.chat.fragment.FileUploadFragment;
import com.test.chat.fragment.NetDiskFileFragment;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class NetDiskTitleAdapter extends FragmentStateAdapter {

    private final List<String> netDiskTitle;

    public NetDiskTitleAdapter(@NonNull FragmentActivity fragmentActivity, List<String> netDiskTitle) {
        super(fragmentActivity);
        this.netDiskTitle = netDiskTitle;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return NetDiskFileFragment.newInstance(netDiskTitle.get(position));
        } else if (position == 2) {
            return FileUploadFragment.newInstance(netDiskTitle.get(position));
        } else {
            return BlankFragment.newInstance(netDiskTitle.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return netDiskTitle.size();
    }
}