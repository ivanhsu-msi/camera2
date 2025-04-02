/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.feature.setting.standardhdr10;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.view.Surface;
import android.hardware.camera2.params.DynamicRangeProfiles;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Configure Standard HDR10 in capture request in camera api2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class StandardHDR10CaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG =
                      new LogUtil.Tag(StandardHDR10CaptureRequestConfig.class.getSimpleName());
    private SettingDevice2Requester mDevice2Requester;
    private Standardhdr10 mStandardhdr10;
    private Context mContext;
    /**
     * Standard HDR10 capture request configure constructor.
     * @param hdr10 The instance of {@link Standardhdr10}.
     * @param device2Requester The implementer of {@link SettingDevice2Requester}.
     */
    public StandardHDR10CaptureRequestConfig(Standardhdr10 hdr10,
                                             SettingDevice2Requester device2Requester,
                                             Context context) {
        mStandardhdr10 = hdr10;
        mDevice2Requester = device2Requester;
        mContext = context;
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        updateSupportedValues(characteristics);
        mStandardhdr10.onValueInitialized();
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

    private void updateSupportedValues(CameraCharacteristics characteristics) {
        List<String> supported = getSupportedList(characteristics);
        mStandardhdr10.setSupportedPlatformValues(supported);
        mStandardhdr10.setEntryValues(supported);
        mStandardhdr10.setSupportedEntryValues(supported);
    }

    private List<String> getSupportedList(CameraCharacteristics characteristics) {
        ArrayList<String> supported = new ArrayList<String>();
        DynamicRangeProfiles profiles = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_DYNAMIC_RANGE_PROFILES);
        if (profiles == null) {
            LogHelper.d(TAG, "[getSupportedList] profiles is null");
            return null;
        }
        Set<Long> profileslist = profiles.getSupportedProfiles();
        if (profileslist.size() == 0) {
            LogHelper.w(TAG, "[getSupportedList] profileslist.size is 0");
            return null;
        }
        for (int i = 0; i < StandardHDR10Helper.setDynamicRangeProfiles.length; i++) {
            if (profileslist.contains(StandardHDR10Helper.setDynamicRangeProfiles[i])) {
                supported.add(Long.toString(StandardHDR10Helper.setDynamicRangeProfiles[i]));
            }
        }
        return supported;
    }



    /**
     * Send request when setting value is changed.
     */
    @Override
    public void sendSettingChangeRequest() {
        mDevice2Requester.requestRestartSession();
    }
}