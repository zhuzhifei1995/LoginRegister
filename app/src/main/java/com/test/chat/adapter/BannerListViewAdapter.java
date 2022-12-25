package com.test.chat.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.ImageDownLoadTask;

import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class BannerListViewAdapter extends BaseAdapter {

    private static final String TAG = ActivityUtil.TAG;
    private final int resourceId;
    private final LayoutInflater layoutInflater;
    private final List<JSONObject> bannerJSONObjectList;
    private final LruCache<String, BitmapDrawable> imageViewLruCache;

    public BannerListViewAdapter(Context context, int resourceId, List<JSONObject> bannerJSONObjectList) {
        layoutInflater = LayoutInflater.from(context);
        this.resourceId = resourceId;
        this.bannerJSONObjectList = bannerJSONObjectList;
        imageViewLruCache = new LruCache<String, BitmapDrawable>(((int) Runtime.getRuntime().maxMemory() / 8)) {
            @Override
            protected int sizeOf(String key, BitmapDrawable bitmapDrawable) {
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap().getByteCount();
                }
                return 1;
            }
        };
    }

    @Override
    public int getCount() {
        return bannerJSONObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return bannerJSONObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        BannerListViewViewHolder bannerListViewViewHolder = null;
        if (view == null) {
            bannerListViewViewHolder = new BannerListViewViewHolder();
            view = layoutInflater.inflate(resourceId, viewGroup, false);
            bannerListViewViewHolder.big_banner_LinearLayout = (LinearLayout) view.findViewById(R.id.root_apk_LinearLayout);
            bannerListViewViewHolder.big_banner_ImageView = (ImageView) view.findViewById(R.id.big_banner_ImageView);
            bannerListViewViewHolder.big_banner_TextView = (TextView) view.findViewById(R.id.big_banner_TextView);
            view.setTag(bannerListViewViewHolder);
        } else {
            bannerListViewViewHolder = (BannerListViewViewHolder) view.getTag();
        }
        try {
            JSONObject jsonObject = bannerJSONObjectList.get(position);
            String bannerLink = jsonObject.getString("banner_link");
            String bannerImage = jsonObject.getString("banner_image");
            String bannerIntroduce = jsonObject.getString("banner_introduce");
            bannerListViewViewHolder.big_banner_TextView.setText(bannerIntroduce);
            bannerListViewViewHolder.big_banner_ImageView.setTag(bannerImage);
            if (imageViewLruCache.get(bannerImage) != null) {
                bannerListViewViewHolder.big_banner_ImageView.setImageDrawable(imageViewLruCache.get(bannerImage));
            } else {
                ImageDownLoadTask imageDownLoadTask = new ImageDownLoadTask((ListView) viewGroup, imageViewLruCache, bannerImage);
                bannerListViewViewHolder.big_banner_ImageView.setImageResource(R.drawable.banner_loading);
                imageDownLoadTask.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private static class BannerListViewViewHolder {
        public LinearLayout big_banner_LinearLayout;
        public ImageView big_banner_ImageView;
        public TextView big_banner_TextView;
    }

}
