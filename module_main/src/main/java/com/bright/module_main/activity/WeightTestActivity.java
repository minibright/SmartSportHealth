package com.bright.module_main.activity;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bright.administrator.lib_common.base.mvc.BaseVcActivity;
import com.bright.administrator.lib_common.widget.customView.DividerListItemDecoration;
import com.bright.administrator.lib_coremodel.bean.BleData;
import com.bright.administrator.lib_coremodel.bean.WeightData;
import com.bright.administrator.lib_coremodel.constant.BaseEventbusBean;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.administrator.lib_coremodel.dao.AppDatabase;
import com.bright.administrator.lib_coremodel.dao.WeightDataDao;
import com.bright.module_main.R;
import com.bright.module_main.adapter.MyWeightAdapter;
import com.bright.module_main.adapter.MyWeightAdapter.OnItemClickListener;
import com.orhanobut.logger.Logger;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@Route(path = RouterURLS.WeightTestActivity)
public class WeightTestActivity extends BaseVcActivity{
    private RecyclerView mRecyclerView;
    private TextView weightValue;
    private ArrayList<WeightData> mDatas;
    private Integer[] point;
    private PublishSubject<BleData> mPublishSubject;
    MyWeightAdapter myWeightAdapter;
    WeightDataDao mWeightDataDao;
    DecimalFormat df = new DecimalFormat("#.0");
    //从数据库中查询到的体重数据
    List<WeightData> allWeightData;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_weight;
    }

    @Override
    protected void initTitle() {
        initData();
    }
    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        super.regEvent=true;
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //体重值
        weightValue = findViewById(R.id.weight_value);
        //数据库中有体重数据时
        if(allWeightData.size()>0){
            weightValue.setText(df.format(mDatas.get(0).getWeight()));
        }else{
            weightValue.setText("暂无体重数据");
        }
        myWeightAdapter = new MyWeightAdapter(this, mDatas);
        myWeightAdapter.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(View view, WeightData data, int index) {
                //点击条目时执行
                Toast.makeText(WeightTestActivity.this,"item"+index+"被点击", Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(myWeightAdapter);
        //添加RecyclerView的分割线
        mRecyclerView.addItemDecoration(new DividerListItemDecoration(WeightTestActivity.this, DividerListItemDecoration.VERTICAL_LIST));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(WeightTestActivity.this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
    private void initData() {
        mDatas= new ArrayList<>();
        point = new Integer[3];
        mWeightDataDao = AppDatabase.getInstance(this).weightDataDao();
        //TODO 从蓝牙得到体重数据
        allWeightData = mWeightDataDao.getAllWeightData();
        Collections.reverse(allWeightData);
        if(allWeightData.size()>0){
            mDatas.addAll(allWeightData);
        }
        mPublishSubject = PublishSubject.create();
        DisposableObserver<List<BleData>> disposableObserver = new DisposableObserver<List<BleData>>() {
            @Override
            public void onNext(List<BleData> list) {
                int size = list.size();
                //至少四个数据才计算
                if(size >= 4){
                    BleData meanData = new BleData();
                    int sum0 = 0;
                    int sum1 = 0;
                    int sum2 = 0;
                    Iterator<BleData> iterator = list.iterator();
                    while (iterator.hasNext()){
                        BleData next = iterator.next();
                        sum0 += next.getPressure0();
                        sum1 +=next.getPressure1();
                        sum2 +=next.getPressure2();
                    }
                    meanData.setPressure0(sum0/size);
                    meanData.setPressure1(sum1/size);
                    meanData.setPressure2(sum2/size);
                    //保存到数据库
                    insertWeightData(meanData);
                    Logger.d("共接受"+size+"个数据,平均值为"+meanData.toString());
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        mPublishSubject.buffer(3000, TimeUnit.MILLISECONDS)
                       //.observeOn(AndroidSchedulers.mainThread())
                       .observeOn(Schedulers.io())
                       .subscribe(disposableObserver);
        addDisposable(disposableObserver);
    }
    @Override
    protected void onEvent(BaseEventbusBean event) {
        if(event.getObj() instanceof BleData){
            BleData bleData = (BleData) event.getObj();
            Integer pressure0 = bleData.getPressure0();
            Integer pressure1 = bleData.getPressure1();
            Integer pressure2 = bleData.getPressure2();
            String time = bleData.getTime();
            if (pressure0 != 0 || pressure1 != 0 || pressure2 != 0){
                mPublishSubject.onNext(bleData);
            }

        }
    }
    private void insertWeightData(BleData bleData) {
        point[0] = bleData.getPressure0();
        point[1] = bleData.getPressure1();
        point[2]= bleData.getPressure2();
        Date date = new Date(System.currentTimeMillis());//获取当前时间
        String hmTime = new SimpleDateFormat("HH:mm:ss").format(date);
        String dataTime = new SimpleDateFormat("yyyy-MM-dd").format(date);
        Double weight = (point[0] + point[1] + point[2]) / 3.0;
        DecimalFormat df = new DecimalFormat("#.#");
        df.format(weight);
        WeightData weightData = new WeightData();
        weightData.setDataTime(dataTime);
        weightData.setWeight(weight);
        weightData.setHmTime(hmTime);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                weightValue.setText(df.format(weight));
                myWeightAdapter.addData(0,weightData);
                mRecyclerView.scrollToPosition(0);
            }
        });
        mWeightDataDao.insert(weightData);//插入体重数据
        Logger.d(weightData);
    }
}
