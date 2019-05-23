package org.linphone.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.team.model.Team;

import org.linphone.R;
import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class GroupListAdapter extends BaseAdapter<Team, GroupListAdapter.Glvh> {


    public GroupListAdapter(ArrayList<Team> data) {
        super(data);
    }

    @Override
    protected Glvh create(View view, int itemViewType) {
        return new Glvh(view);
    }

    @Override
    protected void bindView(Glvh vh, int position, Team item) {
        vh.idx = position;
        vh.mFriendName.setText(item.getName());
        vh.mContent.setText("");
        vh.mContent.setVisibility(View.GONE);
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_chat_user_record;
    }

    class Glvh extends BaseAdapter.ViewHolder {
        public int idx;
        @BindView(R.id.mFriendName)
        TextView mFriendName;
        @BindView(R.id.mContent)
        TextView mContent;
        @BindView(R.id.mIcon)
        ImageView mIcon;
        @BindView(R.id.unReadCount)
        TextView unReadCount;

        public Glvh(View view) {
            super(view);
            unReadCount.setVisibility(View.GONE);
        }


        @OnClick(R.id.mIcon)
        void iconPress() {
            if (listener != null) listener.iconPress(false, data.get(idx).getId());
        }
    }


    private ChatAdapter.IconPressListener listener;

    public void setListener(ChatAdapter.IconPressListener listener) {
        this.listener = listener;
    }

}
