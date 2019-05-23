package org.linphone.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;

import org.linphone.ImClientProxy;
import org.linphone.R;
import org.linphone.preview.SlideSwapAction;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.netease.nimlib.sdk.friend.model.AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST;
import static com.netease.nimlib.sdk.msg.constant.SystemMessageStatus.declined;
import static com.netease.nimlib.sdk.msg.constant.SystemMessageStatus.passed;

public class SystemMessageAdapter extends RecyclerView.Adapter<SystemMessageAdapter.SMVH> {


    private ArrayList<SystemMessage> data = new ArrayList<>();

    @NonNull
    @Override
    public SMVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SMVH(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_add_friend_message_ext, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SMVH vh, int i) {
        vh.name.setText(data.get(i).getFromAccount());
        String content = getContent(data.get(i));
        vh.message.setVisibility(View.VISIBLE);
        vh.message.setText(content);
//        }
        vh.confirm.setTag(i);
        vh.reject.setTag(i);

        if (data.get(i).getStatus() == SystemMessageStatus.init && data.get(i).getAttachObject() != null && ((AddFriendNotify) data.get(i).getAttachObject()).getEvent() == RECV_ADD_FRIEND_VERIFY_REQUEST) {
            vh.confirm.setVisibility(View.VISIBLE);
            vh.reject.setVisibility(View.VISIBLE);
        } else if (data.get(i).getStatus() != SystemMessageStatus.init && data.get(i).getAttachObject() != null && ((AddFriendNotify) data.get(i).getAttachObject()).getEvent() == RECV_ADD_FRIEND_VERIFY_REQUEST) {
            vh.confirm.setVisibility(View.INVISIBLE);
            vh.reject.setVisibility(View.INVISIBLE);
            vh.message.setText(content + parseState(data.get(i).getStatus()));
        }else {
            vh.confirm.setVisibility(View.INVISIBLE);
            vh.reject.setVisibility(View.INVISIBLE);
            vh.message.setText(content + parseState(data.get(i).getStatus()));
        }
    }

    private String parseState(SystemMessageStatus status) {
        switch (status) {
            case passed:
                return "[ 已通过 ]";
            case declined:
                return "[ 已拒绝 ]";
            case ignored:
                return "[ 已忽略 ]";
        }
        return "";
    }


    private String getContent(SystemMessage message) {
        if (message.getAttachObject() instanceof AddFriendNotify) {
            switch (((AddFriendNotify) message.getAttachObject()).getEvent()) {
                case RECV_ADD_FRIEND_VERIFY_REQUEST:
                    return "发起好友验证请求";
                case RECV_AGREE_ADD_FRIEND:
                    return "同意加你为好友";
                case RECV_ADD_FRIEND_DIRECT:
                    return "直接加你为好友";
                case RECV_REJECT_ADD_FRIEND:
                    return "拒绝加你为好友";
            }
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<SystemMessage> data) {
        int size = this.data.size();
        int newSize = data.size();
        this.data.clear();
        notifyItemRangeRemoved(0, size);
        this.data.addAll(data);
        notifyItemRangeInserted(0, newSize);
    }

    public void addItem(SystemMessage d) {
        data.add(d);
        notifyItemInserted(data.size() - 1);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void removeAll() {
        int size = data.size();
        data.clear();
        notifyItemRangeRemoved(0, size - 1);
    }


    class SMVH extends RecyclerView.ViewHolder implements SlideSwapAction {
        @BindView(R.id.requireTitle)
        TextView requireTitle;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.confirm)
        TextView confirm;
        @BindView(R.id.reject)
        TextView reject;
        @BindView(R.id.message)
        TextView message;
        @BindView(R.id.slide_itemView)
        RelativeLayout slide_itemView;
        @BindView(R.id.delete)
        TextView delete;
        @OnClick(R.id.delete)
        void onDeleteCall() {
            SystemMessage systemMessage = data.get(getLayoutPosition());
            NIMClient.getService(SystemMessageService.class).deleteSystemMessage(systemMessage.getMessageId());
            data.remove(getLayoutPosition());
            notifyItemRemoved(getLayoutPosition());
        }

        public SMVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick({R.id.confirm, R.id.reject})
        public void onViewClicked(View view) {
            boolean agree = true;
            switch (view.getId()) {
                case R.id.confirm:
                    agree = true;
                    break;
                case R.id.reject:
                    agree = false;
                    break;
            }
            SystemMessage message = data.get(getLayoutPosition());
            if (message.getType() == SystemMessageType.AddFriend) {
                AddFriendNotify attachData = (AddFriendNotify) message.getAttachObject();
                if (attachData != null) {
                    // 针对不同的事件做处理
                    if (attachData.getEvent() == RECV_ADD_FRIEND_VERIFY_REQUEST) {
                        boolean finalAgree = agree;
                        ImClientProxy.ackAddFriendRequest(message.getFromAccount(), agree, new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                Log.e(TAG, "onSuccess: ");
                                NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(), finalAgree ? passed : declined);

                                Toast.makeText(itemView.getContext(), finalAgree ? "已同意" : "已拒绝", Toast.LENGTH_SHORT).show();
                                if (notifyChanged != null) {
                                    notifyChanged.notifyDataSetChanged();
                                }

                            }

                            @Override
                            public void onFailed(int code) {
                                Log.e(TAG, "onFailed: " + code);
                            }

                            @Override
                            public void onException(Throwable exception) {
                                exception.printStackTrace();
                            }
                        });
                    }
                }
            }

        }

        @Override
        public float getActionWidth() {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, itemView.getContext().getResources().getDisplayMetrics());
        }

        @Override
        public View ItemView() {
            return slide_itemView;
        }
    }

    private NotifyChanged notifyChanged;

    public void setNotifyChangedCallback(NotifyChanged notifyChanged) {
        this.notifyChanged = notifyChanged;
    }

    public interface NotifyChanged {
        void notifyDataSetChanged();
    }

    private String TAG = getClass().getName();
}
