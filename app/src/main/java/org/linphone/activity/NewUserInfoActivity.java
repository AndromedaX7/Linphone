package org.linphone.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.app.App;
import org.linphone.utils.DialogUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewUserInfoActivity extends AppCompatActivity {
    private NimUserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_info);
        ButterKnife.bind(this);
        initViews();
    }

    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.icon)
    ImageView icon;
    @BindView(R.id.userName)
    TextView userName;
    @BindView(R.id.account)
    TextView account;
    @BindView(R.id.addFriend)
    TextView addFriend;


    public void initViews() {
        getIntentData();
        if (userInfo != null) {
            initUserInfo();
        }
    }

    @OnClick(R.id.meBack)
    public void onBack() {
        finish();
    }

    public void getIntentData() {
        userInfo = (NimUserInfo) getIntent().getSerializableExtra("data");
        initUserInfo();
    }

    private void initUserInfo() {
        userName.setText(userInfo.getName());
        account.setText(userInfo.getAccount());
        Glide.with(icon).load(userInfo.getAvatar()).placeholder(R.mipmap.ic_launcher2).error(R.mipmap.ic_launcher2).into(icon);
    }

    @OnClick(R.id.addFriend)
    void addFriend() {
        if (userInfo.getAccount().equals(App.app().getLoginData().getUsername())) {
            Toast toast = Toast.makeText(this, "不可以添加自己为好友", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        } else {
            boolean myFriend = NIMClient.getService(FriendService.class).isMyFriend(userInfo.getAccount());
//        myFriend = false;
            if (myFriend) {
                Toast.makeText(this, userInfo.getName() + "已是你的好友", Toast.LENGTH_SHORT).show();
            } else {
                if (dialog == null) dialog = DialogUtils.progressDialog(this, "正在发送,请稍后...");
                dialog.show();
                AddFriendData addFriendData = new AddFriendData(userInfo.getAccount(), VerifyType.VERIFY_REQUEST);
                NIMClient.getService(FriendService.class).addFriend(addFriendData).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        Log.e(TAG, "onSuccess: ");
                        if (dialog != null)
                            dialog.dismiss();
                        Toast.makeText(NewUserInfoActivity.this, "已发送好友请求", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code) {
                        Log.e(TAG, "onFailed: " + code);
                        if (dialog != null)
                            dialog.dismiss();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Log.e(TAG, "onException: ,", exception);
                    }
                });
            }
        }
    }

    private String TAG = getClass().getName();
    private ProgressDialog dialog;
}

