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
 *   MediaTek Inc. (C) 2022. All rights reserved.
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
package com.mediatek.aovtestapp.presenter;

import android.content.Context;
import android.os.HandlerThread;

import com.mediatek.aovtestapp.Log;
import com.mediatek.aovtestapp.SystemProperties;
import com.mediatek.aovtestapp.WaitDoneHandler;
import com.mediatek.aovtestapp.module.AovDetectionObjectCommon;
import com.mediatek.aovtestapp.module.AovInitParamsCommon;
import com.mediatek.aovtestapp.module.IModule;
import com.mediatek.aovtestapp.module.ModuleAidl;
import com.mediatek.aovtestapp.module.ModuleDataChangeListener;
import com.mediatek.aovtestapp.view.IView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public class Presenter implements IPresenter, ModuleDataChangeListener {

    private static final String TAG = Presenter.class.getSimpleName();

    private IView mView;
    private IModule mModule;

    private HandlerThread mModuleThread = null;
    private WaitDoneHandler mHandler = null;

    public Presenter(IView view) {
        startBackgroundThread();
        mView = view;
        mModule = ModuleAidl.getInstance();
    }

    @Override
    public void actApplyChanged(final boolean applied) {
        mHandler.blockRequest(new Callable<Void>() {
            @Override
            public Void call() {
                if (!applied) {
                    AovInitParamsCommon aovInitParams = summarizeCurrentInitParamsFromUi();
                    if (aovInitParams.detectionMode == 0) {
                        Log.i(TAG, "[call] detectMode is 0 do not start .");
                    } else {
                        mModule.start(aovInitParams);
                    }
                } else {
                    mModule.stop();
                }
                return null;
            }
        });
    }

    @Override
    public void onCreate(Context context) {
        startBackgroundThread();
        mHandler.blockRequest(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                //mView.startService("connect");
                mModule.connect();
                return null;
            }
        });

        //query sensorIdList and show on UI
        int[] sensorIdList = mHandler.blockRequest(new Callable<int[]>() {
            @Override
            public int[] call() throws Exception {
                return mModule.getSensorIdList();
            }
        });
        mView.setCameraIdList(sensorIdList);
        mModule.updateCurrentSelectedSensor(sensorIdList[0]);
        refreshUiDueToSensorIdChanged(sensorIdList[0]);

        //manually turn off apply button to request submit
        //mView.applyManually();
        mModule.registerModuleDataChangeListener(this);
    }

    @Override
    public void onResume() {

    }

    private void startBackgroundThread() {
        if (mModuleThread == null || !mModuleThread.isAlive()) {
            mModuleThread = new HandlerThread("ModuleThread");
            mModuleThread.start();
            mHandler = new WaitDoneHandler(mModuleThread.getLooper());
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        mModule.unRegisterModuleDataChangeListener(this);
        //mModule.disconnect();
        stopBackgroundThread();
        /*mModule.stop();
        mModule.disconnect();*/
    }

    @Override
    public void onCameraIdChanged(int newId) {
        Log.i(TAG,"[onCameraIdChanged]  newId = " + newId);
        mModule.updateCurrentSelectedSensor(newId);
        refreshUiDueToSensorIdChanged(newId);

    }

    @Override
    public void onResolutionChanged(String newResolution) {
        Log.i(TAG,"[onResolutionChanged] + newResolution = " + newResolution);
        String[] resolutionList = mModule.getResolutionList();
        int index = 0;
        for (int i = 0; i < resolutionList.length; i++) {
            if (newResolution.equals(resolutionList[i])){
                index = i;
                break;
            }
            if (i == resolutionList.length -1){
                Log.i(TAG,"[onResolutionChanged]  warning sensor info not updated.");
            }
        }
        mModule.updateAovSensorInfoIndex(index);
        mView.setFps(mModule.getFpsList());
        Log.i(TAG,"[onResolutionChanged] - ");
    }

    private void refreshUiDueToSensorIdChanged(final int newId) {
        //============= these ui related with cameraId change , refresh them when cameraId change =======/
        //query size list and show on UI
        String[] resolutionList = mHandler.blockRequest(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                return mModule.getResolutionList();
            }
        });
        mView.setPictureSizes(resolutionList);

        //query fps list and show on UI
        String[] fpsList = mHandler.blockRequest(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                return mModule.getFpsList();
            }
        });
        mView.setFps(fpsList);

        //query availableMode mode list and show on UI
        List<String> availableModes = mHandler.blockRequest(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return mModule.getSupportedModeList(newId);
            }
        });
        mView.visibleAvailableModes(availableModes);

        //query fd mode list and show on UI
        String[] fdModes = mHandler.blockRequest(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                return mModule.getFDModeList();
            }
        });
        mView.setFDMode(fdModes);
        //============= these ui related with cameraId change , refresh them when cameraId change =======/
    }

    private void stopBackgroundThread() {
        Log.i(TAG,"[stopBackgroundThread] + ");
        if (mModuleThread != null && mModuleThread.isAlive()) {
            mModuleThread.quitSafely();
            try {
                mModuleThread.join();
                mModuleThread = null;
                mModuleThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG,"[stopBackgroundThread] - ");
    }

    private AovInitParamsCommon summarizeCurrentInitParamsFromUi() {
        AovInitParamsCommon aovInitParams = new AovInitParamsCommon();

        aovInitParams.sensorId = Integer.parseInt(mView.getCurrentSelectCameraId());

        String currentSelectedResolution = mView.getCurrentSelectedResolution();
        aovInitParams.sensorHeight = Integer.parseInt(currentSelectedResolution.split("X")[1]);
        aovInitParams.sensorWidth = Integer.parseInt(currentSelectedResolution.split("X")[0]);
        aovInitParams.frameRate = mView.getCurrentSelectedFps();

        String currentSelectedFDMode = mView.getCurrentSelectedFDMode();
        int fdMode = currentSelectedFDMode.equals("NONE")
                ? AovDetectionObjectCommon.eOBJECT_NONE : currentSelectedFDMode.equals("SIMPLE")
                ? AovDetectionObjectCommon.eOBJECT_FACE_SIMPLE
                : AovDetectionObjectCommon.eOBJECT_FACE_FULL;
        aovInitParams.detectionMode |= fdMode;

        aovInitParams.detectionMode |= mView.isQRCodeTurnedOn() ? AovDetectionObjectCommon.eOBJECT_QRCODE_SCANNER : 0;

        aovInitParams.detectionMode |= mView.isGazeTurnedOn() ? AovDetectionObjectCommon.eOBJECT_GAZE : 0;

        aovInitParams.detectionMode |= mView.isGestureTurnedOn() ? AovDetectionObjectCommon.eOBJECT_GESTURE : 0;

        // mode on means no call back will return from hal
        aovInitParams.debugDisableCallback = !mView.isModeTurnedOn();

        Log.i(TAG, "[summarizeCurrentInitParamsFromUi] aovInitParams = " + aovInitParams);

        return aovInitParams;

    }

    @Override
    public void onResultCallback(HashMap<String,Object> data) {
        if (!mView.isModeTurnedOn()){
            //for measure power when mode turn off ignore all callbacks
            return;
        }

        byte[] vecByteData = (byte[]) data.get("yuvo1_output");
        if (vecByteData != null && vecByteData.length != 0) {
            int frameWidth = (int) data.get("frame_width");
            int frameHeight = (int) data.get("frame_height");
            int stride = (int) data.get("yuvo1_stride");
            mView.drawCanvasFrameByFrame(vecByteData,frameWidth,frameHeight,stride);
        }

        //remove yuv output data and show other result
        data.remove("yuvo1_output");

        int nums = (int) data.get("aie_output_FD_TOTAL_NUM");
        mView.setFDNums(String.format(Locale.getDefault(),"FD nums : %d ",nums));
        if (nums > 0){
            mView.wakeUpScreen();
        }
        data.remove("aie_output_FD_TOTAL_NUM");

        int detectMode = (int) data.get("detect_mode");
        boolean isGazeDetected = (detectMode&AovDetectionObjectCommon.eOBJECT_GAZE) != 0;
        mView.setGazeResultNew(isGazeDetected);

        mView.setMultiResultExceptBuffer(data);

        Log.i(TAG, "[onResultCallback] result == " + data);
    }

    @Override
    public void onServiceConnectFail() {
        mView.toast("Service connect fail");
        mView.quitApp();
    }

    @Override
    public void onSwitchTimeChanged(long switchTime) {
        mView.showSwitchTime(switchTime);
    }
}
