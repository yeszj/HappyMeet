package cn.yanhu.agora.ui.liveRoom;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import kotlin.jvm.internal.Intrinsics;

/**
 * @author: zhengjun
 * created: 2025/10/30
 * desc:
 */
public class TextureViewPool {
    private Queue<TextureView> availableViews = new LinkedList<>();
    private Map<String, TextureView> usedViews = new HashMap<>();
    private Context context;
    public final void clearSurface(Surface surface) {
        Intrinsics.checkNotNullParameter(surface, "surface");
        EGLDisplay eglGetDisplay = EGL14.eglGetDisplay(0);
        int[] iArr = new int[2];
        EGL14.eglInitialize(eglGetDisplay, iArr, 0, iArr, 1);
        EGLConfig[] eGLConfigArr = new EGLConfig[1];
        EGL14.eglChooseConfig(eglGetDisplay, new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 8, 12352, 4, 12344, 0, 12344}, 0, eGLConfigArr, 0, 1, new int[1], 0);
        EGLConfig eGLConfig = eGLConfigArr[0];
        EGLContext eglCreateContext = EGL14.eglCreateContext(eglGetDisplay, eGLConfig, EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
        EGLSurface eglCreateWindowSurface = EGL14.eglCreateWindowSurface(eglGetDisplay, eGLConfig, surface, new int[]{12344}, 0);
        EGL14.eglMakeCurrent(eglGetDisplay, eglCreateWindowSurface, eglCreateWindowSurface, eglCreateContext);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(16384);
        EGL14.eglSwapBuffers(eglGetDisplay, eglCreateWindowSurface);
        EGL14.eglDestroySurface(eglGetDisplay, eglCreateWindowSurface);
        EGL14.eglMakeCurrent(eglGetDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(eglGetDisplay, eglCreateContext);
        EGL14.eglTerminate(eglGetDisplay);
    }
    public TextureViewPool(Context context) {
        this.context = context;
        preCreateViews(3); // 预创建3个TextureView
    }

    private void preCreateViews(int count) {
        for (int i = 0; i < count; i++) {
            TextureView textureView = new TextureView(context);
            textureView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            availableViews.offer(textureView);
        }
    }

    public TextureView getTextureView(String streamId) {
        TextureView textureView = availableViews.poll();
        if (textureView != null) {
            usedViews.put(streamId, textureView);
        }
        return textureView;
    }

    public void releaseTextureView(String streamId) {
        TextureView textureView = usedViews.remove(streamId);
        if (textureView != null) {
            // 清理状态
            textureView.setSurfaceTextureListener(null);
            availableViews.offer(textureView);
        }
    }


        /**
         * 安全回收TextureView
         */
        public static void recycleTextureView(TextureView textureView) {
            if (textureView == null) return;

            // 步骤1: 移除所有监听器
            textureView.setSurfaceTextureListener(null);
            // 步骤2: 清理SurfaceTexture
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            if (surfaceTexture != null) {
                try {
                    // 释放SurfaceTexture资源
                    surfaceTexture.release();
                } catch (Exception e) {
                    Log.e("TextureRecycler", "Error releasing SurfaceTexture: " + e.getMessage());
                }
            }

            // 步骤3: 从父视图移除
            if (textureView.getParent() != null) {
                ((ViewGroup) textureView.getParent()).removeView(textureView);
            }

            // 步骤4: 清理其他引用
            textureView.setTag(null);
            textureView.setOnClickListener(null);
            Log.d("TextureRecycler", "TextureView recycled successfully");
        }

}
