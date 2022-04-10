package com.oceanii.androidutils.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.oceanii.androidutils.R;

public class CameraActivity extends AppCompatActivity {
    private CameraV2 mCameraV2;
    private int mCameraWidth = 1080;
    private int mCameraHeight = 1920;
    private CameraGLSurfaceView mCameraGLSurfaceView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceview_camera);
        mCameraV2 = new CameraV2(this);
        mCameraGLSurfaceView.init(this, new CameraRender.OnRenderListener() {
            @Override
            public void onRenderError() {

            }

            @Override
            public void onSurfaceCreated() {

            }

            @Override
            public void onSurfaceChanged(int width, int height) {

            }

            @Override
            public void onRenderToTexture(long frameIndex, int srcTexId, int dstTexId, int dstTexWidth, int dstTexHeight) {

            }
        }, mCameraV2);
        CameraConfig.AspectRatio aspectRatio = CameraConfig.AspectRatio.RATIO_16_9;
        mCameraGLSurfaceView.setAspectRatio(aspectRatio.getShortSide(), aspectRatio.getLongSide());

        findViewById(R.id.btn_switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCameraV2 != null){
                    mCameraV2.switchCamera();
                }
            }
        });

        findViewById(R.id.btn_ratio_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCameraV2 != null){
                    mCameraWidth = 1080;
                    mCameraHeight = 1440;
                    mCameraV2.stopPreview();
                    mCameraV2.closeCamera();
                    mCameraV2.setupCamera(mCameraWidth, mCameraHeight);
                    mCameraV2.openCamera();
                    CameraConfig.AspectRatio aspectRatio = CameraConfig.AspectRatio.RATIO_4_3;
                    mCameraGLSurfaceView.setAspectRatio(aspectRatio.getShortSide(), aspectRatio.getLongSide());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCameraV2 != null){
            mCameraV2.setupCamera(mCameraWidth, mCameraHeight);
            mCameraV2.openCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCameraV2 != null){
            mCameraV2.stopPreview();
            mCameraV2.closeCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCameraV2 != null){
            mCameraV2.releaseCamera();
            mCameraV2 = null;
        }
        if(mCameraGLSurfaceView != null){
            mCameraGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {

                }
            });
            mCameraGLSurfaceView.release();
        }
    }
}
