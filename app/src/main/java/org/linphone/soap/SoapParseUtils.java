package org.linphone.soap;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.LinkedList;

/**
 * 类描述：
 * Created by Harvin on 2016/2/16.
 * Modefy by Harvin on 2016/2/16.
 * 修改备注：
 */
public class SoapParseUtils
{

    /**
     * 去字符串头
     *
     * @param result
     * @return
     * @throws Exception
     */
    public static String removeHeader(String result) throws Exception {
        // 去头
        int index_sta = result.indexOf("json=");
        int index_end = result.indexOf("; result=success");
        String json = result.substring(index_sta, index_end);
        String newJson = json.replace("json=", "");
        return newJson;
    }

}
