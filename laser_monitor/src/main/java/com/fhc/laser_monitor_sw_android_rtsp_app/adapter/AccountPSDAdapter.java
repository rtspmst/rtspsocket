package com.fhc.laser_monitor_sw_android_rtsp_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.AccountPSDBean;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DensityUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;

import java.util.List;

public class AccountPSDAdapter extends RecyclerView.Adapter<AccountPSDAdapter.RecordLogBootHolder> {

    private final int bounds;
    private List<AccountPSDBean> mListData;
    private Context mContext;

    private Drawable eyesOpen;
    private Drawable eyesClosed;
    private final Handler handler;

    public AccountPSDAdapter(List<AccountPSDBean> mListData, Context mContext) {
        this.mListData = mListData;
        this.mContext = mContext;
        bounds = DensityUtil.dip2px(mContext, 48);

        eyesOpen = mContext.getResources().getDrawable(R.drawable.ic_look_over, mContext.getTheme());
        eyesOpen.setBounds(0, 0, bounds, bounds);
        eyesClosed = mContext.getResources().getDrawable(R.drawable.ic_look_hide, mContext.getTheme());
        eyesClosed.setBounds(0, 0, bounds, bounds);

        handler = new Handler();
    }

    public void setmListData(List<AccountPSDBean> mListData) {
        this.mListData = mListData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordLogBootHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = View.inflate(mContext, R.layout.record_log_boot, null);
        //RecyclerView的item横向屏幕没有铺满
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_login_user_list, parent, false);
        return new RecordLogBootHolder(view);
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final RecordLogBootHolder holder, @SuppressLint("RecyclerView") final int position) {

        AccountPSDBean bean = mListData.get(position);
        String account = bean.getAccount();
        String psd = bean.getPsd();

        holder.tv_user_name.setText(account);

        holder.tv_user_psd.setText(DensityUtil.replaceAction(psd));

        if (bean.isSelect()) {
            //选择状态 显示明文--设置为可见的密码
            holder.tv_user_psd.setText(psd);
            holder.iv_look_item.setImageDrawable(eyesOpen);
        } else {
            holder.tv_user_psd.setText(DensityUtil.replaceAction(psd));
            holder.iv_look_item.setImageDrawable(eyesClosed);
        }

        holder.iv_look_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bean.isSelect()) {
                    //选择状态 显示明文--设置为可见的密码

                    holder.tv_user_psd.setText(psd);
                    holder.iv_look_item.setImageDrawable(eyesOpen);
                } else {
                    holder.tv_user_psd.setText(DensityUtil.replaceAction(psd));
                    holder.iv_look_item.setImageDrawable(eyesClosed);
                }

                bean.setSelect(!bean.isSelect());
            }
        });

        holder.btn_change_psd.setText(Language.CHANGE_PSW);
        holder.btn_change_psd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onItemClickListener != null) {
                    onItemClickListener.click(position);
                }
            }
        });

        holder.btn_delete_users.setText(Language.DELETE_USER);
        holder.btn_delete_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //删除账号
                MyApplication.getBoxStore().boxFor(AccountPSDBean.class).remove(bean.getId());

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        setmListData(MyApplication.getBoxStore().boxFor(AccountPSDBean.class).query().build().find());
                    }
                }, 300);
            }
        });
    }

    public OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void click(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class RecordLogBootHolder extends RecyclerView.ViewHolder {
        private TextView tv_user_name;
        private TextView tv_user_psd;
        private Button btn_change_psd;
        private Button btn_delete_users;
        private ImageView iv_look_item;
        private boolean isChecked = true;

        public RecordLogBootHolder(View itemView) {
            super(itemView);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_user_psd = itemView.findViewById(R.id.tv_user_psd);
            btn_change_psd = itemView.findViewById(R.id.btn_change_psd);
            btn_delete_users = itemView.findViewById(R.id.btn_delete_users);
            iv_look_item = itemView.findViewById(R.id.iv_look_item);
        }
    }
}


