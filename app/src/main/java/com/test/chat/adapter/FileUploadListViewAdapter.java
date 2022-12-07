package com.test.chat.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FileUploadListViewAdapter extends BaseAdapter {

    private static final String TAG = ActivityUtil.TAG;
    private final int resourceId;
    private final List<JSONObject> fileJSONObjectList;
    private final LayoutInflater layoutInflater;
    private FileDetailLinearLayoutOnItemClickListener fileDetailLinearLayoutOnItemClickListener;
    private FileDetailLinearLayoutOnItemLongClickListener fileDetailLinearLayoutOnItemLongClickListener;

    public FileUploadListViewAdapter(Context context, int resourceId, List<JSONObject> fileJSONObjectList) {
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
        FileUploadListViewAdapter.FileUploadListViewHolder fileUploadListViewHolder = null;
        if (view == null) {
            fileUploadListViewHolder = new FileUploadListViewAdapter.FileUploadListViewHolder();
            view = layoutInflater.inflate(resourceId, viewGroup, false);
            fileUploadListViewHolder.file_type_ImageView = view.findViewById(R.id.file_type_ImageView);
            fileUploadListViewHolder.file_name_TextView = view.findViewById(R.id.file_name_TextView);
            fileUploadListViewHolder.next_dir_ImageView = view.findViewById(R.id.next_dir_ImageView);
            fileUploadListViewHolder.file_LinearLayout = view.findViewById(R.id.file_LinearLayout);
            view.setTag(fileUploadListViewHolder);
        } else {
            fileUploadListViewHolder = (FileUploadListViewAdapter.FileUploadListViewHolder) view.getTag();
        }
        JSONObject jsonObject = fileJSONObjectList.get(position);
        try {
            int fileType = jsonObject.getInt("file_type");
            String fileName = jsonObject.getString("file_name");
            if (fileType == 0) {
                fileUploadListViewHolder.file_type_ImageView.setImageResource(R.drawable.dir_image);
                fileUploadListViewHolder.next_dir_ImageView.setVisibility(View.VISIBLE);
            } else {
                fileUploadListViewHolder.file_type_ImageView.setImageResource(R.drawable.file_image);
                fileUploadListViewHolder.next_dir_ImageView.setVisibility(View.GONE);
            }
            fileUploadListViewHolder.file_name_TextView.setText(fileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fileUploadListViewHolder.file_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileDetailLinearLayoutOnItemClickListener != null) {
                    fileDetailLinearLayoutOnItemClickListener.onItemClick(position);
                }
            }
        });
        fileUploadListViewHolder.file_LinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (fileDetailLinearLayoutOnItemLongClickListener != null) {
                    fileDetailLinearLayoutOnItemLongClickListener.onItemLongClick(position);
                }
                return true;
            }
        });
        return view;
    }

    public void setOnFileDetailLinearLayoutClickListener(FileUploadListViewAdapter.FileDetailLinearLayoutOnItemClickListener fileDetailLinearLayoutOnItemClickListener) {
        this.fileDetailLinearLayoutOnItemClickListener = fileDetailLinearLayoutOnItemClickListener;
    }

    public void setOnFileDetailLinearLayoutLongClickListener(FileUploadListViewAdapter.FileDetailLinearLayoutOnItemLongClickListener fileDetailLinearLayoutOnItemLongClickListener) {
        this.fileDetailLinearLayoutOnItemLongClickListener = fileDetailLinearLayoutOnItemLongClickListener;
    }

    public interface FileDetailLinearLayoutOnItemClickListener {
        void onItemClick(int position);
    }

    public interface FileDetailLinearLayoutOnItemLongClickListener {
        void onItemLongClick(int position);
    }

    private static class FileUploadListViewHolder {
        private ImageView file_type_ImageView;
        private TextView file_name_TextView;
        private ImageView next_dir_ImageView;
        private LinearLayout file_LinearLayout;
    }
}
