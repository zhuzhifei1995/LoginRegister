package com.test.chat.adapter;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FileListViewAdapter extends BaseAdapter {

    private static final String TAG = ActivityUtil.TAG;
    private final List<JSONObject> fileJSONObjectList;
    private final int resourceId;
    private final LayoutInflater layoutInflater;
    private FileListViewAdapter.DownloadFileImageViewOnItemClickListener downloadFileImageViewOnItemClickListener;
    private FileListViewAdapter.DeleteFileImageViewOnItemClickListener deleteFileImageViewOnItemClickListener;
    private FileListViewAdapter.UpdateFileImageViewOnItemClickListener updateFileImageViewOnItemClickListener;
    private FileListViewAdapter.FileDetailLinearLayoutOnItemClickListener fileDetailLinearLayoutOnItemClickListener;

    public FileListViewAdapter(Context context, int resourceId, List<JSONObject> fileJSONObjectList) {
        layoutInflater = LayoutInflater.from(context);
        this.resourceId = resourceId;
        this.fileJSONObjectList = fileJSONObjectList;
    }

    @Override
    public int getCount() {
        return fileJSONObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileJSONObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        FileListViewAdapter.FileListViewHolder fileListViewHolder = null;
        if (view == null) {
            fileListViewHolder = new FileListViewAdapter.FileListViewHolder();
            view = layoutInflater.inflate(resourceId, viewGroup, false);
            fileListViewHolder.file_name_TextView = (TextView) view.findViewById(R.id.file_name_TextView);
            fileListViewHolder.file_size_TextView = (TextView) view.findViewById(R.id.file_size_TextView);
            fileListViewHolder.delete_ImageView = (ImageView) view.findViewById(R.id.delete_ImageView);
            fileListViewHolder.download_ImageView = (ImageView) view.findViewById(R.id.download_ImageView);
            fileListViewHolder.update_ImageView = (ImageView) view.findViewById(R.id.update_ImageView);
            fileListViewHolder.downing_ProgressBar = (ProgressBar) view.findViewById(R.id.downing_ProgressBar);
            fileListViewHolder.root_file_LinearLayout = (LinearLayout) view.findViewById(R.id.root_file_LinearLayout);
            view.setTag(fileListViewHolder);
        } else {
            fileListViewHolder = (FileListViewAdapter.FileListViewHolder) view.getTag();
        }
        try {
            JSONObject jsonObject = fileJSONObjectList.get(position);
            fileListViewHolder.file_name_TextView.setText(jsonObject.getString("file_name"));
            String fileSize = "文件大小：" + jsonObject.getString("file_size");
            fileListViewHolder.file_size_TextView.setText(fileSize);
            int downloadFlag = jsonObject.getInt("download_flag");
            if (downloadFlag == 0) {
                fileListViewHolder.download_ImageView.setVisibility(View.VISIBLE);
                fileListViewHolder.delete_ImageView.setVisibility(View.GONE);
                fileListViewHolder.update_ImageView.setVisibility(View.GONE);
                fileListViewHolder.downing_ProgressBar.setVisibility(View.GONE);
            } else if (downloadFlag == 1) {
                fileListViewHolder.download_ImageView.setVisibility(View.GONE);
                fileListViewHolder.delete_ImageView.setVisibility(View.VISIBLE);
                fileListViewHolder.update_ImageView.setVisibility(View.GONE);
                fileListViewHolder.downing_ProgressBar.setVisibility(View.GONE);
            } else if (downloadFlag == 2) {
                fileListViewHolder.download_ImageView.setVisibility(View.GONE);
                fileListViewHolder.delete_ImageView.setVisibility(View.GONE);
                fileListViewHolder.update_ImageView.setVisibility(View.VISIBLE);
                fileListViewHolder.downing_ProgressBar.setVisibility(View.GONE);
            } else if (downloadFlag == 3) {
                fileListViewHolder.download_ImageView.setVisibility(View.GONE);
                fileListViewHolder.delete_ImageView.setVisibility(View.GONE);
                fileListViewHolder.update_ImageView.setVisibility(View.GONE);
                fileListViewHolder.downing_ProgressBar.setVisibility(View.VISIBLE);
            }
            fileListViewHolder.download_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (downloadFileImageViewOnItemClickListener != null) {
                        downloadFileImageViewOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
            fileListViewHolder.delete_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deleteFileImageViewOnItemClickListener != null) {
                        deleteFileImageViewOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
            fileListViewHolder.update_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (updateFileImageViewOnItemClickListener != null) {
                        updateFileImageViewOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
            fileListViewHolder.root_file_LinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (fileDetailLinearLayoutOnItemClickListener != null) {
                        fileDetailLinearLayoutOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public void setOnDownloadFileImageViewClickListener(FileListViewAdapter.DownloadFileImageViewOnItemClickListener downloadFileImageViewOnItemClickListener) {
        this.downloadFileImageViewOnItemClickListener = downloadFileImageViewOnItemClickListener;
    }

    public void setOnDeleteFileImageViewClickListener(FileListViewAdapter.DeleteFileImageViewOnItemClickListener deleteFileImageViewOnItemClickListener) {
        this.deleteFileImageViewOnItemClickListener = deleteFileImageViewOnItemClickListener;
    }

    public void setOnUpdateFileImageViewClickListener(FileListViewAdapter.UpdateFileImageViewOnItemClickListener updateFileImageViewOnItemClickListener) {
        this.updateFileImageViewOnItemClickListener = updateFileImageViewOnItemClickListener;
    }

    public void setOnFileDetailLinearLayoutClickListener(FileListViewAdapter.FileDetailLinearLayoutOnItemClickListener fileDetailLinearLayoutOnItemClickListener) {
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

    private static class FileListViewHolder {
        private TextView file_name_TextView;
        private TextView file_size_TextView;
        private ImageView delete_ImageView;
        private ImageView download_ImageView;
        private ImageView update_ImageView;
        private ProgressBar downing_ProgressBar;
        private LinearLayout root_file_LinearLayout;
    }
}
