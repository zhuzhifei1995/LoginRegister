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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.test.chat.R;
import com.test.chat.activity.NetVideoPlayActivity;
import com.test.chat.activity.PhotoShowActivity;
import com.test.chat.adapter.FileListViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
import com.test.chat.util.SharedPreferencesUtils;
import com.test.chat.util.TmpFileUtil;
import com.test.chat.view.PullToRefreshListView;
import com.test.chat.view.view.PullToRefreshBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
public class NetDiskFileFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private static final String PARAM = "param";
    private final int IS_REFRESH = 1;
    private final int LOAD_MORE = 2;
    private final int LOAD_ERROR = 0;
    private String param;
    private Context context;
    private View netDiskFileFragmentView;
    private View loading_layout;
    private List<JSONObject> netDiskJSONObjectList;
    private int PAGE_NUM;
    private PullToRefreshListView net_disk_PullToRefreshListView;
    private final Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == IS_REFRESH) {
                Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
            }
            if (message.what == LOAD_MORE) {
                Toast.makeText(context, "?????????????????????", Toast.LENGTH_SHORT).show();
            }
            if (message.what == LOAD_ERROR) {
                Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
            }
            net_disk_PullToRefreshListView.onRefreshComplete();
        }
    };
    private FileListViewAdapter fileListViewAdapter;
    private final Handler failDownloadNetDiskFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            Toast.makeText(context, "????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
            try {
                netDiskJSONObjectList.get(message.what).put("download_flag", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            fileListViewAdapter.notifyDataSetChanged();
            super.handleMessage(message);
        }
    };
    private final Handler downloadNetDiskFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            String downFileName = (String) message.obj;
            int position = message.what;
            try {
                File downloadFile = new File(ActivityUtil.TMP_DOWNLOAD_PATH, downFileName + ".download");
                TmpFileUtil.copyFile(downloadFile, new File(ActivityUtil.TMP_DOWNLOAD_PATH, downFileName + ".cache"));
                if (downloadFile.delete()) {
                    Log.e(TAG, "handleMessage: ??????????????????????????????" + downFileName);
                } else {
                    Log.e(TAG, "handleMessage: ??????????????????????????????" + downFileName);
                }
                netDiskJSONObjectList.get(position).put("download_flag", 1);
                fileListViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "download success");
            showDownloadNotification(context, downFileName, position, " ???????????????", 1);
            Toast.makeText(context, downFileName + " ?????????????????????", Toast.LENGTH_SHORT).show();
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
                        netDiskJSONObjectList.add(fileJSONObject);
                    }
                    if (netDiskJSONObjectList != null) {
                        fileListViewAdapter = new FileListViewAdapter(context, R.layout.file_list_view, netDiskJSONObjectList);
                        net_disk_PullToRefreshListView.setAdapter(fileListViewAdapter);
                        loading_layout.setVisibility(View.GONE);
                        fileListViewAdapter.setOnDownloadFileImageViewClickListener(new FileListViewAdapter.DownloadFileImageViewOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: ????????????" + netDiskJSONObjectList.get(position));
                                try {
                                    fileListViewAdapter.notifyDataSetChanged();
                                    JSONObject fileJSONObject = netDiskJSONObjectList.get(position);
                                    int download_flag = fileJSONObject.getInt("download_flag");
                                    if (download_flag == 0) {
                                        downloadNetDiskFile(context, fileJSONObject.getString("file_name"),
                                                fileJSONObject.getString("file_download_url"), position);
                                    } else {
                                        Toast.makeText(context, "????????????????????????", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        fileListViewAdapter.setOnUpdateFileImageViewClickListener(new FileListViewAdapter.UpdateFileImageViewOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: ????????????" + netDiskJSONObjectList.get(position));
                                try {
                                    fileListViewAdapter.notifyDataSetChanged();
                                    JSONObject fileJSONObject = netDiskJSONObjectList.get(position);
                                    int download_flag = fileJSONObject.getInt("download_flag");
                                    if (download_flag == 2) {
                                        downloadNetDiskFile(context, fileJSONObject.getString("file_name"),
                                                fileJSONObject.getString("file_download_url"), position);
                                    } else {
                                        Toast.makeText(context, "????????????????????????", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(context, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        fileListViewAdapter.setOnDeleteFileImageViewClickListener(new FileListViewAdapter.DeleteFileImageViewOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: ????????????" + netDiskJSONObjectList.get(position));
                                try {
                                    String deleteFileName = netDiskJSONObjectList.get(position).getString("file_name");
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
                                        dialog_message_TextView.setText(new String("?????????????????? " + deleteFileName + "???"));
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
                                                    netDiskJSONObjectList.get(position).put("download_flag", 0);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                Toast.makeText(context, "???????????????" + deleteFileName + " ?????????", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, "???????????????" + deleteFileName + " ???????????????????????????", Toast.LENGTH_SHORT).show();
                                            }
                                            fileListViewAdapter.notifyDataSetChanged();
                                            progressDialog.dismiss();
                                        }
                                    });
                                } catch (JSONException e) {
                                    Toast.makeText(context, "?????????????????????", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        fileListViewAdapter.setOnFileDetailLinearLayoutClickListener(new FileListViewAdapter.FileDetailLinearLayoutOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Log.e(TAG, "onItemClick: " + netDiskJSONObjectList.get(position));
                                try {
                                    String fileName = netDiskJSONObjectList.get(position).getString("file_name");
                                    if (Arrays.asList(ActivityUtil.MOVIE_TYPE).contains(fileName.substring(fileName.lastIndexOf(".") + 1))) {
                                        Intent intent = new Intent(context, NetVideoPlayActivity.class);
                                        String fileDownloadUrl = netDiskJSONObjectList.get(position).getString("file_download_url");
                                        intent.putExtra("fileDownloadUrl", fileDownloadUrl);
                                        intent.putExtra("fileName", fileName);
                                        startActivity(intent);
                                    } else if (Arrays.asList(ActivityUtil.MUSIC_TYPE).contains(fileName.substring(fileName.lastIndexOf(".") + 1))) {
                                        Intent intent = new Intent(context, PhotoShowActivity.class);
                                        String fileDownloadUrl = netDiskJSONObjectList.get(position).getString("file_download_url");
                                        intent.putExtra("flag", 5);
                                        intent.putExtra("voiceName", fileDownloadUrl);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(context, "????????????????????????", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(message);
        }
    };
    private final Handler refreshHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message message) {
            int flag = message.what;
            if (flag == IS_REFRESH) {
                netDiskJSONObjectList.clear();
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
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
                            netDiskJSONObjectList.add(fileJSONObject);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                fileListViewAdapter.notifyDataSetChanged();
            }
            if (flag == LOAD_MORE) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                        if (jsonArray.length() == 0) {
                            flag = -1;
                            PAGE_NUM = PAGE_NUM - 1;
                            Toast.makeText(context, "????????????????????????", Toast.LENGTH_SHORT).show();
                        } else {
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
                                netDiskJSONObjectList.add(fileJSONObject);
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                fileListViewAdapter.notifyDataSetChanged();
            }
            if (flag == LOAD_ERROR) {
                Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show();
            }
            int finalFlag = flag;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        waitHandler.sendEmptyMessage(finalFlag);
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    };

    public NetDiskFileFragment() {
    }

    public static NetDiskFileFragment newInstance(String netDiskTitle) {
        NetDiskFileFragment netDiskFileFragment = new NetDiskFileFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, netDiskTitle);
        netDiskFileFragment.setArguments(bundle);
        return netDiskFileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            param = getArguments().getString(PARAM);
        }
        Log.e(TAG, "onCreate: " + param);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        netDiskFileFragmentView = inflater.inflate(R.layout.fragment_net_disk_file, viewGroup, false);
        initFragmentView();
        return netDiskFileFragmentView;
    }

    private void initFragmentView() {
        net_disk_PullToRefreshListView = netDiskFileFragmentView.findViewById(R.id.net_disk_PullToRefreshListView);
        loading_layout = netDiskFileFragmentView.findViewById(R.id.loading_layout);
        loading_layout.setVisibility(View.VISIBLE);
        netDiskJSONObjectList = new ArrayList<>();
        PAGE_NUM = 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("login_number", SharedPreferencesUtils.getString(context, "login_number", "", "login_number"));
                    parameter.put("page_num", "" + PAGE_NUM);
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_download_files", parameter);
                    message.what = 1;
                } catch (Exception e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                getDownloadFilesHandler.sendMessage(message);
            }
        }).start();

        net_disk_PullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                @SuppressLint("SimpleDateFormat")
                String timeLabel = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(timeLabel);
                if (refreshView.getHeaderLayout().isShown()) {
                    new Thread() {
                        public void run() {
                            PAGE_NUM = 1;
                            Message message = new Message();
                            try {
                                Map<String, String> parameter = new HashMap<>();
                                parameter.put("login_number", SharedPreferencesUtils.getString(context, "login_number", "", "login_number"));
                                parameter.put("page_num", "" + PAGE_NUM);
                                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_download_files", parameter);
                                message.what = IS_REFRESH;
                            } catch (Exception e) {
                                message.what = LOAD_ERROR;
                                e.printStackTrace();
                            }
                            refreshHandler.sendMessage(message);
                        }
                    }.start();
                }
                if (refreshView.getFooterLayout().isShown()) {
                    new Thread() {
                        public void run() {
                            PAGE_NUM = PAGE_NUM + 1;
                            Message message = new Message();
                            try {
                                Map<String, String> parameter = new HashMap<>();
                                parameter.put("login_number", SharedPreferencesUtils.getString(context, "login_number", "", "login_number"));
                                parameter.put("page_num", "" + PAGE_NUM);
                                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_download_files", parameter);
                                message.what = LOAD_MORE;
                            } catch (Exception e) {
                                message.what = LOAD_ERROR;
                                e.printStackTrace();
                            }
                            refreshHandler.sendMessage(message);
                        }
                    }.start();
                }
            }
        });
    }

    private void downloadNetDiskFile(Context context, String downFileName, String fileDownloadUrl, int position) {
        long startTime = System.currentTimeMillis();
        Toast.makeText(context, "????????????????????????????????????????????????" + downFileName, Toast.LENGTH_SHORT).show();
        try {
            netDiskJSONObjectList.get(position).put("download_flag", 3);
            fileListViewAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "?????????????????????startTime=" + startTime);
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
                showDownloadNotification(context, downFileName, position, "???????????????......", 0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedSink bufferedSink;
                        File downFold = new File(ActivityUtil.TMP_DOWNLOAD_PATH);
                        if (!downFold.exists()) {
                            if (!downFold.mkdir()) {
                                Log.e(TAG, "onResponse: ???????????????????????????");
                            } else {
                                Log.e(TAG, "onResponse: ???????????????????????????");
                            }
                        }
                        File downFile = new File(downFold, downFileName + ".download");
                        if (!downFile.exists()) {
                            try {
                                if (!downFile.createNewFile()) {
                                    Log.e(TAG, "onResponse: ????????????????????????");
                                } else {
                                    Log.e(TAG, "onResponse: ????????????????????????");
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
                Log.e(TAG, "?????????????????????totalTime=" + (System.currentTimeMillis() - startTime));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        net_disk_PullToRefreshListView.setFocusable(false);
    }
}