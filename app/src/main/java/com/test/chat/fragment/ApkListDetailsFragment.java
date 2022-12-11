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
import com.test.chat.activity.ApkDetailActivity;
import com.test.chat.adapter.ApkListViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.util.HttpUtil;
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
public class ApkListDetailsFragment extends Fragment {

    private final static String TAG = ActivityUtil.TAG;
    private static final String PARAM = "param";
    private final int IS_REFRESH = 1;
    private final int LOAD_MORE = 2;
    private final int LOAD_ERROR = 0;
    private String param;
    private View appListDetailsFragmentView;
    private Context context;
    private List<JSONObject> appJSONObjectList;
    private View loading_layout;

    private PullToRefreshListView app_list_PullToRefreshListView;
    private final Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == IS_REFRESH) {
                Toast.makeText(context, "刷新成功！", Toast.LENGTH_SHORT).show();
            }
            if (message.what == LOAD_MORE) {
                Toast.makeText(context, "加载更多成功！", Toast.LENGTH_SHORT).show();
            }
            if (message.what == LOAD_ERROR) {
                Toast.makeText(context, "加载失败！", Toast.LENGTH_SHORT).show();
            }
            app_list_PullToRefreshListView.onRefreshComplete();
        }
    };
    private ApkListViewAdapter apkListViewAdapter;
    private final Handler downloadApkHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            JSONObject jsonObject = (JSONObject) message.obj;
            int position = message.what;
            try {
                String downFileName = jsonObject.getString("apk_name") + "_" + jsonObject.getString("apk_id");
                File downloadFile = new File(ActivityUtil.TMP_APK_FILE_PATH, downFileName + ".download");
                TmpFileUtil.copyFile(downloadFile, new File(ActivityUtil.TMP_APK_FILE_PATH, downFileName + ".cache"));
                if (downloadFile.delete()) {
                    Log.e(TAG, "handleMessage: 临时下载文件删除成功" + downFileName);
                } else {
                    Log.e(TAG, "handleMessage: 临时下载文件删除失败" + downFileName);
                }
                appJSONObjectList.get(position).put("download_flag", 1);
                apkListViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "download success");
            try {
                showDownloadNotification(context, "应用 " + jsonObject.getString("apk_name"), position, " 下载完成！", 1);
                Toast.makeText(context, "应用 " + jsonObject.getString("apk_name") + " 下载成功！", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.handleMessage(message);
        }
    };
    private final Handler failDownloadApkFileHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            Toast.makeText(context, "创建文件下载任务失败，网络异常！", Toast.LENGTH_SHORT).show();
            try {
                appJSONObjectList.get(message.what).put("download_flag", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            apkListViewAdapter.notifyDataSetChanged();
            super.handleMessage(message);
        }
    };
    private final Handler getApkListHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject apkJSONObject = jsonArray.getJSONObject(i);
                            File downloadFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                    + "_" + apkJSONObject.getString("apk_id") + ".download");
                            if (downloadFile.exists()) {
                                apkJSONObject.put("download_flag", 2);
                            } else {
                                File cacheFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                        + "_" + apkJSONObject.getString("apk_id") + ".cache");
                                if (cacheFile.exists()) {
                                    apkJSONObject.put("download_flag", 1);
                                } else {
                                    apkJSONObject.put("download_flag", 0);
                                }
                            }
                            appJSONObjectList.add(apkJSONObject);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apkListViewAdapter = new ApkListViewAdapter(context, R.layout.apk_list_view, appJSONObjectList);
                for (int i = 0; i < appJSONObjectList.size(); i++) {
                    try {
                        JSONObject apkJSONObject = appJSONObjectList.get(i);
                        File downloadFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                + "_" + apkJSONObject.getString("apk_id") + ".download");
                        if (downloadFile.exists()) {
                            apkJSONObject.put("download_flag", 2);
                        } else {
                            File cacheFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                    + "_" + apkJSONObject.getString("apk_id") + ".cache");
                            if (cacheFile.exists()) {
                                apkJSONObject.put("download_flag", 1);
                            } else {
                                apkJSONObject.put("download_flag", 0);
                            }
                        }
                        apkListViewAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                apkListViewAdapter.setOnDownloadApkImageViewClickListener(new ApkListViewAdapter.DownloadApkImageViewOnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        try {
                            downloadApkFile(context, appJSONObjectList.get(position)
                                    , appJSONObjectList.get(position).getString("apk_download_url"), position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                apkListViewAdapter.setOnInstallImageViewClickListener(new ApkListViewAdapter.InstallImageViewOnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        try {
                            String apkFileName = appJSONObjectList.get(position).getString("apk_name") + "_" + appJSONObjectList.get(position).getString("apk_id");
                            File cacheFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkFileName + ".cache");
                            File apkFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkFileName + ".apk");
                            TmpFileUtil.copyFile(cacheFile, apkFile);
                            ActivityUtil.silentInstallApk(apkFile, context);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                apkListViewAdapter.setOnDeleteImageViewClickListener(new ApkListViewAdapter.DeleteImageViewOnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Log.e(TAG, "onItemClick: 正在删除安装包" + appJSONObjectList.get(position));
                        try {
                            String deleteFileName = appJSONObjectList.get(position).getString("apk_name");
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
                                        File deleteFile = new File(ActivityUtil.TMP_APK_FILE_PATH, deleteFileName +
                                                "_" + appJSONObjectList.get(position).getString("apk_id") + ".cache");
                                        if (deleteFile.delete()) {
                                            appJSONObjectList.get(position).put("download_flag", 0);
                                            Toast.makeText(context, "删除安装包：" + deleteFileName + " 成功！", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "删除安装包：" + deleteFileName + " 失败，文件不存在！", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    apkListViewAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();
                                }
                            });
                        } catch (JSONException e) {
                            Toast.makeText(context, "删除安装包失败！", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                apkListViewAdapter.setOnApkDetailLinearLayoutClickListener(new ApkListViewAdapter.ApkDetailLinearLayoutOnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        try {
                            String apkDetailHtml = appJSONObjectList.get(position).getString("apk_detail_html");
                            Intent intent = new Intent(context, ApkDetailActivity.class);
                            intent.putExtra("apkDetailHtml", apkDetailHtml);
                            context.startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                app_list_PullToRefreshListView.setAdapter(apkListViewAdapter);
                loading_layout.setVisibility(View.GONE);
            } else {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            super.handleMessage(message);
        }
    };
    private int PAGE_NUM;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message message) {
            int flag = message.what;
            if (flag == IS_REFRESH) {
                appJSONObjectList.clear();
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject apkJSONObject = jsonArray.getJSONObject(i);
                            File downloadFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                    + "_" + apkJSONObject.getString("apk_id") + ".download");
                            if (downloadFile.exists()) {
                                apkJSONObject.put("download_flag", 2);
                            } else {
                                File cacheFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                        + "_" + apkJSONObject.getString("apk_id") + ".cache");
                                if (cacheFile.exists()) {
                                    apkJSONObject.put("download_flag", 1);
                                } else {
                                    apkJSONObject.put("download_flag", 0);
                                }
                            }
                            appJSONObjectList.add(apkJSONObject);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                apkListViewAdapter.notifyDataSetChanged();
            }
            if (flag == LOAD_MORE) {
                try {
                    JSONObject jsonObject = new JSONObject((String) message.obj);
                    if (jsonObject.getString("code").equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                        if (jsonArray.length() == 0) {
                            flag = -1;
                            PAGE_NUM = PAGE_NUM - 1;
                            Toast.makeText(context, "没有更多应用了！", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject apkJSONObject = jsonArray.getJSONObject(i);
                                File downloadFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                        + "_" + apkJSONObject.getString("apk_id") + ".download");
                                if (downloadFile.exists()) {
                                    apkJSONObject.put("download_flag", 2);
                                } else {
                                    File cacheFile = new File(ActivityUtil.TMP_APK_FILE_PATH, apkJSONObject.getString("apk_name")
                                            + "_" + apkJSONObject.getString("apk_id") + ".cache");
                                    if (cacheFile.exists()) {
                                        apkJSONObject.put("download_flag", 1);
                                    } else {
                                        apkJSONObject.put("download_flag", 0);
                                    }
                                }
                                appJSONObjectList.add(apkJSONObject);
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                apkListViewAdapter.notifyDataSetChanged();
            }
            if (flag == LOAD_ERROR) {
                Toast.makeText(context, "网络异常！", Toast.LENGTH_SHORT).show();
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

    public ApkListDetailsFragment() {
    }

    public static ApkListDetailsFragment newInstance(JSONObject jsonObject) {
        ApkListDetailsFragment apkListDetailsFragment = new ApkListDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, jsonObject.toString());
        apkListDetailsFragment.setArguments(bundle);
        return apkListDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        if (getArguments() != null) {
            param = getArguments().getString(PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle savedInstanceState) {
        appListDetailsFragmentView = inflater.inflate(R.layout.fragment_app_list_details, viewGroup, false);
        initView();
        return appListDetailsFragmentView;
    }

    private void initView() {
        loading_layout = appListDetailsFragmentView.findViewById(R.id.loading_layout);
        loading_layout.setVisibility(View.VISIBLE);
        app_list_PullToRefreshListView = appListDetailsFragmentView.findViewById(R.id.app_list_PullToRefreshListView);
        appJSONObjectList = new ArrayList<>();
        PAGE_NUM = 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    JSONObject kindJSONObject = new JSONObject(param);
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put("kind_link", kindJSONObject.getString("kind_link"));
                    parameter.put("kind_name", kindJSONObject.getString("kind_name"));
                    parameter.put("kind_page", "" + PAGE_NUM);
                    message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_apk_list_by_kind_link", parameter);
                    message.what = 1;
                } catch (Exception e) {
                    message.what = 0;
                    e.printStackTrace();
                }
                getApkListHandler.sendMessage(message);
            }
        }).start();

        app_list_PullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
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
                                JSONObject kindJSONObject = new JSONObject(param);
                                Map<String, String> parameter = new HashMap<>();
                                parameter.put("kind_link", kindJSONObject.getString("kind_link"));
                                parameter.put("kind_name", kindJSONObject.getString("kind_name"));
                                parameter.put("kind_page", "" + PAGE_NUM);
                                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_apk_list_by_kind_link", parameter);
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
                                JSONObject kindJSONObject = new JSONObject(param);
                                Map<String, String> parameter = new HashMap<>();
                                parameter.put("kind_link", kindJSONObject.getString("kind_link"));
                                parameter.put("kind_name", kindJSONObject.getString("kind_name"));
                                parameter.put("kind_page", "" + PAGE_NUM);
                                message.obj = new HttpUtil(context).postRequest(ActivityUtil.NET_URL + "/get_apk_list_by_kind_link", parameter);
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

    private void downloadApkFile(Context context, JSONObject jsonObject, String fileDownloadUrl, int position) {
        long startTime = System.currentTimeMillis();
        String downFileName = null;
        try {
            downFileName = jsonObject.getString("apk_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "创建应用下载任务，开始下载应用：" + downFileName, Toast.LENGTH_SHORT).show();
        try {
            appJSONObjectList.get(position).put("download_flag", 2);
            apkListViewAdapter.notifyDataSetChanged();
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
                failDownloadApkFileHandler.sendMessage(message);
                Log.e(TAG, "download failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                showDownloadNotification(context, "应用： " + finalDownFileName, position, "正在下载中......", 0);
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
                            downFile = new File(downFold, jsonObject.getString("apk_name") + "_" + jsonObject.getString("apk_id") + ".download");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (downFile != null) {
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
                                downloadApkHandler.sendMessage(message);
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

    @Override
    public void onResume() {
        super.onResume();
        app_list_PullToRefreshListView.setFocusable(false);
    }

}