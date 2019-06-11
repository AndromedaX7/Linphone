package org.linphone;
/*
DialerFragment.java
Copyright (C) 2012  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.activity.DialFragment;
import org.linphone.activity.LoginDemoActivity;
import org.linphone.app.App;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;
import org.linphone.soap.SoapParams;
import org.linphone.soap.WebServiceUtils;
import org.linphone.ui.AddressAware;
import org.linphone.ui.AddressText;
import org.linphone.ui.CallButton;
import org.linphone.ui.EraseButton;
import org.linphone.utils.DialogCustomUtils;
import org.linphone.utils.DialogUtils;
import org.linphone.utils.FastJsonHelper;
import org.linphone.utils.JsonParseUtils;
import org.linphone.utils.MyCookie;
import org.linphone.utils.Utils;
import org.linphone.webservice.MyCallBack;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Messenger;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static android.content.Intent.ACTION_MAIN;

/**
 * @author Sylvain Berfini
 */
public class DialerFragment extends Fragment {
    private static DialerFragment instance;
    private static boolean isCallTransferOngoing = false;
    private TextView mDialSettings;
    private TextView mOutPutNum;
    private ImageView call_exit;
    private TextView mReplace;
    private TextView mOutPutWhere;
    private AddressAware numpad;
    private AddressText mAddress;
    private CallButton mCall;
    private ImageView mAddContact;
    private OnClickListener addContactListener, cancelListener, transferListener;
    private boolean shouldEmptyAddressField = true;
    private AlertDialog inputCustomDialog;
    private static Session session;
    private static ChannelShell channelSell;
    private static List<String> commands;
    private DialogUtils mProgressDialogUtils;
    private ProgressDialog progressDialog;
    private String account;
    PopupWindow mPopupWindow;
    private String lastMode;
    private Messenger server;
    private Messenger client;
    private boolean IsPutNum;
    View view;
    private LinphoneCoreListenerBase mListener;
    private boolean isInCall, isAttached = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.dialerto, container, false);
        mDialSettings = (TextView) view.findViewById(R.id.mDialSettings);
        mOutPutNum = (TextView) view.findViewById(R.id.mOutPutNum);
        call_exit = (ImageView) view.findViewById(R.id.call_exit);
        mReplace = (TextView) view.findViewById(R.id.mReplace);
        mOutPutWhere = (TextView) view.findViewById(R.id.mOutPutWhere);
        mAddress = (AddressText) view.findViewById(R.id.address);
        mAddress.setDialerFragment(this);
        account = App.app().getLoginData().getUsername();
        mProgressDialogUtils = new DialogUtils(getActivity());
        //initSsh();
        EraseButton erase = (EraseButton) view.findViewById(R.id.erase);
        erase.setAddressWidget(mAddress);
        mCall = (CallButton) view.findViewById(R.id.call);
        mCall.setAddressWidget(mAddress);
        mCall.setExternalClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (IsPutNum) {
                    try {
                        if (TextUtils.isEmpty(mAddress.getText().toString())) {
                            Toast.makeText(getActivity(), "拨打号码不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<String> list = Utils.readAssetsTxt(getActivity(), "phone");
                        String tag = MyCookie.getString("mDialSettingsTag", "");

                        if (tag.equals("2")) {
                            if (mAddress.getText().toString().length() <= 4) {
                                Toast.makeText(getActivity(), "随机号拨号方式不能拨打集团号码", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        if (tag.equals("3")) {
                            if (mAddress.getText().toString().length() <= 4) {
                                Toast.makeText(getActivity(), "自定义拨号方式不能拨打集团号码", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        if (tag.equals("4") && list != null) {
                            boolean falg = false;
                            for (String str : list) {
                                if (str.equals(mAddress.getText().toString())) {
                                    falg = true;
                                    break;
                                }
                            }
                            if (!falg) {
                                Toast.makeText(getActivity(), "此拨号方式只能拨打集团号码", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
                            if (mAddress.getText().length() > 0) {
                                LinphoneManager.getInstance().newOutgoingCall(mAddress);
                            } else {

                                if (LinphonePreferences.instance().isBisFeatureEnabled()) {
                                    LinphoneCallLog[] logs = LinphoneManager.getLc().getCallLogs();
                                    LinphoneCallLog log = null;
                                    for (LinphoneCallLog l : logs) {
                                        if (l.getDirection() == CallDirection.Outgoing) {
                                            log = l;
                                            break;
                                        }
                                    }
                                    if (log == null) {
                                        return;
                                    }

                                    LinphoneProxyConfig lpc = LinphoneManager.getLc().getDefaultProxyConfig();
                                    if (lpc != null && log.getTo().getDomain().equals(lpc.getDomain())) {
                                        mAddress.setText(log.getTo().getUserName());
                                    } else {
                                        mAddress.setText(log.getTo().asStringUriOnly());
                                    }
                                    mAddress.setSelection(mAddress.getText().toString().length());
                                    mAddress.setDisplayedName(log.getTo().getDisplayName());
                                }
                            }
                        }
                    } catch (LinphoneCoreException e) {
                        LinphoneManager.getInstance().terminateCall();

                    }
                } else {
                    Toast.makeText(getActivity(), "请先设置拨号方式", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        if (LinphoneActivity.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
            if (isCallTransferOngoing) {
                mCall.setImageResource(R.drawable.call_transfer);
            } else {
                mCall.setImageResource(R.drawable.call_add);
            }
        } else {
            mCall.setImageResource(R.drawable.call_audio_start);
        }

        numpad = (AddressAware) view.findViewById(R.id.numpad);
        if (numpad != null) {
            numpad.setAddressWidget(mAddress);
        }

        mAddContact = (ImageView) view.findViewById(R.id.add_contact);
        mAddContact.setEnabled(!(LinphoneActivity.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0));

        addContactListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinphoneActivity.instance().displayContactsForEdition(mAddress.getText().toString());
            }
        };
        cancelListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinphoneActivity.instance().resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        };
        transferListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinphoneCore lc = LinphoneManager.getLc();
                if (lc.getCurrentCall() == null) {
                    return;
                }
                lc.transferCall(lc.getCurrentCall(), mAddress.getText().toString());
                isCallTransferOngoing = false;
                LinphoneActivity.instance().resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        };

        resetLayout(isCallTransferOngoing);

        if (getArguments() != null) {
            shouldEmptyAddressField = false;
            String number = getArguments().getString("SipUri");
            String displayName = getArguments().getString("DisplayName");
            String photo = getArguments().getString("PhotoUri");
            mAddress.setText(number);
            if (displayName != null) {
                mAddress.setDisplayedName(displayName);
            }
            if (photo != null) {
                mAddress.setPictureUri(Uri.parse(photo));
            }
        }

        try {
            account = App.app().getLoginData().getUsername();
            String mDialSettingsTag = MyCookie.getString("mDialSettingsTag", "");
            String mDialSettingsText = MyCookie.getString("mDialSettingsText", "");
            String mOutPutWhereText = MyCookie.getString("mOutPutWhereText", "");
            String mOutPutNumText = MyCookie.getString("mOutPutNumText", "");


            if (!TextUtils.isEmpty(mDialSettingsText)) {
                mDialSettings.setTag(Integer.parseInt(mDialSettingsTag));
                mDialSettings.setText(mDialSettingsText);
                mReplace.setVisibility(mDialSettingsTag.equals("2") || mDialSettingsTag.equals("3") ? View.VISIBLE : View.GONE);
            }
            if (!TextUtils.isEmpty(mOutPutWhereText)) {
                mOutPutWhere.setText(mOutPutWhereText);
            }
            if (!TextUtils.isEmpty(mOutPutNumText)) {
                mOutPutNum.setText(mOutPutNumText);
                mReplace.setVisibility(View.VISIBLE);
            } else {
                mOutPutNum.setText("");
                mReplace.setVisibility(View.GONE);
            }
            if (Integer.parseInt(mDialSettingsTag) == 1) {
                // requestCustomNumber(mOutPutNumText, 1);
            } else if (Integer.parseInt(mDialSettingsTag) == 3) {
                requestCustomNumber(mOutPutNumText, 3);
            } else if (Integer.parseInt(mDialSettingsTag) == 2) {
                requestCustomNumber(mOutPutNumText, 2);
            } else if (Integer.parseInt(mDialSettingsTag) == 4) {
                mReplace.setVisibility(View.GONE);
                mOutPutNumText = App.app().getLoginData().getUsername();
                mOutPutNum.setText(mOutPutNumText);
                requestCustomNumber(mOutPutNumText, 4);
            }
        } catch (Exception ex) {

        }

        showPop();
        instance = this;
        setListener();
        test();
        isAttached = true;
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }

    private void test() {

        mListener = new LinphoneCoreListenerBase() {
            @Override
            public void registrationState(final LinphoneCore lc, final LinphoneProxyConfig proxy, final LinphoneCore.RegistrationState state, String smessage) {
                if (!isAttached || !LinphoneService.isReady()) {
                    return;
                }

                if (lc.getProxyConfigList() == null) {
                    //statusLed.setImageResource(R.drawable.led_disconnected);
                    //statusText.setText(getString(R.string.no_account));
                } else {
                    //statusLed.setVisibility(View.VISIBLE);
                }

                if (lc.getDefaultProxyConfig() != null && lc.getDefaultProxyConfig().equals(proxy)) {
                    //statusLed.setImageResource(getStatusIconResource(state, true));
                    //	statusText.setText(getStatusIconText(state));
                    try {
                        boolean defaultAccountConnected = (lc != null && lc.getDefaultProxyConfig() != null && lc.getDefaultProxyConfig().isRegistered());
                        if (state == LinphoneCore.RegistrationState.RegistrationOk && defaultAccountConnected) {
                            //  Toast.makeText(getActivity(), "RegistrationOk", Toast.LENGTH_LONG).show();
                        } else if (state == LinphoneCore.RegistrationState.RegistrationProgress) {
                            //  Toast.makeText(getActivity(), "RegistrationProgress", Toast.LENGTH_LONG).show();
                        } else if (state == LinphoneCore.RegistrationState.RegistrationFailed) {
                            //  Toast.makeText(getActivity(), "RegistrationFailed", Toast.LENGTH_LONG).show();
                            lc.refreshRegisters();
                        } else {
                            //  Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG).show();
                            lc.refreshRegisters();
                        }
                    } catch (Exception e) {
                        Log.e(e);
                    }

                } else if (lc.getDefaultProxyConfig() == null) {
                    //	statusLed.setImageResource(getStatusIconResource(state, true));
                    //	statusText.setText(getStatusIconText(state));
                }

//				try {
//					statusText.setOnClickListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							lc.refreshRegisters();
//						}
//					});
//				} catch (IllegalStateException ise) {}
            }

            @Override
            public void notifyReceived(LinphoneCore lc, LinphoneEvent ev, String eventName, LinphoneContent content) {

                if (!content.getType().equals("application")) {
                    return;
                }
                if (!content.getSubtype().equals("simple-message-summary")) {
                    return;
                }

                if (content.getData() == null) {
                    return;
                }

                int unreadCount = -1;
                String data = content.getDataAsString();
                String[] voiceMail = data.split("voice-message: ");
                final String[] intToParse = voiceMail[1].split("/", 0);

                unreadCount = Integer.parseInt(intToParse[0]);
                if (unreadCount > 0) {
//					voicemailCount.setText(unreadCount);
//					voicemail.setVisibility(View.VISIBLE);
//					voicemailCount.setVisibility(View.VISIBLE);
                } else {
//					voicemail.setVisibility(View.GONE);
//					voicemailCount.setVisibility(View.GONE);
                }
            }

        };
    }

    private void initSsh() {
        commands = new ArrayList<String>();
        commands.add("export DISPLAY=:0 ");
        String url = "15662205182";
        commands.add("config modify subscriber dn 4089 longdn " + url);
        mProgressDialogUtils.showProgressDialog(true);
        new AsyncSession().execute();

    }

    private void setListener() {
        mDialSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop();
                mPopupWindow.showAsDropDown(mDialSettings);
            }
        });

        mReplace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) mDialSettings.getTag();
                switch (tag) {
//                case 1:
                    case 2:
                        requestRandomNumber();
                        break;
                    case 3:
                        setCustomNumber();
                        break;
//                case 4:
                }

            }
        });
        call_exit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog == null)
                    dialog = DialogCustomUtils.exitCustomDialog(getActivity(), new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
//                        startActivity(new Intent().setClass(this, LinphoneActivity.class).putExtra("isNewProxyConfig", true));

                            // startActivity(new Intent().setClass(getActivity(), LoginDemoActivity.class).putExtra("isNewProxyConfig", true));
                            NIMClient.getService(AuthService.class).logout();
                            getContext().startService(new Intent(getActivity(), RunningService.class).putExtra("launch", false));
                            getContext().stopService(new Intent(getActivity(), RunningService.class).putExtra("launch", false));
                            getActivity().stopService(new Intent(ACTION_MAIN).setClass(getActivity(), LinphoneService.class));
                            getActivity().finish();
                            Process.killProcess(Process.myPid());

                        }
                    }, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                if (!dialog.isShowing())
                    dialog.show();
            }
        });
    }

    private AlertDialog dialog;

    public void showPop() {

        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.popup_dial_setting, null);
        mPopupWindow = new PopupWindow(inflate, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        TextView mUnSetting = inflate.findViewById(R.id.mUnSetting);
        LinearLayout mRandomNumber = inflate.findViewById(R.id.mRandomNumber);
        LinearLayout mCustomNumber = inflate.findViewById(R.id.mCustomNumber);
        TextView unitNumber = inflate.findViewById(R.id.unitNumber);
        String phone = "";
        if (App.app().getLoginData() != null) {
            String[] temp = App.app().getLoginData().getPower().split(",");
            if (temp != null) {
                for (int i = 0; i < temp.length; i++) {
                    if (temp[i].equals("1")) {
                        // mUnSetting.setVisibility(View.VISIBLE);
                    }
                    if (temp[i].equals("2")) {
                        mRandomNumber.setVisibility(View.VISIBLE);
                    }
                    if (temp[i].equals("3")) {
                        mCustomNumber.setVisibility(View.VISIBLE);
                    }
                    if (temp[i].equals("4")) {
                        unitNumber.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
//        int tag = 0;
//        switch (tag) {
//            case 0:
//                mDialSettings.setText("未设置");
//                mOutPutNum.setText("");
//                break;
//            case 2:
//                mDialSettings.setText("随机号码");
//                mOutPutNum.setText(phone);
//                break;
//            case 3:
//                mDialSettings.setText("自定义");
//                mOutPutNum.setText(phone);
//                break;
//            case 4:
//                mDialSettings.setText("集团号码");
//                mOutPutNum.setText(phone);
//                break;
//        }


        mUnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialSettings.setText(R.string.un_settings);
                requestUndefined();
                mPopupWindow.dismiss();
            }
        });
        mRandomNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastMode = mDialSettings.getText().toString();
                requestRandomNumber();
//                mDialSettings.setText(R.string.random_number);
                mPopupWindow.dismiss();
            }
        });
        mCustomNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                lastMode = mDialSettings.getText().toString();
//                mDialSettings.setText(R.string.custom);
                setCustomNumber();
                mPopupWindow.dismiss();
            }
        });
        unitNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastMode = mDialSettings.getText().toString();
//                mDialSettings.setTag(4);
//                mDialSettings.setText(R.string.unit_number);
//                if (progressDialog == null) {
//                    progressDialog = DialogCustomUtils.progressDialog(getActivity());
//                }
//                progressDialog.show();
                requestUnitNumber();
                mPopupWindow.dismiss();
            }
        });
//        mDialSettings.setTag(tag);
//        lastMode = mDialSettings.getText().toString();
//        mReplace.setVisibility(tag == 2 || tag == 3 ? View.VISIBLE : View.GONE);
    }

    /**
     * config modify subscriber dn 4001 longdn 84700123。其中dn 4001为客户登陆时所使用的账号，longdn 84700123为客户在设置界面中指定的号段内随机生成的号码
     */
    private void requestRandomNumber() {

        mProgressDialogUtils.showProgressDialog(true);
        SoapParams params = new SoapParams();
        WebServiceUtils.getPersonDeptNameTo("loadTelno", "", new WebServiceUtils.CallBack() {
            @Override
            public void result(String result) {
                mProgressDialogUtils.showProgressDialog(false);
                if (JsonParseUtils.jsonToBoolean(result)) {
                    String tel = JSON.parseObject(result).getString(
                            "tel");
                    DialogCustomUtils.randomNumDialog(getActivity(), tel, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            requestRandomNumber();
                        }
                    }, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }, new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            requestCustomNumber(tel, 2);
                            mPopupWindow.dismiss();
                            mDialSettings.setTag(2);
                            mDialSettings.setText("随机号码");
                        }
                    }).show();

                }

            }
        });

    }

    /**
     * config modify subscriber dn 4001 longdn 13912345678。其中dn 4001为客户登陆时所使用的账号，longdn 13912345678为客户指定的主叫号码
     *
     * @param code
     */
    private void requestCustomNumber(String code, int tag) {
        String dn = account;
        String longdn = code;
        String preData = "{\"dn\":\"$1\",\"tel\":\"$2\",\"userId\":\"$3\"}";
        int sTag = tag;
        preData = preData.replace("$1", dn);
        preData = preData.replace("$2", longdn);
        preData = preData.replace("$3", dn);
        String finalPreData = preData;
        SoapParams params = new SoapParams();
        params.put("arg0", finalPreData);
        mProgressDialogUtils.showProgressDialog(true, "正在设置拨号方式，请勿操作");
        WebServiceUtils.getPersonDeptNameTo("setting", finalPreData, new WebServiceUtils.CallBack() {
            @Override
            public void result(String result) {
                android.util.Log.e(TAG, "result: ."+result );
                mProgressDialogUtils.showProgressDialog(false);
                if (JsonParseUtils.jsonToBoolean(result)) {
                    Toast.makeText(getActivity(), "设置成功,请拨打电话！", Toast.LENGTH_SHORT).show();
                    IsPutNum = true;
                    mOutPutNum.setText(code);
                    mDialSettings.setTag(tag);
                    if (tag == 4) {
                        mReplace.setVisibility(View.GONE);
                    } else {
                        mReplace.setVisibility(View.VISIBLE);
                    }
                    MyCookie.putString("mOutPutNumText", code);
                    if (tag == 2) {
                        MyCookie.putString("mDialSettingsTag", "2");
                        MyCookie.putString("mDialSettingsText", "随机号码");
                        mDialSettings.setText("随机号码");
                        //随机号码
                    } else if (tag == 3) {
                        //自定义
                        MyCookie.putString("mDialSettingsTag", "3");
                        MyCookie.putString("mDialSettingsText", "自定义");
                        mDialSettings.setText("自定义");
                    } else if (tag == 4) {
                        //自定义
                        MyCookie.putString("mDialSettingsTag", "4");
                        MyCookie.putString("mDialSettingsText", "集团号码");
                        mDialSettings.setText("集团号码");
                    }

                    if (code.length() > 8) {
                        setPhoneCity(code);
                    } else {
                        MyCookie.putString("mOutPutWhereText", "本地号码");
                        mOutPutWhere.setText("本地号码");
                    }
                } else {
                    Toast.makeText(getActivity(), "网络异常,请重新设置！", Toast.LENGTH_SHORT).show();

                }
            }

        });
    }

    private void setPhoneCity(String phone) {
        try {
            WebServiceUtils.newInstanceCall(phone, new WebServiceUtils.CallBack() {
                @Override
                public void result(String result) {
                    try {
                        if (JsonParseUtils.jsonToBoolean(result)) {
                            String obj = JSON.parseObject(result).getString(
                                    "obj");
                            String province = JSON.parseObject(obj).getString(
                                    "province");
                            String city = JSON.parseObject(obj).getString(
                                    "city");
                            String areas = JSON.parseObject(obj).getString(
                                    "areas");
                            String operators = JSON.parseObject(obj).getString(
                                    "operators");
                            String str = province + " " + city + " " + operators;
                            mOutPutWhere.setText(str);
                            MyCookie.putString("mOutPutWhereText", str);
                        }
                    } catch (Exception ex) {
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCustomNumber() {
        EditText e = new EditText(getActivity());
        DialogCustomUtils.inputCustomDialog(getActivity(), e, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String code = e.getText().toString();
                if (!TextUtils.isEmpty(code) && code.length() > 4) {
                    requestCustomNumber(code, 3);
                    mDialSettings.setTag(3);
                    mDialSettings.setText("自定义");
                } else {
                    Toast.makeText(getActivity(), "请输入正确的手机号码或电话号码", Toast.LENGTH_LONG).show();
                }

//                if (org.linphone.activity.Utils.isPhone(code) || org.linphone.activity.Utils.isMobile(code)) {
//                    System.out.println("3这是符合的");
//                    requestCustomNumber(code, 3);
//                    mDialSettings.setTag(3);
//                    mDialSettings.setText("自定义");
//                } else {
//                    Toast.makeText(getActivity(), "请输入正确的手机号码或电话号码", Toast.LENGTH_LONG).show();
//                }
            }

        }, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();

    }

    private void requestUndefined() {
        mOutPutNum.setText("");
        mReplace.setVisibility(View.GONE);
        mOutPutWhere.setText("");
        saveState(0, "");
    }

    private void saveState(int tag, String tel) {

//        ContentValues contentValues = new ContentValues();
//        contentValues.put("account", App.app().getAccountData().getAccount());
//        contentValues.put("tag", tag);
//        contentValues.put("phone", tel);
//        if (getContext().getContentResolver().update(ConfigProvider.uri_account_phone, contentValues, "account=?", new String[]{
//                App.app().getAccountData().getAccount()
//        }) == 0) {
//            getContext().getContentResolver().insert(ConfigProvider.uri_account_phone, contentValues);
    }

    /**
     * config modify subscriber dn 4001 longdn 4001
     * dn 4001为客户登陆时所使用的账号，longdn 4001为客户登陆时所使用的账号
     */
    private void requestUnitNumber() {
        requestCustomNumber(account, 4);
        mDialSettings.setTag(4);
        mDialSettings.setText("集团号码");
    }

    /**
     * @return null if not ready yet
     */
    public static DialerFragment instance() {
        return instance;
    }

    @Override
    public void onPause() {
        instance = null;
        super.onPause();
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        instance = this;

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
            LinphoneProxyConfig lpc = lc.getDefaultProxyConfig();
            if (lpc != null) {
                mListener.registrationState(lc, lpc, lpc.getState(), null);
            }

//            LinphoneCall call = lc.getCurrentCall();
//            if (isInCall && (call != null || lc.getConferenceSize() > 1 || lc.getCallsNb() > 0)) {
//                if (call != null) {
//                    startCallQuality();
//                    refreshStatusItems(call, call.getCurrentParamsCopy().getVideoEnabled());
//                }
//                menu.setVisibility(View.INVISIBLE);
//                encryption.setVisibility(View.VISIBLE);
//                callQuality.setVisibility(View.VISIBLE);
//
//                // We are obviously connected
//                if(lc.getDefaultProxyConfig() == null){
//                    statusLed.setImageResource(R.drawable.led_disconnected);
//                    statusText.setText(getString(R.string.no_account));
//                } else {
//                    statusLed.setImageResource(getStatusIconResource(lc.getDefaultProxyConfig().getState(),true));
//                    statusText.setText(getStatusIconText(lc.getDefaultProxyConfig().getState()));
//                }
//            }
        }
        if (LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().selectMenu(FragmentsAvailable.DIALER);
            LinphoneActivity.instance().updateDialerFragment(this);
            LinphoneActivity.instance().showStatusBar();
            LinphoneActivity.instance().hideTabBar(false);
        }

//        boolean isOrientationLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//        if (isOrientationLandscape && !getResources().getBoolean(R.bool.isTablet)) {
//            ((LinearLayout) numpad).setVisibility(View.GONE);
//        } else {
//            ((LinearLayout) numpad).setVisibility(View.VISIBLE);
//        }
//
//        if (shouldEmptyAddressField) {
//            mAddress.setText("");
//        } else {
//            shouldEmptyAddressField = true;
//        }
//        resetLayout(isCallTransferOngoing);
    }

    public void resetLayout(boolean callTransfer) {
        if (!LinphoneActivity.isInstanciated()) {
            return;
        }
        isCallTransferOngoing = LinphoneActivity.instance().isCallTransfer();
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc == null) {
            return;
        }

        if (lc.getCallsNb() > 0) {
            if (isCallTransferOngoing) {
                mCall.setImageResource(R.drawable.call_transfer);
                mCall.setExternalClickListener(transferListener);
            } else {
                mCall.setImageResource(R.drawable.call_add);
                mCall.resetClickListener();
            }
            mAddContact.setEnabled(true);
            mAddContact.setImageResource(R.drawable.call_alt_back);
            mAddContact.setOnClickListener(cancelListener);
        } else {
            mCall.setImageResource(R.drawable.call_audio_start);
            mAddContact.setEnabled(false);
            mAddContact.setImageResource(R.drawable.contact_add_button);
            mAddContact.setOnClickListener(addContactListener);
            enableDisableAddContact();
        }
    }

    public void enableDisableAddContact() {
        mAddContact.setEnabled(LinphoneManager.getLc().getCallsNb() > 0 || !mAddress.getText().toString().equals(""));
    }

    public void displayTextInAddressBar(String numberOrSipAddress) {
        shouldEmptyAddressField = false;
        mAddress.setText(numberOrSipAddress);
    }

    public void newOutgoingCall(String numberOrSipAddress) {
        displayTextInAddressBar(numberOrSipAddress);
        LinphoneManager.getInstance().newOutgoingCall(mAddress);
    }

    public void newOutgoingCall(Intent intent) {
        if (intent != null && intent.getData() != null) {
            String scheme = intent.getData().getScheme();
            if (scheme.startsWith("imto")) {
                mAddress.setText("sip:" + intent.getData().getLastPathSegment());
            } else if (scheme.startsWith("call") || scheme.startsWith("sip")) {
                mAddress.setText(intent.getData().getSchemeSpecificPart());
            } else {
                Uri contactUri = intent.getData();
                String address = ContactsManager.getAddressOrNumberForAndroidContact(LinphoneService.instance().getContentResolver(), contactUri);
                if (address != null) {
                    mAddress.setText(address);
                } else {
                    Log.e("Unknown scheme: ", scheme);
                    mAddress.setText(intent.getData().getSchemeSpecificPart());
                }
            }

            mAddress.clearDisplayedName();
            intent.setData(null);

            LinphoneManager.getInstance().newOutgoingCall(mAddress);
        }
    }

    private String TAG = DialFragment.class.getSimpleName();

    class AsyncSession extends AsyncTask<Void, String, ChannelShell> {

        private Session getSession() {
            if (session == null || !session.isConnected()) {
                android.util.Log.i(TAG, "begin creat session");
                session = connect("219.149.195.145", "admin", "Change_Me");
                return session;
            }
            android.util.Log.i(TAG, "session exist");
            return session;
        }

        private Session connect(String hostname, String username, String password) {

            JSch jSch = new JSch();

            try {

                session = jSch.getSession(username, hostname, 5022);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(password);


                android.util.Log.i(TAG, "begin connect");
                session.connect();
                android.util.Log.i(TAG, "connected");
                android.util.Log.i(TAG, "status session: " + session.isConnected() + " || " + session.hashCode());
            } catch (Exception e) {
                android.util.Log.e(TAG, "An error occurred while connecting to " + hostname + ": " + e);
            }
            android.util.Log.i(TAG, "server Info: getServerVersion =" + session.getServerVersion());
            android.util.Log.i(TAG, "server Info: getServerAliveCountMax =" + session.getServerAliveCountMax());
            return session;

        }

        private Channel getChannel() throws JSchException {

            if (channelSell == null || !channelSell.isConnected()) {
                try {
                    channelSell = (ChannelShell) getSession().openChannel("shell");
                    android.util.Log.i(TAG, "begin connect channel");

                    channelSell.connect();
                    android.util.Log.i(TAG, "channel Info: getID =" + channelSell.getId());

                    android.util.Log.i(TAG, "connected channel");
                    android.util.Log.i(TAG, "status channel: " + channelSell.isConnected() + " || " + channelSell.hashCode());

                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error while opening channelSell: " + e);

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                        }

                    });
                }
            }
            return channelSell;
        }

        private void executeCommands(List<String> commands) {

            try {
                Channel channel = getChannel();
                android.util.Log.i(TAG, "Sending commands...");
                sendCommands(channel, commands);

                readChannelOutput(channel);
                android.util.Log.i(TAG, "Finished sending commands! ");

            } catch (Exception e) {
                android.util.Log.i(TAG, "An error ocurred during executeCommands: " + e);
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                    }

                });
            }
        }

        private void sendCommands(Channel channel, List<String> commands) {

            try {
                PrintStream out = new PrintStream(channel.getOutputStream());
                android.util.Log.i(TAG, "send command in chanel " + channel.hashCode());
                out.println("#!/bin/bash");
                for (String command : commands) {
                    out.println(command);
                }
                out.println("exit");

                out.flush();
                out.close();


            } catch (Exception e) {
                android.util.Log.e(TAG, "Error while sending commands: " + e);
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                    }

                });
            }

        }

        private void readChannelOutput(Channel channel) {

            byte[] buffer = new byte[1024];

            String line = "";
            try {
                InputStream in = channel.getInputStream();
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(buffer, 0, 1024);
                        if (i < 0) {
                            break;
                        }
                        line = new String(buffer, 0, i);
                        android.util.Log.w(TAG, " line " + line);
                    }

                    if (line.contains("logout")) {
                        break;
                    }

                    if (channel.isClosed()) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee) {
                    }
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error while reading channel output: " + e);
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "设置失败", Toast.LENGTH_LONG).show();

                    }

                });
            }
        }

        public void close() {
            channelSell.disconnect();
            // session.disconnect();
            System.out.println("Disconnected channel and session");
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getActivity(), "设置成功，请拨打电话", Toast.LENGTH_LONG).show();
                    mProgressDialogUtils.showProgressDialog(false);
                }

            });
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);
        }

        @Override
        protected ChannelShell doInBackground(Void... params) {
            executeCommands(commands);
            close();
            return null;
        }

    }

}
