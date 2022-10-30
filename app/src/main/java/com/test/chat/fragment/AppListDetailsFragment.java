package com.test.chat.fragment;

import static com.test.chat.util.ActivityUtil.showDownloadNotification;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chat.R;
import com.test.chat.adapter.ApkRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.TmpFileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressLint("NotifyDataSetChanged")
public class AppListDetailsFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private View appListDetailsFragmentView;
    private final List<JSONObject> jsonObjectList;
    private Context context;
    private ApkRecyclerViewAdapter apkRecyclerViewAdapter;
    private final Handler downloadNetDiskFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            JSONObject jsonObject = (JSONObject) message.obj;
            int position = message.what;
            try {
                String downFileName = jsonObject.getString("apk_name")+"_"+jsonObject.getString("apk_id");
                File downloadFile = new File(ActivityUtil.TMP_APK_FILE_PATH, downFileName + ".download");
                TmpFileUtil.copyFile(downloadFile, new File(ActivityUtil.TMP_APK_FILE_PATH, downFileName + ".cache"));
                if (downloadFile.delete()) {
                    Log.e(TAG, "handleMessage: 临时下载文件删除成功" + downFileName);
                } else {
                    Log.e(TAG, "handleMessage: 临时下载文件删除失败" + downFileName);
                }
                jsonObjectList.get(position).put("download_flag", 1);
                apkRecyclerViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "download success");
            try {
                showDownloadNotification(context, jsonObject.getString("apk_name"), position, " 下载完成！", 1);
                Toast.makeText(context, jsonObject.getString("apk_name") + " 文件下载成功！", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.handleMessage(message);
        }
    };
    private final Handler failDownloadNetDiskFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            Toast.makeText(context, "创建文件下载任务失败，网络异常！", Toast.LENGTH_SHORT).show();
            try {
                jsonObjectList.get(message.what).put("download_flag", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            apkRecyclerViewAdapter.notifyDataSetChanged();
            super.handleMessage(message);
        }
    };

    public AppListDetailsFragment(List<JSONObject> jsonObjectList) {
        this.jsonObjectList = jsonObjectList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        appListDetailsFragmentView = inflater.inflate(R.layout.fragment_app_list_details, container, false);
        initFragmentView();
        return appListDetailsFragmentView;
    }

    private void initFragmentView() {
        for (int i = 0;i<jsonObjectList.size();i++){
            try {
                File downloadFile = new File(ActivityUtil.TMP_APK_FILE_PATH, jsonObjectList.get(i).getString("apk_name")
                        +"_"+jsonObjectList.get(i).getString("apk_id")+ ".download");
                if (downloadFile.exists()){
                    jsonObjectList.get(i).put("download_flag",2);
                }else {
                    File cacheFile = new File(ActivityUtil.TMP_APK_FILE_PATH, jsonObjectList.get(i).getString("apk_name")
                            +"_"+jsonObjectList.get(i).getString("apk_id")+ ".cache");
                    if (cacheFile.exists()){
                        jsonObjectList.get(i).put("download_flag",1);
                    }else {
                        jsonObjectList.get(i).put("download_flag",0);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RecyclerView apk_file_RecyclerView = appListDetailsFragmentView.findViewById(R.id.apk_file_RecyclerView);
        apkRecyclerViewAdapter = new ApkRecyclerViewAdapter(jsonObjectList);
        apkRecyclerViewAdapter.setOnDownloadApkImageViewClickListener(new ApkRecyclerViewAdapter.DownloadApkImageViewOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                try {
                    downloadNetDiskFile(context, jsonObjectList.get(position)
                            , jsonObjectList.get(position).getString("apk_download_url"), position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        apkRecyclerViewAdapter.setOnInstallImageViewClickListener(new ApkRecyclerViewAdapter.InstallImageViewOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                try {
                    String apkFileName = jsonObjectList.get(position).getString("apk_name") + "_"+jsonObjectList.get(position).getString("apk_id");
                    File cacheFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkFileName+ ".cache");
                    File apkFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkFileName+ ".apk");
                    TmpFileUtil.copyFile(cacheFile,apkFile);
                    ActivityUtil.installApk(context,apkFile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        apkRecyclerViewAdapter.setOnDeleteImageViewClickListener(new ApkRecyclerViewAdapter.DeleteImageViewOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.e(TAG, "onItemClick: 正在删除安装包" + jsonObjectList.get(position));
                try {
                    String deleteFileName = jsonObjectList.get(position).getString("apk_name");;
                    ProgressDialog progressDialog = new ProgressDialog(context);
                    Window window = progressDialog.getWindow();
                    if (window != null) {
                        progressDialog.show();
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.gravity = Gravity.CENTER;
                        progressDialog.setCancelable(true);
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        progressDialog.setContentView(R.layout.exit_progress_bar);
                        TextView dialog_message_TextView = progressDialog.findViewById(R.id.dialog_message_TextView);
                        dialog_message_TextView.setText(new String("是否删除安装包： " + deleteFileName + "？"));
                    }
                    progressDialog.findViewById(R.id.cancel_exit_register_TextView).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            progressDialog.dismiss();
                        }
                    });
                    progressDialog.findViewById(R.id.confirm_exit_register_TextView).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                File deleteFile = new File(ActivityUtil.TMP_APK_FILE_PATH, deleteFileName+
                                        "_"+jsonObjectList.get(position).getString("apk_id") + ".cache");
                                File deleteApkFile = new File(ActivityUtil.TMP_APK_FILE_PATH, deleteFileName+
                                        "_"+jsonObjectList.get(position).getString("apk_id") + ".apk");
                                if (deleteFile.delete()) {
                                    try {
                                        jsonObjectList.get(position).put("download_flag", 0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(context, "删除安装包：" + deleteFileName + " 成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "删除安装包：" + deleteFileName + " 失败，文件不存在！", Toast.LENGTH_SHORT).show();
                                }
                                if (deleteApkFile.delete()) {
                                    try {
                                        jsonObjectList.get(position).put("download_flag", 0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(context, "删除安装包：" + deleteFileName + " 成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "删除安装包：" + deleteFileName + " 失败，文件不存在！", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            apkRecyclerViewAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }
                    });
                } catch (JSONException e) {
                    Toast.makeText(context, "删除安装包失败！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        apk_file_RecyclerView.setLayoutManager(new LinearLayoutManager(context));
        apk_file_RecyclerView.setAdapter(apkRecyclerViewAdapter);
    }

    private void downloadNetDiskFile(Context context, JSONObject jsonObject, String fileDownloadUrl, int position) {
        long startTime = System.currentTimeMillis();
        String  downFileName = null;
        try {
            downFileName = jsonObject.getString("apk_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "创建文件下载任务，开始下载文件：" + downFileName, Toast.LENGTH_SHORT).show();
        try {
            jsonObjectList.get(position).put("download_flag", 2);
            apkRecyclerViewAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "开始下载时间：startTime=" + startTime);
        Request request = new Request.Builder().url(fileDownloadUrl).build();
        String finalDownFileName = downFileName;
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Message message = new Message();
                message.what = position;
                failDownloadNetDiskFileHandler.sendMessage(message);
                Log.e(TAG, "download failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                showDownloadNotification(context, finalDownFileName, position, "正在下载中......", 0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedSink bufferedSink;
                        File downFold = new File(ActivityUtil.TMP_APK_FILE_PATH);
                        if (!downFold.exists()) {
                            if (!downFold.mkdir()) {
                                Log.e(TAG, "onResponse: 下载文件夹创建成功");
                            } else {
                                Log.e(TAG, "onResponse: 下载文件夹创建失败");
                            }
                        }
                        File downFile = null;
                        try {
                            downFile = new File(downFold, jsonObject.getString("apk_name")+"_"+jsonObject.getString("apk_id") + ".download");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (downFile != null){
                            if (!downFile.exists()) {
                                try {
                                    if (!downFile.createNewFile()) {
                                        Log.e(TAG, "onResponse: 下载文件创建成功");
                                    } else {
                                        Log.e(TAG, "onResponse: 下载文件创建失败");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Sink sink = Okio.sink(downFile);
                                bufferedSink = Okio.buffer(sink);
                                bufferedSink.writeAll(Objects.requireNonNull(response.body()).source());
                                bufferedSink.close();
                                Message message = new Message();
                                message.obj = jsonObject;
                                message.what = position;
                                downloadNetDiskFileHandler.sendMessage(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();
                Log.e(TAG, "总共下载时间：totalTime=" + (System.currentTimeMillis() - startTime));
            }
        });
    }
}