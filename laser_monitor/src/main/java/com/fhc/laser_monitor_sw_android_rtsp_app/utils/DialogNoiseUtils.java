package com.fhc.laser_monitor_sw_android_rtsp_app.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.JsonHandle6802;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.MethodAlAdapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.MethodLevelAdapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.MethodSelectAdapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.MethodSelectBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 降噪弹窗
 */
public class DialogNoiseUtils {

    private final Context context;
    private final boolean vibratorBoolean;
    private SharedPreferencesUtil spUtil;
    private JsonHandle6802 mJsonHandle6802;
    private String methodText = "降噪一";//降噪方法 也是key
    //用来控制 弹窗未关闭不响应三分钟自动息屏
    public static boolean AlertDialogIsClose = true;
    private Vibrator vibrator;
    private int vibratorTime;

    public DialogNoiseUtils(Context context, JsonHandle6802 mJsonHandle6802, SharedPreferencesUtil spUtil, Vibrator vibrator, boolean vibratorBoolean, int vibratorTime) {

        this.context = context;

        this.mJsonHandle6802 = mJsonHandle6802;

        this.spUtil = spUtil;

        this.vibrator = vibrator;

        this.vibratorBoolean = vibratorBoolean;

        this.vibratorTime = vibratorTime;

    }

    //降噪方法
    public void methodSelect() {

        AlertDialogIsClose = false;

        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setView(getLanguageView())
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //避免刚关闭弹窗 三分钟时间已到 重新赋值 重新计算
                        MainActivity.getRandomNumber("关闭弹窗");
                        //改为true响应三分钟无操作自动息屏
                        AlertDialogIsClose = true;
                    }
                }).show().getWindow().setLayout(DensityUtil.dip2px(context, 600), DensityUtil.dip2px(context, 380));
    }

    private View getLanguageView() {

        if (Language.anInt == 1) {

            return get_yinni_view();

        } else if (Language.anInt == 2) {

            return get_english_View();

        } else {

            return getView();
        }
    }

    //降噪方法弹窗
    private View getView() {
        final List<MethodSelectBean> selectBeans = new ArrayList<>();
        final List<MethodSelectBean> levelBeans = new ArrayList<>();
        final List<MethodSelectBean> ABeans = new ArrayList<>();

        final MethodSelectAdapter selectAdapter = new MethodSelectAdapter(new ArrayList<MethodSelectBean>());
        final MethodLevelAdapter levelAdapter = new MethodLevelAdapter(new ArrayList<MethodSelectBean>());
        final MethodAlAdapter methodAlAdapter = new MethodAlAdapter(new ArrayList<MethodSelectBean>());

        final View view = LayoutInflater.from(context).inflate(R.layout.view_method_select_layout, null);

        final LinearLayout recycleLayout = view.findViewById(R.id.method_recycle_layout);
        final RecyclerView selectRecycleView = view.findViewById(R.id.method_way_recycle);
        final RecyclerView levelRecycleView = view.findViewById(R.id.method_level_recycle);
        final RecyclerView method_A_recycle = view.findViewById(R.id.method_A_recycle);
        final TextView tv_noise_reduction_switch = view.findViewById(R.id.tv_noise_reduction_switch);
        tv_noise_reduction_switch.setText("降噪开关");

        selectRecycleView.setAdapter(selectAdapter);
        levelRecycleView.setAdapter(levelAdapter);
        method_A_recycle.setAdapter(methodAlAdapter);

        Switch method_switch = view.findViewById(R.id.method_switch);
        method_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //设置震动
                setVibrator();

                spUtil.saveMethodSwitch(CV.METHOD_SWITHCH, isChecked);

                if (isChecked) {
                    recycleLayout.setVisibility(View.VISIBLE);

                    selectBeans.clear();
                    levelBeans.clear();
                    ABeans.clear();

                    selectBeans.addAll(selectAdapter.getSelectData());
                    levelBeans.addAll(levelAdapter.getLevelData());
                    ABeans.addAll(methodAlAdapter.getGreadData());

                    String lastClick = spUtil.getLastClick();
                    if (!TextUtils.isEmpty(lastClick)) {

                        methodText = lastClick;
                    }

                    for (int i = 0; i < selectBeans.size(); i++) {

                        if (selectBeans.get(i).getText().equals(lastClick)) {
                            selectBeans.get(i).setSelect(true);
                            mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, selectBeans.get(i).getKey());

                            String level = spUtil.getMethodLevel(lastClick);

                            if (TextUtils.isEmpty(level)) {
                                level = "0";
                            }
                            for (int j = 0; j < levelBeans.size(); j++) {

                                if (j == Integer.parseInt(level)) {
                                    MethodSelectBean bean = levelBeans.get(j);
                                    String levelText = bean.getText();
                                    bean.setSelect(true);

                                    if ("降噪一".equals(lastClick)) {

                                        if (levelText.contains("三级")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "3"));
                                        } else if (levelText.contains("二级")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "2"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "1"));
                                        }
                                    } else if ("降噪二".equals(lastClick) || "降噪四".equals(lastClick)) {

                                        if (levelText.contains("三级")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "6"));
                                        } else if (levelText.contains("二级")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "5"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "4"));
                                        }
                                    } else if ("降噪三".equals(lastClick)) {
                                        if (bean.getText().contains("三")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "2"));
                                        } else if (bean.getText().contains("二")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "1"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "0"));
                                        }
                                    }
                                }
                            }

//                            if (!"降噪三".equals(lastClick) && !"降噪四".equals(lastClick)) {
                            if (!"降噪三".equals(lastClick)) {

                                String indexQuality = spUtil.getMethodLevel(lastClick + lastClick);
                                if (TextUtils.isEmpty(indexQuality)) {
                                    indexQuality = "0";
                                }

                                for (int k = 0; k < ABeans.size(); k++) {
                                    if (k == Integer.parseInt(indexQuality)) {
                                        ABeans.get(k).setSelect(true);


                                        //声源质量
                                        if (ABeans.get(k).getText().contains("三级")) {
                                            //发送声源质量命令
                                            send_3_CMD(2);
                                        } else if (ABeans.get(k).getText().contains("二级")) {
                                            //发送声源质量命令
                                            send_3_CMD(1);
                                        } else {
                                            //发送声源质量命令
                                            send_3_CMD(0);
                                        }
                                    }
                                }
                            }
                        }
                    }

//                    if ("降噪三".equals(methodText) || "降噪四".equals(methodText)) {
                    if ("降噪三".equals(methodText)) {
                        for (int i = 0; i < ABeans.size(); i++) {
                            ABeans.get(i).setSelect(false);
                            ABeans.get(i).setShow(true);
                        }
                    }

                    selectAdapter.setNewData(selectBeans);
                    levelAdapter.setNewData(levelBeans);
                    methodAlAdapter.setNewData(ABeans);
                } else {
                    recycleLayout.setVisibility(View.GONE);
                    selectBeans.clear();
                    levelBeans.clear();
                    ABeans.clear();

                    selectAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    levelAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    methodAlAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    //关闭后发送关闭指令
                    mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, "off");
                }
            }
        });

        //方法选择
        selectAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                //设置震动
                setVibrator();

                methodText = selectBeans.get(position).getText();
                spUtil.saveLastClick(methodText);

                for (MethodSelectBean method : selectBeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }

                //保存的降噪等级下标
                String methodLevel = spUtil.getMethodLevel(methodText);
                if (TextUtils.isEmpty(methodLevel)) {
                    methodLevel = "0";
                }
                int anInt = Integer.parseInt(methodLevel);
                for (int i = 0; i < levelBeans.size(); i++) {

                    if (i == anInt) {
                        levelBeans.get(i).setSelect(true);
                    } else {
                        levelBeans.get(i).setSelect(false);
                    }
                }
                levelAdapter.notifyDataSetChanged();

                //保存的声源质量下标
                String methevel = spUtil.getMethodLevel(methodText + methodText);
                if (TextUtils.isEmpty(methevel)) {
                    methevel = "0";
                }
                int parseInt = Integer.parseInt(methevel);
                for (int i = 0; i < ABeans.size(); i++) {

//                    if ("降噪三".equals(methodText) || "降噪四".equals(methodText)) {
                    if ("降噪三".equals(methodText)) {
                        ABeans.get(i).setSelect(false);
                        ABeans.get(i).setShow(true);
                    } else {
                        ABeans.get(i).setShow(false);
                        if (i == parseInt) {
                            ABeans.get(i).setSelect(true);
                        } else {
                            ABeans.get(i).setSelect(false);
                        }
                    }

                }
                methodAlAdapter.notifyDataSetChanged();

                MethodSelectBean method = selectBeans.get(position);
                method.setSelect(!method.isSelect());
                selectAdapter.notifyDataSetChanged();
                MethodSelectBean level = null;
                for (MethodSelectBean methodSelectBean : levelAdapter.getData()) {
                    if (methodSelectBean.isSelect()) {
                        level = methodSelectBean;
                    }
                }
                if (method.isSelect()) {
                    mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, method.getKey());
                    if (level != null) {
                        spUtil.saveMethodLevel(CV.METHOD_LEVEL, level.getText());

                        if (method.getText().contains("四")) {

                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 4) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);

                        } else if (method.getText().contains("三")) {

                            mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", level.getPosition() + ""));

                        } else if (method.getText().contains("二")) {

                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 4) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);
                        } else {
                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 1) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);
                        }
                    }
                }
            }
        });

        //等级选择
        levelAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                //设置震动
                setVibrator();

                if (!selectAdapter.isItemSelect()) {
//                    com.blankj.utilcode.util.ToastUtils.showShort("请先选择降噪方法");
                    return;
                }
                for (MethodSelectBean method : levelBeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }
                MethodSelectBean method = levelBeans.get(position);
                method.setSelect(!method.isSelect());
                spUtil.saveMethodLevel(CV.METHOD_LEVEL, method.getText());

                String lastClick = spUtil.getLastClick();
                if (!TextUtils.isEmpty(lastClick)) {

                    methodText = lastClick;
                }
                spUtil.saveMethodLevel(methodText, "" + position);
                levelAdapter.notifyDataSetChanged();

                MethodSelectBean selectBean = null;
                for (MethodSelectBean bean : levelAdapter.getData()) {
                    if (bean.isSelect()) {
                        selectBean = bean;
                    }
                }
                if (method.isSelect()) {
                    if (selectBean != null) {
                        LogUtils.e(selectBean.toString());
                        spUtil.saveMethodLevel(CV.METHOD_SELECT, selectBean.getText());

                        if ("降噪二".equals(methodText) || "降噪四".equals(methodText)) {
                            if (selectBean.getText().contains("三")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            } else if (selectBean.getText().contains("二")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            }
                        } else if ("降噪一".equals(methodText)) {
                            if (selectBean.getText().contains("三")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "3"));
                            } else if (selectBean.getText().contains("二")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "2"));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "1"));
                            }
                        } else {

                            if (selectBean.getText().contains("三")) {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "2"));
                            } else if (selectBean.getText().contains("二")) {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "1"));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "0"));
                            }
                        }
                    }
                }
            }
        });

        //声源质量选择
        methodAlAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                //设置震动
                setVibrator();

                if (!selectAdapter.isItemSelect()) {
//                    ToastUtils.showShort("请先选择降噪方法");
                    return;
                }

                for (MethodSelectBean method : ABeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }

                MethodSelectBean method = ABeans.get(position);
                method.setSelect(!method.isSelect());

                spUtil.saveMethodLevel(methodText + methodText, "" + position);

                //发送声源质量命令
                send_3_CMD(position);

                methodAlAdapter.notifyDataSetChanged();
            }
        });
        method_switch.setChecked(spUtil.getMethodSwitch(CV.METHOD_SWITHCH));
        return view;
    }

    //设置震动
    private void setVibrator() {
        if (vibratorBoolean) {

            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(vibratorTime);
//                vibrator.cancel();
            }
        }
    }

    //降噪方法弹窗
    private View get_english_View() {
        final List<MethodSelectBean> selectBeans = new ArrayList<>();
        final List<MethodSelectBean> levelBeans = new ArrayList<>();
        final List<MethodSelectBean> ABeans = new ArrayList<>();

        final MethodSelectAdapter selectAdapter = new MethodSelectAdapter(new ArrayList<MethodSelectBean>());
        final MethodLevelAdapter levelAdapter = new MethodLevelAdapter(new ArrayList<MethodSelectBean>());
        final MethodAlAdapter methodAlAdapter = new MethodAlAdapter(new ArrayList<MethodSelectBean>());

        final View view = LayoutInflater.from(context).inflate(R.layout.view_method_select_layout, null);

        final LinearLayout recycleLayout = view.findViewById(R.id.method_recycle_layout);
        final RecyclerView selectRecycleView = view.findViewById(R.id.method_way_recycle);
        final RecyclerView levelRecycleView = view.findViewById(R.id.method_level_recycle);
        final RecyclerView method_A_recycle = view.findViewById(R.id.method_A_recycle);

        final TextView tv_noise_reduction_switch = view.findViewById(R.id.tv_noise_reduction_switch);
        tv_noise_reduction_switch.setText("Noise Reduction Switch");

        selectRecycleView.setAdapter(selectAdapter);
        levelRecycleView.setAdapter(levelAdapter);
        method_A_recycle.setAdapter(methodAlAdapter);

        Switch method_switch = view.findViewById(R.id.method_switch);
        method_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                spUtil.saveMethodSwitch(CV.METHOD_SWITHCH, isChecked);

                if (isChecked) {
                    recycleLayout.setVisibility(View.VISIBLE);

                    selectBeans.clear();
                    levelBeans.clear();
                    ABeans.clear();

                    selectBeans.addAll(selectAdapter.getSelectData());
                    levelBeans.addAll(levelAdapter.getLevelData());
                    ABeans.addAll(methodAlAdapter.getGreadData());

                    String lastClick = spUtil.getLastClick();
                    if (!TextUtils.isEmpty(lastClick)) {

                        methodText = lastClick;
                    }

                    for (int i = 0; i < selectBeans.size(); i++) {

                        if (selectBeans.get(i).getText().equals(lastClick)) {
                            selectBeans.get(i).setSelect(true);
                            mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, selectBeans.get(i).getKey());

                            String level = spUtil.getMethodLevel(lastClick);

                            if (TextUtils.isEmpty(level)) {
                                level = "0";
                            }
                            for (int j = 0; j < levelBeans.size(); j++) {

                                if (j == Integer.parseInt(level)) {
                                    MethodSelectBean bean = levelBeans.get(j);
                                    String levelText = bean.getText();
                                    bean.setSelect(true);

                                    if ("Mode_1".equals(lastClick)) {

                                        if (levelText.contains("Level_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "3"));
                                        } else if (levelText.contains("Level_2")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "2"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "1"));
                                        }
                                    } else if ("Mode_2".equals(lastClick) || "Mode_4".equals(lastClick)) {

                                        if (levelText.contains("Level_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "6"));
                                        } else if (levelText.contains("Level_2")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "5"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "4"));
                                        }
                                    } else if ("Mode_3".equals(lastClick)) {
                                        if (bean.getText().contains("Level_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "2"));
                                        } else if (bean.getText().contains("Level_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "1"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "0"));
                                        }
                                    }
                                }
                            }

//                            if (!"Mode_3".equals(lastClick) && !"Mode_4".equals(lastClick)) {
                            if (!"Mode_3".equals(lastClick)) {

                                String indexQuality = spUtil.getMethodLevel(lastClick + lastClick);
                                if (TextUtils.isEmpty(indexQuality)) {
                                    indexQuality = "0";
                                }

                                for (int k = 0; k < ABeans.size(); k++) {
                                    if (k == Integer.parseInt(indexQuality)) {
                                        ABeans.get(k).setSelect(true);


                                        //声源质量
                                        if (ABeans.get(k).getText().contains("Level_3")) {
                                            //发送声源质量命令
                                            send_3_CMD(2);
                                        } else if (ABeans.get(k).getText().contains("Level_2")) {
                                            //发送声源质量命令
                                            send_3_CMD(1);
                                        } else {
                                            //发送声源质量命令
                                            send_3_CMD(0);
                                        }
                                    }
                                }
                            }
                        }
                    }

//                    if ("Mode_3".equals(methodText) || "Mode_4".equals(methodText)) {
                    if ("Mode_3".equals(methodText)) {
                        for (int i = 0; i < ABeans.size(); i++) {
                            ABeans.get(i).setSelect(false);
                            ABeans.get(i).setShow(true);
                        }
                    }

                    selectAdapter.setNewData(selectBeans);
                    levelAdapter.setNewData(levelBeans);
                    methodAlAdapter.setNewData(ABeans);
                } else {
                    recycleLayout.setVisibility(View.GONE);
                    selectBeans.clear();
                    levelBeans.clear();
                    ABeans.clear();

                    selectAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    levelAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    methodAlAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    //关闭后发送关闭指令
                    mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, "off");
                }
            }
        });

        //方法选择
        selectAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                methodText = selectBeans.get(position).getText();
                spUtil.saveLastClick(methodText);

                for (MethodSelectBean method : selectBeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }

                //保存的降噪等级下标
                String methodLevel = spUtil.getMethodLevel(methodText);
                if (TextUtils.isEmpty(methodLevel)) {
                    methodLevel = "0";
                }
                int anInt = Integer.parseInt(methodLevel);
                for (int i = 0; i < levelBeans.size(); i++) {

                    if (i == anInt) {
                        levelBeans.get(i).setSelect(true);
                    } else {
                        levelBeans.get(i).setSelect(false);
                    }
                }
                levelAdapter.notifyDataSetChanged();

                //保存的声源质量下标
                String methevel = spUtil.getMethodLevel(methodText + methodText);
                if (TextUtils.isEmpty(methevel)) {
                    methevel = "0";
                }
                int parseInt = Integer.parseInt(methevel);
                for (int i = 0; i < ABeans.size(); i++) {

//                    if ("Mode_3".equals(methodText) || "Mode_4".equals(methodText)) {
                    if ("Mode_3".equals(methodText)) {
                        ABeans.get(i).setSelect(false);
                        ABeans.get(i).setShow(true);
                    } else {
                        ABeans.get(i).setShow(false);
                        if (i == parseInt) {
                            ABeans.get(i).setSelect(true);
                        } else {
                            ABeans.get(i).setSelect(false);
                        }
                    }

                }
                methodAlAdapter.notifyDataSetChanged();

                MethodSelectBean method = selectBeans.get(position);
                method.setSelect(!method.isSelect());
                selectAdapter.notifyDataSetChanged();
                MethodSelectBean level = null;
                for (MethodSelectBean methodSelectBean : levelAdapter.getData()) {
                    if (methodSelectBean.isSelect()) {
                        level = methodSelectBean;
                    }
                }
                if (method.isSelect()) {
                    mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, method.getKey());
                    if (level != null) {
                        spUtil.saveMethodLevel(CV.METHOD_LEVEL, level.getText());

                        if (method.getText().contains("Mode_4")) {

                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 4) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);

                        } else if (method.getText().contains("Mode_3")) {

                            mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", level.getPosition() + ""));

                        } else if (method.getText().contains("Mode_2")) {

                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 4) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);
                        } else {
                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 1) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);
                        }
                    }
                }
            }
        });

        //等级选择
        levelAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!selectAdapter.isItemSelect()) {
//                    com.blankj.utilcode.util.ToastUtils.showShort("请先选择降噪方法");
                    return;
                }
                for (MethodSelectBean method : levelBeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }
                MethodSelectBean method = levelBeans.get(position);
                method.setSelect(!method.isSelect());
                spUtil.saveMethodLevel(CV.METHOD_LEVEL, method.getText());

                String lastClick = spUtil.getLastClick();
                if (!TextUtils.isEmpty(lastClick)) {

                    methodText = lastClick;
                }
                spUtil.saveMethodLevel(methodText, "" + position);
                levelAdapter.notifyDataSetChanged();

                MethodSelectBean selectBean = null;
                for (MethodSelectBean bean : levelAdapter.getData()) {
                    if (bean.isSelect()) {
                        selectBean = bean;
                    }
                }
                if (method.isSelect()) {
                    if (selectBean != null) {
                        LogUtils.e(selectBean.toString());
                        spUtil.saveMethodLevel(CV.METHOD_SELECT, selectBean.getText());

                        if ("Mode_2".equals(methodText) || "Mode_4".equals(methodText)) {
                            if (selectBean.getText().contains("Level_3")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            } else if (selectBean.getText().contains("Level_2")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            }
                        } else if ("Mode_1".equals(methodText)) {
                            if (selectBean.getText().contains("Level_3")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "3"));
                            } else if (selectBean.getText().contains("Level_2")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "2"));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "1"));
                            }
                        } else {

                            if (selectBean.getText().contains("Level_3")) {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "2"));
                            } else if (selectBean.getText().contains("Level_2")) {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "1"));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "0"));
                            }
                        }
                    }
                }
            }
        });

        //声源质量选择
        methodAlAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                if (!selectAdapter.isItemSelect()) {
//                    ToastUtils.showShort("请先选择降噪方法");
                    return;
                }

                for (MethodSelectBean method : ABeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }

                MethodSelectBean method = ABeans.get(position);
                method.setSelect(!method.isSelect());

                spUtil.saveMethodLevel(methodText + methodText, "" + position);

                //发送声源质量命令
                send_3_CMD(position);

                methodAlAdapter.notifyDataSetChanged();
            }
        });
        method_switch.setChecked(spUtil.getMethodSwitch(CV.METHOD_SWITHCH));
        return view;
    }

    //降噪方法弹窗
    private View get_yinni_view() {
        final List<MethodSelectBean> selectBeans = new ArrayList<>();
        final List<MethodSelectBean> levelBeans = new ArrayList<>();
        final List<MethodSelectBean> ABeans = new ArrayList<>();

        final MethodSelectAdapter selectAdapter = new MethodSelectAdapter(new ArrayList<MethodSelectBean>());
        final MethodLevelAdapter levelAdapter = new MethodLevelAdapter(new ArrayList<MethodSelectBean>());
        final MethodAlAdapter methodAlAdapter = new MethodAlAdapter(new ArrayList<MethodSelectBean>());

        final View view = LayoutInflater.from(context).inflate(R.layout.view_method_select_layout, null);

        final LinearLayout recycleLayout = view.findViewById(R.id.method_recycle_layout);
        final RecyclerView selectRecycleView = view.findViewById(R.id.method_way_recycle);
        final RecyclerView levelRecycleView = view.findViewById(R.id.method_level_recycle);
        final RecyclerView method_A_recycle = view.findViewById(R.id.method_A_recycle);

        final TextView tv_noise_reduction_switch = view.findViewById(R.id.tv_noise_reduction_switch);
//        tv_noise_reduction_switch.setText("Noise Reduction Switch");
        tv_noise_reduction_switch.setText("Saklar Pengurangan Kebisingan");

        selectRecycleView.setAdapter(selectAdapter);
        levelRecycleView.setAdapter(levelAdapter);
        method_A_recycle.setAdapter(methodAlAdapter);

        Switch method_switch = view.findViewById(R.id.method_switch);
        method_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                spUtil.saveMethodSwitch(CV.METHOD_SWITHCH, isChecked);

                if (isChecked) {
                    recycleLayout.setVisibility(View.VISIBLE);

                    selectBeans.clear();
                    levelBeans.clear();
                    ABeans.clear();

                    selectBeans.addAll(selectAdapter.getSelectData());
                    levelBeans.addAll(levelAdapter.getLevelData());
                    ABeans.addAll(methodAlAdapter.getGreadData());

                    String lastClick = spUtil.getLastClick();
                    if (!TextUtils.isEmpty(lastClick)) {

                        methodText = lastClick;
                    }

                    for (int i = 0; i < selectBeans.size(); i++) {

                        if (selectBeans.get(i).getText().equals(lastClick)) {
                            selectBeans.get(i).setSelect(true);
                            mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, selectBeans.get(i).getKey());

                            String level = spUtil.getMethodLevel(lastClick);

                            if (TextUtils.isEmpty(level)) {
                                level = "0";
                            }
                            for (int j = 0; j < levelBeans.size(); j++) {

                                if (j == Integer.parseInt(level)) {
                                    MethodSelectBean bean = levelBeans.get(j);
                                    String levelText = bean.getText();
                                    bean.setSelect(true);

                                    if ("Mode_Satu".equals(lastClick)) {

                                        if (levelText.contains("Tingkat_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "3"));
                                        } else if (levelText.contains("Tingkat_2")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "2"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "1"));
                                        }
                                    } else if ("Mode_Dua".equals(lastClick) || "Mode_Empat".equals(lastClick)) {

                                        if (levelText.contains("Tingkat_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "6"));
                                        } else if (levelText.contains("Tingkat_2")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "5"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", bean.getKey(), "4"));
                                        }
                                    } else if ("Mode_Tiga".equals(lastClick)) {
                                        if (bean.getText().contains("Tingkat_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "2"));
                                        } else if (bean.getText().contains("Tingkat_3")) {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "1"));
                                        } else {
                                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s", "0"));
                                        }
                                    }
                                }
                            }

//                            if (!"Mode_3".equals(lastClick) && !"Mode_4".equals(lastClick)) {
                            if (!"Mode_Tiga".equals(lastClick)) {

                                String indexQuality = spUtil.getMethodLevel(lastClick + lastClick);
                                if (TextUtils.isEmpty(indexQuality)) {
                                    indexQuality = "0";
                                }

                                for (int k = 0; k < ABeans.size(); k++) {
                                    if (k == Integer.parseInt(indexQuality)) {
                                        ABeans.get(k).setSelect(true);


                                        //声源质量
                                        if (ABeans.get(k).getText().contains("Tingkat_3")) {
                                            //发送声源质量命令
                                            send_3_CMD(2);
                                        } else if (ABeans.get(k).getText().contains("Tingkat_2")) {
                                            //发送声源质量命令
                                            send_3_CMD(1);
                                        } else {
                                            //发送声源质量命令
                                            send_3_CMD(0);
                                        }
                                    }
                                }
                            }
                        }
                    }

//                    if ("Mode_3".equals(methodText) || "Mode_4".equals(methodText)) {
                    if ("Mode_Tiga".equals(methodText)) {
                        for (int i = 0; i < ABeans.size(); i++) {
                            ABeans.get(i).setSelect(false);
                            ABeans.get(i).setShow(true);
                        }
                    }

                    selectAdapter.setNewData(selectBeans);
                    levelAdapter.setNewData(levelBeans);
                    methodAlAdapter.setNewData(ABeans);
                } else {
                    recycleLayout.setVisibility(View.GONE);
                    selectBeans.clear();
                    levelBeans.clear();
                    ABeans.clear();

                    selectAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    levelAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    methodAlAdapter.setNewData(new ArrayList<MethodSelectBean>());
                    //关闭后发送关闭指令
                    mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, "off");
                }
            }
        });

        //方法选择
        selectAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                methodText = selectBeans.get(position).getText();
                spUtil.saveLastClick(methodText);

                for (MethodSelectBean method : selectBeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }

                //保存的降噪等级下标
                String methodLevel = spUtil.getMethodLevel(methodText);
                if (TextUtils.isEmpty(methodLevel)) {
                    methodLevel = "0";
                }
                int anInt = Integer.parseInt(methodLevel);
                for (int i = 0; i < levelBeans.size(); i++) {

                    if (i == anInt) {
                        levelBeans.get(i).setSelect(true);
                    } else {
                        levelBeans.get(i).setSelect(false);
                    }
                }
                levelAdapter.notifyDataSetChanged();

                //保存的声源质量下标
                String methevel = spUtil.getMethodLevel(methodText + methodText);
                if (TextUtils.isEmpty(methevel)) {
                    methevel = "0";
                }
                int parseInt = Integer.parseInt(methevel);
                for (int i = 0; i < ABeans.size(); i++) {

//                    if ("Mode_3".equals(methodText) || "Mode_4".equals(methodText)) {
                    if ("Mode_Tiga".equals(methodText)) {
                        ABeans.get(i).setSelect(false);
                        ABeans.get(i).setShow(true);
                    } else {
                        ABeans.get(i).setShow(false);
                        if (i == parseInt) {
                            ABeans.get(i).setSelect(true);
                        } else {
                            ABeans.get(i).setSelect(false);
                        }
                    }

                }
                methodAlAdapter.notifyDataSetChanged();

                MethodSelectBean method = selectBeans.get(position);
                method.setSelect(!method.isSelect());
                selectAdapter.notifyDataSetChanged();
                MethodSelectBean level = null;
                for (MethodSelectBean methodSelectBean : levelAdapter.getData()) {
                    if (methodSelectBean.isSelect()) {
                        level = methodSelectBean;
                    }
                }
                if (method.isSelect()) {
                    mJsonHandle6802.sendCmd(CV.SET_DENOISE_METHOD, method.getKey());
                    if (level != null) {
                        spUtil.saveMethodLevel(CV.METHOD_LEVEL, level.getText());

                        if (method.getText().contains("Mode_Empat")) {

                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 4) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);

                        } else if (method.getText().contains("Mode_Tiga")) {

                            mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", level.getPosition() + ""));

                        } else if (method.getText().contains("Mode_Dua")) {

                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 4) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);
                        } else {
                            mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", level.getKey(), (level.getPosition() + 1) + ""));

                            //发送声源质量命令
                            send_3_CMD(parseInt);
                        }
                    }
                }
            }
        });

        //等级选择
        levelAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!selectAdapter.isItemSelect()) {
//                    com.blankj.utilcode.util.ToastUtils.showShort("请先选择降噪方法");
                    return;
                }
                for (MethodSelectBean method : levelBeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }
                MethodSelectBean method = levelBeans.get(position);
                method.setSelect(!method.isSelect());
                spUtil.saveMethodLevel(CV.METHOD_LEVEL, method.getText());

                String lastClick = spUtil.getLastClick();
                if (!TextUtils.isEmpty(lastClick)) {

                    methodText = lastClick;
                }
                spUtil.saveMethodLevel(methodText, "" + position);
                levelAdapter.notifyDataSetChanged();

                MethodSelectBean selectBean = null;
                for (MethodSelectBean bean : levelAdapter.getData()) {
                    if (bean.isSelect()) {
                        selectBean = bean;
                    }
                }
                if (method.isSelect()) {
                    if (selectBean != null) {
                        LogUtils.e(selectBean.toString());
                        spUtil.saveMethodLevel(CV.METHOD_SELECT, selectBean.getText());

                        if ("Mode_Dua".equals(methodText) || "Mode_Empat".equals(methodText)) {
                            if (selectBean.getText().contains("Tingkat_3")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            } else if (selectBean.getText().contains("Tingkat_2")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), (method.getPosition() + 4) + ""));
                            }
                        } else if ("Mode_Satu".equals(methodText)) {
                            if (selectBean.getText().contains("Tingkat_3")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "3"));
                            } else if (selectBean.getText().contains("Tingkat_2")) {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "2"));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_NOISE_VIEW, String.format("%s%s", method.getKey(), "1"));
                            }
                        } else {

                            if (selectBean.getText().contains("Tingkat_3")) {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "2"));
                            } else if (selectBean.getText().contains("Tingkat_2")) {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "1"));
                            } else {
                                mJsonHandle6802.sendCmd(CV.SET_WEBRTC_GRADE, String.format("%s", "0"));
                            }
                        }
                    }
                }
            }
        });

        //声源质量选择
        methodAlAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                if (!selectAdapter.isItemSelect()) {
//                    ToastUtils.showShort("请先选择降噪方法");
                    return;
                }

                for (MethodSelectBean method : ABeans) {
                    if (method.isSelect()) {
                        method.setSelect(false);
                    }
                }

                MethodSelectBean method = ABeans.get(position);
                method.setSelect(!method.isSelect());

                spUtil.saveMethodLevel(methodText + methodText, "" + position);

                //发送声源质量命令
                send_3_CMD(position);

                methodAlAdapter.notifyDataSetChanged();
            }
        });
        method_switch.setChecked(spUtil.getMethodSwitch(CV.METHOD_SWITHCH));
        return view;
    }


    //发送声源质量命令
    private void send_3_CMD(int parseInt) {
        if (parseInt == 0) {
            mJsonHandle6802.sendCmd(CV.DEMODE_SBDSRC, String.format("%s", "0"));
        } else if (parseInt == 1) {
            mJsonHandle6802.sendCmd(CV.DEMODE_SBDSRC, String.format("%s", "1"));
        } else {
            mJsonHandle6802.sendCmd(CV.DEMODE_SBDSRC, String.format("%s", "2"));
        }
    }
}
