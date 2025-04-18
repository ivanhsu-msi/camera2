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
package com.mediatek.camera.feature.setting.zoommanualing;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import android.util.Range;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.text.TextUtils;
import java.math.BigDecimal;


/**
 * Configure ZoomManualing in capture request in camera api2.
 */

public class ZoomManualingCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG
            = new LogUtil.Tag(ZoomManualingCaptureRequestConfig.class.getSimpleName());
    private ZoomManualing mZoomManualing;
    private ISettingManager.SettingDevice2Requester mDevice2Requester;
    private CaptureRequest.Key<int[]> mKeyZoomManualingRequestRatio;
    private boolean mIsSupport = false;
    private Context mContext;

    /**
     * ZoomManualing capture request configure constructor.
     *
     * @param ZoomManualing The instance of {@link ZoomManualing}.
     * @param device2Requester The implementer of {@link SettingDevice2Requester}.
     * @param context The camera context.
     */
    public ZoomManualingCaptureRequestConfig(ZoomManualing zmManualing,
                                   SettingDevice2Requester device2Requester,
                                   Context context) {
        mContext = context;
        mZoomManualing = zmManualing;
        mDevice2Requester = device2Requester;
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        LogHelper.d(TAG, "setCameraCharacteristics");
        initZoomManualingVendorKey(characteristics);
        List<Integer> zmDefaultRatioList = new ArrayList<Integer>();
        zmDefaultRatioList.add(0);
        mZoomManualing.onValueInitialized(zmDefaultRatioList, mIsSupport);
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
        if (captureBuilder == null) {
            LogHelper.d(TAG, "[configCaptureRequest] captureBuilder is null");
            return;
        }
        if (!mZoomManualing.IsSupported()){
            LogHelper.i(TAG, "ZoomManualing is not support");
            return;
        }

        String value = mZoomManualing.getValue();
        if (value != null && !"".equals(value)) {
            ArrayList<Integer> configValue = split(value);
            if (configValue != null) {
                LogHelper.d(TAG, "configCaptureRequest value = " + value
                        + ", configValue[0] = " + configValue.get(0));
                int[] zoomValue = new int[1];
                zoomValue[0] = configValue.get(0);
                captureBuilder.set(mKeyZoomManualingRequestRatio, zoomValue);
            } else {
                LogHelper.i(TAG, "configCaptureRequest configValue = null");
            }
        }
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
    public Surface configRawSurface() {
        return null;
    }



    @Override
    public void sendSettingChangeRequest() {
        mDevice2Requester.createAndChangeRepeatingRequest();
    }

    private void initZoomManualingVendorKey(CameraCharacteristics cs) {
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
                .getDeviceDescriptionMap().get(String.valueOf(mZoomManualing.getCameraId()));
        if (deviceDescription != null) {
            mKeyZoomManualingRequestRatio = deviceDescription.getKeyZoomManualingRequestMode();
            mIsSupport = deviceDescription.isZoomManualingSupport();
        }
        LogHelper.i(TAG, "mIsSupport = " + mIsSupport +
                ", mKeyZoomManualingRequestRatio = " + mKeyZoomManualingRequestRatio);
    }

    private ArrayList<Integer> split(String str) {
        if (str == null) {
            return null;
        }

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<Integer> subInt = new ArrayList<>();
        for (String s : splitter) {
            subInt.add(Integer.parseInt(s));
        }
        return subInt;
    }
}
