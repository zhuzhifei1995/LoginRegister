package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private DownloadFileImageViewOnItemClickListener downloadFileImageViewOnItemClickListener;
    private DeleteFileImageViewOnItemClickListener deleteFileImageViewOnItemClickListener;
    private UpdateFileImageViewOnItemClickListener updateFileImageViewOnItemClickListener;
    private FileDetailLinearLayoutOnItemClickListener fileDetailLinearLayoutOnItemClickListener;

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
                int downloadFlag = jsonObject.getInt("download_flag");
                fileRecyclerViewHolder.file_name_TextView.setText(fileName);
                fileRecyclerViewHolder.file_size_TextView.setText(new String("文件大小：" + fileSize));
                if (downloadFlag == 0) {
                    fileRecyclerViewHolder.download_ImageView.setVisibility(View.VISIBLE);
                    fileRecyclerViewHolder.delete_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.update_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.downing_ImageView.setVisibility(View.GONE);
                } else if (downloadFlag == 1) {
                    fileRecyclerViewHolder.download_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.delete_ImageView.setVisibility(View.VISIBLE);
                    fileRecyclerViewHolder.update_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.downing_ImageView.setVisibility(View.GONE);
                } else if (downloadFlag == 2) {
                    fileRecyclerViewHolder.download_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.delete_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.update_ImageView.setVisibility(View.VISIBLE);
                    fileRecyclerViewHolder.downing_ImageView.setVisibility(View.GONE);
                } else if (downloadFlag == 3) {
                    fileRecyclerViewHolder.download_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.delete_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.update_ImageView.setVisibility(View.GONE);
                    fileRecyclerViewHolder.downing_ImageView.setVisibility(View.VISIBLE);
                }
                fileRecyclerViewHolder.download_ImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (downloadFileImageViewOnItemClickListener != null) {
                            downloadFileImageViewOnItemClickListener.onItemClick(position);
                        } else {
                            Log.e(TAG, "onClick: 不能点击");
                        }
                    }
                });
                fileRecyclerViewHolder.delete_ImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (deleteFileImageViewOnItemClickListener != null) {
                            deleteFileImageViewOnItemClickListener.onItemClick(position);
                        } else {
                            Log.e(TAG, "onClick: 不能点击");
                        }
                    }
                });
                fileRecyclerViewHolder.update_ImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (updateFileImageViewOnItemClickListener != null) {
                            updateFileImageViewOnItemClickListener.onItemClick(position);
                        } else {
                            Log.e(TAG, "onClick: 不能点击");
                        }
                    }
                });
                fileRecyclerViewHolder.root_file_LinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (fileDetailLinearLayoutOnItemClickListener != null) {
                            fileDetailLinearLayoutOnItemClickListener.onItemClick(position);
                        } else {
                            Log.e(TAG, "onClick: 不能点击");
                        }
                    }
                });
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

    public void setOnDownloadFileImageViewClickListener(FileRecyclerViewAdapter.DownloadFileImageViewOnItemClickListener downloadFileImageViewOnItemClickListener) {
        this.downloadFileImageViewOnItemClickListener = downloadFileImageViewOnItemClickListener;
    }

    public void setOnDeleteFileImageViewClickListener(FileRecyclerViewAdapter.DeleteFileImageViewOnItemClickListener deleteFileImageViewOnItemClickListener) {
        this.deleteFileImageViewOnItemClickListener = deleteFileImageViewOnItemClickListener;
    }

    public void setOnUpdateFileImageViewClickListener(FileRecyclerViewAdapter.UpdateFileImageViewOnItemClickListener updateFileImageViewOnItemClickListener) {
        this.updateFileImageViewOnItemClickListener = updateFileImageViewOnItemClickListener;
    }

    public void setOnFileDetailLinearLayoutClickListener(FileRecyclerViewAdapter.FileDetailLinearLayoutOnItemClickListener fileDetailLinearLayoutOnItemClickListener) {
        this.fileDetailLinearLayoutOnItemClickListener = fileDetailLinearLayoutOnItemClickListener;
    }

    public interface DownloadFileImageViewOnItemClickListener {
        void onItemClick(int position);
    }

    public interface DeleteFileImageViewOnItemClickListener {
        void onItemClick(int position);
    }

    public interface UpdateFileImageViewOnItemClickListener {
        void onItemClick(int position);
    }

    public interface FileDetailLinearLayoutOnItemClickListener {
        void onItemClick(int position);
    }

    public static class FileRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView file_name_TextView;
        private final TextView file_size_TextView;
        private final ImageView delete_ImageView;
        private final ImageView download_ImageView;
        private final ImageView update_ImageView;
        private final ProgressBar downing_ImageView;
        private final LinearLayout root_file_LinearLayout;

        public FileRecyclerViewHolder(View view) {
            super(view);
            file_name_TextView = view.findViewById(R.id.file_name_TextView);
            file_size_TextView = view.findViewById(R.id.file_size_TextView);
            delete_ImageView = view.findViewById(R.id.delete_ImageView);
            download_ImageView = view.findViewById(R.id.download_ImageView);
            update_ImageView = view.findViewById(R.id.update_ImageView);
            downing_ImageView = view.findViewById(R.id.downing_ProgressBar);
            root_file_LinearLayout = view.findViewById(R.id.root_file_LinearLayout);
        }
    }
}