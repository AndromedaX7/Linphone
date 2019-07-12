package org.linphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;

import org.linphone.R;
import org.linphone.adapter.SoundSetAdapter;
import org.linphone.bean.Sound;
import org.linphone.utils.ShareHelper;
import org.linphone.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 声音设置
 * Created by 62420 on 2019/7/11 15:09.
 */
public class SoundSettingActivity extends Activity {

    private ImageView meBack;
    private RecyclerView recyclerView;
    private SoundSetAdapter adapter;
    private List<Sound> mData;
    private MediaPlayer mediaPlayer;
    private TextView save;
    private Sound mSound;
    private ShareHelper shareHelper;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sound_set);
        meBack = findViewById(R.id.meBack);
        save = findViewById(R.id.save);
        recyclerView = findViewById(R.id.recyclerView);

        shareHelper = ShareHelper.getInstance();
        setmData();

        adapter = new SoundSetAdapter(R.layout.item_sound, mData);


        recyclerView.setLayoutManager(new LinearLayoutManager(SoundSettingActivity.this));
        recyclerView.setAdapter(adapter);


        mediaPlayer = new MediaPlayer();

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter mAdapter, View view, int position) {
                adapter.clickItme(position);
                play(Uri.parse(mData.get(position).getLocation()));
                mSound = mData.get(position);
            }
        });

        meBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String save = new Gson().toJson(mSound);
                shareHelper.save("soundSettingNew",save).commit();
                Intent intent = new Intent();
                intent.setAction("org.linphone.SOUND_SETTING");
                sendBroadcast(intent);
                Toast.makeText(SoundSettingActivity.this,"更改成功",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void setmData(){
        mData = new ArrayList<>();

        Sound sound = new Sound();
        sound.setName("铃声1");
        sound.setId("channel-im1");
        sound.setLocation("msg1");
        mData.add(sound);

        Sound sound2 = new Sound();
        sound2.setName("铃声2");
        sound2.setId("channel-im2");
        sound2.setLocation("msg");
        mData.add(sound2);

        Sound sound3 = new Sound();
        sound3.setName("铃声3");
        sound3.setId("channel-im3");
        sound3.setLocation("msg3");
        mData.add(sound3);
    }

    public void play(Uri uri) {

        try {
            //        重置音频文件，防止多次点击会报错
            mediaPlayer.reset();
//        调用方法传进播放地址
            mediaPlayer.setDataSource(this,uri);
//            异步准备资源，防止卡顿
            mediaPlayer.prepareAsync();
//            调用音频的监听方法，音频准备完毕后响应该方法进行音乐播放
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
