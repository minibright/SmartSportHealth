package com.bright.module_mine;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bright.administrator.lib_common.base.mvp.BaseVpFragment;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.module_mine.MyInfoContract.MyInfoContract;
import com.bright.module_mine.MyInfoContract.MyInfoContract.presenter;
import com.bright.module_mine.MyInfoContract.MyInfoContract.view;

@Route(path = RouterURLS.MineFragment)
public class MyInfoFragment extends BaseVpFragment<MyInfoContract.view,MyInfoContract.presenter> implements MyInfoContract.view{

    @Override
    protected int getLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void initView() {

    }

    @Override
    public presenter createPresenter() {
        return new MyInfoPresenter();
    }

    @Override
    public view createView() {
        return this;
    }
}
