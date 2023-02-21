package com.fhc.laser_monitor_sw_android_rtsp_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.ItemModel;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DensityUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.FileUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;

import java.io.File;
import java.util.List;

/**
 * 作者：fhc
 * 时间：2017/9/25 14:10
 * 录像回放
 * 路径适配器
 */
public class PathAdapter extends RecyclerView.Adapter<PathAdapter.PathViewHolder> {

    private List<ItemModel> mListData;
    private Context mContext;
    public OnItemClickListener onItemClickListener;
    private AbsoluteSizeSpan sizeSpan25;
    private AbsoluteSizeSpan sizeSpan20;

    public interface OnItemClickListener {
        void click(int position, boolean playVideoFlag);
    }

    public PathAdapter(List<ItemModel> mListData, Context mContext) {
        this.mListData = mListData;
        this.mContext = mContext;
        sizeSpan25 = new AbsoluteSizeSpan(DensityUtil.dip2px(mContext, 22));
        sizeSpan20 = new AbsoluteSizeSpan(DensityUtil.dip2px(mContext, 15));
    }

    @NonNull
    @Override
    public PathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = View.inflate(mContext, R.layout.listitem, null);
        //RecyclerView的item横向屏幕没有铺满
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem, parent, false);
        return new PathViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final PathViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final ItemModel itemModel = mListData.get(position);
        if (itemModel.file.isFile()) {

            Glide.with(mContext)
                    .load(Uri.fromFile(new File(itemModel.file.getAbsolutePath())))
                    .error(R.drawable.ic_error)
                    .into(holder.ivType);

            if (itemModel.file.getAbsolutePath().endsWith(".mp4")) {
                holder.ivVideo.setVisibility(View.VISIBLE);
            } else {
                holder.ivVideo.setVisibility(View.GONE);
            }

            holder.tv_number.setText("" + (1 + position));
            Spannable WordtoSpan = new SpannableString(itemModel.file.getName());
            WordtoSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#455ede")), 0, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            WordtoSpan.setSpan(sizeSpan25, 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            WordtoSpan.setSpan(sizeSpan20, 11, itemModel.file.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvName.setText(WordtoSpan);
            holder.tvDetail.setText(Language.SIZE + FileUtils.getReadableFileSize(itemModel.file.length()));
            holder.cbChoose.setChecked(itemModel.isSelect);
        }

        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                holder.cbChoose.setChecked(!holder.cbChoose.isChecked());

                if (onItemClickListener != null) {

                    if (1024 < itemModel.file.length()) {
                        onItemClickListener.click(position, true);
                    } else {

                        if (MainActivity.IS_ENGLISH) {
                            ToastUtils.showShort("Invalid video");
                        } else {
                            ToastUtils.showShort("无效视频");
                        }
                    }
                }
            }
        });

        holder.cbChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.click(position, false);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class PathViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout layoutRoot;
        private TextView tv_number;
        private ImageView ivType;
        private TextView tvName;
        private TextView tvDetail;
        private CheckBox cbChoose;
        private ImageView ivVideo;

        public PathViewHolder(View itemView) {
            super(itemView);
            ivType = itemView.findViewById(R.id.iv_type);
            layoutRoot = itemView.findViewById(R.id.layout_item_root);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDetail = itemView.findViewById(R.id.tv_detail);
            cbChoose = itemView.findViewById(R.id.cb_choose);
            tv_number = itemView.findViewById(R.id.tv_number);
            ivVideo = itemView.findViewById(R.id.iv_video);
        }
    }
}


