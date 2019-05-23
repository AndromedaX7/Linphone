package org.linphone.ob;

import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;

public interface ReceiveFriendMessageTask extends TaskListener {
    void onReceive(List<IMMessage> msg);
}
