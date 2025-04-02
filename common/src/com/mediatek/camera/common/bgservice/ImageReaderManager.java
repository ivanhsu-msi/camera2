package com.mediatek.camera.common.bgservice;

import android.media.ImageReader;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ImageReaderManager {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            ImageReaderManager.class.getSimpleName());
    public int mCurrentImageReaderWidth;
    public int mCurrentImageReaderHeight;
    public ImageReader mCurrentImageReader;
    public ArrayList mImageReaderList = null;
    public static final String CAPTURE_TYPE_JPEG = "jpeg";
    private Lock mLockImageReaderList = new ReentrantLock();
    public ImageReaderManager() {
        mImageReaderList = new ArrayList();
    }

    public int getImageReaderIndex() {
        int size = mImageReaderList.size();
        int index = -1;
        for (int i = 0; i < size; i++) {
            ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
            if (temp != null && temp.mImageReader == mCurrentImageReader) {
                index = i;
                break;
            }
        }
        LogHelper.d(TAG, "[getImageReaderIndex] index = " + index);
        return index;
    }

    public int getImageReaderId() {
        return mCurrentImageReader.hashCode();
    }

    public int getImageReaderId(int width, int height, int format, int maxImages, String type) {
        ImageReader imageReader = getImageReader(width, height, format, maxImages, type);
        int id = -1;
        if (imageReader != null) {
            id = imageReader.hashCode();
        }
        LogHelper.d(TAG, "[getImageReaderId] imageReaderId = " + id);
        return id;
    }

    public boolean hasImageReader(int width, int height, int format, int maxImages) {
        int size = mImageReaderList.size();
        ImageReaderSet imageReaderSet = null;
        for (int i = 0; i < size; i++) {
            ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
            if (temp != null && temp.mWidth == width && temp
                    .mHeight == height && temp.mFormat == format && temp.mMaxImages == maxImages) {
                imageReaderSet = temp;
                break;
            }
        }
        return imageReaderSet != null;
    }

    public boolean hasTheImageReader(int imageReaderId) {
        int size = mImageReaderList.size();
        for (int i = 0; i < size; i++) {
            ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
            if (temp.mImageReader.hashCode() == imageReaderId) {
                return true;
            }
        }
        return false;
    }

    public String getCaptureTypeByImageReaderId(int imageReaderID) {
        mLockImageReaderList.lock();
        int size = mImageReaderList.size();
        // CAPTURE_TYPE_JPEG is default capture type
        String captureType = CAPTURE_TYPE_JPEG;
        try {
            for (int i = 0; i < size; i++) {
                ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
                if (temp.mImageReader.hashCode() == imageReaderID) {
                    captureType = temp.mCaptureType;
                    break;
                }
            }
        }finally {
            mLockImageReaderList.unlock();
        }
        if (captureType == null) {
            LogHelper.e(TAG, "[getCaptureTypeByImageReaderId] capture id == null!");
            captureType = CAPTURE_TYPE_JPEG;
        }
        return captureType;
    }

    public String getCaptureType(ImageReader imageReader) {
        if (imageReader == null) {
            return null;
        }
        int imageReaderId = imageReader.hashCode();
        return getCaptureTypeByImageReaderId(imageReaderId);
    }

    public boolean hasNoImageReader() {
        return mImageReaderList.isEmpty();
    }

    public ImageReader getImageReader(int width, int height, int format, int maxImages, String type) {
        int size = mImageReaderList.size();
        ImageReaderSet imageReaderSet = null;
        for (int i = 0; i < size; i++) {
            ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
            if (temp != null && temp.mWidth == width
                    && temp.mHeight == height
                    && temp.mFormat == format
                    && temp.mMaxImages == maxImages
                    && temp.mCaptureType == type) {
                imageReaderSet = temp;
                break;
            }
        }
        if (imageReaderSet != null) {
            LogHelper.d(TAG, "[getImageReader] get ImageReader = " + imageReaderSet.mImageReader
                    + " width = " + imageReaderSet.mWidth
                    + " height = " + imageReaderSet.mHeight
                    + " format = " + format
                    + " maxImage = " + maxImages
                    + " captureType = " + type);
            mCurrentImageReader = imageReaderSet.mImageReader;
        } else {
            ImageReaderSet imageReaderSetTemp = new ImageReaderSet(width, height, format,
                    maxImages, type);
            mImageReaderList.add(imageReaderSetTemp);
            LogHelper.d(TAG, "[getImageReader] new ImageReader = " + imageReaderSetTemp.mImageReader
                    + " width = " + imageReaderSetTemp.mWidth
                    + " height = " + imageReaderSetTemp.mHeight
                    + " format = " + format
                    + " maxImage = " + maxImages
                    + " captureType = " + type);
            mCurrentImageReader = imageReaderSetTemp.mImageReader;
        }
        mCurrentImageReaderWidth = width;
        mCurrentImageReaderHeight = height;
        return mCurrentImageReader;
    }

    public void releaseImageReader() {
        mLockImageReaderList.lock();
        try {
            int size = mImageReaderList.size();
            for (int i = 0; i < size; i++) {
                ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
                LogHelper.d(TAG, "[releaseImageReader] release ImageReader = " + temp
                        .mImageReader + " width = " + temp.mWidth + " height = " + temp.mHeight);
                if(temp.mImageReader != null){
                    temp.mImageReader.close();
                }
            }
            mImageReaderList.clear();
        }finally {
            mLockImageReaderList.unlock();
        }
    }

    public void releaseImageReader(int width, int height, int format, int maxImages) {
        int size = mImageReaderList.size();
        for (int i = 0; i < size; i++) {
            ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
            if (temp != null && temp.mWidth == width && temp.mHeight == height
                    && temp.mFormat == format && temp.mMaxImages == maxImages) {
                mImageReaderList.remove(i);
                LogHelper.d(TAG, "[releaseImageReader] release ImageReader = " + temp
                        .mImageReader + " width = " + temp.mWidth + " height = " + temp.mHeight);
                temp.mImageReader.close();
                break;
            }
        }
    }

    public void releaseImageReader(int width, int height) {
        int size = mImageReaderList.size();
        for (int i = 0; i < size; i++) {
            ImageReaderSet temp = (ImageReaderSet) mImageReaderList.get(i);
            if (temp != null && temp.mWidth == width && temp
                    .mHeight == height) {
                mImageReaderList.remove(i);
                temp.mImageReader.close();
                break;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            releaseImageReader();
        } finally {
            super.finalize();
        }
    }

    private class ImageReaderSet {
        public int mWidth;
        public int mHeight;
        public int mFormat;
        public int mMaxImages;
        public String mCaptureType;
        public ImageReader mImageReader;

        ImageReaderSet(int width, int height, int format, int maxImages, String type) {
            mWidth = width;
            mHeight = height;
            mFormat = format;
            mMaxImages = maxImages;
            mCaptureType = type;
            if(width > 0 && height > 0){
                mImageReader = ImageReader.newInstance(width, height, format,
                    maxImages);
            }
        }
    }
}