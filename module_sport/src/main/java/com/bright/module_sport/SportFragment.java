package com.bright.module_sport;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bright.administrator.lib_common.base.mvp.BaseVpFragment;
import com.bright.administrator.lib_common.util.CheckPermissionUtils;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.module_sport.contract.SportContract;
import com.bright.module_sport.contract.SportContract.Presenter;

import butterknife.BindView;

import static android.content.Context.SENSOR_SERVICE;

@Route(path = RouterURLS.SportFragment)
public class SportFragment extends BaseVpFragment<SportContract.View,SportContract.Presenter> implements SportContract.View,View.OnClickListener,SensorEventListener {

    //最开始的布局
    @BindView(R2.id.start_visible)
    public RelativeLayout visible;
    @BindView(R2.id.start_btn)
    public Button start_btn;
    @BindView(R2.id.textView1)
    public TextView warmup_btn;
    @BindView(R2.id.textView2)
    public TextView music_btn;
    @BindView(R2.id.start_map)
    public MapView mMapView;
    // 定位相关
    LocationClient mLocClient;
    private LocationMode mCurrentMode;
    private BDLocationListener myListener = new MyLocationListenner();
    private BaiduMap mBaiduMap;
    float mCurrentZoom = 18f;//默认地图缩放比例值
    boolean isFirstLoc = true; // 是否首次定位
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private double lastX;
    private MyLocationData locData;
    //传感器相关
    private SensorManager mSensorManager;

    @Override
    protected int getLayout() {
        return R.layout.home_startsport;
    }


    @Override
    protected void initView() {
        warmup_btn.setBackgroundColor(Color.parseColor("#FF8C00"));
        music_btn.setBackgroundColor(Color.parseColor("#FF8C00"));
        warmup_btn.setText("热身");
        warmup_btn.setGravity(Gravity.CENTER);
        music_btn.setText("音乐");
        music_btn.setGravity(Gravity.CENTER);
        mSensorManager = (SensorManager)mContext.getSystemService(SENSOR_SERVICE);// 获取传感器管理服务
        //按钮监听
        start_btn.setOnClickListener(this);
        if(CheckPermissionUtils.checkGPSIsOpen(mContext)){
            inintMap();
            inintMapData();
        }else {
            mPresenter.checkGpsSetting();
        }




    }

    private void inintMap() {
        // 地图初始化
        mCurrentMode = LocationMode.NORMAL;
        //mMapView = (MapView) view.findViewById(R.id.start_map);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));//配置定位的图标
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        /**
         * 添加地图缩放状态变化监听，当手动放大或缩小地图时，拿到缩放后的比例，然后获取到下次定位，
         *  给地图重新设置缩放比例，否则地图会重新回到默认的mCurrentZoom缩放比例
         */
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                mCurrentZoom = arg0.zoom;
            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {

            }
        });
    }

    private void inintMapData() {
        // 定位初始化
        mLocClient = new LocationClient(getContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setOpenGps(true); // 打开gps
        mLocClient.setLocOption(option);
        mLocClient.start();//开启了定位

    }
    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        super.onStop();
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        mLocClient.unRegisterLocationListener(myListener);
        if (mLocClient != null && mLocClient.isStarted()) {
            mLocClient.stop();
        }
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.getMap().clear();
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public Presenter createPresenter() {
        return new SportPresenter();
    }

    @Override
    public SportContract.View createView() {
        return this;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start_btn) {
            if(CheckPermissionUtils.checkGPSIsOpen(mContext)){
                ARouter.getInstance().build(RouterURLS.DynamicMapTraceActivity).navigation();
            }else{
                //设置打开gps定位
                mPresenter.checkGpsSetting();
            }
        }
    }

    @Override
    public void goGpsSetting() {
        new AlertDialog.Builder(mContext) //对话框
                .setTitle("提示") //设置对话框的标题
                .setMessage("当前功能需要打开GPS") //设置对话框要传达的具体信息
                .setNegativeButton("取消",   //反面按钮
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();//关闭对话框
                            }
                        })
                .setPositiveButton("前往设置",  //正面按钮
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                .setCancelable(false)    //对话框消失时触发的事件
                .show();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(mCurrentZoom);//设置地图放大倍数
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }
}
