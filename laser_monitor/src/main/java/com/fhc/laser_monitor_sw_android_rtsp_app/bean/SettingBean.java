package com.fhc.laser_monitor_sw_android_rtsp_app.bean;

public class SettingBean {

    private boolean state;
    private String name;

    public SettingBean(boolean state, String name) {

        this.state = state;
        this.name = name;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
