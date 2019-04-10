package com.bright.module_setting;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bright.administrator.lib_common.base.mvp.BaseVpFragment;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.module_setting.contract.SettingContract;
import com.bright.module_setting.contract.SettingContract.presenter;
import com.bright.module_setting.contract.SettingContract.view;

@Route(path = RouterURLS.SettingFragment)
public class SettingFragment extends BaseVpFragment<SettingContract.view,SettingContract.presenter> implements SettingContract.view {


    @Override
    protected int getLayout() {
        return R.layout.fragment_plan;
    }

    @Override
    protected void initView() {

    }

    @Override
    public presenter createPresenter() {
        return new SettingPresenter();
    }

    @Override
    public view createView() {
        return this;
    }
}
