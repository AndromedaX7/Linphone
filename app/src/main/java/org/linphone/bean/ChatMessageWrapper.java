package org.linphone.bean;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

public class ChatMessageWrapper {
    private int type;
    private NimUserInfo userInfo;
    private String title;
    private AddFriendNotify addFriendData;
    private RecentContact recentContact;
    private Team team;

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public NimUserInfo getUserInfo() {
        return userInfo;
    }

    public AddFriendNotify getAddFriendData() {
        return addFriendData;
    }

    public ChatMessageWrapper(NimUserInfo userInfo) {
        this.userInfo = userInfo;
        type = 1;
    }

    public ChatMessageWrapper(AddFriendNotify userInfo) {
        this.addFriendData = userInfo;
        type = 2;
    }

    public ChatMessageWrapper(String title) {
        type = 4;
        this.title = title;
    }

    public ChatMessageWrapper(String title, int type) {
        this.type = type;
        this.title = title;
    }

    public ChatMessageWrapper(RecentContact userInfo) {
        this.recentContact = userInfo;
        if (recentContact.getSessionType() == SessionTypeEnum.P2P) {
            RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(userInfo.getContactId(), SessionTypeEnum.P2P);
            if (recentContact == null)
                this.content = "";
            else {
                content = recentContact.getContent();
                this.userInfo = NIMClient.getService(UserService.class).getUserInfo(this.recentContact.getContactId());
            }
            type = 1;
        } else {
            RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(userInfo.getContactId(), SessionTypeEnum.Team);
            content = recentContact.getContent();
            team = NIMClient.getService(TeamService.class).queryTeamBlock(recentContact.getContactId());
            type = 3;
        }
    }

    public RecentContact getRecentContact() {
        return recentContact;
    }

    public void setRecentContact(RecentContact recentContact) {
        this.recentContact = recentContact;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        else if (obj instanceof ChatMessageWrapper) {
            if (((ChatMessageWrapper) obj).getType() == getType()) {
                switch (type) {
                    case 1:
                        return userInfo.getAccount().equals(((ChatMessageWrapper) obj).userInfo.getAccount());
                    case 2:
                        return addFriendData.getAccount().equals(((ChatMessageWrapper) obj).getAddFriendData().getAccount());
                    case 3:
                        return false;
                }
            }
        }
        return false;
    }

    public String getTitle() {
        return title;
    }

    public Team getTeam() {
        return team;
    }
}
