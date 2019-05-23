package org.linphone.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.activity.ChatItemActivity;
import org.linphone.app.App;
import org.linphone.bean.ChatMessageWrapper;
import org.linphone.preview.OnItemClickListener;
import org.linphone.preview.SlideSwapAction;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecentContactsAdapter extends RecyclerView.Adapter<RecentContactsAdapter.RecentContactsViewHolder> {
    private ArrayList<ChatMessageWrapper> data = new ArrayList<>();

    @NonNull
    @Override
    public RecentContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RecentContactsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_record_ext, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentContactsViewHolder viewHolder, int i) {
        ChatMessageWrapper item = data.get(i);
        viewHolder.type = getData().get(i).getType();
        int type = item.getType();
        switch (type) {
            case 1:
                NimUserInfo userInfo = item.getUserInfo();
                String alias = NIMClient.getService(FriendService.class).getFriendByAccount(userInfo.getAccount()).getAlias();
                if (TextUtils.isEmpty(alias))
                    viewHolder.mFriendName.setText(userInfo.getName());
                else
                    viewHolder.mFriendName.setText(alias);
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
                viewHolder.unReadTip.setVisibility(View.GONE);
                viewHolder.unReadCount.setVisibility(View.GONE);
                viewHolder.mFriendName.setText(item.getTitle());
                viewHolder.mContent.setText("");
                viewHolder.mContent.setVisibility(View.GONE);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public void changeGroup(String fromAccount) {
        for (int i = 0; i < data.size(); i++) {
            Log.e("Type", "change: " + i + data.get(i).getType());
            if (data.get(i).getType() == 3) {
                if (fromAccount.equals(data.get(i).getTeam().getId())) {
                    ArrayList<IMMessage> imMessages = ChatItemActivity.imMessageCache.get(fromAccount);
                    if (imMessages != null && imMessages.size() > 0)
                        data.get(i).setContent(imMessages.get(imMessages.size() - 1).getContent());
                    notifyItemChanged(i);
//                    notifyDataSetChanged();
                }
            }
        }

//        Log.e("c", "change: " + fromAccount);
//        notifyDataSetChanged();
    }

    public void change(String fromAccount) {
        for (int i = 0; i < data.size(); i++) {
            Log.e("Type", "change: " + i + data.get(i).getType());
            if (data.get(i).getType() == 1) {
                if (fromAccount.equals(data.get(i).getUserInfo().getAccount())) {
                    ArrayList<IMMessage> imMessages = ChatItemActivity.imMessageCache.get(fromAccount);
                    if (imMessages != null)
                        data.get(i).setContent(imMessages.get(imMessages.size() - 1).getContent());

                    notifyItemChanged(i);
                }
            }
        }

//        Log.e("c", "change: " + fromAccount);
//        notifyDataSetChanged();
    }

    public void change(String account, String text) {
        for (int i = 0; i < data.size(); i++) {
            Log.e("Type", "change: " + i + data.get(i).getType());
            if (data.get(i).getType() == 1) {
                if (account.equals(data.get(i).getUserInfo().getAccount())) {
                    ArrayList<IMMessage> imMessages = ChatItemActivity.imMessageCache.get(account);
                    if (imMessages != null)
                        data.get(i).setContent(text);

                    notifyItemChanged(i);
                }
            }
        }


//        notifyDataSetChanged();
    }

    public void addItem(ChatMessageWrapper chatMessageWrapper) {
        data.add(chatMessageWrapper);
        notifyItemInserted(data.size() - 1);
    }

    public ArrayList<ChatMessageWrapper> getData() {
        return data;
    }

    public ChatMessageWrapper getItem(int pos) {
        return data.get(pos);
    }

    public void addData(ArrayList<ChatMessageWrapper> wrappers) {
        int size = data.size();
        int newSize = wrappers.size();
        data.addAll(wrappers);
        notifyItemRangeInserted(size, newSize);
    }

    public void clear() {
        int size = data.size();
        data.clear();
        notifyItemRangeChanged(0, size);
    }


    class RecentContactsViewHolder extends RecyclerView.ViewHolder implements SlideSwapAction {

        @BindView(R.id.mFriendName)
        TextView mFriendName;
        @BindView(R.id.mContent)
        TextView mContent;
        @BindView(R.id.unReadCount)
        TextView unReadCount;
        @BindView(R.id.unReadTip)
        TextView unReadTip;

        @BindView(R.id.item_root)
        LinearLayout item_root;
        @BindView(R.id.slide_delete)
        FrameLayout slide_delete;
        @BindView(R.id.delete)
        TextView delete;
        @BindView(R.id.slide_itemView)
        RelativeLayout slide_itemView;

        @OnClick(R.id.delete)
        void onDeleteCall() {
            ChatMessageWrapper chatMessageWrapper = data.get(getLayoutPosition());
            RecentContact recentContact = chatMessageWrapper.getRecentContact();
            if (recentContact != null)
                NIMClient.getService(MsgService.class).deleteRecentContact(recentContact);
            data.remove(getLayoutPosition());
            notifyItemRemoved(getLayoutPosition());
        }

        private int type;

        public RecentContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(getLayoutPosition());
                }
            });
        }

        @Override
        public float getActionWidth() {
            if (type == 4 || type == 5)
                return 0;
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, item_root.getContext().getResources().getDisplayMetrics());
        }

        @Override
        public View ItemView() {
            return slide_itemView;
        }
    }

    private OnItemClickListener itemClickListener;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
