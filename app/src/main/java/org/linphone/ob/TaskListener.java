package org.linphone.ob;

import android.util.Log;

import com.netease.nimlib.sdk.msg.model.SystemMessage;

public interface TaskListener {
    void registerComplete();

    void unregisterComplete();

    String RECEIVE_FRIEND_MESSAGE = "receive_friend_message";
    String RECEIVE_SYSTEM_MESSAGE = "receive_system_message";
    String RECEIVE_MY_TEXT_MESSAGE = "receive_my_text_message";
    String RECEIVE_FRIEND_CHANGED = "receive_friend_changed";

    default void onReceive(Object msg) {
        Log.e("onReceive: ", msg.toString());
    }
//    String RECEIVE_FRIEND_MESSAGE = "receive_friend_message";
//    String RECEIVE_FRIEND_MESSAGE = "receive_friend_message";
//    String RECEIVE_FRIEND_MESSAGE = "receive_friend_message";
}
