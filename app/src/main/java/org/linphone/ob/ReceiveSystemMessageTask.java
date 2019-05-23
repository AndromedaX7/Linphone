package org.linphone.ob;

import com.netease.nimlib.sdk.msg.model.SystemMessage;

public interface ReceiveSystemMessageTask extends TaskListener {
    void onReceive(SystemMessage msg);
}
