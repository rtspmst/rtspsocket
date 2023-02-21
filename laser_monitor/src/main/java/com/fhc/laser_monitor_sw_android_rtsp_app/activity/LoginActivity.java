package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

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

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.AccountPSDBean;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.CV;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DensityUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.MyToastUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.SharedPreferencesUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.views.JellyInterpolator;

import java.util.List;

import io.objectbox.Box;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private float mWidth, mHeight;

    public static String USER_NAME = "admin0";
    public static String USER_PSD = "123456";
    public static int PSD_NUM = 6;

    private LinearLayout mName, mPsw;
    private EditText edt_user_name;
    private EditText edt_user_psw;
    private ImageView iv_look;
    private Button mBtnLogin;
    private Button btnSwitchEnglish;

    private int bounds;

    private Drawable eyesOpen;
    private Drawable eyesClosed;
    private Handler mHandler;

    private Button btn_change_password;
    private Button btn_create_ccount;
    private View progress;
    private View mInputLayout;
    private String key;
    private String psd;
    private Box<AccountPSDBean> accountPSDBeanBox;
    private List<AccountPSDBean> accountList;
    private String userName;
    private String userPsw;
    private boolean loginSuccessful;//普通会员登录成功
    private boolean adminLoginSuccessful;//管理员登录成功

    private SharedPreferencesUtil sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化SP
        sp = new SharedPreferencesUtil();

        if (sp.getReset()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // 全屏
        hideyBar(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        mHandler = new Handler();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {

        mName = (LinearLayout) findViewById(R.id.input_layout_name);
        mPsw = (LinearLayout) findViewById(R.id.input_layout_psw);

        mBtnLogin = (Button) findViewById(R.id.main_btn_login);
        btnSwitchEnglish = (Button) findViewById(R.id.btnSwitchEnglish);
        progress = findViewById(R.id.layout_progress);
        mInputLayout = findViewById(R.id.input_layout);
        iv_look = findViewById(R.id.iv_look);
        btn_change_password = findViewById(R.id.btn_change_password);
        btn_create_ccount = findViewById(R.id.btn_create_ccount);
        edt_user_name = (EditText) findViewById(R.id.edt_user_name);
        edt_user_psw = (EditText) findViewById(R.id.edt_user_psw);

        extracted();

        bounds = DensityUtil.dip2px(LoginActivity.this, 48);

        eyesOpen = getResources().getDrawable(R.drawable.ic_look_over, getTheme());
        eyesOpen.setBounds(0, 0, bounds, bounds);
        eyesClosed = getResources().getDrawable(R.drawable.ic_look_hide, getTheme());
        eyesClosed.setBounds(0, 0, bounds, bounds);

        mBtnLogin.setOnClickListener(this);
        iv_look.setOnClickListener(this);

        btn_change_password.setOnClickListener(this);
        btn_create_ccount.setOnClickListener(this);

        btnSwitchEnglish.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                int decodeInt = MyApplication.getMMKV().decodeInt(CV.LANGUAGE, 1);

                if (1 == decodeInt) {

                    btnSwitchEnglish.setText("English");
                    MyApplication.getMMKV().encode(CV.LANGUAGE, 2);

                } else {

                    btnSwitchEnglish.setText("Indonesian");
                    MyApplication.getMMKV().encode(CV.LANGUAGE, 1);
                }

                Language.drfe();

                extracted();

                return false;
            }
        });
    }

    private void extracted() {

        int decodeInt = MyApplication.getMMKV().decodeInt(CV.LANGUAGE, 1);

        if (1 == decodeInt) {
            btnSwitchEnglish.setText("Indonesian");
        } else {
            btnSwitchEnglish.setText("English");
        }

        edt_user_name.setHint(Language.USER);
        edt_user_psw.setHint(Language.PASSWORD);

        edt_user_name.setText(sp.getLastAccount());
        //登陆
        mBtnLogin.setText(Language.LOGIN);
        //修改密码
        btn_change_password.setText(Language.CHANGE_PSW);
        //账户管理
        btn_create_ccount.setText(Language.ACCOUNT_MANAGEMENT);
    }

    boolean isChecked = true;

    private int onClick123 = 0;//1登录 2 修改密码 3管理账号

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_look:

                if (!TextUtils.isEmpty(edt_user_psw.getText().toString().trim())) {

                    if (isChecked) {
                        //选择状态 显示明文--设置为可见的密码
                        edt_user_psw.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        iv_look.setImageDrawable(eyesOpen);
                    } else {
                        //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                        edt_user_psw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        iv_look.setImageDrawable(eyesClosed);
                    }

                    isChecked = !isChecked;
                }

                break;

            case R.id.main_btn_login:

                onClick123 = 1;

                //判断用户是否合法
                if (judgmentUser()) {
                    return;
                }

//                loginSuccessful = true;

                mBtnLogin.setEnabled(false);
                // 计算出控件的高与宽
                mWidth = mBtnLogin.getMeasuredWidth();
                mHeight = mBtnLogin.getMeasuredHeight();
                // 隐藏输入框
                mName.setVisibility(View.INVISIBLE);
                mPsw.setVisibility(View.INVISIBLE);

                inputAnimator(mInputLayout, mWidth, mHeight);

                break;

            case R.id.btn_change_password:

                onClick123 = 2;
                //判断用户是否合法
                if (judgmentUser()) {
                    return;
                }

//                loginSuccessful = true;

                btn_change_password.setEnabled(false);
                // 计算出控件的高与宽
                mWidth = mBtnLogin.getMeasuredWidth();
                mHeight = mBtnLogin.getMeasuredHeight();
                // 隐藏输入框
                mName.setVisibility(View.INVISIBLE);
                mPsw.setVisibility(View.INVISIBLE);

                inputAnimator(mInputLayout, mWidth, mHeight);

                break;

            case R.id.btn_create_ccount:
                onClick123 = 3;

                //判断用户是否合法
                if (judgmentUser()) {
                    return;
                }

//                adminLoginSuccessful = true;

                btn_create_ccount.setEnabled(false);
                // 计算出控件的高与宽
                mWidth = mBtnLogin.getMeasuredWidth();
                mHeight = mBtnLogin.getMeasuredHeight();
                // 隐藏输入框
                mName.setVisibility(View.INVISIBLE);
                mPsw.setVisibility(View.INVISIBLE);

                inputAnimator(mInputLayout, mWidth, mHeight);

                break;
        }
    }

    //判断用户是否合法
    private boolean judgmentUser() {
        key = sp.getUserName();
        psd = sp.getPsw();

        userName = edt_user_name.getText().toString().trim();
        userPsw = edt_user_psw.getText().toString().trim();


        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPsw)) {

            //提示帐号密码不能为空
            MyToastUtils.show1();

            recovery();

            return true;
        }

        if (userName.length() < PSD_NUM) {

            //提示帐号不能少于6位
            MyToastUtils.show2();

            recovery();

            return true;
        }

        if (userPsw.length() < PSD_NUM) {

            //提示密码不能少于6位
            MyToastUtils.show3();

            recovery();

            return true;
        }

        if (TextUtils.isEmpty(key) && TextUtils.isEmpty(psd)) {
            //未修改过密码时 账号密码输入正确

            if (USER_NAME.equals(userName) && USER_PSD.equals(userPsw)) {
                //账号密码匹配成功 调整找主页面
                adminLoginSuccessful = true;
            }
        } else {
            //修改过原始密码以后
            if (key.equals(userName) && psd.equals(userPsw)) {
                //账号密码正确 登录成功
                adminLoginSuccessful = true;
            }
        }

        accountPSDBeanBox = MyApplication.getBoxStore().boxFor(AccountPSDBean.class);
        accountList = accountPSDBeanBox.query().build().find();
        for (int i = 0; i < accountList.size(); i++) {

            if (accountList.get(i).getAccount().equals(userName) && accountList.get(i).getPsd().equals(userPsw)) {
                //账号密码正确 登录成功
                loginSuccessful = true;
            }
        }
        return false;
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
        set.setDuration(800);
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
//                progressAnimator(progress);
                mInputLayout.setVisibility(View.INVISIBLE);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //延时一秒后 账号密码对比
                        if (loginSuccessful) {
                            //保存账号
                            sp.saveLastAccount(userName);

                            switch (onClick123) {
                                case 1:
                                    //登录
                                    //有账号密码匹配成功
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                                    finish();
                                    break;
                                case 2:
                                    //修改密码
                                    //子账号修改密码
                                    Intent intent = new Intent(LoginActivity.this, LoginModifyAccountActivity.class);
                                    intent.putExtra("FROM", true);
                                    intent.putExtra("USER_NAME", userName);
                                    intent.putExtra("USER_PSD", userPsw);
                                    intent.putExtra("FROM", adminLoginSuccessful);
                                    startActivityForResult(intent, RESULT_CANCELED);
                                    recovery();
                                    clearData();
                                    break;
                                case 3:
                                    //管理账号
                                    recovery();
                                    clearData();
//                                    ToastUtils.showShort("account does not have permission");
                                    break;
                            }


                        } else if (adminLoginSuccessful) {
                            //保存账号
                            sp.saveLastAccount(userName);

                            switch (onClick123) {
                                case 1:

                                    //登录
                                    //有账号密码匹配成功
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                                    finish();
                                    break;
                                case 2:

                                    //管理员修改密码修改密码
                                    Intent intent = new Intent(LoginActivity.this, LoginModifyAccountActivity.class);
                                    intent.putExtra("FROM", true);
                                    intent.putExtra("USER_NAME", userName);
                                    intent.putExtra("USER_PSD", userPsw);
                                    intent.putExtra("FROM", adminLoginSuccessful);
                                    startActivityForResult(intent, RESULT_CANCELED);
                                    recovery();
                                    clearData();
                                    break;
                                case 3:

                                    //管理账号
                                    Intent intent1 = new Intent(LoginActivity.this, LoginCcountListActivity.class);
                                    startActivity(intent1);
                                    recovery();
                                    clearData();
                                    break;
                            }

                        } else {

                            recovery();

                            clearData();

                            //提示帐号或密码错误
                            MyToastUtils.show4();
                        }
                    }
                }, 700);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
    }

    private void clearData() {
        onClick123 = 0;
        userName = "";
        userPsw = "";
        edt_user_psw.setText("");
        loginSuccessful = false;
        adminLoginSuccessful = false;

        mBtnLogin.setEnabled(true);
        btn_create_ccount.setEnabled(true);
        btn_change_password.setEnabled(true);
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