package org.linphone.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.LinphoneService;
import org.linphone.R;
import org.linphone.RunningService;
import org.linphone.app.App;
import org.linphone.update.NotificationInfo;
import org.linphone.update.UpdateInfo;
import org.linphone.update.UpdateManager;
import org.linphone.utils.DialogCustomUtils;
import org.linphone.utils.DialogUtils;
import org.linphone.utils.JsonParseUtils;
import org.linphone.utils.MyCookie;
import org.linphone.webservice.Config;
import org.linphone.webservice.HttpWebServer;
import org.linphone.webservice.MyCallBack;
import org.xutils.common.Callback;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static android.content.Intent.ACTION_MAIN;


/**
 * A simple {@link Fragment} subclass.
 */
public class MineFragment extends Fragment {


    TextView mAccount;
    LinearLayout mAbout;
    LinearLayout mDialBlock;
    LinearLayout mSettings;
    LinearLayout mTextSizeSettings;
    TextView mExit;
    LinearLayout mUserInfo;
    TextView mUserName;
    TextView mVersionName;
    ImageView mIcon;

    private LinearLayout sound;

    private DialogUtils mProgressDialogUtils;
    private AlertDialog dialog;
    private AlertDialog pro;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        mIcon=view.findViewById(R.id.mIcon);
        mAccount = view.findViewById(R.id.mAccount);
        mAbout = view.findViewById(R.id.mAbout);
        mDialBlock = view.findViewById(R.id.mDialBlock);
        mSettings = view.findViewById(R.id.mSettings);
        mTextSizeSettings = view.findViewById(R.id.mTextSettings);
        sound = view.findViewById(R.id.sound);
        mExit = view.findViewById(R.id.mExit);
        mUserInfo = view.findViewById(R.id.mUserInfo);
        mUserName = view.findViewById(R.id.mUserName);
        mVersionName = view.findViewById(R.id.mVersionName);
        mProgressDialogUtils = new DialogUtils(getActivity());
        mVersionName.setText(Config.getVerName(getActivity()));

        if (App.app().getLoginData() != null) {
            mAccount.setText(App.app().getLoginData().getUsername());
            mUserName.setText(App.app().getLoginData().getName());
            String[] temp = App.app().getLoginData().getModelpower().split(",");
            if (temp != null) {
                for (int i = 0; i < temp.length; i++) {
                    if (temp[i].equals("1")) {
                        mDialBlock.setVisibility(View.GONE);
                    } else if (temp[i].equals("2")) {

                    } else if (temp[i].equals("3")) {

                    } else if (temp[i].equals("4")) {
                        mDialBlock.setVisibility(View.GONE);
                    }
                }
            }
        }

        setListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NimUserInfo userInfo = NIMClient.getService(UserService.class).getUserInfo(App.app().getLoginData().getUsername());
        mUserName.setText(userInfo.getName());
        mAccount.setText(userInfo.getAccount());
        Glide.with(mIcon).load(userInfo.getAvatar()).error(R.mipmap.ic_launcher2).placeholder(R.mipmap.ic_launcher2).into(mIcon);
        NIMClient.getService(UserService.class).fetchUserInfo(Arrays.asList(App.app().getLoginData().getUsername()))
                .setCallback(new RequestCallback<List<NimUserInfo>>() {
                    @Override
                    public void onSuccess(List<NimUserInfo> param) {
                        NimUserInfo nimUserInfo = param.get(0);
                        mUserName.setText(nimUserInfo.getName());
                        mAccount.setText(nimUserInfo.getAccount());
                        Glide.with(mIcon).load(nimUserInfo.getAvatar()).error(R.mipmap.ic_launcher2).placeholder(R.mipmap.ic_launcher2).into(mIcon);
                    }

                    @Override
                    public void onFailed(int code) {

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
        Log.e("MineFragment", "MineFragment: "  );
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
                Intent intent = new Intent(getActivity(), NumberBlockingActivity.class);
                getActivity().startActivity(intent);
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
//                String passwd = getActivity().getSharedPreferences("pas-config", Context.MODE_PRIVATE)
//                        .getString("passwd", "");
//                if (TextUtils.isEmpty(passwd) || !passwd.equals("Aa8998")) {
//                    dialog.show();
//                    WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
//                    attributes.width = -2;
//                    attributes.height = -2;
//                    dialog.getWindow().setAttributes(attributes);
//                } else {
//                    startSipSettings();
//                }
            }
        });

        mTextSizeSettings.setOnClickListener(v -> {
            startActivity(new Intent(getContext(),TextSizeSettingsActivity.class));
        });

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),SoundSettingActivity.class));
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogExit == null)
                    dialogExit = DialogCustomUtils.exitCustomDialog(getActivity(), new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            NIMClient.getService(AuthService.class).logout();
                            getContext().startService(new Intent(getActivity(), RunningService.class).putExtra("launch", false));
                            getContext().stopService(new Intent(getActivity(), RunningService.class).putExtra("launch", false));
                            getActivity().stopService(new Intent(ACTION_MAIN).setClass(getActivity(), LinphoneService.class));
                            getActivity().finish();
                            Process.killProcess(Process.myPid());
                        }
                    }, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                if (!dialogExit.isShowing())
                    dialogExit.show();
            }
        });
        mUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent =new Intent(getContext(),UserModifyActivity.class);
                startActivity(intent);
            }
        });
    }



    private AlertDialog dialogExit;

    private void test() {
        View inflate = View.inflate(getActivity(), R.layout.dialog_voip, null);
        dialog = DialogCustomUtils.create(inflate,

                (text) -> {
                    if (TextUtils.isEmpty(text.getText())) {
                        Toast.makeText(getActivity(), "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (text.getText().toString().equals("Aa8998")) {
                        Intent intent = new Intent(getActivity(), EasterEggActivity.class);
                        intent.putExtra("OP_FLAGS", true);
                        startActivity(intent);
                        dialog.dismiss();
                        text.getText().clear();
                    } else {
                        Toast.makeText(getActivity(), "您输入的密码不正确", Toast.LENGTH_SHORT).show();
                    }

                }, (views) -> {
                    dialog.dismiss();
                }
        );
        if (dialog != null) {
            dialog.show();
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
            return;
        }
    }

    public void updateSystem() {
        mProgressDialogUtils.showProgressDialog(true, "正在检测新版本");
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
                        } else {
                            Toast.makeText(getActivity(), "当前已是最新版本", Toast.LENGTH_SHORT).show();

                        }
                    }
                } catch (Exception e) {

                    //Toast.makeText(getActivity(), "检查更新信息失败", Toast.LENGTH_SHORT).show();

                }
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
                mProgressDialogUtils.showProgressDialog(false);
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


}
