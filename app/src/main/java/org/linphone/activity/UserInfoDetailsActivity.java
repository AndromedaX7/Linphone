package org.linphone.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInfoDetailsActivity extends AppCompatActivity {

    @BindView(R.id.icon)
    ImageView icon;
    @BindView(R.id.account)
    TextView account;
    @BindView(R.id.age)
    TextView nickName;
    @BindView(R.id.remarks)
    EditText remarks;
    @BindView(R.id.imageView5)
    TextView remarks2;
    @BindView(R.id.resetRemarks)
    TextView from2;
    @BindView(R.id.blacklist)
    CardView blacklist;
    @BindView(R.id.delete)
    CardView delete;
    @BindView(R.id.flags)
    TextView flags;
    @BindView(R.id.flagsBlackList)
    TextView flagsBlackList;


    private String userAccount;
    private boolean me;
    private boolean friend;
    private boolean isInBlacklist;

    private NimUserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_details);
        ButterKnife.bind(this);
        getIntentData();
        userInfo = NIMClient.getService(UserService.class)
                .getUserInfo(userAccount);
        friend = NIMClient.getService(FriendService.class).isMyFriend(userAccount);
        isInBlacklist = NIMClient.getService(FriendService.class).isInBlackList(userAccount);
        setUserInfo();

    }

    @OnClick(R.id.meBack)
    void onBack() {
        finish();
    }

    private void setUserInfo() {
        Glide.with(this).load(userInfo.getAvatar()).error(R.mipmap.ic_launcher2).into(icon);
        nickName.setText(userInfo.getName());
        account.setText(userAccount);
        if (me) {
            delete.setVisibility(View.GONE);
            blacklist.setVisibility(View.GONE);
            remarks2.setVisibility(View.GONE);
            remarks.setVisibility(View.GONE);
        } else {

//            NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(userAccount);
            boolean myFriend = NIMClient.getService(FriendService.class).isMyFriend(userAccount);
            if (myFriend) {
                Friend friendByAccount = NIMClient.getService(FriendService.class).getFriendByAccount(userAccount);
                if (!TextUtils.isEmpty(friendByAccount.getAlias())) {
                    remarks2.setVisibility(View.VISIBLE);
                    remarks.setVisibility(View.VISIBLE);
                } else {
                    remarks2.setVisibility(View.VISIBLE);
                    remarks.setVisibility(View.VISIBLE);
                }
                remarks.setText(friendByAccount.getAlias());
            } else {
//                delete.setVisibility(View.GONE);
                from2.setVisibility(View.GONE);
                remarks2.setVisibility(View.GONE);
                remarks.setVisibility(View.GONE);
            }

        }

//        birthday.setText(userInfo.getBirthday());
//        from.setText(genderValue(userInfo.getGenderEnum()));
//        from2.setText("未设置");

        setFlag();
    }

    private void setFlag() {
        delete.setCardBackgroundColor(friend ? getResources().getColor(android.R.color.holo_red_light) : getResources().getColor(R.color.colorAccent));
        flags.setText(friend ? "删除好友" : "添加好友");
        blacklist.setCardBackgroundColor(!isInBlacklist ? getResources().getColor(android.R.color.holo_red_light) : getResources().getColor(R.color.colorAccent));
        flagsBlackList.setText(!isInBlacklist ? "加入黑名单" : "移出黑名单");
    }

    @OnClick(R.id.resetRemarks)
    void resetRemarks() {
        if (NIMClient.getService(FriendService.class).isMyFriend(userAccount)) {

            if (remarks.getText().toString().equals(NIMClient.getService(FriendService.class).getFriendByAccount(userAccount).getAlias())) {
                return;
            }
            HashMap<FriendFieldEnum, Object> map = new HashMap<>();
            map.put(FriendFieldEnum.ALIAS, remarks.getText().toString());
            NIMClient.getService(FriendService.class).updateFriendFields(userAccount, map).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    Toast.makeText(UserInfoDetailsActivity.this, "备注信息已修改", Toast.LENGTH_SHORT).show();
                    setUserInfo();
                }

                @Override
                public void onFailed(int code) {
                    Log.e("onFailed: ", "code:" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    private String genderValue(GenderEnum value) {
        switch (value) {
            case MALE:
                return "男";
            case FEMALE:
                return "女";
            case UNKNOWN:
            default:
                return "未设置";
        }
    }


    private void getIntentData() {
        userAccount = getIntent().getStringExtra("id");
        me = getIntent().getBooleanExtra("me", false);
    }


    @OnClick(R.id.delete)
    void deleteFriend() {
        if (friend)
            NIMClient.getService(FriendService.class).deleteFriend(userAccount)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            Toast.makeText(UserInfoDetailsActivity.this, "已解除好友关系", Toast.LENGTH_SHORT).show();
                            friend = NIMClient.getService(FriendService.class).isMyFriend(userAccount);
                            setFlag();
                            setResult(100);

                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
        else {
            AddFriendData data = new AddFriendData(userAccount, VerifyType.VERIFY_REQUEST);
            NIMClient.getService(FriendService.class).addFriend(data)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            friend = NIMClient.getService(FriendService.class).isMyFriend(userAccount);
                            Toast.makeText(UserInfoDetailsActivity.this, "已发送添加好友请求", Toast.LENGTH_SHORT).show();
                            setFlag();
                            setResult(100);

                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
        }
    }

    @OnClick(R.id.blacklist)
    void addToBlackList() {
        if (isInBlacklist) {
            NIMClient.getService(FriendService.class).removeFromBlackList(userAccount).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    Toast.makeText(UserInfoDetailsActivity.this, "已移出黑名单", Toast.LENGTH_SHORT).show();
                    isInBlacklist = NIMClient.getService(FriendService.class).isInBlackList(userAccount);
                    sendBlackListNotifier(userAccount, false);
                    setResult(101);
                    setFlag();
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        } else {
            sendBlackListNotifier(userAccount, true);
            NIMClient.getService(FriendService.class).addToBlackList(userAccount)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            Toast.makeText(UserInfoDetailsActivity.this, "已添加至黑名单", Toast.LENGTH_SHORT).show();
                            isInBlacklist = NIMClient.getService(FriendService.class).isInBlackList(userAccount);
                            setResult(101);
                            setFlag();
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
        }
    }


    private void sendBlackListNotifier(String sessionId, boolean added) {
        IMMessage textMessage = MessageBuilder.createTipMessage(sessionId, SessionTypeEnum.P2P);
        HashMap<String, Object> map = new HashMap<>();
        map.put("blackListNotifier", added);
        textMessage.setRemoteExtension(map);
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                Log.e("inBlackList", "onSuccess: ");
            }

            @Override
            public void onFailed(int code) {
                Log.e("inBlackList", "onFailed: " + code);
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

}
