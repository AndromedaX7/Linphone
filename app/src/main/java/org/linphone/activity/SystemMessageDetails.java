package org.linphone.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;

import org.linphone.R;
import org.linphone.adapter.SystemMessageAdapter;
import org.linphone.ob.ObserverManager;
import org.linphone.ob.ReceiveSystemMessageTask;
import org.linphone.ob.TaskListener;
import org.linphone.preview.PlusItemSlideCallback;
import org.linphone.preview.WItemTouchHelperPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.netease.nimlib.sdk.friend.model.AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST;

public class SystemMessageDetails extends AppCompatActivity implements ReceiveSystemMessageTask {


    private SystemMessageAdapter adapter = new SystemMessageAdapter();
    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.chatView)
    RecyclerView chatView;
    @BindView(R.id.loadHistory)
    SwipeRefreshLayout loadHistory;
    @BindView(R.id.clearAll)
    TextView clearAll;

    private AlertDialog dialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unregister(TaskListener.RECEIVE_SYSTEM_MESSAGE,this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_message_details);
        ButterKnife.bind(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ObserverManager.getInstance().register(TaskListener.RECEIVE_SYSTEM_MESSAGE,this);
        builder.setTitle("清除记录");
        builder.setMessage("是否删除全部验证消息?");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NIMClient.getService(SystemMessageService.class).clearSystemMessages();
                clearAll.postDelayed(
                        () -> {
                            adapter.setData(getSystemMessage());
                            Toast.makeText(SystemMessageDetails.this, "已删除", Toast.LENGTH_SHORT).show();
                        }, 1000
                );

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog = builder.create();

        chatView.setLayoutManager(new LinearLayoutManager(this));
        PlusItemSlideCallback callback = new PlusItemSlideCallback();
        callback.setType(WItemTouchHelperPlus.SLIDE_ITEM_TYPE_ITEMVIEW);
        WItemTouchHelperPlus itemTouchHelperPlus = new WItemTouchHelperPlus(callback);
        itemTouchHelperPlus.attachToRecyclerView(chatView);
        chatView.setAdapter(adapter);
        loadHistory.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                adapter.removeAll();
                adapter.setData(getSystemMessage());
                loadHistory.setRefreshing(false);
            }
        });
        adapter.setData(getSystemMessage());
        adapter.setNotifyChangedCallback(new SystemMessageAdapter.NotifyChanged() {
            @Override
            public void notifyDataSetChanged() {
                loadHistory.setRefreshing(true);
//                adapter.removeAll();
                adapter.setData(getSystemMessage());
                loadHistory.setRefreshing(false);
            }
        });

        Log.e("onCreate: ", "" + getSystemMessage().size());
    }


    private List<SystemMessage> getSystemMessage() {
        ArrayList<SystemMessageType> types = new ArrayList<>();
        types.add(SystemMessageType.AddFriend);
        List<SystemMessage> systemMessages = NIMClient.getService(SystemMessageService.class)
                .querySystemMessageByTypeBlock(types, 0, 2147483647);
        ArrayList<SystemMessage> hasProcessed = new ArrayList<>();
        HashMap<String, SystemMessage> map = new HashMap<>();
        ArrayList<SystemMessage> nowData = new ArrayList<>();
        for (SystemMessage s : systemMessages) {
            if (s.getStatus() == SystemMessageStatus.init && s.getAttachObject() != null && ((AddFriendNotify) s.getAttachObject()).getEvent() == RECV_ADD_FRIEND_VERIFY_REQUEST) {
//未处理
                if (map.get(s.getFromAccount()) == null)
                    map.put(s.getFromAccount(), s);
                else {
                    NIMClient.getService(SystemMessageService.class).deleteSystemMessage(s.getMessageId());
                }
            } else {
                hasProcessed.add(s);
            }
        }

        for (String s : map.keySet()) {
            nowData.add(map.get(s));
        }

        nowData.addAll(hasProcessed);
        return nowData;
    }

    @OnClick(R.id.clearAll)
    void clearAll() {
        dialog.show();
    }

    @OnClick(R.id.meBack)
    void meBack() {
        finish();
    }

    @Override
    public void onReceive(SystemMessage msg) {
        adapter.setData(getSystemMessage());
    }

    @Override
    public void registerComplete() {

    }

    @Override
    public void unregisterComplete() {

    }
}
