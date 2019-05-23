package org.linphone.bean;

import android.util.Log;

import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Map;

public class NimUserInfoWrapper implements NimUserInfo {

    private char pinyinChar;
    private NimUserInfo userInfo;

    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public NimUserInfoWrapper(NimUserInfo userInfo) {
        Log.e("NimUserInfoWrapper: ", userInfo.getName());
        this.userInfo = userInfo;
        if (Character.isDigit(userInfo.getName().toCharArray()[0]))
            pinyinChar = '#';
        else {
            String name = userInfo.getName().toUpperCase().substring(0,1);

            for (char i = 'A'; i <= 'Z'; i++) {
                if (name.toCharArray()[0]==i){
                    pinyinChar=i;
                    return;
                }
            }

            pinyinChar = PinyinHelper.toHanyuPinyinStringArray(userInfo.getName().substring(0, 1).toCharArray()[0])[0].substring(0, 1).toUpperCase().toCharArray()[0];
        }
    }

    public char getPinyinChar() {
        return pinyinChar;
    }

    public NimUserInfo getUserInfo() {

        return userInfo;
    }

    public void setUserInfo(NimUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String getSignature() {
        return userInfo.getSignature();
    }

    @Override
    public GenderEnum getGenderEnum() {
        return userInfo.getGenderEnum();
    }

    @Override
    public String getEmail() {
        return userInfo.getEmail();
    }

    @Override
    public String getBirthday() {
        return userInfo.getBirthday();
    }

    @Override
    public String getMobile() {
        return userInfo.getMobile();
    }

    @Override
    public String getExtension() {
        return userInfo.getExtension();
    }

    @Override
    public Map<String, Object> getExtensionMap() {
        return userInfo.getExtensionMap();
    }

    @Override
    public String getAccount() {
        return userInfo.getAccount();
    }

    @Override
    public String getName() {
        return userInfo.getName();
    }

    @Override
    public String getAvatar() {
        return userInfo.getAvatar();
    }
}
