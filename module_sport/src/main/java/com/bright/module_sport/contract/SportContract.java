package com.bright.module_sport.contract;

import com.bright.administrator.lib_common.base.mvp.inter.IModel;
import com.bright.administrator.lib_common.base.mvp.inter.IPresenter;
import com.bright.administrator.lib_common.base.mvp.inter.IView;

public interface SportContract {
    interface View extends IView{
        void goGpsSetting();
    }
    interface Presenter extends IPresenter<View>{
        void checkGpsSetting();
    }
    interface Model extends IModel{

    }
}
