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
package com.mediatek.camera.feature.mode.vsdof.video;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.View;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI;
import com.mediatek.camera.common.mode.video.videoui.IVideoUI.VideoUIState;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.storage.IStorageService;
import com.mediatek.camera.common.storage.IStorageService.IStorageStateListener;
import com.mediatek.camera.common.storage.MediaSaver.MediaSaverListener;
import com.mediatek.camera.common.utils.BitmapCreator;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.feature.mode.vsdof.photo.SdofPhotoHelper;
import com.mediatek.camera.feature.mode.vsdof.video.device.ISdofVideoDeviceController;
import com.mediatek.camera.feature.mode.vsdof.video.device.SdofDeviceControllerFactory;
import com.mediatek.camera.feature.mode.vsdof.video.recorder.ISdofRecorder;
import com.mediatek.camera.feature.mode.vsdof.video.recorder.SdofNormalRecorder;
import com.mediatek.camera.feature.mode.vsdof.view.SdofViewCtrl;
import com.mediatek.camera.portability.SystemProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static com.mediatek.camera.feature.setting.dualcamerazoom.IDualZoomConfig.KEY_DUAL_ZOOM;

/**
 * SdofVideoMode is use for video record,user can extend it
 * to implement itself features.
 */
public class SdofVideoMode extends CameraModeBase {

    private static final Tag TAG = new Tag(SdofVideoMode.class.getSimpleName());
    private static final String SCENE_MODE_AUTO = "auto-scene-detection";
    private static final String VIDEO_STATUS_KEY = "key_video_status";
    private static final String KEY_STOP_RECORDING = "stop-recording";
    private static final String SCENE_MODE_FIREWORKS = "fireworks";
    private static final String SCENE_MODE_KEY = "key_scene_mode";
    private static final String KEY_SLOW_MOTION_QUALITY = "key_slow_motion_quality";
    private static final String KEY_RECORDING = "recording";
    private static final String KEY_PREVIEW = "preview";
    private static final String KEY_HDR10_PLUS = "key_hdr10";

    private static final int HANDLER_STOP_RECORDING = 1;
    private static final int VIDEO_BAD_PERFORMANCE_AUTO_STOP = 1;
    private static final int VIDEO_RECORDING_NOT_AVAILABLE = 2;
    private static final int VIDEO_RECORDING_ERROR = 4;
    private static final int REACH_SIZE_LIMIT = 5;

    private StatusMonitor.StatusResponder mVideoStatusResponder;
    private ConditionVariable mWaitStopRecording = new ConditionVariable(true);
    private Lock mResumePauseLock = new ReentrantLock();
    private Lock mLock = new ReentrantLock();
    private boolean mCanPauseResumeRecording = false;
    protected boolean mIsParameterExtraCanUse = false;
    private boolean mCanTakePicture = true;
    private int mOrientationHint = 0;

    protected ModeState mModeState = ModeState.STATE_PAUSED;
    protected Surface mSurface = null;

    protected VideoState mVideoState = VideoState.STATE_UNKNOWN;
    protected ISdofVideoDeviceController mISdofDeviceController;
    protected ISettingManager mSettingManager;
    protected IStorageService mStorageService;
    protected ICameraContext mCameraContext;
    protected SdofVideoHelper mVideoHelper;
    protected Handler mVideoHandler;
    protected ISdofRecorder mRecorder;
    protected IVideoUI mVideoUi;
    protected String mCameraId;
    protected IAppUi mAppUi;
    protected IApp mApp;
    protected SdofPhotoHelper mSdofPhotoHelper;
    private SdofViewCtrl mSdofViewCtrl = new SdofViewCtrl();

    /**
     * enum video state used to indicate the video mode state.
     */
    protected enum VideoState {
        STATE_UNKNOWN,
        STATE_PREVIEW,
        STATE_PRE_RECORDING,
        STATE_RECORDING,
        STATE_PAUSED,
        STATE_PRE_SAVING,
        STATE_SAVING,
        STATE_REVIEW_UI
    }

    /**
     * enum mode state used to indicate the current mode state.
     */
    protected enum ModeState {
        STATE_RESUMED,
        STATE_PAUSED
    }

    @Override
    public void init(@Nonnull IApp app,
                     @Nonnull ICameraContext cameraContext, boolean isFromLaunch) {
        LogHelper.i(TAG, "[init]");
        super.init(app, cameraContext, isFromLaunch);
        mVideoHandler = new VideoHandler(Looper.myLooper());

        mCameraContext = cameraContext;
        mApp = app;
        mSdofPhotoHelper = new SdofPhotoHelper(cameraContext);
        mSdofViewCtrl.setViewChangeListener(mViewChangeListener);
        mSdofViewCtrl.init(app);
        initVideoVariables();
        doInitMode();
    }

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        LogHelper.i(TAG, "[resume]");
        mSdofViewCtrl.showView();
        mSdofViewCtrl.onOrientationChanged(mIApp.getGSensorOrientation());
        super.resume(deviceUsage);
        updateModeState(ModeState.STATE_RESUMED);
        doInitDeviceManager();
        initVideoVariables();
        updateVideoState(VideoState.STATE_UNKNOWN);
        doResumeMode();
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        LogHelper.i(TAG, "[pause]");
        super.pause(nextModeDeviceUsage);
        mSdofViewCtrl.unInit();
        updateModeState(ModeState.STATE_PAUSED);
        doPauseMode(mNeedCloseCameraIds);
    }

    @Override
    public void unInit() {
        LogHelper.i(TAG, "[unInit]");
        super.unInit();
        release();
    }

    @Override
    protected ISettingManager getSettingManager() {
        return mSettingManager;
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] new id = " + newCameraId + " old id = " + mCameraId);
        super.onCameraSelected(newCameraId);
        if (canSelectCamera(newCameraId)) {
            mIApp.getAppUi().onCameraSelected(newCameraId);
            mISdofDeviceController.setPreviewCallback(null, getPreviewStartCallback());
            doCameraSelect(newCameraId);
            return true;
        } else {
            mAppUi.applyAllUIEnabled(true);
            return false;
        }
    }

    @Override
    public boolean onUserInteraction() {
        switch (getVideoState()) {
            case STATE_PREVIEW:
            case STATE_UNKNOWN:
            case STATE_PRE_SAVING:
            case STATE_SAVING:
            case STATE_REVIEW_UI:
                super.onUserInteraction();
                return true;
            case STATE_PRE_RECORDING:
            case STATE_RECORDING:
            case STATE_PAUSED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onShutterButtonClick() {
        LogHelper.i(TAG, "video onShutterButtonClick mVideoState = " + mVideoState);
        switch (getVideoState()) {
            case STATE_PREVIEW:
                if (getModeState() == ModeState.STATE_RESUMED) {
                    mAppUi.applyAllUIEnabled(false);
                    startRecording();
                }
                return true;
            case STATE_UNKNOWN:
            case STATE_PRE_RECORDING:
            case STATE_PRE_SAVING:
            case STATE_REVIEW_UI:
            case STATE_SAVING:
                return true;
            case STATE_RECORDING:
            case STATE_PAUSED:
                stopRecording();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onBackPressed() {
        LogHelper.d(TAG, "onBackPressed mVideoState = " + mVideoState);
        switch (getVideoState()) {
            case STATE_UNKNOWN:
            case STATE_SAVING:
            case STATE_PRE_SAVING:
            case STATE_PRE_RECORDING:
                return true;
            case STATE_PAUSED:
            case STATE_RECORDING:
                mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        mSdofViewCtrl.onOrientationChanged(orientation);
        if (mVideoUi != null) {
            mVideoUi.updateOrientation(orientation);
        }
    }

    protected CamcorderProfile getProfile() {
        return mISdofDeviceController.getCamcorderProfile();
    }

    protected boolean initRecorder(boolean isStartRecording) {
        LogHelper.d(TAG, "[initRecorder]");
        releaseRecorder();
        mRecorder = new SdofNormalRecorder();
        try {
            mRecorder.init(configRecorderSpec(isStartRecording));
            initForHal3(isStartRecording);
        } catch (RuntimeException e) {
            e.printStackTrace();
            releaseRecorder();
            return false;
        }
        return true;
    }

    protected void initVideoUi() {
        mVideoUi = mAppUi.getVideoUi();
    }

    protected void initCameraDevice(CameraApi api) {
        mISdofDeviceController = SdofDeviceControllerFactory.createDeviceCtroller(
                mApp.getActivity(), api, mCameraContext);
    }

    protected void addFileToMediaStore() {
        mCameraContext.getMediaSaver().addSaveRequest(
                mVideoHelper.getVideoFilePath(mVideoHelper.getVideoFileUri()),
                mVideoHelper.getVideoFileUri(),mFileSavedListener);
    }

    protected ISdofRecorder.RecorderSpec modifyRecorderSpec(
            ISdofRecorder.RecorderSpec recorderSpec, boolean isRecording) {
        if(isRecording) {
            recorderSpec.outFileDes = mVideoHelper.getVideoFD();
        }else {
            recorderSpec.outFilePath = mVideoHelper.getVideoTempPath();
        }
        return recorderSpec;
    }

    protected IVideoUI.UISpec modifyUISpec(IVideoUI.UISpec spec) {
        return spec;
    }

    protected ContentValues modifyContentValues(ContentValues contentValues) {
        return contentValues;
    }

    protected void updateVideoState(VideoState state) {
        LogHelper.d(TAG, "[updateVideoState] new state = " + state + " old state =" + mVideoState);
        mLock.lock();
        try {
            mVideoState = state;
        } finally {
            mLock.unlock();
        }
    }

    protected VideoState getVideoState() {
        mLock.lock();
        try {
            return mVideoState;
        } finally {
            mLock.unlock();
        }
    }

    private void updateModeState(ModeState state) {
        LogHelper.d(TAG, "[updateModeState] new state = " + state + " old state =" + mModeState);
        mResumePauseLock.lock();
        try {
            mModeState = state;
        } finally {
            mResumePauseLock.unlock();
        }
    }

    private ModeState getModeState() {
        mResumePauseLock.lock();
        try {
            return mModeState;
        } finally {
            mResumePauseLock.unlock();
        }
    }

    protected Relation getPreviewedRestriction() {
        Relation relation = SdofVideoRestriction.getPreviewRelation().getRelation(KEY_PREVIEW,
                true);
        List<String> qualityValues =
                mSettingManager.getSettingController().querySupportedPlatformValues(
                        "key_video_quality");
        relation.addBody("key_video_quality", "" + qualityValues.get(0)
                , "" + qualityValues.get(0));

        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                mApp.getActivity())
                .getDeviceDescriptionMap()
                .get(mCameraId);
        CameraCharacteristics.Key<int[]> zoomSets = deviceDescription.getKeyVsdofKeyOpticalZoomSets();
        if (zoomSets != null) {
            relation.addBody(KEY_DUAL_ZOOM, "on", "on");
        }

        if (!deviceDescription.isFps60SdofSupprot()) {
            LogHelper.d(TAG, "[getPreviewedRestriction] set Fps60 off in stereo" );
            relation.addBody("key_fps60", "off", "off");
        }

        return relation;
    }

    protected List<Relation> getRecordedRestriction(boolean isRecording) {
        List<Relation> relationList = new ArrayList<>();
        return relationList;
    }

    protected void release() {
        releaseRecorder();
        if (mVideoUi != null) {
            mVideoUi.unInitVideoUI();
            mVideoUi = null;
        }
        if (mVideoHandler != null) {
            mVideoHandler.removeCallbacksAndMessages(null);
        }
        if (mISdofDeviceController != null) {
            mISdofDeviceController.release();
            mISdofDeviceController = null;
        }
        deleteCurrentFile();
    }

    protected MediaSaverListener mFileSavedListener = new MediaSaverListener() {
        @Override
        public void onFileSaved(Uri uri) {
            LogHelper.d(TAG, "[onFileSaved] uri = " + uri);
            mApp.notifyNewMedia(uri, true);
            updateThumbnail(uri);
            if (VideoState.STATE_SAVING == getVideoState()) {
                updateVideoState(VideoState.STATE_PREVIEW);
            }
            mAppUi.applyAllUIEnabled(true);
            mAppUi.hideSavingDialog();
            mAppUi.applyAllUIVisibility(View.VISIBLE);
        }
    };

    protected MediaSaverListener mVssSavedListener = new MediaSaverListener() {
        @Override
        public void onFileSaved(Uri uri) {
            LogHelper.d(TAG, "[onFileSaved] mVssSavedListener uri = " + uri);
            mApp.notifyNewMedia(uri, true);
        }
    };

    protected IStorageStateListener mStorageStateListener = new IStorageStateListener() {
        @Override
        public void onStateChanged(int storageState, Intent intent) {
            if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
                LogHelper.i(TAG, "[onStateChanged] storage out service Intent.ACTION_MEDIA_EJECT");
                mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
            }
        }
    };


    protected ISdofVideoDeviceController.DeviceCallback getPreviewStartCallback() {
        return mPreviewStartCallback;
    }

    /**
     * use to handle some thing for video mode.
     */
    private class VideoHandler extends Handler {
        /**
         * the construction method.
         */
        VideoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "[handleMessage] msg = " + msg.what);
            switch (msg.what) {
                case HANDLER_STOP_RECORDING:
                    stopRecording();
                    break;
                default:
                    break;
            }
        }
    }

    private ISdofVideoDeviceController.SettingConfigCallback mSettingConfigCallback =
            new ISdofVideoDeviceController.SettingConfigCallback() {
                @Override
                public void onConfig(Size previewSize) {
                    onSettingConfigCallback(previewSize);
                    mVideoHelper.releasePreviewFrameData();
                    mVideoHelper.updatePreviewSize(previewSize);
                    mAppUi.setPreviewSize(previewSize.getWidth(),
                            previewSize.getHeight(), mVideoHelper.getSurfaceListener());
                    initRecorderForHal3();
                }
            };

    protected void onSettingConfigCallback(Size previewSize) {

    }

    private ISdofVideoDeviceController.RestrictionProvider mRestrictionProvider
            = new ISdofVideoDeviceController.RestrictionProvider() {
        @Override
        public Relation getRestriction() {
            return getPreviewedRestriction();
        }
    };

    private void updateThumbnail() {
        Bitmap bitmap = BitmapCreator.createBitmapFromVideo(
                mVideoHelper.getVideoFilePath(), mAppUi.getThumbnailViewWidth());
        if (bitmap != null) {
            mAppUi.updateThumbnail(bitmap);
        }
    }

    private void updateThumbnail(Uri uri) {
        Bitmap bitmap = BitmapCreator.createBitmapFromVideo(
                mVideoHelper.getVideoFilePath(uri), mAppUi.getThumbnailViewWidth());
        if (bitmap != null) {
            mAppUi.updateThumbnail(bitmap);
        }
    }

    private void deleteCurrentFile() {
        if (mVideoHelper != null && mVideoHelper.getVideoTempPath() != null) {
            mVideoHelper.deleteVideoFile(mVideoHelper.getVideoTempPath());
        }
    }

    private void startRecording() {
        LogHelper.d(TAG, "[startRecording] + ");
        if (getModeState() == ModeState.STATE_PAUSED) {
            LogHelper.e(TAG, "[startRecording] error mode state is paused");
            return;
        }
        if (mCameraContext.getStorageService().getRecordStorageSpace() <= 0) {
            LogHelper.e(TAG, "[startRecording] storage is full");
            mAppUi.applyAllUIEnabled(true);
            return;
        }
        if (!mISdofDeviceController.isReadyForCapture()) {
            LogHelper.i(TAG, "[startRecording] not ready for capture");
            mAppUi.applyAllUIEnabled(true);
            return;
        }
        updateVideoState(VideoState.STATE_PRE_RECORDING);
        mISdofDeviceController.postRecordingRestriction(getRecordedRestriction(true), false);
        if (!initRecorder(true)) {
            initRecorderFail();
            return;
        }
        prepareStartRecording();
        boolean startSuccess = true;
        try {
            mRecorder.start();
            mWaitStopRecording.close();
        } catch (RuntimeException e) {
            startSuccess = false;
            startRecordingFail();
            e.printStackTrace();
        }
        if (!startSuccess) {
            mVideoStatusResponder.statusChanged(VIDEO_STATUS_KEY, KEY_PREVIEW);
            return;
        }
        mApp.enableKeepScreenOn(true);
        mCanPauseResumeRecording = true;
        updateVideoState(VideoState.STATE_RECORDING);
        if (!mIsParameterExtraCanUse) {
            mVideoUi.updateUIState(VideoUIState.STATE_RECORDING);
        }
        LogHelper.d(TAG, "[startRecording] - ");
    }

    private void stopRecording() {
        LogHelper.d(TAG, "[stopRecording]+ VideoState = " + mVideoState);
        if (getVideoState() == VideoState.STATE_RECORDING
                || getVideoState() == VideoState.STATE_PAUSED) {
            prepareStopRecording();
            boolean isNeedSaveVideo = true;
            try {
                LogHelper.i(TAG, "[stopRecording] media recorder stop + ");
                mRecorder.stop();
                LogHelper.i(TAG, "[stopRecording] media recorder stop - ");
            } catch (RuntimeException e) {
                deleteCurrentFile();
                e.printStackTrace();
                isNeedSaveVideo = false;
            }
            
            doAfterRecorderStoped(isNeedSaveVideo);
            mApp.enableKeepScreenOn(false);
            if (getModeState() == ModeState.STATE_RESUMED) {
                mISdofDeviceController.postRecordingRestriction(getRecordedRestriction(false),
                        true);
                updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
            }
        }
        mWaitStopRecording.open();
        LogHelper.d(TAG, "[stopRecording] -");
    }

    private void initRecorderFail() {
        mVideoUi.showInfo(VIDEO_RECORDING_ERROR);
        updateVideoState(VideoState.STATE_PREVIEW);
        mAppUi.applyAllUIEnabled(true);
        mISdofDeviceController.postRecordingRestriction(getRecordedRestriction(false), true);
    }

    private void prepareStartRecording() {
        mVideoHelper.pauseAudioPlayBack(mApp);
        updateModeDeviceState(MODE_DEVICE_STATE_RECORDING);
        mVideoStatusResponder.statusChanged(VIDEO_STATUS_KEY, KEY_RECORDING);
        mCanTakePicture = true;
        mCanPauseResumeRecording = false;
        mVideoUi.initVideoUI(configUISpec());
        mAppUi.applyAllUIVisibility(View.GONE);
        mAppUi.setUIVisibility(IAppUi.INDICATOR, View.VISIBLE);
        mAppUi.setUIVisibility(IAppUi.PREVIEW_FRAME, View.VISIBLE);
        mAppUi.setUIVisibility(IAppUi.SCREEN_HINT, View.VISIBLE);
        mISdofDeviceController.startRecording();
        mVideoUi.updateUIState(VideoUIState.STATE_PRE_RECORDING);
        mVideoUi.updateOrientation(mApp.getGSensorOrientation());
    }

    private void startRecordingFail() {
        releaseRecorder();
        updateVideoState(VideoState.STATE_PREVIEW);
        mVideoUi.updateUIState(VideoUIState.STATE_PREVIEW);
        mAppUi.applyAllUIVisibility(View.VISIBLE);
        mAppUi.applyAllUIEnabled(true);
        mVideoUi.showInfo(VIDEO_RECORDING_ERROR);
    }

    private void prepareStopRecording() {
        updateVideoState(VideoState.STATE_PRE_SAVING);
        mAppUi.applyAllUIEnabled(false);
        mAppUi.setUIVisibility(IAppUi.INDICATOR, View.INVISIBLE);
        mAppUi.setUIVisibility(IAppUi.PREVIEW_FRAME, View.INVISIBLE);
        mAppUi.setUIVisibility(IAppUi.SCREEN_HINT, View.INVISIBLE);
        mAppUi.showSavingDialog(null, true);
        mVideoHelper.releaseAudioFocus(mApp);
        mVideoUi.updateUIState(VideoUIState.STATE_PREVIEW);
        mVideoStatusResponder.statusChanged(VIDEO_STATUS_KEY, KEY_PREVIEW);
        mISdofDeviceController.stopRecording();
        mCanPauseResumeRecording = false;
    }

    private void doAfterRecorderStoped(boolean isNeedSaveVideo) {
        if (isNeedSaveVideo) {
            mApp.enableKeepScreenOn(true);
            updateVideoState(VideoState.STATE_SAVING);
            addFileToMediaStore();
        } else {
            mAppUi.hideSavingDialog();
            mAppUi.applyAllUIVisibility(View.VISIBLE);
            mAppUi.applyAllUIEnabled(true);
            updateVideoState(VideoState.STATE_PREVIEW);
        }
    }

    private void releaseRecorder() {
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void initRecorderForHal3() {
        initRecorder(false);
    }

    private void initForHal3(boolean isStartRecording) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mSurface == null) {
                mSurface = MediaCodec.createPersistentInputSurface();
            }
            mRecorder.getMediaRecorder().setInputSurface(mSurface);
        }
        try {
            mRecorder.prepare();
        } catch (RuntimeException e) {
            if (!isStartRecording) {
                mISdofDeviceController.configCamera(null, false);
            }
            throw new RuntimeException(e);
        }
        if (mSurface != null) {
            mISdofDeviceController.configCamera(mSurface, isStartRecording);
        } else {
            mISdofDeviceController.configCamera(mRecorder.getSurface(), isStartRecording);
        }
    }

    private void pauseRecording() {
        LogHelper.d(TAG, "[pauseRecording] +");
        try {
            mVideoUi.updateUIState(VideoUIState.STATE_PAUSE_RECORDING);
            mRecorder.getMediaRecorder().pause();
        } catch (IllegalStateException e) {
            mVideoUi.showInfo(VIDEO_RECORDING_NOT_AVAILABLE);
            e.printStackTrace();
        }
        LogHelper.d(TAG, "[pauseRecording] -");
    }

    private void resumeRecording() {
        LogHelper.d(TAG, "[resumeRecording] +");
        try {
            mVideoUi.updateUIState(VideoUIState.STATE_RESUME_RECORDING);
            mRecorder.getMediaRecorder().resume();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "[resumeRecording] -");
    }

    private boolean isSupportPauseResume() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return true;
        }
        return false;
    }

    private ISdofRecorder.RecorderSpec configRecorderSpec(boolean isStartRecording) {
        ISdofRecorder.RecorderSpec recorderSpec = mVideoHelper.configRecorderSpec(
                getProfile(), mCameraId, mCameraApi, mSettingManager);
        mOrientationHint = recorderSpec.orientationHint;
        recorderSpec.infoListener = mOnInfoListener;
        recorderSpec.errorListener = mOnErrorListener;
        recorderSpec.releaseListener = mOnInfoListener;
        recorderSpec = modifyRecorderSpec(recorderSpec, isStartRecording);
        return recorderSpec;
    }

    private IVideoUI.UISpec configUISpec() {
        IVideoUI.UISpec spec = new IVideoUI.UISpec();
        spec.isSupportedPause = isSupportPauseResume();
        spec.recordingTotalSize = 0;
        spec.stopListener = mStopRecordingListener;
        spec.isSupportedVss = isVssSupported();
        spec.vssListener = mVssListener;
        spec.pauseResumeListener = mPauseResumeListener;
        spec = modifyUISpec(spec);
        return spec;
    }

    private boolean isVssSupported() {
        if((mSettingManager.getSettingController().queryValue(KEY_HDR10_PLUS))!=null){
            if(mSettingManager.getSettingController().queryValue(KEY_HDR10_PLUS).equals("on")){
                return false;
            }
        }
        return mISdofDeviceController.isVssSupported(Integer.parseInt(mCameraId));
    }

    private void initStatusMonitor() {
        mVideoStatusResponder = mCameraContext.getStatusMonitor(mCameraId)
                .getStatusResponder(VIDEO_STATUS_KEY);
    }

    private void initVideoVariables() {
        mAppUi = mApp.getAppUi();
        mCameraId = CameraUtil.getLogicalCameraId();
        mStorageService = mCameraContext.getStorageService();
        mSettingManager = mCameraContext.getSettingManagerFactory().getInstance(
                mCameraId, getModeKey(), ModeType.VIDEO, mCameraApi);
    }

    private void doInitMode() {
        initStatusMonitor();
        initCameraDevice(mCameraApi);
        mISdofDeviceController.setSettingConfigCallback(mSettingConfigCallback);
        mVideoHelper = new SdofVideoHelper(mCameraContext, mApp, mISdofDeviceController,
                mVideoHandler);
        mISdofDeviceController.setPreviewCallback(
                mVideoHelper.getPreviewFrameCallback(), getPreviewStartCallback());
        initVideoUi();
        mISdofDeviceController.openCamera(
                mSettingManager, mCameraId, false, mRestrictionProvider);
    }

    private void doInitDeviceManager() {
        mISdofDeviceController.queryCameraDeviceManager();
    }

    private void doResumeMode() {
        initStatusMonitor();
        mISdofDeviceController.openCamera(
                mSettingManager, mCameraId, false, mRestrictionProvider);
        mCameraContext.getStorageService().registerStorageStateListener(mStorageStateListener);
    }

    private void pauseForRecorder() {
        if (getVideoState() == VideoState.STATE_RECORDING
                || getVideoState() == VideoState.STATE_PAUSED
                || getVideoState() == VideoState.STATE_PRE_RECORDING) {
            stopRecording();
        } else if (getVideoState() == VideoState.STATE_REVIEW_UI
                || getVideoState() == VideoState.STATE_PREVIEW) {
            updateVideoState(VideoState.STATE_UNKNOWN);
        }
    }

    private void pauseForDevice(ArrayList<String> needCloseCameraIdList) {
        mWaitStopRecording.block();
        if (needCloseCameraIdList == null || needCloseCameraIdList.size() > 0) {
            mISdofDeviceController.preventChangeSettings();
            mISdofDeviceController.closeCamera(true);
            mCameraContext.getSettingManagerFactory().recycle(mCameraId);
        } else {
            mISdofDeviceController.stopPreview();
        }
    }

    private void doPauseMode(ArrayList<String> needCloseCameraIdList) {
        if (mISdofDeviceController != null) {
            mCameraContext.getStorageService().unRegisterStorageStateListener(
                    mStorageStateListener);
            pauseForRecorder();
            pauseForDevice(needCloseCameraIdList);
            mSurface = null;
        }
    }

    private View.OnClickListener mPauseResumeListener = new View.OnClickListener() {
        public void onClick(View view) {
            LogHelper.d(TAG, "[mPauseResumeListener] click video mVideoState = " + mVideoState
                    + " mCanPauseResumeRecording = " + mCanPauseResumeRecording);
            if (mCanPauseResumeRecording) {
                if (getVideoState() == VideoState.STATE_RECORDING) {
                    pauseRecording();
                    updateVideoState(VideoState.STATE_PAUSED);
                } else if (getVideoState() == VideoState.STATE_PAUSED) {
                    resumeRecording();
                    updateVideoState(VideoState.STATE_RECORDING);
                }
            }
        }
    };

    private View.OnClickListener mVssListener = new View.OnClickListener() {
        public void onClick(View view) {
            LogHelper.i(TAG, "[mVssListener] click video state = "
                    + mVideoState + "mCanTakePicture = " + mCanTakePicture);
            if ((getVideoState() == VideoState.STATE_PAUSED ||
                    getVideoState() == VideoState.STATE_RECORDING) && mCanTakePicture) {
                mAppUi.animationStart(IAppUi.AnimationType.TYPE_CAPTURE, null);
                mISdofDeviceController.updateGSensorOrientation(mApp.getGSensorOrientation());
                mISdofDeviceController.takePicture(mJpegCallback);
                mCanTakePicture = false;
            }
        }
    };

    private ISdofVideoDeviceController.JpegCallback mJpegCallback =
            new ISdofVideoDeviceController.JpegCallback() {
                @Override
                public void onDataReceived(byte[] jpegData) {
                    LogHelper.d(TAG, "[onDataReceived] jpegData = " + jpegData);
                    if (jpegData != null) {
                        int orientation = CameraUtil.getOrientationFromExif(jpegData);
                        Size size = CameraUtil.getSizeFromExif(jpegData);
                        mCameraContext.getMediaSaver().addSaveRequest(jpegData,
                                mVideoHelper.prepareContentValues(false, orientation, size),
                                null, mVssSavedListener);
                    }
                    mCanTakePicture = true;
                }
            };

    private View.OnClickListener mStopRecordingListener = new View.OnClickListener() {
        public void onClick(View view) {
            LogHelper.i(TAG, "[mStopRecordingListener] click video state = " + mVideoState);
            mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
        }
    };

    private ISdofVideoDeviceController.DeviceCallback mPreviewStartCallback = new
            ISdofVideoDeviceController.DeviceCallback() {
                @Override
                public void onCameraOpened(String cameraId) {
                    updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
                }

                @Override
                public void afterStopPreview() {
                    updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
                }

                @Override
                public void beforeCloseCamera() {
                    updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
                }

                @Override
                public void onPreviewStart() {
                    if (getModeState() == ModeState.STATE_PAUSED) {
                        LogHelper.e(TAG, "[onPreviewStart] error mode state is paused");
                        return;
                    }
                    updateVideoState(VideoState.STATE_PREVIEW);
                    mAppUi.applyAllUIEnabled(true);
                    updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
                    LogHelper.d(TAG, "[onPreviewStart]");
                }

                @Override
                public void onError() {
                    if (getVideoState() == VideoState.STATE_PAUSED
                            || getVideoState() == VideoState.STATE_RECORDING) {
                        mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
                    }
                }
            };

    private MediaRecorder.OnErrorListener mOnErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            LogHelper.e(TAG, "[onError] what = " + what + ". extra = " + extra);
            if (MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN == what) {
                mVideoHandler.sendEmptyMessage(HANDLER_STOP_RECORDING);
            }
        }
    };

    private MediaRecorder.OnInfoListener mOnInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            LogHelper.d(TAG, "MediaRecorder =" + mr + "what = " + what + " extra = " + extra);
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    stopRecording();
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                    stopRecording();

                    break;
                default:
                    break;
            }
        }
    };

    private boolean canSelectCamera(@Nonnull String newCameraId) {
        if (newCameraId == null || mCameraId.equalsIgnoreCase(newCameraId)
                || getVideoState() != VideoState.STATE_PREVIEW) {
            return false;
        }
        return true;
    }

    private void doCameraSelect(String newCameraId) {
        LogHelper.i(TAG, "[doCameraSelect] + mVideoState = " + mVideoState);
        if (getVideoState() == VideoState.STATE_PREVIEW
                && getModeState() == ModeState.STATE_RESUMED) {
            mISdofDeviceController.preventChangeSettings();
            updateVideoState(VideoState.STATE_UNKNOWN);
            mISdofDeviceController.closeCamera(true);
            mCameraContext.getSettingManagerFactory().recycle(mCameraId);
            mCameraId = newCameraId;
            mSettingManager = mCameraContext.getSettingManagerFactory().getInstance(
                    mCameraId, getModeKey(), ModeType.VIDEO, mCameraApi);
            initStatusMonitor();
            mVideoHelper.releasePreviewFrameData();
            mISdofDeviceController.setPreviewCallback(
                    mVideoHelper.getPreviewFrameCallback(), getPreviewStartCallback());
            mISdofDeviceController.openCamera(mSettingManager, mCameraId, false,
                    mRestrictionProvider);
        }
    }

    private ISdofVideoDeviceController.StereoWarningCallback mWarningCallback
            = new ISdofVideoDeviceController.StereoWarningCallback() {
        @Override
        public void onWarning(int type) {
            LogHelper.i(TAG, "[StereoWarningCallback onWarning] " + type);
            switch (type) {
                case SdofViewCtrl.DUAL_CAMERA_LOW_LIGHT:
                    break;
                case SdofViewCtrl.DUAL_CAMERA_READY:
                    break;
                case SdofViewCtrl.DUAL_CAMERA_TOO_CLOSE:
                    break;
                case SdofViewCtrl.DUAL_CAMERA_LENS_COVERED:
                    break;
                default:
                    LogHelper.w(TAG, "Warning message don't need to show");
                    break;
            }
            mSdofViewCtrl.showWarningView(type);
        }
    };
    private SdofViewCtrl.ViewChangeListener mViewChangeListener
            = new SdofViewCtrl.ViewChangeListener() {
        @Override
        public void onVsDofLevelChanged(int level) {
            if(mISdofDeviceController != null){
                mISdofDeviceController.setVsDofLevelParameter(level);
            }
        }

        @Override
        public void onTouchPositionChanged(int value) {
        }
    };

    @Override
    public DeviceUsage getDeviceUsage(@Nonnull DataStore dataStore, DeviceUsage oldDeviceUsage) {
        ArrayList<String> openedCameraIds = new ArrayList<>();
        String cameraId = getCameraIdByFacing(dataStore.getValue(
                KEY_CAMERA_SWITCHER, null, dataStore.getGlobalScope()));
        openedCameraIds.add(cameraId);
        updateModeDefinedCameraApi();
        return new DeviceUsage(DeviceUsage.DEVICE_TYPE_STEREO_VSDOF, mCameraApi, openedCameraIds);
    }
}
