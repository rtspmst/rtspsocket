package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.HideyBarUtils.hideyBar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.AccountPSDBean;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DensityUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.MyToastUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.views.JellyInterpolator;

import java.util.List;

import io.objectbox.Box;

/**
 * 创建账号
 */
public class LoginNewAccountActivity extends BaseActivity implements View.OnClickListener {

    private Button btn_admin_registered;

    private float mWidth, mHeight;
    private int bounds;
    private Drawable eyesOpen;
    private Drawable eyesClosed;
    private Handler mHandler;

    private LinearLayout mName, mPsw, mPsw1;
    private EditText edt_user_name;
    private EditText edt_user_psw_a;
    private EditText edt_user_psw_b;
    private TextView tv_admin;
    private ImageView iv_look_a;
    private ImageView iv_look_b;

    private View progress;
    private View mInputLayout;
    private boolean isChecked1 = true;
    private boolean isChecked2 = true;
    private Box<AccountPSDBean> accountPSDBeanBox;
    private List<AccountPSDBean> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login_new_account);
        mHandler = new Handler();
        initView();
        // 全屏
        hideyBar(this);
        accountPSDBeanBox = MyApplication.getBoxStore().boxFor(AccountPSDBean.class);
        accountList = accountPSDBeanBox.query().build().find();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {
        btn_admin_registered = (Button) findViewById(R.id.btn_admin_registered);
        btn_admin_registered.setText(Language.SUBMIT);
        progress = findViewById(R.id.layout_progress);
        mInputLayout = findViewById(R.id.input_layout);
        iv_look_a = findViewById(R.id.iv_look_a);
        iv_look_b = findViewById(R.id.iv_look_b);
        mName = (LinearLayout) findViewById(R.id.input_layout_name);

        edt_user_name = (EditText) findViewById(R.id.edt_user_name);
        edt_user_name.setHint(Language.USER);
        edt_user_psw_a = (EditText) findViewById(R.id.edt_user_psw_a);
        edt_user_psw_a.setHint(Language.PASSWORD);

        mPsw = (LinearLayout) findViewById(R.id.input_layout_psw);
        mPsw1 = (LinearLayout) findViewById(R.id.input_layout_psw_a);
        edt_user_psw_b = (EditText) findViewById(R.id.edt_user_psw_b);
        edt_user_psw_b.setHint(Language.PASSWORD_AGAIN);

        tv_admin = findViewById(R.id.tv_admin);
        tv_admin.setText(Language.ADD_USER);

        bounds = DensityUtil.dip2px(LoginNewAccountActivity.this, 48);
        eyesOpen = getResources().getDrawable(R.drawable.ic_look_over, getTheme());
        eyesOpen.setBounds(0, 0, bounds, bounds);
        eyesClosed = getResources().getDrawable(R.drawable.ic_look_hide, getTheme());
        eyesClosed.setBounds(0, 0, bounds, bounds);

        btn_admin_registered.setOnClickListener(this);
        iv_look_a.setOnClickListener(this);
        iv_look_b.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_look_a:

                if (!TextUtils.isEmpty(edt_user_psw_a.getText().toString().trim())) {

                    if (isChecked1) {
                        //选择状态 显示明文--设置为可见的密码
                        edt_user_psw_a.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        iv_look_a.setImageDrawable(eyesOpen);
                    } else {
                        //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                        edt_user_psw_a.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        iv_look_a.setImageDrawable(eyesClosed);
                    }

                    isChecked1 = !isChecked1;
                }

                break;

            case R.id.iv_look_b:

                if (!TextUtils.isEmpty(edt_user_psw_b.getText().toString().trim())) {

                    if (isChecked2) {
                        //选择状态 显示明文--设置为可见的密码
                        edt_user_psw_b.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        iv_look_b.setImageDrawable(eyesOpen);
                    } else {
                        //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                        edt_user_psw_b.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        iv_look_b.setImageDrawable(eyesClosed);
                    }

                    isChecked2 = !isChecked2;
                }

                break;

            case R.id.btn_admin_registered:

                btn_admin_registered.setEnabled(false);

                // 计算出控件的高与宽
                mWidth = btn_admin_registered.getMeasuredWidth();
                mHeight = btn_admin_registered.getMeasuredHeight();
                // 隐藏输入框
                mName.setVisibility(View.INVISIBLE);
                mPsw.setVisibility(View.INVISIBLE);
                mPsw1.setVisibility(View.INVISIBLE);

                inputAnimator(mInputLayout, mWidth, mHeight);
                break;
        }
    }

    //延时一秒后保存账号密码
    private void accountSave() {

        btn_admin_registered.setEnabled(true);

        String userName = edt_user_name.getText().toString().trim();
        String userPsw1 = edt_user_psw_a.getText().toString().trim();
        String userPsw2 = edt_user_psw_b.getText().toString().trim();

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPsw1) || TextUtils.isEmpty(userPsw2)) {

            //提示账号密码不能为空
            MyToastUtils.show1();

            recovery();

            return;
        }

        if (userName.length() < LoginActivity.PSD_NUM) {

            //提示帐号不能少于6位
            MyToastUtils.show2();

            recovery();

            return;
        }

        if (userPsw1.length() < LoginActivity.PSD_NUM || userPsw2.length() < LoginActivity.PSD_NUM) {

            //提示密码不能少于6位
            MyToastUtils.show3();

            recovery();

            return;
        }

        if (!userPsw1.equals(userPsw2)) {

            //提示两次密码不一致
            MyToastUtils.show5();

            recovery();

            return;
        }


        for (int i = 0; i < accountList.size(); i++) {

            if (accountList.get(i).getAccount().equals(userName)) {

                //提示账户已存在
                MyToastUtils.show7();

                recovery();

                return;
            }
        }

        //提示账号注册成功
        MyToastUtils.show8();

        AccountPSDBean accountPSDBean = new AccountPSDBean();
        accountPSDBean.setAccount(userName);
        accountPSDBean.setPsd(userPsw1);

        //保存到数据库
        MyApplication.getBoxStore().boxFor(AccountPSDBean.class).put(accountPSDBean);

        Intent intent = new Intent(LoginNewAccountActivity.this, LoginCcountListActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //输入框的动画效果
    private void inputAnimator(final View view, float w, float h) {

        AnimatorSet set = new AnimatorSet();

        ValueAnimator animator = ValueAnimator.ofFloat(0, w);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view
                        .getLayoutParams();
                params.leftMargin = (int) value;
                params.rightMargin = (int) value;
                view.setLayoutParams(params);
            }
        });

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mInputLayout,
                "scaleX", 1f, 0.5f);
        set.setDuration(500);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(animator, animator2);
        set.start();
        set.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                //动画结束后，先显示加载的动画，然后再隐藏输入框
                progress.setVisibility(View.VISIBLE);
                progressAnimator(progress);
                mInputLayout.setVisibility(View.INVISIBLE);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        accountSave();
                    }
                }, 500);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
    }

    //出现进度动画
    private void progressAnimator(final View view) {
        PropertyValuesHolder animator = PropertyValuesHolder.ofFloat("scaleX",
                0.5f, 1f);
        PropertyValuesHolder animator2 = PropertyValuesHolder.ofFloat("scaleY",
                0.5f, 1f);
        ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(view,
                animator, animator2);
        animator3.setDuration(1000);
        //Q弹效果
        animator3.setInterpolator(new JellyInterpolator());
        animator3.start();
    }

    //恢复初始状态
    private void recovery() {
        progress.setVisibility(View.GONE);
        mInputLayout.setVisibility(View.VISIBLE);
        mName.setVisibility(View.VISIBLE);
        mPsw.setVisibility(View.VISIBLE);
        mPsw1.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mInputLayout.getLayoutParams();
        params.leftMargin = 0;
        params.rightMargin = 0;
        mInputLayout.setLayoutParams(params);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mInputLayout, "scaleX", 0.5f, 1f);
        animator2.setDuration(500);
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animator2.start();
    }

}