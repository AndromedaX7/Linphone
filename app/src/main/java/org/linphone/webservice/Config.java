package org.linphone.webservice;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Config {
    public static final String SERVICE_URL = "http://www.freetk.cn:10006/ws/bussService?wsdl";
    public static final String SERVICE_NAME_SPACE = "http://impl.service.jit.com/";

    /**
     * get version number
     *
     * @param context
     * @return
     */
    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO: handle exception
            //Log.e(TAG, e.getMessage());
        }
        return verCode;
    }

    /**
     * get version number
     *
     * @param context
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // TODO: handle exception
            //Log.e(TAG, e.getMessage());
        }
        return verName;
    }

    /**
     * get apkname
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String verName = context.getResources().getText(R.string.app_name).toString();
        return verName;
    }
}