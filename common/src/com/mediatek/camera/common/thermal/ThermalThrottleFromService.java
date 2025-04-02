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
package com.mediatek.camera.common.thermal;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.IThermalEventListener;
import android.os.RemoteException;
import android.os.Temperature;
import android.os.IThermalService;
import android.os.ServiceManager;
import android.widget.Toast;

import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Use the  thermal service  to monitor the thermal status
 */
public class ThermalThrottleFromService implements IThermalThrottle {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(ThermalThrottleFromService.class.getSimpleName());
    // buffer point
    private static final int THERMAL_BUFFER_VALUE = 1;
    // urgent point
    private static final int THERMAL_URGENT_VALUE = Temperature.THROTTLING_EMERGENCY;

    private static final int WAITING_TIME = 30;
    private static final int UPDATE_TIME_DELAY = 1000;
    private static final int MSG_UPDATE_TIME = 1;

    private int mWaitingTime;

    private WarningDialog mAlertDialog;
    private Activity mActivity;
    private Resources mRes;
    private IThermalService mThermalService;
    private SkinThermalEventListener mSkinThermalEventListener;

    //    private final Handler mHandler =new Handler();
    protected final Handler mHandler;
    private boolean mIsResumed = false;
    private int mThermalStatus = -1;

    public ThermalThrottleFromService(IApp app) {
        mActivity = app.getActivity();
        mAlertDialog = new WarningDialog(app);
        mRes = mActivity.getResources();
        mHandler = new MainHandler(app.getActivity().getMainLooper());
        mSkinThermalEventListener = new SkinThermalEventListener();
    }

    final class SkinThermalEventListener extends IThermalEventListener.Stub {
        @Override
        public void notifyThrottling(Temperature temp) {
            mThermalStatus = temp.getStatus();
            LogHelper.d(TAG, "notifyThrottling mThermalStatus = " + mThermalStatus);
            if (mThermalStatus >= THERMAL_URGENT_VALUE) {
                if (mWaitingTime == WAITING_TIME) {
                    mAlertDialog.show();
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_TIME_DELAY);
                }
            } else {
                if (mAlertDialog.isShowing()) {
                    mAlertDialog.hide();
                    mWaitingTime = WAITING_TIME;
                }
            }
        }
    }

    private void updateCountDownTime(final Activity activity) {
        LogHelper.d(TAG, "[updateCountDownTime]mCountDown = " + mWaitingTime + ",mIsResumed = "
                + mIsResumed + "mWaitingTime = " + mWaitingTime);
        if (mThermalStatus >= THERMAL_BUFFER_VALUE) {
            if (mWaitingTime > 0) {
                mWaitingTime--;
                mAlertDialog.setCountDownTime(String.valueOf(mWaitingTime));
                if (mIsResumed) {
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_TIME_DELAY);
                }
            } else if (mWaitingTime == 0) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (activity.isFinishing() || activity.isDestroyed()) {
                            LogHelper.d(TAG, "[updateCountDownTime] don't need finish activity");
                        } else {
                            activity.finish();
                        }
                    }
                });
            }
        } else {
            LogHelper.d(TAG, "[updateCountDownTime]  mThermalStatus <  THERMAL_BUFFER_VALUE");
            if (mAlertDialog.isShowing()) {
                mAlertDialog.hide();
            }
            mAlertDialog.setCountDownTime(String.valueOf(WAITING_TIME));
            mWaitingTime = WAITING_TIME;
        }
    }

    private void initIThermalService() {
        if (mThermalService == null) {
            try {
                Class c = Class.forName("android.os.ServiceManager");
                Method method = c.getMethod("getService", String.class);
                IBinder binder = (IBinder) method.invoke(null, Context.THERMAL_SERVICE);
                mThermalService = IThermalService.Stub.asInterface(binder);
            } catch (ClassNotFoundException e) {
                LogHelper.d(TAG, "android.os.ServiceManager don't find");
            } catch (NoSuchMethodException e) {
                LogHelper.d(TAG, "method getService(String) don't find" +
                        " in android.os.ServiceManager class");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The main thread handler.
     */
    class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TIME:
                    updateCountDownTime(mActivity);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void resume() {
        LogHelper.d(TAG, "[resume]...");
        mIsResumed = true;
        mWaitingTime = WAITING_TIME;
        if (mThermalService == null) {
            initIThermalService();
            try {
                mThermalStatus = mThermalService.getCurrentThermalStatus();
            } catch (RemoteException e) {
                LogHelper.e(TAG, "Exception while getCurrentThermalStatus");
            }
        }
        LogHelper.d(TAG, "[resume] mThermalStatus = " + mThermalStatus + " " + THERMAL_URGENT_VALUE);
        if (mThermalStatus >= THERMAL_URGENT_VALUE && (!mActivity.isFinishing())) {
            int contentLaunch = mRes.getIdentifier("pref_thermal_dialog_content_launch", "string",
                    mActivity.getPackageName());
            Toast.makeText(mActivity, contentLaunch, Toast.LENGTH_LONG).show();
            mActivity.finish();
        }
        try {
            mThermalService.registerThermalEventListenerWithType(
                    mSkinThermalEventListener, Temperature.TYPE_SKIN);
        } catch (RemoteException e) {
            LogHelper.e(TAG, "Exception while unregisterThermalEventListener " + e);
        }
    }

    @Override
    public void pause() {
        LogHelper.d(TAG, "[pause]...");
        try {
            if (mThermalService != null) {
                mThermalService.unregisterThermalEventListener(mSkinThermalEventListener);
            }
        } catch (RemoteException e) {
            LogHelper.e(TAG, "Exception while unregisterThermalEventListener " + e);
        }

        mIsResumed = false;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mAlertDialog.isShowing()) {
            mAlertDialog.hide();
        }
        mAlertDialog.setCountDownTime(String.valueOf(WAITING_TIME));
        mWaitingTime = WAITING_TIME;
    }

    @Override
    public void destroy() {
        LogHelper.d(TAG, "[destroy]...");
        try {
            if (mThermalService != null) {
                mThermalService.unregisterThermalEventListener(mSkinThermalEventListener);
            }
        } catch (RemoteException e) {
            LogHelper.e(TAG, "Exception while unregisterThermalEventListener " + e);
        }
        mAlertDialog.uninitView();
    }
}
