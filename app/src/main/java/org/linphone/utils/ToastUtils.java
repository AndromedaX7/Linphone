package org.linphone.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by Bowen on 2015-11-02.
 */
public class ToastUtils {

    private Toast mToast;
    private Context mContext;

    public ToastUtils(Context context){
        mContext = context;
        mToast = Toast.makeText(context,"", Toast.LENGTH_LONG);
    }

    public void showToast(String s){
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setText(s);
        mToast.show();
    }
}
