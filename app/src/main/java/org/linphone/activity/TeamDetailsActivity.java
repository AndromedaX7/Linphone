package org.linphone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.linphone.R;
import org.linphone.adapter.TeamUserAdapter;
import org.linphone.app.App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class TeamDetailsActivity extends AppCompatActivity {

    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.teamName)
    TextView teamName;
    @BindView(R.id.teamList)
    ListView teamList;
    @BindView(R.id.addNewItem)
    CardView addNewItem;
    @BindView(R.id.exitTeam)
    CardView exitTeam;
    @BindView(R.id.exitText)
    TextView exitText;

    private Team mTeam;
    private String teamId;
    private ArrayList<TeamMember> teamMember;

    private TeamUserAdapter adapter = new TeamUserAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_details);
        ButterKnife.bind(this);
        teamId = getIntent().getStringExtra("team");
        teamList.setAdapter(adapter);
        adapter.setCallback(new TeamUserAdapter.RemoveUserCallback() {
            @Override
            public void removed(String account) {
                NIMClient.getService(TeamService.class)
                        .removeMember(teamId, account)
                        .setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void param) {
                                Toast.makeText(TeamDetailsActivity.this, "群成员已被移除", Toast.LENGTH_SHORT).show();
                                getTeamUsers();
                            }

                            @Override
                            public void onFailed(int code) {

                            }

                            @Override
                            public void onException(Throwable exception) {

                            }
                        });
            }
        });
        NIMClient.getService(TeamService.class).searchTeam(teamId)
                .setCallback(new RequestCallback<Team>() {
                    @Override
                    public void onSuccess(Team param) {
                        mTeam = param;
                        teamName.setText(mTeam.getName());
                        adapter.setTeam(mTeam);
                        if (mTeam.getCreator().equals(App.app().getLoginData().getUsername())) {
                            exitText.setText("解散群聊");
                        } else {
                            exitText.setText("退出群聊");
                        }
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });

        getTeamUsers();
    }

    private void getTeamUsers() {
        NIMClient.getService(TeamService.class).queryMemberList(teamId)
                .setCallback(new RequestCallback<List<TeamMember>>() {
                    @Override
                    public void onSuccess(List<TeamMember> param) {
                        teamMember = new ArrayList<>(param);
                        adapter.setData(teamMember);
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
    }

    @OnItemClick(R.id.teamList)
    void onTeamItemClick(int pos) {
        startActivityForResult(new Intent(this, UserInfoDetailsActivity.class).putExtra("id", adapter.getData().get(pos).getAccount()).putExtra("me", adapter.getData().get(pos).getAccount().equals(App.app().getLoginData().getUsername())), 100);
    }

    @OnClick(R.id.meBack)
    void meBack() {
        finish();
    }

    @OnClick(R.id.teamName)
    void teamNameEditor() {
        Intent intent = new Intent(this, EditorTeamNameActivity.class).putExtra("team", teamId);
        startActivityForResult(intent, 100);
    }

    @OnClick(R.id.addNewItem)
    void addNewItem() {
        startActivityForResult(new Intent(this,SelectedUsersActivity.class),101);
    }

    @OnClick(R.id.exitTeam)
    void exitTeam() {
        if (mTeam.getCreator().equals(App.app().getLoginData().getUsername())) {
//            exitText.setText("解散群聊");
            NIMClient.getService(TeamService.class).dismissTeam(teamId)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            finish();
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
        } else {
            NIMClient.getService(TeamService.class).quitTeam(teamId)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            finish();
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
//            exitText.setText("退出群聊");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            teamName.setText(data.getStringExtra("name"));
        }else  if (requestCode == 101 && resultCode == RESULT_OK){
            NIMClient.getService(TeamService.class).addMembers(teamId,data.getStringArrayListExtra("data"))
                    .setCallback(new RequestCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> param) {
                            getTeamUsers();
                            Log.e( "onSuccess: ", Arrays.toString(param.toArray()));
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
        }
    }


}
