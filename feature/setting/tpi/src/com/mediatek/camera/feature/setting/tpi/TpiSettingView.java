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

import android.app.Activity;
import android.preference.PreferenceFragment;
import android.widget.SeekBar;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.setting.ICameraSettingView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for third-party plugin interface feature setting view.
 */

public class TpiSettingView implements ICameraSettingView {
	private static final LogUtil.Tag TAG = new LogUtil.Tag(TpiSettingView.class.getSimpleName());
	private SeekBarPreference mTpiSeekBarPreference;
	private boolean mEnabled;
	private String mValue;
	private String mKey;
	private ITpiViewListener.OnValueChangeListener mOnValueChangeListener;
	public static String VALUE_OFF = "0";
	private List<String> mEntries = new ArrayList<>();
	private List<String> mEntryValues = new ArrayList<>();

	public TpiSettingView(String key, Activity activity) {
		mKey = key;
	}

	@Override
	public void loadView(PreferenceFragment fragment) {
		fragment.addPreferencesFromResource(R.xml.tpi_preference);
		mTpiSeekBarPreference = (SeekBarPreference) fragment.findPreference(mKey);
		mTpiSeekBarPreference.setRootPreference(fragment.getPreferenceScreen());
		mTpiSeekBarPreference.setId(R.id.tpi_setting);
		mTpiSeekBarPreference.setContentDescription(fragment.getActivity().getResources()
				.getString(R.string.tpi_content_description));

		mTpiSeekBarPreference.setOnValueChangeSuccessListener(new SeekBarPreference.OnValueChangeSuccessListener() {
			@Override
			public void onValueChangedSuccessListener(String value) {
				mValue = value;
				LogHelper.d(TAG, "[onPreferenceChange] seekBar val = " + mValue);
				mOnValueChangeListener.onValueChanged(mValue);
			}

		});
		mTpiSeekBarPreference.setEnabled(mEnabled);
	}

	@Override
	public void refreshView() {
		if (mTpiSeekBarPreference != null && mEntryValues.size() - 1 >= 0) {
			String maxValue = mEntryValues.get(mEntryValues.size() - 1);
			LogHelper.d(TAG, "[refreshView] maxValue = " + maxValue);
			mTpiSeekBarPreference.setMax(Integer.parseInt(maxValue));
			mTpiSeekBarPreference.setEnabled(mEnabled);
		}
	}

	@Override
	public void unloadView() {

	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	/**
	 * Set listener to listen the changed value of tpi.
	 *
	 * @param listener The instance of {@link SeekBar.OnSeekBarChangeListener}.
	 */
	public void setOnValueChangeListener(ITpiViewListener.OnValueChangeListener listener) {
		mOnValueChangeListener = listener;
	}

	/**
	 * Set the default selected value.
	 *
	 * @param value The default selected value.
	 */
	public void setValue(String value) {
		mValue = value;
	}


	public void setEntryValues(List<String> entryValues) {
		mEntries.clear();
		mEntryValues.clear();

		mEntries.addAll(entryValues);
		mEntryValues.addAll(entryValues);
	}
}
