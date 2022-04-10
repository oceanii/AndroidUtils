package com.oceanii.androidutils.camera;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.oceanii.androidutils.utils.GlesUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by oceanii on 2022/3/10.
 */

public class TextureConvertEffect {
    private static final String TAG = "TextureConvertEffect";

    private static final String VERTEX_SHADER =
            "attribute vec4 a_position;\n" +
                    "attribute vec2 a_coord;\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = a_position;\n" +
                    "    textureCoordinate = a_coord.xy;\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;\n" +
                    "uniform sampler2D inputImageTexture;\n" +
                    "void main()\n" +
                    "{\n" +
                    "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "}\n";
    private static final String VERTEX_SHADER_OES =
            "attribute vec4 a_position;\n" +
                    "attribute vec2 a_coord;\n" +
                    "uniform mat4 u_TexMatrix;\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = a_position;\n" +
                    "    vec4 tmpTexCoord = vec4((1.0 + a_position.x) * 0.5, (1.0 - a_position.y) * 0.5 ,0.0, 1.0);\n" +
                    //"    vec4 tmpTexCoord = vec4(a_coord, 0.0, 1.0);\n" +
                    "    textureCoordinate = (u_TexMatrix * tmpTexCoord).xy;\n" +
                    "}\n";
    private static final String FRAGMENT_SHADER_OES =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "varying highp vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES inputImageTexture;\n" +
                    "void main()\n" +
                    "{\n" +
                    "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "}\n";

    private static final int SIZEOF_FLOAT = 4;

    private static final float VERTEX[] = {
            -1.0f,  1.0f, 0, 1,
            1.0f,  1.0f, 0, 1,
            -1.0f, -1.0f, 0, 1,
            1.0f, -1.0f, 0, 1,
    };

    private static final float COORD[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    private String mVertexShader;
    private String mFragmentShader;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mCoordBuffer;

    private int mProgram;
    private int mPositionHandle;
    protected int mCoordHandle;
    private int mTexMatrixLocation;
    private int mInputTextureHandle;

    private boolean mIsOesTex;

    private float[] mTexMatrix = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    public GlesUtils.FBO mFBO = null;

    public TextureConvertEffect(boolean isOesTex){
        if(isOesTex){
            mVertexShader = VERTEX_SHADER_OES;
            mFragmentShader = FRAGMENT_SHADER_OES;
        }else{
            mVertexShader = VERTEX_SHADER;
            mFragmentShader = FRAGMENT_SHADER;
        }

        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * SIZEOF_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(VERTEX).position(0);

        mCoordBuffer = ByteBuffer.allocateDirect(COORD.length * SIZEOF_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCoordBuffer.put(COORD).position(0);

        mIsOesTex = isOesTex;
    }

    public void init(int width, int height, int format){
        mProgram = GlesUtils.createProgram(mVertexShader, mFragmentShader);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        mCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_coord");
        mTexMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_TexMatrix");
        mInputTextureHandle = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
        mFBO = GlesUtils.createFbo(width, height, format);
    }

    public void render(int texId, int texWidth, int texHeight, float[] aryTexMat){
        int[] oldFboId = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, oldFboId, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBO.fboId);

        GLES20.glViewport(0, 0, texWidth, texHeight);
        GLES20.glUseProgram(mProgram);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 4, GLES20.GL_FLOAT, false, 4 * SIZEOF_FLOAT, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * SIZEOF_FLOAT, mCoordBuffer);
        GLES20.glEnableVertexAttribArray(mCoordHandle);

        if(aryTexMat != null){
            mTexMatrix = aryTexMat;
        }
        GLES20.glUniformMatrix4fv(mTexMatrixLocation, 1, false, mTexMatrix, 0);

        if (texId > 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(mIsOesTex ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D, texId);
            GLES20.glUniform1i(mInputTextureHandle, 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mCoordHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, oldFboId[0]);
    }

    public void release(){
        GlesUtils.deleteFbo(mFBO);
        GlesUtils.deleteProgram(mProgram);
    }
}
