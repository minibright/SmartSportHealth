package com.bright.module_setting;

import com.bright.administrator.lib_common.base.mvp.BasePresenter;
import com.bright.module_setting.contract.SettingContract;
import com.bright.module_setting.contract.SettingContract.model;

public class SettingPresenter extends BasePresenter<SettingContract.view,SettingContract.model> implements SettingContract.presenter{
    @Override
    protected model createModel() {
        return null;
    }
}
