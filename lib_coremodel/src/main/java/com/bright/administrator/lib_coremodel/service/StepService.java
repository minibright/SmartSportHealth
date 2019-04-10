package com.bright.administrator.lib_coremodel.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.bright.administrator.lib_coremodel.R;
import com.bright.administrator.lib_coremodel.bean.StepEntity;
import com.bright.administrator.lib_coremodel.dao.AppDatabase;
import com.bright.administrator.lib_coremodel.dao.StepEntityDao;
import com.bright.administrator.lib_coremodel.utils.SPTools;
import com.bright.administrator.lib_coremodel.utils.TimeTools;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Notification.FLAG_FOREGROUND_SERVICE;


public class StepService extends Service implements SensorEventListener {
    public static final String TAG = "StepService";
    //当前日期
    private static String CURRENT_DATE;
    //当前步数
    private int CURRENT_STEP;
    //传感器
    private SensorManager sensorManager;
    //计步传感器类型 0-counter 1-detector
    private static int stepSensor = -1;
    //广播接收
    private BroadcastReceiver mInfoReceiver;
    //是否有当天的记录
    private boolean hasRecord;
    //未记录之前的步数
    private int hasStepCount;
    //下次记录之前的步数
    private SPTools sp;
    StepEntity entity;//计步数据
    //notification相关
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Intent nfIntent;


    //数据库
    private StepEntityDao stepEntityDao;
    private FootStepCallback footStepCallback; //回调对象
    public void setFootStepCallback(FootStepCallback callback) {
        this.footStepCallback = callback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stepEntityDao = AppDatabase.getInstance(this).stepEntityDao();//数据库操作对象
        sp = new SPTools(StepService.this);//sp操作对象
        initBroadcastReceiver();//设置广播监听手机的状态

        new Thread(new Runnable() {
            @Override
            public void run() {
                getStepDetector(); //注册步数计数传感器的监听，实现回调方法
                initTodayData(); //初始化当天数据
            }
        }).start();

        //测试数据
        sp.setID("1");
        sp.setName("bright");
        //创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "stepNotify";
            String channelName = "计步";
            int importance = NotificationManager.IMPORTANCE_LOW;
            createNotificationChannel(false,false,channelId, channelName, importance);
            channelId = "subscribe";
            channelName = "订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(false,false,channelId, channelName, importance);
        }
        try {
            nfIntent = new Intent(this, Class.forName("com.chuanda.bright.pressure.activity.MainActivity"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            builder = new NotificationCompat.Builder(this.getApplicationContext(),"stepNotify");
        }else {
            builder = new NotificationCompat.Builder(this.getApplicationContext());
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    private void createNotificationChannel(boolean isVibrate,boolean isSound,String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        if (!isVibrate) {
            // 设置通知出现时不震动
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
        }
        if(!isSound){
            //设置通知出现时没有声音
            channel.setSound(null, null);
        }

        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**
         * 此处设将Service为前台，不然当APP结束以后很容易被GC给干掉，这也就是大多数音乐播放器会在状态栏设置一个
         * 原理大都是相通的
         */
        stepNotify(true);
        return START_STICKY;
    }

    /**
     * 初始化广播
     */
    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //监听日期变化
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        mInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    // 屏幕灭屏广播
                    case Intent.ACTION_SCREEN_OFF:
                        //屏幕熄灭
                        saveStepData();
                        break;
                    //关机广播，保存好当前数据
                    case Intent.ACTION_SHUTDOWN:
                        saveStepData();
                        break;
                    // 屏幕解锁广播
                    case Intent.ACTION_USER_PRESENT:
                        saveStepData();
                        break;
                    // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
                    // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
                    // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
                    case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                        saveStepData();
                        break;
                    //监听日期变化
                    case Intent.ACTION_DATE_CHANGED:
                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIME_TICK:
                        saveStepData();
                        isNewDay();
                        break;
                    default:
                        break;
                }
            }
        };
        //注册广播
        registerReceiver(mInfoReceiver, filter);
    }

    /**
     * TODO 初始化当天数据,每天凌晨更新
     */
    private void initTodayData() {
        //获取当前时间
        CURRENT_DATE = TimeTools.getCurrentDate();//yyyy年MM月dd日
        //获取当天的数据，用于展示
        entity = stepEntityDao.findStepEntityByDate(CURRENT_DATE,sp.getID());
        //为空则说明还没有该天的数据，有则说明已经开始当天的计步了
        if (entity == null) {
            CURRENT_STEP = 0;
        } else {
            CURRENT_STEP = Integer.parseInt(entity.getSteps());//从数据库中取步数
        }
    }


    /**
     * 监听晚上0点变化初始化数据
     */
    private void isNewDay() {
        String time = "00:00";
        CURRENT_DATE = TimeTools.getCurrentDate();//yyyy年MM月dd日
        if (time.equals(new SimpleDateFormat("HH:mm").format(new Date())) ||
                !CURRENT_DATE.equals(TimeTools.getCurrentDate())) {
            initTodayData();
        }
    }


    /**
     * 获取传感器实例
     */
    private void getStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        // 获取传感器管理器的实例
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        //android4.4以后可以使用计步传感器
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES >= 19) {
            addCountStepListener();
        }
    }


    /**
     * 添加传感器监听
     */
    private void addCountStepListener() {
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensor = 0;
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectorSensor != null) {
            stepSensor = 1;
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    /**
     * 由传感器记录当前用户运动步数，注意：该传感器只在4.4及以后才有，并且该传感器记录的数据是从设备开机以后不断累加，
     * 只有当用户关机以后，该数据才会清空，所以需要做数据保护
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //TODO 传感器的原始数据在此处取到  CURRENT_STEP为当前步数
        if (stepSensor == 0) { //0号总步数传感器
            Logger.d("传感器总步数数据变化了");

            int tempStep = (int) event.values[0];
            if (!hasRecord) {
                hasRecord = true;
                hasStepCount = tempStep;
            } else {
                int thisStepCount = tempStep - hasStepCount;
                CURRENT_STEP += thisStepCount;
                hasStepCount = tempStep;

            }
        } else if (stepSensor == 1) {//1号单步传感器
            Logger.d("单步传感器数据变化了");
            if (event.values[0] == 1.0) {
                CURRENT_STEP++;
            }
        }
        //实时的存储数据
        saveStepData();
        if(footStepCallback != null){
            footStepCallback.StepDataChange(entity);//回调传递数据CURRENT_STEP
            Logger.d("调用了footStepCallback");
            Logger.d(entity);
        }


    }

    private void stepNotify(boolean isstartForeground) {
        Notification stepNotification = builder
                //.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setContentTitle("今日步数"+CURRENT_STEP+"步")
                .setContentText("加油，要记得勤加运动")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.small_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.smart_shoes_logo))
                .setAutoCancel(true)
                .build();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            //适配安卓8.0以下，关闭通知时的声音和震动
            stepNotification.vibrate = null;
            stepNotification.sound = null;
        }
        assert notificationManager != null;
        notificationManager.notify(1, stepNotification);
        // 参数一：唯一的通知标识；参数二：通知消息。
        if(isstartForeground){
            startForeground(1, stepNotification);// 开始前台服务
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 保存当天的数据到数据库中，并去刷新通知栏
     */
    private void saveStepData() {
        stepNotify(false);//刷新通知栏
        new Thread(new Runnable() {
            @Override
            public void run() {
                //查询数据库中的数据
                entity = stepEntityDao.findStepEntityByDate(CURRENT_DATE,sp.getID());
                Logger.d("更新了entity");
                Logger.d(entity);
                //为空则说明还没有该天的数据，有则说明已经开始当天的计步了
                if (entity == null) {
                    //这里设置新的一天推送置为false
                    //sp.setIsSend(false);
                    //没有则新建一条数据
                    entity = new StepEntity();
                    entity.setCurDate(CURRENT_DATE);
                    entity.setSteps(String.valueOf(CURRENT_STEP));
                    entity.setUserID(Integer.parseInt(sp.getID()));//利用sp获取用户id
                    stepEntityDao.insert(entity);
                    //这里新增一条数据传到服务器
                } else {
                    //有则更新当前的数据
                    entity.setSteps(String.valueOf(CURRENT_STEP));
                    entity.setUserID(Integer.parseInt(sp.getID()));
                    stepEntityDao.update(entity);

                    //TODO 以下写更新数据传到服务器的代码
                }
            }
        }).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //主界面中需要手动调用stop方法service才会结束
        stopForeground(true);
        unregisterReceiver(mInfoReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalStepBinder();
    }
    public class LocalStepBinder extends Binder {
        public StepService getService() {
            return StepService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public interface FootStepCallback {
        void StepDataChange(StepEntity data);
    }
}