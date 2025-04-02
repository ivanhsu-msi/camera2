/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.common.bgservice;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import com.mediatek.camera.feature.setting.format.FormatEntry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for capture surface.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class CaptureSurface {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CaptureSurface.class.getSimpleName());
    private int mPictureWidth;
    private int mPictureHeight;
    @SuppressWarnings("deprecation")
    private int mFormat = PixelFormat.JPEG;
    private int mMaxImages = 12;
    public final Handler mCaptureHandler;
    private final Object mImageReaderSync = new Object();
    private ImageCallback mImageCallback;
    public String mCaptureType = HeifHelper.CAPTURE_TYPE_JPEG;
    private List<Integer> mBGEventCallbackQueue = new ArrayList<>();
    private List<ImageReader> mImageReaderQueue = new ArrayList<>();
    private boolean mIsCreatedByBG = false;
    private boolean mCanReleaseCaptureSurface;
    private int mPictureCount;
    public ImageReaderManager mImageReaderManager;

    //default priority is 0, cshot(1)  has higher priority than other mode
    private int mCurrentImageCallbackPriority = 0;

    /**
     * Capture image callback.
     */
    public interface ImageCallback {
        /**
         * When picture callback will be fired this function.
         *
         * @param data picture data.
         */
        void onPictureCallback(byte[] data, int format, String formatTag, int width, int height);
    }

    public CaptureSurface(Handler handler) {
        LogHelper.d(TAG, "[CaptureSurface]Construct with handler");
        mCaptureHandler = handler;
        mIsCreatedByBG = true;
        mImageReaderManager = new ImageReaderManager();
    }

    /**
     * Prepare the capture surface handler.
     */
    public CaptureSurface() {
        LogHelper.d(TAG, "[CaptureSurface]Construct");
        mCaptureHandler = new Handler(Looper.myLooper());
        mImageReaderManager = new ImageReaderManager();
    }

    /**
     * Update a new picture info,such as size ,format , max image.
     *
     * @param width    the target picture width.
     * @param height   the target picture height.
     * @param format   The format of the Image that this reader will produce.
     *                 this must be one of the {@link android.graphics.ImageFormat} or
     *                 {@link PixelFormat} constants. Note that not
     *                 all formats are supported, like ImageFormat.NV21. The default value is
     *                 PixelFormat.JPEG;
     * @param maxImage The maximum number of images the user will want to
     *                 access simultaneously. This should be as small as possible to
     *                 limit memory use. Once maxImages Images are obtained by the
     *                 user, one of them has to be released before a new Image will
     *                 become available for access through onImageAvailable().
     *                 Must be greater than 0.
     * @return if surface is changed, will return true, otherwise will false.
     */
    public boolean updatePictureInfo(int width, int height, int format, int maxImage) {
        // Check picture info whether is same as before or not.
        // if the info don't change, No need create it again.
        // Create a image reader for images of the desired size,format and max image.
        synchronized (mImageReaderSync) {
            boolean hasImageReader = mImageReaderManager.hasImageReader(width, height, format,
                    maxImage);
            LogHelper.i(TAG, "[updatePictureInfo] width = " + width + ",height = " + height + "," +
                    "format = " + format + ",maxImage = " + maxImage + ", hasImageReader = " +
                    hasImageReader + " mCaptureType = " + mCaptureType);
            if (hasImageReader && mPictureWidth == width && mPictureHeight == height &&
                    format == mFormat && maxImage == mMaxImages) {
                LogHelper.d(TAG, "[updatePictureInfo],the info : " + mPictureWidth + " x " +
                        mPictureHeight + ",format = " + format + ",maxImage = " + maxImage + " is" +
                        " " +
                        "same as before");
                return false;
            }
            // Save the new picture info.
            mPictureWidth = width;
            mPictureHeight = height;
            mFormat = format;
            mMaxImages = maxImage;
            ImageReader captureImageReader = mImageReaderManager.getImageReader(mPictureWidth,
                    mPictureHeight, mFormat, mMaxImages, mCaptureType);
            captureImageReader.setOnImageAvailableListener(mCaptureImageListener, mCaptureHandler);
        }
        return true;
    }

    public boolean updatePictureInfo(int width, int height, int format, int maxImage, String captureType) {
        mCaptureType = captureType;
        return updatePictureInfo(width, height, format, maxImage);
    }

    public boolean updatePictureInfo(int format) {
        return updatePictureInfo(mPictureWidth, mPictureHeight, format, mMaxImages, mCaptureType);

    }

    public boolean updatePictureInfo(int format, String captureType) {
        mCaptureType = captureType;
        return updatePictureInfo(mPictureWidth, mPictureHeight, format, mMaxImages, captureType);

    }

    public boolean hasNoImageReader() {
        synchronized (mImageReaderSync) {
            return mImageReaderManager.hasNoImageReader();
        }
    }

    /**
     * Get the capture surface from image reader.
     *
     * @return the surface is from image reader.
     * if don't have call the updatePictureInfo() before getSurface() will be return null.
     * such as you have calling releaseCaptureSurface(), the value is null.
     */
    public Surface getSurface() {
        synchronized (mImageReaderSync) {
            if (mImageReaderManager != null) {
                ImageReader captureImageReader = mImageReaderManager.getImageReader(mPictureWidth,
                        mPictureHeight, mFormat, mMaxImages, mCaptureType);
                if(captureImageReader != null){
                    return captureImageReader.getSurface();
                }
            }
            return null;
        }
    }

    /**
     * Generally, the ImageReader caches buffers for reuse once they have been allocated,
     * for best performance. However, sometimes it may be important to release all the cached,
     * unused buffers to save on memory.
     */
    public void discardFreeBuffers() {
        synchronized (mImageReaderSync) {
            if (mImageReaderManager != null) {
                ImageReader captureImageReader = mImageReaderManager.getImageReader(mPictureWidth,
                        mPictureHeight, mFormat, mMaxImages, mCaptureType);
                captureImageReader.discardFreeBuffers();
            }
        }
    }

    /**
     * Release the capture surface when don't need again.
     */
    public void releaseCaptureSurface() {
        synchronized (mImageReaderSync) {
            if (mImageReaderManager != null) {
                LogHelper.d(TAG, "[releaseCaptureSurface], mImageReaderManager");
                mImageReaderManager.releaseImageReader();
            }
        }
    }

    /**
     * Release the capture surface when don't need again.
     */
    public void releaseCaptureSurface(int width, int height) {
        LogHelper.d(TAG, "[releaseCaptureSurface], mCaptureImageReader width = " + width + " " +
                "height = " + height);
        synchronized (mImageReaderSync) {
            if (mImageReaderManager != null) {
                mImageReaderManager.releaseImageReader(width, height);
            }
        }
    }

    public void setFormat(String format) {
        if (format == null) {
            LogHelper.e(TAG, "[setFormat], null pointer! mFormatTag: " + format);
            return;
        }
        mCaptureType = format;
        LogHelper.d(TAG, "[setFormat], mFormatTag: " + mCaptureType);
    }

    public String getCaptureType() {
        return mCaptureType;
    }
    /**
     * When activity destroy, release the resource.
     */
    public void release() {
        releaseCaptureSurface();
    }

    /**
     * cshot has higher priority than other mode to get callback
     * @param captureCallback
     */
    public void setCaptureCallback(ImageCallback captureCallback) {
        this.setCaptureCallback(captureCallback,0);
    }

    public void setCaptureCallback(ImageCallback captureCallback,int priority) {
        synchronized (mImageReaderSync) {
            if (priority >= mCurrentImageCallbackPriority) {
                mImageCallback = captureCallback;
                mCurrentImageCallbackPriority = priority;
            }

            //This used for clear cshot callback
            if (captureCallback == null && priority == 0) {
                mCurrentImageCallbackPriority = priority;
            }
        }
    }

    public void increasePictureNum() {
        synchronized (mImageReaderSync) {
            mPictureCount++;
            LogHelper.d(TAG, "<increasePictureNum> captureSurface:" + this
                    + ", mPictureCount = " + mPictureCount);
        }
    }

    public void decreasePictureNum() {
        synchronized (mImageReaderSync) {
            if (mPictureCount > 0) {
                mPictureCount--;
                LogHelper.d(TAG, "<decreasePictureNum> captureSurface:" + this
                        + ", mPictureCount = " + mPictureCount);
            }
        }
    }

    public int getPictureNumLeft() {
        return mPictureCount;
    }

    public void releaseCaptureSurfaceLater(boolean shouldRelease) {
        mCanReleaseCaptureSurface = shouldRelease;
    }

    public boolean shouldReleaseCaptureSurface() {
        return mCanReleaseCaptureSurface;
    }

    public void addBGEventCallbackQueue(int frameNumber) {
        synchronized (mBGEventCallbackQueue) {
            mBGEventCallbackQueue.add(frameNumber);
            LogHelper.d(TAG, "addBGEventCallbackQueue");
        }
    }

    public int getImageReaderId() {
        synchronized (mImageReaderSync) {
            int id = -1;
            if (mImageReaderManager != null) {
                id = mImageReaderManager.getImageReaderId();
                LogHelper.d(TAG, "getImageReaderId id = " + Integer.toHexString(id));
            }
            return id;
        }
    }

    public boolean hasTheImageReader(int imageReaderId) {
        synchronized (mImageReaderSync) {
            return mImageReaderManager.hasTheImageReader(imageReaderId);
        }
    }

    public void notifyImageReader(int imageReaderID) {
        String captureType = mImageReaderManager.getCaptureTypeByImageReaderId(imageReaderID);
        LogHelper.d(TAG, "notifyImageReader imageReaderID = " + imageReaderID
                + " captureType = " + captureType);
        if (mIsCreatedByBG && BGServiceKeeper.supportByBGService(captureType)) {
            synchronized (mImageReaderSync) {
                LogHelper.d(TAG, "notifyImageReader mImageReaderQueue.size = "
                        + mImageReaderQueue.size());
                if (!mImageReaderQueue.isEmpty() && mImageCallback != null) {
                    Image image = mImageReaderQueue.get(0).acquireNextImage();
                    LogHelper.d(TAG, "notifyImageReader ImageReader = " + mImageReaderQueue.get(0)
                            + ", image = " + image);
                    mImageReaderQueue.remove(0);
                    synchronized (mBGEventCallbackQueue) {
                        LogHelper.d(TAG, "notifyImageReader, removeBGEventCallbackQueue");
                        mBGEventCallbackQueue.remove(0);
                    }
                    if (image == null) {
                        LogHelper.w(TAG, "[notifyImageReader] image buffer is null");
                        return;
                    }
                    int format = image.getFormat();
                    int width = image.getWidth();
                    int height = image.getHeight();
                    mImageCallback.onPictureCallback(getBuffer(image, captureType),
                            format, captureType, width, height);
                }
            }
        }
    }

    public final OnImageAvailableListener mCaptureImageListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            String captureFormat = mImageReaderManager.getCaptureType(imageReader);
            LogHelper.d(TAG, "[onImageAvailable] mIsCreatedByBG = " + mIsCreatedByBG
                    + ", mBGEventCallbackQueue.size = " + mBGEventCallbackQueue.size()
                    + ", imageReader = " + imageReader +" captureFormat = " + captureFormat);
            if (mIsCreatedByBG && BGServiceKeeper.supportByBGService(captureFormat)) {
                if (mBGEventCallbackQueue.isEmpty()) {
                    synchronized (mImageReaderSync) {
                        mImageReaderQueue.add(imageReader);
                    }
                } else {
                    synchronized (mImageReaderSync) {
                        if (mImageCallback != null) {
                            Image image = imageReader.acquireNextImage();
                            synchronized (mBGEventCallbackQueue) {
                                if (!mBGEventCallbackQueue.isEmpty()) {
                                    LogHelper.d(TAG, "[onImageAvailable], remove CallbackQueue");
                                    mBGEventCallbackQueue.remove(0);
                                }
                            }
                            if (image == null) {
                                LogHelper.w(TAG, "[onImageAvailable] image buffer is null");
                                return;
                            }
                            int format = image.getFormat();
                            int width = image.getWidth();
                            int height = image.getHeight();
                            mImageCallback.onPictureCallback(getBuffer(image, captureFormat),
                                    format, captureFormat, width, height);
                        }
                    }
                }
            } else {
                synchronized (mImageReaderSync) {
                    if (mImageCallback != null) {
                        Image image = imageReader.acquireNextImage();
                        if (image == null) {
                            LogHelper.w(TAG, "[onImageAvailable] image buffer is null");
                            return;
                        }
                        int format = image.getFormat();
                        int width = image.getWidth();
                        int height = image.getHeight();
                        mImageCallback.onPictureCallback(getBuffer(image, captureFormat),
                                format, captureFormat, width, height);
                    }
                }
            }
        }
    };

    private byte[] getBuffer(Image image, String captureType) {
        synchronized (mImageReaderSync) {
            byte[] imageBuffer = null;
            LogHelper.i(TAG, "[getBuffer] image.getFormat = " + image.getFormat()
                    + " image.getWidth = " + image.getWidth() + " image.getHeight = "
                    + image.getHeight() + " format = " + image.getFormat()
                    + " captureType = " + captureType);

            if (HeifHelper.CAPTURE_TYPE_HEIF.equalsIgnoreCase(captureType)) {
                if (FormatEntry.HEIF_FLOW == FormatEntry.HEIF_AOSP_FLOW){
                    imageBuffer = HeifHelper.getHeicBuffer(image);
                }else{
                    imageBuffer = ThumbnailHelper.getDataFromImage(image);
                }
            } else if (HeifHelper.CAPTURE_TYPE_JPEG.equalsIgnoreCase(captureType)) {
                Image.Plane plane = image.getPlanes()[0];
                ByteBuffer buffer = plane.getBuffer();
                LogHelper.d(TAG, "<getBuffer> image = " + image
                        + ", buffer.remaining() = " + buffer.remaining()
                        + ", plane.getPixelStride()" + plane.getPixelStride());
                imageBuffer = new byte[buffer.remaining()];
                buffer.get(imageBuffer);
                buffer.rewind();
            } else if (ThumbnailHelper.CAPTURE_TYPE_THUMBNAIL.equalsIgnoreCase(captureType)) {
                imageBuffer = ThumbnailHelper.getYUVBuffer(image);
            } else {
                throw new RuntimeException("Unsupported image format.");
            }
            image.close();
            return imageBuffer;
        }
    }

}
