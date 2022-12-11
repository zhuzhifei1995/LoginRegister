package com.test.chat.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.test.chat.R;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCUserAction;
import fm.jiecao.jcvideoplayer_lib.JCUtils;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;

@SuppressLint("NonConstantResourceId")
@RequiresApi(api = Build.VERSION_CODES.M)
public class NetVideoPlayActivity extends AppCompatActivity {

    private static String fileDownloadUrl;
    private static String fileName;
    private JCVideoPlayerStandard playVideo_JCVideoPlayerStandard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_video_play);
        fileDownloadUrl = getIntent().getStringExtra("fileDownloadUrl");
        fileName = getIntent().getStringExtra("fileName");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            PlayVideo(fileDownloadUrl, fileName);
        }
    }

    private void PlayVideo(String fileDownloadUrl, String fileName) {
        ImageView title_left_ImageView = findViewById(R.id.title_left_ImageView);
        title_left_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JCVideoPlayer.releaseAllVideos();
                finish();
            }
        });
        TextView top_title_TextView = findViewById(R.id.top_title_TextView);
        top_title_TextView.setText(fileName.substring(0, fileName.lastIndexOf(".")));
        JCUtils.saveProgress(getApplicationContext(), fileDownloadUrl, 0);
        playVideo_JCVideoPlayerStandard = findViewById(R.id.playVideo_JCVideoPlayerStandard);
        playVideo_JCVideoPlayerStandard.setUp(fileDownloadUrl, JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");
        playVideo_JCVideoPlayerStandard.fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int videoWidth = JCMediaManager.instance().mediaPlayer.getVideoWidth();
                int videoHeight = JCMediaManager.instance().mediaPlayer.getVideoHeight();
                if (videoWidth >= videoHeight) {
                    JCUtils.getAppCompActivity(playVideo_JCVideoPlayerStandard.getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    JCUtils.getAppCompActivity(playVideo_JCVideoPlayerStandard.getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                if (playVideo_JCVideoPlayerStandard.currentState == JCVideoPlayer.CURRENT_STATE_AUTO_COMPLETE) {
                    return;
                }
                if (playVideo_JCVideoPlayerStandard.currentScreen == JCVideoPlayer.SCREEN_WINDOW_FULLSCREEN) {
                    JCVideoPlayer.backPress();
                } else {
                    playVideo_JCVideoPlayerStandard.onEvent(JCUserAction.ON_ENTER_FULLSCREEN);
                    playVideo_JCVideoPlayerStandard.startWindowFullscreen();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        JCVideoPlayer.releaseAllVideos();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        JCVideoPlayer.releaseAllVideos();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PlayVideo(fileDownloadUrl, fileName);
            } else {
                Toast.makeText(this, "播放异常！", Toast.LENGTH_SHORT).show();
                JCVideoPlayer.releaseAllVideos();
                finish();
            }
        }
    }


}