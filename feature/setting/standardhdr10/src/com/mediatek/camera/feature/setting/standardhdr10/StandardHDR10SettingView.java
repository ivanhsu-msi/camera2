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
 * Standard HDR10 setting view.
 */
public class StandardHDR10SettingView implements ICameraSettingView,
                       StandardHDR10Selector.OnItemClickListener {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(StandardHDR10SettingView.class.getSimpleName());
    private List<String> mEntryValues = new ArrayList<>();

    private StandardHDR10Selector mStandardHDR10Selector;
    private OnValueChangeListener mListener;
    private Standardhdr10 mStandardhdr10;
    private String mSelectedValue;
    private boolean mEnabled;
    private Activity mActivity;
    private Preference mPref;
    private String mSummary;
    private String mKey;

    /**
     * Listener to listen Standard HDR10 value changed.
     */
    public interface OnValueChangeListener {
        /**
         * Callback when standard hdr10 value changed.
         * @param value The changed standard hdr10, such as "1920x1080".
         */
        void onValueChanged(String value);
    }
    /**
     * Standard HDR10 setting view constructor.
     * @param key The key of standard hdr10
     * @param Standardhdr10 the standard hdr10
     */
    public StandardHDR10SettingView(String key, Standardhdr10 aosphdrt10) {
        mKey = key;
        mStandardhdr10 = aosphdrt10;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        mActivity = fragment.getActivity();
        if (mStandardHDR10Selector == null) {
            mStandardHDR10Selector = new StandardHDR10Selector();
            mStandardHDR10Selector.setOnItemClickListener(this);

        }
        mStandardHDR10Selector.setActivity(mActivity);
        mStandardHDR10Selector.setCurrentID(Integer.parseInt(mStandardhdr10.getCameraId()));
        mStandardHDR10Selector.setValue(mSelectedValue);
        mStandardHDR10Selector.setEntryValues(mEntryValues);
        fragment.addPreferencesFromResource(R.xml.standard_hdr10_preference);
        mPref = (Preference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.standard_hdr10_setting);
        mPref.setContentDescription(mActivity.getResources()
                .getString(R.string.standardhdr10_content_description));
        mPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                FragmentTransaction transaction = mActivity.getFragmentManager()
                        .beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mStandardHDR10Selector, "key_standard_hdr10").commit();
                return true;
            }
        });
        mPref.setEnabled(mEnabled);
        mSummary = StandardHDR10Helper.getQualityTitle(mActivity,mSelectedValue);
    }

    @Override
    public void refreshView() {
        if (mStandardHDR10Selector != null){
            mStandardHDR10Selector.setValue(mSelectedValue);
            mSummary = StandardHDR10Helper.getQualityTitle(mActivity,mSelectedValue);
        }

        if (mPref != null) {
            mPref.setSummary(mSummary);
            mPref.setEnabled(mEnabled);
        }
        LogHelper.d(TAG, "[refreshView] mSelectedValue = " + mSelectedValue + " mstandardhdr10Selector = " + mStandardHDR10Selector);

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
     * Set listener to listen the changed standard hdr10 value.
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
     * Set the standard hdr10 supported.
     * @param entryValues The standard hdr10 supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * Callback when item clicked.
     * @param value The standard hdr10 clicked.
     */
    @Override
    public void onItemClick(String value) {
        mSelectedValue = value;
        mSummary = StandardHDR10Helper.getQualityTitle(mActivity,value);
        mListener.onValueChanged(value);
    }
}
