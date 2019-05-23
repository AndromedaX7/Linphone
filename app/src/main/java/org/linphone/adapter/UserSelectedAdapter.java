package org.linphone.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnCheckedChanged;

public class UserSelectedAdapter extends BaseAdapter<NimUserInfo, UserSelectedAdapter.UserSelectedViewHolder> {

    private ArrayList<Boolean> checkedList = new ArrayList<>();

    @Override
    protected UserSelectedViewHolder create(View view, int itemViewType) {
        return new UserSelectedViewHolder(view);
    }

    @Override
    protected void bindView(UserSelectedViewHolder vh, int position, NimUserInfo item) {
        vh.idx = position;
        vh.name.setText(item.getName());
        if (checkedList.get(position) == null) {
            vh.selected.setChecked(false);
        } else {
            vh.selected.setChecked(checkedList.get(position));
        }
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_user_select;
    }

    @Override
    public void setData(ArrayList<NimUserInfo> data) {
        super.setData(data);
        if (data != null) {
            checkedList.clear();
            for (int i = 0; i < data.size(); i++) {
                checkedList.add(false);
            }
        }
        notifyDataSetChanged();
    }


    public ArrayList<Boolean> getCheckedList() {
        return checkedList;
    }


    class UserSelectedViewHolder extends BaseAdapter.ViewHolder {

        private int idx;
        @BindView(R.id.selected)
        CheckBox selected;
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.name)
        TextView name;

        public UserSelectedViewHolder(View view) {
            super(view);
        }

        @OnCheckedChanged(R.id.selected)
        void checked(boolean checked) {
            checkedList.set(idx, checked);
            notifyDataSetChanged();
        }
    }
}
