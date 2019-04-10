package com.bright.administrator.lib_common.base.mvc;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.bright.administrator.lib_common.base.mvp.inter.IView;
import com.bright.administrator.lib_common.util.ActivityUtils;
import com.bright.administrator.lib_common.util.L;
import com.bright.administrator.lib_common.util.NetUtils;
import com.bright.administrator.lib_coremodel.ServerException.ServerException;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import io.reactivex.observers.ResourceObserver;
import retrofit2.HttpException;

/**
 * @author quchao
 * @date 2017/11/27
 *观察者base
 * @param <T>
 */

public abstract class BaseVcObserver<T> extends ResourceObserver<T> {
    private IView mView;
    private String mErrorMsg;
    protected IView mDialogView = null;
    protected String msg = "正在加载中...";
    private boolean isShowError = true;
    private SmartRefreshLayout rlRefreshLayout = null;//智能下拉刷新框架



    protected BaseVcObserver(IView view){
        this.mView = view;
        this.mDialogView = view;
    }

    protected BaseVcObserver(IView view, boolean isShowHUD){
        this.mView = view;
        if (isShowHUD){
            this.mDialogView = view;
        }
    }

    protected BaseVcObserver(IView view, SmartRefreshLayout rlRefresh , boolean isShowHUD){
        this.mView = view;
        this.rlRefreshLayout = rlRefresh ;
        if (isShowHUD){
            this.mDialogView = view;
        }

    }

    protected BaseVcObserver(IView view, String msg1){
        mView = view;
        mDialogView = view;
        msg = msg1;
    }

    protected BaseVcObserver(IView view, IView dialogView , String msg1){
        mView = view;
        mDialogView = dialogView;
        msg = msg1;
    }

    protected BaseVcObserver(IView view, String errorMsg, boolean isShowError){
        this.mView = view;
        this.mErrorMsg = errorMsg;
        this.isShowError = isShowError;
    }

    /**
     *  执行开始（可选）
     *  它会在subscribe(订阅)刚开始，而事件还未发送之前被调用，可以用于做一些准备工作
     *  它总是在subscribe(订阅)所发生的线程被调用(不合适在主线程加载进度条)
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (mDialogView != null){
            mDialogView.showHUD(msg);
        }

        Activity currentActivity = ActivityUtils.getInstance().currentActivity();
        if (currentActivity != null && !NetUtils.isNetConnected(currentActivity)) {
            Toast.makeText(currentActivity, "当前无网络", Toast.LENGTH_SHORT).show();
            onComplete();
        }

    }

    /**
     * 执行结果
     */
    @Override
    public void onComplete() {
        L.d("执行结果");
        if (mDialogView != null) {
            mDialogView.dismissHUD();
        }else if (mDialogView == null && rlRefreshLayout != null){
            rlRefreshLayout.finishRefresh();
            rlRefreshLayout.finishLoadMore();
        }
    }

    /**
     * 执行错误
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        L.d("网络异常");
        if (mView == null) {
            return;
        }
        if (mErrorMsg != null && !TextUtils.isEmpty(mErrorMsg)) {
            mView.showError(mErrorMsg ,"-1");
        } else if (e instanceof ServerException) {
            mView.showError(e.toString(),"-1");
        } else if (e instanceof HttpException) {
            mView.showError("网络异常","-1");
        } else {
            mView.showError("未知错误","-1");

        }
       // if (isShowError) {
       //     mView.showError("","-1");
       // }
        onComplete();
    }
}
