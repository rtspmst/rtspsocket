package cn.jzvd;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import cn.jzvd.seekbar.SignSeekBar;

/**
 * Created by Nathen on 16/7/30.
 */
public abstract class Jzvd extends FrameLayout implements View.OnClickListener, SignSeekBar.OnSeekBarChangeListener1, View.OnTouchListener {

    public static final String TAG = "JZVD";
    public static Jzvd CURRENT_JZVD;
    public static LinkedList<ViewGroup> CONTAINER_LIST = new LinkedList<>();

    public static final int SCREEN_NORMAL = 0;
    public static final int SCREEN_FULLSCREEN = 1;
    public static final int SCREEN_TINY = 2;

    public static final int STATE_IDLE = -1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARING_CHANGE_URL = 2;
    public static final int STATE_PREPARING_PLAYING = 3;
    public static final int STATE_PREPARED = 4;
    public static final int STATE_PLAYING = 5;
    public static final int STATE_PAUSE = 6;
    public static final int STATE_AUTO_COMPLETE = 7;
    public static final int STATE_ERROR = 8;

    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT = 1;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP = 2;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL = 3;
    public static boolean TOOL_BAR_EXIST = true;
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static boolean SAVE_PROGRESS = false;
    public static boolean WIFI_TIP_DIALOG_SHOWED = false;
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public static final int THRESHOLD = 80;

    public int state = -1;
    public int screen = -1;
    public JZDataSource jzDataSource;
    public int widthRatio = 0;
    public int heightRatio = 0;
    public Class mediaInterfaceClass;
    public JZMediaInterface mediaInterface;
    public int videoRotation = 0;
    protected long gobakFullscreenTime = 0;//这个应该重写一下，刷新列表，新增列表的刷新，不打断播放，应该是个flag
    protected long gotoFullscreenTime = 0;

    public int seekToManulPosition = -1;
    public long seekToInAdvance = 0;

    public ImageView startButton;
    public SignSeekBar progressBar;

    public TextView currentTimeTextView, totalTimeTextView;
    public ViewGroup textureViewContainer;
    public ViewGroup topContainer, bottomContainer;
    public JZTextureView textureView;

    //更新进度计时器
    protected Timer UPDATE_PROGRESS_TIMER;
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;
    protected ProgressTimerTask mProgressTimerTask;
    protected boolean mTouchingProgressBar;
    protected float mDownX;
    protected float mDownY;
    protected boolean mChangeVolume;
    protected boolean mChangePosition;
    protected long mGestureDownPosition;
    protected long mSeekTimePosition;
    private Context jzvdContext;
    private long my_duration;
    private long currentPlayingTime;//当前播放时间 毫秒

    public Jzvd(Context context) {
        super(context);
        init(context);
    }

    public Jzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public abstract int getLayoutId();

    public void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        startButton = findViewById(R.id.start);

        progressBar = findViewById(R.id.bottom_seek_progress);
        //设置可滑动进度条
        progressBar.setEnabled(false);

        currentTimeTextView = findViewById(R.id.current12);
        totalTimeTextView = findViewById(R.id.total);
        bottomContainer = findViewById(R.id.layout_bottom);
        textureViewContainer = findViewById(R.id.surface_container);
        topContainer = findViewById(R.id.layout_top);

        startButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener1(this);
        bottomContainer.setOnClickListener(this);
        textureViewContainer.setOnClickListener(this);
        textureViewContainer.setOnTouchListener(this);

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

        state = STATE_IDLE;
    }

    public void setData(ArrayList<Long> arrayList) {

        progressBar.setMarkDate(arrayList);

    }

    public void setUp(String url, String title) {
        setUp(new JZDataSource(url, title), SCREEN_NORMAL);
        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (state == STATE_NORMAL) {

            startVideo();

        } else if (state == STATE_PLAYING) {

            mediaInterface.pause();
            onStatePause();

        } else if (state == STATE_PAUSE) {

            mediaInterface.start();
            onStatePlaying();

        } else if (state == STATE_AUTO_COMPLETE) {

            startVideo();

        }
    }

    public void setUp(String url, String title, int screen) {
        setUp(new JZDataSource(url, title), screen);
    }

    public void setUp(JZDataSource jzDataSource, int screen) {
        setUp(jzDataSource, screen, JZMediaSystem.class);
    }

    public void setUp(String url, String title, int screen, Class mediaInterfaceClass) {
        setUp(new JZDataSource(url, title), screen, mediaInterfaceClass);
    }

    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {

        this.jzDataSource = jzDataSource;
        this.screen = screen;
        onStateNormal();
        this.mediaInterfaceClass = mediaInterfaceClass;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {

            if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
                Toast.makeText(getContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (state == STATE_NORMAL) {

                startVideo();

            } else if (state == STATE_PLAYING) {

                mediaInterface.pause();
                onStatePause();

            } else if (state == STATE_PAUSE) {

                mediaInterface.start();
                onStatePlaying();

            } else if (state == STATE_AUTO_COMPLETE) {

                startVideo();

            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    touchActionDown(x, y);

                    break;
                case MotionEvent.ACTION_MOVE:

                    touchActionMove(x, y);

                    break;
                case MotionEvent.ACTION_UP:

                    touchActionUp();

                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private void touchActionUp() {

        mTouchingProgressBar = false;

        if (mChangePosition) {

            mediaInterface.seekTo(mSeekTimePosition);

            progressBar.setProgress1(mGestureDownPosition = getCurrentPositionWhenPlaying());
        }

        startProgressTimer();
    }

    private void touchActionDown(float x, float y) {

        mTouchingProgressBar = true;

        mDownX = x;
        mDownY = y;
        mChangePosition = false;
    }

    private void touchActionMove(float x, float y) {
        float deltaX = x - mDownX;
        float deltaY = y - mDownY;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);
        if (screen == SCREEN_FULLSCREEN) {
            //拖动的是NavigationBar和状态栏
            if (mDownX > JZUtils.getScreenWidth(getContext()) || mDownY < JZUtils.getStatusBarHeight(getContext())) {
                return;
            }
            if (!mChangePosition) {
                if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                    cancelProgressTimer();
                    if (absDeltaX >= THRESHOLD) {
                        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                        // 否则会因为mediaplayer的状态非法导致App Crash
                        if (state != STATE_ERROR) {
                            mChangePosition = true;
                            mGestureDownPosition = getCurrentPositionWhenPlaying();
                        }
                    }
                }
            }
        }
        if (mChangePosition) {
            long totalTimeDuration = getDuration();
            mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
            if (mSeekTimePosition > totalTimeDuration) {
                mSeekTimePosition = totalTimeDuration;
            }

            progressBar.setProgress1(getCurrentPositionWhenPlaying());

        }
    }

    public void onStateNormal() {
        Log.i(TAG, "onStateNormal " + " [" + this.hashCode() + "] ");
        state = STATE_NORMAL;
        cancelProgressTimer();
        if (mediaInterface != null) {
            mediaInterface.release();
        }
    }

    public void onStatePreparing() {
        Log.e(TAG, "onStatePreparing " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARING;
        resetProgressAndTime();
    }

    public void onStatePreparingPlaying() {
        Log.i(TAG, "onStatePreparingPlaying " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARING_PLAYING;
    }

    public void onStatePreparingChangeUrl() {
        Log.i(TAG, "onStatePreparingChangeUrl " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARING_CHANGE_URL;

        releaseAllVideos();
        startVideo();

    }


    public void onPrepared() {
        Log.i(TAG, "onPrepared " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARED;
        if (!preloading) {
            mediaInterface.start();//这里原来是非县城
            preloading = false;
        }
        if (jzDataSource.getCurrentUrl().toString().toLowerCase().contains("mp3") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("wma") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("aac") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("m4a") ||
                jzDataSource.getCurrentUrl().toString().toLowerCase().contains("wav")) {
            onStatePlaying();
        }
    }

    public boolean preloading = false;

    public void startPreloading() {
        preloading = true;
        startVideo();
    }

    /**
     * 如果STATE_PREPARED就播放，如果没准备完成就走正常的播放函数startVideo();
     */
    public void startVideoAfterPreloading() {
        if (state == STATE_PREPARED) {
            mediaInterface.start();
        } else {
            preloading = false;
            startVideo();
        }
    }

    public void onStatePlaying() {
        Log.i(TAG, "onStatePlaying " + " [" + this.hashCode() + "] ");
        if (state == STATE_PREPARED) {//如果是准备完成视频后第一次播放，先判断是否需要跳转进度。
            if (seekToInAdvance != 0) {
                mediaInterface.seekTo(seekToInAdvance);
                seekToInAdvance = 0;
            } else {
                long position = JZUtils.getSavedProgress(getContext(), jzDataSource.getCurrentUrl());
                if (position != 0) {
                    mediaInterface.seekTo(position);//这里为什么区分开呢，第一次的播放和resume播放是不一样的。 这里怎么区分是一个问题。然后
                }
            }
        }
        state = STATE_PLAYING;
        startProgressTimer();
    }

    public void onStatePause() {
        Log.i(TAG, "onStatePause " + " [" + this.hashCode() + "] ");
        state = STATE_PAUSE;
        startProgressTimer();
    }

    public void onStateError() {
        Log.i(TAG, "onStateError " + " [" + this.hashCode() + "] ");
        state = STATE_ERROR;
        cancelProgressTimer();
    }

    public void onStateAutoComplete() {
        Log.i(TAG, "onStateAutoComplete " + " [" + this.hashCode() + "] ");
        state = STATE_AUTO_COMPLETE;
        cancelProgressTimer();
        progressBar.setProgress(100);

        Log.e(TAG, "onStateAutoComplete: ========48513========" + totalTimeTextView.getText());
        currentTimeTextView.setText(totalTimeTextView.getText());
    }

    public static int backUpBufferState = -1;

    public void onInfo(int what, int extra) {
        Log.e(TAG, "onInfo what - " + what + " extra - " + extra);
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {

            Log.e(TAG, "媒体信息视频渲染开始");
            if (state == Jzvd.STATE_PREPARED || state == Jzvd.STATE_PREPARING_CHANGE_URL) {
                onStatePlaying();//开始渲染图像，真正进入playing状态
            }

        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {

            Log.e(TAG, "媒体信息缓冲开始");
            backUpBufferState = state;
            setState(STATE_PREPARING_PLAYING);

        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {

            Log.e(TAG, "媒体信息缓冲结束");
            if (backUpBufferState != -1) {
                setState(backUpBufferState);
                backUpBufferState = -1;
            }

        }
    }

    public void onError(int what, int extra) {
        Log.e(TAG, "onError " + what + " - " + extra + " [" + this.hashCode() + "] ");
        if (what != 38 && extra != -38 && what != -38 && extra != 38 && extra != -19) {
            onStateError();
            mediaInterface.release();
        }
    }

    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        Log.i(TAG, "onAutoCompletion " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();

        onStateAutoComplete();
        mediaInterface.release();
        JZUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), 0);

    }

    public void gotoNormalCompletion() {
        gobakFullscreenTime = System.currentTimeMillis();//退出全屏
        ViewGroup vg = (ViewGroup) (JZUtils.scanForActivity(jzvdContext)).getWindow().getDecorView();
        vg.removeView(this);
        textureViewContainer.removeView(textureView);
        CONTAINER_LIST.getLast().removeAllViews();
        CONTAINER_LIST.getLast().addView(this, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        CONTAINER_LIST.pop();

        setScreenNormal();
        JZUtils.showStatusBar(jzvdContext);
        JZUtils.setRequestedOrientation(jzvdContext, NORMAL_ORIENTATION);
        JZUtils.showSystemUI(jzvdContext);
    }

    /**
     * 多数表现为中断当前播放
     */
    public void reset() {
        Log.i(TAG, "reset " + " [" + this.hashCode() + "] ");
        if (state == STATE_PLAYING || state == STATE_PAUSE) {
            long position = getCurrentPositionWhenPlaying();
            JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), position);
        }
        cancelProgressTimer();

        onStateNormal();
        textureViewContainer.removeAllViews();

        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        JZUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mediaInterface != null) {
            mediaInterface.release();
        }
    }

    /**
     * 里面的的onState...()其实就是setState...()，因为要可以被复写，所以参考Activity的onCreate(),onState..()的方式看着舒服一些，老铁们有何高见。
     *
     * @param state stateId
     */
    public void setState(int state) {

        switch (state) {
            case STATE_NORMAL:
                onStateNormal();
                break;
            case STATE_PREPARING:
                onStatePreparing();
                break;
            case STATE_PREPARING_PLAYING:
                onStatePreparingPlaying();
                break;
            case STATE_PREPARING_CHANGE_URL:
                onStatePreparingChangeUrl();
                break;
            case STATE_PLAYING:
            case STATE_PREPARED:
                onStatePlaying();
                break;
            case STATE_PAUSE:
                onStatePause();
                break;
            case STATE_ERROR:
                onStateError();
                break;
            case STATE_AUTO_COMPLETE:
                onStateAutoComplete();
                break;
            default:
                break;
        }
    }

    public void setScreen(int screen) {//特殊的个别的进入全屏的按钮在这里设置  只有setup的时候能用上
        switch (screen) {
            case SCREEN_NORMAL:
                setScreenNormal();
                break;
            case SCREEN_FULLSCREEN:
                setScreenFullscreen();
                break;
            case SCREEN_TINY:
                setScreenTiny();
                break;
            default:
                break;
        }
    }

    public void startVideo() {
        Log.e(TAG, "startVideo [" + this.hashCode() + "] ");
        setCurrentJzvd(this);
        try {
            Constructor<JZMediaInterface> constructor = mediaInterfaceClass.getConstructor(Jzvd.class);
            this.mediaInterface = constructor.newInstance(this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        addTextureView();
        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        JZUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        onStatePreparing();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (screen == SCREEN_FULLSCREEN || screen == SCREEN_TINY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    public void addTextureView() {
        Log.e(TAG, "addTextureView [" + this.hashCode() + "] ");
        if (textureView != null) {
            textureViewContainer.removeView(textureView);
        }
        textureView = new JZTextureView(getContext().getApplicationContext());
        textureView.setSurfaceTextureListener(mediaInterface);

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(textureView, layoutParams);
    }

    public void onVideoSizeChanged(int width, int height) {
        Log.i(TAG, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        if (textureView != null) {
            if (videoRotation != 0) {
                textureView.setRotation(videoRotation);
            }
            textureView.setVideoSize(width, height);
        }
    }

    //获取视频时长
    public long getLong() {
        return my_duration;
    }


    //获取当前播放时间
    public long getCurrentPlayingTime() {

        return currentPlayingTime;
    }

    public void startProgressTimer() {

        cancelProgressTimer();

        UPDATE_PROGRESS_TIMER = new Timer();

        //获取总时间 60140 一分钟
        my_duration = getDuration();

        mProgressTimerTask = new ProgressTimerTask();

        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 100);
    }

    public void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    public void onProgress(int playPercentage, long position, long duration) {

        if (!mTouchingProgressBar) {
            if (seekToManulPosition != -1) {
                if (seekToManulPosition > playPercentage) {
                    return;
                } else {
                    seekToManulPosition = -1;//这个关键帧有没有必要做
                }
            } else {
                if (playPercentage != 0) {
                    progressBar.setProgress(playPercentage);
                }
            }
        }

        if (position != 0) {
            currentTimeTextView.setText(JZUtils.stringForTime(position));
            //设置可滑动进度条
            progressBar.setEnabled(true);
            currentPlayingTime = position;

//            Log.e(TAG, "当前播放时间===" + currentPlayingTime);

        }


        totalTimeTextView.setText(JZUtils.stringForTime(duration));

        //设置进度条跟着视频进度一块走
        if (playPercentage != 0) {
            progressBar.setProgress1(playPercentage);
        }

        if (position >= getDuration() - 1000) {
            //设置可滑动进度条
            progressBar.setEnabled(false);
            progressBar.setProgress(0);
        }
    }

    public void setBufferProgress(int bufferProgress) {
        if (bufferProgress != 0) {
            progressBar.setProgress(bufferProgress);
        }
    }

    public void resetProgressAndTime() {
        progressBar.setProgress(0);
        currentTimeTextView.setText(JZUtils.stringForTime(0));
        totalTimeTextView.setText(JZUtils.stringForTime(0));
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (state == STATE_PLAYING || state == STATE_PAUSE || state == STATE_PREPARING_PLAYING) {
            try {
                position = mediaInterface.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public long getDuration() {
        long duration = 0;
        try {

            if (mediaInterface != null) {
                duration = mediaInterface.getDuration();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }


    public void cloneAJzvd(ViewGroup vg) {
        try {
            Constructor<Jzvd> constructor = (Constructor<Jzvd>) Jzvd.this.getClass().getConstructor(Context.class);
            Jzvd jzvd = constructor.newInstance(getContext());
            jzvd.setId(getId());
            vg.addView(jzvd);
            jzvd.setUp(jzDataSource.cloneMe(), SCREEN_NORMAL, mediaInterfaceClass);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void gotoScreenFullscreen() {
        gotoFullscreenTime = System.currentTimeMillis();
        jzvdContext = ((ViewGroup) getParent()).getContext();
        ViewGroup vg = (ViewGroup) getParent();
        vg.removeView(this);
        cloneAJzvd(vg);
        CONTAINER_LIST.add(vg);
        vg = (ViewGroup) (JZUtils.scanForActivity(jzvdContext)).getWindow().getDecorView();

        vg.addView(this, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setScreenFullscreen();
        JZUtils.hideStatusBar(jzvdContext);
        JZUtils.setRequestedOrientation(jzvdContext, FULLSCREEN_ORIENTATION);
        JZUtils.hideSystemUI(jzvdContext);//华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326

    }

    public void setScreenNormal() {//TODO 这块不对呀，还需要改进，设置flag之后要设置ui，不设置ui这么写没意义呀
        screen = SCREEN_NORMAL;
    }

    public void setScreenFullscreen() {
        screen = SCREEN_FULLSCREEN;
    }

    public void setScreenTiny() {
        screen = SCREEN_TINY;
    }

    //    //重力感应的时候调用的函数，、、这里有重力感应的参数，暂时不能删除
    public void autoFullscreen(float x) {//TODO写道demo中
        if (CURRENT_JZVD != null
                && (state == STATE_PLAYING || state == STATE_PAUSE)
                && screen != SCREEN_FULLSCREEN
                && screen != SCREEN_TINY) {
            if (x > 0) {
                JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
            gotoScreenFullscreen();
        }
    }


    //TODO 是否有用
    public void onSeekComplete() {

    }


    public Context getApplicationContext() {//这个函数必要吗
        Context context = getContext();
        if (context != null) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext != null) {
                return applicationContext;
            }
        }
        return context;
    }


    //更新进度计时器 每一百毫秒刷新一次
    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (state == STATE_PLAYING || state == STATE_PAUSE || state == STATE_PREPARING_PLAYING) {

                post(() -> {
                    long position = getCurrentPositionWhenPlaying();
                    long duration = getDuration();


                    if (0 != duration) {

                        int playPercentage = (int) (((float) position / duration) * 1000);

//                        Log.e(TAG, "播放千分比dsa ---- " + playPercentage);

                        onProgress(playPercentage, position, duration);
                    }
                });
            }
        }
    }

    public static AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();

                    Log.e(TAG, "AUDIOFOCUS_LOSS 听力损失 [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                    Log.e(TAG, "onAudioFocusChange:视听丢失瞬态 ");
                    try {
                        Jzvd player = CURRENT_JZVD;
                        if (player != null && player.state == Jzvd.STATE_PLAYING) {
                            player.startButton.performClick();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.e(TAG, "onAudioFocusChange:AUDIOFOCUS 损失瞬态 CAN DUCK ");
                    break;
                default:
                    break;
            }
        }
    };


    public static void releaseAllVideos() {
        Log.e(TAG, "releaseAllVideos");
        if (CURRENT_JZVD != null) {
            CURRENT_JZVD.reset();
            CURRENT_JZVD = null;
        }
    }


    public static void setCurrentJzvd(Jzvd jzvd) {
        if (CURRENT_JZVD != null) {
            CURRENT_JZVD.reset();
        }
        CURRENT_JZVD = jzvd;
    }

    //设置纹理视图旋转
    public static void setTextureViewRotation(int rotation) {
        if (CURRENT_JZVD != null && CURRENT_JZVD.textureView != null) {
            CURRENT_JZVD.textureView.setRotation(rotation);
        }
    }

    //设置视频图像显示类型
    public static void setVideoImageDisplayType(int type) {
        Jzvd.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (CURRENT_JZVD != null && CURRENT_JZVD.textureView != null) {
            CURRENT_JZVD.textureView.requestLayout();
        }
    }


    @Override
    public void onStartTrackingTouch1(SeekBar seekBar) {

        //进度条开始 在开始跟踪触摸 回调
    }

    @Override
    public void onStopTrackingTouch1(SeekBar seekBar) {


        //进度条停止触摸回调

    }

    @Override
    public void MY_ONTOUCHEVENT_ACTION_DOWN(boolean isOnTouch) {


    }

    @Override
    public void onProgressChanged1(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            //设置这个progres对应的时间，给textview
            long duration = getDuration();

            currentTimeTextView.setText(JZUtils.stringForTime(progress * duration / 1000));

            //设置到某个时间段
            long time = seekBar.getProgress() * getDuration() / 1000;
            seekToManulPosition = seekBar.getProgress();

            if (mediaInterface != null) {
                mediaInterface.seekTo(time);
            }
        }
    }
}
