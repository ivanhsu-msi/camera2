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
package com.mediatek.camera.feature.setting.videoquality;

import android.app.Activity;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.provider.MediaStore;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.portability.SystemProperties;

/**
 * VideoQuality setting item.
 */
public class VideoQuality extends SettingBase implements
VideoQualitySettingView.OnValueChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(VideoQuality.class.getSimpleName());
    private static final String KEY_VIDEO_QUALITY = "key_video_quality";
    private ISettingChangeRequester mSettingChangeRequester;
    private VideoQualitySettingView mSettingView;
    private StatusMonitor.StatusResponder mViewStatusResponder;
    private static final String KEY_VIDEO_QUALITY_STATUS = "key_video_quality_status";
    private static final String PHOTO_MODE = "com.mediatek.camera.common.mode.photo.PhotoMode";
    private static final String STEREO_VIDEO_MODE
            = "com.mediatek.camera.feature.mode.vsdof.video.SdofVideoMode";
    private String mCurrentMode = PHOTO_MODE;

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingView = new VideoQualitySettingView(getKey(), this);
        mSettingView.setOnValueChangeListener(this);
        mViewStatusResponder = mStatusMonitor.getStatusResponder(KEY_VIDEO_QUALITY_STATUS);
    }

    @Override
    public void unInit() {

    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        LogHelper.d(TAG, "onModeOpened modeKey " + modeKey + ",modeType " + modeType);
        mCurrentMode = modeKey;
    }

    @Override
    public void onModeClosed(String modeKey) {
        LogHelper.d(TAG, "onModeClosed");
        super.onModeClosed(modeKey);
    }

    @Override
    public void addViewEntry() {
        if (mCurrentMode.equals(STEREO_VIDEO_MODE)) {
            LogHelper.d(TAG, "[addViewEntry] not support video quality in stereo mode.");
            return;
        }
        if (!isCaptureByIntent()) {
            mAppUi.addSettingView(mSettingView);
        }
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        mSettingView.setValue(getValue());
        mSettingView.setEntryValues(getEntryValues());
        mSettingView.setEnabled(getEntryValues().size() > 1);
    }

    @Override
    public void postRestrictionAfterInitialized() {
        if (mCurrentMode.equals(STEREO_VIDEO_MODE)) {
            LogHelper.d(TAG, "[postRestrictionAfterInitialized] not support video quality in stereo mode.");
            return;
        }
        checkAndPostRestriction(getValue());
        mSettingController.refreshViewEntry();
        mAppUi.refreshSettingView();
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_VIDEO_QUALITY;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        VideoQualityCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig =
                    new VideoQualityCaptureRequestConfig(this, mSettingDevice2Requester,
                            mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (VideoQualityCaptureRequestConfig) mSettingChangeRequester;
    }

    public String getCameraId() {
        return mSettingController.getCameraId();
    }

    public IApp getApp() {
        return mApp;
    }
    /**
     * Invoked after setting's all values are initialized.
     */
    public void onValueInitialized() {
        mSettingView.setValue(getValue());
        mSettingView.setEntryValues(getEntryValues());
    }
    /**
     * Callback when video quality value changed.
     * @param value The changed video quality, such as "1920x1080".
     */
    @Override
    public void onValueChanged(String value) {
        LogHelper.d(TAG, "[onValueChanged], value:" + value);
        if (!getValue().equals(value)) {
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            checkAndPostRestriction(value);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mViewStatusResponder.statusChanged(KEY_VIDEO_QUALITY_STATUS, value);
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
            mSettingController.refreshViewEntry();
            mAppUi.refreshSettingView();
        }
    }

    /**
     * update set value.
     * @param defaultValue the default value
     */
    public void updateValue(String defaultValue) {
        String value = parseIntent();
        if (value == null) {
            value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
        }
        setValue(value);
    }

    private void checkAndPostRestriction(String qualityValue){
        mSettingController.postRestriction(
                VideoQualityRestriction.getVideoQualityRelationGroup().getRelation(qualityValue, true));
        VideoQualityCaptureRequestConfig captureRequestConfig = (VideoQualityCaptureRequestConfig)getCaptureRequestConfigure();
        if(!captureRequestConfig.isFPS60Support(qualityValue)){
            mSettingController.postRestriction(
                    VideoQualityRestriction.getOffHFPSrelationGroup(qualityValue).getRelation(qualityValue, true));
        }else {
            mSettingController.postRestriction(
                    VideoQualityRestriction.getOnHFPSrelationGroup(getValue()).getRelation(qualityValue, true));
        }
        if(!qualityValue.equals(String.valueOf(CamcorderProfile.QUALITY_1080P)) && !qualityValue.equals(String.valueOf(CamcorderProfile.QUALITY_2160P))){
            mSettingController.postRestriction(
                    VideoQualityRestriction.getOffAINRrelationGroup(qualityValue).getRelation(qualityValue, true));
        }
    }
    private boolean isCaptureByIntent() {
        boolean isCaptureIntent = false;
        Activity activity = mApp.getActivity();
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            isCaptureIntent = true;
        }
        return isCaptureIntent;
    }

    private String parseIntent() {
        String quality = null;
        Activity activity = mApp.getActivity();
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            boolean userLimitQuality = intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY);
            if (userLimitQuality) {
                int extraVideoQuality = intent.getIntExtra(
                        MediaStore.EXTRA_VIDEO_QUALITY, 0);
                if (extraVideoQuality > 0 &&
                        CamcorderProfile.hasProfile(
                                Integer.parseInt(getCameraId()), extraVideoQuality))  {
                    quality = Integer.toString(extraVideoQuality);
                } else {
                    quality = Integer.toString(CamcorderProfile.QUALITY_LOW);
                }
            } else {
                quality = Integer.toString(CamcorderProfile.QUALITY_LOW);
            }
        }
        return quality;
    }
    protected SettingController getSettingController(){
        return mSettingController;
    }
}
