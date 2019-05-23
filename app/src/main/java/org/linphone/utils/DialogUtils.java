package org.linphone.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Created by Bowen on 2015-11-02.
 */
public class DialogUtils {

    private ProgressDialog mProgressDialog;
    private Context mContext;

    public DialogUtils(Context context) {

        mContext = context;
        mProgressDialog = new ProgressDialog(context);
    }

    public static void tips(Context context, DialogInterface.OnClickListener post) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage("语音消息是否发送")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("发送", post)
                .setCancelable(false)
                .create();
        dialog.show();
    }

    public void showProgressDialog(boolean show, String message) {
        if (show) {
            mProgressDialog.setMessage(message);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            Point size = new Point();
            mProgressDialog.getWindow().getWindowManager().getDefaultDisplay().getSize(size);
            int width = size.x;//获取界面的宽度像素
            int height = size.y;
            WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();
            //一定要用mProgressDialog得到当前界面的参数对象，否则就不是设置ProgressDialog的界面了
//            params.alpha = 0.8f;//设置进度条背景透明度
            params.height = height / 5;//设置进度条的高度
            params.gravity = Gravity.CENTER;//设置ProgressDialog的重心
            params.width = 4 * width / 5;//设置进度条的宽度<span style="color:#ff0000;">params.dimAmount = 0f;</span>
            // 设置半透明背景的灰度，范围0~1，系统默认值是0.5，1表示背景完全是黑色的,0表示背景不变暗，
            // 和原来的灰度一样
            mProgressDialog.getWindow().setAttributes(params);
            mProgressDialog.setCancelable(false);//设置进度条是否可以按退回键取消
            // 把参数设置给进度条，注意，一定要先show出
            //  mProgressDialog.show();
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            // mProgressDialog.hide();
        }
    }

    public void showProgressDialog(boolean show) {
        showProgressDialog(show, "正在加载...");
    }

    public void dismiss() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public static ProgressDialog progressDialog(Context context, String msg) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}
