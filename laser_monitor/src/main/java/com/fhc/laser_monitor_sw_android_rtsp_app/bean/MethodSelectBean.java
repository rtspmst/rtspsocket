package com.fhc.laser_monitor_sw_android_rtsp_app.bean;

/**
 * @ClassName: MethodSelectBean
 * @Description:
 * @Author: Lix
 * @CreateDate: 2020/6/19
 * @Version: 1.0
 */
public class MethodSelectBean {
    private String title;
    private String text;
    private String key;
    private String grade_index;
    private String quality_index;
    private int position;
    private boolean isHeader;
    private boolean isSelect;
    private boolean isShow;//判断是否是降噪三四 三四则设置不可点击

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public String getGrade_index() {
        return grade_index;
    }

    public void setGrade_index(String grade_index) {
        this.grade_index = grade_index;
    }

    public String getQuality_index() {
        return quality_index;
    }

    public void setQuality_index(String quality_index) {
        this.quality_index = quality_index;
    }

    public MethodSelectBean(String title, String text, String key, boolean isHeader, boolean isSelect) {
        this.title = title;
        this.text = text;
        this.key = key;
        this.isSelect = isSelect;
        this.isHeader = isHeader;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    @Override
    public String toString() {
        return "MethodSelectBean{" +
                "title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", key='" + key + '\'' +
                ", position=" + position +
                ", isHeader=" + isHeader +
                ", isSelect=" + isSelect +
                '}';
    }
}
