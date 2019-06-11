/*
ContactsListFragment.java
Copyright (C) 2015  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package org.linphone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.activity.ChatItemActivity;
import org.linphone.activity.FindFriendActivity;
import org.linphone.activity.GroupChatActivity;
import org.linphone.activity.UserInfoDetailsActivity;
import org.linphone.adapter.ExpandableAdapter;
import org.linphone.app.App;
import org.linphone.bean.NimUserInfoWrapper;
import org.linphone.ob.ObserverManager;
import org.linphone.ob.ReceiveFriendChanged;
import org.linphone.ob.ReceiveSystemMessageTask;
import org.linphone.ob.TaskListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Sylvain Berfini
 */
public class ContactsListFragment extends Fragment implements DataCallbackInterface, ReceiveSystemMessageTask ,ReceiveFriendChanged {

    private ArrayList<ArrayList<NimUserInfoWrapper>> userList = new ArrayList<>();
    //	private ArrayList<ArrayList<NimUserInfoWrapper>> userList = new ArrayList<>();
    @BindView(R.id.friendList)
    ExpandableListView friendList;
    @BindView(R.id.mAddFriend)
    ImageView mAddFriend;
    @BindView(R.id.mGroupChat)
    LinearLayout mGroupChat;
    PopupWindow mPopupWindow;
    @BindView(R.id.finish_select)
    LinearLayout finishSelect;

    @BindView(R.id.count)
    TextView count;
    @BindView(R.id.mTitle)
    TextView mTitle;
    @BindView(R.id.mRefresh)
    SwipeRefreshLayout mRefresh;
    @BindView(R.id.cancel)
    TextView cancel;

    private ExpandableAdapter adapter = new ExpandableAdapter(new ArrayList<ArrayList<NimUserInfoWrapper>>());

    private ArrayList<NimUserInfoWrapper> userFriend;

    public ContactsListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObserverManager.getInstance().register(TaskListener.RECEIVE_FRIEND_CHANGED, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unregister(TaskListener.RECEIVE_FRIEND_CHANGED, this);
    }


    public void getUserData() {
        List<String> friendAccounts = NIMClient.getService(FriendService.class).getFriendAccounts();
        Log.e("getUserData: ", Arrays.toString(friendAccounts.toArray()));
        if (friendAccounts != null && friendAccounts.size() > 0) {
            parseData(NIMClient.getService(UserService.class).getUserInfoList(friendAccounts));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e( "onHiddenChanged: ","Hidden:"+hidden );
    }




    private void parseData(List<NimUserInfo> userFriend) {
        ArrayList<NimUserInfo> cache = new ArrayList<>(userFriend);
        Iterator<NimUserInfo> iterator = cache.iterator();
        while (iterator.hasNext()){
            NimUserInfo next = iterator.next();
            if (NIMClient.getService(FriendService.class).isInBlackList(next.getAccount())){
                iterator.remove();
            }
        }


        userList.clear();
        HashMap<Character, ArrayList<NimUserInfoWrapper>> userMap = new HashMap<>();


        for (NimUserInfo ui : cache) {
            NimUserInfoWrapper uiWrapper = new NimUserInfoWrapper(ui);
            ArrayList<NimUserInfoWrapper> nimUserInfos = userMap.get(uiWrapper.getPinyinChar());
            if (nimUserInfos == null) {
                nimUserInfos = new ArrayList<>();
                userMap.put(uiWrapper.getPinyinChar(), nimUserInfos);
            }
            nimUserInfos.add(uiWrapper);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ArrayList<NimUserInfoWrapper> nimUserInfoWrappers = userMap.get((char) i);
            if (nimUserInfoWrappers != null) {
                userList.add(nimUserInfoWrappers);
            }
        }
        ArrayList<NimUserInfoWrapper> nimUserInfoWrappers = userMap.get('#');
        userList.add(nimUserInfoWrappers);
        adapter.setData(userList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserData();
                mRefresh.setRefreshing(false);
                for (int i = 0; i < adapter.getGroupCount(); i++)
                    friendList.expandGroup(i);
            }
        });
        getUserData();
        finishSelect.setVisibility(View.GONE);
        mAddFriend.setVisibility(View.VISIBLE);
        ViewHolder viewHolder = new ViewHolder(View.inflate(getContext(), R.layout.popup_window_add_friend, null));
        mPopupWindow = new PopupWindow(viewHolder.getView(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        friendList.setOnGroupClickListener((parent, v, groupPosition, id) -> true);
        adapter.setCallback(this);
        friendList.setAdapter(adapter);
        friendList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                NimUserInfoWrapper child = adapter.getChild(groupPosition, childPosition);
                startActivity(new Intent(getContext(), ChatItemActivity.class).putExtra("data", child.getUserInfo()));
                return true;
            }
        });
        for (int i = 0; i < adapter.getGroupCount(); i++)
            friendList.expandGroup(i);
//        friendList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//                if (groupPosition !=current){
//                    friendList.expandGroup(groupPosition);
//                    friendList.collapseGroup(current);
//                    current =groupPosition;
//                }
//                return true;
//            }
//        });
//        for (int i = 0; i < adapter.getGroupCount(); i++)
//            friendList.collapseGroup(i);
//        friendList.expandGroup(0);

        adapter.setListener(new ExpandableAdapter.IconClickListener() {
            @Override
            public void onIconPress(int group, int pos, NimUserInfoWrapper item) {
                showUserInfo(item);
            }
        });
    }

    private void showUserInfo(NimUserInfoWrapper item) {
        startActivityForResult(new Intent(getContext(), UserInfoDetailsActivity.class).putExtra("id", item.getAccount()).putExtra("me", item.getAccount().equals(App.app().getLoginData().getUsername())), 100);
    }

    @OnClick(R.id.mAddFriend)
    void onAddFriend() {
        if (mPopupWindow != null) {
            mPopupWindow.showAsDropDown(mAddFriend);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getUserData();
        for (int i = 0; i < adapter.getGroupCount(); i++)
            friendList.expandGroup(i);
        new Handler().postDelayed(this::getUserData, 1000);
        adapter.cleanFlag();
    }

    @OnClick(R.id.mGroupChat)
    void onGroupChatPress() {
        startActivity(new Intent(getContext(), GroupChatActivity.class));
//        openCallback.open(GroupChatFragment.class, false);
    }

    @OnClick(R.id.finish_select)
    void onSelectedData() {
        ArrayList<NimUserInfoWrapper> checkData = adapter.getCheckData();
        ArrayList<String> accountList = new ArrayList<>();
        adapter.cleanFlag();
        for (int i = 0; i < checkData.size(); i++) {
            accountList.add(checkData.get(i).getAccount());
        }

        HashMap<TeamFieldEnum, Serializable> field = new HashMap<>();
        field.put(TeamFieldEnum.Name, "群聊" + System.currentTimeMillis());
        NIMClient.getService(TeamService.class).createTeam(field, TeamTypeEnum.Normal, "", accountList)
                .setCallback(new RequestCallback<CreateTeamResult>() {
                    @Override
                    public void onSuccess(CreateTeamResult param) {
                        Toast.makeText(getContext(), "创建成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code) {
                        Log.e("onFailed: ", "code" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
        cancel.setVisibility(View.GONE);
        finishSelect.setVisibility(View.GONE);
        mAddFriend.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.my_friend);
    }

    @OnClick(R.id.cancel)
    void onCancel() {
        cancel.setVisibility(View.GONE);
        adapter.cleanFlag();
        finishSelect.setVisibility(View.GONE);
        mAddFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void callback(Bundle bundle) {
        String string = bundle.getString("data-check-count");

    }

    @Override
    public void onReceive(SystemMessage msg) {

    }

    @Override
    public void registerComplete() {

    }

    @Override
    public void unregisterComplete() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            getUserData();
            for (int i = 0; i < adapter.getGroupCount(); i++)
                friendList.expandGroup(i);
        }
    }

    @Override
    public void onReceive() {
        getUserData();
        for (int i = 0; i < adapter.getGroupCount(); i++)
            friendList.expandGroup(i);
    }

    class ViewHolder {
        @BindView(R.id.addFriend)
        LinearLayout addFriend;
        @BindView(R.id.create_chats)
        LinearLayout createChats;
        private View view;

        ViewHolder(View view) {
            this.view = view;
            ButterKnife.bind(this, view);
        }

        @OnClick({R.id.addFriend, R.id.create_chats})
        void onItemClick(View view) {
            switch (view.getId()) {
                case R.id.addFriend:
                    startActivity(new Intent(getView().getContext(), FindFriendActivity.class));
//                    openCallback.open(FindFriend.class, false);
                    break;
                case R.id.create_chats:
                    cancel.setVisibility(View.VISIBLE);
                    mAddFriend.setVisibility(View.GONE);
                    finishSelect.setVisibility(View.VISIBLE);
                    adapter.needCheck(true);
                    mTitle.setText(R.string.select_contact);
                    break;
            }
            mPopupWindow.dismiss();
        }

        public View getView() {
            return view;
        }
    }

    private int current = 0;

}
