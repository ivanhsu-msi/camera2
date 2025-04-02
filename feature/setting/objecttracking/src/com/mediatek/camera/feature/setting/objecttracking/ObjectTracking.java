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

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.ICameraMode;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingBase;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.common.utils.CoordinatesTransform;
import com.mediatek.camera.common.utils.Size;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This is Face detection feature used to interact with other module.
 */
@SuppressWarnings("deprecation")
public class ObjectTracking extends SettingBase implements IAppUiListener.OnGestureListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ObjectTracking.class.getSimpleName());
    private static final String OBJECT_EXIST_KEY = "key_object_exist";
    private static final String FOCUS_STATE_KEY = "key_focus_state";
    private static final String HIGH_SPEED_KEY = "key_smvr_high_speed";
    private static final String HIGH_SPEED_MODE_KEY = "com.mediatek.camera.feature.mode.slowmotion.SlowMotionMode";
    private static final int PREVIEW_SIZE_DEFAULT_VALUE = 0;
    private static final String OBJECT_TRACKING_OFF = "off";
    private static final String OBJECT_TRACKING_ON = "on";



    private Handler mModeHandler;
    private Size mPreviewSize;
    private ObjectViewCtrl mObjectViewCtrl = new ObjectViewCtrl();
    private ObjectDeviceCtrl mObjectDeviceCtrl = new ObjectDeviceCtrl();
    private ISettingChangeRequester mSettingChangeRequester;
    private List<String> mSupportValues = new ArrayList<>();
    private StatusMonitor.StatusResponder mObjectExistStatusResponder;
    private boolean mIsObjectExistLastTime = false;
    private ObjectTrackingSettingView mSettingView;
    private List<Camera.Area> mTrackArea;
    private final RectF mPreviewRect = new RectF(0, 0, 0, 0);
    private boolean mIsHighSpeedRequest = false;
    private boolean mCanTracking = false;
    private boolean mTap2Update = false;
    private String mModeKey = null;

    private static final int OBJECT_VIEW_PRIORITY = 8;
    private static final float AF_REGION_BOX = 0.15f;

    /**
     * Initialize setting. This will be called when do open camera.
     *
     * @param app               the instance of IApp.
     * @param cameraContext     the CameraContext.
     * @param settingController the SettingController.
     */
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        LogHelper.d(TAG, "[init]");
        super.init(app, cameraContext, settingController);
        setValue(mDataStore.getValue(getKey(), IObjectConfig.OBJECT_TRACKING_OFF, getStoreScope()));
        LogHelper.d(TAG, "[init] value : " + mDataStore.getValue(getKey(),
                IObjectConfig.OBJECT_TRACKING_OFF, getStoreScope()));
        mSettingView = new ObjectTrackingSettingView();
        mSettingView.setObjectTrackingListener(mObjectTrackingViewListener);
        mModeHandler = new Handler(Looper.myLooper());
        mAppUi.registerGestureListener(this, OBJECT_VIEW_PRIORITY);
        mObjectDeviceCtrl.init();
        mObjectViewCtrl.init(app);
        mObjectDeviceCtrl.setObjectValueUpdateListener(mOnObjectValueUpdateListener);
        //init face device and face view first.
        app.registerOnOrientationChangeListener(mOrientationListener);
        app.getAppUi().registerOnPreviewAreaChangedListener(mPreviewAreaChangedListener);
        //initSettingValue();
        updateObjectDisplayOrientation();
        mStatusMonitor.registerValueChangedListener(HIGH_SPEED_KEY, mObjectStatusChangeListener);
        mObjectExistStatusResponder = mStatusMonitor.getStatusResponder(OBJECT_EXIST_KEY);
        mStatusMonitor.registerValueChangedListener(FOCUS_STATE_KEY, mObjectViewCtrl);
    }

    @Override
    public void updateModeDeviceState(String newState) {
        switch (newState) {
            case ICameraMode.MODE_DEVICE_STATE_CLOSED:
            case ICameraMode.MODE_DEVICE_STATE_UNKNOWN:
            case ICameraMode.MODE_DEVICE_STATE_CAPTURING:
                mCanTracking = false;
                break;
            case ICameraMode.MODE_DEVICE_STATE_PREVIEWING:
            case ICameraMode.MODE_DEVICE_STATE_RECORDING:
                mCanTracking = true;
                break;
            default:
                break;
        }
    }

    private StatusMonitor.StatusChangeListener mObjectStatusChangeListener = new StatusMonitor
                .StatusChangeListener() {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.i(TAG, "[onStatusChanged]+ key: " + key + "," +
                    "value: " + value );
            if(key.equals(HIGH_SPEED_KEY)&&HIGH_SPEED_MODE_KEY.equals(mModeKey)){
                mIsHighSpeedRequest = true;
            }
        }
    };
    @Override
    public void unInit() {
        LogHelper.d(TAG, "[unInit]");
        mIsObjectExistLastTime = false;
        mAppUi.unregisterGestureListener(this);
        mObjectViewCtrl.unInit();
        mApp.getAppUi().unregisterOnPreviewAreaChangedListener(mPreviewAreaChangedListener);
        mApp.unregisterOnOrientationChangeListener(mOrientationListener);
        mStatusMonitor.unregisterValueChangedListener(HIGH_SPEED_KEY, mObjectStatusChangeListener);
        mStatusMonitor.unregisterValueChangedListener(FOCUS_STATE_KEY, mObjectViewCtrl);
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
        return IObjectConfig.KEY_OBJECT_TRACKING;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        if (mSettingChangeRequester == null) {
            mSettingChangeRequester
                    = mObjectDeviceCtrl.getCaptureRequestConfigure(mSettingDevice2Requester);
        }
        return (ObjectCaptureRequestConfig) mSettingChangeRequester;
    }

    @Override
    public PreviewStateCallback getPreviewStateCallback() {
        return mPreviewStateCallback;
    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        super.overrideValues(headerKey, currentValue, supportValues);
        String curValue = getValue() == null ? IObjectConfig.OBJECT_TRACKING_OFF : getValue();
        if (mObjectDeviceCtrl.isObjectTrackingStatusChanged(curValue)) {
            LogHelper.d(TAG, "[overrideValues] curValue = " + curValue + ", headerKey = "
                    + headerKey);
            mObjectDeviceCtrl.updateObjectTrackingStatus(curValue);
            mObjectViewCtrl.enableObjectView(IObjectConfig.OBJECT_TRACKING_ON.equals(curValue));
            requestObjectTracking();
        }
    }

    @Override
    public void onModeOpened(String modeKey, ICameraMode.ModeType modeType) {
        LogHelper.d(TAG, "[onModeOpened] modeKey = " + modeKey);
        mModeKey = modeKey;
    }

    @Override
    public void onModeClosed(String modeKey) {
        LogHelper.d(TAG, "[onModeClosed] modeKey = " + modeKey);
        mTap2Update = false;
        if(mObjectViewCtrl != null) {
            mObjectViewCtrl.hideView();
        }
        mIsObjectExistLastTime = false;
        // Avoid to start FD of both front and back camera, when switch from PIP mode to other mode,
        // no need to recover FD state, because camera will be closed and then opened.
        if (!modeKey.startsWith("com.mediatek.camera.feature.mode.pip.")) {
            super.onModeClosed(modeKey);
        }
    }

    private void initSettingValue() {
        mSupportValues.add(IObjectConfig.OBJECT_TRACKING_OFF);
        mSupportValues.add(IObjectConfig.OBJECT_TRACKING_ON);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);
        String value = mDataStore.getValue(
                getKey(), IObjectConfig.OBJECT_TRACKING_ON, getStoreScope());
        setValue(value);
    }

    private IApp.OnOrientationChangeListener mOrientationListener =
            new IApp.OnOrientationChangeListener() {
                @Override
                public void onOrientationChanged(int orientation) {
                    mModeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (IObjectConfig.OBJECT_TRACKING_ON.equals(getValue())) {
                                updateObjectDisplayOrientation();
                                updateImageOrientation();
                            }
                        }
                    });
                }
            };

    private void updateObjectDisplayOrientation() {
        //orientation, g-sensor, no used
        int cameraId = Integer.valueOf(mSettingController.getCameraId());
        int displayRotation = CameraUtil.getDisplayRotation(mApp.getActivity());
        int displayOrientation = CameraUtil.getDisplayOrientationFromDeviceSpec(
                displayRotation, cameraId, mApp.getActivity());
        mObjectViewCtrl.updateObjectDisplayOrientation(displayOrientation, cameraId);
    }

    private void updateImageOrientation() {
        mObjectDeviceCtrl.updateImageOrientation();
    }

    private IAppUiListener.OnPreviewAreaChangedListener mPreviewAreaChangedListener
            = new IAppUiListener.OnPreviewAreaChangedListener() {
        @Override
        public void onPreviewAreaChanged(RectF newPreviewArea, Size previewSize) {
            mModeHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogHelper.d(TAG, "[onPreviewAreaChanged]");
                    mPreviewSize = previewSize;
                    mObjectViewCtrl.onPreviewAreaChanged(newPreviewArea);
                    mModeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setPreviewRect(newPreviewArea);
                        }
                    });
                }
            });
        }
    };

    private IObjectConfig.OnTrackedObjectUpdateListener mOnTrackedObjectUpdateListener
            = new IObjectConfig.OnTrackedObjectUpdateListener() {
        @Override
        public void onTrackedObjectUpdate(Object object) {
            if(!mTap2Update){
                return;
            }
            mObjectViewCtrl.onTrackedObjectUpdate(object);
            boolean isObjectExist = (object != null);
            if (isObjectExist != mIsObjectExistLastTime) {
                if (isObjectExist) {
                    mObjectExistStatusResponder.statusChanged(OBJECT_EXIST_KEY,
                            String.valueOf(true));
                } else {
                    mObjectExistStatusResponder.statusChanged(OBJECT_EXIST_KEY,
                            String.valueOf(false));
                }
                mIsObjectExistLastTime = isObjectExist;
            }
        }

        @Override
        public void onTrackNoObject() {
            mObjectViewCtrl.showWarningView();
        }
    };

    private IObjectConfig.OnObjectValueUpdateListener mOnObjectValueUpdateListener
            = new IObjectConfig.OnObjectValueUpdateListener() {
        @Override
        public Size onObjectPreviewSizeUpdate() {
            LogHelper.d(TAG, "[onObjectPreviewSizeUpdate]");
            if (mPreviewSize != null)
                return new Size(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            else
                return new Size(PREVIEW_SIZE_DEFAULT_VALUE, PREVIEW_SIZE_DEFAULT_VALUE);
        }

        @Override
        public int onUpdateImageOrientation() {
            int cameraId = Integer.valueOf(mSettingController.getCameraId());
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                    cameraId, mApp.getGSensorOrientation(), mApp.getActivity());
            LogHelper.d(TAG, "[onUpdateImageOrientation] camera id = " + cameraId + ", rotation = "
                    + rotation);
            return rotation;
        }

        @Override
        public String onGetValue() {
            return getValue();
        }

        @Override
        public void onObjectSettingValueUpdate(boolean isSupport, List<String> supportList) {
            setSupportedPlatformValues(supportList);
            setSupportedEntryValues(supportList);
            setEntryValues(supportList);
            mObjectDeviceCtrl.setTrackedObjectUpdateListener(mOnTrackedObjectUpdateListener);
        }
    };

    private PreviewStateCallback mPreviewStateCallback =
            new PreviewStateCallback() {

                @Override
                public void onPreviewStopped() {
                    mObjectViewCtrl.onPreviewStatus(false);
                    mObjectDeviceCtrl.onPreviewStatus(false);
                    mObjectDeviceCtrl.setTrackedObjectUpdateListener(null);
                }

                @Override
                public void onPreviewStarted() {
                    mObjectDeviceCtrl.onPreviewStatus(true);
                    mObjectDeviceCtrl.setTrackedObjectUpdateListener(mOnTrackedObjectUpdateListener);
                    requestObjectTracking();
                }
            };

    private void requestObjectTracking() {
        if (mSettingChangeRequester != null) {
            mSettingChangeRequester.sendSettingChangeRequest();
        }
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(float x, float y) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(float x, float y) {
        LogHelper.d(TAG, "[onSingleTapConfirmed] + x " + x + ",y = " + y);
        if(!mCanTracking){
            LogHelper.d(TAG, "[onSingleTapConfirmed] miss can not tracking");
            return false;
        }
        if(getValue().equals(OBJECT_TRACKING_ON)) {
            mObjectViewCtrl.hideView();
            mModeHandler.post(new Runnable() {
                @Override
                public void run() {
                    mObjectDeviceCtrl.sendObjectTrackingCancelCaptureRequest(mIsHighSpeedRequest);
                }
            });

        }
        LogHelper.d(TAG, "[onSingleTapConfirmed] -");
        return false;
    }

    @Override
    public boolean onDoubleTap(float x, float y) {
        mTap2Update = true;
        if (getValue().equals(OBJECT_TRACKING_OFF)) {
            LogHelper.d(TAG, "[onDoubleTap] Object tracking off");
            return false;
        }
        if(!mCanTracking){
            LogHelper.d(TAG, "[onDoubleTap] miss can not tracking");
            return false;
        }
        mObjectExistStatusResponder.statusChanged(OBJECT_EXIST_KEY,String.valueOf(true));
        LogHelper.d(TAG, "[onDoubleTap] + x " + x + ",y = " + y);
        mModeHandler.post(new Runnable() {
            @Override
            public void run() {
                //API2 face detection need to config face detect mode
                if (mSettingChangeRequester != null) {
                    mSettingChangeRequester.sendSettingChangeRequest();
                //step5:init focus and metering area and show focus UI
                    try {
                        initializeFocusAreas(x, y);
                    } catch (IllegalArgumentException e) {
                        LogHelper.e(TAG, "onDoubleTap IllegalArgumentException");
                        return;
                    }
                    mObjectDeviceCtrl.updateObjectArea(mTrackArea);
                    mObjectDeviceCtrl.sendObjectTrackingTriggerCaptureRequest();
                }else {
                    LogHelper.e(TAG, "onDoubleTap mSettingChangeRequester = null");
                }
            }
        });
        LogHelper.d(TAG, "[onDoubleTap]-");
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    @Override
    public boolean onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    @Override
    public boolean onLongPress(float x, float y) {
        LogHelper.d(TAG, "[onLongPress]");
        mObjectViewCtrl.hideView();
        mObjectDeviceCtrl.sendObjectTrackingCancelCaptureRequest(mIsHighSpeedRequest);
        return false;
    }

    @Override
    public void refreshViewEntry() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSettingView != null) {
                    mSettingView.setChecked(IObjectConfig.OBJECT_TRACKING_ON.equals(getValue()));
                    mSettingView.setEnabled(getEntryValues().size() > 1);
                }
            }
        });
    }

    @Override
    public void addViewEntry() {
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    private ObjectTrackingSettingView.OnObjectTrackingViewListener mObjectTrackingViewListener
            = new ObjectTrackingSettingView.OnObjectTrackingViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
            LogHelper.i(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? IObjectConfig.OBJECT_TRACKING_ON :
                    IObjectConfig.OBJECT_TRACKING_OFF;
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }

        @Override
        public boolean onCachedValue() {
            return IObjectConfig.OBJECT_TRACKING_ON.equals(
                    mDataStore.getValue(getKey(), IObjectConfig.OBJECT_TRACKING_OFF,
                            getStoreScope())
            );
        }
    };


    private void initializeFocusAreas(float x, float y) {
        LogHelper.d(TAG, "[initializeFocusAreas]");
        if (mTrackArea == null) {
            mTrackArea = new ArrayList<Camera.Area>();
            mTrackArea.add(new Camera.Area(new Rect(), 1));
        }
        Rect rect = new Rect();
        CameraUtil.rectFToRect(mPreviewRect, rect);
        int displayRotation = CameraUtil.getDisplayRotation(mActivity);
        mTrackArea.get(0).rect = CoordinatesTransform.uiToSensor(new Point((int) x,
                        (int) y), rect, displayRotation, AF_REGION_BOX,
                mObjectDeviceCtrl.getCropRegion(),
                mObjectDeviceCtrl.getCameraCharacteristics());
    }

    /**
     * This setter should be the only way to mutate mPreviewRect.
     */
    private void setPreviewRect(RectF previewRect) {
        LogHelper.d(TAG, "[setPreviewRect] ");
        if (!mPreviewRect.equals(previewRect)) {
            mPreviewRect.set(previewRect);
        }
    }
}
