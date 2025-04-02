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

package com.mediatek.camera.feature.setting.hdr10;

import android.media.CamcorderProfile;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

/**
 * This class is for HDR10+ feature interacted with others.
 */

public class Hdr10 extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Hdr10.class.getSimpleName());
    private static final String KEY_HDR10 = "key_hdr10";
    private static final String HDR10_OFF = "off";
    private static final String HDR10_ON = "on";
    private ISettingChangeRequester mSettingChangeRequester;
    private StatusMonitor.StatusResponder mViewStatusResponder;
    private Hdr10SettingView mSettingView;
    private static final String KEY_VIDEO_QUALITY_STATUS = "key_video_quality_status";
    private static final String KEY_HDR10_STATUS = "key_hdr10_status";

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        setValue(mDataStore.getValue(getKey(), HDR10_OFF, getStoreScope()));
        mSettingView = new Hdr10SettingView();
        mSettingView.setHdr10Listener(mHdr10ViewListener);
        mViewStatusResponder = mStatusMonitor.getStatusResponder(KEY_HDR10_STATUS);
        mStatusMonitor.registerValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);
    }

    @Override
    public void unInit() {
        mStatusMonitor.unregisterValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);
    }

    @Override
    public void addViewEntry() {
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setChecked(HDR10_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {
        updateHdr10Restriction();
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_HDR10;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        Hdr10CaptureRequestConfig requestConfig;
        if (mSettingChangeRequester == null) {
            requestConfig = new Hdr10CaptureRequestConfig(this, mSettingDevice2Requester, mActivity.getApplication());
            mSettingChangeRequester = requestConfig;
        }
        return (Hdr10CaptureRequestConfig) mSettingChangeRequester;
    }

    private Hdr10SettingView.OnHdr10ViewListener mHdr10ViewListener
            = new Hdr10SettingView.OnHdr10ViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
            LogHelper.i(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? HDR10_ON : HDR10_OFF;
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateHdr10Restriction();
                    mViewStatusResponder.statusChanged(KEY_HDR10_STATUS,value);
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }

        @Override
        public boolean onCachedValue() {
            return HDR10_ON.equals(
                    mDataStore.getValue(getKey(), HDR10_ON, getStoreScope()));
        }
    };

    /**
     * update set value.
     *
     * @param value the default value
     */
    public void updateValue(String value) {
        setValue(mDataStore.getValue(getKey(), value, getStoreScope()));
    }

    /**
     * Get current camera id.
     *
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    private void updateHdr10Restriction() {
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mActivity.getApplication())
                .getDeviceDescriptionMap().get(String.valueOf(getCameraId()));
        if (isSupportQuality()) {
            if (deviceDescription.isHDR10EisSupprot()) {
                mSettingController.postRestriction(
                        Hdr10Restriction.getOffHfpsLpRelationGroup().getRelation(getValue(), true));
            } else {
                mSettingController.postRestriction(
                        Hdr10Restriction.getOffHfpsRelationGroup().getRelation(getValue(), true));
            }
        } else {
            if (deviceDescription.isHDR10EisSupprot()) {
                mSettingController.postRestriction(
                        Hdr10Restriction.getSupHfpsLpRelationGroup().getRelation(getValue(), true));
            } else {
                mSettingController.postRestriction(
                        Hdr10Restriction.getSupHfpsRelationGroup().getRelation(getValue(), true));
            }
        }
        mSettingController.refreshViewEntry();
        mAppUi.refreshSettingView();
    }

    private boolean isSupportQuality() {
        boolean is4kQuality;
        int currentQuality = Integer.parseInt(
                mSettingController.queryValue("key_video_quality"));
        is4kQuality = CamcorderProfile.QUALITY_2160P == currentQuality;
        LogHelper.d(TAG, "[is4kQuality]," + is4kQuality);
        return is4kQuality;
    }

    private StatusMonitor.StatusChangeListener mStatusChangeListener = new StatusMonitor.StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            if(key.equals(KEY_VIDEO_QUALITY_STATUS)){
                    updateHdr10Restriction();
                }
        }
    };
}
