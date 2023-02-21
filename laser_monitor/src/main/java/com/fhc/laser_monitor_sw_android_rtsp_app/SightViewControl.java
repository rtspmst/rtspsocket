package com.fhc.laser_monitor_sw_android_rtsp_app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.fhc.laser_monitor_sw_android_rtsp_app.activity.BootLogActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.MyToastUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.views.DrawView;

/**
 * Created by fhc on 8/10/2017.
 */
public class SightViewControl {

    private Context mContext;

    private Handler mHandler;

    private DrawView mDrawView;

    private float centerX = 50f;
    private float centerY = 50f;
    private float scale = 1.7f;

    public SightViewControl(Context context, Handler handler, DrawView drawView) {

        mContext = context;
        mHandler = handler;
        mDrawView = drawView;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX / scale;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY / scale;
    }

    //设置屏幕比例
    public void setScale(float scale) {
        this.scale = scale;
    }


    //保存当前准星位置，有且只有在大预览的时候才会执行此操作
    //先保存 再发送指令 保存的坐标值为640*480坐标系中的值
    public void saveCrosshairLoaction(int currentView) {

        Message msg = Message.obtain();

        //将坐标存入本地
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("CALIBRATE", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (currentView) {
            //大视野视图大预览
            case CV.BIG_SIGHT_VIEW_BIG_PREVIEW:
                editor.putFloat("coordinateX0", centerX);
                editor.putFloat("coordinateY0", centerY);
                msg.what = CV.MSG_SET_CAM3_CENTER;
                mHandler.sendMessage(msg);
                Log.e("准星坐标 保存", "大视野视图大预览 centerX == " + centerX + " centerY == " + centerY);

                break;
            //小视野大预览
            case CV.SMALL_SIGHT_VIEW_BIG_PREVIEW:
                editor.putFloat("coordinateX1", centerX);
                editor.putFloat("coordinateY1", centerY);

                Log.e("准星坐标 保存", "小视野大预览 centerX == " + centerX + "  centerY == " + centerY);

                msg.what = CV.MSG_SET_CAM1_CENTER;
                mHandler.sendMessage(msg);

                break;
            //辅助视图正常预览
            case CV.AUXILIARY_VIEW_NORMAL_PREVIEW:
                editor.putFloat("coordinateX2", centerX);
                editor.putFloat("coordinateY2", centerY);

                msg.what = CV.MSG_SET_CAM2_CENTER;
                mHandler.sendMessage(msg);

                Log.e("准星坐标 保存", "辅助视图正常预览 centerX == " + centerX + "  centerY == " + centerY);

                break;
            case CV.AUXILIARY_RRRR:
                Log.e("准星坐标", "获取测试准星坐标什么也不干");
                break;
            default:
                break;
        }

        editor.commit();
    }

    //保存传上来的数据 保存的坐标值为640*480坐标系中的值
    public void saveCrosshairLoaction(int view, float centerX, float centerY) {

        //将坐标存入本地
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("CALIBRATE",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (view) {
            case CV.BIG_SIGHT_VIEW_NORMAL_PREVIEW:
                editor.putFloat("coordinateX0", centerX);
                editor.putFloat("coordinateY0", centerY);

                break;
            case CV.SMALL_SIGHT_VIEW_NORMAL_PREVIEW:
                editor.putFloat("coordinateX1", centerX);
                editor.putFloat("coordinateY1", centerY);

                break;
            case CV.AUXILIARY_VIEW_NORMAL_PREVIEW:
                editor.putFloat("coordinateX2", centerX);
                editor.putFloat("coordinateY2", centerY);

                break;
            case CV.AUXILIARY_RRRR:
                Log.e("TAG", "保存测试准星坐标: centerX == " + centerX + "   centerY == " + centerY);
//                editor.putFloat("coordinateX3", centerX);
//                editor.putFloat("coordinateY3", centerY);
                break;
            default:
                break;
        }

        editor.commit();
    }

    //刷新十字线位置  保存的数据全部在“大预览”中，刷新时可能在其他预览中
    public void refreashCrosshairLoaction(int currentView) {

        float centerX = 50f;
        float centerY = 50f;

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("CALIBRATE",
                Activity.MODE_PRIVATE);
        switch (currentView) {

            case CV.BIG_SIGHT_VIEW_NORMAL_PREVIEW:
            case CV.BIG_SIGHT_VIEW_BIG_PREVIEW:
            case CV.BIG_SIGHT_VIEW_SMALL_PREVIEW:
                centerX = sharedPreferences.getFloat("coordinateX0", 360f);
                centerY = sharedPreferences.getFloat("coordinateY0", 240f);
                break;

            case CV.SMALL_SIGHT_VIEW_NORMAL_PREVIEW:
            case CV.SMALL_SIGHT_VIEW_BIG_PREVIEW:
            case CV.SMALL_SIGHT_VIEW_SMALL_PREVIEW:
                centerX = sharedPreferences.getFloat("coordinateX1", 360f);
                centerY = sharedPreferences.getFloat("coordinateY1", 240f);
                break;

            case CV.AUXILIARY_VIEW_NORMAL_PREVIEW:
                centerX = sharedPreferences.getFloat("coordinateX2", 360f);
                centerY = sharedPreferences.getFloat("coordinateY2", 240f);
                break;

            case CV.AUXILIARY_RRRR:

                centerX = sharedPreferences.getFloat("coordinateX3", 360f);
                centerY = sharedPreferences.getFloat("coordinateY3", 240f);
                Log.e("TAG", "获取测试准星坐标 centerX == " + centerX + "  centerY == " + centerY);
                break;
            default:
                break;
        }

        mDrawView.drawRec(centerX, centerY, scale);
        mDrawView.invalidate();

    }

    //设置绘制视图消失
    public void setDrawView_GONE() {
        mDrawView.setVisibility(View.GONE);
    }

    //设置绘图视图可见
    public void setDrawView_VISIBLE() {
        mDrawView.setVisibility(View.VISIBLE);
    }

    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    //=====================================================================================================
    private EditText editText;

    public void startCalibration(final int whoAmI) {
        final Dialog keyboardDialog = new Dialog(mContext);
        keyboardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        keyboardDialog.setCancelable(false);
        keyboardDialog.setContentView(R.layout.keyboard_layout);

        editText = keyboardDialog.findViewById(R.id.etPassword);

        keyboardDialog.findViewById(R.id.num1).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num2).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num3).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num4).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num5).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num6).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num7).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num8).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num9).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.num0).setOnClickListener(numsOnClickListenter);
        keyboardDialog.findViewById(R.id.btnReInput).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        keyboardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //弹窗消失时 不弹窗
                MyToastUtils.isShow = true;
            }
        });
        keyboardDialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String trim = editText.getText().toString().trim();

                if (trim.length() < 4) {

                    Log.e("TAG", "少于四位等待输入 ");
                    return;
                }

                //判断用户输入正确或错误的密码
                Message message = new Message();
                if ("1185".equals(trim)) {
                    //准星校准光斑
                    mimaAlertDialog();
                    message.what = CV.PASSWORD_CORRECT;
                    message.arg1 = whoAmI;
                    mHandler.sendMessage(message);

                    keyboardDialog.dismiss();

                } else if ("1203".equals(trim)) { //输入自动对焦调整
                    mimaAlertDialog();
                    message.what = CV.MSG_ADJUST_AUTO_FOCUS;
                    mHandler.sendMessage(message);
                    keyboardDialog.dismiss();

                } else if ("0815".equals(trim)) { //输入自动对焦调整
                    mimaAlertDialog();
                    message.what = CV.MSG_OPERATE_PROTECT_STEPPER;
                    mHandler.sendMessage(message);
                    keyboardDialog.dismiss();

                } else if ("5577".equals(trim)) {
                    //打开开机日志页面
                    mimaAlertDialog();
                    Intent intent = new Intent(mContext, BootLogActivity.class);
                    intent.putExtra("from", "1");
                    mContext.startActivity(intent);

                } else if ("5578".equals(trim)) {
                    //打开开机日志页面
                    mimaAlertDialog();
                    if (mContext != null) {
                        Intent intent = new Intent(mContext, BootLogActivity.class);
                        intent.putExtra("from", "2");
                        mContext.startActivity(intent);
                    }

                } else if ("5579".equals(trim)) {
                    mimaAlertDialog();
                    //返回点击步数
                    message.what = CV.MSG_DISPLAY_MOTOR_STEPS;
                    mHandler.sendMessage(message);
                    keyboardDialog.dismiss();

                } else if ("9999".equals(trim)) {

                    mimaAlertDialog();
                    //中英文切换 按钮显示隐藏
                    message.what = CV.MSG_SWITCH_LANGUAGE;
                    mHandler.sendMessage(message);
                    keyboardDialog.dismiss();

                } else {

//                    ToastUtils.showShort("密码错误");
                }

                editText.setText("");
            }
        });

        keyboardDialog.show();
    }

    View.OnClickListener numsOnClickListenter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.num1:
                    editText.append("1");
                    break;
                case R.id.num2:
                    editText.append("2");
                    break;
                case R.id.num3:
                    editText.append("3");
                    break;
                case R.id.num4:
                    editText.append("4");
                    break;
                case R.id.num5:
                    editText.append("5");
                    break;
                case R.id.num6:
                    editText.append("6");
                    break;
                case R.id.num7:
                    editText.append("7");
                    break;
                case R.id.num8:
                    editText.append("8");
                    break;
                case R.id.num9:
                    editText.append("9");
                    break;
                case R.id.num0:
                    editText.append("0");
                    break;
                default:
                    break;
            }
        }
    };


    public void mimaAlertDialog() {
        if (mContext != null) {
            final String[] items3 = new String[]{"1185 准星校准", "5577 开机日志", "5578 开机日志",
                    "5579 电机步数", "9999 按钮隐藏中英文切换",
                    "0815 电机锁定", "1203"};//创建item
            AlertDialog alertDialog3 = new AlertDialog.Builder(mContext)
                    .setTitle("点击任意位置关闭弹窗")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(items3, new DialogInterface.OnClickListener() {//添加列表
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create();
            alertDialog3.show();
        }
    }
}
