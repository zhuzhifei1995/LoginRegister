package com.test.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.ActivityUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FriendListViewAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = ActivityUtil.TAG;
    private int resource;

    public FriendListViewAdapter(Context context, int resource, List<JSONObject> jsonObjects) {
        super(context, resource, jsonObjects);
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public JSONObject getItem(int position) {
        return super.getItem(position);
    }

    @Override
    @NotNull
    public View getView(int position, View convertView, @NotNull ViewGroup viewGroup) {
        JSONObject jsonObject = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.photo_ImageView = view.findViewById(R.id.photo_ImageView);
            viewHolder.nike_name_ListView_TextView = view.findViewById(R.id.nike_name_ListView_TextView);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        try {
            if (jsonObject != null) {
                String nick_name = jsonObject.getString("nick_name");
                String login_number = jsonObject.getString("login_number");
                String photo = jsonObject.getString("photo");
                String[] photos = photo.split("/");
                String tmpBitmapFileName = photos[photos.length - 1] + ".cache";
                Bitmap bitmap = ImageUtil.getBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/tmp/friend", tmpBitmapFileName);
                if (bitmap != null) {
                    Log.e(TAG, "initFriendView: 正常");
                    viewHolder.photo_ImageView.setImageBitmap(bitmap);
                } else {
                    Log.e(TAG, "initFriendView: 图片为空");
                    viewHolder.photo_ImageView.setImageResource(R.drawable.user_default_photo);
                }
                if (nick_name.equals("")) {
                    viewHolder.nike_name_ListView_TextView.setText(login_number);
                } else {
                    viewHolder.nike_name_ListView_TextView.setText(nick_name);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    static class ViewHolder {
        ImageView photo_ImageView;
        TextView nike_name_ListView_TextView;
    }
}
