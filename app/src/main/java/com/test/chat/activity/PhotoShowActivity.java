package com.test.chat.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.test.chat.R;
import com.test.chat.util.ImageUtil;
import com.test.chat.util.ActivityUtil;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PhotoShowActivity extends Activity {

    private static final String TAG = ActivityUtil.TAG;
    private static boolean IS_PLAYING = true;
    private static boolean IS_SEEK_BAR_CHANGE = false;
    private MediaPlayer mediaPlayer;
    private ImageView play_ImageView;
    private TextView music_play_time_TextView;
    private TextView music_time_TextView;
    private SeekBar music_SeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_show);
        initView();
    }

    private void initView() {
        Log.e(TAG, "initView: " + IS_SEEK_BAR_CHANGE);
        ImageView photo_show_ImageView = findViewById(R.id.photo_show_ImageView);
        LinearLayout voice_LinearLayout = findViewById(R.id.voice_LinearLayout);
        LinearLayout photo_show_LinearLayout = findViewById(R.id.photo_show_LinearLayout);
        photo_show_LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Intent intent = getIntent();
        int flag = intent.getIntExtra("flag", 0);

        if (flag == 0) {
            photo_show_ImageView.setVisibility(View.VISIBLE);
            voice_LinearLayout.setVisibility(View.GONE);
            String photoName = intent.getStringExtra("photoName");
            photo_show_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(
                    Environment.getExternalStorageDirectory().getPath() + "/tmp/friend", photoName));
        } else if (flag == 1) {
            photo_show_ImageView.setVisibility(View.VISIBLE);
            voice_LinearLayout.setVisibility(View.GONE);
            photo_show_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(
                    Environment.getExternalStorageDirectory().getPath() + "/tmp/user", "photo.png.cache"));
        } else if (flag == 2) {
            photo_show_ImageView.setVisibility(View.VISIBLE);
            voice_LinearLayout.setVisibility(View.GONE);
            String photoName = intent.getStringExtra("photoName");
            photo_show_ImageView.setImageBitmap(ImageUtil.getBitmapFromFile(
                    Environment.getExternalStorageDirectory().getPath() + "/tmp/message_image", photoName));
        } else if (flag == 3) {
            photo_show_ImageView.setVisibility(View.GONE);
            String voiceName = intent.getStringExtra("voiceName");
            voice_LinearLayout.setVisibility(View.VISIBLE);
            if (ContextCompat.checkSelfPermission(PhotoShowActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PhotoShowActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            } else {
                initVoiceView(voiceName);
                initMediaPlayer(voiceName);
            }

        } else {
            finish();
            Toast.makeText(PhotoShowActivity.this, "传入的参数错误！", Toast.LENGTH_SHORT).show();
        }
    }

    private void initVoiceView(final String voiceName) {
        mediaPlayer = new MediaPlayer();
        play_ImageView = findViewById(R.id.play_ImageView);
        music_play_time_TextView = findViewById(R.id.music_play_time_TextView);
        music_time_TextView = findViewById(R.id.music_time_TextView);
        music_SeekBar = findViewById(R.id.music_SeekBar);
        play_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IS_PLAYING) {
                    Log.e(TAG, "initView: " + mediaPlayer.getDuration());
                    play_ImageView.setImageResource(R.drawable.voice_play);
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        int duration = mediaPlayer.getDuration();
                        music_SeekBar.setMax(duration);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (!IS_SEEK_BAR_CHANGE) {
                                    music_SeekBar.setProgress(mediaPlayer.getCurrentPosition());
                                }
                            }
                        }, 0, 50);
                    }
                    IS_PLAYING = false;
                } else {
                    play_ImageView.setImageResource(R.drawable.voice_stop);
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        initMediaPlayer(voiceName);
                    }
                    IS_PLAYING = true;
                }
            }
        });
        music_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                music_play_time_TextView.setText(calculateMusicTime(mediaPlayer.getCurrentPosition() / 1000));
                music_time_TextView.setText(calculateMusicTime(mediaPlayer.getDuration() / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                IS_SEEK_BAR_CHANGE = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                IS_SEEK_BAR_CHANGE = false;
                mediaPlayer.seekTo(seekBar.getProgress());
                music_play_time_TextView.setText(calculateMusicTime(mediaPlayer.getCurrentPosition() / 1000));
            }
        });
    }

    public String calculateMusicTime(int time) {
        int minute;
        int second;
        if (time >= 60) {
            minute = time / 60;
            second = time % 60;
            if (minute < 10) {
                if (second < 10) {
                    return "0" + minute + ":" + "0" + second;
                } else {
                    return "0" + minute + ":" + second;
                }
            } else {
                if (second < 10) {
                    return minute + ":" + "0" + second;
                } else {
                    return minute + ":" + second;
                }
            }
        } else {
            second = time;
            if (second >= 0 && second < 10) {
                return "00:" + "0" + second;
            } else {
                return "00:" + second;
            }
        }
    }

    private void initMediaPlayer(String voiceName) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/tmp/voice", voiceName);
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                play_ImageView.setImageResource(R.drawable.voice_stop);
                IS_PLAYING = true;
            }
        });
        music_play_time_TextView.setText(calculateMusicTime(mediaPlayer.getCurrentPosition() / 1000));
        music_time_TextView.setText(calculateMusicTime(mediaPlayer.getDuration() / 1000));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMediaPlayer("");
            } else {
                Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        IS_SEEK_BAR_CHANGE = true;
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


}