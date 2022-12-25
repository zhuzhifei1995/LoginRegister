package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressLint("RecyclerView")
public class ComicKindListViewAdapter extends BaseAdapter {

    private static final String TAG = ActivityUtil.TAG;
    private final int resourceId;
    private final LayoutInflater layoutInflater;
    private final List<JSONObject> comicKindJSONObjectList;
    private final Context context;
    private ComicKindTextViewOnItemClickListener comicKindTextViewOnItemClickListener;

    public ComicKindListViewAdapter(Context context, int resourceId, List<JSONObject> comicKindJSONObjectList) {
        layoutInflater = LayoutInflater.from(context);
        this.resourceId = resourceId;
        this.context = context;
        this.comicKindJSONObjectList = comicKindJSONObjectList;
    }

    public void setOnComicKindTextViewClickListener(ComicKindTextViewOnItemClickListener comicKindTextViewOnItemClickListener) {
        this.comicKindTextViewOnItemClickListener = comicKindTextViewOnItemClickListener;
    }

    @Override
    public int getCount() {
        return comicKindJSONObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return comicKindJSONObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ComicKindListViewAdapter.ComicKindListViewHolder comicKindListViewHolder = null;
        if (view == null) {
            comicKindListViewHolder = new ComicKindListViewAdapter.ComicKindListViewHolder();
            view = layoutInflater.inflate(resourceId, viewGroup, false);
            comicKindListViewHolder.file_name_TextView = (TextView) view.findViewById(R.id.file_name_TextView);
            comicKindListViewHolder.file_LinearLayout = (LinearLayout) view.findViewById(R.id.file_LinearLayout);
            view.setTag(comicKindListViewHolder);
        } else {
            comicKindListViewHolder = (ComicKindListViewAdapter.ComicKindListViewHolder) view.getTag();
        }
        try {
            String kindName = comicKindJSONObjectList.get(position).getString("kind_name");
            int select = comicKindJSONObjectList.get(position).getInt("select");
            comicKindListViewHolder.file_name_TextView.setText(kindName);
            comicKindListViewHolder.file_name_TextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comicKindTextViewOnItemClickListener != null) {
                        comicKindTextViewOnItemClickListener.onItemClick(position);
                    } else {
                        Log.e(TAG, "onClick: 不能点击");
                    }
                }
            });
            if (select == 1) {
                comicKindListViewHolder.file_LinearLayout.setBackgroundColor(context.getColor(R.color.gray));
            } else {
                comicKindListViewHolder.file_LinearLayout.setBackgroundColor(context.getColor(R.color.no_color));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public interface ComicKindTextViewOnItemClickListener {
        void onItemClick(int position);
    }

    public static class ComicKindListViewHolder {
        public TextView file_name_TextView;
        public LinearLayout file_LinearLayout;
    }
}