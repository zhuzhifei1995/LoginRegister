package com.test.chat.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chat.R;
import com.test.chat.adapter.MessageRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ChatFriendActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ActivityUtil.TAG;
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static final String IMAGE_FILE_NAME = "message_tmp.png.cache";
    private static final String MESSAGE_IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image";
    private static boolean IS_VOICE = false;
    private Context context;
    private final Handler getMessageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String json = (String) message.obj;
            try {
                JSONObject jsonObject = new JSONObject(json);
                final JSONArray messagesJSONArray = jsonObject.getJSONArray("message");
                Log.e(TAG, "handleMessage: " + jsonObject.getString("message"));
                for (int i = 0; i < messagesJSONArray.length(); i++) {
                    Log.e(TAG, "handleMessage messagesJSONArray: " + messagesJSONArray.getJSONObject(i));
                    if (messagesJSONArray.getJSONObject(i).getString("message_type").equals("2")) {
                        final String messageImageUrl = messagesJSONArray.getJSONObject(i).getString("message_image_url");
                        final String imageName = messagesJSONArray.getJSONObject(i).getString("message");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap photoBitmap = new HttpUtil(context).getImageBitmap(messageImageUrl);
                                if (photoBitmap != null) {
                                    ImageUtil.saveBitmapToTmpFile(photoBitmap, Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image", imageName + ".cache");
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
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
                                new HttpUtil(context).getSoundFile(messageVoiceUrl, voiceName);
                            }
                        }).start();
                    }
                }
                TmpFileUtil.writeJSONToFile(messagesJSONArray.toString(), Environment.getExternalStorageDirectory().getPath() + "/tmp/message", "message.json");
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(message);
        }
    };
    private Activity activity;
    private EditText send_message_EditText;
    private List<JSONObject> messageJSONObjectList;
    private RecyclerView message_RecyclerView;
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter;
    private Button send_message_Button;
    private Button send_image_message_Button;
    private Button voice_Button;
    private MediaRecorder mediaRecorder;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        setContentView(R.layout.activity_chat_friend);
        initView();
    }

    private void initView() {
        progressDialog = new ProgressDialog(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        String friendName = SharedPreferencesUtils.getString(context, "nick_name_friend", "", "user");
        String friendLoginNumber = SharedPreferencesUtils.getString(context, "login_number_friend", "", "user");
        if (friendName.equals("")) {
            friendName = friendLoginNumber;
        }
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText(friendName);
        send_message_Button = findViewById(R.id.send_message_Button);
        send_message_Button.setOnClickListener(this);
        send_image_message_Button = findViewById(R.id.send_image_message_Button);
        send_image_message_Button.setOnClickListener(this);
        ImageView send_voice_message_ImageView = findViewById(R.id.send_voice_message_ImageView);
        send_voice_message_ImageView.setOnClickListener(this);
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(this);
        voice_Button = findViewById(R.id.voice_Button);
        voice_Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.RECORD_AUDIO}, 400);
                } else {
                    startRecord();
                }
                return false;
            }
        });

        send_message_EditText = findViewById(R.id.send_message_EditText);
        send_message_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e(TAG, "输入框内容有变化！");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e(TAG, "输入框内容正在变化！");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Editable text = send_message_EditText.getText();
                if (text.toString().equals("")) {
                    send_image_message_Button.setVisibility(View.VISIBLE);
                    send_message_Button.setVisibility(View.GONE);
                } else {
                    send_message_Button.setVisibility(View.VISIBLE);
                    send_image_message_Button.setVisibility(View.GONE);
                }
                Log.e(TAG, "输入框内容变化完成！ " + text);
            }
        });
        message_RecyclerView = findViewById(R.id.message_RecyclerView);
        messageJSONObjectList = new ArrayList<>();

        String json = TmpFileUtil.getJSONFileString(Environment.getExternalStorageDirectory().getPath() + "/tmp/message", "message.json");
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.e(TAG, jsonArray.getJSONObject(i).toString());
                messageJSONObjectList.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        messageRecyclerViewAdapter = new MessageRecyclerViewAdapter(context, messageJSONObjectList);
        linearLayoutManager.setStackFromEnd(true);
        message_RecyclerView.setLayoutManager(linearLayoutManager);
        message_RecyclerView.setAdapter(messageRecyclerViewAdapter);

        message_RecyclerView.scrollToPosition(messageRecyclerViewAdapter.getItemCount() - 1);
        messageRecyclerViewAdapter.setOnItemClickListener(new MessageRecyclerViewAdapter.MessageRecyclerViewAdapterOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                JSONObject jsonObject = messageJSONObjectList.get(position);
                try {
                    if (jsonObject.getString("message_type").equals("2")) {
                        if (messageJSONObjectList.get(position).getString("message_type").equals("2")) {
                            Intent intent = new Intent(context, PhotoShowActivity.class);
                            intent.putExtra("flag", 2);
                            intent.putExtra("photoName", messageJSONObjectList.get(position).getString("message") + ".cache");
                            startActivity(intent);
                        }
                    } else if (jsonObject.getString("message_type").equals("3")) {
                        Intent intent = new Intent(context, PhotoShowActivity.class);
                        intent.putExtra("flag", 3);
                        intent.putExtra("voiceName", messageJSONObjectList.get(position).getString("message") + ".cache");
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        send_message_EditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                send_message_EditText.setSelection(send_message_EditText.getText().length());
                send_message_EditText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.showSoftInput(getCurrentFocus(), 0);
                }
            }
        }, 100);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Log.e(TAG, "聊天界面的内容被点击：" + view.getId());
        switch (view.getId()) {
            case R.id.title_left_ImageView:
                backFriendShow();
                break;
            case R.id.send_message_Button:
                sendMessage();
                break;
            case R.id.send_image_message_Button:
                chooseImageMessage();
                break;
            case R.id.send_voice_message_ImageView:
                showVoiceButton();
                break;
            case R.id.cancel_voice_Button:
                cancelVoice();
                break;
            case R.id.send_image_album_message_TextView:
                selectFromAlbum();
                break;
            case R.id.send_image_photo_message_TextView:
                selectFromPhoto();
                break;
            default:
                break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void cancelVoice() {
        progressDialog.dismiss();
        messageRecyclerViewAdapter.notifyDataSetChanged();
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String voiceUploadFileName = UUID.randomUUID().toString().replace("-", "");
                    File voiceTmpFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/voice", "tmp.amr.cache");
                    File voiceUploadFile = new File(Environment.getExternalStorageDirectory().getPath() + "/tmp/voice", voiceUploadFileName + ".amr.cache");
                    TmpFileUtil.copyFile(voiceTmpFile, voiceUploadFile);
                    try {
                        messageJSONObjectList.add(new JSONObject("{"
                                + "'message':'" + voiceUploadFileName + ".amr',"
                                + "'send_code':'1',"
                                + "'message_type':'3'"
                                + "}"));
                        Log.e(TAG, "run: " + messageJSONObjectList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    message_RecyclerView.scrollToPosition(messageRecyclerViewAdapter.getItemCount() - 1);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                    parameter.put("friend_id", SharedPreferencesUtils.getString(context, "id_friend", "", "user"));
                    parameter.put("message", voiceUploadFileName);
                    parameter.put("message_type", 3 + "");
                    new HttpUtil(context).upLoadImageFile(voiceUploadFile, ActivityUtil.NET_URL + "/send_message", parameter);
                }
            }).start();
            Toast.makeText(context, "录音结束！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "录音异常！", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "未初始化录音的工具类！");
        }
    }

    private void showVoiceButton() {
        if (IS_VOICE) {
            voice_Button.setVisibility(View.GONE);
            send_message_EditText.setVisibility(View.VISIBLE);
            IS_VOICE = false;
        } else {
            voice_Button.setVisibility(View.VISIBLE);
            send_message_EditText.setVisibility(View.GONE);
            IS_VOICE = true;
        }
    }

    private void chooseImageMessage() {
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.show();
            progressDialog.setContentView(R.layout.photo_progress_bar);
        }
        TextView send_image_album_message_TextView = progressDialog.findViewById(R.id.send_image_album_message_TextView);
        send_image_album_message_TextView.setOnClickListener(this);
        TextView send_image_photo_message_TextView = progressDialog.findViewById(R.id.send_image_photo_message_TextView);
        send_image_photo_message_TextView.setOnClickListener(this);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectFromAlbum() {
        progressDialog.dismiss();
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                Toast.makeText(context, "未找到图片查看器！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectFromPhoto() {
        progressDialog.dismiss();
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
        } else {
            photoFromCapture();
        }
    }

    @Override
    public void onBackPressed() {
        backFriendShow();
        super.onBackPressed();
    }

    private void backFriendShow() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> parameter = new HashMap<>();
                parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                parameter.put("friend_id", SharedPreferencesUtils.getString(context, "id_friend", "", "user"));
                Message message = new Message();
                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_messages", parameter);
                getMessageHandler.sendMessage(message);
            }
        }).start();
        finish();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendMessage() {
        messageRecyclerViewAdapter.notifyDataSetChanged();
        final String message = send_message_EditText.getText().toString();
        if (!message.trim().equals("")) {
            send_message_EditText.setText("");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        messageJSONObjectList.add(new JSONObject("{"
                                + "'message': '" + message + "',"
                                + "'send_code':'1',"
                                + "'message_type':'1'"
                                + "}"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    message_RecyclerView.scrollToPosition(messageRecyclerViewAdapter.getItemCount() - 1);
                    Log.e(TAG, "sendMessage: " + messageJSONObjectList);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                    parameter.put("friend_id", SharedPreferencesUtils.getString(context, "id_friend", "", "user"));
                    parameter.put("message", message);
                    parameter.put("message_type", 1 + "");
                    new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/send_message", parameter);
                }
            }).start();
        } else {
            Toast.makeText(context, "发送消息不能为空！", Toast.LENGTH_SHORT).show();
            send_message_EditText.setText("");
        }
    }


    private void photoFromCapture() {
        Intent intent;
        Uri pictureUri;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(MESSAGE_IMAGE_PATH);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                }
            }
        }
        File pictureFile = new File(MESSAGE_IMAGE_PATH, IMAGE_FILE_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(context,
                    getPackageName() + ".fileProvider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    public void startSmallPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING);
    }

    private void setSmallImageToImageView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dirFile = new File(MESSAGE_IMAGE_PATH);
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                    } else {
                        Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                    }
                }
                File file = new File(dirFile, IMAGE_FILE_NAME);
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(file);
                    if (photo != null) {
                        photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    }
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendImageMessage();
        }
    }

    public void startBigPhotoZoom(Uri uri) {
        File file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(MESSAGE_IMAGE_PATH);
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e(TAG, "文件夹创建失败：" + dirFile.getAbsolutePath());
                } else {
                    Log.e(TAG, "文件夹创建成功：" + dirFile.getAbsolutePath());
                }
            }

            file = new File(MESSAGE_IMAGE_PATH, IMAGE_FILE_NAME);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(FileProvider.getUriForFile(context,
                    getPackageName() + ".fileProvider", file), "image/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 500);
            intent.putExtra("outputY", 500);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
        } else {
            Toast.makeText(context, "剪切图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendImageMessage() {
        messageRecyclerViewAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageImage = UUID.randomUUID().toString().replace("-", "");
                try {
                    messageJSONObjectList.add(new JSONObject("{"
                            + "'message':'" + messageImage + ".png',"
                            + "'send_code':'1',"
                            + "'message_type':'2'"
                            + "}"));
                    Log.e(TAG, "run: " + messageJSONObjectList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                message_RecyclerView.scrollToPosition(messageRecyclerViewAdapter.getItemCount() - 1);
                File messageImageFile = new File(MESSAGE_IMAGE_PATH, messageImage + ".png.cache");
                TmpFileUtil.copyFile(new File(MESSAGE_IMAGE_PATH, IMAGE_FILE_NAME), messageImageFile);
                Map<String, String> parameter = new HashMap<>();
                parameter.put("user_id", SharedPreferencesUtils.getString(context, "id", "", "user"));
                parameter.put("friend_id", SharedPreferencesUtils.getString(context, "id_friend", "", "user"));
                parameter.put("message", messageImage);
                parameter.put("message_type", 2 + "");
                new HttpUtil(context).upLoadImageFile(messageImageFile, ActivityUtil.NET_URL + "/send_message", parameter);
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SMALL_IMAGE_CUTTING:
                    if (data != null) {
                        setSmallImageToImageView(data);
                    }
                    break;
                case REQUEST_BIG_IMAGE_CUTTING:
                    if (data != null) {
                        sendImageMessage();
                    }
                    break;
                case REQUEST_IMAGE_GET:
                    try {
                        if (data != null) {
                            startSmallPhotoZoom(data.getData());
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    File temp = new File(MESSAGE_IMAGE_PATH, IMAGE_FILE_NAME);
                    startBigPhotoZoom(Uri.fromFile(temp));
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(context, "未找到图片查看器！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case 300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFromCapture();
                }
                break;
            case 400:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecord();
                }
                break;
            default:
                break;
        }
    }

    private void startRecord() {
        File voiceDirPath = new File(Environment.getExternalStorageDirectory().getPath(), "/tmp/voice");
        Toast.makeText(context, "开始录音......", Toast.LENGTH_SHORT).show();
        Window window = progressDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(send_message_EditText.getWindowToken(), 0);
        progressDialog.setContentView(R.layout.voice_progress_bar);
        TextView cancel_voice_Button = progressDialog.findViewById(R.id.cancel_voice_Button);
        cancel_voice_Button.setOnClickListener(this);
        if (mediaRecorder == null) {
            if (!voiceDirPath.exists()) {
                if (voiceDirPath.mkdir()) {
                    Log.e(TAG, "录音文件夹创建成功：" + voiceDirPath);
                } else {
                    Log.e(TAG, "录音文件夹创建失败：" + voiceDirPath);
                }
            }
            File file = new File(voiceDirPath, "tmp.amr.cache");
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        Log.e(TAG, " 录音文件创建成功：" + file.getAbsolutePath());
                    } else {
                        Log.e(TAG, "录音文件创建失败：" + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.AudioEncoder.AMR_WB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "当前的聊天数据为：" + messageJSONObjectList.toString());
        TmpFileUtil.writeJSONToFile(messageJSONObjectList.toString(), Environment.getExternalStorageDirectory().getPath() + "/tmp/message", "message.json");
        super.onDestroy();
    }
}