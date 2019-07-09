package org.linphone.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.ImClientProxy;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneUtils;
import org.linphone.R;
import org.linphone.app.App;
import org.linphone.assistant.AssistantActivity;
import org.linphone.assistant.CodecDownloaderFragment;
import org.linphone.assistant.EchoCancellerCalibrationFragment;
import org.linphone.bean.LoginDataBean;
import org.linphone.core.LinphoneAccountCreator;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Version;
import org.linphone.soap.WebServiceUtils;
import org.linphone.tools.OpenH264DownloadHelper;
import org.linphone.update.NotificationInfo;
import org.linphone.update.UpdateInfo;
import org.linphone.update.UpdateManager;
import org.linphone.utils.DialogCustomUtils;
import org.linphone.utils.DialogUtils;
import org.linphone.utils.IPUtils;
import org.linphone.utils.JsonParseUtils;
import org.linphone.utils.MacAddressWrapper;
import org.linphone.utils.MyCookie;
import org.linphone.webservice.Config;
import org.linphone.webservice.HttpWebServer;
import org.linphone.webservice.MyCallBack;
import org.xutils.common.Callback;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * @author 52845
 */
@ContentView(R.layout.activity_login_demo)
public class LoginDemoActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback, LinphoneAccountCreator.LinphoneAccountCreatorListener {
    private static final String TAG = "LoginActivity";
    @ViewInject(R.id.mEasterEggEndPoint)
    ImageView mEasterEggEndPoint;
    @ViewInject(R.id.mAccount)
    EditText mAccount;
    @ViewInject(R.id.mPassword)
    EditText mPassword;
    @ViewInject(R.id.mRemAccount)
    CheckBox mRemAccount;
    @ViewInject(R.id.mOpenExt)
    ImageView mOpen;
    @ViewInject(R.id.mLogin)
    TextView btnLogin;


    @ViewInject(R.id.mLoginData)
    LinearLayout mLoginData;
    @ViewInject(R.id.mProgress)
    ProgressBar mProgress;
    @ViewInject(R.id.mPasswordHidden)
    ImageView mPasswordHidden;
    String serverIP = "";
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 201;
    private LinphoneCoreListenerBase mListener;
    private boolean remoteProvisioningInProgress;
    private Dialog dialog;
    String account = "";
    String password = "";
    private String ip = "";
    private String mac = "";
    private boolean isShow;
    private String[] datas;
    private ArrayAdapter<String> adapter;
    PopupWindow mPopupWindow;
    DialogUtils mProgressDialogUtils;
    Context mContext;
    private long lastTime;
    private int count = 1;

    //    private LoginPopupAdapter mAdapter = new LoginPopupAdapter(new ArrayList<>());
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        String nem = LoginDemoActivity.this.getPackageName();
        mContext = this;
        mProgressDialogUtils = new DialogUtils(LoginDemoActivity.this);
        updateSystem();
        mPrefs = LinphonePreferences.instance();
        accountCreator = LinphoneCoreFactory.instance().createAccountCreator(LinphoneManager.getLc(), LinphonePreferences.instance().getXmlrpcUrl());
        accountCreator.setDomain(getResources().getString(R.string.default_domain));
        accountCreator.setListener(this);
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        if (!wifiManager.isWifiEnabled()) {
//            wifiManager.setWifiEnabled(true);
//        }
        showPop();
        serverIP = MyCookie.getString("ip", "");
        if (TextUtils.isEmpty(serverIP)) {
            serverIP = "219.149.195.145:5061";
            MyCookie.putString("ip", serverIP);
        }
        try {
            ip = IPUtils.getIpAddress(this);
            mac = MacAddressWrapper.getMac(this);
        } catch (Exception ex) {
        }
        setListener();
    }

    private void Login() {
        mProgressDialogUtils.showProgressDialog(true);
        ImClientProxy.login(mAccount.getText().toString(), "000000", new RequestCallback() {
            @Override
            public void onSuccess(Object param) {
                WebServiceUtils.Login(mAccount.getText().toString(), mPassword.getText().toString(), mac, ip, new WebServiceUtils.CallBack() {
                    @Override
                    public void result(String result) {
                        mProgressDialogUtils.showProgressDialog(false);
                        if (JsonParseUtils.jsonToBoolean(result)) {
                            String obj = JSON.parseObject(result).getString(
                                    "obj");
                            Gson gson = new Gson();
                            LoginDataBean loginData = gson.fromJson(obj, LoginDataBean.class);
                            if (loginData != null) {
                                App.app().setLoginData(loginData);
                                String account_name = MyCookie.getString("account", "0000");

                                NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(App.app().getLoginData().getUsername());
                                Map m = userInfo.getExtensionMap();
                                if (m==null) {
                                    HashMap<UserInfoFieldEnum, Object> maps = new HashMap<>();
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("chat_text_size", 18);
                                    maps.put(UserInfoFieldEnum.EXTEND, map);
                                    NIMClient.getService(UserService.class).updateUserInfo(maps).setCallback(null);
                                }
                                if (account_name.equals(mAccount.getText().toString())) {
                                    startActivity(new Intent().setClass(LoginDemoActivity.this, LinphoneActivity.class).setData(getIntent().getData()));
                                    finish();
//                    goToMainActivity();
                                } else {
                                    saveCreatedAccount(account, password, null, null, serverIP, LinphoneAddress.TransportType.LinphoneTransportUdp);
                                    accountManager(true, account, password, serverIP);
                                    startActivity(new Intent().setClass(LoginDemoActivity.this, LinphoneActivity.class).setData(getIntent().getData()));
                                    finish();
                                }
                                MyCookie.managerAccount(account, password);

                            } else {
                                Toast.makeText(LoginDemoActivity.this, "登录账号或密码错误", Toast.LENGTH_SHORT).show();
                                mProgressDialogUtils.showProgressDialog(false);
                            }
                        } else {
                            String msg = JsonParseUtils.jsonToResult(result, "msg");
                            Toast.makeText(LoginDemoActivity.this, msg, Toast.LENGTH_SHORT).show();
                            mProgressDialogUtils.showProgressDialog(false);
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code) {
                Log.e(TAG, "onFailed: " + code);
                WebServiceUtils.Login(mAccount.getText().toString(), mPassword.getText().toString(), mac, ip, new WebServiceUtils.CallBack() {
                    @Override
                    public void result(String result) {
                        mProgressDialogUtils.showProgressDialog(false);
                        if (JsonParseUtils.jsonToBoolean(result)) {
                            String obj = JSON.parseObject(result).getString(
                                    "obj");
                            Gson gson = new Gson();
                            LoginDataBean loginData = gson.fromJson(obj, LoginDataBean.class);
                            if (loginData != null) {
                                App.app().setLoginData(loginData);
                                String account_name = MyCookie.getString("account", "0000");
                                if (account_name.equals(mAccount.getText().toString())) {
                                    startActivity(new Intent().setClass(LoginDemoActivity.this, LinphoneActivity.class).setData(getIntent().getData()));
                                    finish();
//                    goToMainActivity();
                                } else {
                                    saveCreatedAccount(account, password, null, null, serverIP, LinphoneAddress.TransportType.LinphoneTransportUdp);
                                    accountManager(true, mAccount.getText().toString(), mPassword.getText().toString(), serverIP);
                                    startActivity(new Intent().setClass(LoginDemoActivity.this, LinphoneActivity.class).setData(getIntent().getData()));
                                    finish();
                                }
                                MyCookie.managerAccount(account, password);
                            } else {
                                Toast.makeText(LoginDemoActivity.this, "登录账号或密码错误", Toast.LENGTH_SHORT).show();
                                mProgressDialogUtils.showProgressDialog(false);
                            }
                        } else {
                            String msg = JsonParseUtils.jsonToResult(result, "msg");
                            Toast.makeText(LoginDemoActivity.this, msg, Toast.LENGTH_SHORT).show();
                            mProgressDialogUtils.showProgressDialog(false);
                        }
                    }
                });
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
                WebServiceUtils.Login(mAccount.getText().toString(), mPassword.getText().toString(), mac, ip, new WebServiceUtils.CallBack() {
                    @Override
                    public void result(String result) {
                        mProgressDialogUtils.showProgressDialog(false);
                        if (JsonParseUtils.jsonToBoolean(result)) {
                            String obj = JSON.parseObject(result).getString(
                                    "obj");
                            Gson gson = new Gson();
                            LoginDataBean loginData = gson.fromJson(obj, LoginDataBean.class);
                            if (loginData != null) {
                                App.app().setLoginData(loginData);
                                String account_name = MyCookie.getString("account", "0000");
                                if (account_name.equals(mAccount.getText().toString())) {
                                    startActivity(new Intent().setClass(LoginDemoActivity.this, LinphoneActivity.class).setData(getIntent().getData()));
                                    finish();
//                    goToMainActivity();
                                } else {
                                    saveCreatedAccount(account, password, null, null, serverIP, LinphoneAddress.TransportType.LinphoneTransportUdp);
                                    accountManager(true, mAccount.getText().toString(), mPassword.getText().toString(), serverIP);
                                    startActivity(new Intent().setClass(LoginDemoActivity.this, LinphoneActivity.class).setData(getIntent().getData()));
                                    finish();
                                }
                                MyCookie.managerAccount(account, password);
                            } else {
                                Toast.makeText(LoginDemoActivity.this, "登录账号或密码错误", Toast.LENGTH_SHORT).show();
                                mProgressDialogUtils.showProgressDialog(false);
                            }
                        } else {
                            String msg = JsonParseUtils.jsonToResult(result, "msg");
                            Toast.makeText(LoginDemoActivity.this, msg, Toast.LENGTH_SHORT).show();
                            mProgressDialogUtils.showProgressDialog(false);
                        }
                    }
                });
            }
        });
    }

    private void setListener() {
        mEasterEggEndPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHiddenEndpointPressed();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account = mAccount.getText().toString();
                password = mPassword.getText().toString();
                if (TextUtils.isEmpty(account)) {
                    Toast.makeText(LoginDemoActivity.this, "请输入集团号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginDemoActivity.this, "请输入登录密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                Login();

            }
        });
        mPasswordHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShow) {
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                isShow = !isShow;
                mPassword.setSelection(mPassword.getText().length());
            }
        });
        mOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getCount() != 0) {
                    mPopupWindow.showAsDropDown(mAccount);
                }
            }
        });
        mListener = new LinphoneCoreListenerBase() {

            @Override
            public void configuringStatus(LinphoneCore lc, final LinphoneCore.RemoteProvisioningState state, String message) {
//                if (progress != null) {
//                    progress.dismiss();
//                }
                mProgressDialogUtils.showProgressDialog(false);
                if (state == LinphoneCore.RemoteProvisioningState.ConfiguringSuccessful) {
                    goToLinphoneActivity();
                } else if (state == LinphoneCore.RemoteProvisioningState.ConfiguringFailed) {
                    Toast.makeText(AssistantActivity.instance(), getString(R.string.remote_provisioning_failure), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
                if (remoteProvisioningInProgress) {
//                    if (progress != null) {
//                        progress.dismiss();
//                    }
                    mProgressDialogUtils.showProgressDialog(false);
                    if (state == LinphoneCore.RegistrationState.RegistrationOk) {
                        remoteProvisioningInProgress = false;
                        success();
                    }
                } else if (accountCreated && !newAccount) {
                    if (address != null && address.asString().equals(cfg.getAddress().asString())) {
                        if (state == LinphoneCore.RegistrationState.RegistrationOk) {

//                            if (progress != null) {
//                                progress.dismiss();
//                            }
                            mProgressDialogUtils.showProgressDialog(false);
                            if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
                                accountCreator.isAccountUsed();
                            }
                            goToMainActivity();
                        } else if (state == LinphoneCore.RegistrationState.RegistrationFailed) {
//                            if (progress != null) {
//                                progress.dismiss();
//                            }
//                            for (int i = 0; i < 3; i++) {
//                                saveCreatedAccount(account, password, null, null, serverIP, LinphoneAddress.TransportType.LinphoneTransportUdp);
//                                accountManager(true, account, password, serverIP);
//
//                            }
//                            Toast.makeText(mContext, "网络异常，请重新登录", Toast.LENGTH_SHORT).show();
//                            if (lc != null) {
//                                lc.refreshRegisters();
//                            }else {
////                                if (progress != null) {
////                                    progress.dismiss();
////                                }
//                                mProgressDialogUtils.showProgressDialog(false);
//                            }
//                            if (dialog == null || !dialog.isShowing()) {
//                                dialog = createErrorDialog(cfg, smessage);
//                                dialog.show();
//                            }
                        } else if (!(state == LinphoneCore.RegistrationState.RegistrationProgress)) {
//                            if (progress != null) {
//                                progress.dismiss();
//                            }
                        }
                    }
                }
            }
        };
    }

    private void test() {
        View inflate = View.inflate(this, R.layout.dialog_voip, null);
        dialog = DialogCustomUtils.create(inflate,

                (text) -> {
                    if (TextUtils.isEmpty(text.getText())) {
                        Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (text.getText().toString().equals("Aa8998")) {
                        Intent intent = new Intent(this, EasterEggActivity.class);
                        intent.putExtra("OP_FLAGS", true);
                        startActivity(intent);
                        dialog.dismiss();
                        text.getText().clear();
                    } else {
                        Toast.makeText(this, "您输入的密码不正确", Toast.LENGTH_SHORT).show();
                    }

                    //todo check password

                }, (views) -> {
                    dialog.dismiss();
                }
        );
    }

    private void onHiddenEndpointPressed() {
        if (System.currentTimeMillis() - 2000 <= lastTime) {
//            Log.e(TAG, "onHiddenEndpointPressed: " + count);
            count = count + 1;
            if (count % 5 == 0) {
                test();
                if (dialog != null) {
                    dialog.show();
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    dialog.getWindow().setAttributes(params);
                    lastTime = 0;
                    count = 1;
                    return;
                }
            }
            //count++;
        } else {
            count = 1;
        }
        Log.e(TAG, "onHiddenEndpointPressed: " + count);
        lastTime = System.currentTimeMillis();
    }

    private void showPop() {

        View window = View.inflate(this, R.layout.popupwindow_list, null);
        mPopupWindow = new PopupWindow(window, ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.dp_100), true);
        ListView listView = window.findViewById(R.id.mList);
        String arrs = MyCookie.getString("accountArrs", "");
        Log.e(TAG, "showPop: " + arrs);
        datas = arrs.split(",");
        if (datas.length > 0) {
            mAccount.setText(datas[0]);
            mPassword.setText(MyCookie.getString(datas[0], ""));
        }
        //        初始化适配器
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, datas);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPopupWindow.dismiss();
                mAccount.setText(datas[i]);
                mPassword.setText(MyCookie.getString(datas[i], ""));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        String flag = MyCookie.getString("RemAccount", "1");
        if (flag.equals("0")) {
            String[] strings = accountManager(false);
            mAccount.setText(strings[0]);
            mPassword.setText(strings[1]);
            mAccount.setSelection(mAccount.getText().length());
        }
        serverIP = MyCookie.getString("ip", "");
        if (TextUtils.isEmpty(serverIP)) {
            serverIP = "219.149.195.145:5061";
        }
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
            LinphoneProxyConfig lpc = lc.getDefaultProxyConfig();
            if (lpc != null) {
                mListener.registrationState(lc, lpc, lpc.getState(), null);
            }
        }
    }

    @Override
    public void onPause() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }
        super.onPause();
    }

    private ProgressDialog progress;
    private boolean accountCreated = false, newAccount = false, isLink = false, fromPref = false;
    private LinphoneAddress address;
    private LinphonePreferences mPrefs;
    private LinphoneAccountCreator accountCreator;

    public void saveCreatedAccount(String username, String password, String prefix, String ha1, String domain, LinphoneAddress.TransportType transport) {


        username = LinphoneUtils.getDisplayableUsernameFromAddress(username);
        domain = LinphoneUtils.getDisplayableUsernameFromAddress(domain);

        String identity = "sip:" + username + "@" + domain;
        try {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        } catch (LinphoneCoreException e) {
            org.linphone.mediastream.Log.e(e);
        }

        boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
        LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setHa1(ha1)
                .setPassword(password);

        if (prefix != null) {
            builder.setPrefix(prefix);
        }

        if (isMainAccountLinphoneDotOrg) {
            if (getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            } else {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);
            }

            builder.setExpires("604800")
                    .setAvpfEnabled(true)
                    .setAvpfRRInterval(3)
                    .setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
                    .setQualityReportingEnabled(true)
                    .setQualityReportingInterval(180)
                    .setRealm("sip.linphone.org")
                    .setNoDefault(false);

            mPrefs.enabledFriendlistSubscription(getResources().getBoolean(R.bool.use_friendlist_subscription));

            mPrefs.setStunServer(getString(R.string.default_stun));
            mPrefs.setIceEnabled(true);

            accountCreator.setPassword(password);
            accountCreator.setHa1(ha1);
            accountCreator.setUsername(username);
        } else {
            String forcedProxy = "";
            if (!TextUtils.isEmpty(forcedProxy)) {
                builder.setProxy(forcedProxy)
                        .setOutboundProxyEnabled(true)
                        .setAvpfRRInterval(5);
            }

            if (transport != null) {
                builder.setTransport(transport);
            }
        }

        if (getResources().getBoolean(R.bool.enable_push_id)) {
            String regId = mPrefs.getPushNotificationRegistrationID();
            String appId = getString(R.string.push_sender_id);
            if (regId != null && mPrefs.isPushNotificationEnabled()) {
                String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
                builder.setContactParameters(contactInfos);
            }
        }

        try {
            builder.saveNewAccount();
            if (!newAccount) {
//                displayRegistrationInProgressDialog();
                // mProgressDialogUtils.showProgressDialog(true);
            }
            accountCreated = true;
        } catch (LinphoneCoreException e) {
            org.linphone.mediastream.Log.e(e);
        }
    }

    public void displayRegistrationInProgressDialog() {
        if (LinphoneManager.getLc().isNetworkReachable()) {
            progress = ProgressDialog.show(this, null, null);
            Drawable d = new ColorDrawable(ContextCompat.getColor(this, R.color.colorE));
            d.setAlpha(200);
            progress.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            progress.getWindow().setBackgroundDrawable(d);
            progress.setContentView(R.layout.progress_dialog);
            progress.show();
        }
    }

    private void showProgress(boolean is) {
        mProgress.setVisibility(is ? View.VISIBLE : View.INVISIBLE);
    }

    private void showLogin(boolean is) {
        mLoginData.setVisibility(is ? View.VISIBLE : View.INVISIBLE);
    }

    public String[] accountManager(boolean isSave, String... accountPwdIP) {
        SharedPreferences sp = getSharedPreferences("sipAccount", MODE_PRIVATE);
        if (isSave) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("account", accountPwdIP[0]);
            editor.putString("pwd", accountPwdIP[1]);
            editor.putString("ip", accountPwdIP[2]);
            editor.apply();
            return accountPwdIP;
        } else {
            String[] strings = new String[3];
            strings[0] = sp.getString("account", "4089");
            strings[1] = sp.getString("pwd", "huawei123");
            strings[2] = sp.getString("ip", "219.149.195.145:5061");
            return strings;
        }
    }

    private void goToMainActivity() {
        MyCookie.putString("account", mAccount.getText().toString());
        MyCookie.putString("pwd", mPassword.getText().toString());
        MyCookie.putString("ip", serverIP);
        if (mRemAccount.isChecked()) {
            MyCookie.putString("RemAccount", "0");
        } else {
            MyCookie.putString("RemAccount", "1");
        }
        String arrs = MyCookie.getString("accountArrs", "");
        if (!TextUtils.isEmpty(arrs)) {
            String[] temp = arrs.split(",");
            if (temp.length > 0) {
                boolean f = false;
                for (int i = 0; i < temp.length; i++) {
                    if (mAccount.getText().toString().equals(temp[i])) {
                        f = true;
                        break;
                    }
                }
                if (!f) {
                    arrs += "," + mAccount.getText().toString();
                    MyCookie.putString("accountArrs", arrs);
                }
            } else {
                MyCookie.putString("accountArrs", mAccount.getText().toString());
            }
        } else {
            MyCookie.putString("accountArrs", mAccount.getText().toString());
        }


        startActivity(new Intent().setClass(LoginDemoActivity.this, LinphoneActivity.class).setData(getIntent().getData()));
        //startActivity(new Intent(LoginDemoActivity.this, HomeActivity.class));
        finish();
    }

    public void displayCreateAccount() {
//        fragment = new CreateAccountFragment();
//        Bundle extra = new Bundle();
//        extra.putBoolean("LinkPhoneNumber", isLink);
//        extra.putBoolean("LinkFromPref", fromPref);
//        fragment.setArguments(extra);
//        changeFragment(fragment);
//        currentFragment = AssistantFragmentsEnum.CREATE_ACCOUNT;
//        back.setVisibility(View.VISIBLE);
    }

    public void success() {
        boolean needsEchoCalibration = LinphoneManager.getLc().needsEchoCalibration();
        if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
            launchEchoCancellerCalibration(true);
        } else {
            launchDownloadCodec();
        }
    }

    public Dialog createErrorDialog(LinphoneProxyConfig proxy, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (message.equals("Forbidden")) {
            message = getString(R.string.assistant_error_bad_credentials);
        }
        //登录异常 ：" + message + " ，是否进行重试
        builder.setMessage("登录超时，网络异常，请重试")
                .setTitle("提示")
                .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //success();

                        goToLinphoneActivity();
//                        LinphoneManager.getLc().removeProxyConfig(LinphoneManager.getLc().getDefaultProxyConfig());
//                        LinphonePreferences.instance().resetDefaultProxyConfig();
//                        LinphoneManager.getLc().refreshRegisters();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
//                        LinphoneManager.getLc().removeProxyConfig(LinphoneManager.getLc().getDefaultProxyConfig());
//                        LinphonePreferences.instance().resetDefaultProxyConfig();
//                        LinphoneManager.getLc().refreshRegisters();
                        dialog.dismiss();
                    }
                });
        return builder.show();
    }

    private void launchEchoCancellerCalibration(boolean sendEcCalibrationResult) {
        int recordAudio = getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        org.linphone.mediastream.Log.i("[Permission] Record audio permission is " + (recordAudio == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (recordAudio == PackageManager.PERMISSION_GRANTED) {
            EchoCancellerCalibrationFragment fragment = new EchoCancellerCalibrationFragment();
            fragment.enableEcCalibrationResultSending(sendEcCalibrationResult);
//            changeFragment(fragment);
//            currentFragment = AssistantFragmentsEnum.ECHO_CANCELLER_CALIBRATION;
//            back.setVisibility(View.VISIBLE);
//            cancel.setEnabled(false);
        } else {
            checkAndRequestAudioPermission();
        }
    }

    public void checkAndRequestAudioPermission() {
        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, 0);
    }

    public void checkAndRequestPermission(String permission, int result) {
        int permissionGranted = getPackageManager().checkPermission(permission, getPackageName());
        org.linphone.mediastream.Log.i("[Permission] " + permission + " is " + (permissionGranted == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            if (LinphonePreferences.instance().firstTimeAskingForPermission(permission) || ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                org.linphone.mediastream.Log.i("[Permission] Asking for " + permission);
                ActivityCompat.requestPermissions(this, new String[]{permission}, result);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            org.linphone.mediastream.Log.i("[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
        }

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchEchoCancellerCalibration(true);
            } else {
                isEchoCalibrationFinished();
            }
        }
    }

    public void isEchoCalibrationFinished() {
        launchDownloadCodec();
    }


    private void launchDownloadCodec() {
        if (LinphoneManager.getLc().openH264Enabled()) {
            OpenH264DownloadHelper downloadHelper = LinphoneCoreFactory.instance().createOpenH264DownloadHelper();
            if (Version.getCpuAbis().contains("armeabi-v7a") && !Version.getCpuAbis().contains("x86") && !downloadHelper.isCodecFound()) {
                CodecDownloaderFragment codecFragment = new CodecDownloaderFragment();
//                changeFragment(codecFragment);
//                currentFragment = AssistantFragmentsEnum.DOWNLOAD_CODEC;
//                back.setVisibility(View.VISIBLE);
//                cancel.setEnabled(false);
            } else {
                goToLinphoneActivity();
            }
        } else {
            goToLinphoneActivity();
        }
    }

    private void goToLinphoneActivity() {
        mPrefs.firstLaunchSuccessful();
        startActivity(new Intent().setClass(this, LinphoneActivity.class).putExtra("isNewProxyConfig", true));
        finish();
    }

    public void endDownloadCodec() {
        goToLinphoneActivity();
    }

    @Override
    public void onAccountCreatorIsAccountUsed(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
        if (status.equals(LinphoneAccountCreator.Status.AccountExistWithAlias)) {
            success();
        } else {
            isLink = true;
            displayCreateAccount();
        }
    }

    @Override
    public void onAccountCreatorAccountCreated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorAccountActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorAccountLinkedWithPhoneNumber(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorPhoneNumberLinkActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorIsAccountActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorPhoneAccountRecovered(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorIsAccountLinked(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorIsPhoneNumberUsed(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    @Override
    public void onAccountCreatorPasswordUpdated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {

    }

    public void updateSystem() {
        HttpWebServer webServer = new HttpWebServer();
        webServer.checkUpdate(new MyCallBack<String>() {

            @Override
            public void onSuccess(String result) {

                int vercode = Config.getVerCode(LoginDemoActivity.this);
                try {
                    if (!TextUtils.isEmpty(result)) {
                        int newVerCode = Integer.parseInt(JsonParseUtils.jsonToResult(result, "VersionCode"));
                        String newVerName = JsonParseUtils.jsonToResult(result, "VersionName");
                        String apkname = JsonParseUtils.jsonToResult(result, "apkname");
                        String versionInfo = JsonParseUtils.jsonToResult(result, "VersionInfo");
                        String isMustDownload = JsonParseUtils.jsonToResult(result, "isMustDownload");

                        boolean isForce = false;
                        if (!TextUtils.isEmpty(isMustDownload)) {
                            if (isMustDownload.equals("0")) {
                                isForce = false;
                            } else if (isMustDownload.equals("1")) {
                                isForce = true;
                            }
                        }
                        String versonURL = "http://www.freetk.cn:8789/download/" + apkname;
                        if (newVerCode > vercode) {
                            updateApk(LoginDemoActivity.this, versionInfo, newVerName, isForce, true, 10000000, versonURL, LoginDemoActivity.this.getResources().getString(R.string.app_name));
                        }
                    }
                } catch (Exception e) {

                    //Toast.makeText(getActivity(), "检查更新信息失败", Toast.LENGTH_SHORT).show();

                }

//                JSONArray array = null;
//                    array = new JSONArray(result);
//                    if (array.length() > 0) {
//                        JSONObject obj = array.getJSONObject(0);
//                        try {
//                            int newVerCode = Integer.parseInt(obj.getString("VersionCode"));
//                            String newVerName = obj.getString("VersionName");
//                            String isMustDownload = "1";
//                            String apkname = obj.getString("apkname");
//                            //String versonURL = obj.getString("VersonURL");
//                            String verson_size = obj.getString("volume");
//                            String versionInfo = obj.getString("VersionInfo");
//                            //ss = URLDecoder.decode(obj.getString("VersionInfo"), "UTF-8").split("&");
//                            if (newVerCode > vercode) {
//                                boolean isForce = false;
//                                if (isMustDownload.equals("0")) {
//                                    isForce = false;
//                                } else if (isMustDownload.equals("1")) {
//                                    isForce = true;
//                                }
//                                String versonURL = "http://www.freetk.cn:8789/download/" + apkname;
//                                updateApk(mContext, versionInfo, newVerName, isForce, true, 10000000, versonURL, mContext.getResources().getString(R.string.app_name));
//                                // setIsMustDownload();
//                            } else {
//                                // Toast.makeText(getActivity(), "已是最新版本。版本号：" + newVerName, Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (Exception e) {
//
//                            //Toast.makeText(getActivity(), "检查更新信息失败", Toast.LENGTH_SHORT).show();
//
//                        }

//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Message message = new Message();
//                message.arg1 = 1;
//                mhandler.sendMessage(message);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {
                super.onCancelled(cex);
            }

            @Override
            public void onFinished() {
                super.onFinished();
            }
        });

    }

    /**
     * @param hitContent  提示更新内容
     * @param versionName 更新版本名
     * @param isForce     是否强制升级
     * @param isSlient    是否静默安装
     * @param fileSize    Apk文件大小
     * @param apkURL      Apk下载链接
     * @param apkName     Apk名称
     */
    public void updateApk(Context mContext, String hitContent, String versionName, boolean isForce, boolean isSlient, long fileSize, String apkURL, String apkName) {
        //不用害怕 根据英文名称直译就可以
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.versionName = versionName;
        updateInfo.versionCode = 10;
        updateInfo.isForce = isForce;
        updateInfo.size = fileSize;
        updateInfo.updateContent = hitContent;
        if (isForce) {
            updateInfo.isIgnorable = false;
        }
        NotificationInfo notificationInfo = new NotificationInfo(R.mipmap.ic_launcher_round, R.mipmap.ic_launcher_round, apkName, "正在下载中", hitContent);
        new UpdateManager(mContext, apkURL, apkName, isSlient, updateInfo, notificationInfo).init();
    }

}
