package org.linphone.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.linphone.R;
import org.linphone.update.NotificationInfo;
import org.linphone.update.UpdateInfo;
import org.linphone.update.UpdateManager;
import org.linphone.utils.JsonParseUtils;
import org.linphone.utils.MyCookie;
import org.linphone.webservice.Config;
import org.linphone.webservice.HttpWebServer;
import org.linphone.webservice.MyCallBack;


/**
 * A simple {@link Fragment} subclass.
 */
public class MineFragmentto extends LazyLoadFragment {


    TextView mAccount;
    LinearLayout mAbout;
    LinearLayout mDialBlock;
    LinearLayout mSettings;
    TextView mExit;
    LinearLayout mUserInfo;
    TextView mUserName;
    TextView mVersionName;


    private AlertDialog dialog;
    private AlertDialog pro;

    public static MineFragmentto newInstance() {
        return new MineFragmentto();
    }

    public MineFragmentto() {

    }


    @Override
    protected int setContentView() {
        return R.layout.fragment_mineto;
    }

    @Override
    protected void initView() {
        mAccount = findViewById(R.id.mAccount);
        mAbout = findViewById(R.id.mAbout);
        mDialBlock = findViewById(R.id.mDialBlock);
        mSettings = findViewById(R.id.mSettings);
        mExit = findViewById(R.id.mExit);
        mUserInfo = findViewById(R.id.mUserInfo);
        mUserName = findViewById(R.id.mUserName);
        mVersionName = findViewById(R.id.mVersionName);

        mVersionName.setText(Config.getVerName(getActivity()));
        mUserName.setText(Config.getAppName(getActivity()));
        mAccount.setText(MyCookie.getString("account", "4089"));
        setListener();

    }

    @Override
    protected void lazyLoad() {

    }

    private void setListener() {
        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSystem();
            }
        });
        mDialBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//               openCallback.open(NumberBlockingFragment.class, false);
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwd = getContext().getSharedPreferences("pas-config", Context.MODE_PRIVATE)
                        .getString("passwd", "");
                if (TextUtils.isEmpty(passwd) || !passwd.equals("Aa8998")) {
                    dialog.show();
                    WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
                    attributes.width = -2;
                    attributes.height = -2;
                    dialog.getWindow().setAttributes(attributes);
                } else {
                    startSipSettings();
                }
            }
        });
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        mUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//               openCallback.open(UserInfoFragment.class, false);
            }
        });
    }

    public void updateSystem() {
        HttpWebServer webServer = new HttpWebServer();
        webServer.checkUpdate(new MyCallBack<String>() {

            @Override
            public void onSuccess(String result) {

                int vercode = Config.getVerCode(getActivity());
                try {
                    if (!TextUtils.isEmpty(result)) {
                        int newVerCode = Integer.parseInt(JsonParseUtils.jsonToResult(result, "VersionCode"));
                        String newVerName = JsonParseUtils.jsonToResult(result, "VersionName");
                        String apkname = JsonParseUtils.jsonToResult(result, "apkname");
                        String versionInfo = JsonParseUtils.jsonToResult(result, "VersionInfo");
                        String versonURL = "http://www.freetk.cn:8789/download/" + apkname;
                        if (newVerCode > vercode) {
                            updateApk(getActivity(), versionInfo, newVerName, true, true, 10000000, versonURL, getActivity().getResources().getString(R.string.app_name));
                        }
                    }
                } catch (Exception e) {

                    //Toast.makeText(getActivity(), "检查更新信息失败", Toast.LENGTH_SHORT).show();

                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.arg1 = 1;
                mhandler.sendMessage(message);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
                super.onCancelled(cex);
            }

            @Override
            public void onFinished() {
                super.onFinished();
            }
        });

    }

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // showAdDialog();
        }
    };

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


    public void startSipSettings() {
//        Intent intent = new Intent(getContext(), EasterEggActivity.class);
//        ((HomeActivity) getContext()).startActivityForResult(intent, HomeActivity.REQUEST_ID_SIP_SETTING);
    }


    @Override
    public View getScrollableView() {
        return null;
    }
}
