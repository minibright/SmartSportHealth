package com.bright.module_main.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bright.administrator.lib_common.base.mvp.BaseVpActivity;
import com.bright.administrator.lib_common.util.StringAxisValueFormatter;
import com.bright.administrator.lib_common.widget.customView.MyMarkerView;
import com.bright.administrator.lib_coremodel.bean.BleData;
import com.bright.administrator.lib_coremodel.d_arouter.RouterURLS;
import com.bright.administrator.lib_coremodel.dao.AppDatabase;
import com.bright.administrator.lib_coremodel.dao.BleDataDao;
import com.bright.module_main.R;
import com.bright.module_main.R2;
import com.bright.module_main.contract.DataChartContract;
import com.bright.module_main.contract.DataChartContract.Presenter;
import com.bright.module_main.contract.DataChartContract.View;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

@Route(path = RouterURLS.DataChartActivity)
public class DataChartActivity extends BaseVpActivity<DataChartContract.View,DataChartContract.Presenter>
                               implements DataChartContract.View,
                                            OnChartGestureListener,
                                            OnChartValueSelectedListener{
    @BindView(R2.id.chart1)
    public LineChart mChart;
    private List<BleData> lists;
    private BleDataDao mBleDataDao;
    private XAxis xAxis;  //x轴
    private ArrayList<String> xValues; //存放x轴的数据
    protected Typeface mTfRegular;
    protected Typeface mTfLight;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_datachart;
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        mTfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        mBleDataDao = AppDatabase.getInstance(this).bleDataDao();//获取数据库操作dao
        //初始化表格
        initChart();
        //设置监听
        setChartListener();
        //设置限制线
        setLimitLine();
        //添加数据
        setData();
        //设置x轴开始的动画。调用此函数后会自动刷新图表
        mChart.animateX(2500);
        //设置图例
        setLegend();


    }

    private void setLegend() {
        Legend l = mChart.getLegend();
        // modify the legend ...
        l.setForm(LegendForm.LINE);
        l.setTypeface(mTfLight);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK); //设置特例字体的颜色
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }

    private void setLimitLine() {
        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");//字体格式
        LimitLine ll1 = new LimitLine(120f, "Upper Limit");  //上限制线
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(90f, "Lower Limit");  //下限制线
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);     //以虚线的模式绘制
        ll2.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setTypeface(tf);

        YAxis leftAxis = mChart.getAxisLeft();//获取y轴
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1); //添加限制线
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum(255f); //设置左y轴的最大最小值
        leftAxis.setAxisMinimum(0f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);//不显示右y轴
    }

    private void setChartListener() {
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
    }

    private void initChart() {
        // no description text
        Description description = new Description();
        description.setText("时间—压力折线图");
        description.setTextSize(15f);
        mChart.setDescription(description);//文本描述
        // enable touch gestures
        mChart.setTouchEnabled(true);      //设置图表可触摸，可拖拽，可缩放
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        //不设置网格背景
        mChart.setDrawGridBackground(false);
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);//设置maker
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart
        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");//x轴限制线
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);//以虚线的模式绘制
        llXAxis.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);//x轴以虚线的模式绘制
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//横坐标放在下方
    }

    private void setData() {

        ArrayList<Entry> yVals0 = new ArrayList<Entry>();   //添加y轴的数据
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();   //添加y轴的数据
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();   //添加y轴的数据
        xValues = new ArrayList<String>();       //添加x轴的数据

        //取出数据库中的数据
        lists = mBleDataDao.getAllBleData();
        int size = lists.size(); //列表中数据的个数
        if(size == 0){
            return;
        }
        for (int i = 0; i < size; i++) {
            /////////////////////////////////////在此出添加数据/////////////////////////////////////
            String val3 = lists.get(i).getTime();
            float val0 = (float) lists.get(i).getPressure0();
            float val1 = (float) lists.get(i).getPressure1();
            float val2 = (float) lists.get(i).getPressure2();
            xValues.add(val3);
            yVals0.add(new Entry(i, val0, getResources().getDrawable(R.drawable.star)));//添加数据条目
            yVals1.add(new Entry(i, val1, getResources().getDrawable(R.drawable.star)));//添加数据条目
            yVals2.add(new Entry(i, val2, getResources().getDrawable(R.drawable.star)));//添加数据条目
        }

        LineDataSet set0;  //数据集1
        LineDataSet set1;  //数据集2
        LineDataSet set2;  //数据集3

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {       //查看数据集是否存在
            set0 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set0.setValues(yVals0);
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set1.setValues(yVals1);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(2);
            set2.setValues(yVals2);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set0 = new LineDataSet(yVals0, "DataSet 1");      //创建线状数据集1
            set0.setDrawIcons(false); //不显示图标
            set1 = new LineDataSet(yVals1, "DataSet 2");      //创建线状数据集2
            set1.setDrawIcons(false); //不显示图标
            set2 = new LineDataSet(yVals2, "DataSet 3");      //创建线状数据集3
            set2.setDrawIcons(false); //不显示图标

            set0.setAxisDependency(YAxis.AxisDependency.LEFT);
            set0.setColor(Color.YELLOW);   //设置线的颜色
            set0.setCircleColor(Color.BLACK); //设置点的颜色
            set0.setLineWidth(2f);
            set0.setCircleRadius(3f);
            set0.setFillAlpha(65);
            set0.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            set0.setDrawCircleHole(false);
            set0.setHighLightColor(Color.rgb(244, 117, 117));
            // set the line to be drawn like this "- - - - - -"
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.GRAY);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);

            // create a dataset and give it a type
            set2.setAxisDependency(YAxis.AxisDependency.LEFT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.BLACK);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));

            // create a data object with the datasets
            LineData data = new LineData(set0, set1, set2);
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(9f);
            // set data
            mChart.setData(data);
            //设置x轴坐标显示的值
            xAxis.setValueFormatter(new StringAxisValueFormatter(xValues));
        }
    }

    @Override
    public Presenter createPresenter() {
        return new DataChartPresenter();
    }

    @Override
    public View createView() {
        return this;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartGesture lastPerformedGesture) {
        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) {
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
        }
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
