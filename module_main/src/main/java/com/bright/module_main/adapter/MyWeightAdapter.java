package com.bright.module_main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bright.administrator.lib_coremodel.bean.WeightData;
import com.bright.module_main.R;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MyWeightAdapter extends Adapter<ViewHolder> {
    private Context mContext;
    private ArrayList<WeightData> mDatas;
    public MyWeightAdapter(Context context, ArrayList<WeightData> datas){
        mContext = context;
        mDatas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View itemView = View.inflate(mContext, R.layout.list_item_weight, null);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_weight,parent,false);
        return new ItemWeightViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DecimalFormat df = new DecimalFormat("#.#");
        if(holder instanceof ItemWeightViewHolder){
            WeightData weightData = mDatas.get(position);
            ((ItemWeightViewHolder) holder).weight_time.setText(weightData.getHmTime());
            ((ItemWeightViewHolder) holder).weight_value.setText(df.format(weightData.getWeight()));
            ((ItemWeightViewHolder) holder).weight_datatime.setText(weightData.getDataTime());
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
    public void addData(int position, WeightData data) {
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
    class ItemWeightViewHolder extends RecyclerView.ViewHolder {
         TextView weight_time;
         TextView weight_value;
         TextView weight_datatime;
         ItemWeightViewHolder(View itemView) {
            super(itemView);
            weight_time = (TextView) itemView.findViewById(R.id.weight_time);
            weight_value = (TextView) itemView.findViewById(R.id.weight_value);
             weight_datatime = (TextView) itemView.findViewById(R.id.datatime);
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
        void onItemClick(View view, WeightData data, int index);

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
