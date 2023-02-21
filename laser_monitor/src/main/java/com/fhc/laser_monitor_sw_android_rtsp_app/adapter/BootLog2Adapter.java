package com.fhc.laser_monitor_sw_android_rtsp_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;

import java.util.List;

public class BootLog2Adapter extends RecyclerView.Adapter<BootLog2Adapter.RecordLogBootHolder> {

    private List<String> mListData;
    private Context mContext;

    public BootLog2Adapter(List<String> mListData, Context mContext) {
        this.mListData = mListData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecordLogBootHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = View.inflate(mContext, R.layout.record_log_boot, null);
        //RecyclerView的item横向屏幕没有铺满
        View view = LayoutInflater.from(mContext).inflate(R.layout.record_log_boot, parent, false);
        return new RecordLogBootHolder(view);
    }

    @Override
    public int getItemCount() {
        return mListData.size() - 1;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecordLogBootHolder holder, final int position) {
        holder.tv_log_name.setText("  {   " + mListData.get(position).trim() + "   }");
    }

    class RecordLogBootHolder extends RecyclerView.ViewHolder {
        private TextView tv_log_name;

        public RecordLogBootHolder(View itemView) {
            super(itemView);
            tv_log_name = itemView.findViewById(R.id.tv_log_name);
        }
    }
}


