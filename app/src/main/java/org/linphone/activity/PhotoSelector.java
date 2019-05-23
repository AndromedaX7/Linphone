package org.linphone.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.linphone.R;
import org.linphone.adapter.ImageSelectAdapter;
import org.linphone.bean.ImageSelectBean;
import org.linphone.ob.ObserverManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.linphone.activity.ChatItemActivity.checkBlackList;
import static org.linphone.activity.ChatItemActivity.imMessageCache;

public class PhotoSelector extends AppCompatActivity implements ImageSelectAdapter.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selecter);
        ButterKnife.bind(this);
        getIntentData();
        mPhotos.setAdapter(adapter);
        adapter.setData(getSystemPhotoList(this));
        adapter.setOnItemClick(this);
    }

    @BindView(R.id.mPhotos)
    RecyclerView mPhotos;
    @BindView(R.id.mOpenPhotos)
    TextView mOpenPhotos;
    @BindView(R.id.mSend)
    TextView mSend;
    private ImageSelectAdapter adapter = new ImageSelectAdapter();
    private String targetId;
    private SessionTypeEnum typeEnum;

    @OnClick(R.id.meBack)
    void onBack() {
        finish();
    }

    public void getIntentData( ) {
        this.targetId = getIntent().getStringExtra("targetId");
        this.typeEnum = getIntent().getBooleanExtra("isP2P",true)?SessionTypeEnum.P2P:SessionTypeEnum.Team;
    }

    private ArrayList<ImageSelectBean> getSystemPhotoList(Context context) {
        ArrayList<ImageSelectBean> result = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) return null; // 没有图片
        while (cursor.moveToNext()) {
            int index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(index); // 文件地址
            File file = new File(path);
            if (file.exists()) {
                result.add(new ImageSelectBean(Uri.fromFile(file), file.getAbsolutePath()));
                Log.i(TAG, path);
            }
        }

        return result;
    }

    private String TAG = "photoList";

    @OnClick(R.id.mOpenPhotos)
    public void onMOpenPhotosClicked() {
    }

    @OnClick(R.id.mSend)
    public void onMSendClicked() {
        checkBlackList(targetId,typeEnum==SessionTypeEnum.P2P);
        ArrayList<ImageSelectBean> data = adapter.getData();
        String msg = "[图片]";
        for (int i = 0; i < data.size(); i++) {
            IMMessage textMessage = MessageBuilder.createImageMessage(targetId, typeEnum,new File(  data.get(i).getPath()));
            textMessage.setContent(msg);
            HashMap<String,Object> fp =new HashMap<>();
            fp.put("path",data.get(i).getPath());
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
    }

    @Override
    public void onItemClick(int pos, ImageSelectBean item) {
        startActivity(new Intent(this,ImageDetailsActivity.class).putExtra("imgUrl",item.getPath()));
    }
}
