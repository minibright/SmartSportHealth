package com.bright.module_sport;

import com.bright.administrator.lib_common.base.mvp.BasePresenter;
import com.bright.module_sport.contract.SportContract;
import com.bright.module_sport.contract.SportContract.Model;

public class SportPresenter extends BasePresenter<SportContract.View,SportContract.Model> implements SportContract.Presenter {

    @Override
    protected Model createModel() {
        return new SportModel();
    }

    @Override
    public void checkGpsSetting() {
        mView.goGpsSetting();
    }
}
