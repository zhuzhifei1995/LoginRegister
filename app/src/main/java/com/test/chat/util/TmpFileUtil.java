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

    private static final String TAG = ActivityUtil.TAG;

    public static void writeToTmpFile(String voiceName, InputStream inputStream) {
        Log.e(TAG, "保存声音文件：" + voiceName);
        File file = new File(ActivityUtil.TMP_VOICE_FILE_PATH);
        if (!file.exists()) {
            if (file.mkdir()) {
                Log.e(TAG, "文件夹创建成功：" + ActivityUtil.TMP_VOICE_FILE_PATH);
            } else {
                Log.e(TAG, "文件夹创建失败: " + ActivityUtil.TMP_VOICE_FILE_PATH);
            }
        }
        try {
            int index;
            byte[] bytes = new byte[1024];
            FileOutputStream fileOutputStream = new FileOutputStream(ActivityUtil.TMP_VOICE_FILE_PATH+"/"+voiceName+".cache");
            while ((index = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, index);
                fileOutputStream.flush();
            }
            inputStream.close();
            fileOutputStream.close();
            Log.e(TAG, "保存声音文件成功：" + voiceName);
        } catch (IOException e) {
            Log.e(TAG, "保存声音文件失败：" + voiceName);
            e.printStackTrace();
        }
    }

    public static void writeJSONToFile(String json, String fileDir, String fileName) {
        Log.e(TAG, "文件开始写入数据：" + fileDir + "/" + fileName);
        try {
            File file = new File(fileDir);
            if (!file.exists()) {
                if (file.mkdir()) {
                    Log.e(TAG, "文件夹创建成功：" + fileDir);
                } else {
                    Log.e(TAG, "文件夹创建失败: " + fileDir);
                }
            }
            FileWriter fw = new FileWriter(fileDir + "/" + fileName);
            fw.flush();
            fw.write(json);
            fw.close();
            Log.e(TAG, "文件写入成功：" + fileDir + "/" + fileName);
        } catch (Exception e) {
            Log.e(TAG, "文件写入失败：" + fileDir + "/" + fileName);
            e.printStackTrace();
        }
    }

    public static String getJSONFileString(String fileDir, String fileName) {
        Log.e(TAG, "获取文件的文本数据：" + fileDir + "/" + fileName);
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
            Log.e(TAG, "获取文件的文本数据成功：" + fileDir + "/" + fileName);
        } catch (Exception e) {
            Log.e(TAG, "获取文件的文本数据失败, 文件读取失败, 未点击过好友！" + fileDir + "/" + fileName);
        }
        return json.toString();
    }

    public static void copyFile(File fromFile, File toFile) {
        Log.e(TAG, "copyFile: 开始从：" + fromFile + "拷贝文件到：" + toFile);
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
                Log.e(TAG, "文件夹创建失败：" + toFile.getName());
            } else {
                Log.e(TAG, "文件夹创建成功：" + toFile.getAbsolutePath());
            }
        }
        if (toFile.exists()) {
            if (!toFile.delete()) {
                Log.e(TAG, "文件夹删除失败：" + toFile.getName());
            } else {
                Log.e(TAG, "文件夹删除成功：" + toFile.getName());
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
            Log.e(TAG, "开始从：" + fromFile.getAbsolutePath() + "拷贝文件到：" + toFile.getAbsolutePath() + ", 拷贝成功！");
        } catch (IOException e) {
            Log.e(TAG, "开始从：" + fromFile.getAbsolutePath() + "拷贝文件到：" + toFile.getAbsolutePath() + ", 拷贝失败！");
            e.printStackTrace();
        }
    }

    public static void deleteFileCache(File dirFile) {
        if (dirFile == null || !dirFile.exists() || !dirFile.isDirectory()) {
            Log.e(TAG, "要删除的文件夹不存在！");
            return;
        }
        for (File file : Objects.requireNonNull(dirFile.listFiles())) {
            if (file.isFile()) {
                if (file.delete()) {
                    Log.e(TAG, "删除文件成功：" + file.getAbsolutePath());
                } else {
                    Log.e(TAG, "要删除的文件不存在！");
                }
            } else if (file.isDirectory())
                Log.e(TAG, "要删除的文件是文件夹：" + file.getAbsolutePath());
            deleteFileCache(file);
        }
    }
}
