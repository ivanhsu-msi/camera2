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

package com.mediatek.camera.feature.setting.objecttracking;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;

import java.util.Arrays;
import java.util.List;

/**
 * This is for object detection flow.
 */
@SuppressWarnings("deprecation")
public class ObjectDeviceCtrl {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ObjectDeviceCtrl.class.getSimpleName());
    private boolean mIsPreviewStarted;
    private String mObjectOverrideState = IObjectConfig.OBJECT_TRACKING_ON;
    private boolean mIsObjectTrackingSupported;
    private IObjectPerformerMonitor mObjectMonitor = new ObjectPerformerMonitor();
    private ObjectCaptureRequestConfig mCaptureRequestConfig;
    private IObjectConfig mObjectConfig;
    private IObjectConfig.OnObjectValueUpdateListener mObjectValueUpdateListener;

    /**
     * init object device controller, used to set object config listener.
     */
    public void init() {
    }

    /**
     * Interface for object device to query controller state.
     */
    public interface IObjectPerformerMonitor {

        /**
         * set whether the feature is supported.
         *
         * @param isSupport true is supported.
         */
        public void setSupportedStatus(boolean isSupport);

        /**
         * This is to notify object detection value.
         *
         * @return true if preview started and object detection state is stop
         * and object detection override value by restriction is on, or false.
         */
        public boolean isNeedToStart();

        /**
         * This is to query object detection state.
         *
         * @return true It should do stop flow.
         */
        public boolean isNeedToStop();
    }

    /**
     * This is to notify preview status.
     *
     * @param isPreviewStarted true if preview started,
     *                         or false if preview stopped.
     */
    public void onPreviewStatus(boolean isPreviewStarted) {
        LogHelper.d(TAG, "[onPreviewStatus] isPreviewStarted = " + isPreviewStarted);
        mIsPreviewStarted = isPreviewStarted;
        if (!isPreviewStarted && mObjectConfig != null) {
            //preview stop, FD had stopped.
            mObjectConfig.resetObjectTrackingState();
        }
    }

    /**
     * update image orientation to object algo.
     */
    public void updateImageOrientation() {
        if (mObjectConfig != null) {
            //no need update orientation when preview not started.
            mObjectConfig.updateImageOrientation();
        }
    }

    /**
     * This is to api2 flow.
     *
     * @param settingDevice2Requester the device 2 requester.
     * @return the Api2 interobject.
     */
    public ICameraSetting.ICaptureRequestConfigure getCaptureRequestConfigure(
            ISettingManager.SettingDevice2Requester settingDevice2Requester) {
        if (mCaptureRequestConfig == null) {
            mCaptureRequestConfig = new ObjectCaptureRequestConfig(settingDevice2Requester);
            mCaptureRequestConfig.setObjectMonitor(mObjectMonitor);
            mCaptureRequestConfig.setObjectValueUpdateListener(mObjectValueUpdateListener);
            mObjectConfig = mCaptureRequestConfig;
        }
        mIsPreviewStarted = true;
        return mCaptureRequestConfig;
    }

    /**
     * This is to notify object detection value.
     *
     * @param overrideValue object detection value by other restriction.
     */
    public void updateObjectTrackingStatus(String overrideValue) {
        mObjectOverrideState = overrideValue;
    }

    /**
     * This is to check object detection status changed or not.
     *
     * @param curValue object detection current value.
     * @return true, if object detection status changed.
     */
    public boolean isObjectTrackingStatusChanged(String curValue) {
        return !mObjectOverrideState.equals(curValue);
    }

    /**
     * This is to set object detection update listener.
     *
     * @param trackedObjectUpdateListener the detected object listener.
     */
    public void setTrackedObjectUpdateListener(IObjectConfig.OnTrackedObjectUpdateListener
                                                       trackedObjectUpdateListener) {
        if (mObjectConfig != null) {
            mObjectConfig.setObjectTrackingUpdateListener(trackedObjectUpdateListener);
        }
    }

    /**
     * This is to set object detection update listener.
     *
     * @param onObjectSettingValueUpdateListener the detected object listener.
     */
    public void setObjectValueUpdateListener(IObjectConfig.OnObjectValueUpdateListener
                                                     onObjectSettingValueUpdateListener) {
        mObjectValueUpdateListener = onObjectSettingValueUpdateListener;
    }

    /**
     * Used for object device to query controller state.
     */
    private class ObjectPerformerMonitor implements IObjectPerformerMonitor {

        public void setSupportedStatus(boolean isSupport) {
            mIsObjectTrackingSupported = isSupport;
        }

        public boolean isNeedToStart() {
            boolean overrideState = mObjectOverrideState.equals(IObjectConfig.OBJECT_TRACKING_ON);
            boolean needStart = overrideState && mIsPreviewStarted
                    && mIsObjectTrackingSupported;
            LogHelper.d(TAG, "[isNeedStart]  overrideState = " + overrideState
                    + ", mIsPreviewStarted = " + mIsPreviewStarted
                    + ", mIsObjectTrackingSupported = " + mIsObjectTrackingSupported
                    + ", needStart = " + needStart);
            return needStart;
        }

        public boolean isNeedToStop() {
            boolean overrideState = mObjectOverrideState.equals(IObjectConfig.OBJECT_TRACKING_OFF);
            boolean needStop = overrideState && mIsPreviewStarted
                    && mIsObjectTrackingSupported;
            LogHelper.d(TAG, "[isNeedStop]  overrideState = " + overrideState
                    + ", mIsPreviewStarted = " + mIsPreviewStarted
                    + ", mIsObjectTrackingSupported = " + mIsObjectTrackingSupported
                    + ", needStop = " + needStop);
            return needStop;
        }
    }

    /**
     * Notify to update object area.
     *
     * @param objectArea The object area.
     */
    public void updateObjectArea(List<Camera.Area> objectArea) {
        if (mCaptureRequestConfig != null) {
            mCaptureRequestConfig.updateObjectArea(objectArea);
        }else {
            LogHelper.i(TAG, "[updateObjectArea] mCaptureRequestConfig = null" );
        }
    }

    public Rect getCropRegion() {
        return mCaptureRequestConfig.getCropRegion();
    }

    public CameraCharacteristics getCameraCharacteristics() {
        return mCaptureRequestConfig.getCameraCharacteristics();
    }

    public void sendObjectTrackingTriggerCaptureRequest() {
        if (mCaptureRequestConfig != null) {
            mCaptureRequestConfig.sendObjectTrackingTriggerCaptureRequest();
        }else {
            LogHelper.i(TAG, "[sendObjectTrackingTriggerCaptureRequest] mCaptureRequestConfig = null" );
        }
    }
    public void sendObjectTrackingCancelCaptureRequest(Boolean isHighSpeedRequest) {
        LogHelper.i(TAG, "[sendObjectTrackingCancelCaptureRequest]  isHighSpeedRequest = " + isHighSpeedRequest);
        if (mCaptureRequestConfig != null) {
            mCaptureRequestConfig.sendObjectTrackingCancelCaptureRequest(isHighSpeedRequest);
        }else {
            LogHelper.i(TAG, "[sendObjectTrackingCancelCaptureRequest] mCaptureRequestConfig = null" );
        }
    }
}
