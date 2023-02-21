package com.fhc.laser_monitor_sw_android_rtsp_app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.fhc.laser_monitor_sw_android_rtsp_app.MyApplication;
import com.fhc.laser_monitor_sw_android_rtsp_app.R;
import com.fhc.laser_monitor_sw_android_rtsp_app.activity.MainActivity;
import com.fhc.laser_monitor_sw_android_rtsp_app.utils.FileUtil;
import com.fhc.laser_monitor_sw_android_rtsp_app.views.AngleView;

import org.easydarwin.video.Client;
import org.easydarwin.video.EasyPlayerClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

import uk.copywitchshame.senab.photoview.gestures.PhotoViewAttacher;


/**
 * 播放器Fragment
 */
public class PlayFragment extends Fragment implements TextureView.SurfaceTextureListener, PhotoViewAttacher.OnMatrixChangedListener {
    protected static final String TAG = "PlayFragment";

    public static final String KEY = MyApplication.RTSP_KEY;

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_TRANSPORT_MODE = "ARG_TRANSPORT_MODE";
    public static final String ARG_SEND_OPTION = "ARG_SEND_OPTION";
    public static final String ARG_PARAM3 = "param3";

    public static final int RESULT_REND_START = 1;
    public static final int RESULT_REND_VIDEO_DISPLAY = 2;
    public static final int RESULT_REND_STOP = -1;

    // 等比例,最大化区域显示,不裁剪
    public static final int ASPECT_RATIO_INSIDE = 1;
    // 等比例,裁剪,裁剪区域可以通过拖拽展示\隐藏
    public static final int ASPECT_RATIO_CROPS_MATRIX = 2;
    // 等比例,最大区域显示,裁剪
    public static final int ASPECT_RATIO_CENTER_CROPS = 3;
    // 拉伸显示,铺满全屏
    public static final int FILL_WINDOW = 4;

    private int mRatioType = ASPECT_RATIO_INSIDE;

    protected String mUrl;
    protected int mType;// 0或1表示TCP，2表示UDP
    protected int sendOption;

    private ResultReceiver mRR;// ResultReceiver是一个用来接收其他进程回调结果的通用接口 创建时为null

    protected EasyPlayerClient mStreamRender;
    protected ResultReceiver mResultReceiver;//结果接收器

    protected int mWidth;
    protected int mHeight;

    protected View.OnLayoutChangeListener listener;

    private PhotoViewAttacher mAttacher;
    private AngleView mAngleView; //渲染角度图 隐藏了

    private ImageView mTakePictureThumb;// 显示抓拍的图片
    protected TextureView mSurfaceView;

    protected ImageView cover;//背景图
    private ImageView mRenderCover;//这是一个

    private MediaScannerConnection mScanner;//媒体扫描仪连接

    private AsyncTask<Void, Void, Bitmap> mLoadingPictureThumbTask;

    private OnDoubleTapListener doubleTapListener;
    private SurfaceTexture surface;

    public static PlayFragment newInstance(String url, int transportMode, int sendOption, ResultReceiver rr) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, url);
        args.putInt(ARG_TRANSPORT_MODE, transportMode);
        args.putInt(ARG_SEND_OPTION, sendOption);
        args.putParcelable(ARG_PARAM3, rr);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_PARAM1);
            mType = getArguments().getInt(ARG_TRANSPORT_MODE);
            sendOption = getArguments().getInt(ARG_SEND_OPTION);
            mRR = getArguments().getParcelable(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play, container, false);
        cover = (ImageView) view.findViewById(R.id.surface_cover);

//        if (!TextUtils.isEmpty(mUrl)) {
//            Glide.with(this)
//                    .load(FileUtil.getSnapFile(mUrl))
//                    .signature(new ObjectKey(UUID.randomUUID().toString()))
//                    .fitCenter()
//                    .into(cover);
//        }

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSurfaceView = (TextureView) view.findViewById(R.id.surface_view);
        mSurfaceView.setOpaque(false);
        mSurfaceView.setSurfaceTextureListener(this);

        mAngleView = (AngleView) getView().findViewById(R.id.render_angle_view);
        mRenderCover = (ImageView) getView().findViewById(R.id.surface_cover);
        mTakePictureThumb = (ImageView) getView().findViewById(R.id.live_video_snap_thumb);

        //初始化结果接收器
        initResultReceiver();

        listener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

//                Log.e(TAG, String.format("布局变化 ==1== left:%d,top:%d,right:%d,bottom:%d->oldLeft:%d,oldTop:%d,oldRight:%d,oldBottom:%d", left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) );
                if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                    onVideoSizeChange();
                }
            }
        };

        ViewGroup parent = (ViewGroup) view.getParent();
        parent.addOnLayoutChangeListener(listener);

//        GestureDetector.SimpleOnGestureListener sgl = new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                if (doubleTapListener != null)
//                    doubleTapListener.onDoubleTab(PlayFragment.this);
//
//                return super.onDoubleTap(e);
//            }
//
//            @Override
//            public boolean onSingleTapUp(MotionEvent e) {
//                if (doubleTapListener != null)
//                    doubleTapListener.onSingleTab(PlayFragment.this);
//
//                return super.onSingleTapUp(e);
//            }
//
//            @Override
//            public boolean onDown(MotionEvent e) {
//                return true;
//            }
//        };
//
//        final GestureDetector gd = new GestureDetector(getContext(), sgl);
//
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return gd.onTouchEvent(event);
//            }
//        });
    }

    // 抓拍后隐藏thumb的task
    private final Runnable mAnimationHiddenTakePictureThumbTask = new Runnable() {
        @Override
        public void run() {
            //设置动画
            ViewCompat.animate(mTakePictureThumb).scaleX(0.0f).scaleY(0.0f).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    super.onAnimationEnd(view);
                    view.setVisibility(View.INVISIBLE);
                }
            });
        }
    };

    private void initResultReceiver() {

        //ResultReceiver是一个用来接收其他进程回调结果的通用接口 它是一种进程间(IPC)传递信息信息的机制，和广播类似
        //在其他线程中可以通过send(int, android.os.Bundle)方法发送数据
        //通过把mResultReceiver传入EasyPlayerClient返回的结果

        mResultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                Activity activity = getActivity();

                if (activity == null) {
                    return;
                }

                //视频显示出来了
                if (resultCode == EasyPlayerClient.RESULT_VIDEO_DISPLAYED) {
                    if (resultData != null) {
                        //视频的解码方式
                        DETECT_VIDEO = 1;
                        int videoDecodeType = resultData.getInt(EasyPlayerClient.KEY_VIDEO_DECODE_TYPE, 0);
                        //视频解码方式:硬解码  视频 已显示!!!!1280*720
                        Log.e(TAG, "视频解码方式:" + (videoDecodeType == 0 ? "软解码" : "硬解码"));
                    }

                    onVideoDisplayed();

                } else if (resultCode == EasyPlayerClient.RESULT_VIDEO_SIZE) {
                    mWidth = resultData.getInt(EasyPlayerClient.EXTRA_VIDEO_WIDTH);
                    mHeight = resultData.getInt(EasyPlayerClient.EXTRA_VIDEO_HEIGHT);

                    onVideoSizeChange();

                } else if (resultCode == EasyPlayerClient.RESULT_TIMEOUT) {

                    new AlertDialog.Builder(getActivity()).setMessage("试播时间到").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();

                } else if (resultCode == EasyPlayerClient.RESULT_UNSUPPORTED_AUDIO) {

                    new AlertDialog.Builder(getActivity()).setMessage("音频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();

                } else if (resultCode == EasyPlayerClient.RESULT_UNSUPPORTED_VIDEO) {

                    new AlertDialog.Builder(getActivity()).setMessage("视频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();

                } else if (resultCode == EasyPlayerClient.RESULT_RECORD_BEGIN) {

                    //m通知MainAcitivity去录制声音
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).onRecordState(true);
                    }

                } else if (resultCode == EasyPlayerClient.RESULT_RECORD_END) {

                    //mst停止录像
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).onRecordState(false);
                    }
                }

//                else if (resultCode == EasyPlayerClient.RESULT_EVENT) {
//                    int errorCode = resultData.getInt("errorcode");
////                    if (errorCode != 0) {
////                        stopRending();
////                    }
//
//                    if (activity instanceof PlayActivity) {
//                        ((PlayActivity) activity).onEvent(PlayFragment.this, errorCode, resultData.getString("event-msg"));
//                    }
//                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();

        onVideoDisplayed();
    }

    @Override
    public void onDestroyView() {
        ViewGroup parent = (ViewGroup) getView().getParent();
        if (parent != null) {
            parent.removeOnLayoutChangeListener(listener);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        stopRending();
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {
            // stop
//            stopRending();
            if (mStreamRender != null) {
                mStreamRender.pause();
            }
        } else {
            if (mStreamRender != null) {
                mStreamRender.resume();
            }
        }
    }

    /* ======================== private method ======================== */

    //在显示的视频上
    private void onVideoDisplayed() {
        View view = getView();
        Log.e(TAG, String.format("视频 已显示!!!!%d*%d", mWidth, mHeight));

        if (view != null) {
            //隐藏
            View view1 = view.findViewById(android.R.id.progress);
            if (view1 != null) {
                view1.setVisibility(View.GONE);
            }
        }

//        mSurfaceView.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mWidth != 0 && mHeight != 0) {
//                    Bitmap e = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//                    mSurfaceView.getBitmap(e);
//                    File f = FileUtil.getSnapFile(mUrl);
//                    saveBitmapInFile(f.getPath(), e);
//                    e.recycle();
//                }
//            }
//        });

        cover.setVisibility(View.GONE);
        sendResult(RESULT_REND_VIDEO_DISPLAY, null);
    }

    public boolean onRecordOrStop(String path, boolean isClose) {

        //EasyPlayerClient.isRecording() 是否在录制中

        if (mStreamRender == null) {
            return false;
        }

        //判断是否在录制
        if (!mStreamRender.isRecording()) {

            if (isClose) {
                //EasyPlayerClient开始录制视频
                mStreamRender.startRecord(path);
            }
            return true;

        } else {

            //结束录制
            mStreamRender.stopRecord();
            //通知更新
            MainActivity.mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
            return false;
        }
    }

    public boolean toggleAudioEnable() {
        if (mStreamRender == null) {
            return false;
        }

        mStreamRender.setAudioEnable(!mStreamRender.isAudioEnable());
        return mStreamRender.isAudioEnable();
    }

    public void pumpPCM(byte[] pcm, int length, long stampMS) {

        //fragment得到pcm数据 传递给EasyPlayerClient保存到文件里
        mStreamRender.pumpPCMSample(pcm, length, stampMS);
    }

    public static int DETECT_VIDEO = 0;

    // 开始渲染
    public void startRending() {
        //mResultReceiver用于传值
        mStreamRender = new EasyPlayerClient(getContext(), KEY, new Surface(surface), mResultReceiver);

//        boolean autoRecord = SPUtil.getAutoRecord(getContext());
        boolean autoRecord = false;//记录路径

//        File f = new File(FileUtil.getMoviePath(mUrl));
//        f.mkdirs();

        try {
            //启动播放
            mStreamRender.start(mUrl, mType < 2 ? Client.TRANSTYPE_TCP : Client.TRANSTYPE_UDP,
                    sendOption,
                    Client.EASY_SDK_VIDEO_FRAME_FLAG | Client.EASY_SDK_AUDIO_FRAME_FLAG,
                    "",
                    "",
                    autoRecord ? null : null);

        } catch (Exception e) {
            e.printStackTrace();
            DETECT_VIDEO = 0;
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        sendResult(RESULT_REND_START, null);
    }

    // 停止渲染
    public void stopRending() {
        DETECT_VIDEO = 0;
        if (mStreamRender != null) {
            sendResult(RESULT_REND_STOP, null);
            mStreamRender.stop();
            mStreamRender = null;
        }
    }

    // 抓拍
    public void takePicture(final String path) {
        try {
            if (mWidth <= 0 || mHeight <= 0) {
                return;
            }

            Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mSurfaceView.getBitmap(bitmap);
            saveBitmapInFile(path, bitmap);
            bitmap.recycle();

            mRenderCover.setImageDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));
            mRenderCover.setVisibility(View.VISIBLE);
            mRenderCover.setAlpha(1.0f);

            ViewCompat.animate(mRenderCover).cancel();
            ViewCompat.animate(mRenderCover).alpha(0.3f).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    super.onAnimationEnd(view);
                    mRenderCover.setVisibility(View.GONE);
                }
            });

            if (mLoadingPictureThumbTask != null) {
                mLoadingPictureThumbTask.cancel(true);
            }

            final int w = mTakePictureThumb.getWidth();
            final int h = mTakePictureThumb.getHeight();

//            Log.e(TAG, "布局变化 ==1== 宽 == " + w + "  高 == " + h );

            mLoadingPictureThumbTask = new AsyncTask<Void, Void, Bitmap>() {
                final WeakReference<ImageView> mImageViewRef = new WeakReference<>(mTakePictureThumb);
                final String mPath = path;

                @Override
                protected Bitmap doInBackground(Void... params) {
                    return decodeSampledBitmapFromResource(mPath, w, h);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);

                    if (isCancelled()) {
                        bitmap.recycle();
                        return;
                    }

                    ImageView iv = mImageViewRef.get();

                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //不添加拍照时点击小图会崩溃
                        }
                    });

                    if (iv == null) {
                        return;
                    }

                    iv.setImageBitmap(bitmap);
                    iv.setVisibility(View.VISIBLE);
                    iv.removeCallbacks(mAnimationHiddenTakePictureThumbTask);
                    iv.clearAnimation();

                    ViewCompat.animate(iv).scaleX(1.0f).scaleY(1.0f).setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            super.onAnimationEnd(view);
                            view.postOnAnimationDelayed(mAnimationHiddenTakePictureThumbTask, 4000);
                        }
                    });

                    iv.setTag(mPath);
                    refresh(mPath);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void refresh(String filePath) {
        Uri localUri = Uri.fromFile(new File(filePath));
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
        if (getActivity() != null) {
            getActivity().sendBroadcast(localIntent);
        }
    }


    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void saveBitmapInFile(final String path, Bitmap bitmap) {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);

            if (mScanner == null) {
                MediaScannerConnection connection = new MediaScannerConnection(getContext(), new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onMediaScannerConnected() {

                        // MediaScanner service 创建后回调
                        mScanner.scanFile(path, null /* mimeType */);
                    }

                    @Override
                    public void onScanCompleted(String path1, Uri uri) {

                        // 当MediaScanner完成文件扫描后回调
                        if (path1.equals(path)) {
                            mScanner.disconnect();
                            mScanner = null;
                        }
                    }
                });

                try {
                    connection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mScanner = connection;
            }
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //视频尺寸更改
    private void onVideoSizeChange() {
        Log.e(TAG, String.format("RESULT_VIDEO_SIZE RECEIVED :%d*%d", mWidth, mHeight));

        if (mWidth == 0 || mHeight == 0) {
            return;
        }

        if (mAttacher != null) {
            mAttacher.cleanup();
            mAttacher = null;
        }

        if (mRatioType == ASPECT_RATIO_CROPS_MATRIX) {
            ViewGroup parent = (ViewGroup) getView().getParent();
            parent.addOnLayoutChangeListener(listener);
            fixPlayerRatio(getView(), parent.getWidth(), parent.getHeight());

            mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

            mAttacher = new PhotoViewAttacher(mSurfaceView, mWidth, mHeight);
            mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mAttacher.setOnMatrixChangeListener(PlayFragment.this);
            mAttacher.update();

            mAngleView.setVisibility(View.VISIBLE);
        } else {
            mSurfaceView.setTransform(new Matrix());
            mAngleView.setVisibility(View.GONE);
//            int viewWidth = mSurfaceView.getWidth();
//            int viewHeight = mSurfaceView.getHeight();
            float ratioView = getView().getWidth() * 1.0f / getView().getHeight();
            float ratio = mWidth * 1.0f / mHeight;

            switch (mRatioType) {
                case ASPECT_RATIO_INSIDE:
                    if (ratioView - ratio < 0) {    // 屏幕比视频的宽高比更小.表示视频是过于宽屏了.
                        // 宽为基准.
                        mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                        mSurfaceView.getLayoutParams().height = (int) (getView().getWidth() / ratio + 0.5f);
                    } else {                        // 视频是竖屏了.
                        mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mSurfaceView.getLayoutParams().width = (int) (getView().getHeight() * ratio + 0.5f);
                    }

                    break;
                case ASPECT_RATIO_CENTER_CROPS:
                    // 以更短的为基准
                    if (ratioView - ratio < 0) {    // 屏幕比视频的宽高比更小.表示视频是过于宽屏了.
                        // 宽为基准.
                        mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mSurfaceView.getLayoutParams().width = (int) (getView().getHeight() * ratio + 0.5f);
                    } else {                        // 视频是竖屏了.
                        mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                        mSurfaceView.getLayoutParams().height = (int) (getView().getWidth() / ratio + 0.5f);
                    }

                    break;
                case FILL_WINDOW:
                    mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

                    break;
                default:
                    break;
            }
        }

        mSurfaceView.requestLayout();
    }

    protected void sendResult(int resultCode, Bundle resultData) {
        if (mRR != null) {
            mRR.send(resultCode, resultData);
        }
    }

    /* ======================== SurfaceTextureListener ======================== */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface1, int width, int height) {
        //开始播放了
        this.surface = surface1;
        startRending();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopRending();

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /* ======================== OnMatrixChangedListener ======================== */

    @Override
    public void onMatrixChanged(Matrix matrix, RectF rect) {
        float maxMovement = (rect.width() - mSurfaceView.getWidth());
        float middle = mSurfaceView.getWidth() * 0.5f + mSurfaceView.getLeft();
        float currentMiddle = rect.width() * 0.5f + rect.left;
        mAngleView.setCurrentProgress(-(int) ((currentMiddle - middle) * 100 / maxMovement));
    }

    /* ======================== get/set ======================== */

    public interface OnDoubleTapListener {
        void onDoubleTab(PlayFragment f);

        void onSingleTab(PlayFragment f);
    }

    public boolean isAudioEnable() {
        return mStreamRender != null && mStreamRender.isAudioEnable();
    }

    // 进入全屏模式
    public void enterFullscreen() {
        setScaleType(FILL_WINDOW);
    }

    // 退出全屏模式
    public void quiteFullscreen() {
        setScaleType(ASPECT_RATIO_CROPS_MATRIX);
    }


    // 退出全屏模式
    // 进入全屏模式
    public void setScaleType(@IntRange(from = ASPECT_RATIO_INSIDE, to = FILL_WINDOW) int type) {
        mRatioType = type;

        if (mWidth != 0 && mHeight != 0) {
            onVideoSizeChange();
        }
    }

    public void setOnDoubleTapListener(OnDoubleTapListener listener) {
        this.doubleTapListener = listener;
    }

    public long getReceivedStreamLength() {
        if (mStreamRender != null) {
            return mStreamRender.receivedDataLength();
        }

        return 0;
    }

    public void setUrl(String url) {
        this.mUrl = url;

        if (!TextUtils.isEmpty(mUrl)) {
            Glide.with(this)
                    .load(FileUtil.getSnapFile(mUrl))
                    .signature(new ObjectKey(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(cover);
        }
    }

    public void setTransType(int transType) {
        this.mType = transType;
    }

    public void setResultReceiver(ResultReceiver rr) {
        mRR = rr;
    }

    public void setSelected(boolean selected) {
        mSurfaceView.animate().scaleX(selected ? 0.9f : 1.0f);
        mSurfaceView.animate().scaleY(selected ? 0.9f : 1.0f);
        mSurfaceView.animate().alpha(selected ? 0.7f : 1.0f);
    }

    // 高度固定，宽度可更改
    protected void fixPlayerRatio(View renderView, int maxWidth, int maxHeight) {
//        fixPlayerRatio(renderView, maxWidth, maxHeight, mWidth, mHeight);
    }

    protected void fixPlayerRatio(View renderView, int widthSize, int heightSize, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }

        float aspectRatio = width * 1.0f / height;

        if (widthSize > heightSize * aspectRatio) {
            height = heightSize;
            width = (int) (height * aspectRatio);
        } else {
            width = widthSize;
            height = (int) (width / aspectRatio);
        }

        renderView.getLayoutParams().width = width;
        renderView.getLayoutParams().height = height;
        renderView.requestLayout();
    }

    public static class ReverseInterpolator extends AccelerateDecelerateInterpolator {
        @Override
        public float getInterpolation(float paramFloat) {
            return super.getInterpolation(1.0f - paramFloat);
        }
    }

    protected boolean isLandscape() {
        return getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    }
}
