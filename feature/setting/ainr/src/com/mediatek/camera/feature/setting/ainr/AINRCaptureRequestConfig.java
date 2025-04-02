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

package com.mediatek.camera.feature.setting.ainr;

import android.annotation.TargetApi;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.os.Build;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * This is for AINR capture flow in camera API2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AINRCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG
            = new LogUtil.Tag(AINRCaptureRequestConfig.class.getSimpleName());

    private int mCameraId = -1;
    private static final String VALUE_OFF = "off";
    private static final String VALUE_ON = "on";

    private CaptureRequest.Key<int[]> mKeyAinrRequsetSessionMode;
    private AINR mAinr;
    private SettingDevice2Requester mDeviceRequester;
    private List<String> mSupportedValues = new ArrayList<>();
    private Context mContext;

    /**
     * AINR mode enum value.
     */
    enum ModeEnum {
        OFF(0),
        ON(1);

        private int mValue = 0;
        ModeEnum(int value) {
            this.mValue = value;
        }

        /**
         * Get enum value which is in integer.
         *
         * @return The enum value.
         */
        public int getValue() {
            return this.mValue;
        }

        /**
         * Get enum name which is in string.
         *
         * @return The enum name.
         */
        public String getName() {
            return this.toString();
        }
    }

    /**
     * AINR capture request configure constructor.
     * @param ainr The instance of {@link AINR}.
     * @param device2Requester The implementer of {@link SettingDevice2Requester}.
     * @param context The camera context.
     */
    public AINRCaptureRequestConfig(AINR ainr, SettingDevice2Requester device2Requester,
                                   Context context) {
        mContext = context;
        mAinr = ainr;
        mDeviceRequester = device2Requester;
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
                .getDeviceDescriptionMap().get(String.valueOf(mAinr.getCameraId()));
        if(deviceDescription!=null) {
            mKeyAinrRequsetSessionMode = deviceDescription.getKeyAinrRequsetSessionMode();
        }else {
            LogHelper.i(TAG, "initAinrVendorKey deviceDescription = null ");
        }
        LogHelper.i(TAG, "initAinrVendorKey init vendor key from device spec mCameraId: "
                + mCameraId);

        List<String> supportedModes = new ArrayList<>();
        int[] modes = {0,1};
        mSupportedValues.addAll(convertEnumToString(modes));
        supportedModes.add(VALUE_OFF);
        supportedModes.add(VALUE_ON);
        mAinr.initializeValue(supportedModes, VALUE_OFF);
    }

    public void setmCameraId(int mCameraId) {
        //check this
        this.mCameraId = mCameraId;
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        if (captureBuilder == null) {
            LogHelper.d(TAG, "[configCaptureRequest] captureBuilder is null");
            return;
        }
        if(mKeyAinrRequsetSessionMode == null){
            LogHelper.d(TAG, "[configCaptureRequest] mKeyAinrRequsetSessionMode is null");
            return;
        }
        String value = mAinr.getValue();
        // String overrideValue = mAinr.getOverrideValue();
        LogHelper.d(TAG, "[configCaptureRequest], value:" + value
                + ", ainr override value:");
        if (value == null) {
            return;
        }
        if (VALUE_OFF.equals(value)) {
            LogHelper.d(TAG, "[configCaptureRequest], value:" + value);
        }
        if (VALUE_ON.equals(value)) {
            LogHelper.d(TAG, "[configCaptureRequest], value:" + value);
        }

        if (!mAinr.isAINRSupported()) {
            value = "0";
            LogHelper.d(TAG, "[configCaptureRequest] isAINRsupport false reset value 0");
        }
        int[] mode = new int[1];
        mode[0] = convertStringToEnum(value);
        LogHelper.i(TAG, "[configCaptureRequest], mode[0]:" + mode[0]+",mKeyAinrRequsetSessionMode:"+mKeyAinrRequsetSessionMode);
        captureBuilder.set(mKeyAinrRequsetSessionMode, mode);
    }

    @Override
    public void configSessionParams(CaptureRequest.Builder captureBuilder) {

    }

    @Override
    public void configSessionSurface(List<Surface> surfaces) {

    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return null;
    }

    @Override
    public void sendSettingChangeRequest() {
        mDeviceRequester.createAndChangeRepeatingRequest();
    }

    @Override
    public Surface configRawSurface() {
        return null;
    }

    private List<String> convertEnumToString(int[] enumIndexs) {
        if (enumIndexs == null) {
            LogHelper.d(TAG, "[convertEnumToString], convert enum indexs is null");
            return new ArrayList<>();
        }
        ModeEnum[] modes = ModeEnum.values();
        List<String> names = new ArrayList<>(enumIndexs.length);
        for (int i = 0; i < enumIndexs.length; i++) {
            int enumIndex = enumIndexs[i];
            for (ModeEnum mode : modes) {
                if (mode.getValue() == enumIndex) {
                    String name = mode.getName().replace('_', '-').toLowerCase(Locale.ENGLISH);
                    names.add(name);
                    break;
                }
            }
        }
        return names;
    }

    private int convertStringToEnum(String value) {
        int enumIndex = 0;
        ModeEnum[] modes = ModeEnum.values();
        for (ModeEnum mode : modes) {
            String modeName = mode.getName().replace('_', '-').toLowerCase(Locale.ENGLISH);
            if (modeName.equalsIgnoreCase(value)) {
                enumIndex = mode.getValue();
                break;
            }
        }
        return enumIndex;
    }
}
