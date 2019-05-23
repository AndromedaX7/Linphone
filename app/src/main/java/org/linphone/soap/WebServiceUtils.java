package org.linphone.soap;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.linphone.bean.LoginInfoOut;
import org.linphone.webservice.Config;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebServiceUtils {
    public interface CallBack {
        void result(String result);
    }

    public static void getPersonDeptName(final String methodName, final String arg, final CallBack callBack) {
        // 用于子线程与主线程通信的Handler
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 将返回值回调到callBack的参数中
                callBack.result((String) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 命名空间
                String nameSpace = Config.SERVICE_NAME_SPACE;
                // 调用的方法名称
                //  String methodName = methodNames;
                // EndPoint
                String endPoint = Config.SERVICE_URL;
                SoapObject request = new SoapObject(nameSpace, methodName);

                if (!TextUtils.isEmpty(arg)) {
                    PropertyInfo propertyInfo = new PropertyInfo();
                    propertyInfo.setType(String.class);
                    propertyInfo.setName("arg0");
                    propertyInfo.setValue(arg);
                    request.addProperty(propertyInfo);

                }
                Log.e("newInstanceCall: ", "propertyInfo1:" + arg);
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                try {
                    HttpTransportSE httpTransportSE = new HttpTransportSE(endPoint);
                    httpTransportSE.call(nameSpace + methodName, envelope);
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    Log.e("newInstanceCall: ", "response1:" + response.toString());
                    mHandler.sendMessage(mHandler.obtainMessage(1, response.toString()));
                } catch (Exception e) {
                    mHandler.sendMessage(mHandler.obtainMessage(-1, e.getMessage()));
                }
            }
        }).start();
    }

    public static void getPersonDeptNameTo(final String methodName, final String arg, final CallBack callBack) {
        // 用于子线程与主线程通信的Handler
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 将返回值回调到callBack的参数中
                callBack.result((String) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SoapObject request = new SoapObject("http://impl.webservice.jws.jeecgframework.org/", methodName);
                    String url = "http://www.freetk.cn:8789/cxf/BussService?wsdl";
                   // if (!TextUtils.isEmpty(arg)) {
                        PropertyInfo propertyInfo = new PropertyInfo();
                        propertyInfo.setType(String.class);
                        propertyInfo.setName("arg0");
                        propertyInfo.setValue(arg);
                        request.addProperty(propertyInfo);
                 //   }
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE httpTransportSE = new HttpTransportSE(url);
                    httpTransportSE.call(null, envelope);
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    Log.e("newInstanceCall: ", "response2:" + response.toString());
                    mHandler.sendMessage(mHandler.obtainMessage(1, response.toString()));
                } catch (Exception ex) {
                    mHandler.sendMessage(mHandler.obtainMessage(-1, ex.getMessage()));
                }
            }
        }).start();
    }


    public static void newInstanceCall(final String phone, final CallBack callBack) {
        // 用于子线程与主线程通信的Handler
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 将返回值回调到callBack的参数中
                callBack.result((String) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String url = "http://www.freetk.cn:8789/cxf/SecretPhoneWebServcie?wsdl";
                    SoapObject request = new SoapObject("http://webservice.jws.jeecgframework.org/", "findPhonePlace");
                    PropertyInfo propertyInfo = new PropertyInfo();
                    propertyInfo.setType(String.class);
                    propertyInfo.setName("arg0");
                    propertyInfo.setValue("{\"mobileNum\":\"" + phone + "\"}");
                    Log.e("newInstanceCall: ", "propertyInfo2:" + propertyInfo.getValue().toString());
                    request.addProperty(propertyInfo);
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE httpTransportSE = new HttpTransportSE(url);
                    httpTransportSE.call(null, envelope);
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    // System.out.println("newInstanceCall2: " + response.toString());
                    Log.e("newInstanceCall: ", "response2:" + response.toString());
                    mHandler.sendMessage(mHandler.obtainMessage(1, response.toString()));
                } catch (Exception ex) {
                    mHandler.sendMessage(mHandler.obtainMessage(-1, ex.getMessage()));
                }
            }
        }).start();


    }

    public static void Login(String account, String password, String mac, String ip, final CallBack callBack) {
        // 用于子线程与主线程通信的Handler
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 将返回值回调到callBack的参数中
                callBack.result((String) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SoapObject request = new SoapObject("http://webservice.jws.jeecgframework.org/", "findUserInfo");
                    String url = "http://www.freetk.cn:8789/cxf/SecretPhoneWebServcie?wsdl";
                    PropertyInfo propertyInfo = new PropertyInfo();
                    propertyInfo.setType(String.class);
                    propertyInfo.setName("arg0");
                    propertyInfo.setValue(new Gson().toJson(new LoginInfoOut(account, password, mac, ip)));
                    System.out.println(propertyInfo.getValue());
                    request.addProperty(propertyInfo);
                    Log.e("newInstanceCall: ", "propertyInfo3:" + propertyInfo.getValue().toString());
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE httpTransportSE = new HttpTransportSE(url);
                    httpTransportSE.call(null, envelope);
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    Log.e("newInstanceCall: ", "response2:" + response.toString());
                    mHandler.sendMessage(mHandler.obtainMessage(1, response.toString()));
                } catch (Exception ex) {
                    mHandler.sendMessage(mHandler.obtainMessage(-1, ex.getMessage()));
                }
            }
        }).start();


    }


}
