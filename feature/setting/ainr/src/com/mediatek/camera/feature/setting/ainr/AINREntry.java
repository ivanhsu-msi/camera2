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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.portability.SystemProperties;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

/**
 * AINR entry for feature provider.
 */

public class AINREntry extends FeatureEntryBase {
    private AINR mAinr = null;
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(AINREntry.class.getSimpleName());
    private String mCameraId = "0";

    /**
     * create an entry.
     * @param context   current activity.
     * @param resources current resources.
     */
    public AINREntry(Context context, Resources resources) {
        super(context, resources);
    }
    @Override
    public void notifyBeforeOpenCamera(@Nonnull String cameraId, @Nonnull CameraApi cameraApi) {
        super.notifyBeforeOpenCamera(cameraId, cameraApi);
        mCameraId = cameraId;
        LogHelper.d(TAG, "[notifyBeforeOpenCamera] mCameraId = " + mCameraId);
    }
    @Override
    public boolean isSupport(CameraApi currentCameraApi, Activity activity) {
        if (isThirdPartyIntent(activity)) {
            return false;
        }
        if(!(SystemProperties.getInt("ro.vendor.videoainr.enable", 0) == 1 ? true : false)){
            LogHelper.d(TAG, "[isSupport] videoainr");
            return false;
        }
        ConcurrentHashMap<String, DeviceDescription>
                deviceDescriptionMap = mDeviceSpec.getDeviceDescriptionMap();
        DeviceDescription deviceDescription = deviceDescriptionMap.get(mCameraId);
        if (deviceDescription != null)
            return deviceDescription.isAinrSupprot();
        return false;
    }

    @Override
    public String getFeatureEntryName() {
        return AINREntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public Object createInstance() {
        mAinr = new AINR();
        return mAinr;
    }
}
