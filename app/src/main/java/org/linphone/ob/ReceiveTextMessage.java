package org.linphone.ob;

public interface ReceiveTextMessage extends TaskListener {
    void onReceive(String account, String text);
}
