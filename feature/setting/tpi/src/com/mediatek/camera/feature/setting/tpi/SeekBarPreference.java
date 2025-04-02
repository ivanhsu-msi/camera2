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
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;

import java.util.Locale;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

	private static final LogUtil.Tag TAG = new LogUtil.Tag(SeekBarPreference.class.getSimpleName());

	private TextView mTextValue;

	private View mView;

	private OnValueChangeSuccessListener mOnValueChangeSuccessListener;

	private int mMax = 100;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWidgetLayoutResource(R.layout.seekbar);
	}

	public SeekBarPreference(Context context) {
		this(context, null);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {

		final LayoutInflater layoutInflater =
				(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = layoutInflater.inflate(R.layout.seekbar, parent, false);
		super.onCreateView(parent);
		return mView;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mTextValue = mView.findViewById(R.id.textValue);

		TextView textTitle = mView.findViewById(R.id.textTitle);
		textTitle.setText(getTitle());

		SeekBar seekBar = mView.findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);


		SharedPreferences sharedPreferences = getSharedPreferences();
		int value = sharedPreferences.getInt(getKey(), 0);
		mTextValue.setText(String.format(Locale.getDefault(), "%d", value));
		seekBar.setMax(mMax);
		seekBar.setProgress(value);
	}

	public void setMax(int max) {
		LogHelper.d(TAG, "[setMax] MAX = " + max);
		mMax = max;
		notifyChanged();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		LogHelper.d(TAG, "[onProgressChanged] tpi progress changed to " + progress);
		mTextValue.setText(String.format(Locale.getDefault(), "%d", seekBar.getProgress()));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		SharedPreferences.Editor editor = getEditor();
		editor.putInt(getKey(), seekBar.getProgress());
		editor.apply();

		if (mOnValueChangeSuccessListener != null) {
			mOnValueChangeSuccessListener.onValueChangedSuccessListener(
					String.valueOf(seekBar.getProgress()));
		}
	}


	public void setOnValueChangeSuccessListener(OnValueChangeSuccessListener l) {
		mOnValueChangeSuccessListener = l;
	}

	interface OnValueChangeSuccessListener {
		void onValueChangedSuccessListener(String value);
	}

}
