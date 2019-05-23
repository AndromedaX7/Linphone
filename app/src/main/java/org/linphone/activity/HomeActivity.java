package org.linphone.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import org.linphone.R;
import org.linphone.widgets.AlphaIndicator;
import org.linphone.widgets.AlphaView;
import org.xutils.view.annotation.ContentView;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by Administrator on 2017/12/4.
 */
@ContentView(R.layout.activity_home)
public class HomeActivity extends BaseActivity {
    Handler handler = new Handler();
    private ViewPager viewPager;
    private AlphaIndicator alphaIndicator;
    private AlphaView av_home;
    private AlphaView av_shop;
    private AlphaView av_store;
    private AlphaView av_mine;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private long mkeyTime;
    private int type = 0;
    private Dialog alertDialog;

    private String globalSearch = null;
    private int flag = 0;//1全局搜索
    public static final int REQUEST_ID_SIP_SETTING = 32767;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        //透明状态栏
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window window = getWindow();
//            // Translucent status bar
//            window.setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }


        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        x.view().inject(this);
        mContext = this;
        updateSystem();
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mFragments.add(new DialFragment());
        mFragments.add(new MineFragmentto());

        //适配器
        viewPager.setAdapter(new MainAdapter(getSupportFragmentManager(), mFragments));
        alphaIndicator = (AlphaIndicator) findViewById(R.id.alphaIndicator);
        av_home = (AlphaView) findViewById(R.id.av_home);
//        av_shop = (AlphaView) findViewById(R.id.av_shop);
//        av_store = (AlphaView) findViewById(R.id.av_store);
        av_mine = (AlphaView) findViewById(R.id.av_mine);
        alphaIndicator.setViewPager(viewPager);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if ((System.currentTimeMillis() - mkeyTime) > 2000) {
                mkeyTime = System.currentTimeMillis();
                showToast("再按一次返回键退出");
            } else {
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MainAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<Fragment>();


        public MainAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }


}

