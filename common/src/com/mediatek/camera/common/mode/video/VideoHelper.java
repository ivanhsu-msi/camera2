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

package com.mediatek.camera.common.mode.video;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.provider.MediaStore;
import android.view.OrientationEventListener;

import com.mediatek.camera.CameraApplication;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.mode.video.device.IDeviceController;
import com.mediatek.camera.common.mode.video.recorder.IRecorder;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.CameraUtil.TableList;
import com.mediatek.camera.common.utils.Size;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Video helper is utility class provide some common function.
 */

public class VideoHelper {
    private static final Tag TAG = new Tag(VideoHelper.class.getSimpleName());

    private static final String KEY_PDAF_SUPPORTED = "pdaf-supported";
    private static final String VALUE_ON = "on";
    private static final String VIDEO_FORMAT_HEVC = "HEVC";
    private static final int INVALID_DURATION = -1;
    private static final int FILE_ERROR = -2;
    private static CamcorderProfile sProfile;
    private byte[] mCameraPreviewData;
    private int mOrientation = 0;
    private int mPreviewFormat;
    private long mDateTaken;

    private Lock mLock = new ReentrantLock();
    private IDeviceController mCameraDevice;
    private ICameraContext mCameraContext;
    private Activity mActivity;
    private Size mPreviewSize;
    private IApp mApp;

    private String mFileName;
    private String mFilePath;
    private String mTempPath;
    private String mTitle;
    private Handler mVideoHandler;
    private Uri mUri;
    private static final Pattern PATTERN_RELATIVE_PATH = Pattern.compile(
            "(?i)^/storage/(?:emulated/[0-9]+/|[^/]+/)(Android/sandbox/([^/]+)/)?");

    /**
     * the constructor.
     * @param context ICameraContext
     * @param app the IApp.
     * @param cameraDevice the current device.
     * @param videoHandler the video handler
     */
    public VideoHelper(ICameraContext context, IApp app, IDeviceController cameraDevice,
                       Handler videoHandler) {
        LogHelper.d(TAG, "[VideoHelper]");
        mCameraContext = context;
        mActivity = app.getActivity();
        mCameraDevice = cameraDevice;
        mApp = app;
        mVideoHandler = videoHandler;
    }
    /**
     * used to produce ContentValues for file save.
     * @param isVideo judge whether is need produce video ContentValues.
     * @param orientation the orientation.
     * @param size the picture size.
     * @return the ContentValues used to save.
     */
    public ContentValues prepareContentValues(boolean isVideo, int orientation, Size size) {
        mLock.lock();
        try {
            LogHelper.d(TAG, "[prepareContentValues] isVideo = " + isVideo
                    + " orientation = " + orientation);
            mOrientation = orientation;
            initializeCommonInfo(isVideo);
            if (isVideo) {
                return createVideoValues();
            } else {
                return createPhotoValues(size);
            }
        } finally {
            mLock.unlock();
        }
    }
    /**
     * the temp path used to save video file.
     * @return the file path.
     */
    public String getVideoTempPath() {
        mTempPath = mCameraContext.getStorageService().getFileDirectory() +
                '/' + ".videorecorder" + ".tmp";
        LogHelper.d(TAG, "[getVideoTempPath] mTempPath = " + mTempPath);
        return mTempPath;
    }

    /**
     * the file path which is the result video.
     * @return the file path.
     */
    public String getVideoFilePath() {
        LogHelper.d(TAG, "[getVideoFilePath] mFilePath = " + mFilePath);
        return mFilePath;
    }

    public String getVideoFilePath(Uri uri) {
        String path = getPath(mActivity,uri);
        LogHelper.d(TAG, "[getVideoFilePath] mFilePath = " + path);
        return path;
    }

    private String getPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return path;
    }
    /**
     * this function is used for get orientation which is set
     * to media recorder ,the return value is compute by GSensor and
     * activity display orientation.
     * @param orientation GSensor orientation
     * @param info Camera info
     * @return orientation set to recorder
     */
    public static int getRecordingRotation(int orientation, CameraInfo info) {
        int rotation = -1;
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
        } else {
            rotation = info.orientation;
        }
        LogHelper.d(TAG, "[getRecordingRotation] orientation = " +
                orientation + " info " + info + " rotation = "  + rotation);
        return rotation;
    }
    /**
     * this function is used for get orientation which is set
     * to media recorder ,the return value is compute by GSensor and
     * activity display orientation and used for api2.
     * @param orientation GSensor orientation
     * @param characteristics camera characteristics
     * @return result orientation
     */
    public static int getRecordingRotation(int orientation,
                                           CameraCharacteristics characteristics) {
        int rotation = -1;
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        boolean facingFront = characteristics.get(CameraCharacteristics.LENS_FACING)
                == CameraCharacteristics.LENS_FACING_FRONT;
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            if (facingFront) {
                rotation = (sensorOrientation - orientation + 360) % 360;
            } else {
                rotation = (sensorOrientation + orientation) % 360;
            }
        } else {
            rotation = sensorOrientation;
        }
        return rotation;
    }
    /**
     * Get camera characteristics used for api2.
     * @param activity activity.
     * @param cameraId camera id.
     * @return the result.
     */
    public CameraCharacteristics getCameraCharacteristics(Activity activity, String cameraId) {
        CameraManager camManager =
                (CameraManager) activity.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            return camManager.getCameraCharacteristics(cameraId);
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.e(TAG, "camera process killed due to getCameraCharacteristics() error");
            CameraApplication.finishAllActivity();
            Process.killProcess(Process.myPid());
        }
        return null;
    }
    /**
     * get max recording size for storage and intent limit.
     * @return the result that media recorder can record max size unit is byte.
     */
    public long getRecorderMaxSize() {
        return mCameraContext.getStorageService().getRecordStorageSpace();
    }
    /**
     * delete file.
     * @param fileName the file path
     */
    public void deleteVideoFile(String fileName) {
        File f = new File(fileName);
        if (!f.delete()) {
            LogHelper.i(TAG, "[deleteVideoFile] Could not delete " + fileName);
        }
    }
    /**
     * get the out file type.
     * @return file type
     * @param outputFileFormat the file type
     */
    public String convertOutputFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        }
        return "video/3gpp";
    }
    /**
     * use audio focus to stop audio play back when start recording.
     * @param app the IApp object.
     */

    public void pauseAudioPlayBack(IApp app) {
        LogHelper.i(TAG, "[pauseAudioPlayback]");
        AudioManager am = (AudioManager) app.getActivity().getSystemService(
                app.getActivity().AUDIO_SERVICE);
        if (am != null) {
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }
    /**
     * release audio focus after stop recording.
     * @param app the IApp object.
     */
    public void releaseAudioFocus(IApp app) {
        AudioManager am = (AudioManager) app.getActivity().getSystemService(
                app.getActivity().AUDIO_SERVICE);
        if (am != null) {
            am.abandonAudioFocus(null);
        }
    }
    /**
     * Get Preview frame call back used to get data for animation.
     * @return the preview frame call back.
     */
    public IDeviceController.PreviewCallback getPreviewFrameCallback() {
        return mPreviewCallback;
    }
    /**
     * Release the preview frame data.
     */
    public void releasePreviewFrameData() {
        mCameraPreviewData = null;
    }

    /**
     * Check the camera is need mirror or not.
     * @param cameraId current camera id.
     * @return true means need mirror.
     */
    public boolean isMirror(String cameraId) {
        boolean mirror = false;
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Integer.parseInt(cameraId), info);
            mirror = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.e(TAG, "[isMirror] camera process killed due to getCameraInfo() error");
            CameraApplication.finishAllActivity();
            Process.killProcess(Process.myPid());
        }
        return mirror;
    }

    public SurfaceChangeListener getSurfaceListener() {
        return new SurfaceChangeListener();
    }
    /**
     * Use to generate recorder spec for media recorder.
     * @param profile camcorder profile.
     * @param cameraId current camera id.
     * @param api current api.
     * @param settingManager the setting Manager
     * @return the result.
     */
    public IRecorder.RecorderSpec configRecorderSpec(CamcorderProfile profile, String cameraId,
                                                     CameraDeviceManagerFactory.CameraApi api, ISettingManager settingManager) {
        sProfile = profile;
        IRecorder.RecorderSpec recorderSpec = new IRecorder.RecorderSpec();
        recorderSpec.videoSource = MediaRecorder.VideoSource.SURFACE;
        recorderSpec.orientationHint = getRecordingRotation(mApp.getGSensorOrientation(),
                getCameraCharacteristics(mApp.getActivity(), cameraId));
        recorderSpec.isHEVC = VIDEO_FORMAT_HEVC
                .equals(settingManager.getSettingController().queryValue("key_video_format"));
        LogHelper.d(TAG, "[configRecorderSpec] recorderSpec.isHEVC = " + recorderSpec.isHEVC);
        if (VALUE_ON.equals(settingManager.getSettingController().queryValue("key_microphone"))) {
            recorderSpec.isRecordAudio = true;
            recorderSpec.audioSource = MediaRecorder.AudioSource.CAMCORDER;
        } else {
            recorderSpec.isRecordAudio = false;
        }
        recorderSpec.profile = sProfile;
        recorderSpec.maxDurationMs = 0;
        recorderSpec.maxFileSizeBytes = getRecorderMaxSize();
        recorderSpec.location = mCameraContext.getLocation();
        return recorderSpec;
    }

    /**
     * update preview size and picture size for used
     * @param previewSize the preview size
     */
    public synchronized void updatePreviewSize(Size previewSize) {
        mPreviewSize = previewSize;
    }

    public FileDescriptor getVideoFD(){
        mUri = mActivity.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, getContentValuesNewVideo());
        ParcelFileDescriptor pfd = null;
        try {
            pfd = mActivity.getContentResolver().openFileDescriptor(mUri, "w", null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return pfd.getFileDescriptor();
    }
    public Uri getVideoFileUri(){
        return mUri;
    }

    public ContentValues getContentValuesNewVideo(){
        ContentValues saveContentValues = new ContentValues();
        String fileName = createVideoName();
        String filePath = mCameraContext.getStorageService().getFileDirectory() + '/' +fileName;
        saveContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        String mime = convertOutputFormatToMimeType(sProfile.fileFormat);
        saveContentValues.put(MediaStore.Video.Media.MIME_TYPE, mime);
        saveContentValues.put(MediaStore.Video.Media.RELATIVE_PATH, extractRelativePath(filePath));
        saveContentValues.put(MediaStore.Video.Media.WIDTH, sProfile.videoFrameWidth);
        saveContentValues.put(MediaStore.Video.Media.HEIGHT, sProfile.videoFrameHeight);
        saveContentValues.put(MediaStore.Video.Media.RESOLUTION,
                Integer.toString(sProfile.videoFrameWidth) + "x"
                        + Integer.toString(sProfile.videoFrameHeight));
        saveContentValues.put(MediaStore.Video.Media.IS_PENDING, 1);
        Location location = mCameraContext.getLocation();
        if (location != null) {
            saveContentValues.put(MediaStore.Video.Media.LATITUDE, location.getLatitude());
            saveContentValues.put(MediaStore.Video.Media.LONGITUDE, location.getLongitude());
        }
        if (CameraUtil.isColumnExistInDB(mActivity, TableList.VIDEO_TABLE, "orientation")) {
            saveContentValues.put("orientation", mOrientation);
        }
        return saveContentValues;
    }

    public String extractRelativePath(String filePath) {
        if (filePath == null) return null;
        final Matcher matcher = PATTERN_RELATIVE_PATH.matcher(filePath);
        if (matcher.find()) {
            final int lastSlash = filePath.lastIndexOf('/');
            if (lastSlash == -1 || lastSlash < matcher.end()) {
                // This is a file in the top-level directory, so relative path is "/"
                // which is different than null, which means unknown path
                return "/";
            } else {
                return filePath.substring(matcher.end(), lastSlash + 1);
            }
        } else {
            return null;
        }
    }
    private ContentValues createVideoValues() {
        ContentValues values = new ContentValues();
        long duration = getDuration(mTempPath);
        String mime = convertOutputFormatToMimeType(sProfile.fileFormat);
        values.put(MediaStore.Video.Media.DURATION, duration);
        values.put(MediaStore.Video.Media.TITLE, mTitle);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, mFileName);
        values.put(MediaStore.Video.Media.DATE_TAKEN, mDateTaken);
        values.put(MediaStore.Video.Media.MIME_TYPE, mime);
        values.put(MediaStore.Video.Media.DATA, mFilePath);
        values.put(MediaStore.Video.Media.WIDTH, sProfile.videoFrameWidth);
        values.put(MediaStore.Video.Media.HEIGHT, sProfile.videoFrameHeight);
        values.put(MediaStore.Video.Media.RESOLUTION,
                Integer.toString(sProfile.videoFrameWidth) + "x"
                        + Integer.toString(sProfile.videoFrameHeight));
        values.put(MediaStore.Video.Media.SIZE, new File(mTempPath).length());
        Location location = mCameraContext.getLocation();
        if (location != null) {
            values.put(MediaStore.Video.Media.LATITUDE, location.getLatitude());
            values.put(MediaStore.Video.Media.LONGITUDE, location.getLongitude());
        }
        if (CameraUtil.isColumnExistInDB(mActivity, TableList.VIDEO_TABLE, "orientation")) {
            values.put("orientation", mOrientation);
        }
        return values;
    }

    private ContentValues createPhotoValues(Size size) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, mDateTaken);
        values.put(MediaStore.Images.ImageColumns.TITLE, mTitle);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, mFileName);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, mOrientation);
        values.put(MediaStore.Images.ImageColumns.DATA, mFilePath);
        values.put(MediaStore.Images.ImageColumns.WIDTH, size.getWidth());
        values.put(MediaStore.Images.ImageColumns.HEIGHT, size.getHeight());
        return values;
    }

    private void initializeCommonInfo(boolean isVideo) {
        mDateTaken = System.currentTimeMillis();
        mTitle = createFileTitle(isVideo, mDateTaken);
        mFileName = createFileName(isVideo, mTitle);
        mFilePath = mCameraContext.getStorageService().getFileDirectory() + '/' + mFileName;
    }
    private String createVideoName(){
        return createFileName(true, createFileTitle(true, System.currentTimeMillis()));
    }

    private String createFileTitle(boolean isVideo, long dateTaken) {
        SimpleDateFormat format;
        Date date = new Date(dateTaken);
        if (isVideo) {
            format = new SimpleDateFormat("'VID'_yyyyMMdd_HHmmss");
        } else {
            format = new  SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss_S");
        }
        return format.format(date);
    }

    private String createFileName(boolean isVideo, String  title) {
        String fileName = title + ".jpg";
        if (isVideo) {
            fileName = title + convertOutputFormatToFileExt(sProfile.fileFormat);
        }
        LogHelper.d(TAG, "[createFileName] + fileName = " + fileName);
        return fileName;
    }

    private String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        }
        return ".3gp";
    }

    private long getDuration(String fileName) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(fileName);
            return Long.valueOf(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (IllegalArgumentException e) {
            return INVALID_DURATION;
        } catch (RuntimeException e) {
            return FILE_ERROR;
        } finally {
            try {
                retriever.release();
            }catch (IOException e){
                LogHelper.e(TAG, "IOException:", e);
            }
        }
    }

    private IDeviceController.PreviewCallback mPreviewCallback = new
                                       IDeviceController.PreviewCallback() {
        @Override
        public void onPreviewCallback(byte[] data, int format, String previewCameraId) {
            if (mCameraPreviewData == null) {
                stopSwitchCameraAnimation();
                stopChangeModeAnimation();
                mApp.getAppUi().onPreviewStarted(previewCameraId);
            }
            mCameraPreviewData = data;
            mPreviewFormat = format;
        }
    };

    private int getCameraInfoOrientation(String cameraId) {
        int orientation = 0;
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Integer.parseInt(cameraId), info);
            orientation = info.orientation;
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.e(TAG, "[getCameraInfoOrientation] camera process killed due to" +
                    " getCameraInfo() error");
            CameraApplication.finishAllActivity();
            Process.killProcess(Process.myPid());
        }
        LogHelper.d(TAG, "[getCameraInfoOrientation] orientation = " + orientation);
        return orientation;
    }

    private IAppUi.AnimationData prepareAnimationData(byte[] data, int width,
                                                      int height, int format, String cameraID) {
        IAppUi.AnimationData animationData = new IAppUi.AnimationData();
        animationData.mData = data;
        animationData.mWidth = width;
        animationData.mHeight = height;
        animationData.mFormat = format;
        animationData.mOrientation = getCameraInfoOrientation(cameraID);
        animationData.mIsMirror = isMirror(cameraID);
        return animationData;
    }

    private void stopSwitchCameraAnimation() {
        if (mApp != null) {
            mApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_SWITCH_CAMERA);
        }
    }

    private void stopChangeModeAnimation() {
        mApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_SWITCH_MODE);
    }

    private class SurfaceChangeListener implements IAppUiListener.ISurfaceStatusListener {
        @Override
        public void surfaceAvailable(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "[surfaceAvailable] holder = " + surfaceObject + " width = " + width +
                    " height = " + height);
            updatePreviewSize(new Size(width, height));
            if (mVideoHandler != null) {
                mVideoHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mCameraDevice != null) {
                mCameraDevice.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }
        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "[surfaceChanged] holder = " + surfaceObject + " width = " + width +
                    " height = " + height);
            updatePreviewSize(new Size(width, height));
            if (mVideoHandler != null) {
                mVideoHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mCameraDevice != null) {
                mCameraDevice.updatePreviewSurface(surfaceObject);
            }
        }
                });
            }
        }
        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.i(TAG, "[surfaceDestroyed] holder = " + surfaceObject);
            if (mVideoHandler != null) {
                mVideoHandler.post(new Runnable() {
                    @Override
                    public void run() {
            if (mCameraDevice != null) {
                mCameraDevice.updatePreviewSurface(null);
            }
        }
                });
            }
        }
    }
}
