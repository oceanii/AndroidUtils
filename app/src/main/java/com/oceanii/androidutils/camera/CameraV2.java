package com.oceanii.androidutils.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import com.oceanii.androidutils.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by oceanii on 2022/3/10.
 */

public class CameraV2 {
    private static final String TAG = "CameraV2_" + "CameraV2";
    private CameraConfig.CameraID mCameraId = CameraConfig.CameraID.ID_FRONT;
    private String mCameraStrId;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private Size mPreviewSize;

    SurfaceTexture mSurfaceTexture;

    private boolean mIsPreviewStarted = false;
    public Context mContext;

    private static Range<Integer>[] mFpsRanges;

    //相机拍照的回调，可以拿到具体的图像信息
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    //当相机成功打开后会回调onOpened方法，这里可以拿到CameraDevice对象，也就是具体的摄像头设备
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            LogUtils.i(TAG, "onOpened: mIsPreviewStarted:" + mIsPreviewStarted);
            mCameraDevice = cameraDevice;
            //此处会比onSurfaceCreated()先调用，SurfaceTexure还没有准备好，所以第一次startPreview放在onSurfaceCreated中执行
            if(!mIsPreviewStarted){
                startPreview();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            LogUtils.i(TAG, "onDisconnected: ");
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    public CameraV2(Context context){
        mContext = context;
        startCameraThread();
    }

    /*创建并启动Camera子线程，后面的Camera开启、预览、拍照都在这个子线程中*/
    private void startCameraThread() {
        LogUtils.i(TAG, "startCameraThread");
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    /*停止相机子线程*/
    private void stopCameraThread() {
        LogUtils.i(TAG, "stopCameraThread");
        if(mCameraThread != null){
            mCameraThread.quitSafely();
            try {
                mCameraThread.join();
                mCameraThread = null;
                mCameraHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*设置相机参数*/
    public synchronized void setupCamera(int width, int height){
        //获取摄像头管理者，它主要用来查询和打开可用的摄像头
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            //遍历所有摄像头
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String id : cameraIdList) {
                //获取此ID对应摄像头的参数
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                //默认打开前置摄像头
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == mCameraId.getId()) {
                    mFpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                    //cameraOrientation = ((Integer) characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
                    //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                    StreamConfigurationMap map = (StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    //根据屏幕尺寸（通过参数传进来）匹配最合适的预览尺寸
                    mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                    mCameraStrId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /*打开相机*/
    public synchronized void openCamera(){
        LogUtils.i(TAG, "openCamera: mPreviewSize:" + mPreviewSize.getWidth() + " " + mPreviewSize.getHeight());
        //获取摄像头管理者，它主要用来查询和打开可用的摄像头
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            //打开相机前检查相机权限
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            //打开相机，第一个参数是哪个摄像头，第二个参数是相机状态的回调接口，第三个参数是确定相机状态回调接口在哪个线程执行，为null是指当前线程
            cameraManager.openCamera(mCameraStrId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*切换相机前后置摄像头*/
    public synchronized void switchCamera(){
        LogUtils.i(TAG, "switchCamera");
        if(mIsPreviewStarted){
            if(mCameraId == CameraConfig.CameraID.ID_FRONT){
                mCameraId = CameraConfig.CameraID.ID_BACK;
            }else if(mCameraId == CameraConfig.CameraID.ID_BACK){
                mCameraId = CameraConfig.CameraID.ID_FRONT;
            }
            stopPreview();
            closeCamera();
            setupCamera(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            openCamera();
        }
    }

    /*关闭相机*/
    public synchronized void closeCamera() {
        LogUtils.i(TAG, "closeCamera");
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    /*释放相机，包括相机子线程*/
    public synchronized void releaseCamera(){
        LogUtils.i(TAG, "releaseCamera");
        //删除SurfaceTexture操作放入CameraRender中
        stopPreview();
        closeCamera();
        stopCameraThread();
    }

    /*判断是否已经创建了预览会话窗口*/
    public boolean isPreviewStarted(){
        return mIsPreviewStarted;
    }

    /*获取图像预览尺寸*/
    public Size getPreviewSize() {
        return mPreviewSize;
    }

    /*传入预览的SurfaceTexture*/
    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        this.mSurfaceTexture = surfaceTexture;
    }

    /*开启相机预览*/
    public synchronized void startPreview(){
        LogUtils.i(TAG, "startPreview begin");
        if(mSurfaceTexture == null){
            return;
        }
        //设置SurfaceTexture的默认尺寸
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //根据mSurfaceTexture创建Surface
        Surface surface = new Surface(mSurfaceTexture);
        try {
            //创建preview捕获请求
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            //将此请求输出目标设为创建的Surface对象，这个Surface对象也必须添加给createCaptureSession才行
            mCaptureRequestBuilder.addTarget(surface);

            //设置相机帧率
            //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mFpsRanges[mFpsRanges.length - 1]);

            //创建捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是相机捕获会话的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定回调在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (mCameraDevice == null) {
                        return;
                    }

                    try {
                        //创建捕获请求
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = cameraCaptureSession;
                        //设置重复捕获数据的请求，之后surface绑定的SurfaceTexture中就会一直有数据到达，然后就会回调SurfaceTexture.OnFrameAvailableListener接口，第二个参数是捕捉图像的回调，可以拿到具体的图像信息，也可以传入null
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        //CameraDevice was already closed
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mIsPreviewStarted = true;
        LogUtils.i(TAG, "startPreview end");
    }

    /*停止相机预览*/
    public synchronized void stopPreview(){
        LogUtils.i(TAG, "stopPreview");
        mIsPreviewStarted = false;
        if(mCameraCaptureSession != null){
            try {
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession = null;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                //CameraDevice was already closed
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /*相机拍照*/
    public void takePicture(){

    }

    /*根据期望的宽高计算最优的预览尺寸*/
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() >= width && option.getHeight() >= height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() >= height && option.getHeight() >= width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }
}
