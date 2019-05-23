package org.linphone.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.linphone.R;


/**
 * Created by miao on 2018/10/24.
 */
public class DialogCustomUtils {
    public static AlertDialog create(View view, PostAction post, View.OnClickListener nest) {
        AlertDialog builder = new AlertDialog.Builder(view.getContext())
                .create();
        voipDialogViewSet(view, post, nest);
        builder.setView(view);
        return builder;
    }


    public static void voipDialogViewSet(View view, PostAction post, View.OnClickListener nest) {
        EditText text = view.findViewById(R.id.mPassword);
        TextView confirm = view.findViewById(R.id.mConfirm);
        TextView cancel = view.findViewById(R.id.mCancel);

        cancel.setOnClickListener((view1) -> {
            nest.onClick(view1);
            text.getText().clear();
        });
        confirm.setOnClickListener((view1) -> {
            post.post(text);
        });


    }


    public static AlertDialog randomNumDialog(Context context, String tel, AlertDialog.OnClickListener post, AlertDialog.OnClickListener cancel, AlertDialog.OnClickListener negative) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("随机号码获取成功")
                .setCancelable(false)
                .setMessage("是否使用此号码:\n" + tel)
                .setPositiveButton("换一个", post)
                .setNeutralButton("是", negative)
                .setNegativeButton("否", cancel);
        return builder.create();
    }

    public static ProgressDialog progressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在设置主叫号码...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static ProgressDialog progressDialog(Context context, String msg) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static AlertDialog inputCustomDialog(Context context, EditText view, AlertDialog.OnClickListener post, AlertDialog.OnClickListener cancel) {
        EditText e = new EditText(context);
        e.setHint("主叫号码：");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("请输入自定义号码：")
                .setCancelable(false)
                .setView(view)
                .setPositiveButton("是", post)
                .setNegativeButton("否", cancel);
        return builder.create();
    }

    public static AlertDialog inputDialog(Context context, PostString postString, PostCancel cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        EditText e = new EditText(context);
        e.setHint("主叫号码：");
        builder.setTitle("请输入自定义号码")
                .setView(e)
                .setCancelable(false)
                .setPositiveButton(R.string.confirm, (v, v1) -> {
                    String stel = e.getText().toString();
                    e.setText("");
                    postString.post(stel);
                })
                .setNegativeButton(R.string.cancel, (v, vv) -> cancel.post());
        return builder.create();

    }

    public static AlertDialog exitDialog(Context context, PostCancel invoke) {
        return new AlertDialog.Builder(context)
                .setMessage("是否退出应用")
                .setTitle("提示")
                .setPositiveButton("退出", (v, vv) -> invoke.post())
                .setNegativeButton(R.string.cancel, (v, vv) -> {
                })
                .create();


    }

    public static AlertDialog exitCustomDialog(Context context, AlertDialog.OnClickListener post, AlertDialog.OnClickListener negative) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示")
                .setCancelable(false)
                .setMessage("是否退出应用")
                .setNeutralButton("退出", post)
                .setNegativeButton("取消", negative);
        return builder.create();
    }


    public static AlertDialog updateDialog(Context context, String versionName, String details, PostCancel download, PostCancel cancel) {
        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("软件更新提示:v" + versionName)
                .setMessage(details)
                .setPositiveButton("下载更新", (dialog, which) -> download.post())
                .setNegativeButton("忽略", ((dialog, which) -> cancel.post()))
                .create();
    }


    public static AlertDialog progressDialog(Context context, View view) {
        return new AlertDialog.Builder(context)
                .setTitle("正在更新")
                .setView(view)
                .create();
    }

    public interface PostAction {
        void post(EditText s);
    }

    public interface PostString {
        void post(String s);
    }

    public interface PostCancel {
        void post();
    }


}
