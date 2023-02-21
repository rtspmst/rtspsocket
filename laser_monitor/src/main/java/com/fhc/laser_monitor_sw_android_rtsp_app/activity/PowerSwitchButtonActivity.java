package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.databinding.ActivityPowerSwitchButtonBinding;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.SharedPreferencesUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.views.SwitchView;

public class PowerSwitchButtonActivity extends AppCompatActivity {

    private ActivityPowerSwitchButtonBinding binding;
    private SharedPreferencesUtil sPUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_switch_button);
        hideyBar();
        binding = ActivityPowerSwitchButtonBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        sPUtil = new SharedPreferencesUtil();

        binding.tvA3.setSwitchStatus(sPUtil.getLanguage());
        Chinese切换(sPUtil.getLanguage(), binding.tvA2);
        binding.tvA3.setOnSwitchChangeListener(new SwitchView.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                //false为中文 默认中文
                Chinese切换(open, binding.tvA2);
                sPUtil.saveLanguage(open);
            }
        });

        binding.tvB3.setSwitchStatus(!sPUtil.getWired());
        hideAndDisplay(sPUtil.getWired(), binding.tvB2);
        binding.tvB3.setOnSwitchChangeListener(new SwitchView.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                hideAndDisplay(!open, binding.tvB2);
                sPUtil.saveWired(!open);
            }
        });

        //晃动模式
        binding.tvC3.setSwitchStatus(!sPUtil.getShakeMode());
        hideAndDisplay(sPUtil.getShakeMode(), binding.tvC2);
        binding.tvC3.setOnSwitchChangeListener(new SwitchView.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                hideAndDisplay(!open, binding.tvC2);
                sPUtil.saveShakeMode(!open);
            }
        });

        //NONE
        binding.tvD3.setSwitchStatus(!sPUtil.getNONE());
        hideAndDisplay(sPUtil.getNONE(), binding.tvD2);
        binding.tvD3.setOnSwitchChangeListener(new SwitchView.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                hideAndDisplay(!open, binding.tvD2);
                sPUtil.saveNONE(!open);
            }
        });

        //降噪方法
        binding.tvE3.setSwitchStatus(!sPUtil.getNoiseReduction());
        hideAndDisplay(sPUtil.getNoiseReduction(), binding.tvE2);
        binding.tvE3.setOnSwitchChangeListener(new SwitchView.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                hideAndDisplay(!open, binding.tvE2);
                sPUtil.saveNoiseReduction(!open);
            }
        });

        //开机自检
        binding.tvF3.setSwitchStatus(!sPUtil.getPOST());
        hideAndDisplay(sPUtil.getPOST(), binding.tvF2);
        binding.tvF3.setOnSwitchChangeListener(new SwitchView.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                hideAndDisplay(!open, binding.tvF2);
                sPUtil.savePOST(!open);
            }
        });

        //电动云台
        binding.tvG3.setSwitchStatus(!sPUtil.getPTZ());
        hideAndDisplay(sPUtil.getPTZ(), binding.tvG2);
        binding.tvG3.setOnSwitchChangeListener(new SwitchView.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                hideAndDisplay(!open, binding.tvG2);
                sPUtil.savePTZ(!open);
            }
        });

        //保存
        binding.btnY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(CV.MSG_MAIN_RESULT_CODE, getIntent());
                finish();
            }
        });
    }

    private void Chinese切换(boolean open, TextView textView) {
        if (!open) {
            textView.setText("中文");
            textView.setTextColor(getResources().getColor(R.color.blacky));
        } else {
            textView.setText("英文");
            textView.setTextColor(getResources().getColor(R.color.signal_status_view_bar_green));
        }
    }

    private void hideAndDisplay(boolean open, TextView textView) {
        if (open) {
            textView.setText("隐藏");
            textView.setTextColor(getResources().getColor(R.color.blacky));
        } else {
            textView.setText("显示");
            textView.setTextColor(getResources().getColor(R.color.signal_status_view_bar_green));
        }
    }

    //检测并切换沉浸式模式（也称为“隐藏栏”模式）
    private void hideyBar() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

        // Navigation bar hiding:  Backwards compatible to ICS.

        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }
}