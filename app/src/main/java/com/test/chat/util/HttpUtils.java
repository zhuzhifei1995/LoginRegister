package com.test.chat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.test.chat.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RequiresApi(api = Build.VERSION_CODES.M)
public class HttpUtils {

    private static final String TAG = Utils.TAG;
    private static final long READ_TIMEOUT = 60000;
    private static final long WRITE_TIMEOUT = 60000;
    private static final long CONNECT_TIMEOUT = 60000;
    private static MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static String JSON_RESULT = "";
    private OkHttpClient client;
    private Context context;

    public HttpUtils(Context context) {
        client = new OkHttpClient().newBuilder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
        this.context = context;
    }

    public String getRequest(String url) {
        Log.e(TAG, url);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            JSON_RESULT = Objects.requireNonNull(response.body()).string();
            Log.e(TAG, JSON_RESULT);
        } catch (IOException e) {
            Log.e(TAG, "HttpUtils getRequest()出错");
            e.printStackTrace();
        }
        return JSON_RESULT;
    }

    public String postRequest(String url, Map<String, String> parameter) {
        Log.e(TAG, url);
        FormBody formBody = null;
        if (parameter != null && parameter.size() != 0) {
            Builder builder = new Builder();
            for (Map.Entry<String, String> entry : parameter.entrySet()) {
                builder.addEncoded(entry.getKey(), entry.getValue());
            }
            formBody = builder.build();
        }
        Request request = null;
        if (formBody == null) {
            request = new Request.Builder().url(url).get().build();
        } else {
            request = new Request.Builder().url(url).post(formBody).build();
        }
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new Exception("Unexpected code " + response);
            }
            JSON_RESULT = Objects.requireNonNull(response.body()).string();
            Log.e(TAG, JSON_RESULT);
        } catch (Exception e) {
            Log.e(TAG, "HttpUtils postRequest()出错");
            e.printStackTrace();
        }
        return JSON_RESULT;
    }

    public Bitmap getImageBitmap(String imageUrl) {
        Request request = new Request.Builder().url(imageUrl).build();
        Bitmap bitmap;
        try {
            ResponseBody responseBody = client.newCall(request).execute().body();
            InputStream inputStream = null;
            if (responseBody != null) {
                inputStream = responseBody.byteStream();
            }
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            e.printStackTrace();
        }
        return bitmap;
    }

    public void getSoundFile(String fileUrl, String voiceName) {
        Request request = new Request.Builder().url(fileUrl).build();
        try {
            ResponseBody responseBody = client.newCall(request).execute().body();
            InputStream inputStream;
            if (responseBody != null) {
                String soundFileDir = Environment.getExternalStorageDirectory().getPath() + "/tmp/voice/" + voiceName + ".cache";
                inputStream = responseBody.byteStream();
                TmpFileUtil.writeToTmpFile(soundFileDir, inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String upLoadImageFile(final File file, final String url, final Map<String, String> parameter) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (parameter != null && parameter.size() != 0) {
            for (Map.Entry<String, String> entry : parameter.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        builder.addFormDataPart("file", file.getName(), RequestBody.create(file, MEDIA_TYPE_PNG));
        RequestBody requestBody = builder.build();
        String IMG_CLIENT_ID = "123";
        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + IMG_CLIENT_ID)
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "HttpUtils postRequest()出错");
                JSON_RESULT = "注册失败";
                throw new IOException("Unexpected code " + response);
            } else {
                JSON_RESULT = Objects.requireNonNull(response.body()).string();
            }
        } catch (IOException e) {
            JSON_RESULT = "注册失败";
            Log.e(TAG, "HttpUtils postRequest()出错");
            e.printStackTrace();
        }
        return JSON_RESULT;
    }
}
