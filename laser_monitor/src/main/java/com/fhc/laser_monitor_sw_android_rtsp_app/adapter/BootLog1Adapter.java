package com.fhc.laser_monitor_sw_android_rtsp_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.Student;

import java.util.List;

public class BootLog1Adapter extends RecyclerView.Adapter<BootLog1Adapter.RecordLogBootHolder> {

    private List<Student> mListData;
    private Context mContext;

    public BootLog1Adapter(List<Student> mListData, Context mContext) {
        this.mListData = mListData;
        this.mContext = mContext;
    }

    public void setmListData(List<Student> mListData) {
        this.mListData = mListData;
        notifyDataSetChanged();
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
        return mListData.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final RecordLogBootHolder holder, final int position) {
        holder.tv_log_name.setText("  {   " + mListData.get(position).toString().trim() + "   }");
    }

    class RecordLogBootHolder extends RecyclerView.ViewHolder {
        private TextView tv_log_name;

        public RecordLogBootHolder(View itemView) {
            super(itemView);
            tv_log_name = itemView.findViewById(R.id.tv_log_name);
        }
    }
}


