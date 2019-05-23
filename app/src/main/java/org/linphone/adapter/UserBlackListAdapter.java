package org.linphone.adapter;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;

import butterknife.BindView;
import butterknife.OnClick;

public class UserBlackListAdapter extends BaseAdapter<NimUserInfo, UserBlackListAdapter.UserBlackListViewHolder> {




    @Override
    protected UserBlackListViewHolder create(View view, int itemViewType) {
        return new UserBlackListViewHolder(view);
    }

    @Override
    protected void bindView(UserBlackListViewHolder vh, int position, NimUserInfo item) {
        vh.idx = position;
        vh.mFriendName.setText(item.getName());
        vh.remove.setVisibility(View.VISIBLE);
        vh.remove.setText("移出黑名单");


    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_team_user;
    }

    class UserBlackListViewHolder extends BaseAdapter.ViewHolder {
        public int idx;
        @BindView(R.id.mFriendName)
        TextView mFriendName;
        @BindView(R.id.remove)
        TextView remove;

        public UserBlackListViewHolder(View view) {
            super(view);
        }


        @OnClick(R.id.remove)
        void onRemovePress() {
            if (callback != null) callback.removed(data.get(idx).getAccount());
        }
    }

    private RemoveUserCallback callback;

    public void setCallback(RemoveUserCallback callback) {
        this.callback = callback;
    }

    public interface RemoveUserCallback {
        void removed(String account);
    }
}
