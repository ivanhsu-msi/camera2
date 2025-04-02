/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2022. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.aovtestapp.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import com.mediatek.aovtestapp.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * OpenGL Surface view
 */
public class YUVRenderView extends GLSurfaceView implements GLSurfaceView.Renderer
{
    private final static String TAG = YUVRenderView.class.getSimpleName();

    private int mBufferWidthY, mBufferHeightY, mBufferWidthUV, mBufferHeightUV;
    private ByteBuffer mBuffer;
    private int mBufferPositionY, mBufferPositionUV;

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int SHORT_SIZE_BYTES = 2;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET   = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET    = 3;

    private final float[] TRIANFLE_VERTICES_DATA = {
            -1.0f,  1.0f, 0.0f, 0.0f, 0.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
            1.0f,  1.0f, 0.0f, 1.0f, 0.0f
    };

    private final short[] INDICES_DATA = {
            0, 1, 2, 0, 2, 3};

    private FloatBuffer mTriangleVertices;
    private ShortBuffer mIndices;

    private static final String VERTEX_SHADER_SOURCE =
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_Position = aPosition;\n" +
            "  vTextureCoord = aTextureCoord;\n" +
            "}\n";

    private static final String FRAGMENT_SHADER_SOURCE = "precision mediump float;" +
            "varying vec2 vTextureCoord;" +
            "" +
            "uniform sampler2D SamplerY; " +
            "uniform sampler2D SamplerUV;" +
            "" +
            "const mat3 yuv2rgb = mat3(1, 0, 1.2802,1, -0.214821, -0.380589,1, 2.127982, 0);" +
            "" +
            "void main() {    " +
            "    vec3 yuv = vec3(1.1643 * (texture2D(SamplerY, vTextureCoord).r - 0.0625)," +
            "                    texture2D(SamplerUV, vTextureCoord).r - 0.5," +
            "                    texture2D(SamplerUV, vTextureCoord).a - 0.5);" +
            "    vec3 rgb = yuv * yuv2rgb;    " +
            "    gl_FragColor = vec4(rgb, 1.0);" +
            "} ";

    private int mProgram;
    private int maPositionHandle;
    private int maTextureHandle;
    private int muSamplerYHandle;
    private int muSamplerUVHandle;

    private int[] mTextureY = new int[1];
    private int[] mTextureUV = new int[1];

    private boolean mSurfaceCreated;
    private boolean mSurfaceDestroyed;
    @SuppressWarnings("unused")
    private Context mContext;

    private int mViewWidth, mViewHeight, mViewX, mViewY;
    private boolean mFullScreenRequired;
    private int mStride;

    public YUVRenderView(Context context) {
        this(context,null);
    }

    public YUVRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mContext = context;

        mTriangleVertices = ByteBuffer.allocateDirect(TRIANFLE_VERTICES_DATA.length
                * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(TRIANFLE_VERTICES_DATA).position(0);

        mIndices = ByteBuffer.allocateDirect(INDICES_DATA.length
                * SHORT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(INDICES_DATA).position(0);
    }

    public boolean setParams(boolean fullScreenRequired, int bufferWidth, int bufferHeight,int stride)
    {
        Log.i(TAG, "[ setParams ] +");
        if (mFullScreenRequired == fullScreenRequired
                && bufferWidth == mBufferWidthY && mBufferHeightY == bufferHeight
                && stride == mStride
        ) {
            return false;
        }
        mFullScreenRequired = fullScreenRequired;

        mValidDataList.clear();

        mBufferWidthY = bufferWidth;
        mBufferHeightY = bufferHeight;

        mBufferWidthUV = (mBufferWidthY >> 1);
        mBufferHeightUV = (mBufferHeightY >> 1);

        mBufferPositionY = 0;
        mBufferPositionUV = (stride * mBufferHeightY);

        mStride = stride;

        GLES30.glViewport(0, 0, getWidth(), getHeight());
        setViewport(getWidth(), getHeight());
        Log.i(TAG, "[ setParams ] -");
        return true;
    }


    public boolean isReady(){
        return (mSurfaceCreated && !mSurfaceDestroyed);
    }

    public boolean isDestroyed(){
        return mSurfaceDestroyed;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "[surfaceDestroyed] + ");
        mSurfaceCreated   = false;
        mSurfaceDestroyed = true;
        super.surfaceDestroyed(holder);
        Log.d(TAG, "[surfaceDestroyed] - ");
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES30.glViewport(mViewX, mViewY, mViewWidth, mViewHeight);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        if (mValidDataList.size() < BUFFER_COUNT_MIN)
        {
            Log.e(TAG,"onDrawFrame no valid buffer");
            return;
        }

        byte[] newData = mValidDataList.get(0);
        if ((mBuffer == null || mBuffer.limit()!= newData.length )&& newData.length > 0 )
        {
            mBuffer = ByteBuffer.allocateDirect(newData.length);
            Log.e(TAG,"onDrawFrame buffer limit " + mBuffer.limit() + " data.length "+newData.length);
        }

        mBuffer.rewind();
        try {
            mBuffer.put(newData);
        } catch (Exception e) {
            Log.i(TAG," [onDrawFrame] the size of the buffer " + newData.length
                    + " bufferWidth = " + mBufferWidthY +" bufferHeight = " + mBufferHeightY);
            e.printStackTrace();

        }

        if(mBuffer != null){
            synchronized(this){

                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(  GLES30.GL_TEXTURE_2D, mTextureY[0]);
                if (mStride == 0){
                    Log.e(TAG," [onDrawFrame] mStride is 0 , buffer draw with stride");
                }
                Log.d(TAG," [onDrawFrame] mBufferWidthY " + mBufferWidthY + " mBufferHeightY " + mBufferHeightY);
                GLES30.glPixelStorei(   GLES30.GL_UNPACK_ROW_LENGTH,mStride);
                GLES30.glTexImage2D(   GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, mBufferWidthY, mBufferHeightY, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, mBuffer.position(mBufferPositionY));
                GLES30.glUniform1i(muSamplerYHandle, 0);

                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                GLES30.glBindTexture(  GLES30.GL_TEXTURE_2D, mTextureUV[0]);
                GLES30.glPixelStorei(   GLES30.GL_UNPACK_ROW_LENGTH,mStride / 2);
                GLES30.glTexImage2D(   GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, mBufferWidthUV, mBufferHeightUV, 0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, mBuffer.position(mBufferPositionUV));
                GLES30.glUniform1i(muSamplerUVHandle, 1);
            }
        }

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, INDICES_DATA.length, GLES30.GL_UNSIGNED_SHORT, mIndices);

        mValidDataList.clear();
       
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        setViewport(width, height);
        // GLU.gluPerspective(glUnused, 45.0f, (float)width/(float)height, 0.1f, 100.0f);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES30.glEnable( GLES30.GL_BLEND);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_DITHER);
        GLES30.glDisable(GLES30.GL_STENCIL_TEST);
        GLES30.glDisable(GL10.GL_DITHER);

        String extensions = GLES30.glGetString(GL10.GL_EXTENSIONS);
        Log.d(TAG, "OpenGL extensions=" + extensions);

        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        mProgram = createProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES30.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muSamplerYHandle = GLES30.glGetUniformLocation(mProgram, "SamplerY");
        if (muSamplerYHandle == -1) {
            throw new RuntimeException("Could not get uniform location for SamplerY");
        }
        muSamplerUVHandle = GLES30.glGetUniformLocation(mProgram, "SamplerUV");
        if (muSamplerUVHandle == -1) {
            throw new RuntimeException("Could not get uniform location for SamplerUV");
        }

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES30.glVertexAttribPointer(maPositionHandle, 3, GLES30.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES30.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        GLES30.glVertexAttribPointer(maTextureHandle, 2, GLES30.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES30.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        GLES30.glGenTextures(1, mTextureY, 0);
        GLES30.glBindTexture(  GLES30.GL_TEXTURE_2D, mTextureY[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,     GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,     GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glBindTexture(  GLES30.GL_TEXTURE_2D, GLES30.GL_NONE);

        GLES30.glGenTextures(1, mTextureUV, 0);
        GLES30.glBindTexture(  GLES30.GL_TEXTURE_2D, mTextureUV[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,     GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,     GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glBindTexture(  GLES30.GL_TEXTURE_2D, GLES30.GL_NONE);

        mSurfaceCreated = true;

        setViewport(getWidth(), getHeight());
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES30.glCreateShader(shaderType);
        if (shader != 0) {
            GLES30.glShaderSource(shader, source);
            GLES30.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
                GLES30.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GLES30.glCreateProgram();
        if (program != 0) {
            GLES30.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES30.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES30.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES30.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES30.glGetProgramInfoLog(program));
                GLES30.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void setViewport(int width, int height){
        if(mFullScreenRequired){
            mViewWidth = width;
            mViewHeight = height;
            mViewX = mViewY = 0;
        }
        else{
            float fRatio = ((float) mBufferWidthY / (float) mBufferHeightY);
            mViewWidth = (int) ((float) width / fRatio) > height ? (int) ((float) height * fRatio) : width;
            mViewHeight = (int) (mViewWidth / fRatio) > height ? height : (int) (mViewWidth / fRatio);
            mViewX = ((width - mViewWidth) >> 1);
            mViewY = ((height - mViewHeight) >> 1);
        }
    }
    private void checkGlError(String op) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private final static int BUFFER_COUNT_MIN   = 1;
    private List<byte[]>  mValidDataList = Collections.synchronizedList(new LinkedList<byte[]>());

    public void newDataArrived(final byte[] data)
    {
        synchronized (this){
            if (mValidDataList.size() < BUFFER_COUNT_MIN){
                byte[] newData = new byte[data.length];
                System.arraycopy(data, 0, newData, 0, data.length);

                mValidDataList.clear();
                mValidDataList.add(newData);
                this.requestRender();
            }else{
                Log.i(TAG,"Buffer was abandoned");
            }

        }

    }

}
