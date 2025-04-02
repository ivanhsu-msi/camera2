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

package com.mediatek.camera.feature.setting.objecttracking;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.v2.Camera2CaptureSessionProxy;
import com.mediatek.camera.common.device.v2.Camera2Proxy;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.CoordinatesTransform;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * It is the face implement for API2.
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ObjectCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure,
        IObjectConfig {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            ObjectCaptureRequestConfig.class.getSimpleName());
    /**
     * Metering region weight between 0 and 1.
     *
     * <p>
     * This value has been tested on Nexus 5 and Shamu, but will need to be
     * tuned per device depending on how its ISP interprets the metering box and weight.
     * </p>
     */
    private static final float REGION_WEIGHT = 0.022f;
    /**
     * camera2 API metering region weight.
     */
    private static final int CAMERA2_REGION_WEIGHT = (int)
            (lerp(MeteringRectangle.METERING_WEIGHT_MIN, MeteringRectangle.METERING_WEIGHT_MAX,
                    REGION_WEIGHT));
    private ObjectDeviceCtrl.IObjectPerformerMonitor mObjectMonitor;
    private OnTrackedObjectUpdateListener mOnTrackedObjectUpdateListener;
    private OnObjectValueUpdateListener mOnObjectValueUpdateListener;
    private boolean mIsRequestConfigSupported;
    private boolean mIsVendorObject3ASupported;
    private List<String> mSupportValueList = new ArrayList<String>();
    private static final int[] OBJECT_FORCE_OBJECT_3A_OFF = new int[]{0};
    private static final int[] OBJECT_FORCE_OBJECT_3A_ON = new int[]{1};
    private static final String OBJECT_DETECTION_FORCE_OBJECT_3A =
            "com.mediatek.facefeature.forceface3a";
    private static final int[] OBJECT_ON_KEY_VALUE = new int[]{1};
    private static final int[] OBJECT_OFF_KEY_VALUE = new int[]{0};
    private static final int[] OBJECT_CANCEL_KEY_VALUE = new int[]{1};
    private static final String TRACKING_AF_ON
            = "com.mediatek.trackingaffeature.trackingafMode";
    private static final String TRACKING_AF_TARGET
            = "com.mediatek.trackingaffeature.trackingafTarget";
    private static final String TRACKING_AF_REGION
            = "com.mediatek.trackingaffeature.trackingafRegion";
    private static final String TRACKING_AF_CANCEL
            = "com.mediatek.trackingaffeature.trackingafCancel";
    private static final String TRACKING_AF_NO_OBJECT
            = "com.mediatek.trackingaffeature.trackingafNoObject";
    private CaptureRequest.Key<int[]> mObjectForce3aModesRequestKey;
    private CaptureResult.Key<int[]> mKeyTrackingAfTarget;
    private CaptureRequest.Key<int[]> mKeyTrackingAfOn;
    private CaptureRequest.Key<int[]> mKeyTrackingAfRegion;
    private CaptureRequest.Key<int[]> mKeyTrackingAfCancel;
    private CaptureResult.Key<int[]> mKeyTrackingAfNoObject;
    private static final MeteringRectangle[] ZERO_WEIGHT_3A_REGION = new MeteringRectangle[]{
            new MeteringRectangle(0, 0, 0, 0, 0)
    };
    private MeteringRectangle[] mOTRegions = ZERO_WEIGHT_3A_REGION;
    private int[] mOTRegion = {0, 0, 0, 0};
    private Rect mCropRegion = new Rect();
    private CameraCharacteristics mCameraCharacteristics;
    private ISettingManager.SettingDevice2Requester mSettingDevice2Requester;
    private Handler mModeHandler = null;
    private Boolean mIsTracking = false;


    /**
     * Constructor of face parameter config in api1.
     *
     * @param settingDevice2Requester device requester.
     */
    public ObjectCaptureRequestConfig(ISettingManager.SettingDevice2Requester
                                              settingDevice2Requester) {
        mSettingDevice2Requester = settingDevice2Requester;
        mModeHandler = new Handler(Looper.myLooper());
    }

    @Override
    public void setObjectMonitor(ObjectDeviceCtrl.IObjectPerformerMonitor monitor) {
        mObjectMonitor = monitor;
    }

    @Override
    public void updateImageOrientation() {

    }
    @Override
    public void configSessionParams(CaptureRequest.Builder captureBuilder) {

    }
    @Override
    public void resetObjectTrackingState() {
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        mIsTracking = false;
        mIsRequestConfigSupported = isObjectTrackingSupported(characteristics);
        mObjectMonitor.setSupportedStatus(mIsRequestConfigSupported);
        mSupportValueList.clear();
        mSupportValueList.add(IObjectConfig.OBJECT_TRACKING_ON);
        mSupportValueList.add(IObjectConfig.OBJECT_TRACKING_OFF);
        mIsVendorObject3ASupported = isObject3ASupported(characteristics);
        mCameraCharacteristics = characteristics;
        for (CaptureRequest.Key<?> requestKey : characteristics.getAvailableCaptureRequestKeys()) {
            if (requestKey.getName().equals(TRACKING_AF_REGION)) {
                LogHelper.i(TAG, "[setCameraCharacteristics] mKeyTrackingAfRegion ");
                mKeyTrackingAfRegion = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(TRACKING_AF_CANCEL)) {
                LogHelper.i(TAG, "[setCameraCharacteristics] mKeyTrackingAfCancel ");
                mKeyTrackingAfCancel = (CaptureRequest.Key<int[]>) requestKey;
            }
        }
        for (CaptureResult.Key<?> resultKey : characteristics.getAvailableCaptureResultKeys()) {
            if (resultKey.getName().equals(TRACKING_AF_TARGET)) {
                LogHelper.i(TAG, "[setCameraCharacteristics] mKeyTrackingAfTarget ");
                mKeyTrackingAfTarget = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(TRACKING_AF_NO_OBJECT)) {
                LogHelper.i(TAG, "[setCameraCharacteristics] mKeyTrackingAfNoObject ");
                mKeyTrackingAfNoObject = (CaptureResult.Key<int[]>) resultKey;
            }
        }
        for (CaptureRequest.Key<?> sessionKey : characteristics.getAvailableSessionKeys()) {
            if (sessionKey.getName().equals(TRACKING_AF_ON)) {
                LogHelper.i(TAG, "[setCameraCharacteristics] mKeyTrackingAfOn ");
                mKeyTrackingAfOn = (CaptureRequest.Key<int[]>) sessionKey;
            }
        }
        mOnObjectValueUpdateListener.onObjectSettingValueUpdate(mIsRequestConfigSupported,
                mSupportValueList);
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        if (captureBuilder == null) {
            LogHelper.d(TAG, "[configCaptureRequest] captureBuilder is null");
            return;
        }
        /*if (CameraUtil.isStillCaptureTemplate(captureBuilder)) {
            LogHelper.i(TAG, "[configCaptureRequest] capture request not has face dection.");
            return;
        }*/
        /*if (mObjectMonitor.isNeedToStart()) {
            LogHelper.i(TAG, "[configCaptureRequest] start face detection, this: " + this);
            captureBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);
            if (mIsVendorObject3ASupported) {
                captureBuilder.set(mObjectForce3aModesRequestKey, OBJECT_FORCE_OBJECT_3A_ON);
            }
        }*/

        String mObjectTrackingValue = mOnObjectValueUpdateListener.onGetValue();
        LogHelper.d(TAG, "configCaptureRequest mObjectTrackingValue to " + mObjectTrackingValue);
        if (OBJECT_TRACKING_ON.equals(mObjectTrackingValue)) {
            captureBuilder.set(mKeyTrackingAfOn, OBJECT_ON_KEY_VALUE);
        } else {
            //captureBuilder.set(mKeyTrackingAfOn, OBJECT_OFF_KEY_VALUE);
        }

        if (CameraUtil.isStillCaptureTemplate(captureBuilder)) {
            LogHelper.i(TAG, "[configCaptureRequest] capture request");

        }
        /*if (mObjectMonitor.isNeedToStop()) {
            LogHelper.i(TAG, "[configCaptureRequest] stop face detection");
            captureBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                    CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);
            if (mIsVendorObject3ASupported) {
                captureBuilder.set(mObjectForce3aModesRequestKey, OBJECT_FORCE_OBJECT_3A_OFF);
            }
        }*/
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
        return mPreviewCallback;
    }

    @Override
    public void sendSettingChangeRequest() {
        mSettingDevice2Requester.createAndChangeRepeatingRequest();
    }

    @Override
    public void setObjectTrackingUpdateListener(OnTrackedObjectUpdateListener listener) {
        mOnTrackedObjectUpdateListener = listener;
    }

    @Override
    public void setObjectValueUpdateListener(OnObjectValueUpdateListener listener) {
        mOnObjectValueUpdateListener = listener;
    }

    public void updateObjectArea(List<Camera.Area> objectArea) {
        if (objectArea != null) {
            mOTRegion[0] = objectArea.get(0).rect.left;
            mOTRegion[1] = objectArea.get(0).rect.top;
            mOTRegion[2] = objectArea.get(0).rect.right;
            mOTRegion[3] = objectArea.get(0).rect.bottom;
            mOTRegions = new MeteringRectangle[]{new MeteringRectangle(objectArea.get(0).rect,
                    CAMERA2_REGION_WEIGHT)};
        }
    }

    public static boolean isObjectTrackingSupported(CameraCharacteristics cameraCharacteristics) {
        int faceNum = 0;
        try {
            faceNum = cameraCharacteristics
                    .get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
            LogHelper.d(TAG, "[isObjectTrackingSupported] faceNum = " + faceNum);
        } catch (IllegalArgumentException e) {
            LogHelper.e(TAG, "[isObjectTrackingSupported] IllegalArgumentException");
        }

        return faceNum > 0;
    }

    private boolean isObject3ASupported(CameraCharacteristics cameraCharacteristics) {
        List<CaptureRequest.Key<?>> availableKeys =
                cameraCharacteristics.getAvailableCaptureRequestKeys();
        for (CaptureRequest.Key<?> key : availableKeys) {
            if (OBJECT_DETECTION_FORCE_OBJECT_3A.equals(key.getName())) {
                mObjectForce3aModesRequestKey = (CaptureRequest.Key<int[]>) key;
                return true;
            }
        }
        return false;
    }

    private CameraCaptureSession.CaptureCallback mPreviewCallback
            = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Assert.assertNotNull(result);
            if (mKeyTrackingAfTarget != null) {
                int[] objects
                        = result.get(mKeyTrackingAfTarget);
                mCropRegion = result.get(CaptureResult.SCALER_CROP_REGION);
                Rect[] previewRect = getPreviewRect(objects, mCropRegion);
                if (mOnTrackedObjectUpdateListener != null && objects!= null) {
                    mIsTracking = true;
                    mOnTrackedObjectUpdateListener.onTrackedObjectUpdate(getObject(objects,
                            previewRect, mCropRegion));
                }
                if(mIsTracking && objects ==null && mOnTrackedObjectUpdateListener != null){
                    mIsTracking = false;
                    mOnTrackedObjectUpdateListener.onTrackedObjectUpdate(null);
                }
            }
            if (mKeyTrackingAfNoObject != null) {
                int[] warning = result.get(mKeyTrackingAfNoObject);
                if (warning != null && warning[0] == 1) {
                    mOnTrackedObjectUpdateListener.onTrackNoObject();
                }
            }

        }
    };

    private Object getObject(int[] objects,
                             Rect[] previewRect, Rect cropRegion) {
        if (objects == null || (objects != null && objects.length == 0) || (cropRegion == null)) {
            LogHelper.d(TAG, "[getobjects] return null");
            return null;
        }
        Object objectStructures = new Object();
        Object objectTemp = null;
        //for (int i = 0; i < objects.length; i++) {
        objectTemp = new Object();
        //objectTemp.id = objects[i].getId();
        //objectTemp.score = objects[i].getScore();
        objectTemp.cropRegion = cropRegion;
        objectTemp.rect = previewRect[0];
        objectStructures = objectTemp;
        //}
        return objectStructures;
    }

    private Rect[] getPreviewRect(int[] objects, Rect cropRegion) {
        if (objects == null || objects.length == 0 || cropRegion == null) {
            return null;
        }
        Rect tempObject = new Rect(objects[0], objects[1], objects[2], objects[3]);
        Rect[] previewRect = new Rect[1];
        Rect rectTemp = null;
        for (int i = 0; i < objects.length; i++) {
            rectTemp = CoordinatesTransform.sensorToNormalizedPreview(tempObject,
                    mOnObjectValueUpdateListener.onObjectPreviewSizeUpdate().getWidth(),
                    mOnObjectValueUpdateListener.onObjectPreviewSizeUpdate().getHeight(),
                    cropRegion);
            previewRect[0] = rectTemp;
        }
        return previewRect;
    }

    /**
     * Linear interpolation between a and b by the fraction t. t = 0 --> a, t =
     * 1 --> b.
     */
    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public Rect getCropRegion() {
        return mCropRegion;
    }

    public CameraCharacteristics getCameraCharacteristics() {
        return mCameraCharacteristics;
    }

    public void sendObjectTrackingTriggerCaptureRequest() {
        LogHelper.d(TAG,
                "[sendObjectTrackingTriggerCaptureRequest] mOTRegion : " + Arrays.toString(mOTRegion));
        CaptureRequest.Builder builder = mSettingDevice2Requester.createAndConfigRequest(
                Camera2Proxy.TEMPLATE_PREVIEW);
        if (builder == null) {
            LogHelper.w(TAG, "[sendObjectTrackingTriggerCaptureRequest] builder is null");
            return;
        }
        // Step 2: Call repeatingPreview to update mControlAFMode.
        Camera2CaptureSessionProxy sessionProxy =
                mSettingDevice2Requester.getCurrentCaptureSession();
        if (sessionProxy == null) {
            LogHelper.w(TAG, "[sendObjectTrackingTriggerCaptureRequest] sessionProxy is null");
            return;
        }
        builder.set(mKeyTrackingAfRegion, mOTRegion);
        LogHelper.i(TAG, "[sendObjectTrackingTriggerCaptureRequest] is common mode");
        try {
            sessionProxy.capture(builder.build(), mPreviewCallback, mModeHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void sendObjectTrackingCancelCaptureRequest(Boolean isHighSpeedRequest) {
        if(!mIsTracking){
            return;
        }
        CaptureRequest.Builder builder = mSettingDevice2Requester.createAndConfigRequest(
                Camera2Proxy.TEMPLATE_PREVIEW);
        if (builder == null) {
            LogHelper.w(TAG, "[sendObjectTrackingCancelCaptureRequest] builder is null");
            return;
        }
        Camera2CaptureSessionProxy sessionProxy =
                mSettingDevice2Requester.getCurrentCaptureSession();
        if (sessionProxy == null) {
            LogHelper.w(TAG, "[sendObjectTrackingCancelCaptureRequest] sessionProxy is null");
            return;
        }
        builder.set(mKeyTrackingAfCancel, OBJECT_CANCEL_KEY_VALUE);

        try {
            if(isHighSpeedRequest) {
                LogHelper.i(TAG, "[sendObjectTrackingCancelCaptureRequest] isHighSpeedRequest");
                List<CaptureRequest> captureRequests = null;
                captureRequests = sessionProxy.createHighSpeedRequestList(builder.build());
                sessionProxy.captureBurst(captureRequests, mPreviewCallback, mModeHandler);
            }else {
                LogHelper.i(TAG, "[sendObjectTrackingCancelCaptureRequest]  is common mode");
                sessionProxy.capture(builder.build(), mPreviewCallback, mModeHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
}
