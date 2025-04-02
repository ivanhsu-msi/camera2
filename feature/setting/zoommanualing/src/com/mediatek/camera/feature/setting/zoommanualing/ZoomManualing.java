/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.feature.setting.zoommanualing;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.mode.ICameraMode;

import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;

import javax.annotation.Nonnull;

/**
 * ZoomManualing setting item.
 *
 */
public class ZoomManualing extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ZoomManualing.class.getSimpleName());
    private static final String KEY_ZOOMMANUALING = "key_zoommanualing";

    private ICameraMode.ModeType mModeType;
    private String mCurrentMode = "com.mediatek.camera.common.mode.photo.PhotoMode";
    private ISettingChangeRequester mSettingChangeRequester;
    private ZoomManualingViewController mZMViewController;
    private boolean mIsSupported = false;

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);
        LogHelper.d(TAG, "ZoomManualing init");
        if (mZMViewController == null) {
            mZMViewController = new ZoomManualingViewController(app, this);
        }
    }

    @Override
    public void unInit() {
        LogHelper.d(TAG, "ZoomManualing unInit");
        mZMViewController.zmViewUninit();
    }

    @Override
    public void addViewEntry() {
        if (!mIsSupported) {
            return;
        }
        LogHelper.d(TAG, "ZoomManualing addViewEntry");
        mZMViewController.addQuickSwitchIcon();
        mZMViewController.showQuickSwitchIcon(mIsSupported);
    }

    @Override
    public void removeViewEntry() {
        LogHelper.d(TAG, "ZoomManualing removeViewEntry");
        mZMViewController.removeQuickSwitchIcon();
    }

    @Override
    public void refreshViewEntry() {
        LogHelper.d(TAG, "ZoomManualing refreshViewEntry");
        mZMViewController.showQuickSwitchIcon(mIsSupported);
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_ZOOMMANUALING;
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        mCurrentMode = modeKey;
        mModeType = modeType;
    }

    @Override
    public void onModeClosed(String modeKey) {
        super.onModeClosed(modeKey);
        mZMViewController.modeClosedZMView();
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            ZoomManualingCaptureRequestConfig captureRequestConfig
                    = new ZoomManualingCaptureRequestConfig(this, mSettingDevice2Requester,
                    mActivity.getApplicationContext());
            mSettingChangeRequester = captureRequestConfig;
        }
        return (ZoomManualingCaptureRequestConfig) mSettingChangeRequester;
    }

    public void onValueInitialized(List<Integer> zmDefaultRatioList, boolean isSupport) {
        LogHelper.i(TAG, "ZoomManualing onValueInitialized, zmDefaultRatioList:"
                + zmDefaultRatioList + ", isSupport = " + isSupport);
        if (zmDefaultRatioList != null && zmDefaultRatioList.size() > 0) {
            String value = mDataStore.getValue(getKey(),
                    String.valueOf(zmDefaultRatioList.get(0)), getStoreScope());
            setValue(value);
            mIsSupported = isSupport;
        }
        //set AF&Zoom View
        mZMViewController.setSeekBarValue(zmDefaultRatioList);
    }

    public void onValueChanged( int zoomValue) {
        String value = ""+zoomValue;
        LogHelper.i(TAG, "ZoomManualing onValueChanged,  zoomValue = " + zoomValue
                + ",value = " + value);
        if (!getValue().equals(value)) {
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), true);
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    }

    /**
     * Get current mode type.
     *
     * @return mModeType current mode type.
     */
    public ICameraMode.ModeType getCurrentModeType() {
        return mModeType;
    }

    public IApp getApp() {
        return mApp;
    }

    public boolean IsSupported(){
        return mIsSupported;
    }

    /**
     * Get current camera id.
     * @return The current camera id.
     */
    protected int getCameraId() {
        int cameraId = Integer.parseInt(mSettingController.getCameraId());
        return cameraId;
    }

    private ArrayList<String> split(String str) {
        if (str == null) {
            return null;
        }

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<String> subStrings = new ArrayList<>();
        for (String s : splitter) {
            subStrings.add(s);
        }
        return subStrings;
    }
}
