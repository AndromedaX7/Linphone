package org.linphone.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.adapter.UserBlackListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlackListDetails extends AppCompatActivity {


    @BindView(R.id.mList)
    ListView mList;
    private UserBlackListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list_details);
        ButterKnife.bind(this);
        adapter = new UserBlackListAdapter();
        mList.setAdapter(adapter);
        adapter.setCallback(account -> {
            NIMClient.getService(FriendService.class)
                    .removeFromBlackList(account).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    sendBlackListNotifier(account,false);
                    Toast.makeText(BlackListDetails.this, NIMClient.getService(UserService.class).getUserInfo(account).getName() + "已移出黑名单", Toast.LENGTH_SHORT).show();
                    if (adapter.getData().size() == 1) {
                        adapter.clear();
                    } else
                        getBlackList();
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        });
        getBlackList();
    }

    @OnClick(R.id.meBack)
    void onBack() {
        finish();
    }
    private void getBlackList() {
        List<String> blackList = NIMClient.getService(FriendService.class)
                .getBlackList();
        NIMClient.getService(UserService.class).fetchUserInfo(blackList)
                .setCallback(new RequestCallback<List<NimUserInfo>>() {
                    @Override
                    public void onSuccess(List<NimUserInfo> param) {
                        adapter.setData(new ArrayList<>(param));
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
    }

    private void sendBlackListNotifier(String sessionId, boolean added) {
        IMMessage textMessage = MessageBuilder.createTipMessage(sessionId, SessionTypeEnum.P2P);
        HashMap<String, Object> map = new HashMap<>();
        map.put("blackListNotifier", added);
        textMessage.setRemoteExtension(map);
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true);
    }
}
