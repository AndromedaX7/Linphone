package org.linphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.ImClientProxy;
import org.linphone.R;
import org.linphone.adapter.ChatAdapter;
import org.linphone.app.App;
import org.linphone.cache.CacheManager;
import org.linphone.ob.ObserverManager;
import org.linphone.ob.ReceiveFriendMessageTask;
import org.linphone.utils.DialogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static org.linphone.ImClientProxy.meId;

public class ChatItemActivity extends AppCompatActivity implements ReceiveFriendMessageTask {

    private static final int audio_permission = 0x14;
    private static final int camera_video_permission = 0x11;
    private static final int camera_capture_permission = 0x12;
    private static final int sd_card_permission = 0x13;
    private static final int REQUEST_CODE_RECORD_VIDEO = 20;
    private static final int REQUEST_IMAGE_CAPTURE = 21;

    private static ChatItemActivity sCurrent = null;
    @BindView(R.id.speak)
    ImageView speak;
    @BindView(R.id.photo)
    ImageView photo;
    String TAG = "gestureDetector";
    @BindView(R.id.mIcons)
    ImageView mIcons;

    @BindView(R.id.camera)
    ImageView camera;
    @BindView(R.id.videoRecord)
    ImageView videoRecord;


    public static ChatItemActivity current() {
        return sCurrent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_item);
        ButterKnife.bind(this);
        sCurrent = this;
        initPermission();
        initData();
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.e(TAG, "onDown: ");
                recordAudio();
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.e(TAG, "onShowPress: ");
                showMaskIfNeed(true);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.e(TAG, "onSingleTapUp: ");
                endAudio(true);
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.e(TAG, "onScroll: ");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.e(TAG, "onLongPress: ");

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.e(TAG, "onFling: ");
                return false;
            }
        });
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ObserverManager.getInstance().register(RECEIVE_FRIEND_MESSAGE, this);
        CacheManager.setAdapter(mAdapter);
        onSpeakOrText();
        mAdapter.setListener(new ChatAdapter.IconPressListener() {
            @Override
            public void iconPress(boolean me, String account) {
                startActivity(new Intent(ChatItemActivity.this, UserInfoDetailsActivity.class).putExtra("id", account).putExtra("me", account.equals(App.app().getLoginData().getUsername())));
            }
        });
    }

    @OnClick(R.id.meBack)
    void onBack() {
        finish();
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT)
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        sd_card_permission);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT)
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        sd_card_permission);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sCurrent = null;
        CacheManager.clear();
        ObserverManager.getInstance().unregister(RECEIVE_FRIEND_MESSAGE, this);
        currentAccount = "";
    }

    GestureDetector gestureDetector;
    private boolean text;
    @BindView(R.id.mFriendName)
    TextView mFriendName;
    @BindView(R.id.mMessage)
    EditText mMessage;
    @BindView(R.id.mSend)
    TextView mSend;
    @BindView(R.id.chatView)
    ListView mChatView;
    @BindView(R.id.loadHistory)
    SwipeRefreshLayout loadHistory;
    @BindView(R.id.textOut)
    LinearLayout textOut;
    @BindView(R.id.speakOut)
    TextView speakOut;
    @BindView(R.id.mask)
    CardView mask;
    private InputMethodManager imm;

    private ChatAdapter mAdapter = new ChatAdapter();

    private NimUserInfo userInfo;

    private Team team;
    private static String currentAccount = "";

    private String targetId;

    public void initData() {
        getIntentData();
        if (isP2P) {
            String alias = NIMClient.getService(FriendService.class).getFriendByAccount(userInfo.getAccount()).getAlias();
            if (TextUtils.isEmpty(alias))
                mFriendName.setText(userInfo.getName());
            else
                mFriendName.setText(alias);
        } else mFriendName.setText(team.getName());
        NIMSDK.getMsgService().clearUnreadCount(targetId, isP2P ? SessionTypeEnum.P2P : SessionTypeEnum.Team);
        mAdapter.setWho(meId);
        mChatView.setAdapter(mAdapter);
        loadHistory.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChatList(true);
            }
        });

        if (!isP2P) {
            if (!team.isMyTeam()) {
                Toast.makeText(this, "您不在此群中", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(this::finish, 2000);
            }
        }

        mFriendName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isP2P)
                    startActivity(new Intent(ChatItemActivity.this, UserInfoDetailsActivity.class).putExtra("id", targetId));
                else
                    startActivity(new Intent(ChatItemActivity.this, TeamDetailsActivity.class).putExtra("team", team.getId()));


            }
        });
        currentAccount = targetId;
        mChatView.setSelection(mAdapter.getCount() - 1);
        registerForContextMenu(mChatView);
    }

    private void getChatList(boolean moveToEnd) {
        if (imMessageCache.get(targetId) == null || imMessageCache.get(targetId).size() == 0) {
            ImClientProxy.queryMessageListEx(isP2P, targetId, new RequestCallback<List<IMMessage>>() {
                @Override
                public void onSuccess(List<IMMessage> param) {
                    if (imMessageCache.get(targetId) == null) {
                        imMessageCache.put(targetId, new ArrayList<IMMessage>());
                    }
                    imMessageCache.get(targetId).addAll(param);
                    mAdapter.setData(imMessageCache.get(targetId));
                    if (moveToEnd)
                        mChatView.setSelection(mAdapter.getCount() - 1);
                    loadHistory.setRefreshing(false);
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        } else {
            ArrayList<IMMessage> imMessages = imMessageCache.get(targetId);
            ImClientProxy.queryMessageListEx(imMessages.get(0), new RequestCallback<List<IMMessage>>() {
                @Override
                public void onSuccess(List<IMMessage> param) {
                    ArrayList<IMMessage> loacl = new ArrayList<>(imMessageCache.get(targetId));
                    imMessageCache.get(targetId).clear();
                    imMessageCache.get(targetId).addAll(param);
                    imMessageCache.get(targetId).addAll(loacl);
                    mAdapter.setData(imMessageCache.get(targetId));
                    mChatView.setSelection(param.size());
                    loadHistory.setRefreshing(false);
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

    private static void sendBlackListNotifier() {
        String msg = "消息发送成功,但对方拒收了您的消息";
        Toast t = Toast.makeText(App.app().getApplicationContext(), msg, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    @OnClick(R.id.mSend)
    void onSend() {
        if (isP2P) {

            if (!NIMClient.getService(FriendService.class).isMyFriend(targetId)) {
                Toast t = Toast.makeText(this, "只有好友才能发送消息,请添加好友", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }

            checkBlackList(targetId, isP2P);

        }

        String msg = mMessage.getText().toString();

        if (TextUtils.isEmpty(msg))
            return;
        IMMessage textMessage = MessageBuilder.createTextMessage(targetId, typeEnum, msg);
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true);
        mMessage.getText().clear();

        ArrayList<IMMessage> imMessages = imMessageCache.get(targetId);
        if (imMessages == null) {
            imMessages = new ArrayList<>();
            imMessageCache.put(targetId, imMessages);
        }
        imMessages.add(textMessage);
        mAdapter.setData(imMessageCache.get(targetId));
        mChatView.setSelection(mAdapter.getCount() - 1);
        Log.e("onSend: ", targetId + "<>" + typeEnum.name());
        ObserverManager.getInstance().notifyReceiveSelfText(targetId, msg);

    }

    public static void checkBlackList(String account, boolean isP2P) {
        if (isP2P)
            if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
                NIMClient.getService(UserService.class).fetchUserInfo(Arrays.asList(account))
                        .setCallback(new RequestCallback<List<NimUserInfo>>() {
                            @Override
                            public void onSuccess(List<NimUserInfo> param) {
                                if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
                                    Friend friend = NIMClient.getService(FriendService.class).getFriendByAccount(account);
                                    Map<String, Object> extension = friend.getExtension();
                                    if (extension == null) return;
                                    Object o = extension.get("inBlackList");
                                    if (o != null) {
                                        if (o instanceof Boolean) {
                                            if ((boolean) o) {
                                                sendBlackListNotifier();
                                            }
                                        }
                                    }
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

    private boolean isP2P = true;
    public SessionTypeEnum typeEnum;

    public void getIntentData() {
        isP2P = getIntent().getBooleanExtra("isP2P", true);
        typeEnum = isP2P ? SessionTypeEnum.P2P : SessionTypeEnum.Team;
        if (isP2P) {
            userInfo = (NimUserInfo) getIntent().getSerializableExtra("data");
            targetId = userInfo.getAccount();
        } else {
            team = (Team) getIntent().getSerializableExtra("team");
            targetId = team.getId();
        }
        setHistory(targetId);
    }

    public void setHistory(String id) {
        ArrayList<IMMessage> imMessages = imMessageCache.get(id);
        if (imMessages == null) {
            imMessages = new ArrayList<>();
            imMessageCache.put(id, imMessages);
        }
        mAdapter.isTeam(!isP2P);
        mAdapter.setWho(meId);
        mAdapter.setData(imMessages);

        if (imMessages.size() == 0) {
            ImClientProxy.queryMessageListEx(isP2P, id, new RequestCallback<List<IMMessage>>() {
                @Override
                public void onSuccess(List<IMMessage> param) {
                    if (isP2P) {
                        ArrayList<IMMessage> params = new ArrayList<>(param);
                        Iterator<IMMessage> iterator = params.iterator();
                        while (iterator.hasNext()) {
                            IMMessage next = iterator.next();
                            if (next.getSessionType() != SessionTypeEnum.P2P) {
                                iterator.remove();
                            }
                        }
                        imMessageCache.get(id).addAll(params);
                    } else {
                        ArrayList<IMMessage> params = new ArrayList<>(param);
                        Iterator<IMMessage> iterator = params.iterator();
                        while (iterator.hasNext()) {
                            IMMessage next = iterator.next();
                            if (next.getSessionType() != SessionTypeEnum.Team) {
                                iterator.remove();
                            }
                        }
                        imMessageCache.get(id).addAll(params);
                    }

                    mAdapter.setData(imMessageCache.get(id));
                    mChatView.setSelection(mAdapter.getCount() - 1);
                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        } else {
            mChatView.setSelection(mAdapter.getCount() - 1);
        }
    }

    public void receiveMessage(List<IMMessage> imMessages) {
        for (IMMessage message : imMessages) {
            String fromAccount = message.getSessionId();
            ArrayList<IMMessage> accountMsg = imMessageCache.get(fromAccount);
            if (accountMsg == null) {
                accountMsg = new ArrayList<>();
                imMessageCache.put(fromAccount, accountMsg);
            }
            if (fromAccount.equals(targetId)) {
                NIMSDK.getMsgService().clearUnreadCount(targetId, isP2P ? SessionTypeEnum.P2P : SessionTypeEnum.Team);
            }

            accountMsg.add(message);
        }
        if (targetId != null)
            mAdapter.setData(imMessageCache.get(targetId));
        mChatView.setSelection(mAdapter.getCount() - 1);
    }


    public static HashMap<String, ArrayList<IMMessage>> imMessageCache = new HashMap<>();

    public static void addRecord(List<IMMessage> msg) {
        for (int i = 0; i < msg.size(); i++) {
            if (!msg.get(i).getSessionId().equals(currentAccount)) {
                ArrayList<IMMessage> imMessages = ChatItemActivity.imMessageCache.get(msg.get(i).getSessionId());
                if (imMessages == null) {
                    imMessages = new ArrayList<>();
                    ChatItemActivity.imMessageCache.put(msg.get(i).getSessionId(), imMessages);
                }
                imMessages.add(msg.get(i));
            }
        }
    }

    @Override
    public void onReceive(List<IMMessage> msg) {
        receiveMessage(msg);
    }

    @Override
    public void registerComplete() {
    }

    @Override
    public void unregisterComplete() {
        Log.e(">>>", "unregisterComplet: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isP2P) {
            team = NIMClient.getService(TeamService.class)
                    .queryTeamBlock(targetId);
            mFriendName.setText(team.getName());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isP2P && !NIMClient.getService(TeamService.class).queryTeamBlock(targetId)
                .isMyTeam()) {
            finish();
        }

        if (isP2P) {
            String alias = NIMClient.getService(FriendService.class).getFriendByAccount(userInfo.getAccount()).getAlias();
            if (TextUtils.isEmpty(alias))
                mFriendName.setText(userInfo.getName());
            else
                mFriendName.setText(alias);
        } else mFriendName.setText(team.getName());
        mAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.speak)
    void onSpeakOrText() {
        if (text) {
            textOut.setVisibility(View.INVISIBLE);
            speakOut.setVisibility(View.VISIBLE);
            speak.setImageResource(R.drawable.ic_key_board);
            imm.hideSoftInputFromWindow(mMessage.getWindowToken(), 0);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT)
                    this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                            audio_permission);
            }
        } else {
            textOut.setVisibility(View.VISIBLE);
            speakOut.setVisibility(View.INVISIBLE);
            speak.setImageResource(R.drawable.speak);
        }
        text = !text;
    }


    long pressedTime;

    @OnTouch(R.id.speakOut)
    public boolean onSpeakClicked(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case ACTION_DOWN:
                Log.e("onSpeakClicked: ", "readly");
                break;
            case ACTION_MOVE:
                break;
            case ACTION_UP:
                Log.e("onSpeakClicked: ", "complete");
                endAudio(false);
                showMaskIfNeed(false);
                break;
        }
        return true;
    }


    private void showMaskIfNeed(boolean show) {
        mask.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    AudioRecorder audioRecorder;
    private File currentFile;
    private long last;

    private void endAudio(boolean cancel) {
        if (audioRecorder != null) {
            audioRecorder.completeRecord(cancel);
        }
    }

    private void sendAudioMessage() {
        checkBlackList(targetId, isP2P);
        File file = currentFile;
        long lastDuration = last;
        last = 0;
        currentFile = null;
        String msg = "[语音]";

        if (TextUtils.isEmpty(msg))
            return;
        IMMessage textMessage = MessageBuilder.createAudioMessage(targetId, typeEnum, file, lastDuration);
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true);
        mMessage.getText().clear();

        ArrayList<IMMessage> imMessages = imMessageCache.get(targetId);
        if (imMessages == null) {
            imMessages = new ArrayList<>();
            imMessageCache.put(targetId, imMessages);
        }
        imMessages.add(textMessage);
        mAdapter.setData(imMessageCache.get(targetId));
        mChatView.setSelection(mAdapter.getCount() - 1);
        Log.e("onSend: ", targetId + "<>" + typeEnum.name());
        ObserverManager.getInstance().notifyReceiveSelfText(targetId, msg);

    }

    private long startTime = 0;

    private void recordAudio() {
        if (audioRecorder == null)
            audioRecorder = new AudioRecorder(this, RecordType.AAC, 10000, new IAudioRecordCallback() {
                @Override
                public void onRecordReady() {

                }

                @Override
                public void onRecordStart(File audioFile, RecordType recordType) {
                    currentFile = audioFile;
                    startTime = System.currentTimeMillis();
                }

                @Override
                public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
                    currentFile = audioFile;
                    last = System.currentTimeMillis() - startTime;
                    startTime = 0;
                    DialogUtils.tips(ChatItemActivity.this, (v, c) -> sendAudioMessage());
                }

                @Override
                public void onRecordFail() {
                    Toast.makeText(ChatItemActivity.this, "语音录制时间过短", Toast.LENGTH_SHORT).show();
                    startTime = 0;
                    last = 0;
                }

                @Override
                public void onRecordCancel() {
                    startTime = 0;
                }

                @Override
                public void onRecordReachedMaxTime(int maxTime) {

                }
            });
        audioRecorder.startRecord();
    }

    @OnClick(R.id.photo)
    public void onPhotoClicked() {
        startActivityForResult(new Intent(this, PhotoSelector.class).putExtra("targetId", targetId).putExtra("isP2P", isP2P), 0x01);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == RESULT_OK) {
            mAdapter.setData(imMessageCache.get(targetId));
            mChatView.setSelection(mAdapter.getCount() - 1);
        }

        try {

            if (requestCode == REQUEST_CODE_RECORD_VIDEO && resultCode == RESULT_OK && data != null) {
                checkBlackList(targetId, isP2P);
                Uri u = data.getData();
                if (u != null)
                    try {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(this, u);
                        mediaPlayer.prepare();
                        int duration = mediaPlayer.getDuration();
                        Log.e("onActivityResult: ", mp_fileParser(u.getPath()));
                        IMMessage textMessage = MessageBuilder.createVideoMessage(targetId, typeEnum, new File(mp_fileParser(u.getPath())), duration, mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight(), null);
                        NIMClient.getService(MsgService.class).sendMessage(textMessage, true)
                                .setCallback(new RequestCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void param) {
                                        Log.e("onSuccess: ", u.toString());
                                        mChatView.postDelayed(() -> mAdapter.notifyDataSetChanged(), 1500);
                                    }

                                    @Override
                                    public void onFailed(int code) {
                                        Log.e("onFailed: ", "" + code);
                                    }

                                    @Override
                                    public void onException(Throwable exception) {
                                        exception.printStackTrace();
                                    }
                                });

                        ArrayList<IMMessage> imMessages = imMessageCache.get(targetId);
                        if (imMessages == null) {
                            imMessages = new ArrayList<>();
                            imMessageCache.put(targetId, imMessages);
                        }
                        imMessages.add(textMessage);
                        mAdapter.setData(imMessageCache.get(targetId));
                        mChatView.setSelection(mAdapter.getCount() - 1);
                        Log.e("onSend: ", targetId + "<>" + typeEnum.name());
                        ObserverManager.getInstance().notifyReceiveSelfText(targetId, "[视频]");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


            } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                checkBlackList(targetId, isP2P);
                if (photoFile.exists()) {
                    IMMessage textMessage = MessageBuilder.createImageMessage(targetId, typeEnum, photoFile, null);
                    textMessage.setContent("[图片]");
                    HashMap<String, Object> fp = new HashMap<>();
                    fp.put("path", photoFile.getAbsolutePath());
                    textMessage.setRemoteExtension(fp);
                    NIMClient.getService(MsgService.class).sendMessage(textMessage, true)
                            .setCallback(new RequestCallback<Void>() {
                                @Override
                                public void onSuccess(Void param) {
                                    Log.e("onSuccess: ", photoFile.getAbsolutePath());
                                    mChatView.postDelayed(() -> mAdapter.notifyDataSetChanged(), 1500);
                                }

                                @Override
                                public void onFailed(int code) {
                                    Log.e("onFailed: ", "" + code);
                                }

                                @Override
                                public void onException(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });

                    ArrayList<IMMessage> imMessages = imMessageCache.get(targetId);
                    if (imMessages == null) {
                        imMessages = new ArrayList<>();
                        imMessageCache.put(targetId, imMessages);
                    }
                    imMessages.add(textMessage);
                    mAdapter.setData(imMessageCache.get(targetId));
                    mChatView.setSelection(mAdapter.getCount() - 1);
                    Log.e("onSend: ", targetId + "<>" + typeEnum.name());
                    ObserverManager.getInstance().notifyReceiveSelfText(targetId, "[视频]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String mp_fileParser(String path) {
        return path.replaceAll("/mp_file", getFilesDir().getAbsolutePath());
    }


    @OnClick(R.id.camera)
    void onCameraClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT)
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        camera_capture_permission);
        } else {
            takePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case camera_video_permission: {
                if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    videoRecord();
                } else {
                    Toast.makeText(this, "拍摄视频权限不足,请前往系统设置开启", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case camera_capture_permission: {
                if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    takePhoto();
                else {
                    Toast.makeText(this, "拍照权限不足,请前往系统设置开启", Toast.LENGTH_SHORT).show();
                }
            }
            break;
//            case audio_permission: {
//                if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                    Toast.makeText(this, "录音权限不足,请前往系统设置开启", Toast.LENGTH_SHORT).show();
//                }
//            }
//            break;

        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private File photoFile;

    private void takePhoto() {
        File filePath = getFilesDir();
        File root = new File(filePath, "imMp");
        root.mkdirs();
        File file = new File(root, System.currentTimeMillis() + ".png");
        photoFile = file;
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".mpProvider", file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  // 表示跳转至相机的录视频界面
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // 设置视频录制的最长时间
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void videoRecord() {
        File filePath = getFilesDir();
        File root = new File(filePath, "imMp");
        root.mkdirs();
        File file = new File(root, System.currentTimeMillis() + ".mp4");
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".mpProvider", file);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);  // 表示跳转至相机的录视频界面
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);    // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // 设置视频录制的最长时间
        startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.videoRecord)
    public void onViewClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT)
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        camera_video_permission);
        } else {
            videoRecord();
        }
    }

    @OnClick(R.id.mIcons)
    void mIconSelected() {
        startActivityForResult(new Intent(this, IconsSelector.class).putExtra("targetId", targetId).putExtra("isP2P", isP2P), 0x01);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        IMMessage itemContent = mAdapter.getItem(menuInfo.position);
        switch (item.getItemId()) {
            case 1:
//                Toast.makeText(this, "target 1 >" + menuInfo.position, Toast.LENGTH_SHORT).show();
                NIMClient.getService(MsgService.class).deleteChattingHistory(mAdapter.getItem(menuInfo.position));
                imMessageCache.get(targetId).remove(mAdapter.getItem(menuInfo.position));
                mAdapter.setData(imMessageCache.get(targetId));

                break;
            case 2:
//                Toast.makeText(this, "target 2 >" + menuInfo.position, Toast.LENGTH_SHORT).show();
                NIMClient.getService(MsgService.class).clearChattingHistory(targetId, typeEnum);
                imMessageCache.clear();
                mChatView.postDelayed(() -> {
                    getChatList(false);
                }, 500);
                break;
            case 3:
                if (itemContent.getFromAccount().equals(App.app().getLoginData().getUsername())) {
                    NIMClient.getService(MsgService.class)
                            .revokeMessage(itemContent)
                            .setCallback(
                                    new RequestCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void param) {

                                        }

                                        @Override
                                        public void onFailed(int code) {
                                            if (code == ResponseCode.RES_OVERDUE) {
                                                // 发送时间超过2分钟的消息，不能被撤回
                                                Toast.makeText(ChatItemActivity.this, "发送时间超过2分钟的消息，不能被撤回", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // 其他错误code
                                                Toast.makeText(ChatItemActivity.this, "撤回失败", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                        @Override
                                        public void onException(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    }
                            );
                    imMessageCache.clear();
                    mChatView.postDelayed(() -> {
                        getChatList(false);
                    }, 500);
                } else {
                    Toast.makeText(this, "只能撤回自己发送的消息哦~", Toast.LENGTH_SHORT).show();
                }

        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        menu.setHeaderTitle("删除记录");
        // add context menu item
        menu.add(0, 1, Menu.NONE, "删除此记录");
        menu.add(0, 2, Menu.NONE, "删除全部记录");
//        menu.add(0, 3, Menu.NONE, "撤回(试用)");
    }

    public void notifyRevoke() {
        imMessageCache.clear();
        getChatList(true);
    }

}
