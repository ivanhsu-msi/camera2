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

package com.mediatek.camera.feature.setting.eis;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import android.media.CamcorderProfile;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This class is for EIS feature interacted with others.
 */

public class EIS extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(EIS.class.getSimpleName());
    private static final String KEY_NOISE_EIS = "key_eis";
    private static final String EIS_OFF = "off";
    private static final String EIS_ON = "on";
    private boolean mIsSupported = false;
    private ISettingChangeRequester mSettingChangeRequester;
    private StatusMonitor.StatusResponder mViewStatusResponder;
    private EISSettingView mSettingView;
    private static final String KEY_VIDEO_QUALITY_STATUS = "key_video_quality_status";
    private static final String KEY_EIS_STATUS = "key_eis_status";
    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        setValue(mDataStore.getValue(getKey(), EIS_OFF, getStoreScope()));
        mSettingView = new EISSettingView();
        mSettingView.setEISViewListener(mEISViewListener);
        mViewStatusResponder = mStatusMonitor.getStatusResponder(KEY_EIS_STATUS);
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSettingView != null) {
                    mSettingView.setChecked(EIS_ON.equals(getValue()));
                    mSettingView.setEnabled(getEntryValues().size() > 1);
                }
            }
        });
    }

    @Override
    public void postRestrictionAfterInitialized() {
        updateEisRestriction();
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_NOISE_EIS;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        EISCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig = new EISCaptureRequestConfig(
                    this, mSettingDevice2Requester,
                    mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (EISCaptureRequestConfig) mSettingChangeRequester;
    }

    private EISSettingView.OnEISViewListener mEISViewListener
            = new EISSettingView.OnEISViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
            LogHelper.d(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? EIS_ON : EIS_OFF;
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateEisRestriction();
                    mViewStatusResponder.statusChanged(KEY_EIS_STATUS,value);
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }

        @Override
        public boolean onCachedValue() {
            return EIS_ON.equals(
                    mDataStore.getValue(getKey(), EIS_OFF, getStoreScope()));
        }
    };

    /**
     * if not support 60fps in the select quality in eis mode --> off 60fps
     * in eis support 60fps ,but for the hdr10+ the value is on and quality is 4k --> off 60fps
     * in eis support 60fps ,but for the hdr10+ the setting is closed --> on 60fps
     */
    private void updateEisRestriction(){
        EISCaptureRequestConfig eisCaptureRequestConfig = (EISCaptureRequestConfig)getCaptureRequestConfigure();
        if(mSettingController.queryValue("key_video_quality")==null){
            LogHelper.d(TAG, "[key_video_quality] is null, return");
            return;
        }
        boolean isSupport = eisCaptureRequestConfig.isSupportHfps(mSettingController.queryValue("key_video_quality"));
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mActivity.getApplication())
                .getDeviceDescriptionMap().get(String.valueOf(getCameraId()));
        boolean isSupprotHDR10=deviceDescription.isHDR10EisSupprot();
        if(!isSupport){
            if(isSupprotHDR10){
                mSettingController.postRestriction(
                        EisRestriction.getOffHfpsLpRelationGroup().getRelation(getValue(), true));
            }else {
                mSettingController.postRestriction(
                        EisRestriction.getOffHfpsRelationGroup().getRelation(getValue(), true));
            }
        }else if(!(mSettingController.queryValue("key_video_quality").
                equals(String.valueOf(CamcorderProfile.QUALITY_2160P)))){
            if(mSettingController.queryValue("key_hdr10")!=null){
                if(mSettingController.queryValue("key_hdr10").equals("on")){
                    if(isSupprotHDR10){
                        mSettingController.postRestriction(
                                EisRestriction.getSupHfpsLpRelationGroup().getRelation(getValue(), true));
                    }else {
                        mSettingController.postRestriction(
                                EisRestriction.getSupHfpsRelationGroup().getRelation(getValue(), true));
                    }

                }else if(!isSupprotHDR10){
                    mSettingController.postRestriction(
                            EisRestriction.getCommonEisRelation().getRelation(getValue(),true));
                }
            }else {
                if(isSupprotHDR10){
                    mSettingController.postRestriction(
                            EisRestriction.getSupHfpsLpRelationGroup().getRelation(getValue(), true));
                }else {
                    mSettingController.postRestriction(
                            EisRestriction.getSupHfpsRelationGroup().getRelation(getValue(), true));
                }

            }
        }else if(!isSupprotHDR10){
            mSettingController.postRestriction(
                    EisRestriction.getCommonEisRelation().getRelation(getValue(),true));
        }
        mSettingController.refreshViewEntry();
        mAppUi.refreshSettingView();
    }
    private boolean is4KQuality() {
        boolean is4kQuality;
        int currentQuality = Integer.parseInt(
                mSettingController.queryValue("key_video_quality"));
        is4kQuality = CamcorderProfile.QUALITY_2160P == currentQuality;
        LogHelper.d(TAG, "[is4kQuality]," + is4kQuality);
        return is4kQuality;
    }
    /**
     * update set value.
     *
     * @param value the default value
     */
    public void updateValue(String value) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setValue(mDataStore.getValue(getKey(), value, getStoreScope()));
            }
        });
    }

    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    protected SettingController getSettingController() {
        return mSettingController;
    }
    private StatusMonitor.StatusChangeListener mStatusChangeListener = new StatusMonitor.StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            if(key.equals(KEY_VIDEO_QUALITY_STATUS)){
                    updateEisRestriction();
            }
        }
    };
}
