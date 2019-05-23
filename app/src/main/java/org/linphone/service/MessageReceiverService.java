package org.linphone.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.LinphoneActivity;
import org.linphone.R;
import org.linphone.activity.ChatItemActivity;
import org.linphone.activity.SystemMessageDetails;
import org.linphone.app.App;
import org.linphone.ob.ObserverManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MessageReceiverService extends Service {

    NotificationManager manager;
    NotificationChannel channel;

    private int requestId = 20153489;

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("channel-im", "channel-im", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        registerObserver(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerObserver(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void registerObserver(boolean flag) {
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(new Observer<List<IMMessage>>() {
            @Override
            public void onEvent(List<IMMessage> imMessages) {
                Iterator<IMMessage> iterable = imMessages.iterator();
                while (iterable.hasNext()) {
                    IMMessage target = iterable.next();
                    if (target.getSessionType() == SessionTypeEnum.P2P) {
                        if (target.getMsgType() == MsgTypeEnum.tip) {
                            boolean flag = (boolean) target.getRemoteExtension().get("blackListNotifier");
                            if (NIMClient.getService(FriendService.class).isMyFriend(target.getSessionId())) {
                                HashMap<FriendFieldEnum, Object> map = new HashMap<>();
                                HashMap<String, Object> extension = new HashMap<>();
                                extension.put("inBlackList", flag);
                                map.put(FriendFieldEnum.EXTENSION, extension);
                                NIMClient.getService(FriendService.class).updateFriendFields(target.getSessionId(), map).setCallback(new RequestCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void param) {
                                        Log.e("update", "onSuccess: "  );
                                    }

                                    @Override
                                    public void onFailed(int code) {

                                    }

                                    @Override
                                    public void onException(Throwable exception) {

                                    }
                                });

                            }
                            NIMClient.getService(MsgService.class).deleteChattingHistory(target);
                            iterable.remove();
                        } else if (NIMSDK.getFriendService().isInBlackList(target.getSessionId())) {
//                            sendBlackListNotifier(target.getSessionId());
                            iterable.remove();
                        } else if (!NIMSDK.getFriendService().isMyFriend(target.getSessionId())) {
                            iterable.remove();
                        } else {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MessageReceiverService.this, "channel-im");
                            builder.setSmallIcon(R.mipmap.ic_launcher2);
                            builder.setContentIntent(makeActivityReStart(target.getSessionId(), true, true, requestId));
//                            builder.setContentText("您有一条新短消息");
                            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            builder.setVibrate(new long[]{0,1000,1000,1000});
                            builder.setAutoCancel(true);
                            builder.setContentText("来自用户:" + target.getFromNick());
                            manager.notify(requestId, builder.build());
                        }
                    } else {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MessageReceiverService.this, "channel-im");
                        builder.setSmallIcon(R.mipmap.ic_launcher2);
//                        builder.setContentText("您有一条新短消息");
                        builder.setContentText("群消息");
                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        builder.setVibrate(new long[]{0,1000,1000,1000});
                        builder.setAutoCancel(true);
                        builder.setContentIntent(makeActivityReStart(target.getSessionId(), false, true, requestId));
                        manager.notify(requestId, builder.build());
                    }
                }
                ObserverManager.getInstance().notifyReceiveFriendMessage(imMessages);


                Log.e("Server", "onEvent: " + imMessages.size());
            }
        }, flag);

        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate((Observer<List<NimUserInfo>>) nimUserInfos -> {

//            BaseFragment baseFragment = map2.get(FriendsFragment.class);
//            if (baseFragment == null) {
//                updateUserInfo.addAll(nimUserInfos);
//            } else {
//                if (updateUserInfo.size() > 0) {
//                    baseFragment.updateUserInfo(updateUserInfo);
//                    inComingMessagesUnReceive.clear();
//                }
//                baseFragment.updateUserInfo(nimUserInfos);
//            }
        }, flag);
        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(new Observer<List<OnlineClient>>() {
            @Override
            public void onEvent(List<OnlineClient> onlineClients) {

            }
        }, flag);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
            @Override
            public void onEvent(StatusCode statusCode) {

            }
        }, flag);
        NIMClient.getService(AuthServiceObserver.class).observeLoginSyncDataStatus(new Observer<LoginSyncStatus>() {
            @Override
            public void onEvent(LoginSyncStatus loginSyncStatus) {

            }
        }, flag);

        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(new Observer<SystemMessage>() {
            @Override
            public void onEvent(SystemMessage message) {
                ObserverManager.getInstance().notifyReceiveSystemMessage(message);
                if (message.getType() == SystemMessageType.AddFriend) {
                    App.newRecord = true;
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MessageReceiverService.this, "channel-im");
                    builder.setSmallIcon(R.mipmap.ic_launcher2);
//                    builder.setContentText("您有一条新短消息");
                    builder.setContentText("验证消息");
                    builder.setAutoCancel(true);
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    builder.setVibrate(new long[]{0,1000,1000,1000});
                    builder.setContentIntent(makeActivityReStart("", false, false, requestId));
                    manager.notify(requestId, builder.build());
                }
            }
        }, flag);


        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(new Observer<FriendChangedNotify>() {
            @Override
            public void onEvent(FriendChangedNotify friendChangedNotify) {
                List<Friend> addedOrUpdatedFriends = friendChangedNotify.getAddedOrUpdatedFriends(); // 新增的好友
                List<String> deletedFriendAccounts = friendChangedNotify.getDeletedFriends(); // 删除好友或者被解除好友
                ObserverManager.getInstance().notifyFriendAddOrDelete();
            }
        }, flag);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
            @Override
            public void onEvent(StatusCode statusCode) {
                switch (statusCode) {

                }
            }
        }, flag);
        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(new Observer<List<OnlineClient>>() {
            @Override
            public void onEvent(List<OnlineClient> onlineClients) {

                if (onlineClients == null || onlineClients.size() == 0) {
                    return;
                }
                for (int i = 0; i < onlineClients.size(); i++) {
                    OnlineClient client = onlineClients.get(i);
                    Log.e("OtherClient", "onEvent: " + onlineClients.get(i).getClientType());
                    switch (client.getClientType()) {
                        case ClientType.Windows:
                            // PC端
                            break;
                        case ClientType.MAC:
                            // MAC端
                            break;
                        case ClientType.Web:
                            // Web端
                            break;
                        case ClientType.iOS:
                            // IOS端
                            break;
                        case ClientType.Android:
                            // Android端
                            break;
                        default:
                            break;
                    }
                }
            }
        }, flag);
        NIMClient.getService(MsgServiceObserve.class).observeRevokeMessage(new Observer<RevokeMsgNotification>() {
            @Override
            public void onEvent(RevokeMsgNotification revokeMsgNotification) {
                ChatItemActivity current = ChatItemActivity.current();
                if (current==null){
                    ChatItemActivity.imMessageCache.clear();
                }else {
                    current.notifyRevoke();
                }
            }
        }, flag);
    }


    private PendingIntent makeActivityReStart(String sessionId, boolean isP2P, boolean fromUser, int requestCode) {
//        Intent[] intents = new Intent[3];
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        Intent result = null;
        if (fromUser) {
            result = new Intent(this, ChatItemActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (isP2P) {
                result.putExtra("data", NIMSDK.getUserService().getUserInfo(sessionId)).putExtra("isP2P", true);
            } else {
                result.putExtra("team", NIMSDK.getTeamService().queryTeamBlock(sessionId)).putExtra("isP2P", false);
            }
            taskStackBuilder.addParentStack(ChatItemActivity.class);
        } else {
            result = new Intent(this, SystemMessageDetails.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            taskStackBuilder.addParentStack(SystemMessageDetails.class);
        }


////        intents[2] = new Intent(this, PersonTrajectoryAnalysisActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intents[2] =Intent.makeRestartActivityTask(new ComponentName(this,PersonTrajectoryAnalysisActivity.class)).putExtra("idNumber", idNumber);
//
//        switch (flag) {
//            case "0":
        taskStackBuilder.addNextIntent(new Intent(this, LinphoneActivity.class));
//                break;
//            case "1":
//                taskStackBuilder.addNextIntent(new Intent(this, PetitionActivity.class));
//                break;
//            case "2":
//                taskStackBuilder.addNextIntent(new Intent(this, ConcernedWithDrugsActivity.class));
//            default:
//        }
        taskStackBuilder.addNextIntent(result);

        return taskStackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
