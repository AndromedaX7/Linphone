package org.linphone.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.linphone.R;
import org.linphone.update.NotificationInfo;
import org.linphone.update.UpdateInfo;
import org.linphone.update.UpdateManager;
import org.linphone.utils.DialogUtils;
import org.linphone.utils.JsonParseUtils;
import org.linphone.utils.ToastUtils;
import org.linphone.webservice.Config;
import org.linphone.webservice.HttpWebServer;
import org.linphone.webservice.MyCallBack;
import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;



/**
 * Created by Administrator on 2017-03-23.
 */

public class BaseActivity extends FragmentActivity {
    private DialogUtils mProgressDialogUtils;
    private ToastUtils mToast;
    protected Context mContext = null;
    protected Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setStatusBar(getStatusBarColor());
        // Eyes.setStatusBarLightMode(this, Color.WHITE);
        resources = getResources();
        updateSystem();
        initUtils();
        //注册sdk的event用于接收各种event事件
        // JMessageClient.registerEventReceiver(this);
    }

    public ImageOptions imageOptions = new ImageOptions.Builder()
            // .setIgnoreGif(false)//是否忽略gif图。false表示不忽略。不写这句，默认是true
            .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
            .setFailureDrawableId(R.drawable.not_photo)
            .setLoadingDrawableId(R.drawable.not_photo)
            .build();

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 统计应用时长(也就是Session时长)
         * 不要同时在父和子Activity中重复添加onPause方法，否则会造成重复统计
         * 确保在所有的Activity中都调用
         */
        try {
            //MobclickAgent.onResume(mContext);
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * 统计应用时长(也就是Session时长)
         * 不要同时在父和子Activity中重复添加onPause方法，否则会造成重复统计
         * 确保在所有的Activity中都调用
         */
        try {
            //  MobclickAgent.onPause(mContext);
        } catch (Exception ex) {

        }


    }

    /**
     * Android 6.0 以上设置状态栏颜色
     */
    protected void setStatusBar(@ColorInt int color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 设置状态栏底色颜色
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().setStatusBarColor(color);
                // 如果亮色，设置状态栏文字为黑色
                if (isLightColor(color)) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * 判断颜色是不是亮色
     *
     * @param color
     * @return
     * @from https://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
     */
    private boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

    /**
     * 获取StatusBar颜色，默认白色
     *
     * @return
     */
    protected @ColorInt
    int getStatusBarColor() {
        return Color.WHITE;
    }



    public void startActivity() {
        Intent intent = new Intent(mContext, LoginDemoActivity.class);
        startActivity(intent);
        finish();
    }

    private void initUtils() {
        mProgressDialogUtils = new DialogUtils(this);
        mToast = new ToastUtils(this);
    }

    public void showToast(String message) {
        mToast.showToast(message);
    }

    public void showProgressDialog(boolean show, String message) {
        mProgressDialogUtils.showProgressDialog(show, message);
    }

    public void showProgressDialog(boolean show) {
        mProgressDialogUtils.showProgressDialog(show);
    }

    public void showProgressDialogDismiss() {
        mProgressDialogUtils.dismiss();
    }

    public void ToB(Class<?> pClass) {
        Intent intent = new Intent(this, pClass);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);

    }

    public void ToC(Class<?> pClass) {
        Intent intent = new Intent(this, pClass);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_none);

    }

    public void showProgressDiaolog(boolean show) {
        mProgressDialogUtils.showProgressDialog(show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialogUtils.dismiss();
    }

    //此方法只是关闭软键盘
    public void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 通用消息提示
     *
     * @param resId
     */
    public void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 通用消息提示
     *
     * @param
     */
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    /**
     * 从资源获取字符串
     *
     * @param resId
     * @return
     */
    public String getStr(int resId) {
        return resources.getString(resId);
    }

    /**
     * 从EditText 获取字符串
     *
     * @param editText
     * @return
     */
    public String getStr(EditText editText) {
        return editText.getText().toString();
    }

    public void updateSystem() {
        HttpWebServer webServer = new HttpWebServer();
        webServer.checkUpdate(new MyCallBack<String>() {

            @Override
            public void onSuccess(String result) {

                int vercode = Config.getVerCode(mContext);
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
                            updateApk(mContext, versionInfo, newVerName, isForce, true, 10000000, versonURL, mContext.getResources().getString(R.string.app_name));
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
