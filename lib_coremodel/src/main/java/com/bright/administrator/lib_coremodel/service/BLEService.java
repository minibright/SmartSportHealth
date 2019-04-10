package com.bright.administrator.lib_coremodel.service;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.bright.administrator.lib_coremodel.bean.BleData;
import com.bright.administrator.lib_coremodel.constant.BaseEventbusBean;
import com.bright.administrator.lib_coremodel.utils.UUIDUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BLEService extends Service {
    private static final String TAG =BLEService.class.getSimpleName();
    public static final int SUCCESSCODE = 1;
    public static final int FAILCODE_DEVICENOFOUND = 2;
    public static final int FAILCODE_CONNECTFAIL = 3;
    public static final int FAILCODE_NOTIFYFAIL = 4;
    public BleDevice mBleDevice;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    public int[] point = new int[3];  //存放蓝牙的数据

    public void setBleDeviceCallback(BleDeviceCallback bleDeviceCallback) {
        mBleDeviceCallback = bleDeviceCallback;
    }

    private BleDeviceCallback mBleDeviceCallback;//回调BleDevice对象
    @Override
    public void onCreate() {
        super.onCreate();
        initBluetooth();//初始化蓝牙
    }

    private void initBluetooth() {
        BleManager.getInstance().init(getApplication()); //初始化
        BleManager.getInstance()//初始化配置
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, Service.START_FLAG_RETRY, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //返回与服务通信的中介者对象
        return new LocalBinder();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }
    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }

    }

    public void setScanRule() {
        //配置扫描规则
        String mac = "C1:05:8C:B6:DB:00";
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(new UUID[]{UUIDUtils.UUID_LOST_SERVICE})      // 只扫描指定的服务的设备，可选
                //.setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(true)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }
    public void scanAndConnect(){
        //扫描并连接
        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanStarted(boolean success) {
                // 开始扫描

            }

            @Override
            public void onScanFinished(BleDevice scanResult) {
                // 扫描结束，结果即为扫描到的第一个符合扫描规则的BLE设备，如果为空表示未搜索到
                if(scanResult == null){
                    mBleDeviceCallback.getBleDevice(null,FAILCODE_DEVICENOFOUND);
                }

            }

            @Override
            public void onStartConnect() {
                // 开始连接
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                // 连接失败
               /* mHandler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(getApplicationContext(), "连接失败!", Toast.LENGTH_SHORT).show();
                    }
                });*/
                mBleDeviceCallback.getBleDevice(bleDevice,FAILCODE_CONNECTFAIL);

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                // 连接成功，BleDevice即为所连接的BLE设备
               /* mHandler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(getApplicationContext(), "连接成功!", Toast.LENGTH_SHORT).show();
                    }
                });*/
                //mBleDeviceCallback.getBleDevice(bleDevice,SUCCESSCODE);//通过回调将bleDevice数据发送出去
                mBleDevice = bleDevice; //拿到扫描到的ble设备了
                notifyCharacteristic();//订阅通知notify
                readRssi(bleDevice);//获取设备的信号强度Rssi
                setMtu(bleDevice, 23);//设置最大传输单元MTU
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                // 连接断开，isActiveDisConnected是主动断开还是被动断开
                if (isActiveDisConnected) {

                } else {
                   // ObserverManager.getInstance().notifyObserver(device); //使用观察者模式断开蓝牙连接
                }
            }
        });
    }
    public void notifyCharacteristic() {//订阅通知notify
        BleManager.getInstance().notify(
                mBleDevice,
                //蓝牙的UUID
                UUIDUtils.UUID_LOST_SERVICE.toString(),
                //通知的UUID
                UUIDUtils.READ_CHARACTERISTIC_UUID.toString(),
                new BleNotifyCallback() {
                    // 打开通知操作成功
                    @Override
                    public void onNotifySuccess() {
                        //只有连接成功并且通知成功，才通过回调将bleDevice数据发送出去
                        mBleDeviceCallback.getBleDevice(mBleDevice,SUCCESSCODE);
                        /*mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                Toast.makeText(getApplicationContext(), "打开通知操作成功!", Toast.LENGTH_SHORT).show();
                            }
                        });*/
                    }
                    // 打开通知操作失败
                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        mBleDeviceCallback.getBleDevice(mBleDevice,FAILCODE_NOTIFYFAIL);//通过回调将bleDevice数据发送出去
                        /*mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                Toast.makeText(getApplicationContext(), "打开通知操作失败!", Toast.LENGTH_SHORT).show();
                            }
                        });*/
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        if (retriveData(data)) {
                            return; //没有取到8比特数据则返回
                        }

                        BleData characterBleData = new BleData();
                        // HH:mm:ss
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                        //获取当前时间
                        Date date = new Date(System.currentTimeMillis());
                        characterBleData.setPressure0(point[0]);
                        characterBleData.setPressure1(point[1]);
                        characterBleData.setPressure2(point[2]);
                        characterBleData.setTime(simpleDateFormat.format(date));
                        sendBLEData(characterBleData);//通知activity数据更新,使用eventbus发送事件
                        //Logger.d("sendBLEData(characterBleData) "+point[0]+" "+point[1]+" "+point[2]);


                    }
                });
    }
    private void sendBLEData(BleData characterBleData) {
        //EventBus.getDefault().post(characterBleData);  //eventbus发送蓝牙数据
        EventBus.getDefault().post(new BaseEventbusBean<BleData>(1,characterBleData));  //eventbus发送蓝牙数据
    }
    private boolean retriveData(byte[] data) {  //取数据
        /*if(data.length != 7) {
            return true;
        }*/
        if(data.length != 8) {
            return true;
        }
        int[] val;
        val = new int[data.length];
        int index=0;
        for (byte b : data){
            val[index++]=b & 0xff;      //将蓝牙传过来的比特值放在val[]数组中
        }
        if(val[0]==181 && val[1]==18 && val[5]==71  && val[6]==13 && val[7]==10)  //读取数据的相关处理
        //if(val[0]==181 && val[1]==18 && val[5]==13  && val[6]==10)  //读取数据的相关处理
        {
            for (int i=0;i<3;i++)
            {
                point[i]=255-val[i+2];   // ***************val[2],val[3],val[4]此处给三个点赋值**************
            }
        }
        return false;
    }


    //通过mac地址连接蓝牙
    public void connect(String mac) {
        BleManager.getInstance().connect(mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }// 开始连接

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {// 连接失败


            }

            @Override                      // 连接成功，BleDevice即为所连接的BLE设备
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {


                readRssi(bleDevice);//获取设备的信号强度Rssi
                setMtu(bleDevice, 23);//设置最大传输单元MTU
            }

            @Override                      // 连接中断，isActiveDisConnected表示是否是主动调用了断开连接方法
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {


                if (isActiveDisConnected) {

                } else {

                    //ObserverManager.getInstance().notifyObserver(bleDevice); //使用观察者模式断开蓝牙连接
                }
            }
        });
    }
    private void readRssi(BleDevice bleDevice) {//获取设备的信号强度Rssi
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {

            }

            @Override
            public void onRssiSuccess(int rssi) {

            }
        });
    }
    private void setMtu(BleDevice bleDevice, int mtu) { //设置最大传输单元MTU
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {// 设置MTU失败

            }

            @Override
            public void onMtuChanged(int mtu) {// 设置MTU成功，并获得当前设备传输支持的MTU值

            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();//断开所有设备
        BleManager.getInstance().destroy();//退出使用，清理资源
    }
    public interface BleDeviceCallback{
        void getBleDevice(BleDevice bleDevice,int code);
    }
}
