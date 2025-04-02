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
package com.mediatek.camera.feature.setting.format;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.loader.FeatureEntryBase;
import com.mediatek.camera.common.mode.photo.HeifHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.portability.SystemProperties;

/**
 * This class is for self timer feature entry.
 */

public class FormatEntry extends FeatureEntryBase {
    private static final Tag TAG = new Tag(
            FormatEntry.class.getSimpleName());
    public static final String TAG_HEIF_FLOW_PROPERTY = "vendor.mtk.camera.app.heif.flow";
    public static final String TAG_HEIF_WRITER_SUPPORT="ro.vendor.mtk_heif_capture_support";
    public static final int HEIF_WRITER_FLOW = 1;
    public static final int HEIF_AOSP_FLOW = 0;

    public static int HEIF_FLOW = SystemProperties.getInt(TAG_HEIF_FLOW_PROPERTY, HEIF_AOSP_FLOW);
    private static int mHeifWriterSupport = SystemProperties.getInt(TAG_HEIF_WRITER_SUPPORT,
            0);


    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public FormatEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraApi currentCameraApi, Activity activity) {
        if (isThirdPartyIntent(activity)){
            LogHelper.d(TAG, "[isSupport] false for third party intent");
            return false;
        }
        if (CameraApi.API1.equals(currentCameraApi)) {
            LogHelper.d(TAG, "[isSupport] false for api1");
            return false;
        }
        if (CameraApi.API2.equals(currentCameraApi)&& HEIF_FLOW == HEIF_AOSP_FLOW) {
	        return true;
        }
        LogHelper.i(TAG, "[isSupport] heif writer flow");
        if (CameraApi.API2.equals(currentCameraApi)&&heifWriterSupport()){
        	return true;
        }
	    LogHelper.w(TAG, "HeifWriterSupport false");
        return false;

    }

    public static boolean heifWriterSupport(){
        return 1 == mHeifWriterSupport;
    }

    @Override
    public String getFeatureEntryName() {
        return FormatEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraSetting.class;
    }

    @Override
    public int getStage() {
        return 1;
    }

    @Override
    public Object createInstance() {
        return new Format();
    }
}
