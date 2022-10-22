package com.test.chat.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
public class FriendRecyclerViewAdapter extends RecyclerView.Adapter<FriendRecyclerViewAdapter.FriendRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private final List<JSONObject> jsonObjectList;
    private FriendRecyclerViewAdapterOnItemClickListener friendRecyclerViewAdapterOnItemClickListener;
    private FriendRecyclerViewAdapterOnItemLongClickListener friendRecyclerViewAdapterOnItemLongClickListener;

    public FriendRecyclerViewAdapter(List<JSONObject> jsonObjectList) {
        Log.e(TAG, "初始化FriendRecyclerViewAdapter成功：" + jsonObjectList.toString());
        this.jsonObjectList = jsonObjectList;
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NotNull FriendRecyclerViewHolder messageRecyclerViewHolder, int position) {
        JSONObject jsonObject = jsonObjectList.get(position);
        try {
            if (jsonObject != null) {
                String nick_name = jsonObject.getString("nick_name");
                String login_number = jsonObject.getString("login_number");
                String photo = jsonObject.getString("photo");
                String[] photos = photo.split("/");
                String tmpBitmapFileName = photos[photos.length - 1] + ".cache";
                Bitmap bitmap = ImageUtil.getBitmapFromFile(ActivityUtil.TMP_FRIEND_FILE_PATH, tmpBitmapFileName);
                if (bitmap != null) {
                    Log.e(TAG, "initFriendView: 正常");
                    messageRecyclerViewHolder.photo_ImageView.setImageBitmap(bitmap);
                } else {
                    Log.e(TAG, "initFriendView: 图片为空");
                    messageRecyclerViewHolder.photo_ImageView.setImageResource(R.drawable.user_default_photo);
                }
                if (nick_name.equals("")) {
                    messageRecyclerViewHolder.nike_name_ListView_TextView.setText(login_number);
                } else {
                    messageRecyclerViewHolder.nike_name_ListView_TextView.setText(nick_name);
                }
                messageRecyclerViewHolder.root_friend_LinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (friendRecyclerViewAdapterOnItemClickListener != null) {
                            friendRecyclerViewAdapterOnItemClickListener.onItemClick(position);
                        } else {
                            Log.e(TAG, "onClick: 不能点击");
                        }
                    }
                });

                messageRecyclerViewHolder.root_friend_LinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (friendRecyclerViewAdapterOnItemLongClickListener != null) {
                            friendRecyclerViewAdapterOnItemLongClickListener.onItemLongClick(position);
                        } else {
                            Log.e(TAG, "onClick: 不能长按点击");
                        }
                        return false;
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public FriendRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_list_view, viewGroup, false);
        return new FriendRecyclerViewHolder(view);
    }

    public void setOnItemClickListener(FriendRecyclerViewAdapterOnItemClickListener friendRecyclerViewAdapterOnItemClickListener) {
        this.friendRecyclerViewAdapterOnItemClickListener = friendRecyclerViewAdapterOnItemClickListener;
    }

    public void setOnItemLongClickListener(FriendRecyclerViewAdapterOnItemLongClickListener friendRecyclerViewAdapterOnItemLongClickListener) {
        this.friendRecyclerViewAdapterOnItemLongClickListener = friendRecyclerViewAdapterOnItemLongClickListener;
    }

    public interface FriendRecyclerViewAdapterOnItemClickListener {
        void onItemClick(int position);
    }

    public interface FriendRecyclerViewAdapterOnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public static class FriendRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView photo_ImageView;
        private final TextView nike_name_ListView_TextView;
        private final LinearLayout root_friend_LinearLayout;

        public FriendRecyclerViewHolder(View view) {
            super(view);
            photo_ImageView = view.findViewById(R.id.photo_ImageView);
            nike_name_ListView_TextView = view.findViewById(R.id.nike_name_ListView_TextView);
            root_friend_LinearLayout = view.findViewById(R.id.root_friend_LinearLayout);
        }
    }
}