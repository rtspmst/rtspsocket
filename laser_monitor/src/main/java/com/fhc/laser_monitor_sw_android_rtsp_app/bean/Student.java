package com.fhc.laser_monitor_sw_android_rtsp_app.bean;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Transient;

@Entity
public class Student implements Serializable {

    @Id
    private long id;

    //工作时间
    @NameInDb("JOBSTIME")
    private String jobsTime;

    //是否在充电中
    @NameInDb("CHARGING")
    private int charging;

    //某个字段不想被持久化，可以使用此注解,那么该字段将不会保存到数据库
    @Transient
    private int weizhi;

    //电量剩余
    private float remainingBattery;

    //前后台
    private int isBackstage;

    //串口连接 透传连接
    private int connectionUART;

    //通信连接
    private int connectionJSON;

    //音频连接
    @Index
    private int connectionAudio;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobsTime() {
        return jobsTime;
    }

    public void setJobsTime(String jobsTime) {
        this.jobsTime = jobsTime;
    }

    public int getCharging() {
        return charging;
    }

    public void setCharging(int charging) {
        this.charging = charging;
    }

    public int getIsBackstage() {
        return isBackstage;
    }

    public void setIsBackstage(int isBackstage) {
        this.isBackstage = isBackstage;
    }

    public int getConnectionUART() {
        return connectionUART;
    }

    public void setConnectionUART(int connectionUART) {
        this.connectionUART = connectionUART;
    }

    public int getConnectionJSON() {
        return connectionJSON;
    }

    public void setConnectionJSON(int connectionJSON) {
        this.connectionJSON = connectionJSON;
    }

    public float getRemainingBattery() {
        return remainingBattery;
    }

    public void setRemainingBattery(float remainingBattery) {
        this.remainingBattery = remainingBattery;
    }

    public int getConnectionAudio() {
        return connectionAudio;
    }

    public void setConnectionAudio(int connectionAudio) {
        this.connectionAudio = connectionAudio;
    }

    String strConnectionAudio1 = "音频已连接";
    String strConnectionAudio0 = "音频未连接";

    String strCharging1 = "正在充电";
    String strCharging0 = "未 充 电";

//    String strUart1 = "串口已连接";
//    String strUart0 = "串口未连接";

//    String strJSON1 = "通信已连接";
//    String strJSON0 = "通信未连接";

//    String string0 = "前台工作";
//    String string1 = "后台工作";

    @Override
    public String toString() {

        return "id = " + id +
                "       " + jobsTime +
                "       " + (charging == 1 ? strCharging1 : strCharging0) +
                "       电量 = " + getPercentage(remainingBattery) + "%" +
//                "       " + (isBackstage == 1 ? string0 : (string1 + isBackstage)) +
//                "       " + (connectionUART == 1 ? strUart1 : strUart0) +
//                "       " + (connectionJSON == 1 ? strJSON1 : strJSON0) +
                "       " + (connectionAudio == 1 ? strConnectionAudio1 : strConnectionAudio0) + "  ,";
    }

    private int getPercentage(float voltage) {

        int percentageBattery = 0;

        if (voltage <= 13.0) {
            percentageBattery = 0;
        } else if (voltage > 13.0 && voltage <= 13.2) {
            percentageBattery = 10;
        } else if (voltage > 13.2 && voltage <= 13.4) {
            percentageBattery = 20;
        } else if (voltage > 13.4 && voltage <= 13.6) {
            percentageBattery = 30;
        } else if (voltage > 13.6 && voltage <= 13.8) {
            percentageBattery = 40;
        } else if (voltage > 13.8 && voltage <= 14.0) {
            percentageBattery = 50;
        } else if (voltage > 14.0 && voltage <= 14.2) {
            percentageBattery = 60;
        } else if (voltage > 14.2 && voltage <= 14.4) {
            percentageBattery = 70;
        } else if (voltage > 14.4 && voltage <= 14.6) {
            percentageBattery = 80;
        } else if (voltage > 14.6 && voltage <= 14.8) {
            percentageBattery = 90;
        } else if (voltage > 14.8) {
            percentageBattery = 100;
        }

        return percentageBattery;
    }
}
