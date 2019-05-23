package org.linphone.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.linphone.app.App;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author fty 一个shareprefrence对象 名称为cookie 模拟web的cookie来方便存储一些持久化数据 2015-03-11
 * 15:45:45
 */
public class MyCookie {
    private static SharedPreferences cookie = App.app()
            .getSharedPreferences("cookie", Context.MODE_PRIVATE);
    private static SharedPreferences.Editor cookieEditor = cookie.edit();

    public static void removeCookie() {
        cookieEditor.clear().commit();
    }

    public static void putString(String key, String value) {
        cookieEditor.putString(key, value);
        cookieEditor.commit();
    }

    public static void removeString(String key) {
        cookieEditor.remove(key);
        cookieEditor.commit();
    }

    public static void putLong(String key, Long value) {
        cookieEditor.putLong(key, value);
        cookieEditor.commit();
    }

    public static String getString(String key, String defaultValue) {
        if (cookie.contains(key))
            return cookie.getString(key, defaultValue);
        return defaultValue;
    }

    public static Long getLong(String key, Long defaultValue) {
        if (cookie.contains(key))
            return cookie.getLong(key, defaultValue);
        return defaultValue;
    }

    public static void managerAccount(String account, String password) {
        String[] accountArrs = MyCookie.getString("accountArrs", "").split(",");

        ArrayList<String> accounts = new ArrayList<>(Arrays.asList(accountArrs));
        if (!accounts.contains(account)) {
            String acc = account;
            for (int j = 0; j < accountArrs.length; j++) {
                acc = acc + "," + accountArrs[j];
            }
            MyCookie.putString("accountArrs", acc);
            MyCookie.putString(account, password);
        }else {
            MyCookie.putString(account,password);
        }
    }

}
