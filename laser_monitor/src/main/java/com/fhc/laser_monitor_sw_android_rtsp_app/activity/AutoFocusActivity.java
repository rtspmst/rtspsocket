package com.fhc.laser_monitor_sw_android_rtsp_app.activity;


import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.MyAdapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.widget.AutoFocusItem;

import java.util.List;

public class AutoFocusActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<AutoFocusItem> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_focus);

        mDatas = (List<AutoFocusItem>) getIntent().getSerializableExtra("encoder");

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        for (AutoFocusItem item : mDatas) {
            Log.d("Item", item.distance + "");
            Log.d("Item", item.encoder + "");
        }

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(mDatas);
        mRecyclerView.setAdapter(mAdapter);
    }
}
