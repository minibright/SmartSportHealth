package com.bright.module_main.contract;

import com.bright.administrator.lib_common.base.mvp.inter.IModel;
import com.bright.administrator.lib_common.base.mvp.inter.IPresenter;
import com.bright.administrator.lib_common.base.mvp.inter.IView;

public interface DataChartContract {
    interface View extends IView{

    }
    interface Presenter extends IPresenter<View>{

    }
    interface Model extends IModel{

    }
}
