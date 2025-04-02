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
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.Preference;
import com.mediatek.camera.common.setting.ICameraSettingView;

import java.util.ArrayList;
import java.util.List;

/**
 * Video quality setting view.
 */
public class VideoFormatSettingView implements ICameraSettingView,
                       VideoFormatSelector.OnItemClickListener {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(VideoFormatSettingView.class.getSimpleName());
    private List<String> mEntryValues = new ArrayList<>();

    private VideoFormatSelector mFormatSelector;
    private OnValueChangeListener mListener;
    private Videoformat mVideoformat;
    private String mSelectedValue;
    private boolean mEnabled;
    private Activity mActivity;
    private Preference mPref;
    private String mSummary;
    private String mKey;

    /**
     * Listener to listen video format value changed.
     */
    public interface OnValueChangeListener {
        /**
         * Callback when video format value changed.
         * @param value The changed video format, such as "1920x1080".
         */
        void onValueChanged(String value);
    }
    /**
     * Video format setting view constructor.
     * @param key The key of video format
     * @param videoformat the video format
     */
    public VideoFormatSettingView(String key, Videoformat videoformat) {
        mKey = key;
        mVideoformat = videoformat;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        mActivity = fragment.getActivity();
        if (mFormatSelector == null) {
            mFormatSelector = new VideoFormatSelector();
            mFormatSelector.setOnItemClickListener(this);

        }
        mFormatSelector.setActivity(mActivity);
        mFormatSelector.setCurrentID(Integer.parseInt(mVideoformat.getCameraId()));
        mFormatSelector.setValue(mSelectedValue);
        mFormatSelector.setEntryValues(mEntryValues);
        fragment.addPreferencesFromResource(R.xml.videoformat_preference);
        mPref = (Preference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.video_format_setting);
        mPref.setContentDescription(mActivity.getResources()
                .getString(R.string.video_format_content_description));
        mPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                FragmentTransaction transaction = mActivity.getFragmentManager()
                        .beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mFormatSelector, "video_format_selector").commit();
                return true;
            }
        });
        mPref.setEnabled(mEnabled);
        mSummary = VideoFormatHelper.getQualityTitle(mActivity,mSelectedValue);
    }

    @Override
    public void refreshView() {
        if (mFormatSelector != null){
            mFormatSelector.setValue(mSelectedValue);
            mSummary = VideoFormatHelper.getQualityTitle(mActivity,mSelectedValue);
        }

        if (mPref != null) {
            mPref.setSummary(mSummary);
            mPref.setEnabled(mEnabled);
        }
        LogHelper.d(TAG, "[refreshView] mSelectedValue = " + mSelectedValue + " mFormatSelector = " + mFormatSelector);

    }

    @Override
    public void unloadView() {

    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Set listener to listen the changed video format value.
     * @param listener The instance of {@link OnValueChangeListener}.
     */
    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    /**
     * Set the default selected value.
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
    }

    /**
     * Set the video format supported.
     * @param entryValues The video format supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * Callback when item clicked.
     * @param value The video format clicked.
     */
    @Override
    public void onItemClick(String value) {
        mSelectedValue = value;
        mSummary = VideoFormatHelper.getQualityTitle(mActivity,value);
        mListener.onValueChanged(value);
    }
}
