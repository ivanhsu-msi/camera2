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
package com.mediatek.camera.feature.setting.videoformat;

import android.app.Activity;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.provider.MediaStore;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.portability.SystemProperties;

/**
 * VideoFormat setting item.
 */
public class Videoformat extends SettingBase implements
        VideoFormatSettingView.OnValueChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(Videoformat.class.getSimpleName());
    private static final String KEY_VIDEO_FORMAR = "key_video_format";
    private ISettingChangeRequester mSettingChangeRequester;
    private VideoFormatSettingView mSettingView;
    private static final String FORMAT_DEFAULTVALUE="h264";
    private static final String FORMAT_HEVC="HEVC";
    private static final String KEY_FPS60="key_fps60";
    private static final String KEY_VIDEO_QUALITY ="key_video_quality";
    private static final String KEY_HDR_10 ="key_hdr10";
    private static final String KEY_STANDARD_HDR10 = "key_standard_hdr10";
    private static final String HLG10 = "2";
    private static final String HDR10 = "4";
    private static final String HDR10_PLUS = "8";
    private static final boolean mNeedH264Restrict4k60 =
            SystemProperties.getInt("vendor.mtk.camera.app.4k60.off.h264.enable",0) == 0;

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);
        setValue(mDataStore.getValue(getKey(), FORMAT_DEFAULTVALUE, getStoreScope()));
        mSettingView = new VideoFormatSettingView(getKey(), this);
        mSettingView.setOnValueChangeListener(this);
        mStatusMonitor.registerValueChangedListener(KEY_FPS60, mVideoFormatStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_VIDEO_QUALITY, mVideoFormatStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_HDR_10, mVideoFormatStatusChangeListener);
        mStatusMonitor.registerValueChangedListener(KEY_STANDARD_HDR10, mVideoFormatStatusChangeListener);
    }

    private StatusMonitor.StatusChangeListener mVideoFormatStatusChangeListener = new StatusMonitor
            .StatusChangeListener() {

        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.i(TAG, "[onStatusChanged]+ key: " + key + "," +
                    "value: " + value);
            boolean onlySupportHevc = false;
            switch (key){
                case KEY_HDR_10:
                case KEY_FPS60:
                case KEY_VIDEO_QUALITY:
                    if ("on".equals(mSettingController.queryValue(KEY_HDR_10))) {
                        onlySupportHevc = true;
                        LogHelper.d(TAG, "[onStatusChanged] hevc due to hdr10");
                        break;
                    }
                    if (mNeedH264Restrict4k60){
                        if("on".equals(mSettingController.queryValue(KEY_FPS60))
                                &&CamcorderProfile.QUALITY_2160P == (Integer.parseInt(
                                mSettingController.queryValue(KEY_VIDEO_QUALITY)))){
                            onlySupportHevc = true;
                            LogHelper.d(TAG, "[onStatusChanged] hevc due to fps and video quality");
                            break;
                        }
                    }
                    break;
                case KEY_STANDARD_HDR10:
                    if (value.equals(HDR10) || value.equals(HDR10_PLUS)) {
                        refreshViewEntry(FORMAT_HEVC);
                    }
                    break;
                default:
                    break;
            }
            if (onlySupportHevc){
                refreshViewEntry(FORMAT_HEVC);
            }
            LogHelper.i(TAG, "[onStatusChanged]- onlySupportHevc = " + onlySupportHevc);
        }
    };

    private void refreshViewEntry(String currentValue){
        updateValue(currentValue);
        mSettingView.setValue(currentValue);
        mAppUi.refreshSettingView();
    }

    @Override
    public void unInit() {
        mStatusMonitor.unregisterValueChangedListener(KEY_FPS60, mVideoFormatStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_VIDEO_QUALITY, mVideoFormatStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_HDR_10, mVideoFormatStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(KEY_STANDARD_HDR10, mVideoFormatStatusChangeListener);
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
        mSettingView.setEntryValues(getEntryValues());
        mSettingView.setEnabled(getEntryValues().size() > 1);

    }

    @Override
    public void postRestrictionAfterInitialized() {
        handleRestrictions();
    }

    private void handleRestrictions() {
        Relation relation = VideoFormatRestriction.getRestriction().getRelation(getValue(),true);
        mSettingController.postRestriction(relation);

        if (mNeedH264Restrict4k60){
            Relation multiRelation = VideoFormatRestriction.getMultiRelation().getRelation(getValue(),true);
            mSettingController.postRestriction(multiRelation);
        }

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_VIDEO_FORMAR;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        VideoFormatCaptureRequestConfig captureRequestConfig;
        if (mSettingChangeRequester == null) {
            captureRequestConfig =
                    new VideoFormatCaptureRequestConfig(this, mSettingDevice2Requester,
                            mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (VideoFormatCaptureRequestConfig) mSettingChangeRequester;
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
     * Callback when video format value changed.
     * @param value The changed video format, such as "1920x1080".
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