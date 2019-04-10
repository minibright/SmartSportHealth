package com.bright.module_main.contract;

import android.content.Context;

import com.bright.administrator.lib_common.base.mvp.inter.IModel;
import com.bright.administrator.lib_common.base.mvp.inter.IPresenter;
import com.bright.administrator.lib_common.base.mvp.inter.IView;
import com.bright.administrator.lib_coremodel.bean.StepEntity;
import com.bright.administrator.lib_coremodel.service.BLEService;
import com.bright.module_main.activity.GoAction;
import com.clj.fastble.data.BleDevice;

import java.util.Map;


public interface MainContract {
    interface View extends IView {
        void setSportData(Map<String,String> data);
        void setInitialSportData(Map<String,String> data);
        void goBlueToothSetting();
    }

    interface Presenter extends IPresenter<View> {
        void getSportData(StepEntity data);
        void initSportData(String curDate, String userID);
        void conncetBleDevice(BLEService bleService, GoAction action);
        void openBlueTooth();
    }

    interface Model extends IModel {
        StepEntity getInitialStepEntityData(Context context,String curDate, String userID);
    }



}

