package org.linphone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.activity.BlackListDetails;
import org.linphone.activity.ChatItemActivity;
import org.linphone.activity.SystemMessageDetails;
import org.linphone.adapter.RecentContactsAdapter;
import org.linphone.app.App;
import org.linphone.bean.ChatMessageWrapper;
import org.linphone.ob.ObserverManager;
import org.linphone.ob.ReceiveFriendMessageTask;
import org.linphone.ob.ReceiveSystemMessageTask;
import org.linphone.ob.ReceiveTextMessage;
import org.linphone.ob.TaskListener;
import org.linphone.preview.PlusItemSlideCallback;
import org.linphone.preview.WItemTouchHelperPlus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Sylvain Berfini
 */
public class HistoryListFragment extends Fragment implements ReceiveSystemMessageTask, ReceiveFriendMessageTask, ReceiveTextMessage {//implements OnClickListener, OnItemClickListener, ContactsUpdatedListener {


    @BindView(R.id.mFriendName)
    TextView mFriendName;
    @BindView(R.id.mChatSet)
    RecyclerView mChatSet;
    @BindView(R.id.mRefresh)
    SwipeRefreshLayout mRefresh;

    private RecentContactsAdapter chatUserAdapter = new RecentContactsAdapter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObserverManager.getInstance().register(TaskListener.RECEIVE_FRIEND_MESSAGE, this);
        ObserverManager.getInstance().register(TaskListener.RECEIVE_SYSTEM_MESSAGE, this);
        ObserverManager.getInstance().register(TaskListener.RECEIVE_MY_TEXT_MESSAGE, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unregister(TaskListener.RECEIVE_FRIEND_MESSAGE, this);
        ObserverManager.getInstance().unregister(TaskListener.RECEIVE_SYSTEM_MESSAGE, this);
        ObserverManager.getInstance().unregister(TaskListener.RECEIVE_MY_TEXT_MESSAGE, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void getRecentContacts() {
        ImClientProxy.getRecentContacts(new RequestCallback<List<RecentContact>>() {
            @Override
            public void onSuccess(List<RecentContact> params) {
                ArrayList<ChatMessageWrapper> wrappers = new ArrayList<>();
                chatUserAdapter.clear();
                if (systemCounter > 0) {
                    chatUserAdapter.addItem(new ChatMessageWrapper("验证消息"));
                }
                if (systemCounter2 > 0) {
                    chatUserAdapter.addItem(new ChatMessageWrapper("黑名单", 5));
                }

                Iterator<RecentContact> iterator = params.iterator();
                while (iterator.hasNext()) {
                    RecentContact it = iterator.next();
                    if (it.getSessionType() == SessionTypeEnum.P2P) {
                        if (!NIMSDK.getFriendService().isMyFriend(it.getContactId()) || NIMSDK.getFriendService().isInBlackList(it.getContactId())) {
                            iterator.remove();
                        } else {
                            wrappers.add(new ChatMessageWrapper(it));
                        }
                    }else {
                        wrappers.add(new ChatMessageWrapper(it));
                    }
                }
                chatUserAdapter.addData(wrappers);
            }

            @Override
            public void onFailed(int code) {
                Log.e(TAG, "onFailed: " + code);
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onStart: ");
        getRecentContacts();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated: " + this);
        getRecentContacts();
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRecentContacts();
                mRefresh.setRefreshing(false);
            }
        });
        mChatSet.setLayoutManager(new LinearLayoutManager(getContext()));
        mChatSet.setAdapter(chatUserAdapter);
        chatUserAdapter.setItemClickListener(this::onChatUserItemClick);
        PlusItemSlideCallback plusItemSlideCallback = new PlusItemSlideCallback();
        plusItemSlideCallback.setType(WItemTouchHelperPlus.SLIDE_ITEM_TYPE_ITEMVIEW);
        WItemTouchHelperPlus itemTouchHelperPlus = new WItemTouchHelperPlus(plusItemSlideCallback);
        itemTouchHelperPlus.attachToRecyclerView(mChatSet);
    }

    private String TAG = getClass().getName();

    void onChatUserItemClick(int pos) {
        current_position = pos;
        ChatMessageWrapper item = chatUserAdapter.getItem(pos);
        int type = item.getType();
        switch (type) {
            case 1:
                startActivity(new Intent(this.getContext(), ChatItemActivity.class).putExtra("data", item.getUserInfo()));
                break;
            case 2:
                receiveAddFriend(item.getAddFriendData());
                break;
            case 3:
                startActivity(new Intent(this.getContext(), ChatItemActivity.class).putExtra("team", item.getTeam()).putExtra("isP2P", false));
                break;
            case 4:
                startActivity(new Intent(this.getContext(), SystemMessageDetails.class));
                App.newRecord = false;
                chatUserAdapter.notifyItemChanged(0);
                break;
            case 5:
                startActivity(new Intent(this.getContext(), BlackListDetails.class));
                break;
        }

    }

    private int current_position = -1;

    private void receiveAddFriend(AddFriendNotify attachData) {
        if (addFriendDialog == null)
            addFriendDialog = new AlertDialog.Builder(getContext())
                    .setTitle("系统消息")
                    .setMessage(attachData.getAccount() + "请求添加好友")
                    .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ImClientProxy.ackAddFriendRequest(attachData.getAccount(), true, new RequestCallback<Void>() {

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

                            chatUserAdapter.getData().remove(current_position);
                            chatUserAdapter.notifyItemRemoved(current_position);
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ImClientProxy.ackAddFriendRequest(attachData.getAccount(), false, new RequestCallback<Void>() {

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
                            chatUserAdapter.getData().remove(current_position);
                            chatUserAdapter.notifyItemRemoved(current_position);
                        }
                    }).setCancelable(false)
                    .create();
        else {
            addFriendDialog.setMessage(attachData.getAccount() + "请求添加好友");
        }
        addFriendDialog.show();
    }

    public void addFriendMessage(AddFriendNotify attachData) {
        ArrayList<ChatMessageWrapper> data = chatUserAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getType() == 4) {
                return;
            }
        }
        chatUserAdapter.addItem(new ChatMessageWrapper("验证消息"));
        systemCounter++;
    }


    //    @Override
//    public void pushUserInfo(NimUserInfo userInfo) {
//        ChatMessageWrapper chatMessageWrapper = new ChatMessageWrapper(userInfo);
//        ArrayList<ChatMessageWrapper> data = chatUserAdapter.getData();
//        for (int i = 0; i < data.size(); i++) {
//            if (data.get(i).getType() == 4 && !data.get(i).getRecentContact().getFromAccount().equals(userInfo.getAccount()) && !chatUserAdapter.getData().contains(chatMessageWrapper)) {
//                chatUserAdapter.addItem(chatMessageWrapper);
//            }
//        }
//
//    }

    private AlertDialog addFriendDialog;

    @Override
    public void onReceive(SystemMessage message) {

        Log.e(TAG, "onReceiveSystemMessage: ");
        if (message.getType() == SystemMessageType.AddFriend) {
            AddFriendNotify attachData = (AddFriendNotify) message.getAttachObject();
            if (attachData != null) {
                // 针对不同的事件做处理
                if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT) {
                    // 对方直接添加你为好友
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND) {
                    // 对方通过了你的好友验证请求
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                    // 对方拒绝了你的好友验证请求
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                    addFriendMessage(attachData);
                    chatUserAdapter.notifyItemChanged(0);
                }
            }
        }

    }

    @Override
    public void registerComplete() {
        Log.e(TAG, "registerComplete: ");
    }

    @Override
    public void unregisterComplete() {
        Log.e(TAG, "unregisterComplete: ");
    }

    @Override
    public void onReceive(List<IMMessage> msg) {
        Log.e(TAG, "onReceive: ");
        ChatItemActivity.addRecord(msg);
//        getRecentContacts();
        for (int i = 0; i < msg.size(); i++) {
            if (msg.get(i).getSessionType() == SessionTypeEnum.P2P)
                chatUserAdapter.change(msg.get(i).getSessionId());
            else {
                chatUserAdapter.changeGroup(msg.get(i).getSessionId());
            }
        }
    }


    @Override
    public void onReceive(String account, String text) {
        chatUserAdapter.change(account, text);
    }

    private static int systemCounter = 1;
    private static int systemCounter2 = 1;
}