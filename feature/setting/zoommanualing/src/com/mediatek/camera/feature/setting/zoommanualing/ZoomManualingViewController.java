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

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.widget.RotateImageView;

import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import java.util.List;
import java.math.BigDecimal;

/**
 * ZoomManualingViewController used to control ZoomManual ui and send view click item
 * to ZoomManual.
 */

public class ZoomManualingViewController{
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(ZoomManualingViewController.class.getSimpleName());

    private static final int ZM_PRIORITY = 70;
    private static final int MARGIN_IN_DP = 40;
    private static final String ZM_ON_VALUE = "on";
    private static final String ZM_OFF_VALUE = "off";

    private static final int ZM_VIEW_INIT = 0;
    private static final int ZM_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int ZM_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int ZM_VIEW_UPDATE_INDICATOR = 3;
    private static final int ZM_MODE_CLOSED_VIEW = 4;
    private static final int ZM_VIEW_UPDATE_QUICK_SWITCH_ICON = 5;
    private static final int ZM_VIEW_SET_SEEKBAR_VALUE = 6;
    private static final int ZM_VIEW_UNINIT = 7;

    private Activity mActivity;
    private MainHandler mMainHandler;
    private final IApp mApp;
    private final ZoomManualing mZoomManualing;

    private ImageView mZMEntryView;
    private ViewGroup mZMViewGroup;
    private View mZMSeekBarView;
    private SeekBar mZoomSeekBar;
    private List<Integer> mZmDefaultRatioList;
    private int mZoomValue = 0;

    /**
     * The constructor.
     * @param app IApp.
     * @param hdr Hdr.
     */
    public ZoomManualingViewController(IApp app, ZoomManualing zm) {
        mApp = app;
        mZoomManualing = zm;
        mActivity = mApp.getActivity();
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(ZM_VIEW_INIT);
    }

    /**
     * add ZM icon to quick switch.
     */
    public void addQuickSwitchIcon() {
        mMainHandler.sendEmptyMessage(ZM_VIEW_ADD_QUICK_SWITCH);
    }

    /**
     * remove quick switch icon.
     */
    public void removeQuickSwitchIcon() {
        mMainHandler.sendEmptyMessage(ZM_VIEW_REMOVE_QUICK_SWITCH);
    }

    /**
     * for overrides value, for set visibility.
     * @param isShow true means show.
     */
    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(ZM_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    /**
     * close ZM option menu.
     */
    public void modeClosedZMView() {
        mMainHandler.sendEmptyMessage(ZM_MODE_CLOSED_VIEW);
    }

    /**
     * show ZM indicator.
     * @param isShow true means show.
     */
    public void showZMIndicator(boolean isShow) {
        mMainHandler.obtainMessage(ZM_VIEW_UPDATE_INDICATOR, isShow).sendToTarget();
    }

    public void setSeekBarValue(List<Integer> zmDefaultRatioList){
        mZmDefaultRatioList = zmDefaultRatioList;

        mMainHandler.sendEmptyMessage(ZM_VIEW_SET_SEEKBAR_VALUE);
    }

    public void zmViewUninit(){
        mMainHandler.sendEmptyMessage(ZM_VIEW_UNINIT);
    }

    /**
     * Handler let some task execute in main thread.
     */
    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ZM_VIEW_INIT:
                    mZMEntryView = initZMEntryView();
                    initializeZMSeekBarView();
                    mApp.getAppUi().addOnViewChangeListener(mViewChangeListener);
                    break;

                case ZM_VIEW_ADD_QUICK_SWITCH:
                    mApp.getAppUi().addToQuickSwitcher(mZMEntryView, ZM_PRIORITY);
                    updateZMViewIcon();
                    break;

                case ZM_VIEW_REMOVE_QUICK_SWITCH:
                    mApp.getAppUi().removeFromQuickSwitcher(mZMEntryView);
                    break;

                case ZM_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mZMEntryView.setVisibility(View.VISIBLE);
                        updateZMViewIcon();
                    } else {
                        mZMEntryView.setVisibility(View.GONE);
                    }
                    break;

                case ZM_VIEW_UPDATE_INDICATOR:
                    break;

                case ZM_MODE_CLOSED_VIEW:
                    break;

                case ZM_VIEW_SET_SEEKBAR_VALUE:
                    setDefaultBarValue();
                    break;

                case ZM_VIEW_UNINIT:
                    mApp.getAppUi().removeOnViewChangeListener(mViewChangeListener);
                break;
                default:
                    break;
            }
        }
    }

    private void setDefaultBarValue(){
        if(mZoomSeekBar != null){
            int min = 0;
            mZoomSeekBar.setMin(min);

            int max = 100;
            mZoomSeekBar.setMax(max);

            int defZoomValue = min;
            mZoomValue = defZoomValue;
            mZoomSeekBar.setProgress(defZoomValue);
            LogHelper.i(TAG, "ZoomSeekBar minValue = " + mZoomSeekBar.getMin()
                    + ", maxValue = " + mZoomSeekBar.getMax()
                    + ", defZoomValue = " + defZoomValue);
        }
    }

    /**
     * Initialize the ZM view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initZMEntryView() {
        if(null == mActivity){
            mActivity = mApp.getActivity();
        }
        RotateImageView view = (RotateImageView) mActivity.getLayoutInflater().inflate(
                R.layout.zm_icon, null);
        view.setOnClickListener(mZMIconListener);
        return view;
    }

    /**
     * This listener used to monitor the zm quick switch icon click item.
     */
    private final View.OnClickListener mZMIconListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (!mZoomManualing.IsSupported()) {
                return;
            }

            showOrHideZMSeekBarView();

            updateZMViewIcon();
        }
    };

    private void initializeZMSeekBarView(){
        if (mZMViewGroup == null || mZMSeekBarView == null) {
            mZMViewGroup = mApp.getAppUi().getZMRootView();
            mZMSeekBarView = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.zm_seekbar_view, mZMViewGroup, true);
            mZoomSeekBar = mZMSeekBarView.findViewById(R.id.zm_zoom_seekbar);
            mZoomSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener());

            float density = mApp.getActivity().getResources().getDisplayMetrics().density;
            LogHelper.i(TAG, "initializeZMSeekBarView density = "+density);
            int marginInPix = (int) (MARGIN_IN_DP * density);
            //ViewMargin(mZMViewGroup, 0, marginInPix, 0, 0);
        }
    }

    private void showOrHideZMSeekBarView(){
        if(mZMViewGroup != null){
            if(mZMViewGroup.isShown()){
                LogHelper.d(TAG, "showOrHideZMSeekBarView GONE");
                mZMViewGroup.setVisibility(View.GONE);
            }else{
                LogHelper.d(TAG, "showOrHideZMSeekBarView VISIBLE");
                mZMViewGroup.setVisibility(View.VISIBLE);
                mZMViewGroup.setClickable(true);
                //view.setFocusable(true);
                //view.requestFocusFromTouch();
            }
        }else{
            LogHelper.d(TAG, "mZMViewGroup == null");
        }
    }

    private void hideOnViewChange(){
        if(mZMViewGroup != null && mZMViewGroup.isShown()){
            LogHelper.d(TAG, "hideOnFocusStatusChangeView GONE");
            mZMViewGroup.setVisibility(View.GONE);
        }
    }

    public void ViewMargin(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * Update zm entry view by current hdr value.
     */
    private void updateZMViewIcon() {
        if(mZMViewGroup != null && mZMViewGroup.isShown()){
            mZMEntryView.setImageResource(R.drawable.ic_zm_on);
            mZMEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_zm_on));
        } else {
            mZMEntryView.setImageResource(R.drawable.ic_zm_off);
            mZMEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_zm_off));
        }
    }

    private void updateOnViewChangeIcon() {
        if(mZMViewGroup != null && mZMViewGroup.isShown()){
            mZMEntryView.setImageResource(R.drawable.ic_zm_off);
            mZMEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.accessibility_zm_off));
        }
    }

    private IZoomManualingViewListener.OnZMChangeViewListener mViewChangeListener
            = new IZoomManualingViewListener.OnZMChangeViewListener() {
        @Override
        public void onZMChangeView() {

            updateOnViewChangeIcon();

            hideOnViewChange();
        }
    };

    class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()){
                case R.id.zm_zoom_seekbar:
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            switch (seekBar.getId()){
                case R.id.zm_zoom_seekbar:
                    LogHelper.d(TAG, "Zoom onStartTrackingTouch");
                    break;
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            switch (seekBar.getId()){
                case R.id.zm_zoom_seekbar:
                    mZoomValue = seekBar.getProgress();
                    LogHelper.d(TAG, "Zoom stopTrackingTouch progress = " + seekBar.getProgress());
                    break;
            }
            mZoomManualing.onValueChanged(mZoomValue);
        }
    }
}
