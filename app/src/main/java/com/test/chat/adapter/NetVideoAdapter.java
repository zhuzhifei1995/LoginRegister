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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.ImageDownLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class NetVideoAdapter extends BaseAdapter {

    private static final String TAG = ActivityUtil.TAG;
    private final int resourceId;
    private final List<JSONObject> netVideoJSONObjectList;
    private final LayoutInflater layoutInflater;
    private final LruCache<String, BitmapDrawable> imageViewLruCache;
    private NetVideoLinearLayoutOnItemClickListener netVideoLinearLayoutOnItemClickListener;

    public NetVideoAdapter(Context context, int resourceId, List<JSONObject> netVideoJSONObjectList) {
        this.resourceId = resourceId;
        layoutInflater = LayoutInflater.from(context);
        this.netVideoJSONObjectList = netVideoJSONObjectList;
        imageViewLruCache = new LruCache<String, BitmapDrawable>(((int) Runtime.getRuntime().maxMemory() / 8)) {
            @Override
            protected int sizeOf(String key, BitmapDrawable bitmapDrawable) {
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap().getByteCount();
                }
                return -1;
            }
        };
    }

    @Override
    public int getCount() {
        return netVideoJSONObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return netVideoJSONObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        NetVideoListViewViewHolder netVideoListViewViewHolder = null;
        if (view == null) {
            netVideoListViewViewHolder = new NetVideoListViewViewHolder();
            view = layoutInflater.inflate(resourceId, viewGroup, false);
            netVideoListViewViewHolder.video_img_ImageView = (ImageView) view.findViewById(R.id.video_img_ImageView);
            netVideoListViewViewHolder.video_name_TextView = (TextView) view.findViewById(R.id.video_name_TextView);
            netVideoListViewViewHolder.video_time_TextView = (TextView) view.findViewById(R.id.video_time_TextView);
            netVideoListViewViewHolder.video_length_TextView = (TextView) view.findViewById(R.id.video_length_TextView);
            netVideoListViewViewHolder.video_author_TextView = (TextView) view.findViewById(R.id.video_author_TextView);
            view.setTag(netVideoListViewViewHolder);
        } else {
            netVideoListViewViewHolder = (NetVideoListViewViewHolder) view.getTag();
        }
        JSONObject jsonObject = netVideoJSONObjectList.get(position);
        try {
            String videoImg = jsonObject.getString("video_img");
            String videoName = jsonObject.getString("video_name");
            String videoTime = jsonObject.getString("video_time");
            String videoLength = jsonObject.getString("video_length");
            String videoAuthor = jsonObject.getString("video_author");
            netVideoListViewViewHolder.video_img_ImageView.setTag(videoImg);
            if (imageViewLruCache.get(videoImg) != null) {
                netVideoListViewViewHolder.video_img_ImageView.setImageDrawable(imageViewLruCache.get(videoImg));
            } else {
                ImageDownLoadTask imageDownLoadTask = new ImageDownLoadTask((ListView) viewGroup, imageViewLruCache, videoImg);
                netVideoListViewViewHolder.video_img_ImageView.setImageResource(R.drawable.banner_loading);
                imageDownLoadTask.execute();
            }
            netVideoListViewViewHolder.video_name_TextView.setText(videoName);
            netVideoListViewViewHolder.video_time_TextView.setText(videoTime);
            netVideoListViewViewHolder.video_length_TextView.setText(videoLength);
            netVideoListViewViewHolder.video_author_TextView.setText(videoAuthor);
            netVideoListViewViewHolder.video_img_ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (netVideoLinearLayoutOnItemClickListener != null) {
                        netVideoLinearLayoutOnItemClickListener.onItemClick(position);
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

    public void setOnNetVideoLinearLayoutClickListener(NetVideoLinearLayoutOnItemClickListener netVideoLinearLayoutOnItemClickListener) {
        this.netVideoLinearLayoutOnItemClickListener = netVideoLinearLayoutOnItemClickListener;
    }

    public interface NetVideoLinearLayoutOnItemClickListener {
        void onItemClick(int position);
    }

    private static class NetVideoListViewViewHolder {
        public ImageView video_img_ImageView;
        public TextView video_name_TextView;
        public TextView video_time_TextView;
        public TextView video_length_TextView;
        public TextView video_author_TextView;
    }
}
