package org.linphone.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.linphone.R;
import org.linphone.utils.MyCookie;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_easter_egg)
public class EasterEggActivity extends AppCompatActivity {


    @ViewInject(R.id.mToolbar)
    Toolbar mToolbar;
    @ViewInject(R.id.sip_server_settings)
    EditText sipServerSettings;
    @ViewInject(R.id.mClear)
    TextView mClear;
    @ViewInject(R.id.mConfirm)
    TextView mConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easter_egg);
        x.view().inject(this);
       String serverIP = MyCookie.getString("ip", "");
        sipServerSettings.setText(serverIP);
        setListener();
    }

    private void setListener() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirm();
            }
        });
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClear();
            }
        });
    }

    private void onConfirm() {
        String sipServer = sipServerSettings.getText().toString();
        if (TextUtils.isEmpty(sipServer)) {
            return;
        }
        MyCookie.putString("ip", sipServer);
        Intent intent = new Intent(this, LoginDemoActivity.class);
        startActivity(intent);
        return;

//        boolean op_flags = getIntent().getBooleanExtra("OP_FLAGS", false);
//
//        Pattern ip = Pattern.compile("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])");
//
//        Pattern ipAdnPort = Pattern.compile("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5]):([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{4}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])");

//        Log.e("", "onConfirm: " + ip.matcher(sipServer).matches() + "::" + ipAdnPort.matcher(sipServer).matches());
//
//        if (!ip.matcher(sipServer).matches() || !ipAdnPort.matcher(sipServer).matches()) {
//            return;
//        }

//
//        String[] split = sipServer.split(":");
//
//        String sipAddr = "";
//        int port = 5060;
//        if (split.length == 2) {
//            sipAddr = split[0];
//            port = Integer.parseInt(split[1]);
//        } else {
//            sipAddr = sipServer;
//        }
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("host", sipAddr);
//        contentValues.put("port", port);
//        contentValues.put("time", System.currentTimeMillis());
//        Uri uri = getContentResolver().insert(Uri.parse("content://" + ConfigProvider.authorities + "/" + ConfigProvider.ip_config_name), contentValues);
//        if (uri != null) {
//            String[] uriSplit = uri.toString().split("/");
//            String current = uriSplit[uriSplit.length - 1];
//
//            ContentValues settings = new ContentValues();
//            settings.put("useId", current);
//            getContentResolver().update(Uri.parse("content://" + ConfigProvider.authorities + "/" + ConfigProvider.sys_setting_name), settings, "keys=?", new String[]{
//                    Constants.DB.IP_ADDRESS
//            });
//
////            Toast.makeText(this,"设置成功,重启应用生效",Toast.LENGTH_SHORT).show();
//            if (op_flags) {
//                CoreService.Extra.close(this);
//
//                Intent intent = new Intent(this, LoginActivity.class);
//                startActivity(intent);
//                return;
//            }

//            Toast.makeText(this, "设置成功,重启App生效", Toast.LENGTH_LONG).show();
//            setResult(RESULT_OK);
//            finish();

//        }

    }


    void onClear() {
        sipServerSettings.setText("");

    }
}
