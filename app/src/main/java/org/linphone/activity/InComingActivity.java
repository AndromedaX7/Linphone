package org.linphone.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.linphone.R;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.regex.Pattern;


/**
 * Created by miao on 2018/10/22.
 */
@ContentView(R.layout.activity_in_coming)
public class InComingActivity extends AppCompatActivity implements ServiceConnection ,View.OnClickListener{

    @ViewInject(R.id.mPhone)
    TextView mPhone;
    @ViewInject(R.id.timer)
    TextView timer;
    @ViewInject(R.id.n1)
    LinearLayout n1;
    @ViewInject(R.id.n2)
    LinearLayout n2;
    @ViewInject(R.id.n3)
    LinearLayout n3;
    @ViewInject(R.id.n4)
    LinearLayout n4;
    @ViewInject(R.id.n5)
    LinearLayout n5;
    @ViewInject(R.id.n6)
    LinearLayout n6;
    @ViewInject(R.id.n7)
    LinearLayout n7;
    @ViewInject(R.id.n8)
    LinearLayout n8;
    @ViewInject(R.id.n9)
    LinearLayout n9;
    @ViewInject(R.id.n10)
    LinearLayout n10;
    @ViewInject(R.id.n0)
    LinearLayout n0;
    @ViewInject(R.id.n11)
    LinearLayout n11;
    @ViewInject(R.id.mHandUp)
    FloatingActionButton mHandUp;
    @ViewInject(R.id.mAnswer)
    Button mAnswer;
    @ViewInject(R.id.inCallState)
    TextView mInCallState;
    @ViewInject(R.id.speak)
    CheckBox mSpeak;
    Messenger client;
    Messenger server;
    @ViewInject(R.id.number)
    EditText number;
    private FinishReceiver finishReceiver;
    public static final int CONNECT = 1;
    private AudioManager audioManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setListener();
        audioManager.setMicrophoneMute(true);
        handler = new Handler();
        client = new Messenger(new Handler(msg -> {
            Log.e("handleMessage: ", msg.what + "");
            switch (msg.what) {
                case CONNECT:
                    onTimer();
                    break;
            }
            return true;
        }));

        audioManager.setSpeakerphoneOn(mSpeak.isChecked());
        mSpeak.setTextColor(getResources().getColor(mSpeak.isChecked() ? R.color.colorAccent : R.color.colorTextColorDark));

        timer.setVisibility(View.INVISIBLE);
        finishReceiver = new FinishReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("zhhl.receiver.finish");
        registerReceiver(finishReceiver, intentFilter);

//        mInCallState.setText(isCallIn == 0 ? "正在呼入:" : "正在呼出");
//        switch (isCallIn) {
//            case 0:
//                mAnswer.setVisibility(View.VISIBLE);
//            case 1:
////                ValueAnimator animator = ValueAnimator.ofFloat(-(getResources().getDimension(R.dimen.dp_40)), getResources().getDimension(R.dimen.dp_40));
////                animator.setDuration(500);
////                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////                    @Override
////                    public void onAnimationUpdate(ValueAnimator animation) {
////                        Float v = (Float) animation.getAnimatedValue();
////                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mHandUp.getLayoutParams();
////                        layoutParams.bottomMargin = v.intValue();
////                        mHandUp.setLayoutParams(layoutParams);
////                    }
////                });
////                animator.start();
//                break;
//        }
//        String phone = intent.getStringExtra(CoreService.Extra.PHONE);
//        if (!TextUtils.isEmpty(phone))
//            mPhone.setText(phone);

    }

    private void setListener() {

        n1.setOnClickListener(this);
        n2.setOnClickListener(this);
        n3.setOnClickListener(this);
        n4.setOnClickListener(this);
        n5.setOnClickListener(this);
        n6.setOnClickListener(this);
        n7.setOnClickListener(this);
        n8.setOnClickListener(this);
        n9.setOnClickListener(this);
        n10.setOnClickListener(this);
        n11.setOnClickListener(this);
        n0.setOnClickListener(this);

                mSpeak.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mSpeak.setTextColor(getResources().getColor(isChecked ? R.color.colorAccent : R.color.colorTextColorDark));
                audioManager.setSpeakerphoneOn(isChecked);
            }
        });
        mHandUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close = true;
                audioManager.setMode(AudioManager.MODE_NORMAL);
            }
        });
        mAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                mHandUp.setVisibility(View.VISIBLE);
                mAnswer.setVisibility(View.GONE);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        close = true;
        unbindService(this);
    }

    void onTimer() {
        close = false;
        timer.setVisibility(View.VISIBLE);
        new Thread(() -> {
            Log.e("onTimer: ", close + "");
            while (!close) {
                Log.e("onTimer: ", close + "");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("", "onTimer: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callTime += 1000;
                        Log.e("onTimer: ", callTime + "");
                        timer.setText(DateFormat.format("mm:ss", callTime));
                    }
                });

            }
        }).start();
    }


    private StringBuilder numPtr = new StringBuilder();
    Pattern comp = Pattern.compile("^[0][1|2][0-9]{1,10}");
    Pattern comp2 = Pattern.compile("^[0][3-9]{1,7}[0-9]{1,10}[0-9]{1,10}");
    @Override
    public void onClick(View view) {
     onNumberPress(view);
    }
    void onNumberPress(View view) {
//        if (numPtr.length() > 11)
//            return;

        switch (view.getId()) {
            case R.id.n0:
                numPtr.append("0");
                break;
            case R.id.n1:
                numPtr.append("1");
                break;
            case R.id.n2:
                numPtr.append("2");
                break;
            case R.id.n3:
                numPtr.append("3");
                break;
            case R.id.n4:
                numPtr.append("4");
                break;
            case R.id.n5:
                numPtr.append("5");
                break;
            case R.id.n6:
                numPtr.append("6");
                break;
            case R.id.n7:
                numPtr.append("7");
                break;
            case R.id.n8:
                numPtr.append("8");
                break;
            case R.id.n9:
                numPtr.append("9");
                break;
            case R.id.n10:
                numPtr.append('*');
                break;
            case R.id.n11:
                numPtr.append('#');
                break;
        }
        String substring = numPtr.substring(numPtr.length() - 1);
        Log.e("onNumberPress: ", substring);
//        CoreService.Extra.dtmfSend(numPtr.substring(numPtr.length() - 1), this);

        String phone = numPtr.toString();
        int length = phone.length();
        int times = length / 4;
        int mod = length % 4;
        int scount = 0;

        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        boolean matches = comp.matcher(phone).matches();
        boolean matches1 = comp2.matcher(phone).matches();
        if (matches || matches1) {
            if (matches) {
                int lenM = length - 3;
                int modM = lenM % 4;
                int timesM = lenM / 4;

                sb.append("(\\w{3})");
                scount++;
                if (modM != 0) {
                    sb.append("(\\w{").append(modM).append("})");
                    scount++;
                }
                for (int i = 0; i < timesM; i++) {
                    sb.append("(\\w{4})");
                    scount++;
                }

            } else {
                int lenM = length - 4;
                int modM = lenM % 4;
                int timesM = lenM / 4;

                sb.append("(\\w{4})");
                scount++;
                if (modM != 0) {
                    sb.append("(\\w{").append(modM).append("})");
                    scount++;
                }
                for (int i = 0; i < timesM; i++) {
                    sb.append("(\\w{4})");
                    scount++;
                }
            }

        } else {
            if (mod != 0) {
                sb.append("(\\w{").append(mod).append("})");
                scount++;
            }

            for (int i = 0; i < times; i++) {
                sb.append("(\\w{4})");
                scount++;
            }
        }
        for (int i = 1; i <= scount; i++) {
            if (i == 1 && (matches || matches1)) {
                sb2.append("(");
            }
            sb2.append("$").append(i);
            if (i == 1 && (matches || matches1)) {
                sb2.append(")");
            }

            if (i != scount)
                sb2.append("-");
        }

//        Log.e("onNumberPress: ", sb.toString() + "----" + sb2
//                .toString());
        number.setText(phone.replaceAll(sb.toString(), sb2.toString()));
        number.setSelection(number.length());
//        Log.e("input: ", number.getText().toString() + "--->" + matches + matches1);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        server = new Messenger(service);
        Message message = Message.obtain();
        message.what = 2;
        message.replyTo = client;
        try {
            server.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }



    public class FinishReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }


    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close = true;
        audioManager.setMode(AudioManager.MODE_NORMAL);
        unregisterReceiver(finishReceiver);
    }

    private Handler handler;
    private static boolean close = true;
    private long callTime = 0;
}
