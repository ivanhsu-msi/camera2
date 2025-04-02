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
package com.mediatek.aovtestapp.module;

import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.mediatek.aovtestapp.Log;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import vendor.mediatek.hardware.camera.aovservice.AovCharacteristic;
import vendor.mediatek.hardware.camera.aovservice.AovEvent;
import vendor.mediatek.hardware.camera.aovservice.AovInitParams;
import vendor.mediatek.hardware.camera.aovservice.AovResult;
import vendor.mediatek.hardware.camera.aovservice.AovResultData;
import vendor.mediatek.hardware.camera.aovservice.AovSensorInfo;
import vendor.mediatek.hardware.camera.aovservice.IAovService;
import vendor.mediatek.hardware.camera.aovservice.IAovServiceCallback;

//for aidl

//This module for aidl impl
public class ModuleAidl implements IModule, IBinder.DeathRecipient {

    private static final String TAG = ModuleAidl.class.getSimpleName();

    private static IAovService mAovService;

    private static ModuleAidl INSTANCE = null;
    private static final String AOV_AIDL_SERVICE_NAME = "vendor.mediatek.hardware.camera.aovservice.IAovService/default";

    private AovCharacteristic mAovCharacteristic;

    private final Object mLock = new Object();

    private ArrayList<ModuleDataChangeListener> mModuleDataChangeListeners = new ArrayList<>();

    private List<String> mSupportedModes = new ArrayList<>();

    @Override
    public void start(AovInitParamsCommon aovInitParams) {
        long startTime = System.nanoTime();
        try {
            mAovService.start(aovInitParams.toAidlRequestParams(AovInitParams.class));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        startTime = System.nanoTime() - startTime;
        for (int i = 0; i < mModuleDataChangeListeners.size(); i++) {
            mModuleDataChangeListeners.get(i).onSwitchTimeChanged(startTime);
        }
    }

    public ModuleAidl() {

        IBinder aovServiceBinder = android.os.ServiceManager
                .waitForService(AOV_AIDL_SERVICE_NAME);
        if (aovServiceBinder == null) {
            Log.w(TAG, "[ModuleAidl] aovServiceBinder is null");
            return;
        }

        try {
            aovServiceBinder.linkToDeath(this,/*flags*/ 0);
        } catch (RemoteException e) {
            // Camera service is now down, leave mCameraService as null
            Log.w(TAG, "[ModuleAidl] aov service is now down, leave mAovService as null", e);
            return;
        }

        mAovService = IAovService.Stub.asInterface(aovServiceBinder);

    }

    private int mIndexOfAovSensorInfo;
    @Override
    public String[] getResolutionList() {
        //data is like bellow:
        //AovCharacteristic{facing: 0, infos: [AovSensorInfo{size: [4096, 3072], frameRates: [24]},
        //                                     AovSensorInfo{size: [4096, 2304], frameRates: [60]},
        //                                     AovSensorInfo{size: [8192, 6144], frameRates: [24]},
        //                                     AovSensorInfo{size: [2048, 1152], frameRates: [240]},
        //                                     AovSensorInfo{size: [4208, 3120], frameRates: [30]},
        //                                     AovSensorInfo{size: [2104, 1560], frameRates: [60]}]}
        Log.d(TAG, "[getResolutionList] +");
        AovSensorInfo[] infos = mAovCharacteristic.infos;
        String[] resolutionList = new String[infos.length];
        for (int i = 0; i < infos.length; i++) {
            resolutionList[i] = "" +infos[i].size[0] + "X" + infos[i].size[1];
            Log.d(TAG, String.format(Locale.CANADA,"[getResolutionList] the %d size is %s"
                    ,i,resolutionList[i]));
        }
        Log.d(TAG, "[getResolutionList] -");
        return resolutionList;

    }

    @Override
    public void updateAovSensorInfoIndex(int index){
        mIndexOfAovSensorInfo = index;
    }

    @Override
    public String[] getFpsList() {
        //data is like bellow:
        //AovCharacteristic{facing: 0, infos: [AovSensorInfo{size: [4096, 3072], frameRates: [24]},
        //                                     AovSensorInfo{size: [4096, 2304], frameRates: [60]},
        //                                     AovSensorInfo{size: [8192, 6144], frameRates: [24]},
        //                                     AovSensorInfo{size: [2048, 1152], frameRates: [240]},
        //                                     AovSensorInfo{size: [4208, 3120], frameRates: [30]},
        //                                     AovSensorInfo{size: [2104, 1560], frameRates: [60]}]}
        Log.d(TAG, "[getFpsList] +");
        AovSensorInfo[] infos = mAovCharacteristic.infos;
        AovSensorInfo selectedAovSensorInfo = infos[mIndexOfAovSensorInfo];
        int[] frameRates = selectedAovSensorInfo.frameRates;
        String[] availableFrameList = new String[frameRates.length];

        for (int i = 0; i < frameRates.length; i++) {
            availableFrameList[i] = frameRates[i] + "";
            Log.d(TAG, String.format(Locale.CANADA,"[getFpsList] the %d size is %s"
                    ,i,frameRates[i]));
        }
        Log.d(TAG, "[getFpsList] -");
        return availableFrameList;

    }

    @Override
    public String[] getFDModeList() {
        ArrayList<String> result = new ArrayList<>();
        int length = mSupportedModes.size();
        for (int i = 0; i < length; i++) {
            String val = mSupportedModes.get(i);
            if ((AovDetectionObjectCommon.eOBJECT_NONE + "").equals(val)){
                result.add("NONE");
                continue;
            }
            if ((AovDetectionObjectCommon.eOBJECT_FACE_SIMPLE + "").equals(val)){
                result.add("SIMPLE");
                continue;
            }
            if ((AovDetectionObjectCommon.eOBJECT_FACE_FULL + "").equals(val)){
                result.add("FULL");
            }
        }

        return result.toArray(new String[result.size()]);

    }

    @Override
    public List<String> getSupportedModeList(int newId) {
        try{
            int availableModes = mAovService.getAvailableMode(newId);
            mSupportedModes.clear();
            while (availableModes >= 0) {
                if (availableModes >= AovDetectionObjectCommon.eOBJECT_QRCODE_SCANNER) {
                    mSupportedModes.add(AovDetectionObjectCommon.eOBJECT_QRCODE_SCANNER + "");
                    availableModes -= AovDetectionObjectCommon.eOBJECT_QRCODE_SCANNER;
                    continue;
                }
                if (availableModes >= AovDetectionObjectCommon.eOBJECT_GESTURE) {
                    mSupportedModes.add(AovDetectionObjectCommon.eOBJECT_GESTURE + "");
                    availableModes -= AovDetectionObjectCommon.eOBJECT_GESTURE;
                    continue;
                }

                if (availableModes >= AovDetectionObjectCommon.eOBJECT_GAZE) {
                    mSupportedModes.add(AovDetectionObjectCommon.eOBJECT_GAZE + "");
                    availableModes -= AovDetectionObjectCommon.eOBJECT_GAZE;
                    continue;
                }
                if (availableModes >= AovDetectionObjectCommon.eOBJECT_FACE_FULL) {
                    mSupportedModes.add(AovDetectionObjectCommon.eOBJECT_FACE_FULL + "");
                    availableModes -= AovDetectionObjectCommon.eOBJECT_FACE_FULL;
                    continue;
                }

                if (availableModes >= AovDetectionObjectCommon.eOBJECT_FACE_SIMPLE) {
                    mSupportedModes.add(AovDetectionObjectCommon.eOBJECT_FACE_SIMPLE + "");
                    availableModes -= AovDetectionObjectCommon.eOBJECT_FACE_SIMPLE;
                    continue;
                }

                if (availableModes >= AovDetectionObjectCommon.eOBJECT_NONE) {
                    mSupportedModes.add(AovDetectionObjectCommon.eOBJECT_NONE + "");
                    break;

                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mSupportedModes.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return Integer.parseInt(o1) - Integer.parseInt(o2);
                    }
                });
            }
            Log.i(TAG, "[getSupportedModeList] mSupportedModes = " + mSupportedModes);
            return mSupportedModes;
        }catch (RemoteException e){
            e.printStackTrace();
            return mSupportedModes;
        }

    }

    public static ModuleAidl getInstance() {
        if (INSTANCE == null) {
            synchronized (ModuleAidl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ModuleAidl();
                }
            }
        }
        return INSTANCE;

    }

    public void registerModuleDataChangeListener(ModuleDataChangeListener listener) {
        if (!mModuleDataChangeListeners.contains(listener)) {
            mModuleDataChangeListeners.add(listener);
        }

    }

    public void unRegisterModuleDataChangeListener(ModuleDataChangeListener listener) {
        if (mModuleDataChangeListeners.contains(listener)) {
            mModuleDataChangeListeners.remove(listener);
        }

    }

    @Override
    public void stop() {
        long stopTime = System.nanoTime();
        synchronized (mLock) {
            try {
                Log.d(TAG, "stop: +");
                mAovService.stop();
                Log.d(TAG, "stop: -");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        stopTime = System.nanoTime() - stopTime;
        for (int i = 0; i < mModuleDataChangeListeners.size(); i++) {
            mModuleDataChangeListeners.get(i).onSwitchTimeChanged(stopTime);
        }

    }

    @Override
    public int[] getSensorIdList() {
        synchronized (mLock) {
            try {
                Log.i(TAG, "[getSensorIdList] ");
                return mAovService.getSensorIdList();
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public void updateCurrentSelectedSensor(int sensorId) {
        try {
            mAovCharacteristic = mAovService.getCharacteristic(sensorId);
            Log.i(TAG,"[updateCurrentSelectedSensor] mAovCharacteristic " + mAovCharacteristic);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Object> aovResultToMap(AovResult[] results) {
        HashMap<String,Object> map = new HashMap<>();
        for (int i = 0; i < results.length; i++) {
            AovResult result = results[i];
            String key = result.key;
            if (key != null){
                Log.i(TAG, "[aovResultToMap] key = " + key + " result = " + result.data);
                if (result.data != null){
                    if (result.data.getTag() == AovResultData.intData){
                        int data = result.data.getIntData();
                        map.put(key,data);
                    }else if (result.data.getTag() == AovResultData.stringData){
                        String data = result.data.getStringData();
                        map.put(key,data);
                    }else if (result.data.getTag() == AovResultData.byteData){
                        byte data = result.data.getByteData();
                        map.put(key,data);
                    }else if (result.data.getTag() == AovResultData.vecByteData){
                        byte[] data = result.data.getVecByteData();
                        map.put(key,data);
                    }else if (result.data.getTag() == AovResultData.vecIntData){
                        int[] data = result.data.getVecIntData();
                        List<String> dat = new ArrayList<>();
                        if (data!=null){
                            //convert int[] to list so it can be shown
                            for (int j = 0; j < data.length; j++) {
                                dat.add(i+"");
                            }
                        }
                        map.put(key,dat);
                    }

                }

            }
        }
        return map;
    }

    private IAovServiceCallback.Stub mAovServiceConnectCallback = new IAovServiceCallback.Stub() {

        @Override
        public void onCallback(AovEvent aovEvent) throws RemoteException {
            Log.i(TAG, "[onCallback] ");
            if (aovEvent == null) {
                Log.w(TAG, "[onCallback] aovEvent == null");
                return;
            }

            AovResult[] results = aovEvent.results;

            if (results == null) {
                Log.w(TAG, "[onCallback] results == null");
                return;
            }

            HashMap<String, Object> data = aovResultToMap(results);
            for (int i = 0; i < mModuleDataChangeListeners.size(); i++) {
                mModuleDataChangeListeners.get(i).onResultCallback(data);
            }

        }

        @Override
        public void onPause() throws RemoteException {

        }

        @Override
        public void onResume() throws RemoteException {

        }

        @Override
        public int getInterfaceVersion() throws RemoteException {
            return VERSION;
        }

        @Override
        public String getInterfaceHash() throws RemoteException {
            return HASH;
        }

    };

    @Override
    public boolean connect() {
        synchronized (mLock) {
            try {
                Log.i(TAG, "[connect] connect + ");
                int success = mAovService.connect(mAovServiceConnectCallback);
                Log.i(TAG, "[connect] connect - ");
                if (success != 0) {
                    for (int i = 0; i < mModuleDataChangeListeners.size(); i++) {
                        mModuleDataChangeListeners.get(i).onServiceConnectFail();
                    }
                }
                return success == 0;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }

    }

    @Override
    public void disconnect() {
        synchronized (mLock) {
            try {
                Log.i(TAG, "[disconnect] + ");
                mAovService.disconnect();
                Log.i(TAG, "[disconnect] - ");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    public void binderDied() {
        synchronized (mLock) {
            // Only do this once per service death
            if (mAovService == null) {
                return;
            }
            mAovService = null;
        }
    }

}
