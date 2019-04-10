package com.bright.module_sport.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.module_sport.R;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现时时动态画运动轨迹
 */
@Route(path = RouterURLS.DynamicMapTraceActivity)
public class DynamicMapTrace extends Activity implements SensorEventListener,View.OnClickListener{

    private static final int STARTCOUNT = 0;
    private static final int REMOVEMESS = 1;
    private static final int INFOCONVERTER = 2;
	private static final int TIMECOUNT = 3;

    // 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private int mCurrentDirection = 0;
	private double mCurrentLat = 0.0;
	private double mCurrentLon = 0.0;
    private double lastX;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	//显示搜索的布局
	private TextView info;
	private RelativeLayout progressBarRl;
    //显示信息的布局
    private RelativeLayout visible2;
    private ImageView slide;
    private LinearLayout ly1;
    private TextView km;
    private TextView time;
    private TextView speed;
    private TextView hot;
    private LinearLayout ly2;
    private TextView km2;
    private TextView time2;
    //停止按钮组相关
    private LinearLayout ly3;
    private Button play;
    private Button pause;
    private Button stop;
    //界面的一些标志位
    private boolean isShow = true;//是否详细界面
    private boolean isStart = true;//是否开始跑步
    private double mTime=0;
    //倒计时界面
    private RelativeLayout num_v;
    private TextView num;

	boolean isFirstLoc = true; // 是否首次定位
    boolean isFristIn = true;
	private MyLocationData locData;
	float mCurrentZoom = 18f;//默认地图缩放比例值
	private SensorManager mSensorManager;

	//起点图标
	BitmapDescriptor startBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_startpoint);
	//终点图标
	BitmapDescriptor finishBD = BitmapDescriptorFactory.fromResource(R.drawable.ic_me_history_finishpoint);
	
	List<LatLng> points = new ArrayList<LatLng>();//位置点集合
	Polyline mPolyline;//运动轨迹图层
	LatLng last = new LatLng(0, 0);//上一个定位点
	MapStatus.Builder builder;
	//显示时间速度相关
    SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DecimalFormat db = new DecimalFormat("#.##");
    private DecimalFormat db2 = new DecimalFormat("#");
    //当前速度
    private float mTspeed =0;
    //总距离
    private double distance=0.00;
    //总时间
    private int totalTime=0;
    //总热量
    private double totalHot=0;
    //时、分、秒
    private int HMS=0;
    private int H=0;
    private int M=0;
    private int S=0;

	private static int count = 3;
	private boolean isTicked = true;
	private static int timer = 0;
	@SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){

		@Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case  STARTCOUNT:
                    // TODO 移除所有的msg.what为0等消息，保证只有一个循环消息队列再跑
                    mHandler.removeMessages(STARTCOUNT);
                    // TODO 倒计时功能
                    if(count > 0) {
                        num_v.setVisibility(View.VISIBLE);
                        num.setText((count--)+"");
                        // 再次发出msg，循环更新
                        mHandler.sendEmptyMessageDelayed(STARTCOUNT, 1000);
                    }else {
                        num_v.setVisibility(View.GONE);
                        mHandler.sendEmptyMessage(REMOVEMESS);
                    }
                    break;
                case REMOVEMESS:
                    // 直接移除，定时器停止
                    mHandler.removeMessages(STARTCOUNT);
                    count = 3;
                    break;
                case INFOCONVERTER:
                    km.setText(db.format(distance/2000));
                    km2.setText(db.format(distance/2000));
                    hot.setText(db.format(totalHot));
                    //时间转换
                  /*  S=totalTime%60;
                    HMS=totalTime/60;
                    M=HMS%60;
                    H=HMS/60;
                    time.setText((H>=10?(H+""):("0"+H))+":"+(M>=10?(M+""):("0"+M))+":"+(S>=10?(S+""):("0"+S)));
                    time2.setText((H>=10?(H+""):("0"+H))+":"+(M>=10?(M+""):("0"+M))+":"+(S>=10?(S+""):("0"+S)));*/
                   speed.setText(db.format(mTspeed*5/18.0)+"m/s");
                    break;
				case TIMECOUNT:
					//时间转换
					S=timer%60;
					HMS=timer/60;
					M=HMS%60;
					H=HMS/60;
					time.setText((H>=10?(H+""):("0"+H))+":"+(M>=10?(M+""):("0"+M))+":"+(S>=10?(S+""):("0"+S)));
					time2.setText((H>=10?(H+""):("0"+H))+":"+(M>=10?(M+""):("0"+M))+":"+(S>=10?(S+""):("0"+S)));
					timer++;
					sendEmptyMessageDelayed(TIMECOUNT,1000);

            }
        }
    };



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maptrace);
        num_v = (RelativeLayout) findViewById(R.id.start_num_v);
        num = (TextView) findViewById(R.id.start_num);
        mHandler.sendEmptyMessage(STARTCOUNT);
		initView();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务
		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
				com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, true, null));

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
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//只用gps定位，需要在室外定位。
		//option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//只用gps定位，需要在室外定位。
		option.setOpenGps(true); // 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);//1秒定位一次
		mLocClient.setLocOption(option);
        visible2.setVisibility(View.VISIBLE);
        if (mLocClient != null && !mLocClient.isStarted()) {
            mLocClient.start();
            progressBarRl.setVisibility(View.VISIBLE);
            //TODO
            info.setText("GPS信号搜索中，请稍后...");
            mBaiduMap.clear();
       }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"搜索不到GPS信号，请到户外", Toast.LENGTH_LONG).show();
            }
        },2000*60);//两分钟后提示

	}
	private void initView() {
	    //提示GPS信号质量界面
		info = (TextView) findViewById(R.id.info);
		progressBarRl = (RelativeLayout) findViewById(R.id.progressBarRl);

        //操作按钮显示信息的布局
        visible2= (RelativeLayout)findViewById(R.id.start_visible2);
        slide= (ImageView)findViewById(R.id.start_slide);
        ly1= (LinearLayout)findViewById(R.id.start_ly1);
        km= (TextView) findViewById(R.id.start_km);
        time= (TextView)findViewById(R.id.start_time);
        speed= (TextView)findViewById(R.id.start_speed);
        hot= (TextView)findViewById(R.id.start_hot);
        ly2= (LinearLayout)findViewById(R.id.start_ly2);
        km2= (TextView)findViewById(R.id.start_km2);
        time2= (TextView)findViewById(R.id.start_time2);
        //停止按钮组相关
        ly3= (LinearLayout)findViewById(R.id.start_ly3);
        play= (Button)findViewById(R.id.start_play);
        pause= (Button)findViewById(R.id.start_pause);
        stop= (Button)findViewById(R.id.start_stop);
        //添加监听
        slide.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);

	}

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start_slide) {
            if (isShow) {
                ly1.setVisibility(View.GONE);
                ly3.setVisibility(View.GONE);
                ly2.setVisibility(View.VISIBLE);
                slide.setImageResource(R.mipmap.slideup_pressed);
            } else {
                ly1.setVisibility(View.VISIBLE);
                ly3.setVisibility(View.VISIBLE);
                ly2.setVisibility(View.GONE);
                slide.setImageResource(R.mipmap.slidedown_pressed);
            }
            isShow = !isShow;

        } else if (i == R.id.start_stop) {//退出
            isStart = false;
            if (System.currentTimeMillis() - mTime > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                mTime = System.currentTimeMillis();
            } else {
                //更新画出轨终点
                if (mLocClient != null && mLocClient.isStarted()) {
                    mLocClient.stop();
                    progressBarRl.setVisibility(View.GONE);
                    if (isFirstLoc) {
                        points.clear();
                        last = new LatLng(0, 0);
                        return;
                    }
                    MarkerOptions oFinish = new MarkerOptions();// 地图标记覆盖物参数配置类
                    oFinish.position(points.get(points.size() - 1));
                    oFinish.icon(finishBD);// 设置覆盖物图片
                    mBaiduMap.addOverlay(oFinish); // 在地图上添加此图层
                    mHandler.removeMessages(TIMECOUNT);//撤销计时器
                    //复位
                    points.clear();
                    last = new LatLng(0, 0);
                    isFirstLoc = true;
                }
            }

        } else if (i == R.id.start_pause) {//暂停
            isStart = false;
            mHandler.removeMessages(TIMECOUNT);//撤销计时器
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
            play.setVisibility(View.VISIBLE);

        } else if (i == R.id.start_play) {//继续
            isStart = true;
            pause.setVisibility(View.VISIBLE);
            stop.setVisibility(View.GONE);
            play.setVisibility(View.GONE);
            mHandler.sendEmptyMessageDelayed(TIMECOUNT, 1000);//撤销计时器

        }
    }

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		double x = sensorEvent.values[SensorManager.DATA_X];
		
		if (Math.abs(x - lastX) > 1.0) {
			mCurrentDirection = (int) x;
			
			if (isFirstLoc) {
				lastX = x;
				return;
			}
			
			locData = new MyLocationData.Builder().accuracy(0)
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(mCurrentDirection).latitude(mCurrentLat).longitude(mCurrentLon).build();
			mBaiduMap.setMyLocationData(locData);//设置定位图标
		}
		lastX = x;

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}



    /**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(final BDLocation location) {

			if (location == null || mMapView == null) {
				return;
			}
          	if(isFristIn) {//初始定位
			  isFristIn = false;
			  mCurrentLat = location.getLatitude();
			  mCurrentLon = location.getLongitude();
			  locData = new MyLocationData.Builder().accuracy(location.getRadius())
					  // 此处设置开发者获取到的方向信息，顺时针0-360
					  .direction(mCurrentDirection).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
			  mBaiduMap.setMyLocationData(locData);
			  LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			  MapStatus.Builder builder = new MapStatus.Builder();
			  builder.target(latLng).zoom(mCurrentZoom);
			  mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
		  	}
			//注意这里只接受gps点，需要在室外定位。
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				info.setText("GPS信号弱，请稍后...");
				if (isFirstLoc) {//首次定位
					//第一个点很重要，决定了轨迹的效果，gps刚开始返回的一些点精度不高，尽量选一个精度相对较高的起始点
					LatLng ll = null;
					ll = getMostAccuracyLocation(location);
					if(ll == null){
						return;
					}
					isFirstLoc = false;
					points.add(ll);//加入集合
					last = ll;
					
					//显示当前定位点，缩放地图
					locateAndZoom(location, ll);
					
					//标记起点图层位置
					MarkerOptions oStart = new MarkerOptions();// 地图标记覆盖物参数配置类
					oStart.position(points.get(0));// 覆盖物位置点，第一个点为起点
					oStart.icon(startBD);// 设置覆盖物图片
					mBaiduMap.addOverlay(oStart); // 在地图上添加此图层
				    progressBarRl.setVisibility(View.GONE);//提示消失
					return;//画轨迹最少得2个点，首地定位到这里就可以返回了
				}

				//从第二个点开始
				LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
				//sdk回调gps位置的频率是1秒1个，位置点太近动态画在图上不是很明显，可以设置点之间距离大于为5米才添加到集合中
                distance = DistanceUtil.getDistance(last, ll);
                if (distance < 5) {
					return;
				}
				points.add(ll);//如果要运动完成后画整个轨迹，位置点都在这个集合中
				//判断是否在运动，如果是在运动则记录轨迹
				if(isStart) {
					//计算信息到页面显示
					distance += DistanceUtil.getDistance(last, ll );//距离(m)
					//totalTime+=1;//总时间(秒)
					if(location.getSpeed() >= 5.0/18){ //人一般步行的速度是1m/s，所以1m/s以下的相当于静止，不计算
						mTspeed = location.getSpeed();//速度(km/h)
					}else{
						mTspeed = 0;
					}
					totalHot=distance/500.0*1.036*50;//跑步热量（kcal）＝体重（kg）×距离（公里）×1.036
					mHandler.sendEmptyMessage(INFOCONVERTER);//发送信息给handler处理
					if(isTicked){
						isTicked = false;
						mHandler.sendEmptyMessage(TIMECOUNT);
					}

				}

				last = ll;
				//显示当前定位点，缩放地图
				locateAndZoom(location, ll);
				//清除上一次轨迹，避免重叠绘画
				mMapView.getMap().clear();
				//起始点图层也会被清除，重新绘画
				MarkerOptions oStart = new MarkerOptions();
				oStart.position(points.get(0));
				oStart.icon(startBD);
				mBaiduMap.addOverlay(oStart);

				//将points集合中的点绘制轨迹线条图层，显示在地图上
				OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(points);
				mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);


			}
		}

	}
	
	private void locateAndZoom(final BDLocation location, LatLng ll) {
		mCurrentLat = location.getLatitude();
		mCurrentLon = location.getLongitude();
		locData = new MyLocationData.Builder().accuracy(0)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(mCurrentDirection).latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
		mBaiduMap.setMyLocationData(locData);
		builder = new MapStatus.Builder();
		builder.target(ll).zoom(mCurrentZoom);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
	}

	/**
	 * 首次定位很重要，选一个精度相对较高的起始点
	 * 注意：如果一直显示gps信号弱，说明过滤的标准过高了，
	 你可以将location.getRadius()>25中的过滤半径调大，比如>40，
	 并且将连续5个点之间的距离DistanceUtil.getDistance(last, ll ) > 5也调大一点，比如>10，
	 这里不是固定死的，你可以根据你的需求调整，如果你的轨迹刚开始效果不是很好，你可以将半径调小，两点之间距离也调小，
	 gps的精度半径一般是10-50米
	 */
	private LatLng getMostAccuracyLocation(BDLocation location){
		
		if (location.getRadius()>40) {//gps位置精度大于40米的点直接弃用
			return null;
		}
		
		LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
		
		if (DistanceUtil.getDistance(last, ll ) > 10) {
			last = ll;
			points.clear();//有任意连续两点位置大于10，重新取点
			return null;
		}
		points.add(ll);
		last = ll;
		//有5个连续的点之间的距离小于10，认为gps已稳定，以最新的点为起始点
		if(points.size() >= 5){
			points.clear();
			return ll;
		}
		return null;
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
		// 为系统的方向传感器注册监听器
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onStop() {
		// 取消注册传感器监听
		mSensorManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
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
		startBD.recycle();
		finishBD.recycle();
		mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

}
