package com.bright.module_main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bright.administrator.lib_coremodel.bean.BleData;
import com.bright.module_main.R;

import java.util.ArrayList;
import java.util.List;

public class MyBleDataAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private List<BleData> mDatas;
    public MyBleDataAdapter(Context context, ArrayList<BleData> datas){
        mContext = context;
        mDatas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View itemView = View.inflate(mContext, R.layout.bledata_item, null);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bledata_item,parent,false);
        return new ItemBleDataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder instanceof ItemBleDataViewHolder){
            BleData bleData = mDatas.get(position);
            ((ItemBleDataViewHolder) holder).tvTime.setText(bleData.getTime());
            ((ItemBleDataViewHolder) holder).tvPressture0.setText(bleData.getPressure0()+"");
            ((ItemBleDataViewHolder) holder).tvPressture1.setText(bleData.getPressure1()+"");
            ((ItemBleDataViewHolder) holder).tvPressture2.setText(bleData.getPressure2()+"");
        }

    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }
    /**
     * 添加数据
     * @param position
     * @param data
     */
    public void addData(int position, BleData data) {
        mDatas.add(position,data);
        //刷新适配器
        notifyItemInserted(position);
    }

    /**
     * 移除数据
     * @param position
     */
    public void removeData(int position) {
        mDatas.remove(position);
        //刷新适配器
        notifyItemRemoved(position);
    }
    class ItemBleDataViewHolder extends ViewHolder {
        TextView tvTime;
        TextView tvPressture0;
        TextView tvPressture1;
        TextView tvPressture2;
        ItemBleDataViewHolder(View itemView) {
            super(itemView);
             tvTime = itemView.findViewById(R.id.tv_time);
             tvPressture0 = itemView.findViewById(R.id.tv_pressture0);
             tvPressture1 = itemView.findViewById(R.id.tv_pressture1);
             tvPressture2 = itemView.findViewById(R.id.tv_pressture2);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null){
                        onItemClickListener.onItemClick(v,mDatas.get(getLayoutPosition()),getLayoutPosition());
                    }
                }
            });
        }
    }
    /**
     * 点击RecyclerView某条的监听
     */
    public interface OnItemClickListener{

        /**
         * 当RecyclerView某个被点击的时候回调
         * @param view 点击item的视图
         * @param data 点击得到的数据
         */
        void onItemClick(View view, BleData data, int index);

    }

    private  OnItemClickListener onItemClickListener;

    /**
     * 设置RecyclerView某个的监听
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
