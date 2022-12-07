package com.test.chat.adapter;

import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FileItemRecyclerViewAdapter extends RecyclerView.Adapter<FileItemRecyclerViewAdapter.FileItemRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private final List<String> fileItemJSONObjectList;

    public FileItemRecyclerViewAdapter(List<String> fileItemJSONObjectList) {
        Log.e(TAG, "初始化ApkRecyclerViewAdapter成功：" + fileItemJSONObjectList.toString());
        this.fileItemJSONObjectList = fileItemJSONObjectList;
    }

    @Override
    public int getItemCount() {
        return fileItemJSONObjectList.size();
    }

    @NonNull
    @Override
    public FileItemRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_listview, viewGroup, false);
        return new FileItemRecyclerViewAdapter.FileItemRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull FileItemRecyclerViewHolder pageRecyclerViewHolder, int position) {
        pageRecyclerViewHolder.textView.setText(fileItemJSONObjectList.get(position));
    }

    public static class FileItemRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public FileItemRecyclerViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView);
        }
    }
}