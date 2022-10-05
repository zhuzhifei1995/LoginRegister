package com.test.chat.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class TmpFileUtil {

    private static final String TAG = Utils.TAG;

    public static void writeToTmpFile(String soundFile, InputStream inputStream) {
        Log.e(TAG, "writeToTmpFile: 保存声音" + soundFile);
        try {
            int index;
            byte[] bytes = new byte[1024];
            FileOutputStream fileOutputStream = new FileOutputStream(soundFile);
            while ((index = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, index);
                fileOutputStream.flush();
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeJSONToFile(String json, String fileDir, String fileName) {
        Log.e(TAG, "writeJSONToFile: ");
        try {
            File file = new File(fileDir);
            if (!file.exists()) {
                if (file.mkdir()) {
                    Log.e(TAG, "文件夹创建成功：" + fileName);
                } else {
                    Log.e(TAG, "文件夹创建失败: " + fileName);
                }
            }
            FileWriter fw = new FileWriter(fileDir + "/" + fileName);
            fw.flush();
            fw.write(json);
            fw.close();
            Log.e(TAG, "文件写入成功" + fileName);
        } catch (Exception e) {
            Log.e(TAG, "文件写入失败" + fileName);
            e.printStackTrace();
        }
    }

    public static String getJSONFileString(String fileDir, String fileName) {
        Log.e(TAG, "getJSONFileString: ");
        StringBuilder json = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(fileDir, fileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                json.append(line).append("\n");
            }
            fileInputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "文件读取失败，未点击过好友！");
        }
        return json.toString();
    }

    public static void copyFile(File fromFile, File toFile) {
        Log.e(TAG, "copyFile: ");
        if (!fromFile.exists()) {
            return;
        }
        if (!fromFile.isFile()) {
            return;
        }
        if (!fromFile.canRead()) {
            return;
        }
        if (!Objects.requireNonNull(toFile.getParentFile()).exists()) {
            if (!toFile.getParentFile().mkdirs()) {
                Log.e("TAG", "文件夹创建失败");
            } else {
                Log.e("TAG", "文件夹创建成功");
            }
        }
        if (toFile.exists()) {
            if (!toFile.delete()) {
                Log.e("TAG", "文件夹删除失败");
            } else {
                Log.e("TAG", "文件夹删除成功");
            }
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(fromFile);
            FileOutputStream fileOutputStream = new FileOutputStream(toFile);
            byte[] fileByte = new byte[1024];
            int c;
            while ((c = fileInputStream.read(fileByte)) > 0) {
                fileOutputStream.write(fileByte, 0, c);
            }
            fileInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static void deleteFileCache(File dirFile) {
        Log.e(TAG, "deleteFileCache: ");
        if (dirFile == null || !dirFile.exists() || !dirFile.isDirectory())
            return;
        for (File file : Objects.requireNonNull(dirFile.listFiles())) {
            if (file.isFile()) {
                if (file.delete()) {
                    Log.e(TAG, "deleteFriendListViewCache: friendCacheFile临时图片删除成功");
                } else {
                    Log.e(TAG, "deleteFriendListViewCache: friendCacheFile无生成的图片");
                }
            } else if (file.isDirectory())
                deleteFileCache(file);
        }
    }
}
