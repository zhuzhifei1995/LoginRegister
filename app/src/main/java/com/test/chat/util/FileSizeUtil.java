package com.test.chat.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FileSizeUtil {

    public static final int SIZE_TYPE_B = 1;
    public static final int SIZE_TYPE_KB = 2;
    public static final int SIZE_TYPE_MB = 3;
    public static final int SIZE_TYPE_GB = 4;
    private static final String TAG = ActivityUtil.TAG;

    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "获取文件大小失败!");
        }
        return formatFileSize(blockSize, sizeType);
    }

    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
                Log.e(TAG, "getAutoFileOrFilesSize: " + blockSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "获取文件大小失败!");
        }
        return formatFileSize(blockSize);
    }

    private static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(file);
                size = fileInputStream.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if (!file.createNewFile()) {
                    Log.e(TAG, "获取文件大小不存在!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    private static long getFileSizes(File file) {
        long size = 0;
        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File f : fileList) {
                if (f.isDirectory()) {
                    size = size + getFileSizes(f);
                } else {
                    try {
                        size = size + getFileSize(f);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return size;
    }

    private static String formatFileSize(long fileSize) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileSize == 0) {
            return wrongSize;
        }
        if (fileSize < 1024) {
            fileSizeString = decimalFormat.format((double) fileSize) + "B";
        } else if (fileSize < 1048576) {
            fileSizeString = decimalFormat.format((double) fileSize / 1024) + "KB";
        } else if (fileSize < 1073741824) {
            fileSizeString = decimalFormat.format((double) fileSize / 1048576) + "MB";
        } else {
            fileSizeString = decimalFormat.format((double) fileSize / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    private static double formatFileSize(long fileSize, int sizeType) {

        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZE_TYPE_B:
                fileSizeLong = Double.parseDouble(df.format((double) fileSize));
                break;
            case SIZE_TYPE_KB:
                fileSizeLong = Double.parseDouble(df.format((double) fileSize / 1024));
                break;
            case SIZE_TYPE_MB:
                fileSizeLong = Double.parseDouble(df.format((double) fileSize / 1048576));
                break;
            case SIZE_TYPE_GB:
                fileSizeLong = Double.parseDouble(df.format((double) fileSize / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }
}
