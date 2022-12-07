package com.test.chat.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chat.R;
import com.test.chat.adapter.FileItemRecyclerViewAdapter;
import com.test.chat.adapter.FileUploadListViewAdapter;
import com.test.chat.util.ActivityUtil;
import com.test.chat.view.PullToRefreshListView;
import com.test.chat.view.view.PullToRefreshBase;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressLint("NotifyDataSetChanged")
public class FileUploadFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private static final String PARAM = "param";
    private String param;
    private View fileUploadFragmentView;
    private Context context;
    private List<JSONObject> fileJSONObjectList;
    private FileUploadListViewAdapter fileUploadListViewAdapter;
    private PullToRefreshListView file_upload_PullToRefreshListView;
    private final Handler waitHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            file_upload_PullToRefreshListView.onRefreshComplete();
            if (message.what == 0) {
                Toast.makeText(context, "刷新成功！", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private List<String> fileItemList;
    private FileItemRecyclerViewAdapter fileItemRecyclerViewAdapter;
    private TextView file_empty_TextView;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message message) {
            fileJSONObjectList.clear();
            fileItemList.clear();
            fileItemList.add("本地磁盘");
            fileItemRecyclerViewAdapter.notifyDataSetChanged();
            File[] fileList = (File[]) message.obj;
            int flag = message.what;
            Log.e(TAG, "handleMessage: " + flag);
            if (fileList != null) {
                Log.e(TAG, "initView: " + Arrays.toString(fileList));
                try {
                    List<JSONObject> filesJSONObjectList = new ArrayList<>();
                    List<JSONObject> dirJSONObjectList = new ArrayList<>();
                    for (File file : fileList) {
                        JSONObject fileJSONObject = new JSONObject();
                        if (!file.getName().startsWith(".")) {
                            if (file.isDirectory() && file.exists() && file.canRead()) {
                                fileJSONObject.put("file_type", "0");
                                fileJSONObject.put("file_path", file.getAbsoluteFile());
                                fileJSONObject.put("file_name", file.getName());
                                filesJSONObjectList.add(fileJSONObject);
                            }
                            if (file.isFile() && file.exists() && file.canRead()) {
                                fileJSONObject.put("file_type", "1");
                                fileJSONObject.put("file_path", file.getAbsoluteFile());
                                fileJSONObject.put("file_name", file.getName());
                                dirJSONObjectList.add(fileJSONObject);
                            }
                        }
                    }
                    fileJSONObjectList.addAll(filesJSONObjectList);
                    fileJSONObjectList.addAll(dirJSONObjectList);
                    if (fileJSONObjectList.size() == 0) {
                        file_empty_TextView.setVisibility(View.VISIBLE);
                    } else {
                        file_empty_TextView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    file_empty_TextView.setVisibility(View.VISIBLE);
                }
            } else {
                file_empty_TextView.setVisibility(View.VISIBLE);
            }
            fileUploadListViewAdapter.notifyDataSetChanged();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                        waitHandler.sendEmptyMessage(flag);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            super.handleMessage(message);
        }
    };


    public FileUploadFragment() {
    }

    public static FileUploadFragment newInstance(String param) {
        FileUploadFragment fileUploadFragment = new FileUploadFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, param);
        fileUploadFragment.setArguments(bundle);
        return fileUploadFragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        fileUploadFragmentView = inflater.inflate(R.layout.fragment_file_upload, viewGroup, false);
        initView();
        return fileUploadFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        file_upload_PullToRefreshListView.setFocusable(false);
    }

    private void initView() {
        Log.e(TAG, "initView: " + param);
        file_upload_PullToRefreshListView = fileUploadFragmentView.findViewById(R.id.file_upload_PullToRefreshListView);
        RecyclerView file_RecyclerView = fileUploadFragmentView.findViewById(R.id.file_RecyclerView);
        fileItemList = new ArrayList<>();
        fileItemList.add("本地磁盘");
        fileItemRecyclerViewAdapter = new FileItemRecyclerViewAdapter(context,fileItemList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        file_RecyclerView.setLayoutManager(linearLayoutManager);
        file_RecyclerView.setAdapter(fileItemRecyclerViewAdapter);
        file_empty_TextView = fileUploadFragmentView.findViewById(R.id.file_empty_TextView);
        fileJSONObjectList = new ArrayList<>();
        File[] fileList = new File(ActivityUtil.ROOT_FILE_PATH).listFiles();
        if (fileList != null) {
            Log.e(TAG, "initView: " + Arrays.toString(fileList));
            Log.e(TAG, "initView: " + fileList.length);
            try {
                List<JSONObject> filesJSONObjectList = new ArrayList<>();
                List<JSONObject> dirJSONObjectList = new ArrayList<>();
                for (File file : fileList) {
                    JSONObject fileJSONObject = new JSONObject();
                    if (!file.getName().startsWith(".")) {
                        if (file.isDirectory()) {
                            fileJSONObject.put("file_type", "0");
                            fileJSONObject.put("file_path", file.getAbsoluteFile());
                            fileJSONObject.put("file_name", file.getName());
                            filesJSONObjectList.add(fileJSONObject);
                        }
                        if (file.isFile()) {
                            fileJSONObject.put("file_type", "1");
                            fileJSONObject.put("file_path", file.getAbsoluteFile());
                            fileJSONObject.put("file_name", file.getName());
                            dirJSONObjectList.add(fileJSONObject);
                        }
                    }
                }
                fileJSONObjectList.addAll(filesJSONObjectList);
                fileJSONObjectList.addAll(dirJSONObjectList);
                if (fileJSONObjectList.size() == 0) {
                    file_empty_TextView.setVisibility(View.VISIBLE);
                } else {
                    file_empty_TextView.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                file_empty_TextView.setVisibility(View.VISIBLE);
            }
        } else {
            file_empty_TextView.setVisibility(View.VISIBLE);
        }
        file_upload_PullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                @SuppressLint("SimpleDateFormat")
                String timeLabel = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss").format(new Date());
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(timeLabel);
                if (refreshView.getHeaderLayout().isShown()) {
                    new Thread() {
                        public void run() {
                            File[] fileList = new File(ActivityUtil.ROOT_FILE_PATH).listFiles();
                            Message message = new Message();
                            message.obj = fileList;
                            message.what = 0;
                            refreshHandler.sendMessage(message);
                        }
                    }.start();
                }
            }
        });
        fileUploadListViewAdapter = new FileUploadListViewAdapter(context, R.layout.file_upload_list_view, fileJSONObjectList);
        file_upload_PullToRefreshListView.setAdapter(fileUploadListViewAdapter);
        fileUploadListViewAdapter.setOnFileDetailLinearLayoutClickListener(new FileUploadListViewAdapter.FileDetailLinearLayoutOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                JSONObject jsonObject = fileJSONObjectList.get(position);
                try {
                    int fileType = jsonObject.getInt("file_type");
                    String filePath = jsonObject.getString("file_path");
                    String fileName = jsonObject.getString("file_name");
                    Log.e(TAG, "onItemClick: " + fileType + "------" + filePath + "------" + fileName);
                    if (fileType == 0) {
                        fileJSONObjectList.clear();
                        File[] fileList = new File(filePath).listFiles();
                        if (fileList != null) {
                            List<JSONObject> filesJSONObjectList = new ArrayList<>();
                            List<JSONObject> dirJSONObjectList = new ArrayList<>();
                            Log.e(TAG, "onItemClick: " + Arrays.toString(fileList));
                            Log.e(TAG, "onItemClick: " + fileList.length);
                            for (File file : fileList) {
                                JSONObject fileJSONObject = new JSONObject();
                                if (!file.getName().startsWith(".")) {
                                    if (file.isDirectory()) {
                                        fileJSONObject.put("file_type", "0");
                                        fileJSONObject.put("file_path", file.getAbsoluteFile());
                                        fileJSONObject.put("file_name", file.getName());
                                        filesJSONObjectList.add(fileJSONObject);
                                    }
                                    if (file.isFile()) {
                                        fileJSONObject.put("file_type", "1");
                                        fileJSONObject.put("file_path", file.getAbsoluteFile());
                                        fileJSONObject.put("file_name", file.getName());
                                        dirJSONObjectList.add(fileJSONObject);
                                    }
                                }
                            }
                            fileJSONObjectList.addAll(filesJSONObjectList);
                            fileJSONObjectList.addAll(dirJSONObjectList);
                            if (fileJSONObjectList.size() == 0) {
                                file_empty_TextView.setVisibility(View.VISIBLE);
                            } else {
                                file_empty_TextView.setVisibility(View.GONE);
                            }
                        } else {
                            file_empty_TextView.setVisibility(View.VISIBLE);
                        }
                        fileItemList.add(fileName);
                        fileUploadListViewAdapter.notifyDataSetChanged();
                        fileItemRecyclerViewAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "文件不支持打开！", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        fileUploadListViewAdapter.setOnFileDetailLinearLayoutLongClickListener(new FileUploadListViewAdapter.FileDetailLinearLayoutOnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                Toast.makeText(context, "长按！", Toast.LENGTH_SHORT).show();
            }
        });

        fileItemRecyclerViewAdapter.setOnFileItemTextViewClickListener(new FileItemRecyclerViewAdapter.FileItemTextViewOnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (fileItemList.size() != 1) {
                    if (position == 0) {
                        new Thread() {
                            public void run() {
                                File[] fileList = new File(ActivityUtil.ROOT_FILE_PATH).listFiles();
                                Message message = new Message();
                                message.obj = fileList;
                                message.what = 1;
                                refreshHandler.sendMessage(message);
                            }
                        }.start();
                    } else if (position < fileItemList.size() - 1) {
                        fileJSONObjectList.clear();
                        StringBuilder filePathStringBuilder = new StringBuilder();
                        List<String> tmpFileItemList = new ArrayList<>();
                        tmpFileItemList.add("本地磁盘");
                        for (int i = 1; i <= position; i++) {
                            String name = fileItemList.get(i);
                            tmpFileItemList.add(name);
                            filePathStringBuilder.append("/").append(name);
                        }
                        fileItemList.clear();
                        fileItemList.addAll(tmpFileItemList);
                        File[] fileList = new File(ActivityUtil.ROOT_FILE_PATH + filePathStringBuilder).listFiles();
                        if (fileList != null) {
                            Log.e(TAG, "initView: " + Arrays.toString(fileList));
                            Log.e(TAG, "initView: " + fileList.length);
                            try {
                                List<JSONObject> filesJSONObjectList = new ArrayList<>();
                                List<JSONObject> dirJSONObjectList = new ArrayList<>();
                                for (File file : fileList) {
                                    JSONObject fileJSONObject = new JSONObject();
                                    if (!file.getName().startsWith(".")) {
                                        if (file.isDirectory()) {
                                            fileJSONObject.put("file_type", "0");
                                            fileJSONObject.put("file_path", file.getAbsoluteFile());
                                            fileJSONObject.put("file_name", file.getName());
                                            filesJSONObjectList.add(fileJSONObject);
                                        }
                                        if (file.isFile()) {
                                            fileJSONObject.put("file_type", "1");
                                            fileJSONObject.put("file_path", file.getAbsoluteFile());
                                            fileJSONObject.put("file_name", file.getName());
                                            dirJSONObjectList.add(fileJSONObject);
                                        }
                                    }
                                }
                                fileJSONObjectList.addAll(filesJSONObjectList);
                                fileJSONObjectList.addAll(dirJSONObjectList);
                                if (fileJSONObjectList.size() == 0) {
                                    file_empty_TextView.setVisibility(View.VISIBLE);
                                } else {
                                    file_empty_TextView.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                file_empty_TextView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            file_empty_TextView.setVisibility(View.VISIBLE);
                        }
                        fileUploadListViewAdapter.notifyDataSetChanged();
                        fileItemRecyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}