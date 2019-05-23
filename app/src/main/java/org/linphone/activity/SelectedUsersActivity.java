package org.linphone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.adapter.UserSelectedAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectedUsersActivity extends AppCompatActivity {

    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.finish)
    TextView finish;
    @BindView(R.id.relativeLayout)
    RelativeLayout relativeLayout;
    @BindView(R.id.mList)
    ListView mList;


    private UserSelectedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_users);
        ButterKnife.bind(this);
        adapter =new UserSelectedAdapter();
        mList.setAdapter(adapter);

        List<String> friends = NIMClient.getService(FriendService.class)
                .getFriendAccounts();

        NIMClient.getService(UserService.class).fetchUserInfo(friends)
                .setCallback(new RequestCallback<List<NimUserInfo>>() {
                    @Override
                    public void onSuccess(List<NimUserInfo> param) {
                        adapter.setData(new ArrayList<>(param));
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
    }

    @OnClick(R.id.finish)
    void onFinish(){
        ArrayList<Boolean> checkedList = adapter.getCheckedList();
        ArrayList<NimUserInfo> data = adapter.getData();
        ArrayList<String> accountList =new ArrayList<>();
        for (int i = 0; i < checkedList.size(); i++) {
            if (checkedList.get(i)){
                accountList.add(data.get(i).getAccount());
            }
        }

        setResult(RESULT_OK,new Intent().putExtra("data",accountList));
        finish();
    }





}
