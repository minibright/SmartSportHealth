package com.bright.module_main.activity;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bright.administrator.lib_common.base.mvc.BaseVcActivity;
import com.bright.administrator.lib_common.widget.customView.CircleTextView;
import com.bright.administrator.lib_coremodel.bean.BleData;
import com.bright.administrator.lib_coremodel.constant.BaseEventbusBean;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.administrator.lib_coremodel.dao.AppDatabase;
import com.bright.administrator.lib_coremodel.dao.BleDataDao;
import com.bright.module_main.R;
import com.bright.module_main.R2;
import com.orhanobut.logger.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@Route(path = RouterURLS.PressureTestActivity)
public class PressureTestActivity extends BaseVcActivity implements View.OnClickListener{
    private static final String TAG =PressureTestActivity.class.getSimpleName();
    @Autowired
    public String address;
    @Autowired
    public String name;
    @BindView(R2.id.point1_textView)
    public CircleTextView mDataView1;
    @BindView(R2.id.point2_textView)
    public CircleTextView mDataView2;
    @BindView(R2.id.point3_textView)
    public CircleTextView mDataView3;//显示数字的文本
    @BindView(R2.id.btn_back)
    public ImageView backBtn;//回退按钮
    @BindView(R2.id.btn_front)
    public ImageView frontBtn;//前进按钮
    @BindView(R2.id.deveice_name)
    public TextView deviceNameTxtView;//设备名字文本
    @BindView(R2.id.btn_chart)
    public Button btnChart;
    @BindView(R2.id.btn_start)
    public Button btnStart;

    private int point[]; //用来存放显示的压力数值
    public static boolean isStart;
    public String time;
    final int[] colors = new int[]{
            Color.TRANSPARENT,//透明
            Color.WHITE,//白色
            Color.LTGRAY,//浅灰色
            Color.GRAY,//灰色
            Color.DKGRAY,//灰黑色
            Color.RED,//红色
            Color.YELLOW,//黄色
            Color.GREEN,//绿色
            Color.CYAN,//青绿色
            Color.BLUE,//蓝色
            Color.MAGENTA//红紫色
    };
    BleDataDao bleDataDao;
    private PublishSubject<BleData> mPublishSubject;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_foot;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        super.regEvent=true;
        mDataView1.setBackgroundColor(Color.TRANSPARENT);
        mDataView2.setBackgroundColor(Color.TRANSPARENT);
        mDataView3.setBackgroundColor(Color.TRANSPARENT);//初始化时将背景设成透明
        deviceNameTxtView.setText(name);
        point=new int[3];
        bleDataDao= AppDatabase.getInstance(this).bleDataDao();
        mPublishSubject = PublishSubject.create();
        DisposableObserver<BleData> disposableObserver = new DisposableObserver<BleData>() {
            @Override
            public void onNext(BleData bleData) {
                //保存到数据库
                bleDataDao.insert(bleData);
                Logger.d(" bleDataDao.insert(bleData)"+bleData);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        mPublishSubject.subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(Schedulers.io())
                        .subscribe(disposableObserver);
        addDisposable(disposableObserver);
        setListener();
    }

    private void setListener() {
        btnChart.setOnClickListener(this);
        backBtn.setOnClickListener(this);//后退按钮
        frontBtn.setOnClickListener(this);//前进按钮
        btnStart.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_back) {
            finish();//关闭当前activity

        } else if (i == R.id.btn_front) {
            ARouter.getInstance().build(RouterURLS.ShowDataActivity).navigation();

        } else if (i == R.id.btn_chart) {
            ARouter.getInstance().build(RouterURLS.DataChartActivity).navigation();

        } else if (i == R.id.btn_start) {
            if (isStart) {
                btnStart.setText("结束测试");
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bleDataDao.deleteAll();
                    }
                }).start();*/
                bleDataDao.deleteAll();
            } else {
                btnStart.setText("开始测试");
            }
            isStart = !isStart;

        } else {

        }

    }
    @Override
    protected void onEvent(BaseEventbusBean event) {
        if(event.getObj() instanceof BleData){
            BleData bleData = (BleData) event.getObj();
            point[0] = bleData.getPressure0();
            point[1] = bleData.getPressure1();
            point[2]= bleData.getPressure2();
            time = bleData.getTime();
            updateUi();

            if (point[0] != 0 || point[1] != 0 || point[2] != 0){
                mPublishSubject.onNext(bleData);
                //Logger.d("e.onNext(bleData)"+bleData);
            }

        }
    }

    private void updateUi() {
        mDataView1.setBackgroundColor(colors[point[0]/22]);
        mDataView2.setBackgroundColor(colors[point[1]/22]);
        mDataView3.setBackgroundColor(colors[point[2]/22]);
        mDataView1.setText(point[0]+"");
        mDataView2.setText(point[1]+"");
        mDataView3.setText(point[2]+"");
    }
}
