package org.linphone.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.linphone.R;
import org.linphone.app.App;
import org.linphone.utils.UriTofilePath;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserModifyActivity extends AppCompatActivity {

    @BindView(R.id.mUserName)
    TextView mUserName;
    @BindView(R.id.icon)
    ImageView icon;
    @BindView(R.id.mUserInfo)
    LinearLayout mIconModify;
    @BindView(R.id.code)
    TextView code;
    @BindView(R.id.mCodeModify)
    LinearLayout mCodeModify;
    @BindView(R.id.mName)
    TextView mName;
    @BindView(R.id.mNameModify)
    LinearLayout mNameModify;
    @BindView(R.id.meBack)
    ImageView meBack;

    ProgressDialog dialog;
    private NimUserInfo nimUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_modify);
        ButterKnife.bind(this);
        dialog=new ProgressDialog(this);
        dialog.setTitle("更换头像");
        dialog.setMessage("正在设置头像...");
        setData();
    }

    private void setData() {
        NIMClient.getService(UserService.class).fetchUserInfo(Arrays.asList(App.app().getLoginData().getUsername())).setCallback(new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> param) {

                ObjectAnimator anim =ObjectAnimator.ofInt(icon,"ImageLevel",0,10000);
                anim.setDuration(800);
                anim.setRepeatCount(ObjectAnimator.INFINITE);
                anim.start();

                nimUserInfo = param.get(0);
                if (nimUserInfo.getAvatar() != null)
                    Log.e("onSuccess: ", nimUserInfo.getAvatar());
                mName.setText(nimUserInfo.getName());
                code.setText(nimUserInfo.getAccount());
                if (!TextUtils.isEmpty(nimUserInfo.getAccount())) {
//                    Glide.with(UserModifyActivity.this).load(R.mipmap.ic_loading).error(R.mipmap.ic_launcher2) .into(icon);
                    Glide.with(UserModifyActivity.this).load(nimUserInfo.getAvatar()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            anim.cancel();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (resource instanceof GifDrawable){
                                ((GifDrawable)resource).setLoopCount(2);
                            }
                            anim.cancel();
                            return false;
                        }
                    }).error(R.mipmap.ic_launcher2).placeholder(R.drawable.rotate_loading).into(icon);
                } else {
                    anim.cancel();
                    Glide.with(UserModifyActivity.this).load(R.mipmap.ic_launcher2).into(icon);
                }
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });

    }

    @OnClick(R.id.meBack)
    void back() {
        finish();
    }

    @OnClick(R.id.mUserInfo)
    void onIconSelected() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        startActivityForResult(intent, req_images);
    }

    @OnClick(R.id.mNameModify)
    void onNickNameModify() {
        Intent intent = new Intent(this, UserNameModifyActivity.class);
        startActivityForResult(intent, req_name);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == req_images) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor actualimagecursor = getContentResolver().query(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
            if (img_path == null) {
                img_path = UriTofilePath.getFilePathByUri(this, uri);
            }
            File file = new File(img_path);
            dialog.show();

//            Toast.makeText(this, file.toString(), Toast.LENGTH_SHORT).show();
            NIMClient.getService(NosService.class).upload(file, "image/*").setCallback(new RequestCallback() {
                @Override
                public void onSuccess(Object param) {
                    Log.e("TAG", "onSuccess: " + param.toString());
                    HashMap<UserInfoFieldEnum, Object> map = new HashMap<>();
                    map.put(UserInfoFieldEnum.AVATAR, param.toString());
                    NIMClient.getService(UserService.class).updateUserInfo(map).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
//                            Glide.with(UserModifyActivity.this).load(nimUserInfo.getAvatar()).error(R.mipmap.ic_launcher2)./*placeholder(R.mipmap.ic_launcher2).*/into(icon);
                            setData();
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailed(int code) {

                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });

                }

                @Override
                public void onFailed(int code) {

                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        } else if (resultCode == Activity.RESULT_OK && requestCode == req_name) {
            setData();
        }
    }

    private static int req_images = 100;
    private static int req_name = 101;
}
