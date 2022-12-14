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
@SuppressLint("RecyclerView")
public class ApkRecyclerViewAdapter extends RecyclerView.Adapter<ApkRecyclerViewAdapter.ApkRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private final List<JSONObject> jsonObjectList;
    private DownloadApkImageViewOnItemClickListener downloadApkImageViewOnItemClickListener;
    private InstallImageViewOnItemClickListener installImageViewOnItemClickListener;
    private DeleteImageViewOnItemClickListener deleteImageViewOnItemClickListener;
    private ApkDetailLinearLayoutOnItemClickListener apkDetailLinearLayoutOnItemClickListener;

    public ApkRecyclerViewAdapter(List<JSONObject> jsonObjectList) {
        Log.e(TAG, "初始化ApkRecyclerViewAdapter成功：" + jsonObjectList.toString());
        this.jsonObjectList = jsonObjectList;
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

    @Override
    public void onBindViewHolder(@NotNull ApkRecyclerViewHolder apkRecyclerViewHolder, int position) {
        apkRecyclerViewHolder.root_apk_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apkDetailLinearLayoutOnItemClickListener != null) {
                    apkDetailLinearLayoutOnItemClickListener.onItemClick(position);
                } else {
                    Log.e(TAG, "onClick: 不能点击");
                }
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
        apkRecyclerViewHolder.install_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (installImageViewOnItemClickListener != null) {
                    installImageViewOnItemClickListener.onItemClick(position);
                } else {
                    Log.e(TAG, "onClick: 不能点击");
                }
            }
        });
        apkRecyclerViewHolder.delete_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteImageViewOnItemClickListener != null) {
                    deleteImageViewOnItemClickListener.onItemClick(position);
                } else {
                    Log.e(TAG, "onClick: 不能点击");
                }
            }
        });
        JSONObject jsonObject = jsonObjectList.get(position);
        try {
            String apkName = jsonObject.getString("apk_name");
            Bitmap bitmap = ImageUtil.getBitmapFromFile(ActivityUtil.TMP_APK_ICON_PATH, apkName
                    + "_" + jsonObjectList.get(position).getString("apk_id") + ".cache");
            if (bitmap == null) {
                apkRecyclerViewHolder.apk_icon_ImageView.setImageResource(R.mipmap.ic_launcher);
            } else {
                apkRecyclerViewHolder.apk_icon_ImageView.setImageBitmap(bitmap);
            }
            int downloadFlag = jsonObject.getInt("download_flag");
            if (downloadFlag == 0) {
                apkRecyclerViewHolder.download_ImageView.setVisibility(View.VISIBLE);
                apkRecyclerViewHolder.delete_ImageView.setVisibility(View.GONE);
                apkRecyclerViewHolder.install_ImageView.setVisibility(View.GONE);
                apkRecyclerViewHolder.downing_ImageView.setVisibility(View.GONE);
            } else if (downloadFlag == 1) {
                apkRecyclerViewHolder.download_ImageView.setVisibility(View.GONE);
                apkRecyclerViewHolder.delete_ImageView.setVisibility(View.VISIBLE);
                apkRecyclerViewHolder.install_ImageView.setVisibility(View.GONE);
                apkRecyclerViewHolder.downing_ImageView.setVisibility(View.GONE);
            } else if (downloadFlag == 2) {
                apkRecyclerViewHolder.download_ImageView.setVisibility(View.GONE);
                apkRecyclerViewHolder.delete_ImageView.setVisibility(View.GONE);
                apkRecyclerViewHolder.install_ImageView.setVisibility(View.GONE);
                apkRecyclerViewHolder.downing_ImageView.setVisibility(View.VISIBLE);
            }
            apkRecyclerViewHolder.apk_name_TextView.setText(apkName);
            apkRecyclerViewHolder.apk_size_TextView.setText("应用大小：" + jsonObject.getString("apk_size"));
            apkRecyclerViewHolder.apk_update_time_TextView.setText("最后更新时间：" + jsonObject.getString("apk_update_time"));
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

    public void setOnDownloadApkImageViewClickListener(ApkRecyclerViewAdapter.DownloadApkImageViewOnItemClickListener downloadFileImageViewOnItemClickListener) {
        this.downloadApkImageViewOnItemClickListener = downloadFileImageViewOnItemClickListener;
    }

    public void setOnInstallImageViewClickListener(ApkRecyclerViewAdapter.InstallImageViewOnItemClickListener installImageViewOnItemClickListener) {
        this.installImageViewOnItemClickListener = installImageViewOnItemClickListener;
    }

    public void setOnDeleteImageViewClickListener(ApkRecyclerViewAdapter.DeleteImageViewOnItemClickListener deleteImageViewOnItemClickListener) {
        this.deleteImageViewOnItemClickListener = deleteImageViewOnItemClickListener;
    }

    public void setOnApkDetailLinearLayoutClickListener(ApkRecyclerViewAdapter.ApkDetailLinearLayoutOnItemClickListener apkDetailLinearLayoutOnItemClickListener) {
        this.apkDetailLinearLayoutOnItemClickListener = apkDetailLinearLayoutOnItemClickListener;
    }

    public interface DownloadApkImageViewOnItemClickListener {
        void onItemClick(int position);
    }

    public interface InstallImageViewOnItemClickListener {
        void onItemClick(int position);
    }

    public interface DeleteImageViewOnItemClickListener {
        void onItemClick(int position);
    }

    public interface ApkDetailLinearLayoutOnItemClickListener {
        void onItemClick(int position);
    }

    public static class ApkRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView apk_name_TextView;
        private final TextView apk_size_TextView;
        private final TextView apk_update_time_TextView;
        private final ImageView apk_icon_ImageView;
        private final LinearLayout root_apk_LinearLayout;
        private final ImageView delete_ImageView;
        private final ImageView download_ImageView;
        private final ImageView install_ImageView;
        private final ProgressBar downing_ImageView;

        public ApkRecyclerViewHolder(View view) {
            super(view);
            apk_name_TextView = view.findViewById(R.id.apk_name_TextView);
            apk_size_TextView = view.findViewById(R.id.apk_size_TextView);
            apk_update_time_TextView = view.findViewById(R.id.apk_update_time_TextView);
            apk_icon_ImageView = view.findViewById(R.id.apk_icon_ImageView);
            root_apk_LinearLayout = view.findViewById(R.id.root_apk_LinearLayout);
            delete_ImageView = view.findViewById(R.id.delete_ImageView);
            download_ImageView = view.findViewById(R.id.download_ImageView);
            install_ImageView = view.findViewById(R.id.install_ImageView);
            downing_ImageView = view.findViewById(R.id.downing_ProgressBar);
        }
    }
}