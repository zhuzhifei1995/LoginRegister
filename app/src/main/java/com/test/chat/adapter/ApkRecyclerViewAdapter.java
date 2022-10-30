package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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
import com.test.chat.util.ImageUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ApkRecyclerViewAdapter extends RecyclerView.Adapter<ApkRecyclerViewAdapter.ApkRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private final List<JSONObject> jsonObjectList;
    private DownloadApkImageViewOnItemClickListener downloadApkImageViewOnItemClickListener;

    public ApkRecyclerViewAdapter(List<JSONObject> jsonObjectList) {
        Log.e(TAG, "初始化ApkRecyclerViewAdapter成功：" + jsonObjectList.toString());
        this.jsonObjectList = jsonObjectList;
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NotNull ApkRecyclerViewHolder apkRecyclerViewHolder, int position) {
        apkRecyclerViewHolder.root_apk_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: 外层被点击");
            }
        });
        apkRecyclerViewHolder.download_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (downloadApkImageViewOnItemClickListener != null) {
                    downloadApkImageViewOnItemClickListener.onItemClick(position);
                } else {
                    Log.e(TAG, "onClick: 不能点击");
                }
            }
        });
        JSONObject jsonObject = jsonObjectList.get(position);
        try {
            String apkName = jsonObject.getString("apk_name");
            Bitmap bitmap = ImageUtil.getBitmapFromFile(ActivityUtil.TMP_APK_ICON_PATH, apkName+".cache");
            if (bitmap == null){
                apkRecyclerViewHolder.apk_icon_ImageView.setImageResource(R.mipmap.ic_launcher);
            }else {
                apkRecyclerViewHolder.apk_icon_ImageView.setImageBitmap(bitmap);
            }
            apkRecyclerViewHolder.apk_name_TextView.setText(apkName);
            apkRecyclerViewHolder.apk_size_TextView.setText(new String("应用大小："+jsonObject.getString("apk_size")));
            apkRecyclerViewHolder.apk_update_time_TextView.setText(new String("最后更新时间："+jsonObject.getString("apk_update_time")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public ApkRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.apk_list_view, viewGroup, false);
        return new ApkRecyclerViewHolder(view);
    }

    public interface DownloadApkImageViewOnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnDownloadApkImageViewClickListener(ApkRecyclerViewAdapter.DownloadApkImageViewOnItemClickListener downloadFileImageViewOnItemClickListener) {
        this.downloadApkImageViewOnItemClickListener = downloadFileImageViewOnItemClickListener;
    }

    public static class ApkRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView apk_name_TextView;
        private final TextView apk_size_TextView;
        private final TextView apk_update_time_TextView;
        private final ImageView apk_icon_ImageView;
        private final LinearLayout root_apk_LinearLayout;
        private final ImageView download_ImageView;

        public ApkRecyclerViewHolder(View view) {
            super(view);
            apk_name_TextView = view.findViewById(R.id.apk_name_TextView);
            apk_size_TextView = view.findViewById(R.id.apk_size_TextView);
            apk_update_time_TextView = view.findViewById(R.id.apk_update_time_TextView);
            apk_icon_ImageView = view.findViewById(R.id.apk_icon_ImageView);
            root_apk_LinearLayout = view.findViewById(R.id.root_apk_LinearLayout);
            download_ImageView = view.findViewById(R.id.download_ImageView);

        }
    }
}