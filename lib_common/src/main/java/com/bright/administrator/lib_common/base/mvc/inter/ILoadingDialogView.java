/*
 * Copyright (c) 18-2-1 下午1:58. XQ Yang
 */

package com.bright.administrator.lib_common.base.mvc.inter;

/**
 * @author XQ Yang
 * @date 2017/11/15  11:40
 */
public interface ILoadingDialogView {

    /**
     * 显示Dialog
     */
    void showHUD(String msg);

    /**
     * 关闭Dialog
     */
    void dismissHUD();


}
