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
package com.mediatek.camera.feature.setting.antiflicker;

import com.mediatek.camera.R;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This class is for anti flicker feature interacted with others.
 */

public class AntiFlicker extends SettingBase implements
        AntiFlickerSettingView.OnValueChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(AntiFlicker.class.getSimpleName());
    private static final String KEY_ANTI_FLICKER = "key_anti_flicker";
    private AntiFlickerSettingView mSettingView;
    private ISettingChangeRequester mSettingChangeRequester;

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
    }

    @Override
    public void unInit() {

    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        super.overrideValues(headerKey, currentValue, supportValues);
    }

    @Override
    public void addViewEntry() {
        if (mSettingView == null) {
            mSettingView = new AntiFlickerSettingView(mActivity, getKey());
            mSettingView.setOnValueChangeListener(AntiFlicker.this);
        }

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
            mSettingView.setEntryValues(getEntryValues());
            mSettingView.setValue(getValue());
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }
        });
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_ANTI_FLICKER;
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            AntiFlickerCaptureRequestConfig captureRequestConfig
                    = new AntiFlickerCaptureRequestConfig(this, mSettingDevice2Requester);
            mSettingChangeRequester = captureRequestConfig;
        }
        return (AntiFlickerCaptureRequestConfig) mSettingChangeRequester;
    }

    @Override
    public void onValueChanged(String value) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
        LogHelper.d(TAG, "[onValueChanged], value:" + value);
        if (!getValue().equals(value)) {
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), true);
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    }
        });
    }

    /**
     * Initialize setting all values after platform supported values ready.
     *
     * @param platformSupportedValues The values current platform is supported.
     * @param defaultValue  The anti flicker default value.
     */
    public void initializeValue(List<String> platformSupportedValues, String defaultValue) {
        LogHelper.d(TAG, "[initializeValue], platformSupportedValues:" + platformSupportedValues
                + "default value:" + defaultValue);
        if (platformSupportedValues == null
                || platformSupportedValues.size() <= 0) {
            return;
        }
        setSupportedPlatformValues(platformSupportedValues);
        setSupportedEntryValues(platformSupportedValues);

        List<String> entryValues = new ArrayList<>(platformSupportedValues);
        setEntryValues(entryValues);

        String value = mDataStore.getValue(getKey(), defaultValue, getStoreScope());
        if (!entryValues.contains(value)) {
            value = entryValues.get(0);
        }
        setValue(value);
    }
}
