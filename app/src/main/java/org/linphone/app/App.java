package org.linphone.app;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

import org.linphone.LinphoneService;
import org.linphone.RunningService;
import org.linphone.bean.LoginDataBean;
import org.xutils.x;


/**
 * Created by miao on 2018/10/23.
 */
public class App extends NimApplication implements Application.ActivityLifecycleCallbacks {
//    private ObserverCore core;

    public static boolean newRecord = false;

    private static final String TAG = "LinPhoneDemo";
    ServiceConnection serviceConnection;
    private LoginDataBean loginData;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        x.Ext.init(this);
//        CrashHandler handler = CrashHandler.getInstance();
//        handler.init(getApplicationContext());
        registerActivityLifecycleCallbacks(this);
//        if (NIMUtil.isMainProcess(this)) {
//            core = new ObserverCore();
//            core.registerObserver(true);
//        }
//        initLinphone();
    }

    public LoginDataBean getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginDataBean loginData) {
        this.loginData = loginData;
    }

    private static App app;

    public static App app() {
        return app;
    }

    public boolean IsForbidden = false;

    private void initLinphone() {
//        KLog.i(TAG, "========== BaseActivity.initLinphone");
//        KLog.i(TAG, "========== LinphoneService.isReady() = " + LinphoneService.isReady());
        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            Intent intent = new Intent(this, LinphoneService.class);
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // KLog.i(TAG, "========== BaseActivity.onServiceConnected  " + "name = [" + name + "], service = [" + service + "]");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    //  KLog.i(TAG, "========== BaseActivity.onServiceDisconnected  " + "name = [" + name + "]");
                }
            };
            bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

            mThread = new ServiceWaitThread();
            mThread.start();
        }
    }

    private void onServiceReady() {
        // We need LinphoneService to start bluetoothManager    蓝牙适配 暂不需要
//        if (Version.sdkAboveOrEqual(Version.API11_HONEYCOMB_30)) {
//            BluetoothManager.getInstance().initBluetooth();
//        }

//
//        linphoneLauncher = LinphoneLauncher.getInstance(this);
    }

    private Handler mainHandler = new Handler();
    private ServiceWaitThread mThread;

    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onServiceReady();
                }
            });
            mThread = null;
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.e(TAG, "onActivityCreated: " + activity);

        if (activity.getClass().getName().equals("org.linphone.LinphoneActivity")) {
            startService(new Intent(this, RunningService.class).putExtra("launch", true));
        }
//        if (weakReference != null && activity instanceof LoginDemoActivity) {
//            if (weakReference.get() != null) {
//                weakReference.get().finish();
//                weakReference = new WeakReference<>(activity);
//                return;
//            }
//        }
//
//
//        if (activity instanceof LoginActivity) {
//            weakReference = new WeakReference<>(activity);
//        } else if (activity instanceof EasterEggActivity) {
//            egg = new WeakReference<>(activity);
//        }
//
//        if (weakReference != null && activity instanceof MainActivity) {
//            weakReference.get().finish();
//            weakReference = null;
//        }


    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
