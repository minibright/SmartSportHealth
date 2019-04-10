package com.bright.module_main.activity;

import com.bright.administrator.lib_common.base.mvp.BasePresenter;
import com.bright.module_main.contract.DataChartContract;
import com.bright.module_main.contract.DataChartContract.Model;

public class DataChartPresenter extends BasePresenter<DataChartContract.View,DataChartContract.Model> implements DataChartContract.Presenter{

    @Override
    protected Model createModel() {
        return null;
    }
}
