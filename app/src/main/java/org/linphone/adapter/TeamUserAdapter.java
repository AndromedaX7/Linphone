package org.linphone.adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.app.App;

import butterknife.BindView;
import butterknife.OnClick;

public class TeamUserAdapter extends BaseAdapter<TeamMember, TeamUserAdapter.TeamUserViewHolder> {


    private Team team;

    public void setTeam(Team team) {
        this.team = team;
        notifyDataSetChanged();
        Log.e("bindView: ", team.getCreator());
    }

    @Override
    protected TeamUserViewHolder create(View view, int itemViewType) {
        return new TeamUserViewHolder(view);
    }

    @Override
    protected void bindView(TeamUserViewHolder vh, int position, TeamMember item) {
        NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(item.getAccount());
        vh.idx = position;
        vh.mFriendName.setText(userInfo.getName());
        Glide.with(vh.mIcon).load(userInfo.getAvatar()).error(R.mipmap.ic_launcher2).placeholder(R.mipmap.ic_launcher2).into(vh.mIcon);
        vh.remove.setVisibility(!item.getAccount().equals(App.app().getLoginData().getUsername()) ? View.VISIBLE : View.GONE);
        if (team != null)
            vh.remove.setVisibility(team.getCreator().equals(App.app().getLoginData().getUsername()) ? View.VISIBLE : View.GONE);
        if (item.getAccount().equals(App.app().getLoginData().getUsername())){
            vh.remove.setVisibility( View.GONE);
        }


    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_team_user;
    }

    class TeamUserViewHolder extends BaseAdapter.ViewHolder {
        public int idx;
        @BindView(R.id.mFriendName)
        TextView mFriendName;
        @BindView(R.id.remove)
        TextView remove;
        @BindView(R.id.mIcon)
        ImageView mIcon;

        public TeamUserViewHolder(View view) {
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
