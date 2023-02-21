package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.BootLog1Adapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.BootLog2Adapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.Student;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.TXTManager;
import com.fhc.laser_monitor_sw_android_rtsp_app.widget.EmptyRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.objectbox.Box;

import static com.fhc.laser_monitor_sw_android_rtsp_app.utils.FileUtil.getMoviePath;

//开机日志记录
public class BootLogActivity extends BaseActivity {

    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_number)
    TextView tvNumber;
    @BindView(R.id.tv_previous_page)
    Button tvPreviousPage;
    @BindView(R.id.tv_next_page)
    Button tvNextPage;
    @BindView(R.id.recylerview)
    EmptyRecyclerView mRecylerView;
    @BindView(R.id.rlEmptyView)
    RelativeLayout mEmptyView;

    private BootLog1Adapter log1Adapter;
    private BootLog2Adapter log2Adapter;
    private Box<Student> studentBox;
    private List<Student> students = new ArrayList<>();
    private long count = 0;

    private int pageFirst = 0;
    private int pageEnd = 1000;
    private List<String> strings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_log_boot);
        ButterKnife.bind(this);

        hideyBar();

        String from = getIntent().getStringExtra("from");

        if ("1".equals(from)) {
            studentBox = MyApplication.getBoxStore().boxFor(Student.class);
            count = studentBox.count();

            tvNumber.setText("( 共 " + (count / pageEnd + 1) + " 页 ）" + " 当前页 : " + (pageFirst + 1));

            students = studentBox.query().build().find(pageFirst, (pageFirst * pageEnd) == 0 ? pageEnd : pageFirst * pageEnd);
            //全部
            //students = studentBox.query().build().find();
            //改为倒序
            Collections.reverse(students);

            log1Adapter = new BootLog1Adapter(students, this);
            mRecylerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mRecylerView.setAdapter(log1Adapter);
            mRecylerView.setmEmptyView(mEmptyView);
            tvPreviousPage.setVisibility(View.VISIBLE);
            tvNextPage.setVisibility(View.VISIBLE);

        } else {

            String s = TXTManager.getInstance().readFromXML(getMoviePath() + TXTManager.fileName);

            strings = Arrays.asList(s.split(","));

            tvNumber.setText("( 共 " + (strings.size() - 1) + " 条 ）");

            log2Adapter = new BootLog2Adapter(strings, this);

            mRecylerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mRecylerView.setAdapter(log2Adapter);
            mRecylerView.setmEmptyView(mEmptyView);
            tvPreviousPage.setVisibility(View.GONE);
            tvNextPage.setVisibility(View.GONE);
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

    @OnClick({R.id.tv_back, R.id.tv_previous_page, R.id.tv_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_previous_page:

                //上一页

                if (pageFirst >= 1) {
                    pageFirst--;
                    count = studentBox.count();
                    tvNumber.setText("( 共 " + (count / pageEnd + 1) + " 页 ）" + " 当前页 : " + (pageFirst + 1));
                    students = studentBox.query().build().find(pageFirst * pageEnd, pageEnd);
                    Collections.reverse(students);
                    log1Adapter.setmListData(students);
                }

                break;
            case R.id.tv_next_page:

                //下一页

                count = studentBox.count();

                if (pageFirst < (count / pageEnd)) {
                    pageFirst++;
                }

                tvNumber.setText("( 共 " + (count / pageEnd + 1) + " 页 ）" + " 当前页 : " + (pageFirst + 1));
                students = studentBox.query().build().find(pageFirst * pageEnd, pageEnd);
                Collections.reverse(students);
                log1Adapter.setmListData(students);
                break;
            default:
                break;
        }
    }
}