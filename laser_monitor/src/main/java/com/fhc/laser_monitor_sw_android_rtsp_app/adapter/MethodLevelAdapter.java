package com.fhc.laser_monitor_sw_android_rtsp_app.adapter;

import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
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
public class MethodLevelAdapter extends BaseQuickAdapter<MethodSelectBean, BaseViewHolder> {

    public MethodLevelAdapter(@Nullable List<MethodSelectBean> data) {
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

    public List<MethodSelectBean> getLevelData() {
        final List<MethodSelectBean> levelBeans = new ArrayList<>();

        if (Language.anInt == 1) {

            levelBeans.add(new MethodSelectBean("Tingkat pengurangan kebisingan", "Tingkat_1", "mode", true, false));
            levelBeans.add(new MethodSelectBean("", "Tingkat_2", "mode", false, false));
            levelBeans.add(new MethodSelectBean("", "Tingkat_3", "mode", false, false));

        } else if (Language.anInt == 2) {

            levelBeans.add(new MethodSelectBean("Level selection", "Level_1", "mode", true, false));
            levelBeans.add(new MethodSelectBean("", "Level_2", "mode", false, false));
            levelBeans.add(new MethodSelectBean("", "Level_3", "mode", false, false));

        } else {

            levelBeans.add(new MethodSelectBean("降噪等级选择", "一级", "mode", true, false));
            levelBeans.add(new MethodSelectBean("", "二级", "mode", false, false));
            levelBeans.add(new MethodSelectBean("", "三级", "mode", false, false));
        }

        return levelBeans;
    }
}
