package org.linphone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.ImClientProxy;
import org.linphone.R;
import org.linphone.adapter.SearchAdapter;
import org.linphone.app.App;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class FindFriendActivity extends AppCompatActivity {

    @BindView(R.id.mSearchBtn)
    TextView mSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        ButterKnife.bind(this);
        initView();
    }


    private ArrayList<String> currentSearchUser = new ArrayList<>();

    @BindView(R.id.meBack)
    ImageView meBack;
    @BindView(R.id.mSearchInput)
    EditText mSearchInput;
    @BindView(R.id.mSearch)
    ListView mSearch;


    private SearchAdapter adapter;

    private String TAG = getClass().getName();

    public void initView() {
        adapter = new SearchAdapter(new ArrayList<>());
        mSearch.setAdapter(adapter);
        mSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(v.getText().toString())) {
                    currentSearchUser.clear();
                    currentSearchUser.add(v.getText().toString());
                    Log.e(TAG, "onEditorAction: " + v.getText().toString());
                    ImClientProxy.fetchUserInfo(currentSearchUser, new RequestCallback<List<NimUserInfo>>() {
                        @Override
                        public void onSuccess(List<NimUserInfo> param) {
                            if (param != null && param.size() > 0) {
                                adapter.getData().clear();
                                adapter.addData(new ArrayList<>(param));
                            }
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }

    @OnItemClick(R.id.mSearch)
    void onUserItemClick(int pos) {
        NimUserInfo item = adapter.getItem(pos);
        startActivity(new Intent(this, NewUserInfoActivity.class).putExtra("data", item));
//        openCallback.open(NewUserInfoFragment.class, item, false);
    }

    @OnClick(R.id.meBack)
    void onBack() {
        finish();
    }

    @OnClick(R.id.mSearchBtn)
    public void onViewClicked() {
        if (mSearchInput.getText().toString().equals(App.app().getLoginData().getUsername())) {
            Toast.makeText(this, "不可以添加自己为好友", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSearchUser.clear();
        currentSearchUser.add(mSearchInput.getText().toString());
        Log.e(TAG, "onEditorAction: " + mSearchInput.getText().toString());
        ImClientProxy.fetchUserInfo(currentSearchUser, new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> param) {
                if (param != null && param.size() > 0) {
                    adapter.getData().clear();
                    adapter.addData(new ArrayList<>(param));
                }
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