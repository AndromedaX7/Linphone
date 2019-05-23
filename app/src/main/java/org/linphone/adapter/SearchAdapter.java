package org.linphone.adapter;

import android.view.View;
import android.widget.TextView;

import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by miao on 2018/10/25.
 */
public class SearchAdapter extends BaseAdapter<NimUserInfo, SearchAdapter.SearchViewHolder> {


    public SearchAdapter(ArrayList<NimUserInfo> data) {
        super(data);
    }

    @Override
    protected SearchViewHolder create(View view, int itemViewType) {
        return new SearchViewHolder(view);
    }

    @Override
    protected void bindView(SearchViewHolder searchBViewHolder, int position, NimUserInfo item) {
        searchBViewHolder.mFriendName.setText(item.getName());
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_search_user;
    }

    public static class SearchViewHolder extends BaseAdapter.ViewHolder {

        @BindView(R.id.mFriendName)
        TextView mFriendName;

        public SearchViewHolder(View view) {
            super(view);
        }
    }
}
