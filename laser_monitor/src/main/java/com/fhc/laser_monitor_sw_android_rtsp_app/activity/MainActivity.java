package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.LEFT_OF;
import static android.widget.RelativeLayout.RIGHT_OF;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.DatesUtils.cccccccc;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.getRealHeight;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.getRealWidth;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.hideyBar;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.setWindowBrightness;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.MyToastUtils.isShow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.ToastUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.DataHandleThread;
import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.SightViewControl;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.AudioDecoder;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.JsonHandle6802;
import com.fhc.laser_monitor_sw_android_rtsp_app.action.UartHandle6804;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.MessageEventBean;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.VideoTag;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ActivityMainBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewBottomButtonAreaBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewDirectionBarABinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewDirectionBarBBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewLeftHalfScreenBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewMotorAreaABinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewMotorAreaBBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewRightHalfScreenBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewTopSignalBarBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ViewVolumeAreaBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.fragment.PlayFragment;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DatesUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DensityUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DialogInspectionUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DialogNoiseUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DialogWiredNETUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.FileUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.OtgWorkReceiver;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.PermissionsUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.SharedPreferencesUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.views.DrawView;
import com.fhc.laser_monitor_sw_android_rtsp_app.views.SignSeekBar2;

import org.easydarwin.video.EasyPlayerClient;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity implements View.OnClickListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public static boolean IS_ENGLISH = true;
    public boolean longPressSetLocation = false;

    //    private String BigViewUrl = "rtsp://192.168.1.13:554/test";
//    private String SmallViewUrl = "rtsp://192.168.1.13:554/test";
//    private String AuxViewUrl = "rtsp://192.168.1.13:554/test";
    private String BigViewUrl = "rtsp://192.168.137.14:554/user=admin&password=&channel=1&stream=0.sdp?real_stream --rtp-caching=50";
    private String SmallViewUrl = "rtsp://192.168.137.12:554/user=admin&password=&channel=1&stream=0.sdp?real_stream --rtp-caching=50";
    private String AuxViewUrl = "rtsp://192.168.137.10:554/user=admin&password=&channel=1&stream=0.sdp?real_stream --rtp-caching=50";

    private String LEFT_VALUE = "左值";
    private String RIGHT_VALUE = "右值";
    private static final String TAG = "MainActivity";

    public static MainActivity mContext;

    private ActivityMainBinding bindingMain;
    private ViewLeftHalfScreenBinding bindingLeft;
    private ViewRightHalfScreenBinding bindingRight;
    private ViewTopSignalBarBinding bindingTop;//头部布局
    private ViewBottomButtonAreaBinding bindingBottom;//底部布局
    private ViewMotorAreaABinding bindingMotorA;//电机A
    private ViewMotorAreaBBinding bindingMotorB;//电机B
    private ViewVolumeAreaBinding bindingVolume;//声音
    private ViewDirectionBarABinding bindingDirectionA;//方向键A
    private ViewDirectionBarBBinding bindingDirectionB;//方向键B

    private RelativeLayout.LayoutParams rv1Lp;//左屏
    private RelativeLayout.LayoutParams rv2Lp;//右屏
    private RelativeLayout.LayoutParams rv3LPBottom;//底部
    private RelativeLayout.LayoutParams rv4LPMotor;//电机

    public static final int IMAGE_HEIGHT = 480; // 720 * 480

    private int GESTURE_FLAG = 0;// 1,调节RK3399音量，2，调节音量,3.调节亮度
    private final static int GESTURE_MODIFY_RK3399_VOLUME = 1;
    private final static int GESTURE_MODIFY_VOLUME = 2;
    private final static int GESTURE_MODIFY_BRIGHT = 3;
    private final static int GESTURE_MODIFY_TX2SPK = 4;
    public static boolean CHARGING = false; //是否充电中

    //音频解码器
    private AudioDecoder mAudioDecoder;

    private float mBrightness = -1f; // 亮度

    //亮度 音量
    private int maxTx2spkVolume = 500, Tx2spk_CurrentVolume = 32;

    private String distance = "186.88";//距离

    private boolean LEFT_DRAW_IS_BIG_PREVIEW = false;//记录左屏幕是大视野还是小视野
    private boolean RIGHT_DRAW_IS_BIG_PREVIEW = false; //记录右屏幕是大视野还是小视野
    private boolean LEFT_IS_CALIBRATION = false;//左准星校准 是否可滑动
    private boolean RIGHT_IS_CALIBRATION = false;//右准星校准 是否可滑动

    private JsonHandle6802 mJsonHandle_6802;
    private UartHandle6804 mUartHandle_6804;

    private byte RV1_STATUS = CV.NORMAL;
    private byte RV2_STATUS = CV.NORMAL;

    private SightViewControl sightViewControlLeft;//左准星
    private SightViewControl sightViewControlRight;//右准星
    private SightViewControl RRRRRRR;//

    private boolean isRecording = false; //判断是否在录像
    private GestureDetector mGestureDetector;//手势识别器
    private boolean firstScroll = false;// 每次触摸屏幕后，第一次scroll的标志

    private float leftDrawRectangleScale = 1.7f;
    private float rightDrawRectangleScale = 1.7f;

    private int leftLongClickCnt = 0;
    private int rightLongClickCnt = 0;

    private byte currentView = CV.BIG_SIGHT_VIEW;
    private byte currentPreview = CV.NORMAL_PREVIEW;
    private byte currentRealView = (byte) (currentView | currentPreview);

    private double exitTime;//退出时间
    private int screenWidth = 0;
    private int screenHeight = 0;
    private PlayFragment fragment;//大视场
    private PlayFragment fragment2;//小视场
    private PlayFragment fragment1;//辅助视图
    private SharedPreferencesUtil sPUtil;
    public static float voltage = 0f;//电量
    private boolean screen_height_change = false;//屏幕高是否改变了 1200改为1080

    private DialogNoiseUtils dialogNoiseUtils;

    //"AmpPwr":"low/high"  放大器功率切换
    private boolean AmpliNONEFlag = false;
    private float centerX;
    private float centerY;
    public static float huibo_power1 = 1.23f;
    public static int DISTANCE_VALUE = 0;
    private Drawable highDrawable;//高功率
    private Drawable lowDrawable;//低功率
    private Drawable shakingDrawable;//晃动
    private Drawable normalDrawable;//正常

    //检测有线连接
    private DialogWiredNETUtil inspectionNET;
    private DialogInspectionUtil inspection;
    private Drawable focusDrawable;
    private Drawable focusDrawable1;
    private Drawable focusDrawable2;

    private Drawable locationDrawable;
    private Drawable locationDrawable1;
    private Drawable pictureMute;//静音图片
    private Drawable noSound;//没有声音 未获得控制权

    private int bounds = 0;
    private boolean respond2Clicks;//点击声音按钮 三秒内响应连续点击三次隐藏按钮
    // tbRow3 tbRow4 是否显示
    public static int isShowTbRow = 0;
    private Vibrator vibrator;
    private boolean vibratorBoolean = true;
    private int vibratorTime = 50;//毫秒
    private int logo_red;
    private int white;
    private Drawable ic_stop_record_selector;//录像暂停 圆点
    private Drawable ic_record_selector;//录像原始图片
    private Drawable ic_manual_focus_selector;
    private AudioManager mAudioManager;
    private AudioManager am;
    private SharedPreferences sp;

    @SuppressLint("SetTextI18n")
    private void setLanguage() {

        //云台 调整架
        bindingDirectionA.btnHide.setText(Language.ADJUSTING_FRAME);
        //距离
        bindingTop.tvDistanceBar.setText(Language.DISTANCE);

        //自动对焦
        bindingBottom.btnAutoFocus.setText(Language.AUTO_FOCUS);
//            bindingBottom.btnTabRow2AutoFocus.setText(Language.AUTO_FOCUS);

        //开始录像
        bindingBottom.btnStartRecord.setText(Language.RECORDING);
//            bindingBottom.btnTabRow2StartRecord.setText(Language.RECORDING);

        //左电机
        bindingMotorA.tvHintA.setText(Language.LEFT_MOTOR);

        //声音
//        bindingVolume.tvSound.setText(Language.SOUND);

        //录像回放
        bindingBottom.btnPlayback.setText(Language.PLAYBACK);
//        bindingBottom.btnTabRow4Playback.setText(Language.PLAYBACK);

        //降噪选择
        bindingBottom.btnArithmeticSelect.setText(Language.DENOISE_METHOD);
//        bindingBottom.btnTabRow3NoiseSet.setText(Language.DENOISE_METHOD);


        //系统复位
        bindingBottom.btnReset.setText(Language.SYSTEM_RESET);
//            bindingBottom.btnTabRow4Reset.setText("reset");

        //设置位置 保存位置
//        bindingBottom.btnTabRow3SetLocation.setText(Language.SET_LOCATION);

        //拍照
        bindingBottom.btnPhotograph.setText(Language.PHOTOGRAPH);

        //normal
        bindingBottom.btnDemodeSelect.setText(Language.NORMALMODE);

        //有线模式
        bindingBottom.btnOnlineSelect.setText(Language.WIRED_MODE);

        //省电模式
        bindingBottom.shengdian.setText(Language.POWER_SAVE);

        //一键自检
        bindingBottom.btnInspection.setText(Language.ONE_KEY_SELF_CHECKING);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //关闭其他音乐播放器
        mAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);//add by song

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        EventBus.getDefault().register(this);

        initGuangbo();

        vibrator = (Vibrator) getSystemService(MainActivity.VIBRATOR_SERVICE);

        mContext = this;

        //初始化SP
        sPUtil = new SharedPreferencesUtil();

        //初始化UI
        initUI();

        //获取保存的音量
        Tx2spk_CurrentVolume = sPUtil.getTx2spkVolume();

        // 全屏
        hideyBar(this);

        //权限
        PermissionsUtils.getPermissions(mContext);

        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //初始化图片资源
        initResources();

        //语言标志
        IS_ENGLISH = sPUtil.getLanguage();
        IS_ENGLISH = true;

        //设置语言
        setLanguage();

        //手势检测器初始化
        mGestureDetector = new GestureDetector(this, this);

        //透传连接
        mUartHandle_6804 = new UartHandle6804();
        //透传连接 开启新线程
        new DataHandleThread(mHandler, mUartHandle_6804).start();

        //准星坐标 回传
        mJsonHandle_6802 = new JsonHandle6802();
        //准星坐标回调
        initHandleCallback();

        //等比例屏幕处理
        screenSplit();

        //准星 init
        crosshairInit();

        //初始化Fragment
        initFragment();

        // OPUS 音频编解码器
        startAudio(fragment1);

        //初始化降噪弹窗
        dialogNoiseUtils = new DialogNoiseUtils(mContext, mJsonHandle_6802, sPUtil, vibrator, vibratorBoolean, vibratorTime);

        //初始化开机自检弹窗
        inspection = new DialogInspectionUtil(mContext);

        inspectionNET = new DialogWiredNETUtil(mContext);

        //自动息屏
        automaticRestScreen();

        animation = AnimationUtils.loadAnimation(this, R.anim.translate_anim);
        animation1 = AnimationUtils.loadAnimation(this, R.anim.translate_anim1);
        animation2 = AnimationUtils.loadAnimation(this, R.anim.translate_anim2);
        animation3 = AnimationUtils.loadAnimation(this, R.anim.translate_anim3);

        setLogo(true);

        sp = getSharedPreferences("CALIBRATE", Activity.MODE_PRIVATE);

    }


    private void initResources() {

        logo_red = ContextCompat.getColor(mContext, R.color.logo_red);
        white = ContextCompat.getColor(mContext, R.color.white);

        bounds = DensityUtil.dip2px(mContext, 40);

        //手指
        ic_manual_focus_selector = getResources().getDrawable(R.drawable.ic_manual_focus_selector, getTheme());
        ic_manual_focus_selector.setBounds(0, 0, bounds, bounds);

        //录像暂停 圆点
        ic_stop_record_selector = getResources().getDrawable(R.drawable.ic_stop_record_selector, getTheme());
        ic_stop_record_selector.setBounds(0, 0, bounds, bounds);

        //录像原始图片
        ic_record_selector = getResources().getDrawable(R.drawable.ic_record_selector, getTheme());
        ic_record_selector.setBounds(0, 0, bounds, bounds);

        highDrawable = getResources().getDrawable(R.drawable.ic_scenario_selector, getTheme());
        highDrawable.setBounds(0, 0, bounds, bounds);

        lowDrawable = getResources().getDrawable(R.drawable.ic_scenario_back_selector, getTheme());
        lowDrawable.setBounds(0, 0, bounds, bounds);

        shakingDrawable = getResources().getDrawable(R.drawable.ic_shaking, getTheme());
        shakingDrawable.setBounds(0, 0, bounds, bounds);
        normalDrawable = getResources().getDrawable(R.drawable.ic_normal, getTheme());
        normalDrawable.setBounds(0, 0, bounds, bounds);

        focusDrawable = getResources().getDrawable(R.drawable.ic_focus_selector1, getTheme());
        focusDrawable.setBounds(0, 0, bounds, bounds);

        focusDrawable1 = getResources().getDrawable(R.drawable.ic_focus_selector, getTheme());
        focusDrawable1.setBounds(0, 0, bounds, bounds);

        focusDrawable2 = getResources().getDrawable(R.drawable.ic_focus_selector2, getTheme());
        focusDrawable2.setBounds(0, 0, bounds, bounds);

        locationDrawable = getResources().getDrawable(R.drawable.ic_dingwei_red, getTheme());
        locationDrawable.setBounds(0, 0, bounds, bounds);

        locationDrawable1 = getResources().getDrawable(R.drawable.ic_dingwei_white, getTheme());
        locationDrawable1.setBounds(0, 0, bounds, bounds);

        pictureMute = getResources().getDrawable(R.drawable.souhu_player_silence, getTheme());
        pictureMute.setBounds(0, 0, bounds, bounds);

        noSound = getResources().getDrawable(R.drawable.ic_control, getTheme());
        noSound.setBounds(0, 0, bounds, bounds);
    }

    private void screenSplit() {

        // 获取屏幕的实际宽度和高度 设置屏幕宽高
        screenWidth = getRealWidth(this);
        screenHeight = getRealHeight(this);

//        if (screenHeight == 1200) {
//            screenHeight = 1080;
//            screen_height_change = true;
//        }

        // 左边屏幕
        rv1Lp = (RelativeLayout.LayoutParams) bindingLeft.rv1.getLayoutParams();
        rv1Lp.width = screenWidth / 2;
        rv1Lp.height = rv1Lp.width * 2 / 3;
        bindingLeft.rv1.setLayoutParams(rv1Lp);
        //左 更改左矩形的比例
        leftDrawRectangleScale = rv1Lp.height / (float) IMAGE_HEIGHT;

        //右边屏幕
        rv2Lp = (RelativeLayout.LayoutParams) bindingRight.rv2.getLayoutParams();
        rv2Lp.width = screenWidth / 2;
        rv2Lp.height = rv2Lp.width * 2 / 3;
        bindingRight.rv2.setLayoutParams(rv2Lp);
        //右 更改右矩形的比例
        rightDrawRectangleScale = rv2Lp.height / (float) IMAGE_HEIGHT;

        //底部屏幕
        rv3LPBottom = (RelativeLayout.LayoutParams) bindingBottom.tabBottom.getLayoutParams();
        rv3LPBottom.width = screenWidth;
        rv3LPBottom.height = screenHeight - rv2Lp.height;
        bindingBottom.tabBottom.setLayoutParams(rv3LPBottom);

        //电机
//        rv4LPMotor = (RelativeLayout.LayoutParams) bindingMotorB.lyFabPlusMinusB.getLayoutParams();

        //设置方向键
        move_btn_from_right_to_left(ALIGN_PARENT_RIGHT, 0, 0, 90, 20);

    }

    //调整准星位置
    private void adjustTheFrontSight() {
        int centerX = screenWidth / 2 / 2;
        int centerY = screenWidth / 3 / 2;

        //拖动设置位置
        sightViewControlLeft.setCenterX(centerX);
        sightViewControlLeft.setCenterY(centerY);
        leftDrawView.drawRecForCalibration(centerX, centerY, leftDrawRectangleScale);
        leftDrawView.invalidate();
        sightViewControlLeft.saveCrosshairLoaction(CV.BIG_SIGHT_VIEW_BIG_PREVIEW);
        sightViewControlLeft.saveCrosshairLoaction(CV.SMALL_SIGHT_VIEW_BIG_PREVIEW);

        //拖动设置位置
        sightViewControlRight.setCenterX(centerX);
        sightViewControlRight.setCenterY(centerY);
        rightDrawView.drawRecForCalibration(centerX, centerY, rightDrawRectangleScale);
        rightDrawView.invalidate();
        sightViewControlRight.saveCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);
    }

    private void initFragment() {
        photographWhich = false;
        fragment = PlayFragment.newInstance(BigViewUrl, 1, 1, null);
        getSupportFragmentManager().beginTransaction().add(R.id.surfaceView1, fragment).commit();
        fragment.enterFullscreen();// 进入全屏模式

        fragment1 = PlayFragment.newInstance(AuxViewUrl, 1, 1, null);
        getSupportFragmentManager().beginTransaction().add(R.id.surfaceView2, fragment1).commit();
        fragment1.enterFullscreen();// 进入全屏模式
    }

    //准星坐标回调
    private void initHandleCallback() {

        mJsonHandle_6802.setmCamCenterXYCallback(new JsonHandle6802.CamCenterXYCallback() {

            @Override
            public void onCamCenterXY(String[] data) {

                if (data != null && data.length >= 2) {
                    sightViewControlLeft.saveCrosshairLoaction(CV.SMALL_SIGHT_VIEW_NORMAL_PREVIEW, Float.parseFloat(data[0]), Float.parseFloat(data[1]));
                    Log.e(TAG, "准星坐标 取出: 00 小视野 普通 预览 " + data[0] + " , " + data[1]);
                    sightViewControlLeft.refreashCrosshairLoaction(currentRealView);
                }
            }

            @Override
            public void onCamCenterXY1(String[] data) {

                if (data != null && data.length >= 2) {
                    sightViewControlRight.saveCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW, Float.parseFloat(data[0]), Float.parseFloat(data[1]));
                    Log.e(TAG, "准星坐标 取出: 11 辅助视 普通 预览 " + data[0] + " , " + data[1]);
                    sightViewControlRight.refreashCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);
                }
            }

            @Override
            public void onCamCenterXY2(String[] data) {

                if (data != null && data.length >= 2) {
                    sightViewControlLeft.saveCrosshairLoaction(CV.BIG_SIGHT_VIEW_NORMAL_PREVIEW, Float.parseFloat(data[0]), Float.parseFloat(data[1]));
                    Log.e(TAG, "准星坐标 取出: 22 大视野 普通 预览 " + data[0] + " , " + data[1]);
                    sightViewControlLeft.refreashCrosshairLoaction(currentRealView);
                }
            }
        });

        mJsonHandle_6802.setmRectanglePointCallback(new JsonHandle6802.rectanglePointCallback() {
            @Override
            public void onRectanglePoint(int[] points) {

                //返回左右两个准星的坐标
                float[] floatPoints = new float[8];

                for (int i = 0; i < points.length; i++) {
                    if (i < 4) {
                        floatPoints[i] = points[i] * leftDrawRectangleScale;
                    } else if (i < 8) {
                        floatPoints[i] = points[i] * rightDrawRectangleScale;
                    }
                }

                // 重绘左侧准星repaint left rectangle
                leftDrawView.drawRec(floatPoints[0], floatPoints[1], floatPoints[0] + floatPoints[2], floatPoints[1] + floatPoints[3]);
                leftDrawView.invalidate();

                // 重新右侧准星repaint right rectangle
                rightDrawView.drawRec(floatPoints[4], floatPoints[5], floatPoints[4] + floatPoints[6], floatPoints[5] + floatPoints[7]);
                rightDrawView.invalidate();
            }
        });
    }

    //左屏幕处理
    private void leftZoom() {
        if (RV1_STATUS == CV.NORMAL) {
            //从小屏幕到大屏幕处理

            //电机相关设置
            leftOrRight = false;
            clickMotor();

            //左电机区展示并靠左展示
            showLeftZoom(30, 320);

            setLogo(false);

            //头部 音量 显示隐藏
//            controlDisplayHidden(Gravity.CENTER_VERTICAL, VISIBLE);

            rv1Lp.width = screenHeight * 3 / 2;
            rv1Lp.height = screenHeight;
            bindingLeft.rv1.setLayoutParams(rv1Lp);
            //更改左矩形的比例
            leftDrawRectangleScale = rv1Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlLeft.setScale(leftDrawRectangleScale);
            sightViewControlLeft.refreashCrosshairLoaction(currentRealView);

            // 右半屏 从 大屏 到 小屏 处理
            rv2Lp.width = 0;
            rv2Lp.height = 0;
            bindingRight.rv2.setLayoutParams(rv2Lp);
            // 更改矩形的比例
            rightDrawRectangleScale = rv2Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlRight.setScale(rightDrawRectangleScale);
            sightViewControlRight.refreashCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);

//            rv3LPBottom.removeRule(LEFT_OF);
//            rv3LPBottom.addRule(RIGHT_OF, R.id.rv1);

//            rv3LPBottom.width = screenWidth - screenHeight * 3 / 2;
//
//            if (screen_height_change) {
//                rv3LPBottom.height = screenHeight + 120;
//            } else {
//                rv3LPBottom.height = screenHeight;
//            }
//            bindingBottom.tabBottom.setLayoutParams(rv3LPBottom);
//            showBottomButton(View.GONE, View.VISIBLE, isShowTbRow = 3);

            /*电机*/
//            rv4LPMotor.removeRule(RIGHT_OF);
//            rv4LPMotor.addRule(LEFT_OF, R.id.tab_bottom);
//            rv4LPMotor.addRule(ALIGN_TOP, R.id.tab_bottom);

            if (VISIBLE == bindingDirectionA.btnHide.getVisibility()) {
                bindingDirectionA.tbQuickDirectionButtons.setVisibility(VISIBLE);
                //改变云台字体 快慢显示
                changePtzText(Language.BALL_HEAD);
                isConsole = true;
            } else {
                /*隐藏方向按钮*/
                bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.GONE);
            }

            currentPreview = CV.BIG_PREVIEW;
            setCurrentRealView(currentPreview, currentView);

            //现在是大视野
            LEFT_DRAW_IS_BIG_PREVIEW = true;
            RV1_STATUS = CV.BIG;

            // 现在是小视图
            RIGHT_DRAW_IS_BIG_PREVIEW = false;
            RV2_STATUS = CV.SMALL;

            if (!photographWhich) {
                //如果是大市场 隐藏调节焦距按钮
                bindingMotorA.lyFabPlusMinusA.setVisibility(GONE);
            }

        } else if (RV1_STATUS == CV.BIG || RV1_STATUS == CV.SMALL) {
            //左半屏  大屏 变 小屏 处理 或还原右屏幕

            //电机相关设置
            leftOrRight = false;
            clickMotor();

            setLogo(true);

            //左电机区展示并靠左展示
            showLeftZoom(25, 180);

            //头部布局 音量 显示隐藏
//            controlDisplayHidden(Gravity.CENTER_VERTICAL, View.VISIBLE);

            //设置左半屏幕 更改矩形的比例  更新准星位置
            rv1Lp.width = screenWidth / 2;
            rv1Lp.height = rv1Lp.width * 2 / 3;
            bindingLeft.rv1.setLayoutParams(rv1Lp);

            leftDrawRectangleScale = rv1Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlLeft.setScale(leftDrawRectangleScale);
            sightViewControlLeft.refreashCrosshairLoaction(currentRealView);


            //设置右半屏幕 更改矩形的比例  更新准星位置
            rv2Lp.width = screenWidth / 2;
            rv2Lp.height = rv2Lp.width * 2 / 3;
            bindingRight.rv2.setLayoutParams(rv2Lp);

            rightDrawRectangleScale = rv2Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlRight.setScale(rightDrawRectangleScale);
            sightViewControlRight.refreashCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);

            //设置底部屏幕
            rv3LPBottom.removeRule(LEFT_OF);
            rv3LPBottom.removeRule(RIGHT_OF);
            rv3LPBottom.width = screenWidth;
            rv3LPBottom.height = screenHeight - rv2Lp.height;

            bindingBottom.tabBottom.setLayoutParams(rv3LPBottom);

//            showBottomButton(View.VISIBLE, View.GONE, isShowTbRow = 0);

            //设置方向键
            move_btn_from_right_to_left(ALIGN_PARENT_RIGHT, 0, 0, 90, 20);
            bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.VISIBLE);

            currentPreview = CV.NORMAL_PREVIEW;
            setCurrentRealView(currentPreview, currentView);

            //现在是正常的视图
            LEFT_DRAW_IS_BIG_PREVIEW = false;
            RV1_STATUS = CV.NORMAL;

            // 现在是正常的视图
            RIGHT_DRAW_IS_BIG_PREVIEW = false;
            RV2_STATUS = CV.NORMAL;

            if (!photographWhich) {
                //如果是大市场 隐藏调节焦距按钮
                bindingMotorA.lyFabPlusMinusA.setVisibility(VISIBLE);
            }

        }
    }

//    private void controlDisplayHidden(int centerVertical, int visible) {
//        //头部布局
////        bindingTop.lyHint.setGravity(centerVertical);
//        //音量区
////        bindingVolume.ryQuickOpt.setVisibility(GONE);
//    }

//    private void showBottomButton(int lord, int vice, int isShowTbRow) {
//        bindingBottom.tbRow1.setVisibility(lord);
//        bindingBottom.tbRow2.setVisibility(vice);
//        switch (isShowTbRow) {
//            case 3:
//
//                if (GONE == bindingDirectionA.btnHide.getVisibility()) {
//                    //当方向键中间按钮隐藏时 显示234 隐藏设置位置按钮
//                    bindingBottom.btnTabRow2StartRecord.setVisibility(VISIBLE);
//                    bindingBottom.btnTabRow3SetLocation.setVisibility(GONE);
//                    bindingBottom.tbRow3.setVisibility(View.VISIBLE);
//                    bindingBottom.tbRow4.setVisibility(View.VISIBLE);
//                } else {
//                    //当方向键中间按钮显示时 录像按钮隐藏
//                    bindingBottom.btnTabRow2StartRecord.setVisibility(GONE);
//                    bindingBottom.tbRow3.setVisibility(View.INVISIBLE);
//                    bindingBottom.tbRow4.setVisibility(View.INVISIBLE);
//                    bindingBottom.btnTabRow3SetLocation.setVisibility(VISIBLE);
//                }
//                break;
//            default:
//                bindingBottom.tbRow3.setVisibility(vice);
//                bindingBottom.tbRow4.setVisibility(vice);
//                bindingBottom.btnTabRow3SetLocation.setVisibility(GONE);
//                bindingBottom.btnTabRow2StartRecord.setVisibility(VISIBLE);
//                break;
//        }
//    }

    private void move_btn_from_right_to_left(int alignParent, int left, int top, int right, int bottom) {

        int w = DensityUtil.dip2px(mContext, 210);
        int h = DensityUtil.dip2px(mContext, 210);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, h);
        lp.addRule(alignParent, RelativeLayout.TRUE);
        lp.addRule(ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        lp.setMargins(left, top, right, 120);
        bindingDirectionA.tbQuickDirectionButtons.setLayoutParams(lp);
        bindingDirectionB.tbDirectionButtons.setLayoutParams(lp);
    }

    //右屏幕处理
    private void rightZoom() {
        if (RV2_STATUS == CV.NORMAL) {
            //右>>小屏>>双击>>大屏

            //电机相关设置
            leftOrRight = true;
            clickMotor();
            //不隐藏左电机 左电机区域处理7.24
//            showRightZoom();

            setLogo(false);

            showLeftZoom(30, 320);

            //控件显示隐藏
//            controlDisplayHidden(Gravity.CENTER, VISIBLE);

            //设置左屏 更改左矩形的比例 更新准星位置
            rv1Lp.width = screenWidth - screenHeight * 3 / 2;
            rv1Lp.width = 0;
            rv1Lp.height = rv1Lp.width * 2 / 3;
            bindingLeft.rv1.setLayoutParams(rv1Lp);

            leftDrawRectangleScale = rv1Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlLeft.setScale(leftDrawRectangleScale);
            sightViewControlLeft.refreashCrosshairLoaction(currentRealView);

            //设置右屏 更改矩形的比例 更新准星位置
            rv2Lp.width = screenHeight * 3 / 2;
            rv2Lp.height = screenHeight;
            bindingRight.rv2.setLayoutParams(rv2Lp);

            rightDrawRectangleScale = rv2Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlRight.setScale(rightDrawRectangleScale);
            sightViewControlRight.refreashCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);

            //设置方向键
            move_btn_from_right_to_left(ALIGN_PARENT_RIGHT, 0, 0, 90, 20);
            bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.VISIBLE);

            //设置底部屏幕
            rv3LPBottom.removeRule(RIGHT_OF);
            rv3LPBottom.addRule(LEFT_OF, R.id.rv2);
//            rv3LPBottom.width = screenWidth - screenHeight * 3 / 2;
            rv3LPBottom.width = 0;
            if (screen_height_change) {
                rv3LPBottom.height = screenHeight + 120;
            } else {
                rv3LPBottom.height = screenHeight;
            }
            bindingBottom.tabBottom.setLayoutParams(rv3LPBottom);
//            showBottomButton(View.GONE, View.VISIBLE, isShowTbRow = 0);

            //设置左右电机
//            rv4LPMotor.removeRule(LEFT_OF);
//            rv4LPMotor.addRule(RIGHT_OF, R.id.tab_bottom);
//            rv4LPMotor.addRule(ALIGN_TOP, R.id.tab_bottom);

            //更新CurrentRealView
            currentPreview = CV.SMALL_PREVIEW;
            setCurrentRealView(currentPreview, currentView);

            //现在是小视图
            LEFT_DRAW_IS_BIG_PREVIEW = false;
            RV1_STATUS = CV.SMALL;

            //现在是大视野
            RIGHT_DRAW_IS_BIG_PREVIEW = true;
            RV2_STATUS = CV.BIG;

        } else if (RV2_STATUS == CV.BIG || RV2_STATUS == CV.SMALL) {
            //右半屏>>大屏>>双击>>小屏 或还原左屏幕

            //电机区设置
            leftOrRight = false;
            clickMotor();
            //左电机区展示并靠左展示
            showLeftZoom(25, 180);

            setLogo(true);

            //控件显示隐藏
//            controlDisplayHidden(Gravity.CENTER_VERTICAL, View.VISIBLE);

            //左屏幕设置 更改左矩形的比例
            rv1Lp.width = screenWidth / 2;
            rv1Lp.height = rv1Lp.width * 2 / 3;
            bindingLeft.rv1.setLayoutParams(rv1Lp);

            leftDrawRectangleScale = rv1Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlLeft.setScale(leftDrawRectangleScale);
            sightViewControlLeft.refreashCrosshairLoaction(currentRealView);

            //右屏幕设置 更改矩形的比例
            rv2Lp.width = screenWidth / 2;
            rv2Lp.height = rv2Lp.width * 2 / 3;
            bindingRight.rv2.setLayoutParams(rv2Lp);

            rightDrawRectangleScale = rv2Lp.height / (float) IMAGE_HEIGHT;
            sightViewControlRight.setScale(rightDrawRectangleScale);
            sightViewControlRight.refreashCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);

            //方向键设置
            move_btn_from_right_to_left(ALIGN_PARENT_RIGHT, 0, 0, 90, 20);
            bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.VISIBLE);

            //底部布局设置
            rv3LPBottom.removeRule(RIGHT_OF);
            rv3LPBottom.removeRule(LEFT_OF);
            rv3LPBottom.width = screenWidth;
            rv3LPBottom.height = screenHeight - rv2Lp.height;
            bindingBottom.tabBottom.setLayoutParams(rv3LPBottom);
//            showBottomButton(View.VISIBLE, View.GONE, isShowTbRow = 0);

            // 更新
            currentPreview = CV.NORMAL_PREVIEW;
            setCurrentRealView(currentPreview, currentView);

            //现在是正常的看法
            LEFT_DRAW_IS_BIG_PREVIEW = false;
            RV1_STATUS = CV.NORMAL;

            //现在是正常的看法
            RIGHT_DRAW_IS_BIG_PREVIEW = false;
            RV2_STATUS = CV.NORMAL;

        }
    }

    //电机区靠you展示
//    private void showRightZoom() {
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        lp.addRule(ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//        lp.addRule(CENTER_VERTICAL, RelativeLayout.TRUE);
//        lp.leftMargin = DensityUtil.dip2px(this, 10);
//        bindingMotorA.lyFabPlusMinusA.setLayoutParams(lp);
//        bindingMotorA.lyFabPlusMinusA.setVisibility(View.VISIBLE);
//    }

    //电机区靠左展示
    private void showLeftZoom(int leftMargin, int topMargin) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//        lp.addRule(CENTER_VERTICAL, RelativeLayout.TRUE);
        lp.leftMargin = DensityUtil.dip2px(this, leftMargin);
        lp.topMargin = DensityUtil.dip2px(this, topMargin);
        bindingMotorA.lyFabPlusMinusA.setLayoutParams(lp);
        bindingMotorA.lyFabPlusMinusA.setVisibility(View.VISIBLE);

        if (320 == topMargin) {
            bindingMotorB.lyFabPlusMinusB.setVisibility(GONE);
        } else {
            bindingMotorB.lyFabPlusMinusB.setVisibility(VISIBLE);
        }
    }

    private void startAudio(PlayFragment fragment) {
        // audio OPUS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //音频解码器
            mAudioDecoder = new AudioDecoder(fragment, getSystemService(AudioManager.class));
            //设置音量进度条
            bindingBottom.arcViewVolume.setProgress((AudioDecoder.VALUE_VOLUME));

            bindingBottom.topSeekProgress.setProgress1((AudioDecoder.VALUE_VOLUME));

            if (AudioDecoder.VALUE_VOLUME == 0) {
                bindingRight.gestureTx2spkIvPlayerVolume.setImageDrawable(pictureMute);
                bindingBottom.imageA.setImageDrawable(pictureMute);

            } else {
                bindingRight.gestureTx2spkIvPlayerVolume.setImageResource(R.drawable.iv_volume);
                bindingBottom.imageA.setImageResource(R.drawable.iv_volume);

            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {

                // 密码正确，然后进入校准状态
                case CV.PASSWORD_CORRECT:

                    //开始校准

                    bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.GONE);
                    bindingDirectionB.tbDirectionButtons.setVisibility(View.VISIBLE);

                    //判断哪一个，并显示相应的按钮，然后禁用放大按钮
                    if (msg.arg1 == CV.MSG_I_AM_LEFT_VIEW) {

                        bindingLeft.fabLeftDone.setVisibility(View.VISIBLE);
                        //左准星校准
                        LEFT_IS_CALIBRATION = true;
                        bindingLeft.btnLeftX.setVisibility(View.VISIBLE);
                        bindingLeft.btnLeftY.setVisibility(View.VISIBLE);
                        //左边十字
                        bindingLeft.ivOneA.setVisibility(View.VISIBLE);
                        bindingLeft.ivTwoA.setVisibility(View.VISIBLE);

                    } else if (msg.arg1 == CV.MSG_I_AM_RIGHT_VIEW) {

                        bindingRight.fabRightDone.setVisibility(View.VISIBLE);
                        //右准星校准
                        RIGHT_IS_CALIBRATION = true;
                        bindingRight.btnRightX.setVisibility(View.VISIBLE);
                        bindingRight.btnRightY.setVisibility(View.VISIBLE);
                        //右边十字
                        bindingRight.ivOneB.setVisibility(View.VISIBLE);
                        bindingRight.ivTwoB.setVisibility(View.VISIBLE);
                    }
                    break;
                case CV.LONG_PRESS:

                    isShow = false;
                    //长按
                    //打开安全认证 输入坐标开始校准弹窗
                    if (msg.arg1 == CV.MSG_I_AM_LEFT_VIEW) {
                        sightViewControlLeft.startCalibration(CV.MSG_I_AM_LEFT_VIEW);

                        //打开安全认证 输入坐标开始校准弹窗
                    } else if (msg.arg1 == CV.MSG_I_AM_RIGHT_VIEW) {
                        sightViewControlRight.startCalibration(CV.MSG_I_AM_RIGHT_VIEW);
                    }
                    break;
                case CV.MSG_UPDATE_DISTANCE_VALUE:

                    //距离
                    char[] ch = new char[10];
                    int[] dis = (int[]) msg.obj;

                    for (int i = 0; i < ch.length; i++) {
                        ch[i] = (char) (dis[i] & 0xFF);
                    }
                    if (ch[0] == 0) {
                        //DISTANCE 距离
                        DISTANCE_VALUE = 1;
                        bindingTop.tvDistanceBar.setText(Language.DISTANCE + "：" + "0.0m");
                        distance = "0.0";
                    } else {
                        distance = String.valueOf(ch).trim();

                        //距离
                        if (!TextUtils.isEmpty(distance)) {
                            DISTANCE_VALUE = 2;
                            if (distance.contains("m")) {
                                bindingTop.tvDistanceBar.setText(Language.DISTANCE + "：" + distance);
                            } else {
                                bindingTop.tvDistanceBar.setText(Language.DISTANCE + "：" + distance + " m");
                            }
                        }
                    }
                    break;
                case CV.MSG_UPDATE_NOISE_LEVEL:

                    //降噪等级
                    int data = msg.arg1;
                    String noiseLevel = Integer.toString(data);
                    if (msg.arg2 == CV.JZ1) {
                        bindingTop.tvJZ1.setText(noiseLevel + "L");
                    } else if (msg.arg2 == CV.JZ2) {
                        bindingTop.tvJZ2.setText(noiseLevel + "L");
                    }
                    break;
                case CV.MSG_BATTERY_VOLTAGE:

                    //电池状态判断
                    batteryStatusJudgment(msg);

                    break;
                case CV.SHOW_MOTOR_STEPS:
                    //步进电机步数
                    bindingBottom.motorSteps.setText(msg.obj.toString());

                    break;
                case CV.MSG_CHARGE_STATUS:
                    //充电状态
                    if (msg.arg1 == 1) {
                        //充电中
                        CHARGING = true;
                    } else if (msg.arg1 == 0) {
                        //未充电
                        CHARGING = false;
                    }
                    break;
                case CV.MSG_HUIBO_POWER:
                    /*
                     * 12位ADC参考电压2.5V
                     * 为便于划分等级，将其扩大100倍
                     * 光回波信号 左上角信号强度
                     */
                    huibo_power1 = (float) (((msg.arg1 * 2.5) / 4096.0) * 100);
                    bindingTop.signalStatus.setmProcess(huibo_power1);
                    break;
                case CV.MSG_M62429_1:
                    break;
                case CV.MSG_M62429_2:
                    break;
                case CV.MSG_AUTO_FOCUS_DONE:
                    break;
                case CV.AUTO_FOCUS_BUTTON_STATE:

                    //自动对焦按钮停止状态
                    setButtonState(true);

                    break;
                case CV.MSG_CURRENT_JZ://左右电机 位置 展示降噪几
                    break;
                case CV.MSG_CURRENT_MOTOR:
                    if (MANUAL_FOCUS_OR_NOISE_SET_MODE != CV.PLUS_MINUS_NOISE_SET) {
                        if (msg.arg1 == CV.MSG_CURRENT_RIGHT_MOTOR) {
                            //右电机
                            bindingMotorB.tvHintB.setText(Language.RIGHT_MOTOR);
                            bindingMotorA.tvHintA.setText(Language.RIGHT_MOTOR);
                        } else if (msg.arg1 == CV.MSG_CURRENT_LEFT_MOTOR) {
                            //左电机
                            bindingMotorB.tvHintB.setText(Language.LEFT_MOTOR);
                            bindingMotorA.tvHintA.setText(Language.LEFT_MOTOR);
                        }
                    }
                    break;
                case CV.MSG_STOP_TWO_CH_AUTO_FOCUS:
                    //停止两个通道自动对焦 方向键中间那个按钮 自动对焦完成后 回传
                    //自动调校完成
                    ToastUtils.showShort(Language.AUTOMATIC_ADJUSTMENT_COMPLETED);
                    break;
                case CV.MSG_SET_CAM1_CENTER:

                    Log.e(TAG, "准星坐标 发送: 小视野大预览   " + sp.getFloat("coordinateX1", 360f) + "," + sp.getFloat("coordinateY1", 240f));

                    mJsonHandle_6802.sendCmd(CV.SET_CAMERA1_CNETER, sp.getFloat("coordinateX1", 360f) + "," + sp.getFloat("coordinateY1", 240f));

                    break;
                case CV.MSG_SET_CAM2_CENTER:

                    Log.e(TAG, "准星坐标 发送: 辅助视图正常预览   " + sp.getFloat("coordinateX2", 360f) + "," + sp.getFloat("coordinateY2", 240f));

                    mJsonHandle_6802.sendCmd(CV.SET_CAMERA2_CNETER, sp.getFloat("coordinateX2", 360f) + "," + sp.getFloat("coordinateY2", 240f));

                    break;
                case CV.MSG_SET_CAM3_CENTER:

                    Log.e(TAG, "准星坐标 发送: 大视野视图大预览   " + sp.getFloat("coordinateX0", 360f) + "," + sp.getFloat("coordinateY0", 240f));

                    mJsonHandle_6802.sendCmd(CV.SET_CAMERA3_CNETER, sp.getFloat("coordinateX0", 360f) + "," + sp.getFloat("coordinateY0", 240f));

                    break;
                case CV.MSG_ADJUST_AUTO_FOCUS:

                    //开始校准电机编码值
                    ToastUtils.showShort(Language.CALIBRATION_MOTOR_CODE_VALUE);
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.ADJUST_AUTO_FOCUS_START);
                    bindingTop.tvLeftEncoder.setVisibility(View.VISIBLE);
                    bindingTop.tvRightEncoder.setVisibility(View.VISIBLE);
                    break;
                case CV.MSG_ADJUST_AUTO_FOCUS_UPDATE:

                    //调整自动对焦开始
                    bindingTop.tvLeftEncoder.setText(LEFT_VALUE + " ：" + msg.arg2);
                    bindingTop.tvRightEncoder.setText(RIGHT_VALUE + " ：" + msg.arg1);
                    break;
                case CV.MSG_OPERATE_PROTECT_STEPPER:

                    //弹窗
                    OPSDialog();
                    break;

                case CV.MSG_DISPLAY_MOTOR_STEPS:

                    //5579 显示获取步进电机步数按钮 密码正确
                    bindingBottom.motorSteps.setVisibility(View.VISIBLE);

                    break;
                case CV.MSG_SWITCH_LANGUAGE:

                    //9999
                    //打开开关按钮设置页面
                    Intent intent = new Intent(mContext, PowerSwitchButtonActivity.class);
                    startActivityForResult(intent, CV.MSG_MAIN_RESULT_CODE);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (CV.MSG_MAIN_RESULT_CODE == requestCode && CV.MSG_MAIN_RESULT_CODE == resultCode) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    systemReset();
                }
            }, 1000);
        }
    }

    private boolean k_or_m;

    //电池状态判断 12位adc参考电压是2.5V 电池电压最高17V  电池电压的分压电阻是51K 和 10K
    private void batteryStatusJudgment(Message msg) {

        voltage = (float) (((msg.arg1 * 2.5) / 4096.0) * 6.1);

        if (CHARGING) { //是否充电中
            if (voltage <= 13.0) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_alert));
            } else if (voltage > 13.0 && voltage <= 13.5) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_20));
            } else if (voltage > 13.5 && voltage <= 14.0) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_50));
            } else if (voltage > 14.0 && voltage <= 14.8) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_80));
            } else if (voltage > 14.8) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_charging_full));
            }
        } else {
            if (voltage <= 13.0) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_alert));
            } else if (voltage > 13.0 && voltage <= 13.5) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_20));
            } else if (voltage > 13.5 && voltage <= 14.0) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_50));
            } else if (voltage > 14.0 && voltage <= 14.8) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_80));
            } else if (voltage > 14.8) {
                bindingTop.ivBattery.setImageDrawable(getDrawable(R.drawable.ic_battery_full));
            }
        }
    }

    //步进电机弹窗 长按点击
    private void OPSDialog() {
        Dialog OperateProtectStepperDialog = new Dialog(MainActivity.this);
        OperateProtectStepperDialog.getWindow().setGravity(Gravity.LEFT);
        OperateProtectStepperDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        OperateProtectStepperDialog.setCancelable(true);
        OperateProtectStepperDialog.setContentView(R.layout.operate_protect_stepper);

        //步进电机 全局搜 Protect Stepper
        ImageView fabProtectStepperUp = OperateProtectStepperDialog.findViewById(R.id.fabProtectStepperUp);
        ImageView fabProtectStepperDown = OperateProtectStepperDialog.findViewById(R.id.fabProtectStepperDown);

        fabProtectStepperUp.setOnClickListener(MainActivity.this);
        fabProtectStepperUp.setOnLongClickListener(longClickListener);

        fabProtectStepperDown.setOnClickListener(MainActivity.this);
        fabProtectStepperDown.setOnLongClickListener(longClickListener);

        OperateProtectStepperDialog.show();
    }

    //录像视频名称
    private String recordname;

    //开始录像
    public void codecToggle(boolean isClose) {

        // 从RTSP记录 record from RTSP
        if (fragment1 != null) {
            recordname = FileUtil.getMovieName().getPath();

            //记录或者停止录像 由fragment 调用MainActivity onRecordState方法
            fragment1.onRecordOrStop(recordname, isClose);
        }
    }

    //fragment通知音频解码器去录音
    public void onRecordState(boolean status) {
        isRecording = !isRecording;
        if (status) {

            //开始录音
            mAudioDecoder.startRecord(recordname.replace(".mp4", ".pcm").replace("movie", "pcm"));
            //开始计时
            updateRecordTime(true);
            //更改录像按钮
            bindingBottom.btnStartRecord.setCompoundDrawables(null, ic_stop_record_selector, null, null);
            bindingBottom.btnStartRecord.setTextColor(logo_red);
//            bindingBottom.btnTabRow2StartRecord.setCompoundDrawables(null, ic_stop_record_selector, null, null);
//            bindingBottom.btnTabRow2StartRecord.setTextColor(logo_red);

        } else {

            //停止计时
            updateRecordTime(false);
            //停止录音
            mAudioDecoder.stopRecord();

            //开始录像
//            bindingBottom.btnTabRow2StartRecord.setCompoundDrawables(null, ic_record_selector, null, null);
//            bindingBottom.btnTabRow2StartRecord.setText(START_RECORDING);
//            bindingBottom.btnTabRow2StartRecord.setTextColor(white);

            //开始录像
            bindingBottom.btnStartRecord.setCompoundDrawables(null, ic_record_selector, null, null);
            bindingBottom.btnStartRecord.setText(Language.RECORDING);
            bindingBottom.btnStartRecord.setTextColor(white);
        }
    }

    //录像计时用 ---------start------------
    private Handler updateTimeHandler = new Handler();
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    //计时并展示
                    bindingBottom.btnStartRecord.setText(
                            String.format(Locale.CHINA, "%02d", mRecordedSecond / 3600)
                                    + ":" + String.format(Locale.CHINA, "%02d", mRecordedSecond % 3600 / 60)
                                    + ":" + String.format(Locale.CHINA, "%02d", mRecordedSecond % 60));
//                    bindingBottom.btnTabRow2StartRecord.setText(hourString + ":" + minString + ":" + secString);
                }
            });

            mRecordedSecond++;
            updateTimeHandler.postDelayed(updateTimeRunnable, 1000);
        }
    };

    private volatile int mRecordedSecond = 0;

    //更新记录时间
    private void updateRecordTime(boolean isRecording) {

        if (isRecording) {
            //启动计时并展示
            updateTimeHandler.post(updateTimeRunnable);
        } else {
            //还原

            for (int i = 0; i < biaoji_timer.size(); i++) {
                VideoTag.add(recordname, i, biaoji_timer.get(i) * 1000, mRecordedSecond * 1000);
            }
            mRecordedSecond = 0;
            updateTimeHandler.removeCallbacks(updateTimeRunnable);
        }
    }

    private void resetPlusMinusMode() {
        MANUAL_FOCUS_OR_NOISE_SET_MODE = CV.NONE;

        //手动对焦标志
        manual_focus_flag = true;

//        bindingBottom.btnTabRow3ManualFocus.setCompoundDrawables(null, ic_manual_focus_selector, null, null);
//        //手动对焦
//        bindingBottom.btnTabRow3ManualFocus.setText(MANUAL_FOCUS);
//        bindingBottom.btnTabRow3ManualFocus.setEnabled(true);
    }

    private boolean isConsole = false;//云台or调整架 （false调整架）

    // 发送指令 电机调节
    // 电机左右电机会切换这个值 leftOrRight 默认false(左电机)
    private void adjustMotor(boolean PLUS_OR_MINUS) {

        energyCancel();

        if (left_right) {
            //右电机
            if (PLUS_OR_MINUS) {//增大
                //右电机手册加号
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.RIGHT_MOTOR_MANUAL_FOCUS_PLUS);
//                Log.e(TAG, "plusClickEvent: ------------右电机手册加号=====");
            } else {//减小
                //右电机手册减号
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.RIGHT_MOTOR_MANUAL_FOCUS_MINUS);
//                Log.e(TAG, "plusClickEvent: ------------右电机手册减号=====");
            }

        } else {

            if (leftOrRight) {//右电机
                if (PLUS_OR_MINUS) {//增大
                    //右电机手册加号
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.RIGHT_MOTOR_MANUAL_FOCUS_PLUS);
//                    Log.e(TAG, "plusClickEvent: ------------右电机手册加号=====");
                } else {//减小
                    //右电机手册减号
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.RIGHT_MOTOR_MANUAL_FOCUS_MINUS);
//                    Log.e(TAG, "plusClickEvent: ------------右电机手册减号=====");
                }

            } else {//左电机

                if (PLUS_OR_MINUS) {//增大
                    //左马达手册加号
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.LEFT_MOTOR_MANUAL_FOCUS_PLUS);
//                    Log.e(TAG, "plusClickEvent: -----111-------左马达手册加号=====");
                } else {//减小
                    //左马达手册减号
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.LEFT_MOTOR_MANUAL_FOCUS_MINUS);
//                    Log.e(TAG, "plusClickEvent: -----111-------左马达手册减号=====");
                }
            }
        }
    }

    private DrawView leftDrawView;
    private DrawView rightDrawView;
    private DrawView hideDrawView;//-------------
    private boolean photographWhich = false;

    //初始化准星
    private void crosshairInit() {

        //left准星
        RelativeLayout leftLayout = findViewById(R.id.rv1);
        leftDrawView = new DrawView(this, 0);
        leftLayout.addView(leftDrawView); //添加准星控制View

        sightViewControlLeft = new SightViewControl(MainActivity.this, mHandler, leftDrawView);
        sightViewControlLeft.setScale(leftDrawRectangleScale);
        sightViewControlLeft.refreashCrosshairLoaction(currentRealView);

        //right准星
        RelativeLayout rightLayout = findViewById(R.id.rv2);
        rightDrawView = new DrawView(this, 0);
        rightLayout.addView(rightDrawView);//添加准星控制View

        sightViewControlRight = new SightViewControl(MainActivity.this, mHandler, rightDrawView);
        sightViewControlRight.setScale(rightDrawRectangleScale);
        sightViewControlRight.refreashCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);

        //测试准星
        hideDrawView = new DrawView(this, 1);
        rightLayout.addView(hideDrawView);//添加准星控制View

        RRRRRRR = new SightViewControl(MainActivity.this, mHandler, hideDrawView);
        RRRRRRR.setScale(rightDrawRectangleScale);
        RRRRRRR.refreashCrosshairLoaction(CV.AUXILIARY_RRRR);
        RRRRRRR.setDrawView_GONE();
    }

    // manual focus mode or noise set mode
    private byte MANUAL_FOCUS_OR_NOISE_SET_MODE = CV.NONE;

    private int clickRecord1 = 0;//两个值记录谁是最后一次点击
    private int clickRecord2 = 0;
    private int clicksNumber = 0;

    @SuppressLint({"NonConstantResourceId", "MissingPermission"})
    @Override
    public void onClick(final View view) {

        //获取一个随机数 重新计算三分钟自动息屏
        getRandomNumber("点击事件");

        if (vibratorBoolean) {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(vibratorTime);
//                vibrator.cancel();
                }
            }
        }

        switch (view.getId()) {
            case R.id.btnReset:
//            case R.id.btnTabRow4Reset:

                if (!isRecording) {
                    //系统复位
                    systemReset();
                } else {

                    //请先关闭录像
//                    ToastUtils.showShort(TURN_OFF_RECORDING);
                    Toast.makeText(this, Language.TURN_OFF_VIDEO, Toast.LENGTH_SHORT).show();
//                    codecToggle();
                }

                break;
            case R.id.btnPhotograph:

                if (photographWhich) {
                    //小视场拍照
                    if (fragment2 != null) {
                        fragment2.takePicture(FileUtil.getPictureName().getPath());
                    }
                } else {
                    //大市场拍照
                    if (fragment != null) {
                        fragment.takePicture(FileUtil.getPictureName().getPath());
                    }
                }

                break;
            case R.id.btnDemodeSelect:
                //正常模式 晃动模式 切换
                if (getIntervalTime()) {
                    return;
                }

                switchMode();

                break;
            case R.id.btnStartRecord:
//            case R.id.btnTabRow2StartRecord:

                //开始录像
                codecToggle(true);

                break;
//            case R.id.btnTabRow3SetLocation:
//                //设置位置
//
//                longPressSetLocation = !longPressSetLocation;
//                if (longPressSetLocation) {
//                    bindingBottom.btnTabRow3SetLocation.setCompoundDrawables(null, locationDrawable, null, null);
//                    bindingBottom.btnTabRow3SetLocation.setTextColor(ContextCompat.getColor(mContext, R.color.signal_status_view_bar_warning));
//                    //第一次点击按钮 显示 保存位置
//                    bindingBottom.btnTabRow3SetLocation.setText(EXIT_SETTINGS);
////                    点击屏幕 闪动五次 功能
////                    bindingLeft.rv0Set.setVisibility(VISIBLE);
////                    bindingLeft.rv0BG.setVisibility(VISIBLE);
//
//                    bindingLeft.rv1Set.setVisibility(VISIBLE);
//                    bindingLeft.rv1BG.setVisibility(VISIBLE);
//                    bindingLeft.mydragrectangleview.setVisibility(VISIBLE);
//
//                } else {
////                    bindingBottom.btnTabRow3SetLocation.setCompoundDrawables(null, locationDrawable1, null, null);
////                    bindingBottom.btnTabRow3SetLocation.setTextColor(ContextCompat.getColor(mContext, R.color.white));
//                    //第二次点击按钮 还原默认显示 设置位置
//                    bindingBottom.btnTabRow3SetLocation.setText(SET_LOCATION);
////                    点击屏幕 闪动五次 功能
////                    bindingLeft.rv0Set.setVisibility(GONE);
////                    bindingLeft.rv0BG.setVisibility(GONE);
//
//                    bindingLeft.rv1Set.setVisibility(GONE);
//                    bindingLeft.rv1BG.setVisibility(GONE);
//
//                    bindingLeft.mydragrectangleview.setVisibility(GONE);
//
//                    //获取设置位置 返回的坐标
//                    mainGetCoordinate();
//
//                    /**true通知控件直接重绘 顺序不可变*/
//                    bindingLeft.mydragrectangleview.myInvalidate(true);
//                }
////                bindingLeft.rv0Set.setAllowSetting(longPressSetLocation);
//
//                break;
//            case R.id.voicePlus:
//                //增强音量 现在控制的是设备端音量大小
//                //voicePlus(true);
//
//                //先控制音量 在展示
//                if (AudioDecoder.VALUE_VOLUME < 10) {
//                    AudioDecoder.VALUE_VOLUME++;
//                    if (mAudioDecoder != null) {
//                        mAudioDecoder.controlVolume();
//                    }
//                    Log.e(TAG, "onClick: ========本地==========" + AudioDecoder.VALUE_VOLUME);
//                } else if (AudioDecoder.VALUE_VOLUME < 15) {
//                    AudioDecoder.VALUE_VOLUME++;
//                    //设置音量 48台
//                    mUartHandle_6804.setVolume(450 - (15 - AudioDecoder.VALUE_VOLUME) * 50);
//                    Log.e(TAG, "onClick: ====设备==============" + (450 - (15 - AudioDecoder.VALUE_VOLUME) * 50));
//                } else {
//                    mUartHandle_6804.setVolume(450);
//                }
//                //喇叭显示和隐藏
//                speakerDisplay();
//
//                break;
//            case R.id.voiceMinus:
//                //音量减小 现在控制的是设备端音量大小
//                // voiceMinus(true);
//
//                //先控制音量 在展示
//                if (AudioDecoder.VALUE_VOLUME > 10) {
//                    AudioDecoder.VALUE_VOLUME--;
//                    //设置音量 48台
//                    mUartHandle_6804.setVolume((450 - (15 - AudioDecoder.VALUE_VOLUME) * 50));
//                    Log.e(TAG, "onClick: ====设备==============" + (450 - (15 - AudioDecoder.VALUE_VOLUME) * 50));
//                } else if (AudioDecoder.VALUE_VOLUME >= 1) {
//                    AudioDecoder.VALUE_VOLUME--;
//                    if (mAudioDecoder != null) {
//                        mAudioDecoder.controlVolume();
//                    }
//                    Log.e(TAG, "onClick: ========本地==========" + AudioDecoder.VALUE_VOLUME);
//                }
//                //喇叭显示和隐藏
//                speakerDisplay();
//
//                break;
            case R.id.btnArithmeticSelect:
//            case R.id.btnTabRow3NoiseSet:

                //getView
                //降噪弹窗 降噪方法 弹窗 及处理 点击事件
                dialogNoiseUtils.methodSelect();

                break;
            case R.id.btnAmplifierControl:

                //NONE 按钮点击事件 放大器功率切换
                switchingPower();

                break;
            case R.id.btnAutoFocus:
//            case R.id.btnTabRow2AutoFocus:

                //自动对焦 每隔五秒发送一次指令
                autoFocus();

                break;
//            case R.id.btnTabRow3ManualFocus:
//
//                //9.12小屏幕 手动对焦的点击事件 弹出 电机加减按钮
//
//                if (!notClick) {
//                    //长按事件不响应点击事件控制
//                    manualFocus(bindingBottom.btnTabRow3ManualFocus);
//                } else {
//                    notClick = false;
//                }
//
//                break;
            case R.id.btnQuickUpA:

                //点击向上
                upClick();

                break;
            case R.id.btnQuickDownA:

                //点击向下
                downClick();

                break;
            case R.id.btnQuickLeftA:

                //点击向左
                leftClick();

                break;
            case R.id.btnQuickRightA:

                //点击向右
                rightClick();

                break;
            case R.id.fabBtnUpB:
                //"点击", "向上"
                centerY--;
                fineTuneSight();

                break;
            case R.id.fabBtnDownB:
                //"点击", "向下"
                centerY++;
                fineTuneSight();

                break;
            case R.id.fabBtnLeftB:
                //"点击", "向左"
                centerX--;
                fineTuneSight();

                break;
            case R.id.fabBtnRightB:
                //"点击", "向右"
                centerX++;
                fineTuneSight();

                break;
            case R.id.fabLeftDone:

                //左 确定保存准星坐标
                okToSaveLeft();

                break;
            case R.id.fabRightDone:

                //右 确定保存准星坐标
                okToSaveRight();

                break;

            case R.id.btnHide:

                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    clicksNumber = 1;
                } else {
                    clicksNumber++;
                }

                if (isConsole) {

                    if (btnHideIsLong) {
                        //切换快慢 开始为快 点击后为慢
                        k_or_m = !k_or_m;

                        //改变云台字体 快慢显示
                        changePtzText(Language.BALL_HEAD);
                    }

                    btnHideIsLong = true;
                }

                exitTime = System.currentTimeMillis();

                if (clicksNumber == 20) {

                    //调整准星位置
                    adjustTheFrontSight();
                    exitTime = 0;
                    clicksNumber = 0;
                }

                break;
            case R.id.btnPlayback:
//            case R.id.btnTabRow4Playback:

                //跳转到录像回放
                if (!isRecording && !LEFT_IS_CALIBRATION && !RIGHT_IS_CALIBRATION) {
                    Intent launchIntentPreview = new Intent();
                    launchIntentPreview.setClass(MainActivity.this, VideoPlaybackActivity.class);
                    startActivitySafety(MainActivity.this, launchIntentPreview);
                    VideoPlaybackActivity.isPlayback = true;
                } else {
                    //请先关闭录像或自动对焦或自动调焦
//                    ToastUtils.showShort(PROMPT_TURN_OFF_RECORDING);
                    Toast.makeText(this, Language.TURN_OFF_VIDEO, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnOnlineSelect:

                //有线模式切换

                if (inspectionNET != null) {
                    inspectionNET.startSelfTest();
                }

//                mAdbUtil.sendEthernetIp();

//                mHandler.postDelayed(new Runnable() {
//                    public void run() {
//                        if (mUartHandle_6804 != null) {
//                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.RESET);
//                        }
//                    }
//                }, 2000);

                break;
            case R.id.shengdian:

                //省电模式
                setWindowBrightness(this, 0);
                bindingLeft.rv1.setVisibility(View.GONE);
                bindingRight.rv2.setVisibility(View.GONE);
                bindingMain.tvKsdj.setVisibility(View.VISIBLE);
                isShow = false;

                break;

//            case R.id.tvSound:
//
//                respond2Clicks = true;
//
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        respond2Clicks = false;
//
//                    }
//                }, 2500);
//
//                break;
            case R.id.tvHideHigh:
                //发送一条 high

                if (!respond2Clicks) {
                    return;
                }

                if ((System.currentTimeMillis() - exitTime) > 500) {
                    clicksNumber = 1;
                } else {
                    clicksNumber++;
                }

                exitTime = System.currentTimeMillis();

                if (clicksNumber == 3) {

                    ToastUtils.showShort("high");

//                  放大器功率切换
//                    mJsonHandle_6802.sendCmd(CV.DEMODE_NONE, "high");
                    mUartHandle_6804.switchAmpPwrHigh();

                    respond2Clicks = false;
                    exitTime = 0;
                    clicksNumber = 0;
                }

                break;

            case R.id.motorSteps:
                //返回电机步数
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.SHOW_MOTOR_STEPS);

                break;
            case R.id.tvHintA:
                //AAA 电机区 电机点击事件
            case R.id.tvHintB:
                //BBB 电机区 电机点击事件
                if (RV2_STATUS == CV.NORMAL) {

                    leftOrRight = !leftOrRight;
                    clickMotor();
                }

                break;
            case R.id.fabBtnPlusA://AAA

                left_right = false;
                // 电机区 + 号 点击事件
                adjustMotor(true);

                break;
            case R.id.fabBtnPlusB://BBB

                left_right = true;
                // 电机区 + 号 点击事件
                adjustMotor(true);

                break;
            case R.id.fabBtnMinusA://AAA

                left_right = false;
                // 电机区 - 号 点击事件
                adjustMotor(false);

                break;
            case R.id.fabBtnMinusB://BBB

                left_right = true;
                // 电机区 - 号 点击事件
                adjustMotor(false);

                break;
            case R.id.fabProtectStepperUp:

                longClickHandler.removeCallbacksAndMessages(null);
                mUartHandle_6804.protectStepperSendCmd(100);

                break;
            case R.id.fabProtectStepperDown:

                longClickHandler.removeCallbacksAndMessages(null);

                mUartHandle_6804.protectStepperSendCmd(-100);

                break;
            case R.id.btnInspection:
                //自检
                inspection.startSelfTest();

                break;

            case R.id.tvBiaoji:
                //添加录像标记  隐藏

                if (mRecordedSecond != 0) {

                    markNumber++;
                    bindingMain.tvBiaoji.setText("标记 :" + markNumber);
                    biaoji_timer.add(mRecordedSecond * 1000);
                }
                break;
            case R.id.tvCruise:

                //弹出左侧巡航点选择框
                isCloseDrawerLayout = !isCloseDrawerLayout;
                bindingMain.myDrawerLayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.btnSet1:

                //设置巡航点 1
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.CRUISING_POINT_SET_S1);
                ToastUtils.showShort(CV.CRUISING_POINT_SET_S1);
                break;
            case R.id.btnSet2:

                //设置巡航点 2
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.CRUISING_POINT_SET_S2);
                ToastUtils.showShort(CV.CRUISING_POINT_SET_S2);
                break;
            case R.id.btnSet3:

                //设置巡航点 3
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.CRUISING_POINT_SET_S3);
                ToastUtils.showShort(CV.CRUISING_POINT_SET_S3);
                break;
            case R.id.btnGet1:

                //返回巡航点 1
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.CRUISING_POINT_RECALL_S1);
                ToastUtils.showShort(CV.CRUISING_POINT_RECALL_S1);
                break;
            case R.id.btnGet2:

                //返回巡航点 2
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.CRUISING_POINT_RECALL_S2);
                ToastUtils.showShort(CV.CRUISING_POINT_RECALL_S2);
                break;
            case R.id.btnGet3:

                //返回巡航点 3
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.CRUISING_POINT_RECALL_S3);
                ToastUtils.showShort(CV.CRUISING_POINT_RECALL_S3);
                break;
            default:
                break;
        }
    }

    //获取设置位置 返回的坐标
    private void mainGetCoordinate() {
        int[] coordinate = bindingLeft.mydragrectangleview.getCoordinate();

        int number = 0;
        for (int i = 0; i < coordinate.length; i++) {
            if (coordinate[i] == 0) {
                number++;
            }
        }

        if (number == 4) {
            //坐标为空 不做处理
        } else {
            Log.e(TAG, "选中区域坐标点  X轴开始 X轴结束   Y轴开始 Y轴结束 == " + Arrays.toString(coordinate));
            ToastUtils.showLong("选中区域坐标点  X轴开始 X轴结束   Y轴开始 Y轴结束 == " + Arrays.toString(coordinate));
        }
    }

    public static boolean isCloseDrawerLayout;
    //记录第几次标记
    private int markNumber = 0;
    private ArrayList<Integer> biaoji_timer = new ArrayList();

    //拖动准星时微调准星位置
    private void fineTuneSight() {
        if (centerX < rv1Lp.width && centerY < rv1Lp.height && LEFT_IS_CALIBRATION) {
            sightViewControlLeft.setCenterX(centerX);
            sightViewControlLeft.setCenterY(centerY);

            //拖动设置位置
            leftDrawView.drawRecForCalibration(centerX, centerY, leftDrawRectangleScale);
            leftDrawView.invalidate();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bindingLeft.btnLeftX.setText(Float.toString(centerX));
                    bindingLeft.btnLeftY.setText(Float.toString(centerY));
                }
            });

        } else if (centerX > rv1Lp.width && centerY < rv2Lp.height && RIGHT_IS_CALIBRATION) {
            sightViewControlRight.setCenterX(centerX - rv1Lp.width);
            sightViewControlRight.setCenterY(centerY);

            //拖动设置位置
            rightDrawView.drawRecForCalibration(centerX - rv1Lp.width, centerY, rightDrawRectangleScale);
            rightDrawView.invalidate();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bindingRight.btnRightX.setText(Float.toString(centerX - rv1Lp.width));
                    bindingRight.btnRightY.setText(Float.toString(centerY));
                }
            });

        } else if (centerX > rv1Lp.width && centerY < rv2Lp.height) {
            //测试用 右大屏时 可滑动新添加的准星框
        } else {
//            Log.e(TAG, "onClick: 555555555555 ==  " + centerX);
        }
    }

    //区分点击还是长按
    private boolean is_UP_SingleClick = true;

    //点击 向上(长按松开时也会走该方法)
    private void upClick() {

        bindingDirectionA.btnQuickUpA.setEnabled(false);

        if (isConsole) { // todo 云台

            if (is_UP_SingleClick) {
                //向上 单次
                if (k_or_m) {
//                    Log.e(TAG, "upClick:--- 云台 点击 向上 慢 单次");
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_UP);
                } else {
//                    Log.e(TAG, "upClick:--- 云台 点击 向上 快 点击并停止 ");
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_UP);
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
                }

            } else {
                //向上 长按停止
//                Log.e(TAG, "upClick:--- 云台 向上 长按停止 ");
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
            }

        } else { // todo 调整架

            if (is_UP_SingleClick) {
                //向上
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_FORWARD);
                //停止
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_STOP);
            }
        }

        //修改变量 默认是点击事件
        is_UP_SingleClick = true;
        bindingDirectionA.btnQuickUpA.setEnabled(true);
    }

    //区分点击还是长按
    private boolean is_DOWN_SingleClick = true;

    //点击 向下(长按松开时也会走该方法)
    private void downClick() {

        bindingDirectionA.btnQuickDownA.setEnabled(false);

        if (isConsole) { // todo 云台

            if (is_DOWN_SingleClick) {
                //单次点击 向下

                if (k_or_m) {
//                    Log.e(TAG, "upClick:--- 云台 点击 向下 慢 单次");
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_DOWN);
                } else {
//                    Log.e(TAG, "upClick:--- 云台 点击 向下 快 点击并停止 ");
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_DOWN);
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
                }

            } else {
                //结束长按 向下
//                Log.e(TAG, "upClick:--- 云台 长按 向下 停止 ");
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
            }

        } else { // todo 调整架

            if (is_DOWN_SingleClick) {
                //向下
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_REVERSAL);
                //停止
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_STOP);
            }
        }

        //修改变量 默认是点击事件
        is_DOWN_SingleClick = true;

        bindingDirectionA.btnQuickDownA.setEnabled(true);
    }

    //区分点击还是长按
    private boolean is_LEFT_SingleClick = true;

    //点击 向左(长按松开时也会走该方法)
    private void leftClick() {

        bindingDirectionA.btnQuickLeftA.setEnabled(false);

        if (isConsole) { // todo 云台

            if (is_LEFT_SingleClick) {
                //向左单次点击

                if (k_or_m) {
//                    Log.e(TAG, "upClick:--- 云台 点击 向左 慢 单次开始 " + k_or_m);
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LEFT);
                } else {
//                    Log.e(TAG, "upClick:--- 云台 点击 向左 快 点击并停止 ");
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_LEFT);
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
                }

            } else {
                //向左长按停止
//                Log.e(TAG, "upClick:--- 云台 长按 向左 停止 ");
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
            }

        } else { // todo 调整架

            if (is_LEFT_SingleClick) {
                //向左
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_FORWARD);
                //停止
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_STOP);
            }
        }

        //修改变量 默认是点击事件
        is_LEFT_SingleClick = true;
        bindingDirectionA.btnQuickLeftA.setEnabled(true);
    }

    //区分点击还是长按
    private boolean is_RIGHT_SingleClick = true;

    //点击 向右(长按松开时也会走该方法)
    private void rightClick() {

        bindingDirectionA.btnQuickRightA.setEnabled(false);

        if (isConsole) {
            // todo 云台
            if (is_RIGHT_SingleClick) {
                //向右单次点击

                if (k_or_m) {
//                    Log.e(TAG, "upClick:--- 云台 点击 向右 慢 单次");
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_RIGHT);
                } else {
//                    Log.e(TAG, "upClick:--- 云台 点击 向右 快 点击并停止 ");
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_RIGHT);
                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
                }
            } else {
                //向右长按停止
//                Log.e(TAG, "upClick:--- 云台 长按 向右 停止 ");
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
            }

        } else {
            // todo 调整架
            if (is_RIGHT_SingleClick) {
                //向右
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_REVERSAL);
                //停止
                mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_STOP);
            }
        }

        //修改变量 默认是点击事件
        is_RIGHT_SingleClick = true;
        bindingDirectionA.btnQuickRightA.setEnabled(true);
    }

    //自动对焦 每隔五秒发送一次指令
    private void autoFocus() {

        if (getIntervalTime()) {
            return;
        }

        energyCancel();

        if (!isResponsePress && !btnState) {
            //todo 按钮正常状态 非长按触发

            //此处判断是因为当长按方向键时 按自动对焦按钮再次松开方向键时 设备那边做了控制不接受指令
            if (is_UP_SingleClick && is_DOWN_SingleClick && is_LEFT_SingleClick && is_RIGHT_SingleClick) {

                if (!UartHandle6804.socketIsBroken) {

                    setButtonState(false);

                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.AUTO_FOCUS);

                }

            } else {
                if (isConsole) {
                    //云台
//                    ToastUtils.showLong("请等待其他操作停止以后再点击");
                } else {
                    //调整架
//                    ToastUtils.showLong("请等待其他操作停止以后再点击");
                }

            }
        }
        isResponsePress = false;
    }

    //自动对焦按钮停止状态
    public void setButtonState(boolean bState) {

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                if (bState) {
//
//                    //可以点击
//                    bindingBottom.btnAutoFocus.setBackground(getResources().getDrawable(R.drawable.anniu_bg_button1));
//
//                    bindingDirectionA.btnQuickLeftA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang1));
//                    bindingDirectionA.btnQuickUpA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang1));
//                    bindingDirectionA.btnQuickRightA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang1));
//                    bindingDirectionA.btnQuickDownA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang1));
//
//                    bindingMotorA.fabBtnMinusA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang));
//                    bindingMotorA.fabBtnPlusA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang));
//
//                    bindingMotorB.fabBtnMinusB.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang));
//                    bindingMotorB.fabBtnPlusB.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang));
//
//                } else {
//                    //不可点击
//                    bindingBottom.btnAutoFocus.setBackground(getResources().getDrawable(R.drawable.anniu_bg_button4));
//                    bindingDirectionA.btnQuickLeftA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//                    bindingDirectionA.btnQuickUpA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//                    bindingDirectionA.btnQuickRightA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//                    bindingDirectionA.btnQuickDownA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//
//                    bindingMotorA.fabBtnMinusA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//                    bindingMotorA.fabBtnPlusA.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//
//                    bindingMotorB.fabBtnMinusB.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//                    bindingMotorB.fabBtnPlusB.setBackground(getResources().getDrawable(R.drawable.bg_fangxiang3));
//                }
//
//
//                bindingDirectionA.btnQuickLeftA.setEnabled(bState);
//                bindingDirectionA.btnQuickUpA.setEnabled(bState);
//                bindingDirectionA.btnQuickRightA.setEnabled(bState);
//                bindingDirectionA.btnQuickDownA.setEnabled(bState);
//
//                bindingMotorA.fabBtnMinusA.setEnabled(bState);
//                bindingMotorA.fabBtnPlusA.setEnabled(bState);
//
//                bindingMotorB.fabBtnMinusB.setEnabled(bState);
//                bindingMotorB.fabBtnPlusB.setEnabled(bState);
//
//                bindingBottom.btnAutoFocus.setEnabled(bState);
//            }
//        });
    }

    //右 确定保存准星坐标
    private void okToSaveRight() {

        bindingRight.fabRightDone.setVisibility(View.GONE);
        bindingDirectionB.tbDirectionButtons.setVisibility(View.GONE);
        bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.VISIBLE);

        if (!"X".equals(bindingRight.btnRightX.getText()) && !"Y".equals(bindingRight.btnRightY)) {
            //不做判断重新打开app 1185 bu拖动准星就保存准星会回到初始位置
            // 保存坐标值
            sightViewControlRight.saveCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);
            sightViewControlRight.refreashCrosshairLoaction(CV.AUXILIARY_VIEW_NORMAL_PREVIEW);
        }

        bindingRight.btnRightX.setVisibility(View.GONE);
        bindingRight.btnRightY.setVisibility(View.GONE);

        RIGHT_IS_CALIBRATION = false;

        //右边十字
        bindingRight.ivOneB.setVisibility(View.GONE);
        bindingRight.ivTwoB.setVisibility(View.GONE);
    }

    //左 确定保存准星坐标
    private void okToSaveLeft() {
        /*隐藏浮动按钮*/
        bindingLeft.fabLeftDone.setVisibility(View.GONE);
        bindingDirectionB.tbDirectionButtons.setVisibility(View.GONE);
        if (VISIBLE == bindingDirectionA.btnHide.getVisibility()) {
            bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.VISIBLE);
        } else {
            /*隐藏方向按钮*/
            bindingDirectionA.tbQuickDirectionButtons.setVisibility(View.GONE);
        }

        if (!"X".equals(bindingLeft.btnLeftX.getText()) && !"Y".equals(bindingLeft.btnLeftY)) {
            //不做判断重新打开app 1185 bu拖动准星就保存准星会回到初始位置
            // 保存坐标值
            sightViewControlLeft.saveCrosshairLoaction(currentRealView);
            sightViewControlLeft.refreashCrosshairLoaction(currentRealView);
        }
        bindingLeft.btnLeftX.setVisibility(View.GONE);
        bindingLeft.btnLeftY.setVisibility(View.GONE);

        LEFT_IS_CALIBRATION = false;

        //左边十字
        bindingLeft.ivOneA.setVisibility(View.GONE);
        bindingLeft.ivTwoA.setVisibility(View.GONE);
    }

    //喇叭显示和隐藏
    private void speakerDisplay() {

        if (AudioDecoder.VALUE_VOLUME == 0) {
            bindingRight.gestureTx2spkIvPlayerVolume.setImageDrawable(pictureMute);
            bindingBottom.imageA.setImageDrawable(pictureMute);

        } else {
            bindingRight.gestureTx2spkIvPlayerVolume.setImageResource(R.drawable.iv_volume);
            bindingBottom.imageA.setImageResource(R.drawable.iv_volume);

        }

        bindingRight.gestureTx2spkVolumeLayout.setVisibility(View.VISIBLE);
        bindingRight.gestureTx2spkTvVolumePercentage.setText(AudioDecoder.VALUE_VOLUME * 10 + "%");
        bindingBottom.arcViewVolume.setProgress((AudioDecoder.VALUE_VOLUME));
        bindingBottom.topSeekProgress.setProgress1((AudioDecoder.VALUE_VOLUME));

        //隐藏喇叭
        speakerHidden();
    }

    //隐藏喇叭
    private void speakerHidden() {
        clickRecord1++;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                clickRecord2++;
                if (clickRecord2 == clickRecord1) {
                    bindingRight.gestureTx2spkVolumeLayout.setVisibility(View.GONE);

                    clickRecord1 = 0;
                    clickRecord2 = 0;
                }
            }
        }, 500);
    }

    private boolean leftOrRight = false;//默认为false
    private boolean left_right = false;//默认为false左边  区分大屏幕展示时应该响应左还是右

    //AAA BBB 设置左右电机颜色
    private void clickMotor() {

        if (leftOrRight) {
            //右电机
            bindingMotorA.tvHintA.setText(Language.RIGHT_MOTOR);
            bindingMotorA.tvHintA.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            bindingMotorA.tvHintA.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_style2));

            bindingMotorB.tvHintB.setText(Language.RIGHT_MOTOR);
        } else {
            //左电机
            bindingMotorA.tvHintA.setText(Language.LEFT_MOTOR);
            bindingMotorA.tvHintA.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            bindingMotorA.tvHintA.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_style));

            bindingMotorB.tvHintB.setText(Language.LEFT_MOTOR);
        }
    }

    //放大器功率切换  NONE
    private void switchingPower() {

        if (getIntervalTime()) {
            return;
        }

        //"AmpPwr":"low/high"  放大器功率切换
        if (AmpliNONEFlag) {
            AmpliNONEFlag = false;

            bindingBottom.btnAmplifierControl.setCompoundDrawables(null, highDrawable, null, null);
            bindingBottom.btnAmplifierControl.setTextColor(ContextCompat.getColor(mContext, R.color.white));

//            "AmpPwr":"low/high"  放大器功率切换为高
//            mJsonHandle_6802.sendCmd(CV.DEMODE_NONE, "high");
            mUartHandle_6804.switchAmpPwrHigh();

        } else {
            AmpliNONEFlag = true;

            bindingBottom.btnAmplifierControl.setCompoundDrawables(null, lowDrawable, null, null);
            bindingBottom.btnAmplifierControl.setTextColor(ContextCompat.getColor(mContext, R.color.def_gray));

//            "AmpPwr":"low/high"  放大器功率切换为低
//            mJsonHandle_6802.sendCmd(CV.DEMODE_NONE, "low");
            mUartHandle_6804.switchAmpPwrLow();
        }
    }


    //正常模式 晃动模式 切换
    private void switchMode() {
        String mode = bindingBottom.btnDemodeSelect.getText().toString();

        if (Language.NORMALMODE.equals(mode)) {

            bindingBottom.btnDemodeSelect.setCompoundDrawables(null, shakingDrawable, null, null);
            bindingBottom.btnDemodeSelect.setText(Language.WOBBLEMODE);//显示晃动模式

            //切换为晃动模式
//            mJsonHandle_6802.sendCmd(CV.DEMODE_VALUE, "Wobble");
            mUartHandle_6804.switchWobbleMode();

        } else {

            bindingBottom.btnDemodeSelect.setCompoundDrawables(null, normalDrawable, null, null);
            bindingBottom.btnDemodeSelect.setText(Language.NORMALMODE);//显示正常模式

            //切换为正常模式
//            mJsonHandle_6802.sendCmd(CV.DEMODE_VALUE, "Normal");
            mUartHandle_6804.switchNormalMode();
        }
    }

    //系统复位
    private void systemReset() {

        sPUtil.saveReset(true);
        bindingTop.tvDistanceBar.setText(Language.DISTANCE + "：0.0m");
        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.RESET);
        restartApplication(MainActivity.this);
    }


    private boolean manual_focus_flag = true;

    //9.12小屏幕 手动对焦的点击事件 弹出 电机加减按钮
    private void manualFocus(TextView btnManualFocus) {

        if (manual_focus_flag) {

            manual_focus_flag = false;

            /* change the mode to PLUS_MINUS_MANUAL_FOCUS */
            MANUAL_FOCUS_OR_NOISE_SET_MODE = CV.PLUS_MINUS_MANUAL_FOCUS;

            /* change the drawable of the view */
            Drawable topDrawable = getResources().getDrawable(R.drawable.ic_done_line_selector, getTheme());
            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
            btnManualFocus.setCompoundDrawables(null, topDrawable, null, null);

            //完成对焦
            btnManualFocus.setText(Language.COMPLETE_FOCUS);

        } else {
            manual_focus_flag = true;

            /* change the mode to PLUS_MINUS_MANUAL_FOCUS */
            MANUAL_FOCUS_OR_NOISE_SET_MODE = CV.PLUS_MINUS_NONE;

            Drawable topDrawable = getResources().getDrawable(R.drawable.ic_manual_focus_selector, getTheme());
            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
            btnManualFocus.setCompoundDrawables(null, topDrawable, null, null);
            //手动对焦
            btnManualFocus.setText(Language.MANUAL_FOCUS);
        }
    }

    /*
     * when the password dialog show(this window loss focus),the navigation bar also show;
     *当密码对话框显示时（此窗口丢失焦点），导航栏也显示；
     * so,when the password dialog dismiss(this window again get focus),re-hide the navigation bar
     *因此，当密码对话框关闭（此窗口再次获得焦点）时，请重新隐藏导航栏。
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideyBar(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        System.exit(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //当我们用手指移动视线时，我们的手指将覆盖视线
        // so plus 200
        centerX = event.getX() + 200;
        centerY = event.getY() + 200;

        if (centerX < rv1Lp.width && centerY < rv1Lp.height && LEFT_IS_CALIBRATION) {
            sightViewControlLeft.setCenterX(centerX);
            sightViewControlLeft.setCenterY(centerY);

            //拖动设置位置
            leftDrawView.drawRecForCalibration(centerX, centerY, leftDrawRectangleScale);
            leftDrawView.invalidate();

            bindingLeft.btnLeftX.setText(Float.toString(centerX));
            bindingLeft.btnLeftY.setText(Float.toString(centerY));

        } else if (centerX > rv1Lp.width && centerY < rv2Lp.height && RIGHT_IS_CALIBRATION) {
            sightViewControlRight.setCenterX(centerX - rv1Lp.width);
            sightViewControlRight.setCenterY(centerY);

            //拖动设置位置
            rightDrawView.drawRecForCalibration(centerX - rv1Lp.width, centerY, rightDrawRectangleScale);
            rightDrawView.invalidate();

            bindingRight.btnRightX.setText(Float.toString(centerX - rv1Lp.width));
            bindingRight.btnRightY.setText(Float.toString(centerY));
        } else if (centerX > rv1Lp.width && centerY < rv2Lp.height) {

            //m 测试用 右大屏时 可滑动新添加的准星框

            if (isShowDebug && RIGHT_DRAW_IS_BIG_PREVIEW) {
                centerX = centerX - 400;
                centerY = centerY - 200;

                //可拖动准星
                RRRRRRR.setDrawView_VISIBLE();

                RRRRRRR.setCenterX(centerX - rv1Lp.width);
                RRRRRRR.setCenterY(centerY);

                //拖动设置位置
                hideDrawView.drawRecForCalibration(centerX - rv1Lp.width, centerY, rightDrawRectangleScale);
                hideDrawView.invalidate();
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志

            bindingLeft.gestureBrightLayout.setVisibility(View.GONE);
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        //重新计算三分钟自动息屏
        getRandomNumber("onDown");

        if (!LEFT_IS_CALIBRATION && !RIGHT_IS_CALIBRATION) {
            firstScroll = true;// 设定是触摸屏幕后第一次scroll的标志
        } else {
            firstScroll = false;
            GESTURE_FLAG = 0;
        }
        return false;
    }

    //手势识别器 如果是按下的时间超过瞬间，而且在按下的时候没有松开或者是拖动的，那么onShowPress就会执行，具体这个瞬间是多久，我也不清楚呃……
    @Override
    public void onShowPress(MotionEvent e) {
//        Log.d("gesture","onShowPress");
    }

    //手势识别器 一次单独的轻击抬起操作,也就是轻击一下屏幕，立刻抬起来，才会有这个触发
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    //手势识别器 在屏幕上拖动事件。无论是用手拖动view，或者是以抛的动作滚动，都会多次触发,这个方法
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        float mOldX = e1.getX(), mOldY = e1.getY();
        int y = (int) e2.getRawY();
        if (firstScroll) {// 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱

            // 横向的距离变化大则调整进度，纵向的变化大则调整音量
            if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                bindingLeft.gestureBrightLayout.setVisibility(View.GONE);
                bindingRight.gestureTx2spkVolumeLayout.setVisibility(View.GONE);
            } else {
                if ((mOldX > rv1Lp.width * 3.0 / 5) && (mOldX < rv1Lp.width) && (mOldY < rv1Lp.height)) {
                    // 音量
                    bindingLeft.gestureBrightLayout.setVisibility(View.GONE);
                    bindingRight.gestureTx2spkVolumeLayout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                } else if ((mOldX < rv1Lp.width * 2.0 / 5) && (mOldY < rv1Lp.height)) {
                    // 亮度调节
                    bindingLeft.gestureBrightLayout.setVisibility(View.VISIBLE);
                    bindingRight.gestureTx2spkVolumeLayout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;

                } else if ((mOldX > rv1Lp.width) && (mOldX < rv1Lp.width + (rv2Lp.width * 2.0 / 5)) && (mOldY < rv2Lp.height)) {
                    // rk3399 volume
                    bindingLeft.gestureBrightLayout.setVisibility(View.GONE);
                    bindingRight.gestureTx2spkVolumeLayout.setVisibility(View.GONE);
                    GESTURE_FLAG = GESTURE_MODIFY_RK3399_VOLUME;

                } else if ((mOldX > rv1Lp.width + (rv2Lp.width * 3.0 / 5)) && (mOldY < rv2Lp.height)) {
                    // rk3399 volume
                    if (!isShowDebug) {
                        //音量布局展示

//                        bindingRight.gestureTx2spkVolumeLayout.setVisibility(View.VISIBLE);
                        //亮度控制布局隐藏
                        bindingLeft.gestureBrightLayout.setVisibility(View.GONE);

                        GESTURE_FLAG = GESTURE_MODIFY_TX2SPK;

                        //声音=====distanceY=如果移动距离大于0表示想向上调节=====

                        if (distanceY > 0) {

//      todo                    voicePlus(false);

                            //先控制音量 在展示
                            if (AudioDecoder.VALUE_VOLUME < 10) {
                                AudioDecoder.VALUE_VOLUME++;
                                if (mAudioDecoder != null) {
                                    mAudioDecoder.controlVolume();
                                }
                                Log.e(TAG, "onClick: ========本地==========" + AudioDecoder.VALUE_VOLUME);
                            } else if (AudioDecoder.VALUE_VOLUME < 15) {
                                AudioDecoder.VALUE_VOLUME++;
                                //设置音量 48台
                                mUartHandle_6804.setVolume(450 - (15 - AudioDecoder.VALUE_VOLUME) * 50);
                                Log.e(TAG, "onClick: ====设备==============" + (450 - (15 - AudioDecoder.VALUE_VOLUME) * 50));
                            } else {
                                mUartHandle_6804.setVolume(450);
                            }
                            //喇叭显示和隐藏
                            speakerDisplay();

                        } else {

//      todo                      voiceMinus(false);

                            //先控制音量 在展示
                            if (AudioDecoder.VALUE_VOLUME > 10) {
                                AudioDecoder.VALUE_VOLUME--;
                                //设置音量 48台
                                mUartHandle_6804.setVolume((450 - (15 - AudioDecoder.VALUE_VOLUME) * 50));
                                Log.e(TAG, "onClick: ====设备==============" + (450 - (15 - AudioDecoder.VALUE_VOLUME) * 50));
                            } else if (AudioDecoder.VALUE_VOLUME >= 1) {
                                AudioDecoder.VALUE_VOLUME--;
                                if (mAudioDecoder != null) {
                                    mAudioDecoder.controlVolume();
                                }
                                Log.e(TAG, "onClick: ========本地==========" + AudioDecoder.VALUE_VOLUME);
                            }
                            //喇叭显示和隐藏
                            speakerDisplay();

                        }
                    }
                }
            }
        } else if (GESTURE_FLAG == GESTURE_MODIFY_BRIGHT) {

            // 如果每次触摸屏幕后第一次scroll是调节亮度，那之后的scroll事件都处理亮度调节，直到离开屏幕执行下一次操作
            bindingLeft.gestureIvPlayerBright.setImageResource(R.drawable.souhu_player_bright);

            mBrightness = sPUtil.getBrightness() + (mOldY - y) / rv1Lp.height / 50;

            if (mBrightness > 1.0f) {
                mBrightness = 1.0f;
            } else if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
            sPUtil.saveBrightness(mBrightness);

            setWindowBrightness(this, mBrightness);

            bindingLeft.getureTvBrightPercentage.setText((int) (mBrightness * 100) + "%");

            bindingBottom.bottomSeekProgress.setProgress1(mBrightness * 100);
        }

        firstScroll = false;// 第一次scroll执行完成，修改标志

        return false;
    }

    //手势识别器 长按触摸屏，超过一定时长，就会触发这个事件
    @Override
    public void onLongPress(MotionEvent e) {

        if ((System.currentTimeMillis() - exitTime) > 8000) {
            leftLongClickCnt = 0;
            rightLongClickCnt = 0;
            exitTime = System.currentTimeMillis();
        }

        if (e.getX() < rv1Lp.width && e.getY() < rv1Lp.height) {
            // if isn't big view, return
            if (!LEFT_DRAW_IS_BIG_PREVIEW || LEFT_IS_CALIBRATION || RIGHT_IS_CALIBRATION) {
                return;
            }

            // long click 3 times
            rightLongClickCnt++;
            if (rightLongClickCnt >= 3) {

                rightLongClickCnt = 0;
                Message msg = new Message();
                mHandler.obtainMessage();
                msg.what = CV.LONG_PRESS;
                msg.arg1 = 0x01;
                mHandler.sendMessage(msg);

            }
        } else if (e.getX() > rv1Lp.width && e.getY() < rv2Lp.height) {

            // if isn't big view, return
            if (!RIGHT_DRAW_IS_BIG_PREVIEW || LEFT_IS_CALIBRATION || RIGHT_IS_CALIBRATION) {
                return;
            }

            // long click 3 times
            leftLongClickCnt++;
            if (leftLongClickCnt >= 3) {
                leftLongClickCnt = 0;
                Message msg = new Message();
                mHandler.obtainMessage();
                msg.what = CV.LONG_PRESS;
                msg.arg1 = 0x02;
                mHandler.sendMessage(msg);
            }
        }
    }

    //手势识别器 滑屏，用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        //在图像处理和左校准中时无法切换视图
        if (!LEFT_IS_CALIBRATION) {

            final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;
            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY
                    /* limit touch range */
                    && e1.getX() <= rv1Lp.width
                    && e1.getY() <= rv1Lp.height) {

                refreashView();

            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY
                    /* limit touch range */
                    && e1.getX() <= rv1Lp.width
                    && e1.getY() <= rv1Lp.height) {

                refreashView();
            }
        } else {
            ToastUtils.showShort("自动调校中，无法切换视场!");
        }
        return false;
    }

    //切换视图 切换地址
    private void doSwitchView() {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentView == CV.BIG_SIGHT_VIEW) {
            //展示大视场
            photographWhich = false;
            if (fragment == null) {
                fragment = PlayFragment.newInstance(BigViewUrl, 1, 1, null);
                if (!fragment.isAdded()) {
                    transaction.add(R.id.surfaceView1, fragment);
                } else {
                    transaction.show(fragment);
                }

            } else {

                if (fragment2 != null && fragment2.isAdded()) {
                    transaction.hide(fragment2);
                }

                if (fragment.isAdded()) {
                    transaction.show(fragment);
                }
            }

            // 进入全屏模式
            fragment.enterFullscreen();
            transaction.commit();

            if (!photographWhich) {
                //如果是大市场 隐藏调节焦距按钮
                if (LEFT_DRAW_IS_BIG_PREVIEW) {
                    bindingMotorA.lyFabPlusMinusA.setVisibility(GONE);
                } else {
                    bindingMotorA.lyFabPlusMinusA.setVisibility(VISIBLE);
                }
            } else {
                bindingMotorA.lyFabPlusMinusA.setVisibility(VISIBLE);
            }

        } else if (currentView == CV.SMALL_SIGHT_VIEW) {
            //展示小视场
            photographWhich = true;
            if (fragment2 == null) {
                fragment2 = PlayFragment.newInstance(SmallViewUrl, 1, 1, null);
                if (!fragment2.isAdded()) {
                    transaction.add(R.id.surfaceView1, fragment2);
                }
                transaction.show(fragment2);

            } else {

                if (fragment != null && fragment.isAdded()) {
                    transaction.hide(fragment);
                }

                if (fragment2.isAdded()) {
                    transaction.show(fragment2);
                }
            }
            // 进入全屏模式
            fragment2.enterFullscreen();
            transaction.commit();

            if (!photographWhich) {
                //如果是大市场 隐藏调节焦距按钮
                if (LEFT_DRAW_IS_BIG_PREVIEW) {
                    bindingMotorA.lyFabPlusMinusA.setVisibility(GONE);
                } else {
                    bindingMotorA.lyFabPlusMinusA.setVisibility(VISIBLE);
                }
            } else {
                bindingMotorA.lyFabPlusMinusA.setVisibility(VISIBLE);
            }
        }
    }

    //左边屏幕 左右滑动 切换
    private void refreashView() {

        switch (currentPreview) {
            case CV.BIG_PREVIEW:
            case CV.NORMAL_PREVIEW:
            case CV.SMALL_PREVIEW:

                if (currentView == CV.BIG_SIGHT_VIEW) {
                    //如果当前是大视场 改为小视场
                    currentView = CV.SMALL_SIGHT_VIEW;
                    setCurrentRealView(currentPreview, currentView);
                    sightViewControlLeft.refreashCrosshairLoaction(currentRealView);

                } else if (currentView == CV.SMALL_SIGHT_VIEW) {
                    //如果是小视场改为大视场
                    currentView = CV.BIG_SIGHT_VIEW;
                    setCurrentRealView(currentPreview, currentView);
                    sightViewControlLeft.refreashCrosshairLoaction(currentRealView);
                }

                break;
            default:
                break;
        }

        //切换视图 切换地址
        doSwitchView();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    //双击
    @Override
    public boolean onDoubleTap(MotionEvent e) {

        //左右两屏幕双击才会响应的点击事件
        //获取点击区域
        float mOldX = e.getX();
        float mOldY = e.getY();

        if ((mOldX < rv1Lp.width) && (mOldY < rv1Lp.height)) {// left
            if (!LEFT_IS_CALIBRATION) {

                //设置不可调整测试准星
                notAdjustable();

                //左屏幕处理
                leftZoom();

                resetPlusMinusMode();

            } else {
                //请先完成准星校准
                ToastUtils.showShort(Language.SIGHT_CALIBRATION);
            }

        } else if ((mOldX < rv1Lp.width + rv2Lp.width) && (mOldY < rv2Lp.height)) {// right
            if (!RIGHT_IS_CALIBRATION) {

                //设置不可调整测试准星
                notAdjustable();

                //右屏幕处理
                rightZoom();

                resetPlusMinusMode();

            } else {
                //请先完成准星校准
                ToastUtils.showShort(Language.SIGHT_CALIBRATION);
            }
        }
        return false;
    }

    //设置不可调整测试准星
    private void notAdjustable() {
        //不可调整
        isShowDebug = false;

        isAdjustment = 0;

        RRRRRRR.setDrawView_GONE();

        sightViewControlRight.setDrawView_VISIBLE();

        bindingTop.tvDistanceBar.setTextColor(ContextCompat.getColor(mContext, R.color.white));

        String tempDistance = bindingTop.tvDistanceBar.getText().toString().trim();
        if (!TextUtils.isEmpty(tempDistance)) {
            if (tempDistance.contains("米")) {
                bindingTop.tvDistanceBar.setText(tempDistance.replace("米", "m"));
            }
        }
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    //音量减小 滑动
    private void voiceMinus(boolean fromClick) {

        //隐藏喇叭
        speakerHidden();

        if (fromClick) {
            if (Tx2spk_CurrentVolume % 25 == 0) {
                Tx2spk_CurrentVolume = 25 * (Tx2spk_CurrentVolume / 25) - 25;
            } else {
                Tx2spk_CurrentVolume = 25 * (Tx2spk_CurrentVolume / 25);
            }

        } else {

            //音量调节为百分之二
//            if (Tx2spk_CurrentVolume % 10 == 0) {
//                Tx2spk_CurrentVolume = 10 * (Tx2spk_CurrentVolume / 10) - 10;
//            } else {
//                Tx2spk_CurrentVolume = 10 * (Tx2spk_CurrentVolume / 10);
//            }

            //2020 12 11 调节音量改为百分之五
            if (Tx2spk_CurrentVolume >= 75) {
                Tx2spk_CurrentVolume = Tx2spk_CurrentVolume - 25;
            }
        }

        if (Tx2spk_CurrentVolume <= 0) {
            Tx2spk_CurrentVolume = 0;
        }

        bindingRight.gestureTx2spkTvVolumePercentage.setText((Tx2spk_CurrentVolume * 10 / 50 * 10) + "%");

        if (Tx2spk_CurrentVolume == 0) {// 静音，设定静音独有的图片
            bindingRight.gestureTx2spkIvPlayerVolume.setImageDrawable(pictureMute);
        }

        //设置音量
        setTx2Volume();

        sPUtil.saveTx2spkVolume(Tx2spk_CurrentVolume);
    }

    //设置音量
    private void setTx2Volume() {

        //设置音量 哈萨克斯坦（之前为百分之二后改为百分之五）（最开始控制音量方案）
//        mJsonHandle_6802.sendCmd(CV.TX2SPK_VOLUME, String.valueOf(Tx2spk_CurrentVolume));

        //设置音量 48台
        mUartHandle_6804.setVolume(Tx2spk_CurrentVolume);
    }

    //增强音量点击
    private void voicePlus(boolean fromClick) {

        //隐藏喇叭
        speakerHidden();

        if (Tx2spk_CurrentVolume < maxTx2spkVolume) {// 为避免调节过快，distanceY应大于一个设定值
            if (fromClick) {
                Tx2spk_CurrentVolume = 25 * (Tx2spk_CurrentVolume / 25) + 25;
            } else {
                //调节改为百分之二
//                Tx2spk_CurrentVolume = 10 * (Tx2spk_CurrentVolume / 10) + 10;
                //2020 12 11 调节改为百分之五
                Tx2spk_CurrentVolume = Tx2spk_CurrentVolume + 25;
            }
        }

        bindingRight.gestureTx2spkIvPlayerVolume.setImageResource(R.drawable.iv_volume);
        bindingRight.gestureTx2spkTvVolumePercentage.setText((Tx2spk_CurrentVolume * 10 / 50) + "%");

        //设置音量
        setTx2Volume();

        sPUtil.saveTx2spkVolume(Tx2spk_CurrentVolume);
    }

    //三次长按 展示可手动调整准星
    private int isAdjustment = 0;
    //长按事件不响应点击事件控制
    private boolean notClick = false;
    //判断是否可以拖动准星
    private boolean isShowDebug;

    //判断自动对焦按钮是否响应点击事件
    private boolean isResponsePress = false;
    //判断自动对焦按钮状态 并根据此变量判断是否发送指令
    private boolean btnState;

    //标注长按触发的点击事件还是点击触发的点击事件 默认点击触发

    /*手动对焦长按 不可删除 测试准星*/
    private void textZhunXing3() {
        notClick = true;
        isAdjustment++;
        String tempDistance = bindingTop.tvDistanceBar.getText().toString().trim();

        if (isAdjustment == 1) {
            //可调整
            isShowDebug = true;
            RRRRRRR.setDrawView_VISIBLE();
            sightViewControlRight.setDrawView_GONE();

            bindingTop.tvDistanceBar.setTextColor(logo_red);

            if (!TextUtils.isEmpty(tempDistance)) {
                if (tempDistance.contains("m")) {
                    bindingTop.tvDistanceBar.setText(tempDistance.replace("m", "米"));
                } else {
                    bindingTop.tvDistanceBar.setText(tempDistance + "米");
                }
            }

        } else if (isAdjustment == 2) {
            //不可调整
            isShowDebug = false;

            if (!TextUtils.isEmpty(tempDistance)) {
                if (tempDistance.contains("米")) {
                    bindingTop.tvDistanceBar.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                }
            }

        } else {

            //不可调整
            notAdjustable();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mAudioDecoder.setPause(AudioDecoder.AUDIO_START);
    }

    private void setCurrentRealView(byte currentView, byte currentPreview) {
        this.currentRealView = (byte) (currentView | currentPreview);
    }

    //重启app
    public void restartApplication(Context context) {
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        System.exit(0);
    }

    private static void startActivitySafety(Activity a, Intent intent) {
        try {
            if (intent == null) {
                return;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            a.startActivity(intent);
        } catch (Exception e) {
        }
    }

    private void initUI() {

        bindingMain = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(bindingMain.getRoot());
        bindingLeft = ViewLeftHalfScreenBinding.bind(bindingMain.getRoot());
        bindingRight = ViewRightHalfScreenBinding.bind(bindingMain.getRoot());
        bindingTop = ViewTopSignalBarBinding.bind(bindingMain.getRoot());
        bindingBottom = ViewBottomButtonAreaBinding.bind(bindingMain.getRoot());

        bindingMotorA = ViewMotorAreaABinding.bind(bindingMain.getRoot());
        bindingMotorB = ViewMotorAreaBBinding.bind(bindingMain.getRoot());
        bindingVolume = ViewVolumeAreaBinding.bind(bindingMain.getRoot());
        bindingDirectionA = ViewDirectionBarABinding.bind(bindingMain.getRoot());
        bindingDirectionB = ViewDirectionBarBBinding.bind(bindingMain.getRoot());

        buttonShowHidden();

        //Main
        bindingMain.tvKsdj.setOnLongClickListener(longClickListener);
        bindingMain.tvBiaoji.setOnClickListener(this);
        bindingMain.menuLayoutLeft.setOnClickListener(this);
        bindingMain.btnSet1.setOnClickListener(this);
        bindingMain.btnSet2.setOnClickListener(this);
        bindingMain.btnSet3.setOnClickListener(this);
        bindingMain.btnGet1.setOnClickListener(this);
        bindingMain.btnGet2.setOnClickListener(this);
        bindingMain.btnGet3.setOnClickListener(this);
        //顶部布局
        bindingTop.tvDistanceBar.setOnClickListener(this);//距离点击
        //左半屏幕
        bindingLeft.fabLeftDone.setOnClickListener(this);
        //右半屏幕
        bindingRight.fabRightDone.setOnClickListener(this);

        //电机A
        bindingMotorA.fabBtnPlusA.setOnClickListener(this);
        bindingMotorA.fabBtnPlusA.setOnLongClickListener(longClickListener);
        bindingMotorA.fabBtnMinusA.setOnClickListener(this);
        bindingMotorA.fabBtnMinusA.setOnLongClickListener(longClickListener);
        bindingMotorA.tvHintA.setOnClickListener(this);
        //电机B
        bindingMotorB.fabBtnPlusB.setOnClickListener(this);
        bindingMotorB.fabBtnPlusB.setOnLongClickListener(longClickListener);
        bindingMotorB.fabBtnMinusB.setOnClickListener(this);
        bindingMotorB.fabBtnMinusB.setOnLongClickListener(longClickListener);
        bindingMotorB.tvHintB.setOnClickListener(this);
        //音量
//        bindingVolume.voicePlus.setOnClickListener(this);
//        bindingBottom.voicePlus.setOnClickListener(this);
//        bindingVolume.voiceMinus.setOnClickListener(this);
//        bindingBottom.voiceMinus.setOnClickListener(this);
        //底部按钮
//        bindingBottom.llConsumerClick.setOnClickListener(this);
        bindingBottom.btnAutoFocus.setOnClickListener(this);
//        bindingBottom.tvSound.setOnClickListener(this);
//        bindingVolume.tvSound.setOnClickListener(this);
        bindingBottom.btnStartRecord.setOnClickListener(this);
        bindingBottom.btnPlayback.setOnClickListener(this);
        bindingBottom.btnReset.setOnClickListener(this);
        bindingBottom.btnArithmeticSelect.setOnClickListener(this);
        bindingBottom.btnPhotograph.setOnClickListener(this);
        bindingBottom.btnAmplifierControl.setOnClickListener(this);
        bindingBottom.btnDemodeSelect.setOnClickListener(this);
        bindingBottom.btnOnlineSelect.setOnClickListener(this);
        bindingBottom.btnInspection.setOnClickListener(this);
        bindingBottom.shengdian.setOnClickListener(this);
        bindingBottom.tvHideHigh.setOnClickListener(this);
        bindingBottom.tvCruise.setOnClickListener(this);
        bindingBottom.motorSteps.setOnClickListener(this);
//        bindingBottom.btnTabRow2AutoFocus.setOnClickListener(this);
//        bindingBottom.btnTabRow2StartRecord.setOnClickListener(this);
//        bindingBottom.btnTabRow3SetLocation.setOnClickListener(this);
//        bindingBottom.btnTabRow3ManualFocus.setOnClickListener(this);
//        bindingBottom.btnTabRow3NoiseSet.setOnClickListener(this);
//        bindingBottom.btnTabRow4Reset.setOnClickListener(this);
//        bindingBottom.btnTabRow4Playback.setOnClickListener(this);
//        bindingBottom.btnTabRow3ManualFocus.setOnLongClickListener(longClickListener);
        bindingBottom.btnAutoFocus.setOnLongClickListener(longClickListener);//自动对焦长按
        bindingBottom.motorSteps.setOnLongClickListener(longClickListener);//自动对焦长按
//        bindingBottom.btnTabRow2AutoFocus.setOnLongClickListener(longClickListener);//自动对焦长按
        //方向A
        bindingDirectionA.btnHide.setOnClickListener(this);
        bindingDirectionA.btnHide.setOnLongClickListener(longClickListener);
        bindingDirectionA.btnQuickUpA.setOnClickListener(this);
        bindingDirectionA.btnQuickUpA.setOnLongClickListener(longClickListener);
        bindingDirectionA.btnQuickDownA.setOnClickListener(this);
        bindingDirectionA.btnQuickDownA.setOnLongClickListener(longClickListener);
        bindingDirectionA.btnQuickLeftA.setOnClickListener(this);
        bindingDirectionA.btnQuickLeftA.setOnLongClickListener(longClickListener);
        bindingDirectionA.btnQuickRightA.setOnClickListener(this);
        bindingDirectionA.btnQuickRightA.setOnLongClickListener(longClickListener);
        //方向B
        bindingDirectionB.fabBtnUpB.setOnClickListener(this);
        bindingDirectionB.fabBtnDownB.setOnClickListener(this);
        bindingDirectionB.fabBtnLeftB.setOnClickListener(this);
        bindingDirectionB.fabBtnRightB.setOnClickListener(this);

        //禁止手势滑动
        bindingMain.myDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //打开手势滑动
//        bindingMain.myDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        bindingBottom.btnDemodeSelect.setText(Language.NORMALMODE);
        bindingMain.tvKsdj.setText(Language.LONG_PRESS_THE_SCREEN_TO_RESTORE);

        initOnTouchListener();
    }

    private boolean outLeft = false;
    private boolean outUp = false;
    private boolean outRight = false;
    private boolean outDown = false;

    @SuppressLint("ClickableViewAccessibility")
    private void initOnTouchListener() {
        //------方向键--------A--------越界
        bindingDirectionA.btnQuickLeftA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (isConsole) {
                        //云台 向左 长按停止
                        if (event.getX() < 0 || event.getY() < 0
                                || event.getX() > bindingDirectionA.btnQuickLeftA.getWidth()
                                || event.getY() > bindingDirectionA.btnQuickLeftA.getHeight()) {
                            outLeft = true;
//                            Log.e(TAG, "upClick:----左---------------- ");
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    longClickHandler.removeCallbacksAndMessages(null);

                    if (isConsole) {
                        //云台 向左 长按停止
                        if (outLeft) {
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
//                            Log.e(TAG, "upClick:--- 左 滑出边界停止 ");
                            //修改变量 默认是点击事件
                            is_LEFT_SingleClick = true;
                        }

                        outLeft = false;
                    }
                }
                return false;
            }
        });

        bindingDirectionA.btnQuickUpA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    if (isConsole) {
                        //云台 向上 长按停止
                        if (event.getX() < 0 || event.getY() < 0
                                || event.getX() > bindingDirectionA.btnQuickUpA.getWidth()
                                || event.getY() > bindingDirectionA.btnQuickUpA.getHeight()) {
                            outUp = true;
//                            Log.e(TAG, "upClick:----上---------------- ");
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    longClickHandler.removeCallbacksAndMessages(null);

                    if (isConsole) {
                        //云台 向上 长按停止
                        if (outUp) {
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
//                            Log.e(TAG, "upClick:---上 滑出边界停止 ");
                            is_UP_SingleClick = true;
                        }

                        outUp = false;
                    }
                }
                return false;
            }
        });

        bindingDirectionA.btnQuickRightA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    if (isConsole) {
                        //云台 向右 长按停止
                        if (event.getX() < 0 || event.getY() < 0
                                || event.getX() > bindingDirectionA.btnQuickRightA.getWidth()
                                || event.getY() > bindingDirectionA.btnQuickRightA.getHeight()) {
                            outRight = true;
//                            Log.e(TAG, "upClick:----右---------------- ");
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    longClickHandler.removeCallbacksAndMessages(null);

                    if (isConsole) {
                        //云台 向右 长按停止
                        if (outRight) {
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
//                            Log.e(TAG, "upClick:--- 右 滑出边界停止 ");
                            is_RIGHT_SingleClick = true;
                        }

                        outRight = false;
                    }
                }
                return false;
            }
        });

        bindingDirectionA.btnQuickDownA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    if (isConsole) {
                        //云台 向下 长按停止
                        if (event.getX() < 0 || event.getY() < 0
                                || event.getX() > bindingDirectionA.btnQuickDownA.getWidth()
                                || event.getY() > bindingDirectionA.btnQuickDownA.getHeight()) {
                            outDown = true;
//                            Log.e(TAG, "upClick:----下---------------- ");
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    longClickHandler.removeCallbacksAndMessages(null);

                    if (isConsole) {
                        //云台 向下 长按停止
                        if (outDown) {
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_STOP);
//                            Log.e(TAG, "upClick:---下 滑出边界停止 ");
                            is_DOWN_SingleClick = true;
                        }

                        outDown = false;
                    }
                }
                return false;
            }
        });

//        //------声音加减-------------
//        bindingBottom.voicePlus.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    longClickHandler.removeCallbacksAndMessages(null);
//                }
//                return false;
//            }
//        });
//
//        bindingBottom.voiceMinus.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    longClickHandler.removeCallbacksAndMessages(null);
//                }
//                return false;
//            }
//        });

        //------焦距电机--------A--------
        bindingMotorA.fabBtnMinusA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    longClickHandler.removeCallbacksAndMessages(null);
                }
                return false;
            }
        });

        bindingMotorA.fabBtnPlusA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    longClickHandler.removeCallbacksAndMessages(null);
                }
                return false;
            }
        });

        bindingMotorB.fabBtnMinusB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    longClickHandler.removeCallbacksAndMessages(null);
                }
                return false;
            }
        });

        bindingMotorB.fabBtnPlusB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    longClickHandler.removeCallbacksAndMessages(null);
                }
                return false;
            }
        });

        //设置窗口亮度
        mBrightness = sPUtil.getBrightness();

        setWindowBrightness(this, mBrightness);

        //设置亮度进度条
        bindingBottom.arcViewbrightnes.setProgress(mBrightness * 100);

        bindingBottom.bottomSeekProgress.setProgress1(mBrightness * 100);

        bindingBottom.bottomSeekProgress.setOnSeekBarChangeListener1(new SignSeekBar2.OnSeekBarChangeListener1() {
            @Override
            public void onProgressChanged1(SeekBar seekBar, int progress, boolean fromUser) {

                mBrightness = progress / 100f;

                setWindowBrightness(MainActivity.mContext, mBrightness);
                //设置亮度进度条
                bindingBottom.arcViewbrightnes.setProgress(mBrightness * 100);
            }

            @Override
            public void onStopTrackingTouch1(int seekBarNumber) {

                //保存亮度值 接口返回值未用到
                sPUtil.saveBrightness(mBrightness);
            }

            @Override
            public void MY_ONTOUCHEVENT_ACTION_DOWN(boolean isOnTouch) {
            }
        });

        bindingBottom.topSeekProgress.setOnSeekBarChangeListener1(new SignSeekBar2.OnSeekBarChangeListener1() {
            @Override
            public void onProgressChanged1(SeekBar seekBar, int progress, boolean fromUser) {
                //设置声音进度条
                bindingBottom.arcViewVolume.setProgress(progress * 10);
            }


            @Override
            public void onStopTrackingTouch1(int seekBarNumber) {

                if (seekBarNumber <= 10) {
                    //设置音量 48台
                    mUartHandle_6804.setVolume(200);
                    AudioDecoder.VALUE_VOLUME = seekBarNumber;
                    if (mAudioDecoder != null) {
                        mAudioDecoder.controlVolume();
                    }
                } else {

                    AudioDecoder.VALUE_VOLUME = 10;
                    if (mAudioDecoder != null) {
                        mAudioDecoder.controlVolume();
                    }
                    //设置音量 48台
                    mUartHandle_6804.setVolume(450 - (15 - seekBarNumber) * 50);
                }

                if (AudioDecoder.VALUE_VOLUME == 0) {
                    //设置静音
                    bindingRight.gestureTx2spkIvPlayerVolume.setImageDrawable(pictureMute);
                    bindingBottom.imageA.setImageDrawable(pictureMute);

                } else {
                    bindingRight.gestureTx2spkIvPlayerVolume.setImageResource(R.drawable.iv_volume);
                    bindingBottom.imageA.setImageResource(R.drawable.iv_volume);

                }
            }

            @Override
            public void MY_ONTOUCHEVENT_ACTION_DOWN(boolean isOnTouch) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (!isRecording) {
                //退出APP
                exitApp();
            } else {
                //请先关闭录像
//                ToastUtils.showShort(TURN_OFF_RECORDING);
                Toast.makeText(this, Language.TURN_OFF_VIDEO, Toast.LENGTH_SHORT).show();
            }

            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    //退出app处理
    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            //两秒内 再点一次退出
            ToastUtils.showShort(Language.DROP_OUT_PROMPT);
//            Toast.makeText(this, Language.DROP_OUT_PROMPT, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            exet();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        sPUtil.saveReset(false);

        //重新计算三分钟自动息屏
        getRandomNumber("onResume");

        isShow = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        isShow = false;

        // 停止影像处理stop image process
        mJsonHandle_6802.sendCmd(CV.IMG_PROCESS_OFF, null);
        if (mAudioDecoder != null) {
            mAudioDecoder.setPause(AudioDecoder.AUDIO_PAUSE);
        }
    }


    //记录循环了多少次
    private static int intervalCounter = 0;
    private int recordBootTimer = 0;
    private int controlTimer = 0;

    //自动息屏
    private void automaticRestScreen() {

        /* 表示0毫秒之後，每隔100毫秒執行一次 */
        MyApplication.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                recordBootTimer++;

                //判断是否拥有控制权
                isHaveControl();

                //一键自检弹窗
                if (inspection != null && inspection.dialog != null && inspection.dialog.isShowing()) {
                    inspection.recordBoot();
                }

                //检测有线连接弹窗
                if (inspectionNET != null && inspectionNET.dialog != null && inspectionNET.dialog.isShowing()) {
                    inspectionNET.checkLoop();
                }

                if (recordBootTimer % 2 == 0 && btnState) {

                    energyCancel();

                    if (!UartHandle6804.socketIsBroken) {
                        Log.e(TAG, "onClick: 自动对焦 每隔五秒发送一次指令");
                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.AUTO_FOCUS);
                    }
                }

                if (recordBootTimer % 60 == 0) {
                    PermissionsUtils.recordBoot();
                }

                if (bindingLeft.rv1.getVisibility() == View.VISIBLE) {

                    intervalCounter++;

                    if (isShow && DialogNoiseUtils.AlertDialogIsClose && intervalCounter >= 20 * 3) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (bindingLeft.rv1.getVisibility() == View.VISIBLE) {
                                    intervalCounter = 0;
                                    //关闭弹窗
                                    if (inspection != null) {
                                        inspection.closeAlertDialog();
                                    }
                                    if (inspectionNET != null) {
                                        inspectionNET.closeAlertDialog();
                                    }
                                    isShow = false;

                                    /*省电模式*/
                                    //关闭左侧弹窗
                                    isCloseDrawerLayout = false;
                                    bindingMain.myDrawerLayout.closeDrawers();
                                    setWindowBrightness(MainActivity.this, 0);
                                    bindingLeft.rv1.setVisibility(View.GONE);
                                    bindingRight.rv2.setVisibility(View.GONE);
                                    bindingMain.tvKsdj.setVisibility(View.VISIBLE);

                                    setButtonState(true);

                                    cccccccc("结束");
                                }
                            }
                        });
                    }
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    //重新计算三分钟自动息屏
    public static void getRandomNumber(String from) {
        intervalCounter = 0;
        cccccccc("开始" + from);
    }

    //按钮显示隐藏
    private void buttonShowHidden() {
        //有线模式的显示和隐藏
        if (sPUtil.getWired()) {
            bindingBottom.btnOnlineSelect.setVisibility(View.GONE);
        } else {
//            bindingBottom.btnOnlineSelect.setVisibility(View.VISIBLE);
            bindingBottom.btnOnlineSelect.setVisibility(View.GONE);
        }

        //晃动模式的显示和隐藏
        if (sPUtil.getShakeMode()) {
            bindingBottom.btnDemodeSelect.setVisibility(View.GONE);
        } else {
//            bindingBottom.btnDemodeSelect.setVisibility(View.VISIBLE);
            bindingBottom.btnDemodeSelect.setVisibility(View.GONE);
        }

        //NONE的显示和隐藏
        if (sPUtil.getNONE()) {
            bindingBottom.btnAmplifierControl.setVisibility(View.GONE);
        } else {
//            bindingBottom.btnAmplifierControl.setVisibility(View.VISIBLE);
            bindingBottom.btnAmplifierControl.setVisibility(View.GONE);
        }

        //降噪选择的显示和隐藏
        if (sPUtil.getNoiseReduction()) {
            bindingBottom.btnArithmeticSelect.setVisibility(View.GONE);
        } else {
            bindingBottom.btnArithmeticSelect.setVisibility(View.VISIBLE);
//            bindingBottom.btnArithmeticSelect.setVisibility(View.GONE);
        }

        //开机自检的显示和隐藏
        if (sPUtil.getPOST()) {
            bindingBottom.btnInspection.setVisibility(View.GONE);
        } else {
            bindingBottom.btnInspection.setVisibility(View.VISIBLE);
//            bindingBottom.btnInspection.setVisibility(View.GONE);
        }

        //电动云台的显示和隐藏
        if (sPUtil.getPTZ()) {
            bindingDirectionA.btnHide.setVisibility(View.GONE);
        } else {
            bindingDirectionA.btnHide.setVisibility(View.VISIBLE);
//            bindingDirectionA.btnHide.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {

        exet();

        super.onDestroy();
    }

    public void exet() {

        if (mUartHandle_6804 != null) {
            //关闭焦距电机使能
            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.ASDF2);
        }

        //停止录像
        codecToggle(false);

        //解注册广播
        unregisterReceiver(networkChangeReceiver);

        if (mAudioDecoder != null) {
            mAudioDecoder.stop();
        }

        if (mUartHandle_6804 != null) {
            mUartHandle_6804.stop();
        }

        if (mJsonHandle_6802 != null) {
            mJsonHandle_6802.stop();
        }

//        longClickHandler
//        updateTimeHandler
//        mHandler

//        会提前退出 引发报错
//        MyApplication.getScheduledExecutorService().shutdown();
//        MyApplication.getSingleThreadExecutor().shutdown();
//        MyApplication.getThreadPool().shutdown();

        EventBus.getDefault().unregister(this);

        System.exit(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEventBean messageEventBean) {

        switch (messageEventBean.getMessage()) {
            case "HOME":
                //停止录像
                codecToggle(false);

                sPUtil.saveReset(true);

                exet();

                break;
            default:
                break;
        }
    }

    /**
     * 获取点击按钮的间隔时间
     */
    private Long lastTime = 0L;//自动对焦点击事件时间间隔计时器

    // todo 获取距离上次点击的时间差
    public boolean getIntervalTime() {

        if ((System.currentTimeMillis() - lastTime) < 1000) {

            //请不要频繁点击
//            ToastUtils.showShort(NOT_CLICK_FREQUENTLY);
            Toast.makeText(this, Language.NOT_CLICK_FREQUENTLY, Toast.LENGTH_SHORT).show();
            lastTime = System.currentTimeMillis();

            return true;

        } else {

            lastTime = System.currentTimeMillis();

            return false;
        }
    }

    //---------长按点击事件----------------------------------------------------------------------------------------------------

    //长按连续发送焦点增减命令
    private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            longClickHandler.sendEmptyMessage(v.getId());
            return false;
        }
    };

    private boolean btnHideIsLong;

    private Handler longClickHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case R.id.btnHide:

                    btnHideIsLong = false;
                    //左边屏幕为大屏时不响应此长按事件
                    if (!LEFT_DRAW_IS_BIG_PREVIEW) {
                        isConsole = !isConsole;
                        if (isConsole) {
                            //改变云台字体 快慢显示
                            changePtzText(Language.BALL_HEAD);
                        } else {
                            //显示调整架
                            bindingDirectionA.btnHide.setText(Language.ADJUSTING_FRAME);
                        }
                    }
                    break;

                case R.id.fabBtnPlusA:
                    //发送指令 电机调节
                    left_right = false;
                    adjustMotor(true);
                    longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    break;
                case R.id.fabBtnPlusB:
                    //发送指令 电机调节
                    left_right = true;
                    adjustMotor(true);
                    longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    break;
                case R.id.fabBtnMinusA:
                    //发送指令 电机调节
                    left_right = false;
                    adjustMotor(false);
                    longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    break;
                case R.id.fabBtnMinusB:
                    //发送指令 电机调节
                    left_right = true;
                    adjustMotor(false);
                    longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    break;
                case R.id.btnQuickUpA:

                    is_UP_SingleClick = false;

                    if (isConsole) {

                        if (k_or_m) {
                            //云台 向上 单次
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_UP);
//                            Log.e(TAG, "upClick:--- 云台 长按 向上 慢200毫秒发送一次指令 ");
                            longClickHandler.sendEmptyMessageDelayed(msg.what, 100);

                        } else {
                            //云台 长按 向上开始
//                            Log.e(TAG, "upClick:--- 云台 长按 向上  ");
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_UP);
                        }

                    } else {
                        //调整架 长按 向上
//                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_FORWARD);
                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_FORWARD_LONG_CLICK);
                        longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    }

                    break;
                case R.id.btnQuickDownA:

                    is_DOWN_SingleClick = false;

                    if (isConsole) {
                        //云台 长按 向下

                        if (k_or_m) {
                            //云台 单次点击 向下
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_DOWN);
//                            Log.e(TAG, "upClick:--- 云台 长按 向下 慢 200毫秒发送一次指令 ");
                            longClickHandler.sendEmptyMessageDelayed(msg.what, 200);
                        } else {
//                            Log.e(TAG, "upClick:--- 云台 长按 向下  ");
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_DOWN);
                        }

                    } else {
                        //调整架 长按 向下
//                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_REVERSAL);
                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_REVERSAL_LONG_CLICK);
                        longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    }

                    break;
                case R.id.btnQuickLeftA:

                    is_LEFT_SingleClick = false;

                    if (isConsole) {

                        if (k_or_m) {
                            //云台 向左单次点击
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LEFT);
//                            Log.e(TAG, "upClick:--- 云台 长按 向左 慢 200毫秒发送一次指令 ");
                            longClickHandler.sendEmptyMessageDelayed(msg.what, 200);
                        } else {
                            //云台 长按 向左
//                            Log.e(TAG, "upClick:--- 云台 长按 向左");
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_LEFT);
                        }
                    } else {
                        //调整架 长按 向左
//                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_FORWARD);
                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_FORWARD_LONG_CLICK);
                        longClickHandler.sendEmptyMessageDelayed(msg.what, 200);
                    }

                    break;
                case R.id.btnQuickRightA:

                    is_RIGHT_SingleClick = false;

                    if (isConsole) {
                        if (k_or_m) {
                            //云台 向右单次点击
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_RIGHT);
//                            Log.e(TAG, "upClick:--- 云台 长按 向右 慢 200毫秒发送一次指令 ");
                            longClickHandler.sendEmptyMessageDelayed(msg.what, 200);
                        } else {
                            //云台 长按 向右
//                            Log.e(TAG, "upClick:--- 云台 长按 向右 ");
                            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M5_LONG_RIGHT);
                        }
                    } else {
                        //调整架 长按 向右
//                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M4_REVERSAL);
                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.M3_REVERSAL_LONG_CLICK);
                        longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    }

                    break;
                case R.id.fabProtectStepperUp:
                    //步进电机增增长 全局搜 Protect Stepper 点击事件
                    mUartHandle_6804.protectStepperSendCmd(100);
                    longClickHandler.sendEmptyMessageDelayed(msg.what, 100);
                    break;
                case R.id.fabProtectStepperDown:
                    //步进点击减减减 全局搜 Protect Stepper 点击事件
                    mUartHandle_6804.protectStepperSendCmd(-100);
                    longClickHandler.sendEmptyMessageDelayed(msg.what, 100);

                    break;
                case R.id.btnAutoFocus:
//                case R.id.btnTabRow2AutoFocus:
                    //自动对焦长按点击事件

                    isResponsePress = true;
                    btnState = !btnState;

                    //主线程
                    if (btnState) {
                        //todo send 进入自动对焦 cmd
                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.AUTO_FOCUS_IN);
                        bindingBottom.btnAutoFocus.setCompoundDrawables(null, focusDrawable2, null, null);
                        bindingBottom.btnAutoFocus.setTextColor(ContextCompat.getColor(mContext, R.color.purple_a700));
                    } else {
                        //todo send 退出自动对焦 cmd
                        mUartHandle_6804.sendWriteCmd_WithAttrID(CV.AUTO_FOCUS_OUT);
                        bindingBottom.btnAutoFocus.setCompoundDrawables(null, focusDrawable1, null, null);
                        bindingBottom.btnAutoFocus.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    }

                    break;
//                case R.id.btnTabRow3ManualFocus:

                /*手动对焦长按 不可删除 测试准星*/
//                    textZhunXing3();

//                    break;
                case R.id.tvKsdj:
                    //显示屏幕 省电
                    if (bindingLeft.rv1.getVisibility() != View.VISIBLE) {
                        //省电
                        bindingMain.tvKsdj.setVisibility(View.GONE);
                        bindingLeft.rv1.setVisibility(View.VISIBLE);
                        bindingRight.rv2.setVisibility(View.VISIBLE);
                        isShow = true;
                        setWindowBrightness(MainActivity.this, sPUtil.getBrightness());
                    }

                    break;

                case R.id.motorSteps:

                    //长按隐藏步进电机步数按钮
                    bindingBottom.motorSteps.setVisibility(View.GONE);

                    break;
                default:
                    break;
            }
            return false;
        }
    });

    //改变云台字体 快慢显示
    private void changePtzText(String ball_head) {
        if (k_or_m) {

            //显示慢
            bindingDirectionA.btnHide.setText(Language.SLOW + '\n' + ball_head);

        } else {

            //显示快
            bindingDirectionA.btnHide.setText(Language.FAST + '\n' + ball_head);

        }
    }

    private IntentFilter intentFilter;
    private OtgWorkReceiver networkChangeReceiver;

    private void initGuangbo() {
        // 创建 IntentFilter 实例
        intentFilter = new IntentFilter();
        // 添加广播值
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // 创建 NetworkChangeReceiver 实例
        networkChangeReceiver = new OtgWorkReceiver();
        // 注册广播
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    //显示控制权标志
    public void showFlag(boolean isShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                if (AudioDecoder.VALUE_VOLUME == 0) {
//                    //静音图片
//                    bindingMain.tvIsOccupy.setImageDrawable(pictureMute);
//                } else {
//                    //小圆点
//                    bindingMain.tvIsOccupy.setImageDrawable(noSound);
//                }
//
//                if (isShow) {
//                    bindingMain.tvIsOccupy.setVisibility(VISIBLE);
//                } else {
//                    bindingMain.tvIsOccupy.setVisibility(GONE);
//                }
            }
        });
    }


    //判断是否拥有控制权
    private void isHaveControl() {
        if (isActivityTop()) {
            if (EasyPlayerClient.VIDEO_STATUS) {
                controlTimer++;
                if (controlTimer % 4 == 0) {
                    controlTimer = 0;
                    if (am == null) {
                        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    }
                    if (am.isMusicActive()) {//如果有声音
                        showFlag(false);
                    } else { //如果没有声音
                        showFlag(true);
                    }
                } else {
                    if (am.isMusicActive()) {//如果有声音
                        showFlag(false);
                    }
                }
            } else {//如果没有图像
                controlTimer = 0;
                showFlag(false);
            }
        } else {
            controlTimer = 0;
        }
    }


    /**
     * 判断某activity是否处于栈顶
     *
     * @return true在栈顶 false不在栈顶
     */
    private boolean isActivityTop() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(MainActivity.class.getName());
    }

    private int energyClick1 = 0;
    private int energyClick2 = 0;
    private boolean isSend;

    //10秒内自动发送命令 取消使能 能量
    private void energyCancel() {

        energyClick1++;

        if (!isSend) {

            Log.e(TAG, "energyCancel: === 开始使能 === " + DatesUtils.cccccccc("开始使能"));

            isSend = true;
            mUartHandle_6804.sendWriteCmd_WithAttrID(CV.ASDF1);
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                energyClick2++;
                Log.e(TAG, "energyCancel: === === " + energyClick1 + "    " + energyClick2 + "    " + DatesUtils.cccccccc("取消使能"));
                if (energyClick1 == energyClick2) {

                    Log.e(TAG, "energyCancel: === 取消使能 === " + DatesUtils.cccccccc("取消使能"));

                    mUartHandle_6804.sendWriteCmd_WithAttrID(CV.ASDF2);
                    isSend = false;
                    energyClick1 = 0;
                    energyClick2 = 0;
                }
            }
        }, 4000);
    }

    private Animation animation;
    private Animation animation1;
    private Animation animation2;
    private Animation animation3;

    private void setLogo(boolean isShow) {

        if (isShow) {
            bindingMain.imageView.setVisibility(VISIBLE);
            bindingMain.imageView1.setVisibility(VISIBLE);
            bindingMain.imageView.startAnimation(animation);
            bindingMain.imageView1.startAnimation(animation1);
            bindingBottom.tabBottom.startAnimation(animation2);
            bindingBottom.tabBottom.setVisibility(VISIBLE);
            bindingDirectionA.tbQuickDirectionButtons.startAnimation(animation2);
        } else {
            bindingBottom.tabBottom.startAnimation(animation3);
            bindingMain.imageView.startAnimation(animation);
            bindingMain.imageView1.startAnimation(animation1);
            bindingBottom.tabBottom.setVisibility(GONE);
        }
    }
}
