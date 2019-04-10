package com.bright.module_main;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bright.administrator.lib_common.base.mvp.BaseVpFragment;
import com.bright.administrator.lib_common.util.CheckPermissionUtils;
import com.bright.administrator.lib_common.util.ToastUtils;
import com.bright.administrator.lib_coremodel.bean.StepEntity;
import com.bright.administrator.lib_coremodel.constant.BaseEventbusBean;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.administrator.lib_coremodel.service.BLEService;
import com.bright.administrator.lib_coremodel.service.BLEService.LocalBinder;
import com.bright.administrator.lib_coremodel.service.StepService.FootStepCallback;
import com.bright.administrator.lib_coremodel.service.StepService.LocalStepBinder;
import com.bright.administrator.lib_coremodel.utils.SPTools;
import com.bright.administrator.lib_coremodel.utils.TimeTools;
import com.bright.module_main.R2.id;
import com.bright.module_main.activity.GoAction;
import com.bright.module_main.contract.MainContract;
import com.bright.module_main.contract.MainContract.Presenter;
import com.bright.module_main.contract.MainContract.View;
import com.clj.fastble.data.BleDevice;

import java.util.Map;

import butterknife.BindView;

@Route(path = RouterURLS.MainFragment)
public class MainFragment extends BaseVpFragment<MainContract.View, MainContract.Presenter> implements MainContract.View, OnClickListener {
    @BindView(R2.id.home_start)
    Button btnStart;//开始运动按钮
    @BindView(R2.id.start_weight)
    Button btnStartWeight;//测量体重按钮
    @BindView(id.home_progress)
    ImageView progress;//目标图片
    @BindView(id.home_target)
    TextView target;//目标值
    @BindView(id.home_reality)
    TextView step;//步数
    @BindView(id.home_heat)
    TextView heat;//热量值
    @BindView(id.home_sport1)
    TextView sport1;//距离值
    BLEService mBLEService;

    @Override
    protected int getLayout() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView() {
        super.regEvent = true;
        btnStart.setOnClickListener(this);
        btnStartWeight.setOnClickListener(this);
        mPresenter.initSportData(TimeTools.getCurrentDate(), new SPTools(mContext).getID());
    }

    @Override
    public Presenter createPresenter() {
        return new MainPresenter();
    }

    @Override
    public View createView() {
        return this;
    }

    @Override
    public void setSportData(Map<String, String> data) {
        //获取全局的步数
        step.setText(data.get("steps"));
        //计算总公里数
        sport1.setText(data.get("totalKM"));
        //计算卡路里
        heat.setText(data.get("toatalHeat"));
        //显示进度
        showImagth(data.get("steps"));
    }

    @Override
    public void setInitialSportData(Map<String, String> data) {
        setSportData(data);
    }


    private void goPressureTestActivity(BleDevice bleDevice) {
        ARouter.getInstance().build(RouterURLS.PressureTestActivity)
                .withString("name", bleDevice.getName())
                .withString("address", bleDevice.getMac())
                .withTransition(R.anim.activity_up_in, R.anim.activity_up_out)
                .navigation();
    }


    private void goWeightTestActivity(BleDevice bleDevice) {
        ARouter.getInstance().build(RouterURLS.WeightTestActivity)
                .withString("name", bleDevice.getName())
                .withString("address", bleDevice.getMac())
                .withTransition(R.anim.activity_up_in, R.anim.activity_up_out)
                .navigation();
    }

    @Override
    public void goBlueToothSetting() {
        //对话框
        new AlertDialog.Builder(mContext)
                //设置对话框的标题
                .setTitle("提示")
                //设置对话框要传达的具体信息
                .setMessage("当前功能需要使用蓝牙")
                //反面按钮
                .setNegativeButton("取消",
                        (dialog, which) -> {
                            dialog.dismiss();//关闭对话框
                        })
                //正面按钮
                .setPositiveButton("前往设置",
                        (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                            startActivity(intent);
                        })
                //对话框消失时触发的事件
                .setCancelable(false)
                .show();
    }

    private void showImagth(String data) {
        int steps = Integer.parseInt(data);
        //TODO 显示步数图标
        Integer targ = new Integer(target.getText().toString()).intValue();//目标值
        int count = targ / 20;
        int progress_pic = R.mipmap.progress_0;
        if (steps >= 1 && steps <= count) {
            progress_pic = R.mipmap.progress_1;
        } else if (steps >= count && steps <= count * 2) {
            progress_pic = R.mipmap.progress_2;
        } else if (steps > count * 2 && steps <= count * 3) {
            progress_pic = R.mipmap.progress_3;
        } else if (steps > count * 3 && steps <= count * 4) {
            progress_pic = R.mipmap.progress_4;
        } else if (steps > count * 4 && steps <= count * 5) {
            progress_pic = R.mipmap.progress_5;
        } else if (steps > count * 5 && steps <= count * 6) {
            progress_pic = R.mipmap.progress_6;
        } else if (steps > count * 6 && steps <= count * 7) {
            progress_pic = R.mipmap.progress_7;
        } else if (steps > count * 7 && steps <= count * 8) {
            progress_pic = R.mipmap.progress_8;
        } else if (steps > count * 8 && steps <= count * 9) {
            progress_pic = R.mipmap.progress_9;
        } else if (steps > count * 9 && steps <= count * 10) {
            progress_pic = R.mipmap.progress_10;
        } else if (steps > count * 10 && steps <= count * 11) {
            progress_pic = R.mipmap.progress_11;
        } else if (steps > count * 11 && steps <= count * 12) {
            progress_pic = R.mipmap.progress_12;
        } else if (steps > count * 12 && steps <= count * 13) {
            progress_pic = R.mipmap.progress_13;
        } else if (steps > count * 13 && steps <= count * 14) {
            progress_pic = R.mipmap.progress_14;
        } else if (steps > count * 14 && steps <= count * 15) {
            progress_pic = R.mipmap.progress_15;
        } else if (steps > count * 15 && steps <= count * 16) {
            progress_pic = R.mipmap.progress_16;
        } else if (steps > count * 16 && steps <= count * 17) {
            progress_pic = R.mipmap.progress_17;
        } else if (steps > count * 17 && steps <= count * 18) {
            progress_pic = R.mipmap.progress_18;
        } else if (steps > count * 18 && steps <= count * 19) {
            progress_pic = R.mipmap.progress_19;
        } else if (steps > count * 19 && steps < count * 20) {
            progress_pic = R.mipmap.progress_20;
        }
        if (steps >= count * 20) {
            progress_pic = R.mipmap.progress_21;
        }
        progress.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), progress_pic));
    }

    @Override
    protected void onEvent(BaseEventbusBean event) {
        if (event.getObj() instanceof LocalStepBinder) {
            ((LocalStepBinder) event.getObj()).getService().setFootStepCallback(new FootStepCallback() {
                @Override
                public void StepDataChange(StepEntity data) {
                    mPresenter.getSportData(data);//发送回调取来的数据给presenter
                }
            });
        } else if (event.getObj() instanceof LocalBinder) {
            mBLEService = ((LocalBinder) event.getObj()).getService();
        }
    }


    @Override
    public void onClick(android.view.View v) {
        //压力显示
        if (v.getId() == R.id.home_start) {
            checkAndGoActivity(() -> {
                //跳转到压力测试页面
                goPressureTestActivity(mBLEService.mBleDevice);
            });
            //体重测试
        } else if (v.getId() == R.id.start_weight) {
            checkAndGoActivity(() -> {
                //跳转到体重测试页面
                goWeightTestActivity(mBLEService.mBleDevice);
            });
        }
    }

    //检查蓝牙，并跳转到指定页面
    private void checkAndGoActivity(GoAction action) {
        if (CheckPermissionUtils.checkBlueToothIsOpen()) {
            if (mBLEService != null) {
                //连接蓝牙
                mPresenter.conncetBleDevice(mBLEService, action);
            }
        } else {
            ToastUtils.showLong(mContext, "请先打开蓝牙");
            mPresenter.openBlueTooth();
        }
    }


}
