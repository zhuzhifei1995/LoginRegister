package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
public class FileItemRecyclerViewAdapter extends RecyclerView.Adapter<FileItemRecyclerViewAdapter.FileItemRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private final List<String> fileItemJSONObjectList;
    private final Context context;
    private FileItemTextViewOnItemClickListener fileItemTextViewOnItemClickListener;

    public FileItemRecyclerViewAdapter(Context context, List<String> fileItemJSONObjectList) {
        Log.e(TAG, "初始化ApkRecyclerViewAdapter成功：" + fileItemJSONObjectList.toString());
        this.fileItemJSONObjectList = fileItemJSONObjectList;
        this.context = context;
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
        pageRecyclerViewHolder.file_name_TextView.setText(fileItemJSONObjectList.get(position));
        if (position == getItemCount() - 1) {
            pageRecyclerViewHolder.file_LinearLayout.setBackgroundColor(context.getColor(R.color.gray));
        } else {
            pageRecyclerViewHolder.file_LinearLayout.setBackgroundColor(context.getColor(R.color.no_color));
        }
        pageRecyclerViewHolder.file_name_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileItemTextViewOnItemClickListener != null) {
                    fileItemTextViewOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    public void setOnFileItemTextViewClickListener(FileItemTextViewOnItemClickListener fileItemTextViewOnItemClickListener) {
        this.fileItemTextViewOnItemClickListener = fileItemTextViewOnItemClickListener;
    }

    public interface FileItemTextViewOnItemClickListener {
        void onItemClick(int position);
    }

    public static class FileItemRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView file_name_TextView;
        private final LinearLayout file_LinearLayout;

        public FileItemRecyclerViewHolder(View view) {
            super(view);
            file_name_TextView = view.findViewById(R.id.file_name_TextView);
            file_LinearLayout = view.findViewById(R.id.file_LinearLayout);
        }
    }
}