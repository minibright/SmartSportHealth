package com.bright.administrator.lib_coremodel.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class BleData {
    @PrimaryKey(autoGenerate = true)
    private Integer id;
    private Integer pressure0;
    private Integer pressure1;
    private Integer pressure2;
    private String time;


    public Integer getPressure0() {
        return pressure0;
    }

    public void setPressure0(Integer pressure0) {
        this.pressure0 = pressure0;
    }

    public Integer getPressure1() {
        return pressure1;
    }

    public String getTime() {
        return time;
    }

    public void setPressure1(Integer pressure1) {
        this.pressure1 = pressure1;
    }

    public Integer getPressure2() {
        return pressure2;
    }

    public void setPressure2(Integer pressure2) {
        this.pressure2 = pressure2;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BleData{" +
                "id=" + id +
                ", pressure0=" + pressure0 +
                ", pressure1=" + pressure1 +
                ", pressure2=" + pressure2 +
                ", time='" + time + '\'' +
                '}';
    }
}


