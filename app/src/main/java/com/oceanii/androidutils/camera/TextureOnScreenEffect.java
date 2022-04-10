package com.oceanii.androidutils.camera;

import android.opengl.GLES20;

import com.oceanii.androidutils.utils.GlesUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by oceanii on 2022/3/10.
 */

public class TextureOnScreenEffect {
    private static final String TAG = "TextureOnScreenEffect";

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

    private static final int SIZEOF_FLOAT = 4;

    private static final float VERTEX[] = {
            -1.0f,  1.0f, 0, 1,
            1.0f,  1.0f, 0, 1,
            -1.0f, -1.0f, 0, 1,
            1.0f, -1.0f, 0, 1,
    };

    private static final float COORD[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    private String mVertexShader;
    private String mFragmentShader;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mCoordBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mCoordHandle;
    private int mInputTextureHandle;

    public TextureOnScreenEffect(){
        mVertexShader = VERTEX_SHADER;
        mFragmentShader = FRAGMENT_SHADER;

        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * SIZEOF_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(VERTEX).position(0);

        mCoordBuffer = ByteBuffer.allocateDirect(COORD.length * SIZEOF_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCoordBuffer.put(COORD).position(0);
    }

    public void init(){
        mProgram = GlesUtils.createProgram(mVertexShader, mFragmentShader);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        mCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_coord");
        mInputTextureHandle = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
    }

    public void render(int texId, int viewportWidth, int viewportHeight){
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
        GLES20.glUseProgram(mProgram);

        //GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        //GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 4, GLES20.GL_FLOAT, false, 4 * SIZEOF_FLOAT, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * SIZEOF_FLOAT, mCoordBuffer);
        GLES20.glEnableVertexAttribArray(mCoordHandle);

        if (texId > 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
            GLES20.glUniform1i(mInputTextureHandle, 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mCoordHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public void release(){
        GlesUtils.deleteProgram(mProgram);
    }

}
