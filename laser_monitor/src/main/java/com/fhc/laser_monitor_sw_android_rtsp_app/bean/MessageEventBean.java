package com.fhc.laser_monitor_sw_android_rtsp_app.bean;

public class MessageEventBean {

    private String message;

    public MessageEventBean(String message){
        this.message = message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
