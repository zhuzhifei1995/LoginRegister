package com.test.chat.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.ImageDownLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ApkListViewAdapter extends BaseAdapter {

    private static final String TAG = ActivityUtil.TAG;
    private final int resourceId;
    private final LayoutInflater layoutInflater;
    private final List<JSONObject> appJSONObjectList;
    private final LruCache<String, BitmapDrawable> imageViewLruCache;
    private DownloadApkImageViewOnItemClickListener downloadApkImageViewOnItemClickListener;
    private InstallImageViewOnItemClickListener installImageViewOnItemClickListener;
    private DeleteImageViewOnItemClickListener deleteImageViewOnItemClickListener;
    private ApkDetailLinearLayoutOnItemClickListener apkDetailLinearLayoutOnItemClickListener;

    public ApkListViewAdapter(Context context, int resourceId, List<JSONObject> appJSONObjectList) {
        layoutInflater = LayoutInflater.from(context);
        this.resourceId = resourceId;
        this.appJSONObjectList = appJSONObjectList;
        imageViewLruCache = new LruCache<String, BitmapDrawable>(((int) Runtime.getRuntime().maxMemory() / 8)) {
            @Override
            protected int sizeOf(String key, BitmapDrawable bitmapDrawable) {
                return bitmapDrawable.getBitmap().getByteCount();
            }
        };
    }

    public void setOnDownloadApkImageViewClickListener(ApkListViewAdapter.DownloadApkImageViewOnItemClickListener downloadFileImageViewOnItemClickListener) {
        this.downloadApkImageViewOnItemClickListener = downloadFileImageViewOnItemClickListener;
    }

    public void setOnInstallImageViewClickListener(ApkListViewAdapter.InstallImageViewOnItemClickListener installImageViewOnItemClickListener) {
        this.installImageViewOnItemClickListener = installImageViewOnItemClickListener;
    }

    public void setOnDeleteImageViewClickListener(ApkListViewAdapter.DeleteImageViewOnItemClickListener deleteImageViewOnItemClickListener) {
        this.deleteImageViewOnItemClickListener = deleteImageViewOnItemClickListener;
    }

    public void setOnApkDetailLinearLayoutClickListener(ApkListViewAdapter.ApkDetailLinearLayoutOnItemClickListener apkDetailLinearLayoutOnItemClickListener) {
        this.apkDetailLinearLayoutOnItemClickListener = apkDetailLinearLayoutOnItemClickListener;
    }

    @Override
    public int getCount() {
        return appJSONObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return appJSONObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ApkListViewViewHolder apkListViewViewHolder = null;
        if (view == null) {
            apkListViewViewHolder = new ApkListViewViewHolder();
            view = layoutInflater.inflate(resourceId, viewGroup, false);
            apkListViewViewHolder.apk_name_TextView = (TextView) view.findViewById(R.id.apk_name_TextView);
            apkListViewViewHolder.apk_size_TextView = (TextView) view.findViewById(R.id.apk_size_TextView);
            apkListViewViewHolder.apk_update_time_TextView = (TextView) view.findViewById(R.id.apk_update_time_TextView);
            apkListViewViewHolder.apk_icon_ImageView = (ImageView) view.findViewById(R.id.apk_icon_ImageView);
            apkListViewViewHolder.root_apk_LinearLayout = (LinearLayout) view.findViewById(R.id.root_apk_LinearLayout);
            apkListViewViewHolder.delete_ImageView = (ImageView) view.findViewById(R.id.delete_ImageView);
            apkListViewViewHolder.download_ImageView = (ImageView) view.findViewById(R.id.download_ImageView);
            apkListViewViewHolder.install_ImageView = (ImageView) view.findViewById(R.id.install_ImageView);
            apkListViewViewHolder.downing_ProgressBar = (ProgressBar) view.findViewById(R.id.downing_ProgressBar);
            view.setTag(apkListViewViewHolder);
        } else {
            apkListViewViewHolder = (ApkListViewViewHolder) view.getTag();
        }
        try {
            JSONObject jsonObject = appJSONObjectList.get(position);
            String apkName = jsonObject.getString("apk_name");
            String apkIcon = jsonObject.getString("apk_icon");
            apkListViewViewHolder.apk_name_TextView.setText(apkName);
            apkListViewViewHolder.apk_icon_ImageView.setTag(apkIcon);
            if (imageViewLruCache.get(apkIcon) != null) {
                apkListViewViewHolder.apk_icon_ImageView.setImageDrawable(imageViewLruCache.get(apkIcon));
            } else {
                ImageDownLoadTask imageDownLoadTask = new ImageDownLoadTask((ListView) viewGroup, imageViewLruCache, apkIcon);
                apkListViewViewHolder.apk_icon_ImageView.setImageResource(R.drawable.ic_launcher);
                imageDownLoadTask.execute();
            }
            int downloadFlag = jsonObject.getInt("download_flag");
            if (downloadFlag == 0) {
                apkListViewViewHolder.download_ImageView.setVisibility(View.VISIBLE);
                apkListViewViewHolder.delete_ImageView.setVisibility(View.GONE);
                apkListViewViewHolder.install_ImageView.setVisibility(View.GONE);
                apkListViewViewHolder.downing_ProgressBar.setVisibility(View.GONE);
            } else if (downloadFlag == 1) {
                apkListViewViewHolder.download_ImageView.setVisibility(View.GONE);
                apkListViewViewHolder.delete_ImageView.setVisibility(View.VISIBLE);
                apkListViewViewHolder.install_ImageView.setVisibility(View.GONE);
                apkListViewViewHolder.downing_ProgressBar.setVisibility(View.GONE);
            } else if (downloadFlag == 2) {
                apkListViewViewHolder.download_ImageView.setVisibility(View.GONE);
                apkListViewViewHolder.delete_ImageView.setVisibility(View.GONE);
                apkListViewViewHolder.install_ImageView.setVisibility(View.GONE);
                apkListViewViewHolder.downing_ProgressBar.setVisibility(View.VISIBLE);
            }
            apkListViewViewHolder.apk_name_TextView.setText(apkName);
            String apkSize = "应用大小：" + jsonObject.getString("apk_size");
            apkListViewViewHolder.apk_size_TextView.setText(apkSize);
            String apkUpdateTime = "最后更新时间：" + jsonObject.getString("apk_update_time");
            apkListViewViewHolder.apk_update_time_TextView.setText(apkUpdateTime);
            apkListViewViewHolder.root_apk_LinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (apkDetailLinearLayoutOnItemClickListener != null) {
                        apkDetailLinearLayoutOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
            apkListViewViewHolder.download_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (downloadApkImageViewOnItemClickListener != null) {
                        downloadApkImageViewOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
            apkListViewViewHolder.install_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (installImageViewOnItemClickListener != null) {
                        installImageViewOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
            apkListViewViewHolder.delete_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deleteImageViewOnItemClickListener != null) {
                        deleteImageViewOnItemClickListener.onItemClick(position);
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

    private static class ApkListViewViewHolder {
        public TextView apk_name_TextView;
        public TextView apk_size_TextView;
        public TextView apk_update_time_TextView;
        public ImageView apk_icon_ImageView;
        public LinearLayout root_apk_LinearLayout;
        public ImageView delete_ImageView;
        public ImageView download_ImageView;
        public ImageView install_ImageView;
        public ProgressBar downing_ProgressBar;
    }

}