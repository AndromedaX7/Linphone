package org.linphone.activity;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import org.linphone.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoPlayerActivity extends AppCompatActivity {

    @BindView(R.id.mVideo)
    VideoView mVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);
        String path = getIntent().getStringExtra("path");
        mVideo.setMediaController(new MediaController(this));
        mVideo.setVideoPath(path);
        mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VideoPlayerActivity.this, "视频加载出错", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }
        });
        mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(VideoPlayerActivity.this, "已播放完成", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
