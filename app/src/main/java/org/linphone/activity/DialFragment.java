package org.linphone.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.linphone.CallActivity;
import org.linphone.CallIncomingActivity;
import org.linphone.CallOutgoingActivity;
import org.linphone.DialerFragment;
import org.linphone.FragmentsAvailable;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;
import org.linphone.R;
import org.linphone.RunningService;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.Reason;
import org.linphone.ui.AddressText;
import org.linphone.ui.CallButton;
import org.linphone.utils.DialogCustomUtils;
import org.linphone.utils.MyCookie;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 * Created by miao on 2018/10/18.
 */
public class DialFragment extends LazyLoadFragment implements View.OnClickListener {


    ImageView callExit;
    FrameLayout mDialCall;

    LinearLayout n1;
    LinearLayout n2;
    LinearLayout n3;
    LinearLayout n4;
    LinearLayout n5;
    LinearLayout n6;
    LinearLayout n7;
    LinearLayout n8;
    LinearLayout n9;
    LinearLayout n10;
    LinearLayout n0;
    LinearLayout n11;
    AddressText number;
    ImageView mDialDelete;
    TextView mDialSettings;
    TextView mOutPutNum;
    TextView mOutPutWhere;
    TextView mReplace;
    Button btnURL;
    EditText etUrl;
    private String TAG = DialFragment.class.getSimpleName();
    private Messenger server;
    private Messenger client;
    PopupWindow mPopupWindow;
    private StringBuffer numPtr = new StringBuffer();
    Pattern comp = Pattern.compile("^[0][1|2][0-9]{1,10}");
    Pattern comp2 = Pattern.compile("^[0][3-9]{1,7}[0-9]{1,10}[0-9]{1,10}");
    private String lastMode;
    private String account;

    private AlertDialog inputCustomDialog;
    private static Session session;
    private static ChannelShell channelSell;
    private static List<String> commands;
    private boolean flag;
    private OrientationEventListener mOrientationHelper;
    private LinphoneCoreListenerBase mListener;
    @Override
    protected int setContentView() {
        return R.layout.fragment_dial_pad;
    }
    private boolean newProxyConfig;
    private boolean emptyFragment = false;
    private boolean isTrialAccount = false;
    private int mAlwaysChangingPhoneAngle = -1;
    private static final int CALL_ACTIVITY = 19;
    private boolean callTransfer = false;

    @Override
    protected void initView() {
        initdata();
        //data();
    }

    protected void initdata() {

        n1 = findViewById(R.id.n1);
        n2 = findViewById(R.id.n2);
        n3 = findViewById(R.id.n3);
        n4 = findViewById(R.id.n4);
        n5 = findViewById(R.id.n5);
        n6 = findViewById(R.id.n6);
        n7 = findViewById(R.id.n7);
        n8 = findViewById(R.id.n8);
        n9 = findViewById(R.id.n9);
        n10 = findViewById(R.id.n10);
        n0 = findViewById(R.id.n0);
        n11 = findViewById(R.id.n11);
        number = findViewById(R.id.number);
        callExit = findViewById(R.id.call_exit);
        mDialCall = findViewById(R.id.mDialCall);
        mDialDelete = findViewById(R.id.mDialDelete);
        mDialSettings = findViewById(R.id.mDialSettings);
        mOutPutNum = findViewById(R.id.mOutPutNum);
        mOutPutWhere = findViewById(R.id.mOutPutWhere);
        mReplace = findViewById(R.id.mReplace);
        etUrl = findViewById(R.id.etUrl);
        etUrl.setSelection(etUrl.getText().length());
        btnURL = findViewById(R.id.btnURL);
        commands = new ArrayList<String>();
        commands.add("export DISPLAY=:0 ");
        String url = etUrl.getText().toString();
        commands.add("config modify subscriber dn 4089 longdn " + url);
        new AsyncSession().execute();
        showProgressDialog(true);
        flag = true;
        setListener();
        mListener = new LinphoneCoreListenerBase() {
            @Override
            public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneChatMessage message) {
                displayMissedChats(getUnreadMessageCount());
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig proxy, LinphoneCore.RegistrationState state, String smessage) {
                if (state.equals(LinphoneCore.RegistrationState.RegistrationCleared)) {
                    if (lc != null) {
                        LinphoneAuthInfo authInfo = lc.findAuthInfo(proxy.getIdentity(), proxy.getRealm(), proxy.getDomain());
                        if (authInfo != null)
                            lc.removeAuthInfo(authInfo);
                    }
                }

                refreshAccounts();

                if (getResources().getBoolean(R.bool.use_phone_number_validation)) {
                    if (state.equals(LinphoneCore.RegistrationState.RegistrationOk)) {
                        LinphoneManager.getInstance().isAccountWithAlias();
                    }
                }

                if (state.equals(LinphoneCore.RegistrationState.RegistrationFailed) && newProxyConfig) {
                    newProxyConfig = false;
                    if (proxy.getError() == Reason.BadCredentials) {
                        //displayCustomToast(getString(R.string.error_bad_credentials), Toast.LENGTH_LONG);
                    }
                    if (proxy.getError() == Reason.Unauthorized) {
                      //  showToast(getString(R.string.error_unauthorized));
                    }
                    if (proxy.getError() == Reason.IOError) {
                      //  showToast(getString(R.string.error_io_error));
                    }
                }
            }

            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {

//                Intent intent = new Intent(getActivity(), InComingActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("OP_FLAG", 0);
//                intent.putExtra("PHONE", numPtr.toString());
//                startActivity(intent);
                if (state == LinphoneCall.State.IncomingReceived) {
                    getActivity().startActivity(new Intent(getActivity(), CallIncomingActivity.class));
                } else if (state == LinphoneCall.State.OutgoingInit || state == LinphoneCall.State.OutgoingProgress) {
                    getActivity().startActivity(new Intent(getActivity(), CallOutgoingActivity.class));
                } else if (state == LinphoneCall.State.CallEnd || state == LinphoneCall.State.Error || state == LinphoneCall.State.CallReleased) {
                    resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
                }

                int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
                displayMissedCalls(missedCalls);
            }
        };
        int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
        displayMissedCalls(missedCalls);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
                break;
        }

        LinphoneManager.getLc().setDeviceRotation(rotation);
        mAlwaysChangingPhoneAngle = rotation;
//        TSSipPhone.addCallback(null, new PhoneCallback() {
//            @Override
//            public void On_SIPincomingCall(LinphoneCall linphoneCall) {
//                super.On_SIPincomingCall(linphoneCall);
//                // 开启铃声免提
//                TSSipPhone.toggleSpeaker(true);
//
//            }
//
//            @Override
//            public void On_SipoutgoingInit() {
//                super.On_SipoutgoingInit();
//
//            }
//
//            @Override
//            public void On_SipcallConnected() {
//                super.On_SipcallConnected();
//                // 视频通话默认免提，语音通话默认非免提
//                TSSipPhone.toggleSpeaker(TSSipPhone.getVideoEnabled());
//                // 所有通话默认非静音
//                TSSipPhone.toggleMicro(false);
//
//            }
//
//            @Override
//            public void On_SipcallEnd() {
//                super.On_SipcallEnd();
//
//            }
//        });

    }
    public void displayMissedCalls(final int missedCallsCount) {
//        if (missedCallsCount > 0) {
//            missedCalls.setText(missedCallsCount + "");
//            missedCalls.setVisibility(View.VISIBLE);
//        } else {
//            LinphoneManager.getLc().resetMissedCallsCount();
//            missedCalls.clearAnimation();
//            missedCalls.setVisibility(View.GONE);
//        }
    }
    public void refreshAccounts() {
//        if (LinphoneManager.getLc().getProxyConfigList().length > 1) {
//            accountsList.setVisibility(View.VISIBLE);
//            accountsList.setAdapter(new LinphoneActivity.AccountsListAdapter());
//            accountsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    if (view != null) {
//                        int position = Integer.parseInt(view.getTag().toString());
//                        LinphoneActivity.instance().displayAccountSettings(position);
//                    }
//                    openOrCloseSideMenu(false);
//                }
//            });
//        } else {
//            accountsList.setVisibility(View.GONE);
//        }
//        displayMainAccount();
    }
    private class LocalOrientationEventListener extends OrientationEventListener {
        public LocalOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(final int o) {
            if (o == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            int degrees = 270;
            if (o < 45 || o > 315)
                degrees = 0;
            else if (o < 135)
                degrees = 90;
            else if (o < 225)
                degrees = 180;

            if (mAlwaysChangingPhoneAngle == degrees) {
                return;
            }
            mAlwaysChangingPhoneAngle = degrees;

            org.linphone.mediastream.Log.d("Phone orientation changed to ", degrees);
            int rotation = (360 - degrees) % 360;
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (lc != null) {
                lc.setDeviceRotation(rotation);
                LinphoneCall currentCall = lc.getCurrentCall();
                if (currentCall != null && currentCall.cameraEnabled() && currentCall.getCurrentParamsCopy().getVideoEnabled()) {
                    lc.updateCall(currentCall, null);
                }
            }
        }
    }
    public void resetClassicMenuLayoutAndGoBackToCallIfStillRunning() {
//        DialerFragment dialerFragment = DialerFragment.instance();
//        if (dialerFragment != null) {
//            ((DialerFragment) dialerFragment).resetLayout(true);
//        }

        if (LinphoneManager.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
            LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
            if (call.getState() == LinphoneCall.State.IncomingReceived) {
                startActivity(new Intent(getActivity(), CallIncomingActivity.class));
            } else {
                startIncallActivity(call);
            }
        }
    }
    public void startIncallActivity(LinphoneCall currentCall) {
        Intent intent = new Intent(getActivity(), CallActivity.class);
        startOrientationSensor();
        startActivityForResult(intent, CALL_ACTIVITY);
    }
    private synchronized void startOrientationSensor() {
        if (mOrientationHelper == null) {
            mOrientationHelper = new LocalOrientationEventListener(getActivity());
        }
        mOrientationHelper.enable();
    }
    public int getUnreadMessageCount() {
        int count = 0;
        LinphoneChatRoom[] chats = LinphoneManager.getLc().getChatRooms();
        for (LinphoneChatRoom chatroom : chats) {
            count += chatroom.getUnreadMessagesCount();
        }
        return count;
    }
    private void displayMissedChats(final int missedChatCount) {
//        if (missedChatCount > 0) {
//            missedChats.setText(missedChatCount + "");
//            missedChats.setVisibility(View.VISIBLE);
//        } else {
//            missedChats.clearAnimation();
//            missedChats.setVisibility(View.GONE);
        }

    @Override
    protected void lazyLoad() {

    }

    class AsyncSession extends AsyncTask<Void, String, ChannelShell> {

        private Session getSession() {
            if (session == null || !session.isConnected()) {
                android.util.Log.i(TAG, "begin creat session");
                session = connect("219.149.195.145", "admin", "Change_Me");
                return session;
            }
            android.util.Log.i(TAG, "session exist");
            return session;
        }

        private Session connect(String hostname, String username, String password) {

            JSch jSch = new JSch();

            try {

                session = jSch.getSession(username, hostname, 5022);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);


                android.util.Log.i(TAG, "begin connect");
                session.connect();
                android.util.Log.i(TAG, "connected");
                android.util.Log.i(TAG, "status session: " + session.isConnected() + " || " + session.hashCode());
            } catch (Exception e) {
                android.util.Log.e(TAG, "An error occurred while connecting to " + hostname + ": " + e);
            }
            android.util.Log.i(TAG, "server Info: getServerVersion =" + session.getServerVersion());
            android.util.Log.i(TAG, "server Info: getServerAliveCountMax =" + session.getServerAliveCountMax());
            return session;

        }

        private Channel getChannel() throws JSchException {

            if (channelSell == null || !channelSell.isConnected()) {
                try {
                    channelSell = (ChannelShell) getSession().openChannel("shell");
                    android.util.Log.i(TAG, "begin connect channel");

                    channelSell.connect();
                    android.util.Log.i(TAG, "channel Info: getID =" + channelSell.getId());

                    android.util.Log.i(TAG, "connected channel");
                    android.util.Log.i(TAG, "status channel: " + channelSell.isConnected() + " || " + channelSell.hashCode());

                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error while opening channelSell: " + e);

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                        }

                    });
                }
            }
            return channelSell;
        }

        private void executeCommands(List<String> commands) {

            try {
                Channel channel = getChannel();
                android.util.Log.i(TAG, "Sending commands...");
                sendCommands(channel, commands);

                readChannelOutput(channel);
                android.util.Log.i(TAG, "Finished sending commands! ");

            } catch (Exception e) {
                android.util.Log.i(TAG, "An error ocurred during executeCommands: " + e);
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                    }

                });
            }
        }

        private void sendCommands(Channel channel, List<String> commands) {

            try {
                PrintStream out = new PrintStream(channel.getOutputStream());
                android.util.Log.i(TAG, "send command in chanel " + channel.hashCode());
                out.println("#!/bin/bash");
                for (String command : commands) {
                    out.println(command);
                }
                out.println("exit");

                out.flush();
                out.close();


            } catch (Exception e) {
                android.util.Log.e(TAG, "Error while sending commands: " + e);
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                    }

                });
            }

        }

        private void readChannelOutput(Channel channel) {

            byte[] buffer = new byte[1024];

            String line = "";
            try {
                InputStream in = channel.getInputStream();
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(buffer, 0, 1024);
                        if (i < 0) {
                            break;
                        }
                        line = new String(buffer, 0, i);
                        android.util.Log.w(TAG, " line " + line);
                    }

                    if (line.contains("logout")) {
                        break;
                    }

                    if (channel.isClosed()) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee) {
                    }
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error while reading channel output: " + e);
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                    }

                });
            }
        }

        public void close() {
            channelSell.disconnect();
            // session.disconnect();
            System.out.println("Disconnected channel and session");
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getActivity(), "设置成功，请拨打电话", Toast.LENGTH_LONG).show();
                    showProgressDialog(false);
                }

            });
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);
        }

        @Override
        protected ChannelShell doInBackground(Void... params) {
            executeCommands(commands);
            close();
            return null;
        }

    }

    private void getRandomNumber(String url) {
        String account = MyCookie.getString("account", "4089");
        commands.add("config modify subscriber dn " + account + " longdn " + url);
        new AsyncSession().execute();

//        DialogCustomUtils.randomNumDialog(getContext(), tel,
//                (v, v2) -> {
//                    requestCustomNumber(tel, 2);
//                    mDialSettings.setTag(2);
//                    mDialSettings.setText("随机号码");
//                    progressDialog = DialogCustomUtils.progressDialog(getContext());
//                    progressDialog.show();
//                    mPopupWindow.dismiss();
//                }, (v, vv) -> cancel(),
//                (v, v2) -> requestRandomNumber()).show();
    }

    private void setListener() {
        btnURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = etUrl.getText().toString();
                if (!url.isEmpty()) {
//                    commands.add("gnome-open "+url);
                    commands = new ArrayList<String>();
                    commands.add("export DISPLAY=:0 ");
                    commands.add("config modify subscriber dn 4089 longdn " + url);
                    new AsyncSession().execute();
                    showProgressDialog(true);
                    flag = true;

                }
            }
        });
        mReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (int) mDialSettings.getTag();
                lastMode = mDialSettings.getText().toString();
                switch (tag) {
//                case 1:
                    case 2:
                        requestRandomNumber();
                        break;
                    case 3:
                        setCustomNumber();
                        break;
//                case 4:
                }
            }
        });
        callExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (inputCustomDialog == null)
                    inputCustomDialog = DialogCustomUtils.exitDialog(getActivity(), () -> {
//                        getContext().startService(new Intent( getActivity(),RunningService.class).putExtra("launch",false));
//                        getContext().stopService(new Intent( getActivity(),RunningService.class).putExtra("launch",false));
                        getActivity().finish();
                    });
                inputCustomDialog.show();

            }
        });
        number.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        mDialCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (mDialSettings.getText().equals("未设置")) {
//                    Toast.makeText(getContext(), "请设置主叫号码", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                Log.e("onCallPress: ", numPtr.toString());
                if (TextUtils.isEmpty(numPtr.toString())) {
                    Toast.makeText(getContext(), "请输入号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (flag) {
                    //TSSipPhone.callTo(numPtr.toString(), false);


                    try {
                        if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
                            if (number.getText().length() > 0) {
                                LinphoneManager.getInstance().newOutgoingCall(number);
                            } else {
                                if (LinphonePreferences.instance().isBisFeatureEnabled()) {
                                    LinphoneCallLog[] logs = LinphoneManager.getLc().getCallLogs();
                                    LinphoneCallLog log = null;
                                    for (LinphoneCallLog l : logs) {
                                        if (l.getDirection() == CallDirection.Outgoing) {
                                            log = l;
                                            break;
                                        }
                                    }
                                    if (log == null) {
                                        return;
                                    }

                                    LinphoneProxyConfig lpc = LinphoneManager.getLc().getDefaultProxyConfig();
                                    if (lpc != null && log.getTo().getDomain().equals(lpc.getDomain())) {
                                        number.setText(log.getTo().getUserName());
                                    } else {
                                        number.setText(log.getTo().asStringUriOnly());
                                    }
                                    number.setSelection(number.getText().toString().length());
                                     number.setDisplayedName(log.getTo().getDisplayName());
                                }
                            }
                        }
                    } catch (LinphoneCoreException e) {
                        LinphoneManager.getInstance().terminateCall();
                        showToast("呼叫失败");
                    }
                } else {
                    showToast("请先设置当前号码");
                }
            }
        });

        mDialSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.showAsDropDown(mDialSettings);
            }
        });
        n0.setOnClickListener(this);
        n1.setOnClickListener(this);
        n2.setOnClickListener(this);
        n3.setOnClickListener(this);
        n4.setOnClickListener(this);
        n5.setOnClickListener(this);
        n6.setOnClickListener(this);
        n7.setOnClickListener(this);
        n8.setOnClickListener(this);
        n9.setOnClickListener(this);
        n10.setOnClickListener(this);
        n11.setOnClickListener(this);

        mDialDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numPtr.length() > 0) {
                    String phone = numPtr.delete(numPtr.length() - 1, numPtr.length()).toString();
                    int length = phone.length();
                    int times = length / 4;
                    int mod = length % 4;
                    int scount = 0;

                    StringBuilder sb = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    boolean matches = comp.matcher(phone).matches();
                    boolean matches1 = comp2.matcher(phone).matches();
                    if (matches || matches1) {
                        if (matches) {
                            int lenM = length - 3;
                            int modM = lenM % 4;
                            int timesM = lenM / 4;

                            sb.append("(\\w{3})");
                            scount++;
                            if (modM != 0) {
                                sb.append("(\\w{").append(modM).append("})");
                                scount++;
                            }
                            for (int i = 0; i < timesM; i++) {
                                sb.append("(\\w{4})");
                                scount++;
                            }

                        } else {
                            int lenM = length - 4;
                            int modM = lenM % 4;
                            int timesM = lenM / 4;

                            sb.append("(\\w{4})");
                            scount++;
                            if (modM != 0) {
                                sb.append("(\\w{").append(modM).append("})");
                                scount++;
                            }
                            for (int i = 0; i < timesM; i++) {
                                sb.append("(\\w{4})");
                                scount++;
                            }
                        }

                    } else {
                        if (mod != 0) {
                            sb.append("(\\w{").append(mod).append("})");
                            scount++;
                        }

                        for (int i = 0; i < times; i++) {
                            sb.append("(\\w{4})");
                            scount++;
                        }
                    }
                    for (int i = 1; i <= scount; i++) {
                        if (i == 1 && (matches || matches1)) {
                            sb2.append("(");
                        }
                        sb2.append("$").append(i);
                        if (i == 1 && (matches || matches1)) {
                            sb2.append(")");
                        }

                        if (i != scount)
                            sb2.append("-");
                    }


                    number.setText(phone.replaceAll(sb.toString(), sb2.toString()));
                    number.setSelection(number.length());
                    Log.e("onDeletePress: ", number.getText().toString());
                }
            }
        });
        mDialDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                numPtr.delete(0, numPtr.length());
                number.setText("");
                return true;
            }
        });

    }


    @Override
    public void onClick(View view) {
        if (numPtr.length() > 11)
            return;

        switch (view.getId()) {
            case R.id.n0:
                numPtr.append("0");
                break;
            case R.id.n1:
                numPtr.append("1");
                break;
            case R.id.n2:
                numPtr.append("2");
                break;
            case R.id.n3:
                numPtr.append("3");
                break;
            case R.id.n4:
                numPtr.append("4");
                break;
            case R.id.n5:
                numPtr.append("5");
                break;
            case R.id.n6:
                numPtr.append("6");
                break;
            case R.id.n7:
                numPtr.append("7");
                break;
            case R.id.n8:
                numPtr.append("8");
                break;
            case R.id.n9:
                numPtr.append("9");
                break;
            case R.id.n10:
                numPtr.append("*");
                break;
            case R.id.n11:
                numPtr.append("#");
                break;
        }

        String phone = numPtr.toString();
        int length = phone.length();
        int times = length / 4;
        int mod = length % 4;
        int scount = 0;

        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        boolean matches = comp.matcher(phone).matches();
        boolean matches1 = comp2.matcher(phone).matches();
        if (matches || matches1) {
            if (matches) {
                int lenM = length - 3;
                int modM = lenM % 4;
                int timesM = lenM / 4;

                sb.append("(\\w{3})");
                scount++;
                if (modM != 0) {
                    sb.append("(\\w{").append(modM).append("})");
                    scount++;
                }
                for (int i = 0; i < timesM; i++) {
                    sb.append("(\\w{4})");
                    scount++;
                }

            } else {
                int lenM = length - 4;
                int modM = lenM % 4;
                int timesM = lenM / 4;

                sb.append("(\\w{4})");
                scount++;
                if (modM != 0) {
                    sb.append("(\\w{").append(modM).append("})");
                    scount++;
                }
                for (int i = 0; i < timesM; i++) {
                    sb.append("(\\w{4})");
                    scount++;
                }
            }

        } else {
            if (mod != 0) {
                sb.append("(\\w{").append(mod).append("})");
                scount++;
            }

            for (int i = 0; i < times; i++) {
                sb.append("(\\w{4})");
                scount++;
            }
        }
        for (int i = 1; i <= scount; i++) {
            if (i == 1 && (matches || matches1)) {
                sb2.append("(");
            }
            sb2.append("$").append(i);
            if (i == 1 && (matches || matches1)) {
                sb2.append(")");
            }

            if (i != scount)
                sb2.append("-");
        }

        Log.e("onNumberPress: ", sb.toString() + "----" + sb2
                .toString());
        number.setText(phone.replaceAll(sb.toString(), sb2.toString()));
        number.setSelection(number.length());
        Log.e("input: ", number.getText().toString() + "--->" + matches + matches1);

    }


    /**
     * config modify subscriber dn 4001 longdn 84700123。其中dn 4001为客户登陆时所使用的账号，longdn 84700123为客户在设置界面中指定的号段内随机生成的号码
     */
    private void requestRandomNumber() {
        Message msg = Message.obtain();
        msg.what = 4;
        msg.replyTo = client;
        try {
            server.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * config modify subscriber dn 4001 longdn 4001
     * dn 4001为客户登陆时所使用的账号，longdn 4001为客户登陆时所使用的账号
     */
    private void requestUnitNumber() {
        Bundle bundle = new Bundle();
        bundle.putString("dn", account);
        bundle.putString("longdn", account);
        Message msg = Message.obtain();
        msg.what = 5;
        msg.replyTo = client;
        msg.setData(bundle);
        try {
            server.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * config modify subscriber dn 4001 longdn 13912345678。其中dn 4001为客户登陆时所使用的账号，longdn 13912345678为客户指定的主叫号码
     *
     * @param code
     */
    private void requestCustomNumber(String code, int tag) {
        Bundle bundle = new Bundle();
        bundle.putString("dn", account);
        bundle.putString("longdn", code);
        bundle.putInt("sTag", tag);
        Message msg = Message.obtain();
        msg.what = 5;
        msg.replyTo = client;
        msg.setData(bundle);
        try {
            server.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setCustomNumber() {
        if (inputCustomDialog == null)
            inputCustomDialog = DialogCustomUtils.inputDialog(getContext(), (s) -> {
                if (progressDialog == null)
                    progressDialog = DialogCustomUtils.progressDialog(getContext());
                progressDialog.show();
                requestCustomNumber(s, 3);
            }, this::cancel);
        inputCustomDialog.show();
    }

    private void requestUndefined() {
        mOutPutNum.setText("");
        mReplace.setVisibility(View.GONE);
        mOutPutWhere.setText("");
        saveState(0, "");
    }

    private void cancel() {
        if (TextUtils.isEmpty(lastMode))
            return;
        mDialSettings.setText(lastMode);


        switch (mDialSettings.getText().toString()) {

            case "未设置":
                mDialSettings.setTag(0);
                mReplace.setVisibility(View.GONE);
                break;
            case "随机号码":
                mDialSettings.setTag(2);
                mReplace.setVisibility(View.VISIBLE);
                break;
            case "自定义":
                mDialSettings.setTag(3);
                mReplace.setVisibility(View.VISIBLE);
                break;
            case "集团号码":
                mDialSettings.setTag(4);
                mReplace.setVisibility(View.GONE);
                break;
        }


    }


    private void saveState(int tag, String tel) {

//        ContentValues contentValues = new ContentValues();
//        contentValues.put("account", App.app().getAccountData().getAccount());
//        contentValues.put("tag", tag);
//        contentValues.put("phone", tel);
//        if (getContext().getContentResolver().update(ConfigProvider.uri_account_phone, contentValues, "account=?", new String[]{
//                App.app().getAccountData().getAccount()
//        }) == 0) {
//            getContext().getContentResolver().insert(ConfigProvider.uri_account_phone, contentValues);
    }

    private void getCallPermission(String power) {
        String[] permission = power.split(",");
        if (permission.length == 0) {
            getCodeToShowCallItem((power));
        } else {
            getCodeToShowCallItem((permission));
        }
        Log.e("getCallPermission: ", power);
    }

    private void getCodeToShowCallItem(String... i) {
//        for (String s : i) {
//            switch (s) {
//                case "1":
//                    mUnSetting.setVisibility(View.VISIBLE);
//                    break;
//                case "2":
//                    mRandomNumber.setVisibility(View.VISIBLE);
//                    break;
//                case "3":
//                    mCustomNumber.setVisibility(View.VISIBLE);
//                    break;
//                case "4":
//                    unitNumber.setVisibility(View.VISIBLE);
//                    break;
//            }
//        }
    }


    public void data() {

        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.popup_dial_setting, null);
        mPopupWindow = new PopupWindow(inflate, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        String phone = "";
        int tag = 0;
        switch (tag) {
            case 0:
                mDialSettings.setText("未设置");
                mOutPutNum.setText("");
                break;
            case 2:
                mDialSettings.setText("随机号码");
                mOutPutNum.setText(phone);
                break;
            case 3:
                mDialSettings.setText("自定义");
                mOutPutNum.setText(phone);
                break;
            case 4:
                mDialSettings.setText("集团号码");
                mOutPutNum.setText(phone);
                break;
        }

        TextView mUnSetting = inflate.findViewById(R.id.mUnSetting);
        TextView mRandomNumber = inflate.findViewById(R.id.mRandomNumber);
        TextView mCustomNumber = inflate.findViewById(R.id.mCustomNumber);
        TextView unitNumber = inflate.findViewById(R.id.unitNumber);
        mUnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialSettings.setText(R.string.un_settings);
                requestUndefined();

                mPopupWindow.dismiss();
            }
        });
        mRandomNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastMode = mDialSettings.getText().toString();
                requestRandomNumber();
                mDialSettings.setText(R.string.random_number);
                mPopupWindow.dismiss();
            }
        });
        mCustomNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastMode = mDialSettings.getText().toString();
                mDialSettings.setText(R.string.custom);
                setCustomNumber();
                mPopupWindow.dismiss();
            }
        });
        unitNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastMode = mDialSettings.getText().toString();
                mDialSettings.setTag(4);
                mDialSettings.setText(R.string.unit_number);
                if (progressDialog == null) {
                    progressDialog = DialogCustomUtils.progressDialog(getContext());
                }
                progressDialog.show();
                requestUnitNumber();
                mPopupWindow.dismiss();
            }
        });
        mDialSettings.setTag(tag);
        lastMode = mDialSettings.getText().toString();
        mReplace.setVisibility(tag == 2 || tag == 3 ? View.VISIBLE : View.GONE);


    }


    @Override
    public void onResume() {
        super.onResume();

        if (!LinphoneService.isReady()) {
            getActivity().startService(new Intent(Intent.ACTION_MAIN).setClass(getActivity(), LinphoneService.class));
        }

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }

//        if (isTablet()) {
//            // Prevent fragmentContainer2 to be visible when rotating the device
//            LinearLayout ll = (LinearLayout) findViewById(R.id.fragmentContainer2);
//            if (currentFragment == FragmentsAvailable.DIALER
//                    || currentFragment == FragmentsAvailable.ABOUT
//                    || currentFragment == FragmentsAvailable.SETTINGS
//                    || currentFragment == FragmentsAvailable.ACCOUNT_SETTINGS) {
//                ll.setVisibility(View.GONE);
//            }
//        }

//        refreshAccounts();
//
//        if (getResources().getBoolean(R.bool.enable_in_app_purchase)) {
//            isTrialAccount();
//        }

      //  updateMissedChatCount();
        if (LinphonePreferences.instance().isFriendlistsubscriptionEnabled() && LinphoneManager.getLc().getDefaultProxyConfig() != null) {
            LinphoneManager.getInstance().subscribeFriendList(true);
        } else {
            LinphoneManager.getInstance().subscribeFriendList(false);
        }

        displayMissedCalls(LinphoneManager.getLc().getMissedCallsCount());

        LinphoneManager.getInstance().changeStatusToOnline();

//        if (getIntent().getIntExtra("PreviousActivity", 0) != CALL_ACTIVITY && !doNotGoToCallActivity) {
//            if (LinphoneManager.getLc().getCalls().length > 0) {
//                LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
//                LinphoneCall.State callState = call.getState();
//
//                if (callState == LinphoneCall.State.IncomingReceived) {
//                    startActivity(new Intent(this, CallIncomingActivity.class));
//                } else if (callState == LinphoneCall.State.OutgoingInit || callState == LinphoneCall.State.OutgoingProgress || callState == LinphoneCall.State.OutgoingRinging) {
//                    startActivity(new Intent(this, CallOutgoingActivity.class));
//                } else {
//                    startIncallActivity(call);
//                }
//            }
//        }
//        doNotGoToCallActivity = false;
    }

    @Override
    public void onDestroy() {
        if (mOrientationHelper != null) {
            mOrientationHelper.disable();
            mOrientationHelper = null;
        }


        super.onDestroy();

       // unbindDrawables(findViewById(R.id.topLayout));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view != null && view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }


    @Override
    public void onPause() {


        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }
        callTransfer = false;

        super.onPause();
    }

    private ProgressDialog progressDialog;

    @Override
    public View getScrollableView() {
        return null;
    }


    class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            String tel = data.getString("tel");
            switch (msg.what) {
                case 1:
                    DialogCustomUtils.randomNumDialog(getContext(), tel,
                            (v, v2) -> {
                                requestCustomNumber(tel, 2);
                                mDialSettings.setTag(2);
                                mDialSettings.setText("随机号码");
                                progressDialog = DialogCustomUtils.progressDialog(getContext());
                                progressDialog.show();
                                mPopupWindow.dismiss();
                            }, (v, vv) -> cancel(),
                            (v, v2) -> requestRandomNumber()).show();
//                    viewHolder.randomShow.setText(tel);

                    Log.e(TAG, "handleMessage: " + tel);
                    break;
                case 2:
                    int sTag = data.getInt("sTag");

                    try {
                        Log.e(TAG, "handleMessage: " + tel);
                        Toast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        if (TextUtils.isEmpty(tel)) {
                            return true;
                        }
                        mDialSettings.setTag(sTag);
                        int tag = (int) mDialSettings.getTag();
                        mReplace.setVisibility(tag == 2 || tag == 3 ? View.VISIBLE : View.GONE);
//                        if (tag == 4) {
//                            mainViewHolder.mOutPutNum.setText("sip:" + tel + "@" + host(getContext()));
//                        } else
                        mOutPutNum.setText(tel);

                        saveState(tag, tel);
                        if (tel.length() > 8)
                            findPhonePlace(tel);
                        else {
                            mOutPutWhere.setText("本地号码");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case 3:
                    String s = msg.getData().getString("data");
//                    FindPhonePlace find = new Gson().fromJson(s, FindPhonePlace.class);
//                    FindPhonePlace.ObjBean bean = find.getObj();
                    //                   mainViewHolder.mOutPutWhere.setText(bean.getProvince() + "-" + bean.getCity() + "-" + bean.getOperators());
                    break;
                case 4:
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(getContext(), "设置失败,请重试", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "handleMessage: 设置失败" + tel);
                    cancel();
                    break;
                case 400:
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    cancel();
                    Toast.makeText(getContext(), "连接超时,请检查网络", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }


        private void findPhonePlace(String tel) {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("tel", tel);
            message.what = 0;
            message.setData(bundle);
            try {
                server.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}




