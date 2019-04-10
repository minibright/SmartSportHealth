package com.bright.module_main.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.bright.administrator.lib_common.base.mvc.BaseVcActivity;
import com.bright.administrator.lib_coremodel.bean.BleData;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.administrator.lib_coremodel.dao.AppDatabase;
import com.bright.administrator.lib_coremodel.dao.BleDataDao;
import com.bright.module_main.R;
import com.bright.module_main.R2;
import com.bright.module_main.adapter.MyBleDataAdapter;
import com.bright.module_main.adapter.MyBleDataAdapter.OnItemClickListener;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

@Route(path = RouterURLS.ShowDataActivity)
public class ShowDataActivity extends BaseVcActivity {
    @BindView(R2.id.recyclerView)
    RecyclerView recyclerView;
    List<BleData> mDataList;
    BleDataDao bleDataDao;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_data;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        bleDataDao = AppDatabase.getInstance(this).bleDataDao();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initData();

    }

    private void initData() {

        Disposable subscribe = Observable.create(new ObservableOnSubscribe<List<BleData>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BleData>> e){
                mDataList = bleDataDao.getAllBleData();
                //Logger.d("mDataList的数据"+mDataList.toString());
                e.onNext(mDataList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BleData>>(){
                    @Override
                    public void accept(List<BleData> bleData) {
                        MyBleDataAdapter myBleDataAdapter = new MyBleDataAdapter(getContext(), (ArrayList<BleData>) mDataList);
                        myBleDataAdapter.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, BleData data, int index) {
                                //点击条目时执行
                                Toast.makeText(ShowDataActivity.this,"item"+index+"被点击", Toast.LENGTH_SHORT).show();
                            }
                        });
                        recyclerView.setAdapter(myBleDataAdapter);
                    }
                });
        addDisposable(subscribe);

    }
}
