package com.chuanda.bright.pressure.activity;

import android.Manifest;
import android.Manifest.permission;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bright.administrator.lib_common.base.mvc.BaseVcActivity;
import com.bright.administrator.lib_common.util.StatusBarUtil;
import com.bright.administrator.lib_coremodel.bean.EventBusBean;
import com.bright.administrator.lib_coremodel.constant.BaseEventbusBean;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.administrator.lib_coremodel.service.BLEService;
import com.bright.administrator.lib_coremodel.service.BLEService.LocalBinder;
import com.bright.administrator.lib_coremodel.service.StepService;
import com.bright.administrator.lib_coremodel.service.StepService.LocalStepBinder;
import com.chuanda.bright.pressure.R;
import com.chuanda.bright.pressure.helper.BottomNavigationViewHelper;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseVcActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @BindView(R.id.layout_pager)
    FrameLayout mFrameLayout;
    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.nav_view)
    NavigationView mNavView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.common_toolbar_title_tv)
    public TextView mTitleTv;
    @BindView(R.id.common_toolbar)
    Toolbar mToolbar;
    private static String TAG = "HomeActivity";
    private MenuItem mItemAuthor;
    private MenuItem mItemVideo;
    private MenuItem mItemAboutUs;
    private MenuItem mItemLogout;
    private TextView mMUsTv;
    private List<Fragment> mFragmentList;
    private Fragment mCurrentFragment;
    private BLEService.LocalBinder bleBinder;
    private StepService.LocalStepBinder stepBinder;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initTitle() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        mTitleTv.setText("首页");
        StatusBarUtil.setStatusColor(getWindow(), ContextCompat.getColor(this, R.color.main_status_bar_blue), 1f);
        mToolbar.setNavigationOnClickListener(v -> onBackPressedSupport());
    }

    private void onBackPressedSupport() {
        EventBus.getDefault().post(new EventBusBean(EventBusBean.SHOP_MALL_HOME, 1));
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    protected void initView() {
        //注册EventBus
        super.regEvent = true ;
        //开启蓝牙服务
        initBLEService();
        initStepService();
        initNavigationView();
        initFragment();
        initBottomNavigationView();
        MainActivityPermissionsDispatcher.PermissionNeedWithPermissionCheck(this);
    }

    private void initStepService() {
        Intent intent = new Intent(this,StepService.class);
        //连接服务
        startService(intent);
        bindService(intent, new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                stepBinder = (LocalStepBinder) service;
                sendStepLocalBinder(stepBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                stepBinder = null;
            }
        },BIND_AUTO_CREATE);

    }

    private void initBLEService() {
        Intent intent = new Intent(this,BLEService.class);
        //连接服务
        ServiceConnection conn = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleBinder = (BLEService.LocalBinder) service;
                sendBLELocalBinder(bleBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bleBinder = null;
            }
        };
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }
   //将BLEService.LocalBinder发送出去
    private void sendBLELocalBinder(LocalBinder binder) {
        EventBus.getDefault().post(new BaseEventbusBean<>(1, binder));
        Logger.d("LocalBinder已发送");
    }
   //将StepService.LocalStepBinder发送出去
    private void sendStepLocalBinder(StepService.LocalStepBinder binder) {
        EventBus.getDefault().post(new BaseEventbusBean<>(1, binder));
        //EventBus.getDefault().post(binder);

    }

    private void initNavigationView() {
        //头部布局  登录
        mMUsTv = mNavView.getHeaderView(0).findViewById(R.id.nav_header_login_tv);
        //作者
        mItemAuthor = mNavView.getMenu().findItem(R.id.nav_item_author);
        //视频
        mItemVideo = mNavView.getMenu().findItem(R.id.nav_item_video);
        //关于我们
        mItemAboutUs = mNavView.getMenu().findItem(R.id.nav_item_about_us);
        //退出登录
        mItemLogout = mNavView.getMenu().findItem(R.id.nav_item_logout);

        //通过actionbardrawertoggle将toolbar与drawablelayout关联起来
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this ,mDrawerLayout, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //可以重新侧滑方法,该方法实现侧滑动画,整个布局移动效果
                //获取mDrawerLayout中的第一个子布局，也就是布局中的RelativeLayout
                //获取抽屉的view
                View mContent = mDrawerLayout.getChildAt(0);
                float scale = 1 - slideOffset;
                float endScale = 0.8f + scale * 0.2f;
                float startScale = 1 - 0.3f * scale;

                //设置左边菜单滑动后的占据屏幕大小
                drawerView.setScaleX(startScale);
                drawerView.setScaleY(startScale);
                //设置菜单透明度
                drawerView.setAlpha(0.6f + 0.4f * (1 - scale));

                //设置内容界面水平和垂直方向偏转量
                //在滑动时内容界面的宽度为 屏幕宽度减去菜单界面所占宽度
                mContent.setTranslationX(drawerView.getMeasuredWidth() * (1 - scale));
                //设置内容界面操作无效（比如有button就会点击无效）
                mContent.invalidate();
                //设置右边菜单滑动后的占据屏幕大小
                mContent.setScaleX(endScale);
                mContent.setScaleY(endScale);
            }
        };

        toggle.syncState();
        mDrawerLayout.addDrawerListener(toggle);

        //设置图片为本身的颜色
        mNavView.setItemIconTintList(null);
        //设置item的点击事件
        mNavView.setNavigationItemSelectedListener(this);
        //头部设置监听
        mMUsTv.setOnClickListener(this);
    }
    private void initFragment() {
        mFragmentList = new ArrayList<>();
        Fragment fragmentMain = (Fragment) ARouter.getInstance().build(RouterURLS.MainFragment)
                .withTransition(R.anim.activity_up_in, R.anim.activity_up_out)
                .navigation();
        Fragment fragmentSport = (Fragment) ARouter.getInstance().build(RouterURLS.SportFragment)
                .withTransition(R.anim.activity_up_in, R.anim.activity_up_out)
                .navigation();
        Fragment fragmentSetting = (Fragment) ARouter.getInstance().build( RouterURLS.SettingFragment ).navigation();
        Fragment fragmentMine = (Fragment) ARouter.getInstance().build( RouterURLS.MineFragment ).navigation();

        mFragmentList.add(fragmentMain);
        mFragmentList.add(fragmentSport);
        mFragmentList.add(fragmentSetting);
        mFragmentList.add(fragmentMine);
    }
    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void initBottomNavigationView() {
        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
        // 预设定进来后,默认显示fragment
        addFragment(R.id.layout_pager, mFragmentList.get(0));
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.tab_home) {
                    mTitleTv.setText("首页");
                    addFragment(R.id.layout_pager, mFragmentList.get(0));
                } else if (item.getItemId() == R.id.tab_sport) {
                    mTitleTv.setText("运动");
                    addFragment(R.id.layout_pager, mFragmentList.get(1));
                } else if (item.getItemId() == R.id.tab_setting) {
                    mTitleTv.setText("设置");
                    addFragment(R.id.layout_pager, mFragmentList.get(2));
                } else if (item.getItemId() == R.id.tab_self) {
                    mTitleTv.setText("我的");
                    addFragment(R.id.layout_pager, mFragmentList.get(3));
                }
                return true;
            }
        });
    }
    /**
     * 显示fragment
     * @param frameLayoutId
     * @param fragment
     */
    private void addFragment(int frameLayoutId, Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (fragment.isAdded()) {
                if (mCurrentFragment != null) {
                    transaction.hide(mCurrentFragment).show(fragment);
                } else {
                    transaction.show(fragment);
                }
            } else {
                if (mCurrentFragment != null) {
                    transaction.hide(mCurrentFragment).add(frameLayoutId, fragment);
                } else {
                    transaction.add(frameLayoutId, fragment);
                }
            }
            mCurrentFragment = fragment;
            transaction.commit();
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item == mItemAuthor){
            Logger.d(TAG, "点击了福利");
            closeDrawer();
        }else if (item == mItemVideo){
            Logger.d(TAG, "点击了视频");
            closeDrawer();
        }else if (item == mItemAboutUs){
            Logger.d(TAG, "点击了关于我们");
            closeDrawer();
        }else if (item == mItemLogout){
            Logger.d(TAG, "点击了退出");
            closeDrawer();
        }
        return true;
    }
    /**
     * 关闭侧滑
     */
    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onEvent(BaseEventbusBean event) {
        super.onEvent(event);
        int type = event.getType();

        if (EventBusBean.SHOP_MALL_HOME == type){
            //检查侧滑菜单的状态
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        }
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.nav_header_login_tv){
            Logger.d(TAG, "点击了登录");
        }
    }
    private long mTime=0;
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-mTime > 2000){
            Toast.makeText(getApplicationContext(),"再按一次将退出应用",Toast.LENGTH_SHORT).show();
            mTime=System.currentTimeMillis();
        }else {
            super.onBackPressed();
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE})
    void PermissionNeed() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                      Manifest.permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE})
    void PermissionNeedForRation(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("必要权限请通过")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();//再次执行请求
                    }
                })
                .show();
    }
}

