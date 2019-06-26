package org.linphone.adapter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.media.player.AudioPlayer;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.activity.ImageDetailsActivity;
import org.linphone.activity.VideoPlayerActivity;
import org.linphone.cache.CacheManager;
import org.linphone.utils.DateUtil;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class ChatAdapter extends BaseAdapter<IMMessage, ChatAdapter.ChatViewHolder> implements View.OnClickListener {

    private int textSize = 18;
    private String who;
    private boolean isTeam;

    public ChatAdapter() {
        super();
    }

    @Override
    protected ChatViewHolder create(View view, int itemViewType) {
        return new ChatViewHolder(view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void bindView(ChatViewHolder vh, int position, IMMessage item) {
        NimUserInfo me = NIMClient.getService(UserService.class).getUserInfo(who);
        if (me.getExtensionMap().containsKey("chat_text_size")) {
            textSize = (int) me.getExtensionMap().get("chat_text_size");
        }
        vh.message.setTextSize(textSize);

        NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(item.getFromAccount());
//        if (who.equals(item.getFromAccount())){
//            Glide.with(vh.me).load(userInfo.getAvatar()).placeholder(R.mipmap.ic_launcher2).error(R.mipmap.ic_launcher2).into(vh.me);
//        }else {
//            Glide.with(vh.iconOther).load(userInfo.getAvatar()).placeholder(R.mipmap.ic_launcher2).error(R.mipmap.ic_launcher2).into(vh.iconOther);
//        }

        if (item.getMsgType() == MsgTypeEnum.tip) {
            vh.itemView.setVisibility(View.GONE);
        } else {
            vh.itemView.setVisibility(View.VISIBLE);
        }
        vh.setIMMessage(item);
        vh.time.setText(DateUtil.format("yyyy-MM-dd HH:mm:ss", item.getTime()));
        vh.message.setText(item.getContent());
        vh.speak.setTag(position);
        vh.speak.setOnClickListener(this);
        vh.message.setGravity(!who.equals(item.getFromAccount()) ? Gravity.START : Gravity.END);
        vh.speak.setGravity(!who.equals(item.getFromAccount()) ? Gravity.START : Gravity.END);
        FrameLayout.LayoutParams lpSpeak = (FrameLayout.LayoutParams) vh.speak.getLayoutParams();
        lpSpeak.gravity = !who.equals(item.getFromAccount()) ? Gravity.START : Gravity.END;
        vh.speak.setLayoutParams(lpSpeak);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) vh.videoRoot.getLayoutParams();
        lp.gravity = !who.equals(item.getFromAccount()) ? Gravity.START : Gravity.END;
        vh.videoRoot.setLayoutParams(lp);
        vh.iconOther.setVisibility(!who.equals(item.getFromAccount()) ? View.VISIBLE : View.INVISIBLE);
        vh.me.setVisibility(!who.equals(item.getFromAccount()) ? View.INVISIBLE : View.VISIBLE);
//        item.getMsgType()==MsgTypeEnum.text/**/
        vh.mImg.setScaleType(who.equals(item.getFromAccount()) ? ImageView.ScaleType.FIT_END : ImageView.ScaleType.FIT_START);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) vh.mImg.getLayoutParams();
        layoutParams.gravity = !who.equals(item.getFromAccount()) ? Gravity.START : Gravity.END;
        vh.mImg.setLayoutParams(layoutParams);
        if (isTeam) {
            if (NIMClient.getService(FriendService.class).isMyFriend(item.getFromAccount())) {
                String alias = NIMClient.getService(FriendService.class).getFriendByAccount(item.getFromAccount()).getAlias();
                if (!TextUtils.isEmpty(alias)) {
                    vh.name.setText(alias);
                } else
                    vh.name.setText(item.getFromNick());
            } else {
                vh.name.setText(item.getFromNick());
            }
            vh.name.setVisibility(View.VISIBLE);
            vh.name.setGravity(!who.equals(item.getFromAccount()) ? Gravity.START : Gravity.END);

        } else {
            vh.name.setVisibility(View.GONE);
        }
        switch (item.getMsgType()) {
            case tip:
//                vh.time.setVisibility(View.GONE);
//                vh.tips.setText(item.getRemoteExtension().get("blackListNotifier").toString());
//                vh.tips.setVisibility(View.GONE);
//                vh.iconOther.setVisibility(View.GONE);
//                vh.mImg.setVisibility(View.GONE);
//                vh.me.setVisibility(View.GONE);
//                vh.name.setVisibility(View.GONE);
//                vh.speak.setVisibility(View.GONE);
//                vh.duration.setVisibility(View.GONE);
//                vh.videoRoot.setVisibility(View.GONE);
//                vh.mVideo.setVisibility(View.GONE);
//                vh.message.setVisibility(View.GONE);
                break;
            case text:
                vh.message.setVisibility(View.VISIBLE);
                vh.mImg.setVisibility(View.GONE);
                vh.speak.setVisibility(View.GONE);
                vh.videoRoot.setVisibility(View.GONE);
                vh.tips.setVisibility(View.GONE);
                break;
            case image:
                vh.mImg.setVisibility(View.VISIBLE);
                vh.message.setVisibility(View.GONE);
                vh.videoRoot.setVisibility(View.GONE);
                vh.speak.setVisibility(View.GONE);
                vh.tips.setVisibility(View.GONE);
                if (!who.equals(item.getFromAccount()) && item.getRemoteExtension() == null)
                    Glide.with(vh.mImg).load(((ImageAttachment) item.getAttachment()).getUrl()).placeholder(R.drawable.photos).error(R.drawable.photos).into(vh.mImg);
                else if (who.equals(item.getFromAccount()) && item.getRemoteExtension() != null && !TextUtils.isEmpty((CharSequence) item.getRemoteExtension().get("path")))
                    Glide.with(vh.mImg).load(new File(item.getRemoteExtension().get("path").toString())).into(vh.mImg);
                else
                    Glide.with(vh.mImg).load(((ImageAttachment) item.getAttachment()).getUrl()).placeholder(R.drawable.photos).error(R.drawable.photos).into(vh.mImg);
                break;
            case audio:
                vh.speak.setVisibility(View.VISIBLE);
                vh.message.setVisibility(View.GONE);
                vh.videoRoot.setVisibility(View.GONE);
                vh.tips.setVisibility(View.GONE);
                vh.mImg.setVisibility(View.GONE);
                AudioAttachment attachment = (AudioAttachment) item.getAttachment();
                vh.speak.setText(DateUtil.format("mm:ss", attachment.getDuration()));
                break;
            case video:
                vh.videoRoot.setVisibility(View.VISIBLE);
                vh.speak.setVisibility(View.GONE);
                vh.message.setVisibility(View.GONE);
                vh.mImg.setVisibility(View.GONE);
                vh.tips.setVisibility(View.GONE);
                VideoAttachment va = (VideoAttachment) item.getAttachment();
                if (va.getUrl() != null) {
                    vh.duration.setText(DateUtil.format("mm:ss", va.getDuration()));
                    CacheManager.cache(va.getUrl());
                    Glide.with(vh.mVideo).load(CacheManager.pathCache.get(va.getUrl())).error(R.drawable.video_placeholder).placeholder(R.drawable.video_placeholder).into(vh.mVideo);
                } else {
                    Glide.with(vh.mVideo).load(R.drawable.video_placeholder).placeholder(R.drawable.video_placeholder).into(vh.mVideo);
                }

                break;
            case notification:
                NotificationAttachment current = (NotificationAttachment) item.getAttachment();
                vh.iconOther.setVisibility(View.GONE);
                vh.mImg.setVisibility(View.GONE);
                vh.tips.setVisibility(View.GONE);
                vh.message.setText("系统通知:" + parseNotificationType(current.getType()));
                vh.me.setVisibility(View.GONE);
                vh.name.setVisibility(View.GONE);
                vh.speak.setVisibility(View.GONE);
                vh.duration.setVisibility(View.GONE);
                vh.videoRoot.setVisibility(View.GONE);
                vh.mVideo.setVisibility(View.GONE);
                vh.message.setGravity(Gravity.CENTER);
                break;


        }

        Log.e("type", "bindView: " + item.getMsgType());
    }

    private CharSequence parseNotificationType(NotificationType type) {
        switch (type) {
            case LeaveTeam:
                return "有成员离群";
            case undefined:
                return "未定义类型";
            case KickMember:
                return "移除群成员";
            case UpdateTeam:
                return "群资料更新";
            case DismissTeam:
                return "群被解散";
            case AcceptInvite:
                return "用户接受入群邀请";
            case InviteMember:
                return "邀请新成员";
            case ChatRoomClose:
                return "聊天室被关闭了";
            case PassTeamApply:
                return "管理员通过用户入群申请";
            case TransferOwner:
                return "群组拥有者权限转移通知";
            case AddTeamManager:
                return "新增管理员通知";
            case MuteTeamMember:
                return "群成员禁言/解禁";
            case ChatRoomMemberIn:
                return "成员进入聊天室";
            case ChatRoomCommonAdd:
                return "成员设定为固定成员";
            case ChatRoomRoomMuted:
                return "聊天室被禁言了,只有管理员可以发言";
            case RemoveTeamManager:
                return "撤销管理员通知";
            case ChatRoomManagerAdd:
                return "设置为管理员";
            case ChatRoomMemberExit:
                return "成员离开聊天室";
            case ChatRoomInfoUpdated:
                return "聊天室信息被更新了";
            case ChatRoomQueueChange:
                return "队列中有变更";
            case ChatRoomRoomDeMuted:
                return "聊天室解除全体禁言状态";
            case ChatRoomCommonRemove:
                return "成员取消固定成员";
            case ChatRoomMemberKicked:
                return "成员被踢了";
            case ChatRoomManagerRemove:
                return "取消管理员";
            case ChatRoomMemberMuteAdd:
                return "成员被设置禁言";
            case ChatRoomMemberBlackAdd:
                return "成员被加黑";
            case ChatRoomMemberMuteRemove:
                return "成员被取消禁言";
            case ChatRoomQueueBatchChange:
                return "队列批量变更";
            case ChatRoomMemberBlackRemove:
                return "成员被取消黑名单";
            case ChatRoomMemberTempMuteAdd:
                return "新增临时禁言";
            case ChatRoomMyRoomRoleUpdated:
                return "成员主动更新了聊天室内的角色信息";
            case ChatRoomMemberTempMuteRemove:
                return "主动解除临时禁言";
            default:
                return "";
        }
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_chat_me_2;
    }


    public void setWho(String account) {
        who = account;
        notifyDataSetChanged();
    }


    @Override
    public void setData(ArrayList<IMMessage> data) {
        super.setData(data);
    }
//    private  ArrayList<IMMessage>  sortRecord(ArrayList<IMMessage> accountMsg) {

//        ArrayList<IMMessage> src =new ArrayList<>(accountMsg);
//        for (int i = 0; i < src.size(); i++) {
//            for (int j = i+1; j <src.size();j++) {
//                if (src.get(i).getTime()>src.get(j).getTime()){
//                    IMMessage imMessage=   src.get(i);
//                    src.set(i,src.get(j));
//                    src.set(j,imMessage);
//                }
//            }
//        }
//        return  src;
//    }


    public void isTeam(boolean team) {
        this.isTeam = team;
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        MsgAttachment attachment = data.get(tag).getAttachment();
        if (attachment instanceof AudioAttachment) {
            String fileName = ((AudioAttachment) attachment).getPathForSave();
            Log.e("fp", "onClick: " + fileName);
            AudioPlayer player = new AudioPlayer(v.getContext());
            player.setDataSource(fileName);
            player.start(AudioManager.ROUTE_EARPIECE);
        }
    }


    class ChatViewHolder extends BaseAdapter.ViewHolder {
        @BindView(R.id.mImg)
        ImageView mImg;
        @BindView(R.id.icon_other)
        ImageView iconOther;
        @BindView(R.id.message)
        TextView message;
        @BindView(R.id.me)
        ImageView me;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.speak)
        TextView speak;
        @BindView(R.id.duration)
        TextView duration;
        @BindView(R.id.videoRoot)
        LinearLayout videoRoot;
        @BindView(R.id.mVideo)
        ImageView mVideo;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.tips)
        TextView tips;
        private IMMessage mmessage;

        public ChatViewHolder(View view) {
            super(view);
        }


        public void setIMMessage(IMMessage mmessage) {
            this.mmessage = mmessage;
        }

        @OnClick(R.id.mVideo)
        void videoPlay() {
            if (mmessage.getMsgType() == MsgTypeEnum.video)

                if (((VideoAttachment) mmessage.getAttachment()).getUrl() == null) {
                    Toast.makeText(duration.getContext(), "视频文件已损坏", Toast.LENGTH_SHORT).show();
                } else
                    mVideo.getContext().startActivity(new Intent(mVideo.getContext(), VideoPlayerActivity.class)
                            .putExtra("path",
                                    new File(CacheManager.getCachedDir(), CacheManager.splitFileName(((VideoAttachment) mmessage.getAttachment()).getUrl()) + ".mp4").getAbsolutePath()));
        }

        @OnClick(R.id.mImg)
        void watchImage() {
            mImg.getContext().startActivity(new Intent(mImg.getContext(), ImageDetailsActivity.class).putExtra("imgUrl", ((ImageAttachment) mmessage.getAttachment()).getUrl()));
        }

        @OnClick({R.id.icon_other, R.id.me})
        void iconPress(View view) {
            boolean isMe = false;
            switch (view.getId()) {
                case R.id.icon_other:
                    isMe = false;
                    break;
                case R.id.me:
                    isMe = true;
                    break;
            }
            if (listener != null) {
                listener.iconPress(isMe, mmessage.getFromAccount());
            }


        }
    }

    public void setListener(IconPressListener listener) {
        this.listener = listener;
    }

    private IconPressListener listener;

    public interface IconPressListener {
        void iconPress(boolean me, String account);
    }
}
