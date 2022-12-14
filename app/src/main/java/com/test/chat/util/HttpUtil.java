package com.test.chat.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.test.chat.R;
import com.test.chat.util.https.SSLSocketClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
public class HttpUtil {

    private static final String TAG = ActivityUtil.TAG;
    private static final long READ_TIMEOUT = 6000;
    private static final long WRITE_TIMEOUT = 6000;
    private static final long CONNECT_TIMEOUT = 6000;
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static String JSON_RESULT = "";
    private final OkHttpClient client;
    private Context context;

    public HttpUtil() {
        Log.e(TAG, "新建HttpUtil工具类成功：");
        client = new OkHttpClient().newBuilder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .build();
    }

    public HttpUtil(Context context) {
        Log.e(TAG, "新建HttpUtil工具类成功：");
        client = new OkHttpClient().newBuilder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .build();
        this.context = context;
    }

    public HttpUtil(Context context, int multiple) {
        Log.e(TAG, "新建倍数的HttpUtil工具类成功：");
        client = new OkHttpClient().newBuilder().connectTimeout(CONNECT_TIMEOUT * multiple, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT * multiple, TimeUnit.MILLISECONDS).writeTimeout(WRITE_TIMEOUT * multiple, TimeUnit.MILLISECONDS)
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .build();
        this.context = context;
    }

    @SuppressLint("TrustAllX509TrustManager,CustomX509TrustManager,BadHostnameVerifier")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    public String getRequest(String url) throws IOException {
        Log.e(TAG, "请求的地址链接：" + url);
        Log.e(TAG, url);
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        try {
            assert response.body() != null;
            JSON_RESULT = Objects.requireNonNull(response.body()).string();
            Log.e(TAG, JSON_RESULT);
        } catch (IOException e) {
            Log.e(TAG, "HttpUtils getRequest()出错");
            e.printStackTrace();
        }
        return JSON_RESULT;
    }

    public String postRequest(String url, Map<String, String> parameter) throws IOException {
        Log.e(TAG, "请求的地址链接：" + url);
        Log.e(TAG, "请求的地址参数：" + parameter);
        FormBody formBody = null;
        if (parameter != null && parameter.size() != 0) {
            Builder builder = new Builder();
            for (Map.Entry<String, String> entry : parameter.entrySet()) {
                builder.addEncoded(entry.getKey(), entry.getValue());
            }
            formBody = builder.build();
        }
        Request request;
        if (formBody == null) {
            request = new Request.Builder().url(url).get().build();
        } else {
            request = new Request.Builder().url(url).post(formBody).build();
        }
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        JSON_RESULT = Objects.requireNonNull(response.body()).string();
        Log.e(TAG, JSON_RESULT);
        return JSON_RESULT;
    }

    public Bitmap getImageToBitmap(String imageUrl) {
        Bitmap bitmap;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();
            InputStream inputStream = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_progress);
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap getImageBitmap(String imageUrl) {
        Log.e(TAG, "请求的图片链接：" + imageUrl);
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

    public Bitmap getImageBitmap(String imageUrl, int resourceId) {
        Log.e(TAG, "请求的图片链接：" + imageUrl);
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
            bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            e.printStackTrace();
        }
        return bitmap;
    }

    public void getSoundFile(String fileUrl, String voiceName) {
        Log.e(TAG, "请求下载的文件链接：" + fileUrl);
        Log.e(TAG, "请求下载的文件名称：" + voiceName);
        Request request = new Request.Builder().url(fileUrl).build();
        try {
            ResponseBody responseBody = client.newCall(request).execute().body();
            InputStream inputStream;
            if (responseBody != null) {
                inputStream = responseBody.byteStream();
                TmpFileUtil.writeToTmpFile(voiceName, inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String upLoadImageFile(@NonNull final File file, final String url, final Map<String, String> parameter) {
        Log.e(TAG, "请求上传的文件链接：" + url);
        Log.e(TAG, "请求上传的文件位置：" + file.getAbsolutePath());
        Log.e(TAG, "请求上传的文件参数：" + parameter);
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
