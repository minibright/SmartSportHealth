package com.bright.module_main;

import android.content.Context;

import com.bright.administrator.lib_common.base.mvp.DisposablePool;
import com.bright.administrator.lib_coremodel.bean.StepEntity;
import com.bright.administrator.lib_coremodel.dao.AppDatabase;
import com.bright.administrator.lib_coremodel.dao.StepEntityDao;
import com.bright.module_main.contract.MainContract;
import com.orhanobut.logger.Logger;

public class MainModel extends DisposablePool implements MainContract.Model {
    private StepEntityDao stepEntityDao;
    private StepEntity stepEntity;
    @Override
    public StepEntity getInitialStepEntityData(Context context,String curDate, String userID) {
        stepEntityDao = AppDatabase.getInstance(context).stepEntityDao();//数据库操作对象
        stepEntity = stepEntityDao.findStepEntityByDate(curDate, userID);
        //Logger.d("使用stepEntityDao查询数据库");
        return stepEntity;
    }

}
