package org.linphone;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.adapter.ExpandableAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImClientProxy {

    public static String meId = "";

    public static void login(String account, String password, RequestCallback callback) {

//          if (account.equals("4089")) account="1001";
        meId = account;
        NIMClient.getService(AuthService.class).login(new LoginInfo(account, password)).setCallback(callback);
    }

    public static void getRecentContacts(RequestCallback<List<RecentContact>> requestCallback) {
        NIMClient.getService(MsgService.class).queryRecentContacts()
                .setCallback(requestCallback);
    }

    public static void ackAddFriendRequest(String account, boolean agree, RequestCallback<Void> callback) {
        NIMClient.getService(FriendService.class).ackAddFriendRequest(account, agree).setCallback(callback);
    }

    public static void queryMessageListEx(boolean p2p, String account, RequestCallback<List<IMMessage>> callback) {
        IMMessage current = MessageBuilder.createEmptyMessage(account, p2p ? SessionTypeEnum.P2P : SessionTypeEnum.Team, System.currentTimeMillis());
        NIMClient.getService(MsgService.class).queryMessageListEx(current, QueryDirectionEnum.QUERY_OLD, 20, true).setCallback(
                callback);
    }

    public static void queryMessageListEx(IMMessage imMessage, RequestCallback<List<IMMessage>> callback) {
        NIMClient.getService(MsgService.class).queryMessageListEx(imMessage, QueryDirectionEnum.QUERY_OLD, 20, true).setCallback(
                callback);
    }

    public static String queryRecentContactContent(String account) {
        return NIMClient.getService(MsgService.class).queryRecentContact(account, SessionTypeEnum.P2P).getContent();
    }

    public static void fetchUserInfo(ArrayList<String> currentSearchUser, RequestCallback<List<NimUserInfo>> requestCallback) {
        NIMClient.getService(UserService.class).fetchUserInfo(currentSearchUser).setCallback(requestCallback);
    }
}
