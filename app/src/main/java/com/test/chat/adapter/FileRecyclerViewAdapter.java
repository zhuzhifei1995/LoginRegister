package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.FileRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private final List<JSONObject> jsonObjectList;

    public FileRecyclerViewAdapter(List<JSONObject> jsonObjectList) {
        Log.e(TAG, "初始化FriendRecyclerViewAdapter成功：" + jsonObjectList.toString());
        this.jsonObjectList = jsonObjectList;
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NotNull FileRecyclerViewHolder fileRecyclerViewHolder, int position) {
        JSONObject jsonObject = jsonObjectList.get(position);
        try {
            if (jsonObject != null) {
                String fileName = jsonObject.getString("file_name");
                String fileSize = jsonObject.getString("file_size");
                fileRecyclerViewHolder.file_name_TextView.setText(fileName);
                fileRecyclerViewHolder.file_size_TextView.setText(new String("文件大小："+fileSize));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public FileRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_list_view, viewGroup, false);
        return new FileRecyclerViewHolder(view);
    }

    public static class FileRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView file_name_TextView;
        private final TextView file_size_TextView;

        public FileRecyclerViewHolder(View view) {
            super(view);
            file_name_TextView = view.findViewById(R.id.file_name_TextView);
            file_size_TextView = view.findViewById(R.id.file_size_TextView);
        }
    }
}