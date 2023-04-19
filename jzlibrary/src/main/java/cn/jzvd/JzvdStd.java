package cn.jzvd;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;

/**
 * On 2016/04/18 16:15
 */
public class JzvdStd extends Jzvd {

    protected static Timer DISMISS_CONTROL_VIEW_TIMER;

    public ProgressBar bottomProgressBar, loadingProgressBar;
    public TextView titleTextView;
    public ImageView posterImageView;
    public TextView replayTextView;
    public TextView mRetryBtn;
    public LinearLayout mRetryLayout;
    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    public JzvdStd(Context context) {
        super(context);
    }

    public JzvdStd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.jz_layout_std;
    }

    @Override
    public void init(Context context) {
        super.init(context);
        bottomProgressBar = findViewById(R.id.bottom_progress);
        titleTextView = findViewById(R.id.title);
        posterImageView = findViewById(R.id.poster);
        loadingProgressBar = findViewById(R.id.loading);
        replayTextView = findViewById(R.id.replay_text);
        mRetryBtn = findViewById(R.id.retry_btn);
        mRetryLayout = findViewById(R.id.retry_layout);

        posterImageView.setOnClickListener(this);
        mRetryBtn.setOnClickListener(this);
    }

    @Override
    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {
        if ((System.currentTimeMillis() - gobakFullscreenTime) < 200) {
            return;
        }

        if ((System.currentTimeMillis() - gotoFullscreenTime) < 200) {
            return;
        }

        super.setUp(jzDataSource, screen, mediaInterfaceClass);
        titleTextView.setText(jzDataSource.title);
        setScreen(screen);

    }


    @Override
    public void onStateNormal() {
        super.onStateNormal();
        changeUiToNormal();
    }

    @Override
    public void onStatePreparing() {
        super.onStatePreparing();
        changeUiToPreparing();
    }

    @Override
    public void onStatePreparingPlaying() {
        super.onStatePreparingPlaying();
        changeUIToPreparingPlaying();
    }

    @Override
    public void onStatePreparingChangeUrl() {
        super.onStatePreparingChangeUrl();
        changeUIToPreparingChangeUrl();
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        changeUiToPlayingClear();
    }

    @Override
    public void onStatePause() {
        super.onStatePause();
        changeUiToPauseShow();
        cancelDismissControlViewTimer();
    }

    @Override
    public void onStateError() {
        super.onStateError();
        changeUiToError();
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();

        Log.e(TAG, "开启状态自动完成");
        changeUiToComplete();
        cancelDismissControlViewTimer();
        bottomProgressBar.setProgress(1000);
    }

    @Override
    public void startVideo() {
        super.startVideo();
    }

    //doublClick 这两个全局变量只在ontouch中使用，就近放置便于阅读
    private long lastClickTime = 0;
    private long doubleTime = 200;
    private ArrayDeque<Runnable> delayTask = new ArrayDeque<>();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:

                    if (mChangePosition) {
                        long duration = getDuration();

                        int progress = (int) (mSeekTimePosition * 1000 / (duration == 0 ? 1 : duration));
                        bottomProgressBar.setProgress(progress);
                    }

                    //加上延时是为了判断点击是否是双击之一，双击不执行这个逻辑
                    Runnable task = () -> {
                        if (!mChangePosition && !mChangeVolume) {
                            onClickUiToggle();
                        }
                    };
                    v.postDelayed(task, doubleTime + 20);
                    delayTask.add(task);
                    while (delayTask.size() > 2) {
                        delayTask.pollFirst();
                    }

                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastClickTime < doubleTime) {
                        for (Runnable taskItem : delayTask) {
                            v.removeCallbacks(taskItem);
                        }
                        if (state == STATE_PLAYING || state == STATE_PAUSE) {
                            startButton.performClick();
                        }
                    }
                    lastClickTime = currentTimeMillis;
                    break;
                default:
                    break;
            }
        }
        return super.onTouch(v, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.poster) {

            if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }

            if (state == STATE_NORMAL) {

                if (!jzDataSource.getCurrentUrl().toString().startsWith("file") &&
                        !jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                        !JZUtils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                    return;
                }

                startVideo();

            } else if (state == STATE_AUTO_COMPLETE) {

                onClickUiToggle();

            }
        } else if (i == R.id.surface_container) {

            //视频控件点击事件
            startDismissControlViewTimer();

        } else if (i == R.id.retry_btn) {

            if (jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!jzDataSource.getCurrentUrl().toString().startsWith("file") && !
                    jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                    !JZUtils.isWifiConnected(getContext()) && !WIFI_TIP_DIALOG_SHOWED) {
                return;
            }

            addTextureView();

            onStatePreparing();
        }
    }

    @Override
    public void setScreenNormal() {
        super.setScreenNormal();
    }

    @Override
    public void setScreenFullscreen() {
        super.setScreenFullscreen();
        //进入全屏之后要保证原来的播放状态和ui状态不变，改变个别的ui
    }

    @Override
    public void setScreenTiny() {
        super.setScreenTiny();
        setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
    }

    public void onClickUiToggle() {//这是事件

        if (state == STATE_PREPARING) {
            changeUiToPreparing();
            if (bottomContainer.getVisibility() == View.VISIBLE) {
            } else {
            }
        } else if (state == STATE_PLAYING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClear();
            } else {
                changeUiToPlayingShow();
            }
        } else if (state == STATE_PAUSE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPauseClear();
            } else {
                changeUiToPauseShow();
            }
        }
    }

    @Override
    public void onProgress(int playPercentage, long currentPlayingTime, long duration) {
        super.onProgress(playPercentage, currentPlayingTime, duration);

        if (playPercentage != 0) {
            bottomProgressBar.setProgress(playPercentage);
        }
    }

    @Override
    public void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
        if (bufferProgress != 0) {
            bottomProgressBar.setSecondaryProgress(bufferProgress);
        }
    }

    @Override
    public void resetProgressAndTime() {
        super.resetProgressAndTime();
        bottomProgressBar.setProgress(0);
        bottomProgressBar.setSecondaryProgress(0);
    }

    public void changeUiToNormal() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUiToPreparing() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUIToPreparingPlaying() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUIToPreparingChangeUrl() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUiToPlayingShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUiToPlayingClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUiToPauseShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUiToPauseClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUiToComplete() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }
    }

    public void changeUiToError() {
        switch (screen) {
            case SCREEN_NORMAL:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage();
                break;
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
            default:
                break;
        }

    }

    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro,
                                        int posterImg, int bottomPro, int retryLayout) {

        topContainer.setVisibility(topCon);
        bottomContainer.setVisibility(bottomCon);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
        posterImageView.setVisibility(posterImg);

        mRetryLayout.setVisibility(retryLayout);
    }

    public void updateStartImage() {
        if (state == STATE_PLAYING) {
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.jz_click_pause_selector);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_ERROR) {
            startButton.setVisibility(INVISIBLE);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_AUTO_COMPLETE) {
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.jz_click_replay_selector);
            replayTextView.setVisibility(VISIBLE);
        } else {
            startButton.setImageResource(R.drawable.jz_click_play_selector);
            replayTextView.setVisibility(GONE);
        }
    }


    public void cancelDismissControlViewTimer() {
        if (DISMISS_CONTROL_VIEW_TIMER != null) {
            DISMISS_CONTROL_VIEW_TIMER.cancel();
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }

    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        cancelDismissControlViewTimer();
    }

    @Override
    public void reset() {
        super.reset();
        cancelDismissControlViewTimer();

    }

    //启动关闭控制查看计时器
    public void startDismissControlViewTimer() {

        cancelDismissControlViewTimer();
        DISMISS_CONTROL_VIEW_TIMER = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, 10000);
    }


    //定时器 五秒后关闭控制视图
    public class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {

            if (!isCloseAllow) {
                //关闭控制视图
                if (state != STATE_NORMAL && state != STATE_ERROR && state != STATE_AUTO_COMPLETE) {
                    post(() -> {
                        bottomContainer.setVisibility(View.INVISIBLE);
                        topContainer.setVisibility(View.INVISIBLE);
                        startButton.setVisibility(View.INVISIBLE);

                    });
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch1(SeekBar seekBar) {
        super.onStartTrackingTouch1(seekBar);
    }

    @Override
    public void onStopTrackingTouch1(SeekBar seekBar) {
        isCloseAllow = false;

//        Log.e(TAG, "启动关闭控制查看计时器: ");
        startDismissControlViewTimer();
        super.onStopTrackingTouch1(seekBar);
    }

    @Override
    public void MY_ONTOUCHEVENT_ACTION_DOWN(boolean isOnTouch) {

//        Log.e(TAG, "正在触摸进度条: " );
        isCloseAllow = isOnTouch;

    }

    public boolean isCloseAllow = false;//判断是否在触摸进度条 如果在触摸 不允许关闭底部布局


}
