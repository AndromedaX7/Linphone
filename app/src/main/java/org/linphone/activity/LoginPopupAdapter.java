package org.linphone.activity;

import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import org.linphone.R;

import java.util.ArrayList;


/**
 * Created by miao on 2018/11/13.
 */
public class LoginPopupAdapter{


//        extends BaseAdapter<LoginAccountData, LoginPopupAdapter.LoginItem> {
//
//    public interface Callback {
//        void callback();
//    }
//
//    private Callback callback;
//
//    public void setCallback(Callback callback) {
//        this.callback = callback;
//    }
//
//    public LoginPopupAdapter(ArrayList<LoginAccountData> data) {
//        super(data);
//    }
//
//    @Override
//    protected LoginItem create(View view, int itemViewType) {
//        return new LoginItem(view);
//    }
//
//    @Override
//    protected void bindView(LoginItem loginItem, int position, LoginAccountData item) {
//        loginItem.mAccount.setText(item.getAccount());
//        loginItem.mDelete.setTag(position);
//    }
//
//    @Override
//    protected int getDefaultLayout() {
//        return R.layout.item_login_account;
//    }
//
//
//    class LoginItem extends BaseAdapter.ViewHolder {
//
//        @BindView(R.id.mAccount)
//        TextView mAccount;
//        @BindView(R.id.mDelete)
//        ImageView mDelete;
//
//        @OnClick(R.id.mDelete)
//        public void onViewClicked(View view) {
//
//            LoginAccountData item = getItem((Integer) view.getTag());
//            Log.e("onViewClicked: ", item.toString());
//            context.getContentResolver().delete(ConfigProvider.uri_user_list_remb, "_id=?", new String[]{String.valueOf(item.getId())});
//            data.remove((int) view.getTag());
//            notifyDataSetChanged();
//
//            if (data.size() == 0 && callback != null) {
//                callback.callback();
//            }
//        }
//
//
//        public LoginItem(View view) {
//            super(view);
//        }
//    }
}
