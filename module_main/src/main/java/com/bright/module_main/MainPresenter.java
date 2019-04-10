package com.bright.module_main;

import android.app.Activity;

import com.bright.administrator.lib_common.base.mvp.BasePresenter;
import com.bright.administrator.lib_common.util.ActivityUtils;
import com.bright.administrator.lib_common.util.ToastUtils;
import com.bright.administrator.lib_coremodel.bean.StepEntity;
import com.bright.administrator.lib_coremodel.service.BLEService;
import com.bright.module_main.activity.GoAction;
import com.bright.module_main.contract.MainContract;
import com.bright.module_main.contract.MainContract.Model;
import com.bright.module_main.contract.MainContract.View;
import com.clj.fastble.data.BleDevice;
import com.orhanobut.logger.Logger;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter extends BasePresenter<MainContract.View, MainContract.Model>implements MainContract.Presenter {
    public Activity mCurrentActivity;
    private DecimalFormat df = new DecimalFormat("#.##");
    private DecimalFormat df2 = new DecimalFormat("#");
    @Override
    protected Model createModel() {
        return new MainModel();
    }

    @Override
    public void getSportData(StepEntity data) {
        //addDisposable(mModel.getStepEntityData().subscribeWith();//也可以取数据库取数据
        Map<String,String> map = new HashMap<>();
        String steps = data.getSteps();
        map.put("steps",steps);
        String toatalHeat = countTotalHeat(Integer.parseInt(steps));
        map.put("toatalHeat",toatalHeat);
        String totalKM = countTotalKM(Integer.parseInt(steps));
        map.put("totalKM",totalKM);
        mView.setSportData(map);//发送给view取更新页面
    }

    @Override
    public void initSportData(String curDate, String userID) {
        Disposable subscribe = Observable.create(
                    (ObservableOnSubscribe<Map<String, String>>) e -> {
                    StepEntity initialData = mModel.getInitialStepEntityData(getContext(), curDate, userID);
                    if (initialData != null) {
                        Map<String, String> map = new HashMap<>();
                        String steps = initialData.getSteps();
                        Logger.d(steps);
                        map.put("steps", steps);
                        String toatalHeat = countTotalHeat(Integer.parseInt(steps));
                        map.put("toatalHeat", toatalHeat);
                        String totalKM = countTotalKM(Integer.parseInt(steps));
                        map.put("totalKM", totalKM);
                        e.onNext(map);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(map -> mView.setInitialSportData(map));
        addDisposable(subscribe);

    }

    @Override
    public void conncetBleDevice(BLEService bleService, GoAction action) {
        if (bleService.mBleDevice != null){
            action.go();
            return;
        }
        mView.showHUD("开始连接蓝牙");
        bleService.setBleDeviceCallback(
                (bleDevice, code) -> Observable
                        .create((ObservableOnSubscribe<BleDevice>) e -> {
                                if(code == BLEService.SUCCESSCODE && bleDevice != null){
                                    e.onNext(bleDevice);
                                }else{
                                    switch (code){
                                        case BLEService.FAILCODE_DEVICENOFOUND:
                                            throw new Exception("未搜索到指定蓝牙设备");
                                        case BLEService.FAILCODE_CONNECTFAIL:
                                            throw new Exception("蓝牙连接失败");
                                        case BLEService.FAILCODE_NOTIFYFAIL:
                                            throw new Exception("通知蓝牙失败");
                                        default:
                                            break;
                                    }

                                } })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bleDevice1 -> {
                            mView.dismissHUD();
                            ToastUtils.showCenter("连接成功");
                            //页面跳转
                            action.go();
                        }, throwable -> {
                            mView.dismissHUD();
                            ToastUtils.showCenter(throwable.getMessage());
                        }));
        //扫描蓝牙设置规则
        bleService.setScanRule();
        //连接蓝牙，订阅通知
        bleService.scanAndConnect();
    }

    @Override
    public void openBlueTooth() {
        mView.goBlueToothSetting();
    }


    /**
     * 简单的计算卡路里
     * 跑步卡路里（kcal）＝体重（kg）×距离（公里）×1.036
     * 走路卡路里=步数*0.042
     * @param steps 用户当前步数
     * @return
     */
    private String countTotalHeat(int steps) {
        double totalMeters = steps * 0.042;
        //保留整数
        return df2.format(totalMeters);
    }

    /**
     * 简易计算公里数，假设一步大约有0.7米
     * @param steps 用户当前步数
     * @return
     */
    private String countTotalKM(int steps) {
        double totalMeters = steps * 0.7;
        //保留两位有效数字
        return df.format(totalMeters / 1000);
    }
    @Override
    public void attachView(View view) {
        super.attachView(view);
        mCurrentActivity = ActivityUtils.getInstance().currentActivity();
    }
}
