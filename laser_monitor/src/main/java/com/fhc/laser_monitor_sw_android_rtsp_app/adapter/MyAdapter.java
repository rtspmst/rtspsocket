package com.fhc.laser_monitor_sw_android_rtsp_app.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.widget.AutoFocusItem;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTvDis;
        public TextView mTvEnc;
        public TextView mTvM2Dis;
        public TextView mTvM2Enc;

        public MyViewHolder(View v) {
            super(v);
            mTvDis = v.findViewById(R.id.tvDis);
            mTvEnc = v.findViewById(R.id.tvEnc);
            mTvM2Dis = v.findViewById(R.id.tvM2Dis);
            mTvM2Enc = v.findViewById(R.id.tvM2Enc);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_text_view, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        AutoFocusItem autoFocusItem = (AutoFocusItem) mDataset.get(position);

        holder.mTvDis.setText("距离：" + Integer.toString(autoFocusItem.distance));
        holder.mTvEnc.setText("左值：" + Integer.toString(autoFocusItem.encoder));

        holder.mTvM2Dis.setText("距离：" + Integer.toString(autoFocusItem.m2_distance));
        holder.mTvM2Enc.setText("右值：" + Integer.toString(autoFocusItem.m2_encoder));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
