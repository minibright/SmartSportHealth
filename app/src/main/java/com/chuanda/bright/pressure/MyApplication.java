package com.chuanda.bright.pressure;


import com.bright.administrator.lib_common.constants.BaseApplication;

public class MyApplication extends BaseApplication {
    private static MyApplication myApplication;

    public static MyApplication getInstance() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this ;
    }
}
