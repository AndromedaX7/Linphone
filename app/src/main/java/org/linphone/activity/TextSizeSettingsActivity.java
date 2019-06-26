package org.linphone.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.adapter.TextChangeAdapter;
import org.linphone.app.App;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TextSizeSettingsActivity extends AppCompatActivity {

    TextChangeAdapter adapter;
    @BindView(R.id.textList)
    ListView textList;
    @BindView(R.id.seek)
    SeekBar seek;
    @BindView(R.id.textSize)
    TextView textSize;
    @BindView(R.id.meBack)
    ImageView meBack;
    private int[] textSizes = {
            16, 18, 20, 22, 24
//            12, 14, 16, 18, 20
    };

    @OnClick({R.id.meBack})
    void onBack(){
        finish();
    }

    private String[] name = {
            "最小号", "小号", "标准", "大号", "最大号"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_size_settings);
        ButterKnife.bind(this);
        adapter = new TextChangeAdapter();
        NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(App.app().getLoginData().getUsername());
        if (userInfo.getExtensionMap().containsKey("chat_text_size")) {
            int chat_text_size = (int) userInfo.getExtensionMap().get("chat_text_size");
            adapter.setTextSize(chat_text_size);
            for (int i = 0; i < textSizes.length; i++) {
                if (textSizes[i] == chat_text_size) {
                    seek.setProgress(i);
                    textSize.setText(name[i]);
                }
            }
        } else {
            adapter.setTextSize(18);
            seek.setProgress(1);
            textSize.setText(name[1]);
        }

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("拖动下方的滑块，可以设置聊天字体大小");
        arrayList.add("设置聊天字体大小");
        adapter.setData(arrayList);
        textList.setAdapter(adapter);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSize.setText(name[progress]);
                adapter.setTextSize(textSizes[progress]);
                HashMap<UserInfoFieldEnum, Object> maps = new HashMap<>();
                HashMap<String, Object> map = new HashMap<>();
                map.put("chat_text_size", textSizes[progress]);
                maps.put(UserInfoFieldEnum.EXTEND, map);
                NIMClient.getService(UserService.class).updateUserInfo(maps).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {

                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


}
