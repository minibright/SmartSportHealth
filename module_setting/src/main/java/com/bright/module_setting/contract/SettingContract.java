package com.bright.module_setting.contract;

import com.bright.administrator.lib_common.base.mvp.inter.IModel;
import com.bright.administrator.lib_common.base.mvp.inter.IPresenter;
import com.bright.administrator.lib_common.base.mvp.inter.IView;

public interface SettingContract {
    interface view extends IView{

    }
    interface presenter extends IPresenter<view>{

    }
    interface model extends IModel{

    }
}
