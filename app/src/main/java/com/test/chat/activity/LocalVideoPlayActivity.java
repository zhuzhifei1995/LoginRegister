package com.test.chat.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.test.chat.R;
import com.test.chat.view.ConditionVideoView;

import java.io.File;

public class LocalVideoPlayActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_video_play);
        context = this;
        initView();
    }

    private void initView() {
        String filePath = getIntent().getStringExtra("file_path");
        ConditionVideoView local_ConditionVideoView = findViewById(R.id.local_ConditionVideoView);
        File file = new File(filePath);
        if (file.exists()) {
            local_ConditionVideoView.setVideoPath(file.getAbsolutePath());
        } else {
            Toast.makeText(context, "要播放的视频文件不存在", Toast.LENGTH_SHORT).show();
            finish();
        }
        MediaController mediaController = new MediaController(this);
        local_ConditionVideoView.setMediaController(mediaController);
        local_ConditionVideoView.setFocusable(true);
        local_ConditionVideoView.start();
        local_ConditionVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Toast.makeText(context, "该视频播放完毕！", Toast.LENGTH_SHORT).show();
                mediaPlayer.release();
            }
        });
    }
}