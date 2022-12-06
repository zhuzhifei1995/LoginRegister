package com.test.chat.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FileUploadFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private static final String PARAM = "PARAM";
    private String param;
    private View fileUploadFragmentView;
    private Context context;
    private List<JSONObject> fileJSONObjectList;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fileUploadFragmentView = inflater.inflate(R.layout.fragment_file_upload, container, false);
        initView();
        return fileUploadFragmentView;
    }

    private void initView() {
        Log.e(TAG, "initView: " + param);
        File[] fileList = new File(ActivityUtil.ROOT_FILE_PATH).listFiles();
        if (fileList != null) {
            if (fileList.length == 0){
                Toast.makeText(context, "没有文件！", Toast.LENGTH_LONG).show();
            }else {
                try {
                    fileJSONObjectList = new ArrayList<>();
                    for (File file : fileList) {
                        JSONObject fileJSONObject = new JSONObject();
                        if (file.isDirectory()) {
                            fileJSONObject.put("file_name",file.getName());
                            fileJSONObject.put("file_flag","0");
                        }
                        if (file.isFile()){
                            fileJSONObject.put("file_name",file.getName());
                            fileJSONObject.put("file_flag","1");
                        }
                        fileJSONObjectList.add(fileJSONObject);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(context, "没有文件！", Toast.LENGTH_LONG).show();
        }
    }
}