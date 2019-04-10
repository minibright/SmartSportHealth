package com.bright.administrator.lib_coremodel.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class WeightData {
    @PrimaryKey(autoGenerate = true)
    private Integer id;
    private Double weight;
    private String dataTime;
    private String hmTime;
    public Double getWeight() {
        return weight;
    }
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getHmTime() {
        return hmTime;
    }
    public void setHmTime(String hmTime) {
        this.hmTime = hmTime;
    }


    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "WeightData{" +
                "id=" + id +
                ", weight=" + weight +
                ", dataTime='" + dataTime + '\'' +
                ", hmTime='" + hmTime + '\'' +
                '}';
    }
}
