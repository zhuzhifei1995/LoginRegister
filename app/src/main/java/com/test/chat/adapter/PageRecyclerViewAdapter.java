package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressLint("RecyclerView")
public class PageRecyclerViewAdapter extends RecyclerView.Adapter<PageRecyclerViewAdapter.PageRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private final List<Integer> pageList;
    private PageRecyclerViewAdapter.PageSelectOnItemClickListener pageSelectOnItemClickListener;

    public PageRecyclerViewAdapter(List<Integer> pageList) {
        Log.e(TAG, "初始化ApkRecyclerViewAdapter成功：" + pageList.toString());
        this.pageList = pageList;
    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }

    @NonNull
    @Override
    public PageRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.page_show_list_view, viewGroup, false);
        return new PageRecyclerViewAdapter.PageRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull PageRecyclerViewHolder pageRecyclerViewHolder, int position) {
        pageRecyclerViewHolder.page_TextView.setText(String.valueOf(pageList.get(position)));
        Log.e(TAG, "onBindViewHolder: " + (position == getItemCount() - 1));
        if (position == getItemCount() - 1) {
            pageRecyclerViewHolder.page_main_LinearLayout.setBackgroundResource(R.drawable.button_bottom_bg);
        }
        pageRecyclerViewHolder.page_main_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageSelectOnItemClickListener.onItemClick(position);
            }
        });
    }

    public void setOnPageSelectOnItemClickListener(PageRecyclerViewAdapter.PageSelectOnItemClickListener pageSelectOnItemClickListener) {
        this.pageSelectOnItemClickListener = pageSelectOnItemClickListener;
    }

    public interface PageSelectOnItemClickListener {
        void onItemClick(int position);
    }

    public static class PageRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView page_TextView;
        private final LinearLayout page_main_LinearLayout;

        public PageRecyclerViewHolder(View view) {
            super(view);
            page_TextView = view.findViewById(R.id.page_TextView);
            page_main_LinearLayout = view.findViewById(R.id.page_main_LinearLayout);
        }
    }
}