package com.test.chat.fragment;


import static com.test.chat.util.ActivityUtil.showDownloadNotification;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.chat.R;
import com.test.chat.activity.NetVideoPlayActivity;
import com.test.chat.activity.PhotoShowActivity;
import com.test.chat.adapter.FileRecyclerViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class NetDiskFileFragment1 extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ActivityUtil.TAG;
    private View netDiskFileFragmentView;
    private Context context;
    private RecyclerView net_disk_RecyclerView;
    private List<JSONObject> fileJSONObjectList;
    private View loading_layout;
    private FileRecyclerViewAdapter fileRecyclerViewAdapter;
    private final Handler downloadNetDiskFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            String downFileName = (String) message.obj;
            int position = message.what;
            try {
                File downloadFile = new File(ActivityUtil.TMP_DOWNLOAD_PATH, downFileName + ".download");
                TmpFileUtil.copyFile(downloadFile, new File(ActivityUtil.TMP_DOWNLOAD_PATH, downFileName + ".cache"));
                if (downloadFile.delete()) {
                    Log.e(TAG, "handleMessage: 临时下载文件删除成功" + downFileName);
                } else {
                    Log.e(TAG, "handleMessage: 临时下载文件删除失败" + downFileName);
                }
                fileJSONObjectList.get(position).put("download_flag", 1);
                fileRecyclerViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "download success");
            showDownloadNotification(context, downFileName, position, " 下载完成！", 1);
            Toast.makeText(context, downFileName + " 文件下载成功！", Toast.LENGTH_SHORT).show();
            super.handleMessage(message);
        }
    };
    private final Handler failDownloadNetDiskFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            Toast.makeText(context, "创建文件下载任务失败，网络异常！", Toast.LENGTH_SHORT).show();
            try {
                fileJSONObjectList.get(message.what).put("download_flag", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            fileRecyclerViewAdapter.notifyDataSetChanged();
            super.handleMessage(message);
        }
    };
    private final Handler getDownloadFilesHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    JSONArray jsonArray = jsonObject.getJSONArray("message");
                    fileJSONObjectList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject fileJSONObject = jsonArray.getJSONObject(i);
                        String cacheFileName = fileJSONObject.getString("file_name") + ".cache";
                        File cacheFile = new File(ActivityUtil.TMP_DOWNLOAD_PATH, cacheFileName);
                        if (cacheFile.exists()) {
                            String fileMD5 = TmpFileUtil.getFileMD5(ActivityUtil.TMP_DOWNLOAD_PATH, cacheFileName);
                            if (fileMD5 != null) {
                                if (fileMD5.equals(fileJSONObject.getString("md5_number"))) {
                                    fileJSONObject.put("download_flag", 1);
                                } else {
                                    fileJSONObject.put("download_flag", 2);
                                }
                            } else {
                                fileJSONObject.put("download_flag", 1);
                            }
                            Log.e(TAG, "handleMessage: " + fileMD5);
                        } else {
                            File downloadFile = new File(ActivityUtil.TMP_DOWNLOAD_PATH, fileJSONObject.getString("file_name") + ".download");
                            if (downloadFile.exists()) {
                                fileJSONObject.put("download_flag", 3);
                            } else {
                                fileJSONObject.put("download_flag", 0);
                            }
                        }
                        fileJSONObjectList.add(fileJSONObject);
                    }
                    if (fileJSONObjectList != null) {
                        fileRecyclerViewAdapter = new FileRecyclerViewAdapter(fileJSONObjectList);
                        net_disk_RecyclerView.setLayoutManager(new LinearLayoutManager(context));
                        net_disk_RecyclerView.setAdapter(fileRecyclerViewAdapter);
                        loading_layout.setVisibility(View.GONE);
                        fileRecyclerViewAdapter.setOnDownloadFileImageViewClickListener(new FileRecyclerViewAdapter.DownloadFileImageViewOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: 开始下载" + fileJSONObjectList.get(position));
                                try {
                                    fileRecyclerViewAdapter.notifyDataSetChanged();
                                    JSONObject fileJSONObject = fileJSONObjectList.get(position);
                                    int download_flag = fileJSONObject.getInt("download_flag");
                                    if (download_flag == 0) {
                                        downloadNetDiskFile(context, fileJSONObject.getString("file_name"),
                                                fileJSONObject.getString("file_download_url"), position);
                                    } else {
                                        Toast.makeText(context, "文件正在下载中！", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "创建文件下载任务失败！", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        fileRecyclerViewAdapter.setOnUpdateFileImageViewClickListener(new FileRecyclerViewAdapter.UpdateFileImageViewOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: 开始更新" + fileJSONObjectList.get(position));
                                try {
                                    fileRecyclerViewAdapter.notifyDataSetChanged();
                                    JSONObject fileJSONObject = fileJSONObjectList.get(position);
                                    int download_flag = fileJSONObject.getInt("download_flag");
                                    if (download_flag == 2) {
                                        downloadNetDiskFile(context, fileJSONObject.getString("file_name"),
                                                fileJSONObject.getString("file_download_url"), position);
                                    } else {
                                        Toast.makeText(context, "文件正在更新中！", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "创建文件更新任务失败！", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        fileRecyclerViewAdapter.setOnDeleteFileImageViewClickListener(new FileRecyclerViewAdapter.DeleteFileImageViewOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: 正在删除" + fileJSONObjectList.get(position));
                                try {
                                    String deleteFileName = fileJSONObjectList.get(position).getString("file_name");
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
                                        dialog_message_TextView.setText("是否删除文件 " + deleteFileName + "？");
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
                                            File deleteFile = new File(ActivityUtil.TMP_DOWNLOAD_PATH, deleteFileName + ".cache");
                                            if (deleteFile.delete()) {
                                                try {
                                                    fileJSONObjectList.get(position).put("download_flag", 0);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                Toast.makeText(context, "删除文件：" + deleteFileName + " 成功！", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, "删除文件：" + deleteFileName + " 失败，文件不存在！", Toast.LENGTH_SHORT).show();
                                            }
                                            fileRecyclerViewAdapter.notifyDataSetChanged();
                                            progressDialog.dismiss();
                                        }
                                    });
                                } catch (JSONException e) {
                                    Toast.makeText(context, "删除文件失败！", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        fileRecyclerViewAdapter.setOnFileDetailLinearLayoutClickListener(new FileRecyclerViewAdapter.FileDetailLinearLayoutOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: " + fileJSONObjectList.get(position));
                                try {
                                    String fileName = fileJSONObjectList.get(position).getString("file_name");
                                    if (Arrays.asList(ActivityUtil.MOVIE_TYPE).contains(fileName.substring(fileName.lastIndexOf(".") + 1))) {
                                        Intent intent = new Intent(context, NetVideoPlayActivity.class);
                                        String fileDownloadUrl = fileJSONObjectList.get(position).getString("file_download_url");
                                        intent.putExtra("fileDownloadUrl", fileDownloadUrl);
                                        intent.putExtra("fileName", fileName);
                                        startActivity(intent);
                                    } else if (Arrays.asList(ActivityUtil.MUSIC_TYPE).contains(fileName.substring(fileName.lastIndexOf(".") + 1))) {
                                        Intent intent = new Intent(context, PhotoShowActivity.class);
                                        String fileDownloadUrl = fileJSONObjectList.get(position).getString("file_download_url");
                                        intent.putExtra("flag", 5);
                                        intent.putExtra("voiceName", fileDownloadUrl);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(context, "文件格式不支持！", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(message);
        }
    };

    public NetDiskFileFragment1() {
    }

    private void downloadNetDiskFile(Context context, String downFileName, String fileDownloadUrl, int position) {
        long startTime = System.currentTimeMillis();
        Toast.makeText(context, "创建文件下载任务，开始下载文件：" + downFileName, Toast.LENGTH_SHORT).show();
        try {
            fileJSONObjectList.get(position).put("download_flag", 3);
            fileRecyclerViewAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "开始下载时间：startTime=" + startTime);
        Request request = new Request.Builder().url(fileDownloadUrl).build();
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
                showDownloadNotification(context, downFileName, position, "正在下载中......", 0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedSink bufferedSink;
                        File downFold = new File(ActivityUtil.TMP_DOWNLOAD_PATH);
                        if (!downFold.exists()) {
                            if (!downFold.mkdir()) {
                                Log.e(TAG, "onResponse: 下载文件夹创建成功");
                            } else {
                                Log.e(TAG, "onResponse: 下载文件夹创建失败");
                            }
                        }
                        File downFile = new File(downFold, downFileName + ".download");
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
                            message.obj = downFileName;
                            message.what = position;
                            downloadNetDiskFileHandler.sendMessage(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Log.e(TAG, "总共下载时间：totalTime=" + (System.currentTimeMillis() - startTime));
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        netDiskFileFragmentView = inflater.inflate(R.layout.fragment_net_disk_file1, container, false);
        initFragmentView();
        return netDiskFileFragmentView;
    }

    private void initFragmentView() {
        net_disk_RecyclerView = netDiskFileFragmentView.findViewById(R.id.net_disk_RecyclerView);
        SwipeRefreshLayout net_disk_SwipeRefreshLayout = netDiskFileFragmentView.findViewById(R.id.net_disk_SwipeRefreshLayout);
        net_disk_SwipeRefreshLayout.setOnRefreshListener(this);
        loading_layout = netDiskFileFragmentView.findViewById(R.id.loading_layout);
        net_disk_SwipeRefreshLayout.setRefreshing(false);
        loading_layout.setVisibility(View.VISIBLE);
        net_disk_SwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                Map<String, String> parameter = new HashMap<>();
                parameter.put("login_number", SharedPreferencesUtils.getString(context, "login_number", "", "login_number"));
                parameter.put("page_num", "3");
                try {
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_download_files", parameter);
                    message.what = 1;
                } catch (IOException e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                getDownloadFilesHandler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onRefresh() {
        initFragmentView();
    }
}