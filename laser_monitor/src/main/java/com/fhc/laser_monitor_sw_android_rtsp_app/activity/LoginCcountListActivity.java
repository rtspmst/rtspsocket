package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.AccountPSDAdapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.AccountPSDBean;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.DensityUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.SharedPreferencesUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.widget.EmptyRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.objectbox.Box;

//账号列表页面
public class LoginCcountListActivity extends BaseActivity {

    @BindView(R.id.tv_back)
    TextView tvBack;

    @BindView(R.id.tv_name_admin)
    TextView tv_name_admin;

    @BindView(R.id.tv_admin_psd)
    TextView tv_admin_psd;

    @BindView(R.id.btn_change_psd_admin)
    Button btn_change_psd_admin;

    @BindView(R.id.iv_look_item_admin)
    ImageView iv_look_item_admin;

    @BindView(R.id.btn_add)
    Button btn_add;

    @BindView(R.id.recylerview)
    EmptyRecyclerView mRecylerView;
    @BindView(R.id.rlEmptyView)
    RelativeLayout mEmptyView;

    private AccountPSDAdapter adapter;
    private Box<AccountPSDBean> accountPSDBeanBox;
    private List<AccountPSDBean> accountList = new ArrayList<>();
    private boolean isShow = false;
    private String user_psd = "";
    private String user_name = "";

    private Drawable eyesOpen;
    private Drawable eyesClosed;
    private int bounds;

    private SharedPreferencesUtil sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_account_list);
        ButterKnife.bind(this);

        //所有成员
        tvBack.setText(Language.ALL_USERS);
        //添加
        btn_add.setText(Language.ADD);
        //修改密码按钮
        btn_change_psd_admin.setText(Language.CHANGE_PSW);
        //初始化SP
        sp = new SharedPreferencesUtil();

        bounds = DensityUtil.dip2px(this, 48);

        eyesOpen = getResources().getDrawable(R.drawable.ic_look_over, getTheme());
        eyesOpen.setBounds(0, 0, bounds, bounds);
        eyesClosed = getResources().getDrawable(R.drawable.ic_look_hide, getTheme());
        eyesClosed.setBounds(0, 0, bounds, bounds);

        hideyBar();

        if (TextUtils.isEmpty(sp.getUserName())) {
            user_name = LoginActivity.USER_NAME;

        } else {
            user_name = sp.getUserName();
        }

        tv_name_admin.setText(user_name);

        if (TextUtils.isEmpty(sp.getPsw())) {
            user_psd = LoginActivity.USER_PSD;
        } else {
            user_psd = sp.getPsw();
        }

        tv_admin_psd.setText(DensityUtil.replaceAction(user_psd));

        iv_look_item_admin.setImageDrawable(eyesClosed);

        iv_look_item_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShow) {
                    //选择状态 显示明文--设置为可见的密码

                    tv_admin_psd.setText(user_psd);
                    iv_look_item_admin.setImageDrawable(eyesOpen);
                } else {
                    tv_admin_psd.setText(DensityUtil.replaceAction(user_psd));
                    iv_look_item_admin.setImageDrawable(eyesClosed);
                }
                isShow = !isShow;
            }
        });

        btn_change_psd_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //修改账号密码
                Intent intent = new Intent(LoginCcountListActivity.this, LoginModifyAccountActivity.class);
                intent.putExtra("FROM", true);
                intent.putExtra("USER_NAME", user_name);
                intent.putExtra("USER_PSD", user_psd);
                startActivityForResult(intent, RESULT_CANCELED);
            }
        });

        accountPSDBeanBox = MyApplication.getBoxStore().boxFor(AccountPSDBean.class);
        accountList = accountPSDBeanBox.query().build().find();

        adapter = new AccountPSDAdapter(accountList, this);
        mRecylerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecylerView.setAdapter(adapter);
        mRecylerView.setmEmptyView(mEmptyView);

        adapter.setOnItemClickListener(new AccountPSDAdapter.OnItemClickListener() {
            @Override
            public void click(int position) {
                //修改账号密码
                Intent intent = new Intent(LoginCcountListActivity.this, LoginModifyAccountActivity.class);
                AccountPSDBean accountPSDBean = accountList.get(position);
                intent.putExtra("FROM", false);
                intent.putExtra("USER_NAME", accountPSDBean.getAccount());
                intent.putExtra("USER_PSD", accountPSDBean.getPsd());
                startActivityForResult(intent, RESULT_FIRST_USER);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_FIRST_USER == resultCode) {
            accountList = MyApplication.getBoxStore().boxFor(AccountPSDBean.class).query().build().find();
            adapter.setmListData(accountList);
        } else if (RESULT_CANCELED == resultCode) {

            if (TextUtils.isEmpty(sp.getUserName())) {
                user_name = LoginActivity.USER_NAME;
            } else {
                user_name = sp.getUserName();
            }
            tv_name_admin.setText(user_name);

            if (TextUtils.isEmpty(sp.getPsw())) {
                user_psd = LoginActivity.USER_PSD;
            } else {
                user_psd = sp.getPsw();
            }

            tv_admin_psd.setText(DensityUtil.replaceAction(user_psd));

            iv_look_item_admin.setImageDrawable(eyesClosed);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideyBar();
        }
    }

    //检测并切换沉浸式模式（也称为“隐藏栏”模式）
    private void hideyBar() {

        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    @OnClick({R.id.tv_back, R.id.btn_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;

            case R.id.btn_add:

                //添加新成员
                //创建账号
                Intent intent1 = new Intent(LoginCcountListActivity.this, LoginNewAccountActivity.class);
                startActivityForResult(intent1, RESULT_FIRST_USER);

                break;
        }
    }
}