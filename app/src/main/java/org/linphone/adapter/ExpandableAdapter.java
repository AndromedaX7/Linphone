package org.linphone.adapter;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;

import org.linphone.DataCallbackInterface;
import org.linphone.R;
import org.linphone.bean.NimUserInfoWrapper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by miao on 2018/10/25.
 */
public class ExpandableAdapter extends BaseExpandableListAdapter {

    private ArrayList<ArrayList<NimUserInfoWrapper>> data = new ArrayList<>();
    private DataCallbackInterface callback;
    private IconClickListener listener;

    public void setListener(IconClickListener listener) {
        this.listener = listener;
    }

    public ExpandableAdapter(ArrayList<ArrayList<NimUserInfoWrapper>> data) {

        if (data != null)
            this.data = data;
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (data.get(groupPosition) == null) return 0;
        return data.get(groupPosition).size();
    }

    @Override
    public ArrayList<NimUserInfoWrapper> getGroup(int groupPosition) {
        Log.e("getGroup: ", data.size() + ":");
        return data.get(groupPosition);
    }

    @Override
    public NimUserInfoWrapper getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder vh = null;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.expand_group_item, null);
            vh = new GroupViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (GroupViewHolder) convertView.getTag();
        }

        if (getGroup(groupPosition) == null) {
            data.remove(groupPosition);
//            notifyDataSetChanged();
            notifyDataSetInvalidated();
        } else
            vh.title.setText(
                    String.valueOf(getGroup(groupPosition).get(0).getPinyinChar())
            );
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ChildViewHolder vh = null;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.expand_child_item, null);
            vh = new ChildViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ChildViewHolder) convertView.getTag();
        }

        vh.checkBox.setVisibility(flag ? View.VISIBLE : View.GONE);
        vh.group = groupPosition;
        vh.pos = childPosition;
        vh.checkBox.setChecked(getChild(groupPosition, childPosition).isChecked());

        if (getChild(groupPosition, childPosition) != null) {
            String alias = NIMClient.getService(FriendService.class).getFriendByAccount(getChild(groupPosition, childPosition).getAccount()).getAlias();
            if (TextUtils.isEmpty(alias)) {
                vh.mName.setText(getChild(groupPosition, childPosition).getName());
            }else {
                vh.mName.setText(alias);
            }
        }else
        vh.mName.setText(getChild(groupPosition, childPosition).getName());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setCallback(DataCallbackInterface callback) {
        this.callback = callback;
    }

    public void setData(ArrayList<ArrayList<NimUserInfoWrapper>> userList) {
        this.data = userList;
        notifyDataSetInvalidated();
//        notifyDataSetChanged();
    }

    class GroupViewHolder {
        @BindView(R.id.tag)
        LinearLayout tag;
        @BindView(R.id.title)
        TextView title;

        GroupViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    class ChildViewHolder {
        public int group;
        public int pos;
        @BindView(R.id.mIcon)
        ImageView mIcon;
        @BindView(R.id.mName)
        TextView mName;
        @BindView(R.id.checked)
        CheckBox checkBox;

        @OnClick(R.id.mIcon)
        void iconClick() {
            if (listener != null) {
                listener.onIconPress(group, pos, data.get(group).get(pos));
            }
        }


        ChildViewHolder(View view) {
            ButterKnife.bind(this, view);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    getChild(group, pos).setChecked(isChecked);
                    if (isChecked) {
                        count++;
                    } else {
                        count--;
                    }
                    if (callback != null) {
                        bundle.clear();
                        bundle.putString("data-check-count", "(" + count + ")");
                        callback.callback(bundle);
                    }
                }
            });
        }

        private Bundle bundle = new Bundle();


    }

    private boolean flag;

    public void needCheck(boolean flag) {
        cleanFlag();
        this.flag = flag;
        count = 0;
        notifyDataSetChanged();
    }

    public ArrayList<NimUserInfoWrapper> getCheckData() {
        ArrayList<NimUserInfoWrapper> arrayList = new ArrayList<>();
        for (int i = 0; i < getGroupCount(); i++) {
            for (int j = 0; j < getChildrenCount(i); j++) {
                if (getChild(i, j).isChecked())
                    arrayList.add(getChild(i, j));
            }

        }

        return arrayList;
    }

    public void cleanFlag() {
        for (int i = 0; i < getGroupCount(); i++) {
            for (int j = 0; j < getChildrenCount(i); j++) {
                if (getChild(i, j).isChecked())
                    getChild(i, j).setChecked(false);
            }

        }
        flag = false;
        notifyDataSetChanged();
    }


    public interface IconClickListener {
        void onIconPress(int group, int pos, NimUserInfoWrapper item);
    }

    private int count;
}
