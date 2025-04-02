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
package com.mediatek.camera.feature.setting.objecttracking;

import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.widget.PreviewFrameLayout;
import com.mediatek.camera.portability.SystemProperties;

/**
 * This is for object view ctrl.
 */
@SuppressWarnings("deprecation")
public class ObjectViewCtrl implements StatusMonitor.StatusChangeListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ObjectViewCtrl.class.getSimpleName());
    private static final String ROI_DEBUG_PROPERTY = "vendor.mtk.camera.app.3a.debug";

    private static final String FOCUS_STATE_KEY = "key_focus_state";
    private static final String FOCUS_STATE_PASSIVE_SCAN = "PASSIVE_SCAN";
    private static final String FOCUS_STATE_PASSIVE_FOCUSED = "PASSIVE_FOCUSED";
    private static final String FOCUS_STATE_PASSIVE_UNFOCUSED = "PASSIVE_UNFOCUSED";

    private static final int OBJECT_VIEW_PRIORITY = 10;
    private static final int HIDE_VIEW_DELAY_WHEN_NO_Object = 1000;
    private static final int HIDE_VIEW_TIMEOUT_WAIT_AF_SCAN = 1500;
    private static final int HIDE_VIEW_TIMEOUT_WAIT_AF_DONE = 3000;
    private static final int SHOW_INFO_LENGTH_LONG = 5 * 1000;

    private static final int MSG_OBJECT_VIEW_INIT = 0;
    private static final int MSG_OBJECT_VIEW_UNINIT = 1;
    private static final int MSG_OBJECT_VIEW_HIDE = 2;
    private static final int MSG_OBJECT_VIEW_NONE = 3;
    private static final int MSG_OBJECT_VIEW_AREA_SIZE = 4;
    private static final int MSG_OBJECT_VIEW_ORIENTATION_UPDATE = 5;
    private static final int MSG_OBJECT_UPDATE = 6;
    private static final int MSG_AUTO_FOCUS_CHANGED = 7;
    private static final int MSG_OBJECT_VIEW_WARNING_VIEW_SHOW = 8;

    private MainHandler mMainHandler;
    private PreviewFrameLayout mRootViewGroup;
    private FrameLayout mObjectLayout;
    private ObjectView mObjectView;
    private IApp mApp;
    private IAppUi mAppUi;
    private int mObjectNum;
    private boolean mIsEnable = true;
    private Animation mObjectExitAnim;
    private boolean mHideViewWhenObjectCountNotChange = true;
    private ObjectViewState mObjectViewState = ObjectViewState.STATE_UNINIT;
    private WaitFocusState mWaitFocusState = WaitFocusState.WAIT_NOTHING;

    private IAppUi.HintInfo mGuideHint;

    @Override
    public void onStatusChanged(String key, String value) {
//        if (!key.equals(FOCUS_STATE_KEY)) {
//            return;
//        }
//        if (value.equals(FOCUS_STATE_PASSIVE_SCAN)
//                || value.equals(FOCUS_STATE_PASSIVE_FOCUSED)
//                || value.equals(FOCUS_STATE_PASSIVE_UNFOCUSED)) {
//            mMainHandler.obtainMessage(MSG_AUTO_FOCUS_CHANGED, value).sendToTarget();
//        }
    }

    /**
     * Object View state.
     */
    private enum ObjectViewState {
        STATE_INIT,
        STATE_UNINIT
    }

    private enum WaitFocusState {
        WAIT_PASSIVE_SCAN,
        WAIT_PASSIVE_DONE,
        WAIT_NOTHING
    }

    /**
     * Init the view.
     *
     * @param app The camera activity.
     */
    public void init(IApp app) {
        mApp = app;
        mAppUi = app.getAppUi();
        mGuideHint = new IAppUi.HintInfo();
        int id = mApp.getActivity().getResources().getIdentifier("hint_text_background",
                "drawable", mApp.getActivity().getPackageName());
        mGuideHint.mBackground = mApp.getActivity().getDrawable(id);
        mGuideHint.mType = IAppUi.HintType.TYPE_AUTO_HIDE;
        mGuideHint.mDelayTime = SHOW_INFO_LENGTH_LONG;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(MSG_OBJECT_VIEW_INIT);
        mObjectExitAnim = AnimationUtils.loadAnimation(app.getActivity(),
                R.anim.object_tracking_exit);

        if (SystemProperties.getInt(ROI_DEBUG_PROPERTY, 0) == 1) {
            LogHelper.d(TAG, "[init] roi debug mode, set mHideViewWhenObjectCountNotChange = " +
                    "false");
            mHideViewWhenObjectCountNotChange = false;
        }
    }

    /**
     * Used to destroy the object view.
     */
    public void unInit() {
        mMainHandler.sendEmptyMessage(MSG_OBJECT_VIEW_UNINIT);

    }

    /**
     * For the preview area changed, object will update the Object coordinate.
     *
     * @param newPreviewArea the preview area.
     */
    public void onPreviewAreaChanged(RectF newPreviewArea) {
        mMainHandler.obtainMessage(MSG_OBJECT_VIEW_AREA_SIZE, newPreviewArea).sendToTarget();
    }

    /**
     * Enable object view to update
     *
     * @param enableView true, if need to enable, or false.
     */
    public void enableObjectView(boolean enableView) {
        mIsEnable = enableView;
        if (!enableView) {
            if (mObjectView != null) {
                mObjectView.resetReallyShown();
            }
            mMainHandler.sendEmptyMessage(MSG_OBJECT_VIEW_HIDE);
        }
    }

    /**
     * The object callback handle.
     *
     * @param object the tracked object.
     */
    public void onTrackedObjectUpdate(Object object) {
        if (!mIsEnable) {
            return;
        }
        if (object != null) {
            mMainHandler.obtainMessage(MSG_OBJECT_UPDATE, object).sendToTarget();
        } else {
            mMainHandler.obtainMessage(MSG_OBJECT_VIEW_NONE).sendToTarget();
        }
    }

    /**
     * For the preview state, when stop it should hide object view.
     *
     * @param isPreviewStarted is previewing or not.
     */
    public void onPreviewStatus(boolean isPreviewStarted) {
        if (!isPreviewStarted) {
            mMainHandler.sendEmptyMessage(MSG_OBJECT_VIEW_HIDE);
        }
    }

    /**
     * when activity orientation changed, the object will be updated.
     *
     * @param orientation the orientation for display.
     * @param cameraId    camera id.
     */
    public void updateObjectDisplayOrientation(int orientation, int cameraId) {
        mMainHandler.obtainMessage(MSG_OBJECT_VIEW_ORIENTATION_UPDATE,
                orientation, cameraId).sendToTarget();
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
            LogHelper.d(TAG, "[handleMessage] msg : " + msg.what);
            switch (msg.what) {
                case MSG_OBJECT_VIEW_HIDE:
                    hideView();
                    break;
                case MSG_OBJECT_VIEW_INIT:
                    initObjectView();
                    break;
                case MSG_OBJECT_VIEW_UNINIT:
                    unInitObjectView();
                    break;
                case MSG_OBJECT_UPDATE:
                    updateObjectViewByObject((Object) msg.obj);
                    break;
                case MSG_OBJECT_VIEW_NONE:
                    mObjectNum = 0;
                    hideView();
                    break;
                case MSG_OBJECT_VIEW_AREA_SIZE:
                    setObjectViewPreviewSize((RectF) msg.obj);
                    break;
                case MSG_OBJECT_VIEW_ORIENTATION_UPDATE:
                    setObjectViewDisplayOrientation(msg.arg1, msg.arg2);
                    break;
                case MSG_AUTO_FOCUS_CHANGED:
                    updateObjectViewByFocus((String) msg.obj);
                    break;
                case MSG_OBJECT_VIEW_WARNING_VIEW_SHOW:
                    showGuideView();
                    break;
                default:
                    break;
            }
        }
    }

    private void setObjectViewDisplayOrientation(int orientation, int cameraId) {
        updateViewDisplayOrientation(orientation, cameraId);
    }

    private void setObjectViewPreviewSize(RectF previewArea) {
        int width = Math.abs((int) previewArea.right - (int) previewArea.left);
        int height = Math.abs((int) previewArea.top - (int) previewArea.bottom);
        updateViewPreviewSize(width, height);
    }

    private void updateObjectViewByObject(Object object) {
        if (!mIsEnable) {
            LogHelper.d(TAG, "[updateObjectViewByObject] mIsEnable is false, ignore this time");
            return;
        }
        if (object != null
                && mObjectViewState == ObjectViewState.STATE_INIT) {
            // Check if object view has really been shown, if not , not hide view this time.
            // Why to do this check?
            // Maybe higher priority view is shown when object view wants to show, after higher
            // priority view is not shown, maybe object num is not changed too, it's time to hide
            // object view. So object view has no chance to show out.
                LogHelper.d(TAG, "[updateObjectViewByObject] new object num = " + object +
                        ", clear hide msg, show view right now");
                mMainHandler.removeMessages(MSG_OBJECT_VIEW_HIDE);
                mWaitFocusState = WaitFocusState.WAIT_PASSIVE_SCAN;
                showView();
                mObjectView.resetReallyShown();
                mObjectView.setObject(object);
        }
    }

    private void updateObjectViewByFocus(String focusState) {
        LogHelper.d(TAG, "[updateObjectViewByFocus] enter, focusState = " + focusState
                + ", mWaitFocusState = " + mWaitFocusState);
        if (!mIsEnable) {
            LogHelper.d(TAG, "[updateObjectViewByFocus] mIsEnable is false, ignore this time");
            return;
        }

        if (mObjectNum <= 0) {
            LogHelper.d(TAG, "[updateObjectViewByFocus] object num <= 0, ignore this time");
            return;
        }

        if (mObjectViewState != ObjectViewState.STATE_INIT) {
            LogHelper.d(TAG, "[updateObjectViewByFocus] object view not init, ignore this time");
            return;
        }

        if (focusState.equals(FOCUS_STATE_PASSIVE_SCAN)
                && mWaitFocusState == WaitFocusState.WAIT_PASSIVE_SCAN) {
            mWaitFocusState = WaitFocusState.WAIT_PASSIVE_DONE;
            LogHelper.d(TAG, "[updateObjectViewByFocus] clear hide msg, send hide msg delay " +
                    HIDE_VIEW_TIMEOUT_WAIT_AF_DONE + " ms");
            mMainHandler.removeMessages(MSG_OBJECT_VIEW_HIDE);
            mMainHandler.sendEmptyMessageDelayed(MSG_OBJECT_VIEW_HIDE,
                    HIDE_VIEW_TIMEOUT_WAIT_AF_DONE);
        } else if ((focusState.equals(FOCUS_STATE_PASSIVE_FOCUSED)
                || focusState.equals(FOCUS_STATE_PASSIVE_UNFOCUSED))
                && mWaitFocusState == WaitFocusState.WAIT_PASSIVE_DONE) {
            mWaitFocusState = WaitFocusState.WAIT_NOTHING;
            LogHelper.d(TAG, "[updateObjectViewByFocus] clear hide msg, hide view right now");
            mMainHandler.removeMessages(MSG_OBJECT_VIEW_HIDE);
            hideView();
        }
        LogHelper.d(TAG, "[updateObjectViewByFocus] exit, mWaitFocusState = " + mWaitFocusState);
    }

    private void initObjectView() {
        mRootViewGroup = mAppUi.getPreviewFrameLayout();
        mObjectLayout = (FrameLayout) mApp.getActivity().getLayoutInflater().inflate(
                R.layout.object_tracking_view, mRootViewGroup, true);
        mObjectView = (ObjectView) mObjectLayout.findViewById(R.id.object_view);
        mObjectViewState = ObjectViewState.STATE_INIT;
        mRootViewGroup.registerView(mObjectView, OBJECT_VIEW_PRIORITY);
    }

    public void showView() {
        if (mObjectView != null && mObjectView.getVisibility() != View.VISIBLE) {
            LogHelper.d(TAG, "[showView]");
            mObjectView.setVisibility(View.VISIBLE);
        }
    }

    public void hideView() {
        if (mObjectView != null && mObjectView.getVisibility() == View.VISIBLE) {
            LogHelper.d(TAG, "[hideView]");
            mMainHandler.removeCallbacksAndMessages(null);
            mObjectExitAnim.reset();
            mObjectView.clearAnimation();
            mObjectView.startAnimation(mObjectExitAnim);
            mObjectView.setVisibility(View.INVISIBLE);
        }
    }

    private void unInitObjectView() {
        mRootViewGroup.unRegisterView(mObjectView);
        mObjectView.setVisibility(View.GONE);
        mRootViewGroup.removeView(mObjectView);
        mObjectViewState = ObjectViewState.STATE_UNINIT;
        mObjectView = null;
    }

    private void updateViewDisplayOrientation(int displayOrientation, int cameraId) {
        if (mObjectView != null) {
            mObjectView.setDisplayOrientation(displayOrientation, cameraId);
        }
    }

    private void updateViewPreviewSize(int width, int height) {
        if (mObjectView != null) {
            mObjectView.updatePreviewSize(width, height);
        }
    }

    private void showGuideView() {
        mGuideHint.mHintText = mApp.getActivity().getString(R.string.object_tracking_warning);
        mApp.getAppUi().showScreenHint(mGuideHint);
    }

    /**
     * show warning info view.
     *
     */
    public void showWarningView() {
        if (mMainHandler != null) {
            mMainHandler.obtainMessage(MSG_OBJECT_VIEW_WARNING_VIEW_SHOW).sendToTarget();
        }
    }
}
