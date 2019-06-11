package org.linphone.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.app.App;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserNameModifyActivity extends AppCompatActivity {

    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.finish)
    TextView finish;
    @BindView(R.id.editText)
    EditText editText;

    private NimUserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_team_name);
        ButterKnife.bind(this);
        userInfo = NIMClient.getService(UserService.class).getUserInfo(App.app().getLoginData().getUsername());
        editText.setText(userInfo.getName());

    }

    @OnClick(R.id.meBack)
    public void onMeBackClicked() {
        finish();
    }

    @OnClick(R.id.finish)
    public void onFinishClicked() {
        String teamName = editText.getText().toString();
        if (TextUtils.isEmpty(teamName)) {
            return;
        }
        HashMap<UserInfoFieldEnum, Object> map = new HashMap<>();
        map.put(UserInfoFieldEnum.Name, teamName);
        NIMClient.getService(UserService.class)
                .updateUserInfo(map)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        setResult(RESULT_OK);
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
