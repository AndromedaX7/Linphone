package org.linphone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;

import org.linphone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditorTeamNameActivity extends AppCompatActivity {

    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.finish)
    TextView finish;
    @BindView(R.id.editText)
    EditText editText;

    private Team team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_team_name);
        ButterKnife.bind(this);
        team = NIMClient.getService(TeamService.class).queryTeamBlock(getIntent().getStringExtra("team"));
        editText.setText(team.getName());

    }

    @OnClick(R.id.meBack)
    public void onMeBackClicked() {
        finish();
    }

    @OnClick(R.id.finish)
    public void onFinishClicked() {
        String teamName =editText.getText().toString();
        if (TextUtils.isEmpty(teamName)){
            return;
        }
        NIMClient.getService(TeamService.class)
                .updateName(team.getId(),teamName)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        setResult(RESULT_OK,new Intent().putExtra("name",NIMClient.getService(TeamService.class).queryTeamBlock(team.getId()).getName()));
                        finish();
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
