package com.bright.administrator.lib_coremodel.d_arouter;

/**
 * @Created by TOME .
 * @时间 2018/4/26 10:20
 * @描述 ${路由地址}
 */
// 注意事项！！！  /模块/页面名称   或  /模块/子模块../页面名称   至少两级  例子 /商城/商品详情 /shop/shopDetails
    //url 第一次相同会报错??
public interface RouterURLS {

    /**首页Fragment*/
    String MainFragment = "/fragmentmain/list";
    /**运动Fragment*/
    String SportFragment = "/fragmentsport/list";
    /**我的Fragment*/
    String MineFragment = "/fragmentmine/list";
    /**设置Fragment*/
    String SettingFragment = "/fragmentsetting/list";
    //PressureTestActivity
    String PressureTestActivity = "/fragmentmain/PressureTestActivity";
    //PressureTestActivity
    String WeightTestActivity = "/fragmentmain/WeightTestActivity";
    //ShowDataActivity
    String ShowDataActivity = "/fragmentmain/ShowDataActivity";
    //ShowDataActivity
    String DataChartActivity = "/fragmentmain/DataChartActivity";
    //ShowDataActivity
    String DynamicMapTraceActivity = "/fragmentsport/DynamicMapTraceActivity";


}
