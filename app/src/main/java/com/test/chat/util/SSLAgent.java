package com.test.chat.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SSLAgent {
    private final static String TAG = ActivityUtil.TAG;
    private final static boolean DEBUG = true;

    private static SSLAgent mSSLAgent;
    @SuppressLint("BadHostnameVerifier")
    private final HostnameVerifier mHostnameVerifier = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            Log.e(TAG, "hostname:" + hostname);
            return true;
        }

    };

    public static SSLAgent getInstance() {
        if (mSSLAgent == null) {
            mSSLAgent = new SSLAgent();
        }
        return mSSLAgent;
    }

    public void trustAllHttpsCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[1];
            TrustManager tm = new MyTrustManager();
            trustAllCerts[0] = tm;
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(mHostnameVerifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"TrustAllX509TrustManager", "CustomX509TrustManager"})
    private static class MyTrustManager implements TrustManager, X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

    }

}
