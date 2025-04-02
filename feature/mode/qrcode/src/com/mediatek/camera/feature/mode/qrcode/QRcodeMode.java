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
 *     MediaTek Inc. (C) 2019. All rights reserved.
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
package com.mediatek.camera.feature.mode.qrcode;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.CameraSysTrace;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.memory.IMemoryManager;
import com.mediatek.camera.common.memory.MemoryManagerImpl;
import com.mediatek.camera.common.mode.CameraModeBase;
import com.mediatek.camera.common.mode.DeviceUsage;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.setting.ISettingManager;
import com.mediatek.camera.common.setting.SettingManagerFactory;
import com.mediatek.camera.common.utils.Size;

import javax.annotation.Nonnull;

/**
 * Demo mode that is used to take normal picture.
 */

public class QRcodeMode extends CameraModeBase implements IQRcodeDeviceController.CaptureDataCallback,
        IQRcodeDeviceController.DeviceCallback, IQRcodeDeviceController.PreviewSizeCallback {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(QRcodeMode.class.getSimpleName());
    private static final String KEY_MATRIX_DISPLAY_SHOW = "key_matrix_display_show";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_VIDEO_QUALITY = "key_video_quality";

    private static final String JPEG_CALLBACK = "jpeg callback";
    private static final String POST_VIEW_CALLBACK = "post view callback";


    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";

    protected IQRcodeDeviceController mIDeviceController;
    protected QRcodeModeHelper mDemoModeHelper;
    protected int mCaptureWidth;
    // make sure the picture size ratio = mCaptureWidth / mCaptureHeight not to NAN.
    protected int mCaptureHeight = Integer.MAX_VALUE;
    //the reason is if surface is ready, let it to set to device controller, otherwise
    //if surface is ready but activity is not into resume ,will found the preview
    //can not start preview.
    protected volatile boolean mIsResumed = false;
    private String mCameraId;
    private ISettingManager mISettingManager;
    private MemoryManagerImpl mMemoryManager;
    private byte[] mPreviewData;
    private int mPreviewFormat;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Activity mActivity;

    private Object mPreviewDataSync = new Object();
    private StatusMonitor.StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private IMemoryManager.MemoryAction mMemoryState = IMemoryManager.MemoryAction.NORMAL;
    private View mScanView;
    private ImageView mScan_line;
    private IAppUiListener.ISurfaceStatusListener mISurfaceStatusListener
            = new SurfaceChangeListener();
    private IAppUi.HintInfo  mQRScanHint;
    private View mScanRootView;

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
                     boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
        super.init(app, cameraContext, isFromLaunch);
        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));
        LogHelper.d(TAG, "[init] mCameraId " + mCameraId);
        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        mIDeviceController = new QRcodeDevice2Controller(app.getActivity(), mICameraContext);
        mActivity = mIApp.getActivity();
        initQRcodeView();
        initAppUi();
        initSettingManager(mCameraId);
        initStatusMonitor();

        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        mDemoModeHelper = new QRcodeModeHelper(cameraContext);
        LogHelper.d(TAG, "[init]- ");
    }
    private void initQRcodeView(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScanRootView = mActivity.getLayoutInflater().inflate(R
                                    .layout.qrcodecapture,null);
                mIApp.getAppUi().getModeRootView().addView(mScanRootView);
                mScanView = mScanRootView.findViewById(R.id.scan_view);
                mScan_line = mScanRootView.findViewById(R.id.scan_line);
                mQRScanHint =  new IAppUi.HintInfo();
                mQRScanHint.mType = IAppUi.HintType.TYPE_ALWAYS_TOP;
                mQRScanHint.mHintText = mActivity.getString(R.string.qrcode_guide_hint);
                mIApp.getAppUi().showScreenHint(mQRScanHint);
                mIDeviceController.setScanView(mScanView);
                startScanAnimation();
            }
            });
    }

    private void startScanAnimation(){
        Animation tran_animation= AnimationUtils.loadAnimation(mActivity, R.anim.scan_line_tran);
        Animation alpha_animation= AnimationUtils.loadAnimation(mActivity, R.anim.scan_line_alpha);
        AnimationSet as = new AnimationSet(true);
        as.addAnimation(tran_animation);
        as.addAnimation(alpha_animation);
        mScan_line.setAnimation(as);
        tran_animation.startNow();
        alpha_animation.startNow();

    }
    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        LogHelper.d(TAG, "[resume]+");
        super.resume(deviceUsage);
        mIsResumed = true;
        initSettingManager(mCameraId);
        initStatusMonitor();
        mIDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);
        LogHelper.d(TAG, "[resume]-");
    }
    private void removeScanView(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mScanView.setVisibility(View.INVISIBLE);
                mIApp.getAppUi().getModeRootView().removeView(mScanRootView);
                if(mQRScanHint!=null){
                    mIApp.getAppUi().hideScreenHint(mQRScanHint);
                }
            }
        });
    }
    @Override
    public void pause(@Nonnull DeviceUsage nextModeDeviceUsage) {
        LogHelper.d(TAG, "[pause]+");
        super.pause(nextModeDeviceUsage);
        mIsResumed = false;
        //clear the surface listener
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);
        if (mNeedCloseCameraIds.size() > 0) {
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else if (mNeedCloseSession) {
            clearAllCallbacks(mCameraId);
            mIDeviceController.closeSession();
        } else {
            clearAllCallbacks(mCameraId);
            mIDeviceController.stopPreview();
        }
        LogHelper.d(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        super.unInit();
        removeScanView();
        mIDeviceController.destroyDeviceController();
    }

    @Override
    public boolean onShutterButtonFocus(boolean pressed) {
        return true;
    }

    @Override
    protected boolean doShutterButtonClick() {
        //Storage case
        return true;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        return false;
    }


    private void prepareAndOpenCamera(boolean b, String CameraId, boolean isFromLaunch) {
        mCameraId = CameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);


        //before open camera, prepare the device callback and size changed callback.
        mIDeviceController.setDeviceCallback(this);
        mIDeviceController.setPreviewSizeReadyCallback(this);
        //prepare device info.
        QRcodeDeviceInfo info = new QRcodeDeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        mIDeviceController.openCamera(info);
    }

    private void initSettingManager(String mCameraId) {
        SettingManagerFactory smf = mICameraContext.getSettingManagerFactory();
        mISettingManager = smf.getInstance(
                mCameraId,
                getModeKey(),
                ModeType.PHOTO,
                mCameraApi);
    }

    @Override
    protected ISettingManager getSettingManager() {
        return mISettingManager;
    }

    @Override
    public void onDataReceived(IQRcodeDeviceController.DataCallbackInfo dataCallbackInfo) {
        //when mode receive the data, need save it.
        byte[] data = dataCallbackInfo.data;
        int format = dataCallbackInfo.mBufferFormat;
        boolean needUpdateThumbnail = dataCallbackInfo.needUpdateThumbnail;
        boolean needRestartPreview = dataCallbackInfo.needRestartPreview;
        LogHelper.d(TAG, "onDataReceived, data = " + data + ",mIsResumed = " + mIsResumed +
                ",needUpdateThumbnail = " + needUpdateThumbnail + ",needRestartPreview = " +
                needRestartPreview);
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        }
        if (data != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, false);
        }
    }

    @Override
    public void onPostViewCallback(byte[] data) {
        LogHelper.d(TAG, "[onPostViewCallback] data = " + data + ",mIsResumed = " + mIsResumed);
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, true);
    }

    @Override
    public void onCameraOpened(String cameraId) {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void beforeCloseCamera() {
        updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
    }

    @Override
    public void afterStopPreview() {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void onPreviewCallback(byte[] data, int format) {
        if (!mIsResumed) {
            return;
        }
        mIApp.getAppUi().applyAllUIEnabled(true);
        mIApp.getAppUi().onPreviewStarted(mCameraId);
        synchronized (mPreviewDataSync) {
            LogHelper.d(TAG, "onPreviewCallback");
            updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);
            mPreviewData = data;
            mPreviewFormat = format;
        }
    }

    @Override
    public void onPreviewSizeReady(Size previewSize) {
        LogHelper.d(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
        updatePictureSizeAndPreviewSize(previewSize);
    }

    private void initStatusMonitor() {
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
    }


    private void updatePictureSizeAndPreviewSize(Size previewSize) {
        ISettingManager.SettingController controller = mISettingManager.getSettingController();
        String size = controller.queryValue(KEY_PICTURE_SIZE);
        if (size != null&&(checkQRScanActivity())?true:mIsResumed) {
            String[] pictureSizes = size.split("x");
            mCaptureWidth = Integer.parseInt(pictureSizes[0]);
            mCaptureHeight = Integer.parseInt(pictureSizes[1]);
            mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            LogHelper.d(TAG, "[updatePictureSizeAndPreviewSize] picture size: " + mCaptureWidth +
                    " X" + mCaptureHeight + ",current preview size:" + mPreviewWidth + " X " +
                    mPreviewHeight + ",new value :" + width + " X " + height);
            if (width != mPreviewWidth || height != mPreviewHeight) {
                onPreviewSizeChanged(width, height);
            }
        }

    }
    private boolean checkQRScanActivity() {
      android.app.ActivityManager am=(android.app.ActivityManager)mActivity
	.getApplicationContext()
	.getSystemService(mActivity.ACTIVITY_SERVICE);
	android.content.ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
	return cn.getClassName().equals("com.mediatek.camera.QrCodeActivity");
    }

    private void initAppUi() {
        mIApp.getAppUi().setUIVisibility(IAppUi.THUMBNAIL,View.GONE);
        mIApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_BUTTON,View.GONE);
        mIApp.getAppUi().setUIVisibility(IAppUi.SHUTTER_TEXT,View.GONE);
    }


    private void recycleSettingManager(String mCameraId) {
        mICameraContext.getSettingManagerFactory().recycle(mCameraId);
    }

    private void clearAllCallbacks(String mCameraId) {
        mIDeviceController.setPreviewSizeReadyCallback(null);
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.unregisterValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);


    }

    private void prePareAndCloseCamera(boolean needSync, String mCameraId) {
        clearAllCallbacks(mCameraId);
        mIDeviceController.closeCamera(needSync);
        //reset the preview size and preview data.
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    /**
     * surface changed listener.
     */
    private class SurfaceChangeListener implements IAppUiListener.ISurfaceStatusListener {

        @Override
        public void surfaceAvailable(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceAvailable,device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mIDeviceController != null && mIsResumed) {
                            mIDeviceController.updatePreviewSurface(surfaceObject);
                        }
                    }
                });
            }
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceChanged, device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mModeHandler != null) {
                mModeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mIDeviceController != null && mIsResumed) {
                            mIDeviceController.updatePreviewSurface(surfaceObject);
                        }
                    }
                });
            }
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceDestroyed,device controller = " + mIDeviceController);
        }
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusMonitor.StatusChangeListener {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged] key = " + key + ",value = " + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)) {
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mIDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            }
        }
    }

    private void onPreviewSizeChanged(int width, int height) {
        //Need reset the preview data to null if the preview size is changed.
        synchronized (mPreviewDataSync) {
            mPreviewData = null;
        }
        mPreviewWidth = width;
        mPreviewHeight = height;
        mIApp.getAppUi().setPreviewSize(mPreviewWidth, mPreviewHeight, mISurfaceStatusListener);
    }
}
