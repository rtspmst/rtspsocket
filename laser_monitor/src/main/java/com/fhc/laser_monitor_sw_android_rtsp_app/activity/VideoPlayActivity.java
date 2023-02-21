package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import android.os.Bundle;
import android.util.Log;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.VideoTag;

import java.util.ArrayList;
import java.util.Arrays;

import cn.jzvd.Jzvd;

public class VideoPlayActivity extends BaseActivity {

    private Jzvd myJzvdStd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        myJzvdStd = findViewById(R.id.jz_video);

        String str = getIntent().getStringExtra("URL");
        String name = getIntent().getStringExtra("NAME");

        VideoTag videoTag = VideoTag.inquiry(name);

        ArrayList arrayList = new ArrayList();

        if (videoTag != null) {

            long totaltime = videoTag.getTotaltime();

            long tag1 = videoTag.getTag1();
            long tag2 = videoTag.getTag2();
            long tag3 = videoTag.getTag3();
            long tag4 = videoTag.getTag4();
            long tag5 = videoTag.getTag5();
            long tag6 = videoTag.getTag6();
            long tag7 = videoTag.getTag7();
            long tag8 = videoTag.getTag8();
            long tag9 = videoTag.getTag9();
            long tag10 = videoTag.getTag10();


            if (0 != totaltime) {

                //tag单位是毫秒 求得百分比再放大1000倍
                arrayList.add(tag1 * 1000 / totaltime);
                arrayList.add(tag2 * 1000 / totaltime);
                arrayList.add(tag3 * 1000 / totaltime);
                arrayList.add(tag4 * 1000 / totaltime);
                arrayList.add(tag5 * 1000 / totaltime);
                arrayList.add(tag6 * 1000 / totaltime);
                arrayList.add(tag7 * 1000 / totaltime);
                arrayList.add(tag8 * 1000 / totaltime);
                arrayList.add(tag9 * 1000 / totaltime);
                arrayList.add(tag10 * 1000 / totaltime);
            }

            Log.e("TAG", "获取保存的千分比 集合: ==========" + Arrays.toString(arrayList.toArray()));
        }

        myJzvdStd.setData(arrayList);

        //本地文件转URL
        try {
//            URL url = new URL(str);
//            url = new File("d:/temp/a.txt").toURI().toURL();

            myJzvdStd.setUp(str, name);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        myJzvdStd.reset();
        super.onDestroy();
    }
}