package com.test.chat.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("StaticFieldLeak")
@SuppressWarnings("deprecation")
@RequiresApi(api = Build.VERSION_CODES.M)
public class ImageDownLoadTask extends AsyncTask<String, Void, BitmapDrawable> {

    private final ListView listView;
    private final LruCache<String, BitmapDrawable> imageViewLruCache;
    private final String imageUrl;
    private Context context;

    public ImageDownLoadTask(ListView listView, LruCache<String, BitmapDrawable> imageViewLruCache, String imageUrl) {
        this.listView = listView;
        this.imageViewLruCache = imageViewLruCache;
        this.imageUrl = imageUrl;
    }

    public ImageDownLoadTask(Context context, ListView listView, LruCache<String, BitmapDrawable> imageViewLruCache, String imageUrl) {
        this.listView = listView;
        this.imageViewLruCache = imageViewLruCache;
        this.imageUrl = imageUrl;
        this.context = context;
    }

    @Override
    protected BitmapDrawable doInBackground(String... params) {
        Bitmap bitmap = downloadImage();
        BitmapDrawable bitmapDrawable = new BitmapDrawable(listView.getResources(), bitmap);
        if (imageViewLruCache.get(imageUrl) == null) {
            imageViewLruCache.put(imageUrl, bitmapDrawable);
        }
        return bitmapDrawable;
    }

    @Override
    protected void onPostExecute(BitmapDrawable bitmapDrawable) {
        ImageView apk_icon_ImageView = (ImageView) listView.findViewWithTag(imageUrl);
        if (apk_icon_ImageView != null && bitmapDrawable != null) {
            apk_icon_ImageView.setImageDrawable(bitmapDrawable);
        }
    }

    private Bitmap downloadImage() {
        HttpURLConnection httpURLConnection = null;
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5 * 1000);
            httpURLConnection.setReadTimeout(10 * 1000);
            bitmap = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return bitmap;
    }
}