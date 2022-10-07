package com.test.chat.adapter;

import android.content.Context;
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
import com.test.chat.util.SharedPreferencesUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.MessageRecyclerViewHolder> {

    private static final String TAG = ActivityUtil.TAG;
    private List<JSONObject> jsonObjectList;
    private MessageRecyclerViewAdapter.MessageRecyclerViewAdapterOnItemClickListener messageRecyclerViewAdapterOnItemClickListener;
    private Context context;

    public MessageRecyclerViewAdapter(Context context, List<JSONObject> jsonObjectList) {
        Log.e(TAG, "初始化MessageRecyclerViewAdapter成功：" + jsonObjectList.toString());
        this.jsonObjectList = jsonObjectList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return jsonObjectList.size();
    }

    @Override
    public void onBindViewHolder(@NotNull final MessageRecyclerViewHolder messageRecyclerViewHolder, final int position) {
        JSONObject jsonObject = jsonObjectList.get(position);
        try {
            String sendCode = jsonObject.getString("send_code");
            String messageType = jsonObject.getString("message_type");
            final String message = jsonObject.getString("message");
            if (sendCode.equals("1")) {
                messageRecyclerViewHolder.right_message_LinearLayout.setVisibility(View.GONE);
                messageRecyclerViewHolder.left_message_LinearLayout.setVisibility(View.VISIBLE);
                Bitmap bitmap = ImageUtil.getBitmapFromFile(
                        Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache");
                messageRecyclerViewHolder.left_message_ImageView.setImageBitmap(bitmap);
                if (messageType.equals("1")) {
                    messageRecyclerViewHolder.left_message_TextView.setVisibility(View.VISIBLE);
                    messageRecyclerViewHolder.left_message_TextView.setText(message);
                    messageRecyclerViewHolder.left_ImageView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.left_voice_ImageView.setVisibility(View.GONE);

                } else if (messageType.equals("2")) {
                    messageRecyclerViewHolder.left_message_TextView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.left_ImageView.setVisibility(View.VISIBLE);
                    messageRecyclerViewHolder.left_voice_ImageView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.left_ImageView.setImageBitmap(ImageUtil
                            .getBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image", message + ".cache"));
                    messageRecyclerViewHolder.left_ImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (messageRecyclerViewAdapterOnItemClickListener != null) {
                                Log.e(TAG, "onClick: 点了");
                                messageRecyclerViewAdapterOnItemClickListener.onItemClick(position);
                            } else {
                                Log.e(TAG, "onClick: 不能点击");
                            }
                        }
                    });
                } else {
                    messageRecyclerViewHolder.left_message_TextView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.left_ImageView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.left_voice_ImageView.setVisibility(View.VISIBLE);
                    messageRecyclerViewHolder.left_voice_ImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (messageRecyclerViewAdapterOnItemClickListener != null) {
                                Log.e(TAG, "onClick: 点了");
                                messageRecyclerViewAdapterOnItemClickListener.onItemClick(position);
                            } else {
                                Log.e(TAG, "onClick: 不能点击");
                            }
                        }
                    });
                }
            } else {
                messageRecyclerViewHolder.right_message_LinearLayout.setVisibility(View.VISIBLE);
                messageRecyclerViewHolder.left_message_LinearLayout.setVisibility(View.GONE);
                String[] photos = SharedPreferencesUtils.getString(context, "photo_friend", "", "user").split("/");
                Bitmap bitmap = ImageUtil.getBitmapFromFile(
                        Environment.getExternalStorageDirectory().getPath() + "/tmp/friend", photos[photos.length - 1] + ".cache");
                messageRecyclerViewHolder.right_message_ImageView.setImageBitmap(bitmap);
                messageRecyclerViewHolder.right_message_TextView.setText(message);
                if (messageType.equals("1")) {
                    messageRecyclerViewHolder.right_message_TextView.setVisibility(View.VISIBLE);
                    messageRecyclerViewHolder.right_message_TextView.setText(message);
                    messageRecyclerViewHolder.right_voice_ImageView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.right_ImageView.setVisibility(View.GONE);
                } else if (messageType.equals("2")) {
                    messageRecyclerViewHolder.right_message_TextView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.right_ImageView.setVisibility(View.VISIBLE);
                    messageRecyclerViewHolder.right_voice_ImageView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.right_ImageView.setImageBitmap(ImageUtil
                            .getBitmapFromFile(Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image", message + ".cache"));
                    messageRecyclerViewHolder.right_ImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (messageRecyclerViewAdapterOnItemClickListener != null) {
                                Log.e(TAG, "onClick: 点了");
                                messageRecyclerViewAdapterOnItemClickListener.onItemClick(position);
                            } else {
                                Log.e(TAG, "onClick: 不能点击");
                            }
                        }
                    });
                } else {
                    messageRecyclerViewHolder.right_message_TextView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.right_ImageView.setVisibility(View.GONE);
                    messageRecyclerViewHolder.right_voice_ImageView.setVisibility(View.VISIBLE);
                    messageRecyclerViewHolder.right_voice_ImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (messageRecyclerViewAdapterOnItemClickListener != null) {
                                Log.e(TAG, "onClick: 点了");
                                messageRecyclerViewAdapterOnItemClickListener.onItemClick(position);
                            } else {
                                Log.e(TAG, "onClick: 不能点击");
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public MessageRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_list_view, viewGroup, false);
        return new MessageRecyclerViewHolder(view);
    }

    public void setOnItemClickListener(MessageRecyclerViewAdapter.MessageRecyclerViewAdapterOnItemClickListener messageRecyclerViewAdapterOnItemClickListener) {
        this.messageRecyclerViewAdapterOnItemClickListener = messageRecyclerViewAdapterOnItemClickListener;
    }

    public interface MessageRecyclerViewAdapterOnItemClickListener {
        void onItemClick(int position);
    }

    public static class MessageRecyclerViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout right_message_LinearLayout;
        private LinearLayout left_message_LinearLayout;
        private ImageView right_message_ImageView;
        private ImageView left_message_ImageView;
        private TextView right_message_TextView;
        private TextView left_message_TextView;
        private ImageView right_ImageView;
        private ImageView left_ImageView;
        private ImageView right_voice_ImageView;
        private ImageView left_voice_ImageView;

        public MessageRecyclerViewHolder(View view) {
            super(view);
            right_message_LinearLayout = view.findViewById(R.id.right_message_LinearLayout);
            left_message_LinearLayout = view.findViewById(R.id.left_message_LinearLayout);
            right_message_ImageView = view.findViewById(R.id.right_message_ImageView);
            left_message_ImageView = view.findViewById(R.id.left_message_ImageView);
            right_message_TextView = view.findViewById(R.id.right_message_TextView);
            left_message_TextView = view.findViewById(R.id.left_message_TextView);
            right_ImageView = view.findViewById(R.id.right_ImageView);
            left_ImageView = view.findViewById(R.id.left_ImageView);
            right_voice_ImageView = view.findViewById(R.id.right_voice_ImageView);
            left_voice_ImageView = view.findViewById(R.id.left_voice_ImageView);
        }
    }

}