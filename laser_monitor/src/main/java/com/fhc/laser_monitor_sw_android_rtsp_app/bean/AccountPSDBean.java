package com.fhc.laser_monitor_sw_android_rtsp_app.bean;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.NameInDb;
import io.objectbox.annotation.Transient;

@Entity
public class AccountPSDBean implements Serializable {

    @Id
    private long id;

    //账号
    @NameInDb("ACCOUNT")
    private String account;

    //密码
    @NameInDb("PSD")
    private String psd;

    //某个字段不想被持久化，可以使用此注解,那么该字段将不会保存到数据库
    @Transient
    private int weizhi;

    private boolean isSelect;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWeizhi() {
        return weizhi;
    }

    public void setWeizhi(int id) {
        this.weizhi = weizhi;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPsd() {
        return psd;
    }

    public void setPsd(String psd) {
        this.psd = psd;
    }

    @Override
    public String toString() {

        return "id = " + id +
                "       " + account +
                "       " + psd
                ;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
