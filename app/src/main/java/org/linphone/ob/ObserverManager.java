package org.linphone.ob;

import android.util.Log;

import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObserverManager {
    private static ObserverManager manager = new ObserverManager();

    private HashMap<String, ArrayList<TaskListener>> obsCache = new HashMap<>();

    private ObserverManager() {
    }

    public static ObserverManager getInstance() {
        return manager;
    }

    public void register(String key, TaskListener target) {
        ArrayList<TaskListener> taskListeners = obsCache.get(key);
        if (taskListeners == null) {
            taskListeners = new ArrayList<>();
            obsCache.put(key, taskListeners);
        }
        taskListeners.add(target);
        target.registerComplete();
        notifyCacheMessage(key);
    }

    private void notifyCacheMessage(String key) {
        switch (key) {
            case TaskListener.RECEIVE_SYSTEM_MESSAGE:
                for (int i = 0; i < systemMessagesCache.size(); i++) {
                    notifyReceiveSystemMessage(systemMessagesCache.get(i));
                }
                systemMessagesCache.clear();
                break;
            case TaskListener.RECEIVE_FRIEND_MESSAGE:
                notifyReceiveFriendMessage(friendMessagesCache);
                friendMessagesCache.clear();
                break;
        }
    }

    public void unregister(String key, TaskListener target) {
        ArrayList<TaskListener> taskListeners = obsCache.get(key);
        if (taskListeners != null) {
            taskListeners.remove(target);
            target.unregisterComplete();
        }
    }

    public void notifyReceiveFriendMessage(List<IMMessage> imMessage) {
        ArrayList<TaskListener> taskListeners = obsCache.get(TaskListener.RECEIVE_FRIEND_MESSAGE);
        if (taskListeners == null) {
            friendMessagesCache.addAll(imMessage);
        } else {
            for (int i = 0; i < taskListeners.size(); i++) {
                Log.e("Task", "notifyReceiveFriendMessage: " + taskListeners.get(i).getClass().getName());
                if (taskListeners.get(i) instanceof ReceiveFriendMessageTask) {
                    ((ReceiveFriendMessageTask) taskListeners.get(i)).onReceive(imMessage);
                } else {
                    taskListeners.get(i).onReceive(imMessage);
                }
            }
        }
    }

    public void notifyReceiveSystemMessage(SystemMessage msg) {
        ArrayList<TaskListener> taskListeners = obsCache.get(TaskListener.RECEIVE_SYSTEM_MESSAGE);
        if (taskListeners == null) {
            systemMessagesCache.add(msg);
        } else {
            for (int i = 0; i < taskListeners.size(); i++) {
                if (taskListeners.get(i) instanceof ReceiveSystemMessageTask) {
                    ((ReceiveSystemMessageTask) taskListeners.get(i)).onReceive(msg);
                } else {
                    taskListeners.get(i).onReceive(msg);
                }
            }
        }
    }

    private ArrayList<SystemMessage> systemMessagesCache = new ArrayList<>();
    private ArrayList<IMMessage> friendMessagesCache = new ArrayList<>();


    public void notifyReceiveSelfText(String account, String msg) {
        ArrayList<TaskListener> taskListeners = obsCache.get(TaskListener.RECEIVE_MY_TEXT_MESSAGE);
        if (taskListeners != null)
            for (int i = 0; i < taskListeners.size(); i++) {
                if (taskListeners.get(i) instanceof ReceiveTextMessage){
                    ((ReceiveTextMessage) taskListeners.get(i)).onReceive(account,msg);
                }
            }
    }

    public void notifyFriendAddOrDelete() {
        ArrayList<TaskListener> taskListeners = obsCache.get(TaskListener.RECEIVE_FRIEND_CHANGED);
        if (taskListeners!=null)
            for (int i = 0; i < taskListeners.size(); i++) {
                if (taskListeners.get(i) instanceof ReceiveFriendChanged){
                    ((ReceiveFriendChanged) taskListeners.get(i)).onReceive();
                }
            }
    }
}
