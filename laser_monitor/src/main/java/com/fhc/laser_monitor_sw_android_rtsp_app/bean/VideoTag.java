package com.fhc.laser_monitor_sw_android_rtsp_app.bean;

import android.util.Log;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.NameInDb;

@Entity
public class VideoTag implements Serializable {

    @Id
    private long id;

    @NameInDb("KEY")
    private String key;

    @NameInDb("TOTALTIME")
    private long totaltime;

    @NameInDb("TAG1")
    private long tag1;

    @NameInDb("TAG2")
    private long tag2;

    @NameInDb("TAG3")
    private long tag3;

    @NameInDb("TAG4")
    private long tag4;

    @NameInDb("TAG5")
    private long tag5;

    @NameInDb("TAG6")
    private long tag6;

    @NameInDb("TAG7")
    private long tag7;

    @NameInDb("TAG8")
    private long tag8;

    @NameInDb("TAG9")
    private long tag9;

    @NameInDb("TAG10")
    private long tag10;


    public long getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(long totaltime) {
        this.totaltime = totaltime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTag1() {
        return tag1;
    }

    public void setTag1(long tag1) {
        this.tag1 = tag1;
    }

    public long getTag2() {
        return tag2;
    }

    public void setTag2(long tag2) {
        this.tag2 = tag2;
    }

    public long getTag3() {
        return tag3;
    }

    public void setTag3(long tag3) {
        this.tag3 = tag3;
    }

    public long getTag4() {
        return tag4;
    }

    public void setTag4(long tag4) {
        this.tag4 = tag4;
    }

    public long getTag5() {
        return tag5;
    }

    public void setTag5(long tag5) {
        this.tag5 = tag5;
    }

    public long getTag6() {
        return tag6;
    }

    public void setTag6(long tag6) {
        this.tag6 = tag6;
    }

    public long getTag7() {
        return tag7;
    }

    public void setTag7(long tag7) {
        this.tag7 = tag7;
    }

    public long getTag8() {
        return tag8;
    }

    public void setTag8(long tag8) {
        this.tag8 = tag8;
    }

    public long getTag9() {
        return tag9;
    }

    public void setTag9(long tag9) {
        this.tag8 = tag9;
    }

    public long getTag10() {
        return tag10;
    }

    public void setTag10(long tag10) {
        this.tag8 = tag10;
    }

    public static void delete(String key) {

        Box<VideoTag> tagBox = MyApplication.getBoxStore().boxFor(VideoTag.class);
        List<VideoTag> huas = tagBox.query()
                .equal(VideoTag_.key, key)
                .build()
                .find();

        for (int i = 0; i < huas.size(); i++) {

            if (huas.get(i).getKey().equals(key)) {

                //删除账号
                MyApplication.getBoxStore().boxFor(VideoTag.class).remove(huas.get(i).getId());
            }
        }

        List<VideoTag> videoTags = tagBox.query().build().find();

        Log.e("TAG", "删除: ========" + Arrays.toString(videoTags.toArray()));
    }

    public static VideoTag inquiry(String key) {
        Box<VideoTag> tagBox = MyApplication.getBoxStore().boxFor(VideoTag.class);
        List<VideoTag> huas = tagBox.query()
                .equal(VideoTag_.key, key)
                .build()
                .find();

        for (int i = 0; i < huas.size(); i++) {

            if (huas.get(i).getKey().equals(key)) {
                VideoTag videoTag = huas.get(i);

                long totaltime = videoTag.getTotaltime();
                long tag1 = videoTag.getTag1();
                long tag2 = videoTag.getTag2();


                return videoTag;
            }
        }
        return null;
    }

    public static void add(String key, int number, long currentPlayingTime, long videoDurationTotal) {

        Box<VideoTag> tagBox = MyApplication.getBoxStore().boxFor(VideoTag.class);

        List<VideoTag> huas = tagBox.query()
                .equal(VideoTag_.key, key)
                .build()
                .find();


        if (huas.size() <= 0) {

            VideoTag videoTag = new VideoTag();

            videoTag.setKey(key);

            setVideoTag(number, currentPlayingTime, videoTag, videoDurationTotal);

            //保存到数据库
            tagBox.put(videoTag);

        } else {
            for (int i = 0; i < huas.size(); i++) {

                if (huas.get(i).getKey().equals(key)) {

                    VideoTag tagBean = huas.get(i);

                    tagBean.setKey(huas.get(i).getKey());

                    tagBean.setId(huas.get(i).getId());

                    setVideoTag(number, currentPlayingTime, tagBean, videoDurationTotal);

                    //保存到数据库
                    tagBox.put(tagBean);
                }
            }
        }


        List<VideoTag> videoTags = tagBox.query().build().find();

        Log.e("TAG", "保存的值: ========" + Arrays.toString(videoTags.toArray()));
    }

    private static void setVideoTag(int number, long currentPlayingTime, VideoTag videoTag, long aLong) {

        videoTag.setTotaltime(aLong);

        switch (number) {
            case 0:
                videoTag.setTag1(currentPlayingTime);
                break;
            case 1:
                videoTag.setTag2(currentPlayingTime);
                break;
            case 2:
                videoTag.setTag3(currentPlayingTime);
                break;
            case 3:
                videoTag.setTag4(currentPlayingTime);
                break;
            case 4:
                videoTag.setTag5(currentPlayingTime);
                break;
            case 5:
                videoTag.setTag6(currentPlayingTime);
                break;
            case 6:
                videoTag.setTag7(currentPlayingTime);
                break;
            case 7:
                videoTag.setTag8(currentPlayingTime);
                break;
            case 8:
                videoTag.setTag9(currentPlayingTime);
                break;
            case 9:
                videoTag.setTag10(currentPlayingTime);
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return "" + key + " -- " + totaltime + " ---- " +
                tag1 + ' ' +
                tag2 + ' ' +
                tag3 + ' ' +
                tag4 + ' ' +
                tag5 + ' ' +
                tag6 + ' ' +
                tag7 + ' ' +
                tag8;
    }
}
