package com.bright.administrator.lib_coremodel.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class StepEntity {
    @PrimaryKey(autoGenerate = true)
    private Integer userID;
    private String curDate; //当天的日期
    private String steps;   //当天的步数
    private String userName;  //用户名
    private String ImageURL;


    public String getCurDate() {
        return curDate;
    }

    public void setCurDate(String curDate) {
        this.curDate = curDate;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }



    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        return "StepEntity{" +
                "curDate='" + curDate + '\'' +
                ", steps=" + steps +
                '}';
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }
}
