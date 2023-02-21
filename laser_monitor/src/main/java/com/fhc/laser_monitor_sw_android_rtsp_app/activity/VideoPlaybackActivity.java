package com.fhc.laser_monitor_sw_android_rtsp_app.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.adapter.PathAdapter;
import com.fhc.laser_monitor_sw_android_rtsp_app.bean.ItemModel;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.FileUtils;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.Language;
import com.fhc.laser_monitor_sw_android_rtsp_app.widget.EmptyRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 录像回放
 */
public class VideoPlaybackActivity extends BaseActivity {

    private EmptyRecyclerView mRecylerView;
    private RelativeLayout mEmptyView;
    private List<ItemModel> mDataList = new ArrayList<>();
    private PathAdapter mPathAdapter;
    private TextView tvBack;
    private HashSet<Integer> positionSet = new HashSet<>();
    private boolean isSelectAll = true;
    private TextView btnSelectAll;
    public static boolean isPlayback = true;
    private TextView tvDelete;
    private TextView tvEmpty;


    @SuppressLint("SetTextI18n")
    private void setLanguage() {

        tvBack.setText(Language.VIDEO_PLAYBACK);
        tvDelete.setText(Language.DELETE);
        btnSelectAll.setText(Language.SELECT_ALL);
        tvEmpty.setText(Language.NO_DATA);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play_back);

        initView();

        hideyBar();

        setLanguage();

        initToolbar();

        String mPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ShunFengEr/movie";
        String mPath0 = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/ShunFengEr/picture";
        List<File> mListFiles = getFileList(mPath, mPath0);

        for (int i = 0; i < mListFiles.size(); i++) {
            ItemModel itemModel = new ItemModel();
            itemModel.file = mListFiles.get(i);
            itemModel.isSelect = false;
            mDataList.add(itemModel);
//            refresh(mListFiles.get(i).getAbsolutePath());
        }

//        for (File file : mListFiles) {
//            ItemModel itemModel = new ItemModel();
//            itemModel.file = file;
//            itemModel.isSelect = false;
//            mDataList.add(itemModel);
//            refresh(file.getAbsolutePath());
//        }

        mPathAdapter = new PathAdapter(mDataList, this);
        mRecylerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecylerView.setAdapter(mPathAdapter);
        mRecylerView.setmEmptyView(mEmptyView);

        initListener();
    }

    private void refresh(String filePath) {
        Uri localUri = Uri.fromFile(new File(filePath));
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
        sendBroadcast(localIntent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideyBar();
        }
    }

    //更新Toolbar展示
    private void initToolbar() {
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlayback = false;
                finish();
            }
        });
    }

    private void initListener() {

        mPathAdapter.setOnItemClickListener(new PathAdapter.OnItemClickListener() {
            @Override
            public void click(int position, boolean playVideoFlag) {

                final ItemModel itemModel = mDataList.get(position);
                final String path = itemModel.file.getAbsolutePath();

                if (playVideoFlag) {

                    if (path != null && path.endsWith(".mp4")) {

                        /* play this video */
                        openVideoFileIntent(path, itemModel.file.getName());

                    } else if (path != null && path.endsWith(".jpg")) {

                        Intent intent = new Intent(VideoPlaybackActivity.this, PictureZoomActivity.class);
                        intent.putExtra("path", path);
                        startActivity(intent);

                    }

                } else {
                    // cancel if it is chosen,or add it
                    if (positionSet.contains(position)) {
                        positionSet.remove(position);
                        itemModel.isSelect = false; //modify the state
                    } else {
                        positionSet.add(position);
                        itemModel.isSelect = true; //modify the state
                    }
                }
                mDataList.set(position, itemModel); //save the state
            }
        });
    }

    private void openVideoFileIntent(String path, String name) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Uri contentUri = FileProvider.getUriForFile(VideoPlaybackActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", new File(path));
//            intent.setDataAndType(contentUri, "video/*");
//        } else {
//            intent.setDataAndType(Uri.fromFile(new File(path)), "video/*");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        startActivity(intent);

        Intent intent = new Intent(this, VideoPlayActivity.class);
        intent.putExtra("URL", path);
        intent.putExtra("NAME", name);
        startActivity(intent);

    }

    /*
     * 根据地址获取当前地址下的所有目录和文件，并且排序
     * @param path
     * @return List<File>
     */
    private List<File> getFileList(String path, String path0) {
        return FileUtils.getFileListByDirPath(path, path0, null);
    }

    private void initView() {
        mRecylerView = findViewById(R.id.recylerview);
        mEmptyView = findViewById(R.id.rlEmptyView);
        tvBack = findViewById(R.id.tvBack);
        tvDelete = findViewById(R.id.tvDelete);
        tvEmpty = findViewById(R.id.tvEmpty);

        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectAll) {
                    isSelectAll = false;

                    Drawable topDrawable = getResources().getDrawable(R.drawable.ic_select_all_pressed, getTheme());
                    topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
                    btnSelectAll.setCompoundDrawables(null, topDrawable, null, null);
                    btnSelectAll.setTextColor(0xFFDA4336);
                    //取消
                    btnSelectAll.setText(Language.CANCEL);

                    for (int i = 0; i < mDataList.size(); i++) {
                        mDataList.get(i).isSelect = true;
                        positionSet.add(i);
                    }
                } else {
                    isSelectAll = true;

                    Drawable topDrawable = getResources().getDrawable(R.drawable.ic_select_all, getTheme());
                    topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
                    btnSelectAll.setCompoundDrawables(null, topDrawable, null, null);
                    btnSelectAll.setTextColor(0xFFFFFFFF);
                    //全选
                    btnSelectAll.setText(Language.SELECT_ALL);
                    for (int i = 0; i < mDataList.size(); i++) {
                        mDataList.get(i).isSelect = false;
                        positionSet.remove(i);
                    }
                }
                mPathAdapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.tvDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positionSet.isEmpty()) {
                    //请至少选择一个文件
                    ToastUtils.showShort(Language.SELECT_FILE);
                } else {

                    long secondTime = System.currentTimeMillis();
                    if (secondTime - firstTime > 2000) {
                        //再按一次删除文件
                        ToastUtils.showShort(Language.CLICK_AGAIN_DELETE);
                        firstTime = secondTime;

                    } else {
                        delete();
                        // reset the ICON after delete operations is finished.
                        Drawable topDrawable = getResources().getDrawable(R.drawable.ic_select_all, getTheme());
                        topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
                        btnSelectAll.setCompoundDrawables(null, topDrawable, null, null);
                        btnSelectAll.setTextColor(0xFFFFFFFF);
                        //"全选"
                        btnSelectAll.setText(Language.SELECT_ALL);
                        //删除成功
                        ToastUtils.showShort(Language.DELETE_SUCCESS);
                    }
                }
            }
        });
    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    private void delete() {
        HashSet<ItemModel> valueSet = new HashSet<>();
        for (Integer integer : positionSet) {
            valueSet.add(mDataList.get(integer));
        }

        for (ItemModel itemModel : valueSet) {

            String fileName = itemModel.file.getAbsolutePath();
            File f = new File(fileName);
            if (!f.exists()) {
                return;
            }
            f.delete();

            String pcmFileName = fileName.replaceFirst("mp4", "pcm");
            File pcmFile = new File(pcmFileName.replaceFirst("mux", "pcm"));
            if (pcmFile.exists()) {
                pcmFile.delete();
            }

            mDataList.remove(itemModel);
        }
        mPathAdapter.notifyDataSetChanged();
        positionSet.clear();
    }


    //检测并切换沉浸式模式（也称为“隐藏栏”模式）
    private void hideyBar() {

        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
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
}
