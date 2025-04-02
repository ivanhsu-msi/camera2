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

package com.mediatek.camera.feature.setting.tpi;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;

import java.util.ArrayList;
import java.util.List;

import static com.mediatek.camera.feature.setting.tpi.TpiSettingView.VALUE_OFF;

public class TpiCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure{

	private static final LogUtil.Tag TAG = new LogUtil.Tag(TpiCaptureRequestConfig.class.getSimpleName());

	private final Tpi mTpi;
	private final SettingDevice2Requester mDevice2Requester;
	private Context mContext;
	private CameraCharacteristics.Key<int[]> mKeyTpiSupportValue;
	private CaptureRequest.Key<int[]> mKeyTpiRequestValue;


	/**
	 * third-party plugin interface capture quest configure constructor.
	 *
	 * @param tpi The instance of {@link Tpi}.
	 * @param device2Requester The implementer of {@link SettingDevice2Requester}.
	 */
	public TpiCaptureRequestConfig(Tpi tpi, SettingDevice2Requester device2Requester,
	                               Context context) {
		mTpi = tpi;
		mDevice2Requester = device2Requester;
		mContext = context;
	}

	@Override
	public void sendSettingChangeRequest() {
		mDevice2Requester.requestRestartSession();
	}

	private void initTpiVendorKey(CameraCharacteristics cs) {
		DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
				.getDeviceDescriptionMap().get(String.valueOf(mTpi.getCameraId()));
		if (deviceDescription != null) {
			mKeyTpiSupportValue = deviceDescription.getKeyTpiSupportValue();
			mKeyTpiRequestValue = deviceDescription.getKeyTpiRequestValue();
		}
		LogHelper.d(TAG, "mKeyTpiSupportValue = " + mKeyTpiSupportValue
			+ " mKeyTpiRequestValue " + mKeyTpiRequestValue);
	}

	@Override
	public void setCameraCharacteristics(CameraCharacteristics characteristics) {
		initTpiVendorKey(characteristics);
		List<String> supportedValueList = getSupportedList(characteristics);
		String defaultValue = VALUE_OFF;
		mTpi.onValueInitialized(supportedValueList, defaultValue);

	}

	@Override
	public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {
		String value = mTpi.getValue();
		LogHelper.d(TAG, "[configCaptureRequest], value:" + value);
		if (value != null && captureBuilder != null && mKeyTpiRequestValue!=null
            && mTpi.isCurrentModeSupported()) {
			int[] sceniar = new int[1];
			sceniar[0] = Integer.parseInt(value);
			captureBuilder.set(mKeyTpiRequestValue, sceniar);
		}

	}

	@Override
	public void configSessionParams(CaptureRequest.Builder captureBuilder) {

	}

	/**
	 * Get supported value list.
	 *
	 * @param characteristics THe Characteristics.
	 * @return The supported value list.
	 */
	public List<String> getSupportedList(CameraCharacteristics characteristics) {

		if (mKeyTpiSupportValue == null){
			LogHelper.w(TAG, "[getSupportedList] mKeyTpiSupportValue null");
			return null;
		}

		int[] val = characteristics.get(mKeyTpiSupportValue);
		if (val == null) {
			LogHelper.w(TAG, "[getSupportedList] val null");
			return null;
		}
		LogHelper.d(TAG, "[getSupportedList] tpi range (" + 0 + ", " + val[0] + ")");
		ArrayList<String> values = new ArrayList<>();
		for (int i = 0; i <= val[0]; i ++) {
			values.add(String.valueOf(i));
		}
		LogHelper.d(TAG, "[getSupportedList] values = " + values);
		return values;
	}

	@Override
	public void configSessionSurface(List<Surface> surfaces) {

	}

	@Override
	public Surface configRawSurface() {
		return null;
	}

	@Override
	public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
		return mPreviewCallback;
	}

	private CameraCaptureSession.CaptureCallback mPreviewCallback
			= new CameraCaptureSession.CaptureCallback() {

	};

}
