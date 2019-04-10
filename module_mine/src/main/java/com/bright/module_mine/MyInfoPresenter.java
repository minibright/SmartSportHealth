package com.bright.module_mine;

import com.bright.administrator.lib_common.base.mvp.BasePresenter;
import com.bright.module_mine.MyInfoContract.MyInfoContract;
import com.bright.module_mine.MyInfoContract.MyInfoContract.model;

public class MyInfoPresenter extends BasePresenter<MyInfoContract.view,MyInfoContract.model> implements MyInfoContract.presenter {

    @Override
    protected model createModel() {
        return null;
    }
}
