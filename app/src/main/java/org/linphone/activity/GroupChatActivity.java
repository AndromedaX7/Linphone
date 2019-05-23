package org.linphone.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;

import org.linphone.R;
import org.linphone.adapter.ChatAdapter;
import org.linphone.adapter.GroupListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class GroupChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        ButterKnife.bind(this);
        initViews();
    }


    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.groupList)
    ListView groupList;

    private GroupListAdapter adapter = new GroupListAdapter(new
            ArrayList<>());

    public void initViews() {
        groupList.setAdapter(adapter);
        Log.e(TAG, "initViews: " + NIMClient.getService(TeamService.class).queryTeamCountBlock());
        NIMClient.getService(TeamService.class)
                .queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> param) {
                adapter.setData(new ArrayList<>(param));
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
        adapter.setListener(new ChatAdapter.IconPressListener() {
            @Override
            public void iconPress(boolean me, String account) {
                groupDetails(account);
            }
        });
    }

    @OnClick(R.id.meBack)
    void onBack() {
        finish();
    }

    @OnItemClick(R.id.groupList)
    void onGroupClick(int pos) {
        Team item = adapter.getItem(pos);
        startActivity(new Intent(this, ChatItemActivity.class).putExtra("isP2P", false).putExtra("team", item));
    }


    private void groupDetails(String tid) {
        startActivity(new Intent(this, TeamDetailsActivity.class).putExtra("team", tid));
    }

    @Override
    protected void onStart() {
        super.onStart();
        NIMClient.getService(TeamService.class)
                .queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> param) {
                adapter.setData(new ArrayList<>(param));
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

    private String TAG = getClass().getName();
}
