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
public class ComicListViewAdapter extends BaseAdapter {

    private static final String TAG = ActivityUtil.TAG;
    private final int resourceId;
    private final LayoutInflater layoutInflater;
    private final List<JSONObject> comicJSONObjectList;
    private final LruCache<String, BitmapDrawable> imageViewLruCache;
    private final Context context;

    public ComicListViewAdapter(Context context, int resourceId, List<JSONObject> comicJSONObjectList) {
        layoutInflater = LayoutInflater.from(context);
        this.resourceId = resourceId;
        this.comicJSONObjectList = comicJSONObjectList;
        this.context = context;
        imageViewLruCache = new LruCache<String, BitmapDrawable>(((int) Runtime.getRuntime().maxMemory() / 4)) {
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
        return comicJSONObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return comicJSONObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ComicListViewAdapter.ComicListViewViewHolder comicListViewViewHolder = null;
        if (view == null) {
            comicListViewViewHolder = new ComicListViewAdapter.ComicListViewViewHolder();
            view = layoutInflater.inflate(resourceId, viewGroup, false);
            comicListViewViewHolder.comic_cover_ImageView = (ImageView) view.findViewById(R.id.comic_cover_ImageView);
            comicListViewViewHolder.comic_last_update_time_TextView = (TextView) view.findViewById(R.id.comic_last_update_time_TextView);
            comicListViewViewHolder.comic_name_TextView = (TextView) view.findViewById(R.id.comic_name_TextView);
            comicListViewViewHolder.comic_update_chapter_TextView = (TextView) view.findViewById(R.id.comic_update_chapter_TextView);
            view.setTag(comicListViewViewHolder);
        } else {
            comicListViewViewHolder = (ComicListViewAdapter.ComicListViewViewHolder) view.getTag();
        }
        try {
            String comicCover = comicJSONObjectList.get(position).getString("comic_cover");
            String comicName = comicJSONObjectList.get(position).getString("comic_name");
            String comicLastUpdateTime = comicJSONObjectList.get(position).getString("comic_last_update_time");
            String comicUpdateChapter = comicJSONObjectList.get(position).getString("comic_update_chapter");
            comicListViewViewHolder.comic_cover_ImageView.setTag(comicCover);
            if (!comicCover.endsWith(".jpg")) {
                comicListViewViewHolder.comic_cover_ImageView.setImageResource(R.drawable.comic_default_cover);
            } else {
                if (imageViewLruCache.get(comicCover) != null) {
                    comicListViewViewHolder.comic_cover_ImageView.setImageDrawable(imageViewLruCache.get(comicCover));
                } else {
                    ImageDownLoadTask imageDownLoadTask = new ImageDownLoadTask(context, (ListView) viewGroup, imageViewLruCache, comicCover);
                    comicListViewViewHolder.comic_cover_ImageView.setImageResource(R.drawable.comic_default_cover);
                    imageDownLoadTask.execute();
                }
            }
            comicListViewViewHolder.comic_name_TextView.setText(comicName);
            comicListViewViewHolder.comic_last_update_time_TextView.setText(comicLastUpdateTime);
            comicListViewViewHolder.comic_update_chapter_TextView.setText(comicUpdateChapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    private static class ComicListViewViewHolder {
        public ImageView comic_cover_ImageView;
        public TextView comic_last_update_time_TextView;
        public TextView comic_name_TextView;
        public TextView comic_update_chapter_TextView;
    }
}
