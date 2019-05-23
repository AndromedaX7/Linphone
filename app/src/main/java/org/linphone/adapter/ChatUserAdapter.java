package org.linphone.adapter;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.activity.ChatItemActivity;
import org.linphone.app.App;
import org.linphone.bean.ChatMessageWrapper;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by miao on 2018/10/25.
 */
public class ChatUserAdapter extends BaseAdapter<ChatMessageWrapper, ChatUserAdapter.ViewHolder> {


    public ChatUserAdapter(ArrayList<ChatMessageWrapper> data) {
        super(data);
    }

    @Override
    protected ViewHolder create(View view, int itemViewType) {
        return new ViewHolder(view);
    }

    @Override
    protected void bindView(ViewHolder viewHolder, int position, ChatMessageWrapper item) {
        int type = item.getType();
        switch (type) {
            case 1:
                NimUserInfo userInfo = item.getUserInfo();
                viewHolder.mFriendName.setText(userInfo.getName());
                viewHolder.mContent.setText(item.getContent());
                int c = NIMSDK.getMsgService().queryRecentContact(userInfo.getAccount(), SessionTypeEnum.P2P).getUnreadCount();

                viewHolder.unReadCount.setVisibility(c > 0 ? View.VISIBLE : View.GONE);
                viewHolder.unReadCount.setText(String.valueOf(c));
                break;
            case 2:
                AddFriendNotify addFriendData = item.getAddFriendData();
                viewHolder.mFriendName.setText("系统消息:请求添加好友");
                viewHolder.mContent.setText(addFriendData.getAccount());
                break;
            case 3:


                Team team = item.getTeam();
                int c2 = NIMSDK.getMsgService().queryRecentContact(team.getId(), SessionTypeEnum.Team).getUnreadCount();
                viewHolder.mFriendName.setText(team.getName());
                viewHolder.mContent.setText(item.getContent());
                viewHolder.unReadCount.setVisibility(c2 > 0 ? View.VISIBLE : View.GONE);
                viewHolder.unReadCount.setText(String.valueOf(c2));
                break;
            case 4:
                if (App.newRecord)
                    viewHolder.unReadTip.setVisibility(View.VISIBLE);
                else {
                    viewHolder.unReadTip.setVisibility(View.GONE);

                }
                viewHolder.unReadCount.setVisibility(View.GONE);
                viewHolder.unReadCount.setText("");
                viewHolder.mFriendName.setText(item.getTitle());
                viewHolder.mContent.setText("");
                viewHolder.mContent.setVisibility(View.GONE);
                break;
            case 5:
                viewHolder.unReadCount.setVisibility(View.GONE);
                viewHolder.mFriendName.setText(item.getTitle());
                viewHolder.mContent.setText("");
                viewHolder.mContent.setVisibility(View.GONE);
                break;

        }
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_chat_user_record;
    }

    public void addItem(ChatMessageWrapper chatMessageWrapper) {
        data.add(chatMessageWrapper);
        notifyDataSetChanged();
    }

    public void change(String fromAccount) {
        for (int i = 0; i < data.size(); i++) {
            Log.e("Type", "change: " + i + data.get(i).getType());
            if (data.get(i).getType() == 1) {
                if (fromAccount.equals(data.get(i).getUserInfo().getAccount())) {
                    ArrayList<IMMessage> imMessages = ChatItemActivity.imMessageCache.get(fromAccount);
                    if (imMessages != null)
                        data.get(i).setContent(imMessages.get(imMessages.size() - 1).getContent());

                    notifyDataSetChanged();
                }
            }
        }

        Log.e("c", "change: " + fromAccount);
        notifyDataSetChanged();
    }

    public void changeGroup(String fromAccount) {
        for (int i = 0; i < data.size(); i++) {
            Log.e("Type", "change: " + i + data.get(i).getType());
            if (data.get(i).getType() == 3) {
                if (fromAccount.equals(data.get(i).getTeam().getId())) {
                    ArrayList<IMMessage> imMessages = ChatItemActivity.imMessageCache.get(fromAccount);
                    if (imMessages != null && imMessages.size() > 0)
                        data.get(i).setContent(imMessages.get(imMessages.size() - 1).getContent());

                    notifyDataSetChanged();
                }
            }
        }

        Log.e("c", "change: " + fromAccount);
        notifyDataSetChanged();
    }

    public void change(String account, String text) {
        for (int i = 0; i < data.size(); i++) {
            Log.e("Type", "change: " + i + data.get(i).getType());
            if (data.get(i).getType() == 1) {
                if (account.equals(data.get(i).getUserInfo().getAccount())) {
                    ArrayList<IMMessage> imMessages = ChatItemActivity.imMessageCache.get(account);
                    if (imMessages != null)
                        data.get(i).setContent(text);

                    notifyDataSetChanged();
                }
            }
        }


        notifyDataSetChanged();
    }

    public static class ViewHolder extends BaseAdapter.ViewHolder {
        @BindView(R.id.mFriendName)
        TextView mFriendName;
        @BindView(R.id.mContent)
        TextView mContent;
        @BindView(R.id.unReadCount)
        TextView unReadCount;
        @BindView(R.id.unReadTip)
        TextView unReadTip;

        public ViewHolder(View view) {
            super(view);
        }
    }
}
