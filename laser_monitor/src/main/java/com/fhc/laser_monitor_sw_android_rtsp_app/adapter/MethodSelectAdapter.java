package com.fhc.laser_monitor_sw_android_rtsp_app.adapter;

import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.MethodSelectBean;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: MethodSelectAdapter
 * @Description:
 * @Author: Lix
 * @CreateDate: 2020/6/19
 * @Version: 1.0
 */
public class MethodSelectAdapter extends BaseQuickAdapter<MethodSelectBean, BaseViewHolder> {

    public MethodSelectAdapter(@Nullable List<MethodSelectBean> data) {
        super(R.layout.view_method_select_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MethodSelectBean item) {
        helper.setGone(R.id.method_header_layout, item.isHeader());
        if (item.isHeader()) {
            helper.setText(R.id.method_header, item.getTitle());
        }
        item.setPosition(helper.getAdapterPosition());
        RadioButton radioButton = helper.getView(R.id.method_radio);
        radioButton.setText(item.getText());
        radioButton.setChecked(item.isSelect());
        radioButton.setClickable(false);
        radioButton.setFocusable(false);
    }

    public boolean isItemSelect() {
        for (MethodSelectBean selectBean : getData()) {
            if (selectBean.isSelect()) {
                return true;
            }
        }
        return false;
    }

    public List<MethodSelectBean> getSelectData() {
        final List<MethodSelectBean> selectBeans = new ArrayList<>();

        if (Language.anInt == 1) {

            selectBeans.add(new MethodSelectBean("Metode pengurangan kebisingan", "Mode_Satu", "Strong", true, false));
            selectBeans.add(new MethodSelectBean("", "Mode_Dua", "Strong", false, false));
            selectBeans.add(new MethodSelectBean("", "Mode_Tiga", "WebRTC", false, false));
            selectBeans.add(new MethodSelectBean("", "Mode_Empat", "Wobble", false, false));


        } else if (Language.anInt == 2) {

            selectBeans.add(new MethodSelectBean("Method selection", "Mode_1", "Strong", true, false));
            selectBeans.add(new MethodSelectBean("", "Mode_2", "Strong", false, false));
            selectBeans.add(new MethodSelectBean("", "Mode_3", "WebRTC", false, false));
            selectBeans.add(new MethodSelectBean("", "Mode_4", "Wobble", false, false));

        } else {

            selectBeans.add(new MethodSelectBean("降噪方法选择", "降噪一", "Strong", true, false));
            selectBeans.add(new MethodSelectBean("", "降噪二", "Strong", false, false));
            selectBeans.add(new MethodSelectBean("", "降噪三", "WebRTC", false, false));
            selectBeans.add(new MethodSelectBean("", "降噪四", "Wobble", false, false));
        }

        return selectBeans;
    }
}
