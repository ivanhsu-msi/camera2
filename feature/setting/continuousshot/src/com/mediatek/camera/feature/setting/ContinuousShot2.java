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
 *   MediaTek Inc. (C) 2016. All rights reserved.
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

package com.mediatek.camera.feature.setting;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.view.Surface;

import com.mediatek.camera.common.IAppUiListener.OnShutterButtonListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.bgservice.CaptureSurface;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.v2.Camera2CaptureSessionProxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.setting.ICameraSetting.ICaptureRequestConfigure;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.feature.setting.CsState.State;
import com.mediatek.camera.feature.setting.format.IFormatViewListener;

import junit.framework.Assert;

import java.util.List;

import javax.annotation.Nonnull;


/**
 * This is used for API2 continuous shot.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ContinuousShot2 extends ContinuousShotBase implements ICaptureRequestConfigure,
        OnShutterButtonListener {

    private static final Tag TAG = new Tag(ContinuousShot2.class.getSimpleName());
    private static final int[] mCaptureMode = new int[]{1};
    private CaptureRequest.Key<int[]> mKeyCsCaptureRequest;
    private CaptureRequest.Key<int[]> mKeyP2NotificationRequest;
    private CaptureResult.Key<int[]> mKeyP2NotificationResult;
    private CsState mState;
    private final Object mNumberLock = new Object();
    private volatile int mP2CallbackNumber = 0;
    private volatile int mImageCallbackNumber = 0;
    private volatile int mCaptureRequestNumber = 0;
    private boolean mIsSpeedUpSupported = false;
    private boolean mIsCshotSupported = false;
    private volatile long mLastUpdatedCaptureNumber = -1;
    private CaptureSurface mCaptureSurface;

    @Override
    public void init(IApp app,
            ICameraContext cameraContext,
            ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mState = new CsState();
        mState.updateState(State.STATE_INIT);
    }

    @Override
    public void unInit() {
        super.unInit();
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return this;
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        super.overrideValues(headerKey, currentValue, supportValues);
        LogHelper.d(TAG, "[overrideValues] getValue() = " + getValue() + ", headerKey = "
                + headerKey + ", currentValue = " + currentValue + ", supportValues  = "
                + supportValues);
        mIsCshotSupported = CONTINUOUSSHOT_ON.equals(getValue());
    }

    @Override
    public void onModeClosed(String modeKey) {
        mState.updateState(State.STATE_INIT);
        super.onModeClosed(modeKey);
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        //front camera, not support
        if (characteristics.get(CameraCharacteristics.LENS_FACING)
                == CameraCharacteristics.LENS_FACING_FRONT) {
            mIsCshotSupported = false;
            return;
        }

        DeviceDescription deviceDescription = CameraApiHelper.
                getDeviceSpec(mActivity.getApplicationContext()).getDeviceDescriptionMap()
                .get(String.valueOf(Integer.parseInt(mSettingController.getCameraId())));
        if (deviceDescription != null) {
            mIsCshotSupported = deviceDescription.isCshotSupport()
                    && ICameraMode.ModeType.PHOTO == getModeType();
            mIsSpeedUpSupported = deviceDescription.isSpeedUpSupport()
                    && ICameraMode.ModeType.PHOTO == getModeType();
        }

        initializeValue(mIsCshotSupported);
        if (deviceDescription != null) {
            mKeyCsCaptureRequest = deviceDescription.getKeyCshotRequestMode();
            mKeyP2NotificationRequest = deviceDescription.getKeyP2NotificationRequestMode();
            mKeyP2NotificationResult = deviceDescription.getKeyP2NotificationResult();
        }

    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
    }

    @Override
    public void configSessionParams(CaptureRequest.Builder captureBuilder) {

    }

    @Override
    public void configSessionSurface(List<Surface> surfaces) {

    }

    @Override
    public Surface configRawSurface() {
        return null;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return null;
    }

    @Override
    public void sendSettingChangeRequest() {
    }

    protected boolean startContinuousShot() {
        if (mState.getCShotState() == State.STATE_INIT) {
            if (mHandler == null) {
                return false;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(mState.getCShotState() == State.STATE_INIT){
                            LogHelper.i(TAG, "[startContinuousShot]");
                            synchronized (mNumberLock) {
                                mP2CallbackNumber = 0;
                                mImageCallbackNumber = 0;
                                mCaptureRequestNumber = 0;
                                mLastUpdatedCaptureNumber = -1;
                            }
                            mState.updateState(State.STATE_CAPTURE_STARTED);
                            onContinuousShotStarted();
                            createCaptureRequest(true);
                            playSound();
                        }
                    } catch (CameraAccessException e) {
                        mState.updateState(State.STATE_ERROR);
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        mState.updateState(State.STATE_ERROR);
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        return false;
    }

    @Override
    protected boolean isContinuousShotSupported() {
        return mIsCshotSupported;
    }

    protected boolean stopContinuousShot() {
        super.stopContinuousShot();
        if (mState.getCShotState() == State.STATE_ERROR) {
            onContinuousShotStopped();
            onContinuousShotDone(0);
            mState.updateState(State.STATE_INIT);
        } else if (mState.getCShotState() == State.STATE_CAPTURE_STARTED) {
            if (mHandler == null) {
                return false;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mState.updateState(State.STATE_STOPPED);
                    LogHelper.i(TAG, "[stopContinuousShot]");
                    Camera2CaptureSessionProxy session =
                            mSettingDevice2Requester.getCurrentCaptureSession();
                    onContinuousShotStopped();
                    onContinuousShotDone(mImageCallbackNumber);
                    stopSound();
                    mState.updateState(State.STATE_INIT);
                    if (mCaptureSurface != null){
                        mCaptureSurface.setCaptureCallback(null,0);
                    }
                }
            });

            return true;
        }
        stopSound();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //reset callback priority so that other mode can override callback to CaptureSurface
                if (mCaptureSurface != null){
                    mCaptureSurface.setCaptureCallback(null,0);
                }
            }
        });

        return false;
    }

    protected void requestChangeOverrideValues() {
        mSettingDevice2Requester.createAndChangeRepeatingRequest();
    }

    private void createCaptureRequest(boolean isFirstRequest)
            throws CameraAccessException, IllegalStateException {
        LogHelper.d(TAG, "[createCaptureRequest] number: " + mCaptureRequestNumber
                        + " current sZsdValue =" + sZsdValue);
        if (mCaptureRequestNumber >= MAX_CAPTURE_NUMBER) {
            return;
        }
        int n = 1;
        if (isFirstRequest) {
            n = 3;
        }
        CaptureRequest.Builder captureBuilder = mSettingDevice2Requester
                .createAndConfigRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
        captureBuilder.set(mKeyCsCaptureRequest, mCaptureMode);
        if (mIsSpeedUpSupported) {
            //if support p2done, trigger p2 done to speed up capture
            captureBuilder.set(mKeyP2NotificationRequest, mCaptureMode);
        }
        captureBuilder.set(CaptureRequest.JPEG_QUALITY, JPEG_QUALITY_VALUE);
        mCaptureSurface = mSettingDevice2Requester
                .getModeSharedCaptureSurface();
        Surface captureSurface = mCaptureSurface.getSurface();
        Assert.assertNotNull(captureSurface);
        captureBuilder.addTarget(captureSurface);
        if (VALUE_ZSD_OFF.equalsIgnoreCase(sZsdValue)) {
            n = 1;
            Surface previewSurface = mSettingDevice2Requester.getModeSharedPreviewSurface();
            Assert.assertNotNull(previewSurface);
            captureBuilder.addTarget(previewSurface);
            LogHelper.d(TAG, "[createCaptureRequest] zsd is off, so add previewSurface to request");
        }
        mCaptureSurface.setCaptureCallback(mImageCallback,1);
        String  settingFormat=mSettingController.queryValue(IFormatViewListener.KEY_FORMAT);
        mCaptureSurface.updatePictureInfo(HeifHelper.getCaptureFormat(settingFormat));
        Surface thumbnailSurface = mSettingDevice2Requester.getModeSharedThumbnailSurface();
        captureBuilder.removeTarget(thumbnailSurface);
        prepareCaptureInfo(captureBuilder);
        Camera2CaptureSessionProxy session = mSettingDevice2Requester
                .getCurrentCaptureSession();
        if (session == null) {
            return;
        }
        for (int i = 0; i < n; i++) {
            session.capture(captureBuilder.build(), mCaptureCallback, mHandler);
            mCaptureRequestNumber++;
        }
    }

    private void prepareCaptureInfo(CaptureRequest.Builder captureBuilder) {
        LogHelper.d(TAG, "[prepareCaptureInfo] current builder : " + captureBuilder);
        //don't care preview surface, because the preview is added in device controller.
        //set the jpeg orientation
        int mJpegRotation = mApp.getGSensorOrientation();
        //TODO how to get the camera id from characteristics.
        //Current CS just support back camera.
        int mCurrentCameraId = 0;
        int rotation = CameraUtil.getJpegRotationFromDeviceSpec(mCurrentCameraId,
                mJpegRotation, mActivity);
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
        if (mCameraContext.getLocation() != null) {
            captureBuilder.set(CaptureRequest.JPEG_GPS_LOCATION, mCameraContext.getLocation());
        }
    }

    private CaptureSurface.ImageCallback mImageCallback = new CaptureSurface.ImageCallback() {

        @Override
        public void onPictureCallback(
                byte[] data, int format, String formatTag, int width, int height) {
            synchronized (mNumberLock) {
                if (data != null) {
                    mImageCallbackNumber ++;
                    LogHelper.d(TAG, "[mImageCallback] Number = " + mImageCallbackNumber);
                    saveJpeg(data,format);
                    if (mImageCallbackNumber >= MAX_CAPTURE_NUMBER && mCaptureSurface != null) {
                        mCaptureSurface.discardFreeBuffers();
                    }
                }
            }
        }
    };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long
                timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            LogHelper.d(TAG, "[onCaptureStarted] mState: " + mState.getCShotState()
                    + "frameNumber: " + frameNumber + ", request = " + request);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            LogHelper.d(TAG, "[onCaptureProgressed] mState = " + mState.getCShotState()
                    + ", frameNumber: " + partialResult.getFrameNumber()
                        + ", request = " + request);
            if (mIsSpeedUpSupported
                    && CameraUtil.isStillCaptureTemplate(request)
                    && mState.getCShotState() == State.STATE_CAPTURE_STARTED) {
                int[] value = partialResult.get(mKeyP2NotificationResult);
                if (value != null && value[0] == mCaptureMode[0]) {
                    try {
                        mP2CallbackNumber ++;
                        LogHelper.d(TAG, "[onCaptureProgressed] p2 done callback: "
                                + mP2CallbackNumber + "frameNumber: "
                                + partialResult.getFrameNumber());
                        if (partialResult.getFrameNumber() > mLastUpdatedCaptureNumber) {
                            mLastUpdatedCaptureNumber = partialResult.getFrameNumber();
                            LogHelper.v(TAG, "[onCaptureProgressed] mLastUpdatedCaptureNumber "
                                + mLastUpdatedCaptureNumber);
                            createCaptureRequest(false);
                        }
                    } catch (CameraAccessException e) {
                        mState.updateState(State.STATE_ERROR);
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        mState.updateState(State.STATE_ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            LogHelper.d(TAG, "[onCaptureCompleted] framenumber: " + result.getFrameNumber()
                        + ", request = " + request);
            if (CameraUtil.isStillCaptureTemplate(request)
                && mState.getCShotState() == State.STATE_CAPTURE_STARTED){
                if (result.getFrameNumber() > mLastUpdatedCaptureNumber){
                    try {
                        mLastUpdatedCaptureNumber = result.getFrameNumber();
                        LogHelper.v(TAG, "[onCaptureCompleted] update mLastUpdatedCaptureNumber "
                                + mLastUpdatedCaptureNumber);
                        createCaptureRequest(false);
                    } catch (CameraAccessException e) {
                        mState.updateState(State.STATE_ERROR);
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        mState.updateState(State.STATE_ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            if (CameraUtil.isStillCaptureTemplate(request)
                    && mState.getCShotState() == State.STATE_CAPTURE_STARTED) {
                LogHelper.e(TAG, "[onCaptureFailed] fail: " + failure.getReason()
                    + "frameNumber: " + failure.getFrameNumber() + ", request = " + request);
                stopContinuousShot();
            }
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long
                frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            LogHelper.d(TAG, "[onCaptureSequenceCompleted]");

        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            LogHelper.d(TAG, "[onCaptureSequenceAborted]");

        }

        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request,
                Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            LogHelper.d(TAG, "[onCaptureBufferLost]");

        }
    };

}


