package org.linphone.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.linphone.R;
import org.linphone.adapter.IconSelectorAdapter;
import org.linphone.bean.ImageSelectBean;
import org.linphone.ob.ObserverManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.linphone.activity.ChatItemActivity.checkBlackList;
import static org.linphone.activity.ChatItemActivity.imMessageCache;

public class IconsSelector extends AppCompatActivity implements IconSelectorAdapter.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons_selector);
        ButterKnife.bind(this);
        getIntentData();
        readIcon();
        mPhotos.setAdapter(adapter);
        adapter.setOnItemClick(this);
    }
    @OnClick(R.id.meBack)
    void onBack() {
        finish();
    }

    private void readIcon() {
        File filePath = getFilesDir();
        File root = new File(filePath, "imMp");
        root.mkdirs();
        File ltDir = new File(root, "lt");
        ltDir.mkdirs();
        File ltLock = new File(ltDir, "lt.lock");
        if (ltLock.exists()) {
            readIconSdCard(ltDir);
        } else {
            writeLt2Sd(ltDir);
            readIconSdCard(ltDir);
        }
    }

    private void writeLt2Sd(File ltDir) {
        for (int i = 1; i <= 20; i++) {
            try {
                String text = i < 10 ? "0" + i : "" + i;
                File file = new File(ltDir, "lt0" + text + ".png");
                InputStream open = getAssets().open("lt/lt0" + text + ".png");
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                int len = 0;
                byte[] buff = new byte[1024];
                while ((len = open.read(buff)) > 0) {
                    out.write(buff, 0, len);
                }
                out.flush();
                out.close();
                open.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File ltLock = new File(ltDir, "lt.lock");
        try {
            ltLock.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readIconSdCard(File ltDir) {
        ArrayList<ImageSelectBean> icons =new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String text = i < 10 ? "0" + i : "" + i;
            File file =new File(ltDir,"lt0"+text+".png");
            icons.add(new ImageSelectBean(Uri.fromFile(file) ,file.getAbsolutePath()));
        }
        adapter.setData(icons);
    }

    @BindView(R.id.mPhotos)
    RecyclerView mPhotos;
    private IconSelectorAdapter adapter = new IconSelectorAdapter();
    private String targetId;
    private SessionTypeEnum typeEnum;


    public void getIntentData() {
        this.targetId = getIntent().getStringExtra("targetId");
        this.typeEnum = getIntent().getBooleanExtra("isP2P", true) ? SessionTypeEnum.P2P : SessionTypeEnum.Team;
    }
    private String TAG = "photoList";
    public void onMSendClicked(ImageSelectBean item) {
        checkBlackList(targetId,typeEnum==SessionTypeEnum.P2P);
        String msg = "[表情]";
        IMMessage textMessage = MessageBuilder.createImageMessage(targetId, typeEnum, new File(item.getPath()));
        textMessage.setContent(msg);
        HashMap<String, Object> fp = new HashMap<>();
        fp.put("path", item.getPath());
        textMessage.setRemoteExtension(fp);
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true);


        ArrayList<IMMessage> imMessages = imMessageCache.get(targetId);
        if (imMessages == null) {
            imMessages = new ArrayList<>();
            imMessageCache.put(targetId, imMessages);
        }
        imMessages.add(textMessage);
        Log.e("onSend: ", targetId + "<>" + typeEnum.name());
        ObserverManager.getInstance().notifyReceiveSelfText(targetId, msg);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onItemClick(int pos, ImageSelectBean item) {
        onMSendClicked(item);
    }
}
