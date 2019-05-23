package org.linphone.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * 读取assets下的txt文件，返回utf-8 String
     *
     * @param context
     * @param fileName 不包括后缀
     * @return
     */
    public static List<String> readAssetsTxt(Context context, String fileName) {
        List<String> list = new ArrayList<String>();
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = context.getAssets().open(fileName + ".txt");


            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer buffer = new StringBuffer("");
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                list.add(str);
            }
            reader.close();
            is.close();
//            int size = is.available();
//            // Read the entire asset into a local byte buffer.
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            // Convert the buffer into a string.
//            String text = new String(buffer, "utf-8");
//            while ((line = is.readLine()) != null) {
//                sb.append("\n" + line);
//            }
            // Finally stick the string into the text view.
//            return text;
        } catch (IOException e) {
            // Should never happen!
//            throw new RuntimeException(e);
            list = null;
            e.printStackTrace();
        }
        //  return "读取错误，请检查文件名";
        return list;
    }

    /**
     * 按行读取txt
     *
     * @param is
     * @return
     * @throws Exception
     */
    private String readTextFromSDcard(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuffer buffer = new StringBuffer("");
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
            buffer.append("\n");
        }
        return buffer.toString();
    }

}
