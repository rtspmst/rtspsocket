package org.easydarwin.util;


import android.graphics.SurfaceTexture;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import java.lang.ref.WeakReference;

/**
 * LifecycleOwner
 * 具有Android生命周期的接口
 * 可以处理生命周期带来的变化却不需要在Activity或者Fragment中实现任何代码。
 * 暂无用到
 */
public class TextureLifecycler implements LifecycleOwner {
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    WeakReference<TextureView> mRef;

    public TextureLifecycler(TextureView view) {
        mRef = new WeakReference<>(view);
        mLifecycleRegistry.markState(Lifecycle.State.INITIALIZED);
        if (view.isAvailable()) {
            mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
            mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        }
        view.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
                mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
                mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
                mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
                mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

}
