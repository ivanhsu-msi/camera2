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
 * MediaTek Inc. (C) 2021. All rights reserved.
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
package com.mediatek.camera.feature.setting.afbc;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager.SettingController;
import com.mediatek.camera.common.setting.SettingBase;

import java.util.List;

/**
 * This setting preview compression
 */
public class AFBC extends SettingBase{

	private static final LogUtil.Tag TAG = new LogUtil.Tag(AFBC.class.getSimpleName());

	private ISettingChangeRequester mSettingChangeRequester;
	private static final String VALUE_ON = "on";
	private static final String KEY_AFBC = "key_afbc";

	@Override
	public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
		super.init(app,cameraContext,settingController);
		String value = mDataStore.getValue(getKey(), VALUE_ON, getStoreScope());
		LogHelper.d(TAG, "[init], value:" + value);
		setValue(value);
	}

	@Override
	public void unInit() {

	}

	@Override
	public void addViewEntry() {

	}


	@Override
	public void removeViewEntry() {
	}

	@Override
	public void refreshViewEntry() {

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
		return KEY_AFBC;
	}

	@Override
	public ICaptureRequestConfigure getCaptureRequestConfigure() {
		if (mSettingChangeRequester == null) {
			AFBCCaptureRequestConfig captureRequestConfig =
					new AFBCCaptureRequestConfig(this, mSettingDevice2Requester,mActivity);
			mSettingChangeRequester = captureRequestConfig;
		}
		return (AFBCCaptureRequestConfig) mSettingChangeRequester;
	}

	/**
	 * Get current camera id.
	 * @return The current camera id.
	 */
	protected int getCameraId() {
		return Integer.parseInt(mSettingController.getCameraId());
	}


}
