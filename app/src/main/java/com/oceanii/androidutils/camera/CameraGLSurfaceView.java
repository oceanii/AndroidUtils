package com.oceanii.androidutils.camera;

import android.content.Context;
import android.util.AttributeSet;

import com.oceanii.androidutils.utils.LogUtils;

/**
 * Created by oceanii on 2022/3/10.
 */

public class CameraGLSurfaceView extends AutoFitGLSurfaceView{
    private static final String TAG = "CameraV2_" + "CameraGLSurfaceView";
    private CameraRender mCameraRender;

    public CameraGLSurfaceView(Context context) {
        super(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Context context, CameraRender.OnRenderListener onRenderListener, CameraV2 cameraV2){
        //setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(3);
        //setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mCameraRender = new CameraRender(context);
        mCameraRender.setRenderListener(onRenderListener);
        mCameraRender.setCamera(this, cameraV2);
        this.setRenderer(mCameraRender);
        this.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    /*获取Render*/
    public CameraRender getRender(){
        return mCameraRender;
    }

    /*GL线程释放资源*/
    public void release(){
        LogUtils.i(TAG, "release ");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if(mCameraRender != null){
                    mCameraRender.release();
                }
            }
        });
    }

    /*进入后台,不主动释放GL资源*/
    @Override
    public void onPause() {
        super.onPause();
        LogUtils.i(TAG, "onPause ");
    }
}
