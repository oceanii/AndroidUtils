package com.oceanii.androidutils.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.oceanii.androidutils.utils.GlesUtils;
import com.oceanii.androidutils.utils.LogUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by oceanii on 2022/3/10.
 */

public class CameraRender implements GLSurfaceView.Renderer{
    private static final String TAG = "CameraV2_" + "CameraRender";
    private Context mContext;
    private OnRenderListener mOnRenderListener;
    private CameraGLSurfaceView mCameraGLSurfaceView;
    private CameraV2 mCameraV2;

    private SurfaceTexture mSurfaceTexture;
    private int mOesTextureId;
    private int mPreviewWidth;
    private int mPreviewHeight;

    private int mViewportWidth;
    private int mViewportHeight;

    private TextureConvertEffect mTextureConvertEffect;
    private TextureOnScreenEffect mTextureOnScreenEffect;

    private long mTimestamp;
    private float[] mTransformMatrix = new float[16];

    private int mDstTexId;
    private int mDstTexWidth;
    private int mDstTexHeight;

    //private RenderEngine mRenderEngine = null;
    private long mFrameIndex = 0;

    public CameraRender(Context context){
        mContext = context;
        //mRenderEngine = new RenderEngine();
    }

    public void setRenderListener(OnRenderListener onRenderListener){
        mOnRenderListener = onRenderListener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        LogUtils.i(TAG, "onSurfaceCreated");

        if(mOnRenderListener != null){
            mOnRenderListener.onSurfaceCreated();
        }
        //mRenderEngine.createEngine(mContext, true);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        LogUtils.i(TAG, "onSurfaceChanged: width:" + width + " height:" + height);
        mViewportWidth = width;
        mViewportHeight = height;

        mOesTextureId = GlesUtils.createOesTexture();
        initSurfaceTexture();

        mDstTexWidth = mPreviewHeight;
        mDstTexHeight = mPreviewWidth;
        mDstTexId = GlesUtils.createTexture(mDstTexWidth, mDstTexHeight);

        mTextureConvertEffect = new TextureConvertEffect(true);
        mTextureConvertEffect.init(mPreviewHeight, mPreviewWidth, GLES20.GL_RGBA);
        mTextureOnScreenEffect = new TextureOnScreenEffect();
        mTextureOnScreenEffect.init();

        if(mOnRenderListener != null){
            mOnRenderListener.onSurfaceChanged(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //LogUtils.i(TAG, "onDrawFrame: ");
        if(mSurfaceTexture != null){
            //更新数据，其实也是消耗数据，将上一帧的数据处理或者抛弃掉，要不然SurfaceTexture接收不到最新数据
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTransformMatrix);
            mTimestamp = mSurfaceTexture.getTimestamp();
        }

        mTextureConvertEffect.render(mOesTextureId, mPreviewHeight, mPreviewWidth, mTransformMatrix);
        if(mOnRenderListener != null){
            mOnRenderListener.onRenderToTexture(mFrameIndex, mTextureConvertEffect.mFBO.texId, mDstTexId, mDstTexWidth, mDstTexHeight);
        }

        //mRenderEngine.renderToTexture(mFrameIndex, mSrcTexId, mDstTexId, mDstTexWidth, mDstTexHeight);
        mTextureOnScreenEffect.render(mTextureConvertEffect.mFBO.texId, mViewportWidth, mViewportHeight);
        mFrameIndex++;
    }

    public void setCamera(CameraGLSurfaceView cameraGLSurfaceView, CameraV2 cameraV2){
        mCameraGLSurfaceView = cameraGLSurfaceView;
        mCameraV2 = cameraV2;
    }

    /*根据OES纹理ID创建SurfaceTexture，用来接收Camera的预览数据*/
    private void initSurfaceTexture(){
        LogUtils.i(TAG, "initSurfaceTexture: isPreviewStarted:" + mCameraV2.isPreviewStarted());
        if(mSurfaceTexture != null){
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        //根据OES纹理ID实例化SurfaceTexture
        mSurfaceTexture = new SurfaceTexture(mOesTextureId);
        //当SurfaceTexture接收到一帧数据时，请求OpenGL ES进行渲染
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mCameraGLSurfaceView.requestRender();
            }
        });

        mCameraV2.setPreviewTexture(mSurfaceTexture);
        if(!mCameraV2.isPreviewStarted()){
            mCameraV2.startPreview();
        }
        mPreviewWidth = mCameraV2.getPreviewSize().getWidth();
        mPreviewHeight = mCameraV2.getPreviewSize().getHeight();
    }

    /*GL线程释放资源*/
    public void release(){
        GlesUtils.deleteTexture(mOesTextureId);
        GlesUtils.deleteTexture(mDstTexId);
        mTextureConvertEffect.release();
        mTextureOnScreenEffect.release();
        //mRenderEngine.release();
    }

    /*渲染回调接口，用于接入第三方SDK进行纹理进纹理出渲染*/
    public interface OnRenderListener {
        void onRenderError();
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onRenderToTexture(long frameIndex, int srcTexId, int dstTexId, int dstTexWidth, int dstTexHeight);
    }
}
