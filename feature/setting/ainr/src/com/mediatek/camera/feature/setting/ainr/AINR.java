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


import android.media.CamcorderProfile;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.mode.ICameraMode;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This class is for AINR feature interacted with others.
 */

public class AINR extends SettingBase implements AINRSettingView.OnAINRClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(AINR.class.getSimpleName());
    private static final String KEY_AINR = "key_ainr";
    private static final String AINR_OFF = "off";
    private static final String AINR_ON = "on";
    private ISettingChangeRequester mSettingChangeRequester;
    private static final String KEY_VIDEO_QUALITY_STATUS = "key_video_quality_status";
    private AINRSettingView mSettingView;
    private String mOverrideValue;
    private List<String> mNoSupported = new ArrayList<>();
    private List<String> mSupported = new ArrayList<>();
    private boolean mIsAINRSupported = false;
    private String[] supportResolution;

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        getSupportResolution();
        mSupported.add(AINR_OFF);
        mSupported.add(AINR_ON);
        mStatusMonitor.registerValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);
    }

    @Override
    public void unInit() {
        mStatusMonitor.unregisterValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);
    }

    private void getSupportResolution(){
//check vendor tag
    }
    @Override
    public void addViewEntry() {
        mIsAINRSupported = true;
        LogHelper.d(TAG, "[addViewEntry] mIsAINRSupported = on");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSettingView == null) {
                    mSettingView = new AINRSettingView();
                    mSettingView.setAINRClickListener(AINR.this);
                }
                mAppUi.addSettingView(mSettingView);
            }
        });
    }

    @Override
    public void removeViewEntry() {
        mIsAINRSupported = false;
        LogHelper.d(TAG, "[removeViewEntry] mIsAINRSupported = off");
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSettingView != null) {
                    mSettingView.setChecked(AINR_ON.equals(getValue()));
                    mSettingView.setEnabled(getEntryValues().size() > 1);
                }
            }
        });
    }

    @Override
    public void postRestrictionAfterInitialized() {
        Relation relation = AINRRestriction.getRestrictionGroup()
                .getRelation(getValue(), false);
        if (relation != null) {
            mSettingController.postRestriction(relation);
        }
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_AINR;
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        mOverrideValue = currentValue;
        super.overrideValues(headerKey, currentValue, supportValues);
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        AINRCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig = new AINRCaptureRequestConfig(this, mSettingDevice2Requester,
                    mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (AINRCaptureRequestConfig) mSettingChangeRequester;
    }

    public boolean isAINRSupported() {
        return mIsAINRSupported;
    }

    @Override
    public void onAINRClicked(boolean isOn) {
        LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
        String value = isOn ?AINR_ON : AINR_OFF;
        setValue(value);
        if (isOn){
            mIsAINRSupported = true;
        }else{
            mIsAINRSupported = false;
        }
        mDataStore.setValue(getKey(), value, getStoreScope(), false);
        Relation relation = AINRRestriction.getRestrictionGroup().getRelation(value, true);
        mSettingController.postRestriction(relation);
        mSettingController.refreshViewEntry();
        mAppUi.refreshSettingView();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSettingChangeRequester.sendSettingChangeRequest();
            }
        });
    }

    /**
     * Initialize setting all values after platform supported values ready.
     *
     * @param platformSupportedValues The values current platform is supported.
     * @param defaultValue            The scene mode default value.
     */
    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
            setEntryValues(platformSupportedValues);
            setSupportedEntryValues(platformSupportedValues);
            setSupportedPlatformValues(platformSupportedValues);
            setValue(mDataStore.getValue(getKey(), defaultValue, getStoreScope()));
    }

    @Override
    public void updateModeDeviceState(String newState) {
        super.updateModeDeviceState(newState);
    }

    /**
     * Get AINR override value.
     *
     * @return The override value.
     */
    public String getOverrideValue() {
        return mOverrideValue;
    }

    @Override
    public synchronized void onModeClosed(String modeKey) {

        mIsAINRSupported = false;
        super.onModeClosed(modeKey);
//        Relation relation = AINRRestriction.getRestrictionGroup().getRelation(KEY_AINR, true);
//        mSettingController.postRestriction(relation);
        LogHelper.d(TAG, "[onModeClosed] modeKey = " + modeKey);
    }

    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }
    private StatusMonitor.StatusChangeListener mStatusChangeListener = new StatusMonitor.StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            //todo check support size in vendor tag
            if(key.equals(KEY_VIDEO_QUALITY_STATUS)){
                LogHelper.d(TAG, "onStatusChanged" + value);
                if(value.equals(String.valueOf(CamcorderProfile.QUALITY_1080P)) || value.equals(String.valueOf(CamcorderProfile.QUALITY_2160P))){
                    mIsAINRSupported = true;
                    initializeValue(mSupported, null);
                }else{
                    mIsAINRSupported = false;
                    initializeValue(mNoSupported, null);
                    mDataStore.setValue(getKey(), AINR_OFF, getStoreScope(), false);
                }
                mSettingController.refreshViewEntry();
                mAppUi.refreshSettingView();
                mSettingChangeRequester.sendSettingChangeRequest();
            }
        }
    };
}