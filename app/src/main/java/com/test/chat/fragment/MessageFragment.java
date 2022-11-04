package com.test.chat.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.test.chat.R;
import com.test.chat.activity.CaptureActivity;
import com.test.chat.activity.FriendShowActivity;
import com.test.chat.adapter.FriendRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MessageFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private View messageFragmentView;
    private Context context;
    private final Handler getMessageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 1) {
                try {
                    String json = (String) message.obj;
                    JSONObject jsonObject = new JSONObject(json);
                    final JSONArray messagesJSONArray = jsonObject.getJSONArray("message");
                    for (int i = 0; i < messagesJSONArray.length(); i++) {
                        if (messagesJSONArray.getJSONObject(i).getString("message_type").equals("2")) {
                            final String messageImageUrl = messagesJSONArray.getJSONObject(i).getString("message_image_url");
                            final String imageName = messagesJSONArray.getJSONObject(i).getString("message");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap photoBitmap = new HttpUtil(context).getImageBitmap(messageImageUrl);
                                    if (photoBitmap != null) {
                                        ImageUtil.saveBitmapToTmpFile(photoBitmap, ActivityUtil.TMP_MESSAGE_FILE_PATH, imageName + ".cache");
                                    }
                                }
                            }).start();
                        }
                        if (messagesJSONArray.getJSONObject(i).getString("message_type").equals("3")) {
                            final String messageVoiceUrl = messagesJSONArray.getJSONObject(i).getString("message_voice_url");
                            final String voiceName = messagesJSONArray.getJSONObject(i).getString("message");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        new HttpUtil(context).getSoundFile(messageVoiceUrl, voiceName);
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }
                    TmpFileUtil.writeJSONToFile(messagesJSONArray.toString(), ActivityUtil.TMP_MESSAGE_FILE_PATH, "message.json");
                } catch (Exception e) {
                    Toast.makeText(context, "网络异常", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常", Toast.LENGTH_LONG).show();
            }
            super.handleMessage(message);
        }
    };
    private Activity activity;

    public MessageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getActivity();
        activity = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        messageFragmentView = layoutInflater.inflate(R.layout.fragment_message, viewGroup, false);
        initFragmentView();
        return messageFragmentView;
    }

    private void initFragmentView() {
        ActivityUtil.setLinearLayoutBackground(messageFragmentView.findViewById(R.id.message_fragment_LinearLayout),
                SharedPreferencesUtils.getInt(context, "themeId", 0, "user"));
        initTitleView();
        initChatRecyclerView();
    }

    private void initChatRecyclerView() {
        final List<JSONObject> chatJSONObjectList = new ArrayList<>();
        String json = TmpFileUtil.getJSONFileString(ActivityUtil.TMP_USER_FILE_PATH, "chat.json");
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                chatJSONObjectList.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "initChatRecyclerView: 读取失败");
        }
        TextView no_chat_TextView = messageFragmentView.findViewById(R.id.no_chat_TextView);
        if (chatJSONObjectList.size() == 0) {
            no_chat_TextView.setVisibility(View.VISIBLE);
        } else {
            no_chat_TextView.setVisibility(View.GONE);
            Collections.reverse(chatJSONObjectList);
            RecyclerView chat_RecyclerView = messageFragmentView.findViewById(R.id.chat_RecyclerView);
            FriendRecyclerViewAdapter chatRecyclerViewAdapter = new FriendRecyclerViewAdapter(chatJSONObjectList);
            chatRecyclerViewAdapter.setOnItemClickListener(new FriendRecyclerViewAdapter.FriendRecyclerViewAdapterOnItemClickListener() {
                @Override
                public void onItemClick(final int position) {
                    Log.e(TAG, "第" + position + "个被短时的点击：" + chatJSONObjectList.get(position).toString());
                    Intent intent = new Intent(context, FriendShowActivity.class);
                    intent.putExtra("friendJSON", chatJSONObjectList.get(position).toString());
                    startActivity(intent);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, String> parameter = new HashMap<>();
                            parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                            try {
                                parameter.put("friend_id", chatJSONObjectList.get(position).getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Message message = new Message();
                            try {
                                message.what = 1;
                                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_messages", parameter);
                            } catch (Exception e) {
                                message.what = 0;
                                e.printStackTrace();
                            }
                            getMessageHandler.sendMessage(message);
                        }
                    }).start();
                }
            });
            chat_RecyclerView.setLayoutManager(new LinearLayoutManager(context));
            chat_RecyclerView.setAdapter(chatRecyclerViewAdapter);
        }
    }

    private void initTitleView() {
        TextView top_title_TextView = messageFragmentView.findViewById(R.id.top_title_TextView);
        top_title_TextView.setText("消息");
        ImageView title_left_ImageView = messageFragmentView.findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setImageBitmap(null);
        ImageView title_right_ImageView = messageFragmentView.findViewById(R.id.title_right_ImageView);
        title_right_ImageView.setImageResource(R.drawable.scan_button);
        title_right_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats();
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.setPrompt("请对准二维码");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: " + getClass().getSimpleName());
        initFragmentView();
        super.onHiddenChanged(hidden);
    }
}