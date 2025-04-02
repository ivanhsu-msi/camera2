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

import android.app.Activity;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.provider.MediaStore;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;


/**
 * standard hdr10 setting item.
 */
public class Standardhdr10 extends SettingBase implements
        StandardHDR10SettingView.OnValueChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Standardhdr10.class.getSimpleName());
    private static final String KEY_STANDARD_HDR10 = "key_standard_hdr10";
    private ISettingChangeRequester mSettingChangeRequester;
    private StandardHDR10SettingView mSettingView;
    private static final String HDR_DEFAULTVALUE="1";
    private static final String HLG10 = "2";
    private static final String HDR10 = "4";
    private static final String HDR10_PLUS = "8";
    private static final String HEVC = "HEVC";
    private static final String FORMAT_DEFAULTVALUE = "h264";

    private static final String KEY_VIDEO_QUALITY_STATUS = "key_video_quality_status";
    private static final String KEY_VIDEO_FROMAT_STATUS = "key_video_format";

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);
        setValue(mDataStore.getValue(getKey(), HDR_DEFAULTVALUE, getStoreScope()));
        mSettingView = new StandardHDR10SettingView(getKey(), this);
        mSettingView.setOnValueChangeListener(this);
        mStatusMonitor.registerValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_VIDEO_FROMAT_STATUS,mStatusChangeListener);
    }

    private StatusMonitor.StatusChangeListener mStatusChangeListener = new StatusMonitor
            .StatusChangeListener() {

        @Override
        public void onStatusChanged(String key, String value) {
            if(key.equals(KEY_VIDEO_QUALITY_STATUS)){
                updateRestriction();
            }
            if(key.equals(KEY_VIDEO_FROMAT_STATUS)) {
                if (value.equals(FORMAT_DEFAULTVALUE)) {
                    if (getValue().equals(HDR10) || getValue().equals(HDR10_PLUS)) {
                        refreshViewEntry(HDR_DEFAULTVALUE);
                    }
                }
            }
        }
    };

    private void refreshViewEntry(String currentValue) {
        updateValue(currentValue);
        mSettingView.setValue(currentValue);
        mAppUi.refreshSettingView();
    }

    private void updateRestriction() {
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mActivity.getApplication())
                .getDeviceDescriptionMap().get(String.valueOf(getCameraId()));
        if (isSupportQuality()) {
            if (!deviceDescription.isHDR10EisSupprot())
                mSettingController.postRestriction(
                        StandardHDR10Restriction.getOffHfpsRelationGroup().getRelation(getValue(), true));
        } else {
            if (!deviceDescription.isHDR10EisSupprot())
                mSettingController.postRestriction(
                        StandardHDR10Restriction.getSupHfpsRelationGroup().getRelation(getValue(), true));
        }
        mSettingController.refreshViewEntry();
        mAppUi.refreshSettingView();
    }

    @Override
    public void unInit() {
        mStatusMonitor.unregisterValueChangedListener(KEY_VIDEO_QUALITY_STATUS,mStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_VIDEO_FROMAT_STATUS,mStatusChangeListener);
    }

    @Override
    public void addViewEntry() {
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
        mSettingView.setEntryValues(getSupportedPlatformValues());
        mSettingView.setEnabled(getEntryValues().size() > 1);

    }

    @Override
    public void postRestrictionAfterInitialized() {
        updateRestriction();
    }

    private boolean isSupportQuality() {
        boolean is4kQuality;
        int currentQuality = Integer.parseInt(
                mSettingController.queryValue("key_video_quality"));
        is4kQuality = CamcorderProfile.QUALITY_2160P == currentQuality;
        LogHelper.d(TAG, "[is4kQuality]," + is4kQuality);
        return is4kQuality;
    }
    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_STANDARD_HDR10;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        StandardHDR10CaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig =
                    new StandardHDR10CaptureRequestConfig(this, mSettingDevice2Requester,
                            mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (StandardHDR10CaptureRequestConfig) mSettingChangeRequester;
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
        mSettingView.setEntryValues(getSupportedPlatformValues());

    }
    /**
     * Callback when standard hdr10 value changed.
     * @param value The changed standard hdr10, such as "1920x1080".
     */
    @Override
    public void onValueChanged(String value) {
        LogHelper.d(TAG, "[onValueChanged], value:" + value);
        if (!getValue().equals(value)) {
            updateValue(value);
            postRestrictionAfterInitialized();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
            mSettingController.refreshViewEntry();
            mAppUi.refreshSettingView();
        }
    }

    /**
     * update set value.
     * @param value the default value
     */
    public void updateValue(String value) {
        setValue(value);
        mDataStore.setValue(getKey(), value, getStoreScope(), false);
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
    protected SettingController getSettingController(){
        return mSettingController;
    }

}
