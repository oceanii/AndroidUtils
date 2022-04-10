package com.oceanii.androidutils.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by oceanii on 2022/3/10.
 */

public class GlesUtils {
    private static final String TAG = "GlesUtils";

    /**
     * 获取设备信息，判断当前设备是否支持OpenGL ES 3.0
     *
     * @param activity
     * @return
     */
    public static boolean isSupportGles30(Activity activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(
                Context.ACTIVITY_SERVICE);
        return activityManager.getDeviceConfigurationInfo().reqGlEsVersion >= 0x30000;
    }

    /*FBO结构体，等同于简化版的FrameBuffer*/
    public static class FBO{
        public int fboId;
        public int texId;
        public int width;
        public int height;
        public int format;
    }

    /*创建FBO*/
    public static FBO createFbo(int width, int height, int format){

        int[] tmpFboId = new int[1], tmpTexId = new int[1];
        // generate fbo id
        GLES20.glGenFramebuffers(1, tmpFboId, 0);
        // generate texture
        GLES20.glGenTextures(1, tmpTexId, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, tmpFboId[0]);
        // Bind texture
        // glActiveTexture(textureIndex);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tmpTexId[0]);
        // Define texture parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format,
                GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                tmpTexId[0], 0);
        int glRet = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (glRet != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "createFbo glCheckFramebufferStatus failed: " + glRet);
            return null;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        FBO fbo = new FBO();
        fbo.fboId = tmpFboId[0];
        fbo.texId = tmpTexId[0];
        fbo.width = width;
        fbo.height = height;
        fbo.format = format;

        Log.d(TAG, "createFbo id:" + fbo.fboId + " tex id:" + fbo.texId);
        return fbo;
    }

    /*删除FBO*/
    public static void deleteFbo(FBO fbo){
        if(fbo != null){
            if(fbo.fboId > 0){
                int[] fbos = new int[1];
                fbos[0] = fbo.fboId;
                GLES20.glDeleteFramebuffers(1, fbos, 0);
            }
            if(fbo.texId > 0){
                int[] textures = new int[1];
                textures[0] = fbo.texId;
                GLES20.glDeleteTextures(1, textures, 0);
            }
        }
    }

    /*创建program*/
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(pixelShader);
        return program;
    }

    /*删除program*/
    public static void deleteProgram(int program){
        if(program > 0){
            GLES20.glDeleteProgram(program);
        }
    }

    /*加载shader*/
    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /*纹理保存到bitmap*/
    public static Bitmap textureToBitmap(int textureId, int imageWidth, int imageHeight){
        ByteBuffer mTmpBuffer = ByteBuffer.allocate(imageWidth * imageHeight * 4);

        int[] oldFboId = new int[1];
        int[] mFrameBuffers = new int[1];

        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, oldFboId, 0);

        if(textureId != 0) {
            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,textureId, 0);
        }

        GLES20.glReadPixels(0, 0, imageWidth, imageHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mTmpBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, oldFboId[0]);
        GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);

        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(mTmpBuffer);

        return bitmap;
    }

    /*当前framebuffer中的纹理保存到bitmap*/
    public static Bitmap currentFrameBufferToBitmap(int imageWidth, int imageHeight){
        ByteBuffer mTmpBuffer = ByteBuffer.allocate(imageWidth * imageHeight * 4);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glReadPixels(0, 0, imageWidth, imageHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mTmpBuffer);

        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(mTmpBuffer);

        return bitmap;
    }

    /*创建一个空的2D纹理*/
    public static int createTexture(){
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textures[0];
    }

    /*根据宽高创建一个2D纹理*/
    public static int createTexture(int width, int height){
        int texId = createTexture();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return texId;
    }

    /*根据bitmap创建一个2D纹理*/
    public static int createTexture(Bitmap bitmap){
        int texId = createTexture();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return texId;
    }

    /*创建Oes纹理*/
    public static int createOesTexture(){
        int[] oesTexId = new int[1];
        GLES20.glGenTextures(1, oesTexId, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexId[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return oesTexId[0];
    }

    /*删除纹理*/
    public static void deleteTexture(int texId){
        if(texId > 0){
            int[] textures = new int[1];
            textures[0] = texId;
            GLES20.glDeleteTextures(1, textures, 0);
        }
    }

    /*检查GL错误*/
    public static void checkGlError(String glFuncStr) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = glFuncStr + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
        }
    }
}
