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

package com.mediatek.camera.common;

import android.app.Activity;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;

import com.mediatek.camera.CameraApplication;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.bgservice.BGServiceKeeper;
import com.mediatek.camera.common.device.CameraDeviceManager;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.loader.FeatureProvider;
import com.mediatek.camera.common.location.LocationManager;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.relation.StatusMonitor;
import com.mediatek.camera.common.relation.StatusMonitorFactory;
import com.mediatek.camera.common.setting.SettingManagerFactory;
import com.mediatek.camera.common.sound.ISoundPlayback;
import com.mediatek.camera.common.sound.SoundPlaybackImpl;
import com.mediatek.camera.common.storage.IStorageService;
import com.mediatek.camera.common.storage.MediaSaver;
import com.mediatek.camera.common.storage.StorageServiceImpl;
import com.mediatek.camera.common.thermal.IThermalThrottle;
import com.mediatek.camera.common.thermal.ThermalThrottle;
import com.mediatek.camera.common.thermal.ThermalThrottleFromService;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.portability.SystemProperties;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * An implement of ICameraContext.
 */
public class CameraContext implements ICameraContext {
    private Activity mActivity;
    private SettingManagerFactory mSettingManagerFactory;
    private CameraDeviceManagerFactory mCameraDeviceManagerFactory;
    private StorageServiceImpl mStorageService;
    private MediaSaver mMediaSaver;
    private FeatureProvider mFeatureProvider;
    private DataStore mDataStore;
    private StatusMonitorFactory mStatusMonitorFactory;
    private SoundPlaybackImpl mSoundPlayback;
    private LocationManager mLocationManager;
    private IThermalThrottle mThermalThrottle;
    private BGServiceKeeper mBGServiceKeeper;
    private boolean mIsBGServiceEnabled;

    private static final int MTK_CAMERA_APP_VERSION_SEVEN = 7;
    private static final String THERMAL_THROTTLE_PATH = "/proc/driver/cl_cam_status";

    @Override
    public void create(IApp app, Activity activity) {
        mActivity = activity;
        mSoundPlayback = new SoundPlaybackImpl(activity);
        mLocationManager = new LocationManager(activity);
        mMediaSaver = new MediaSaver(activity);
        mDataStore = new DataStore(activity);
        mStatusMonitorFactory = new StatusMonitorFactory();
        mFeatureProvider = new FeatureProvider(app);

        mCameraDeviceManagerFactory = CameraDeviceManagerFactory.getInstance();
        mSettingManagerFactory = new SettingManagerFactory(app, this);
        mStorageService = new StorageServiceImpl(app, this);
        if(isThermalFileExist()){
            mThermalThrottle = new ThermalThrottle(app);
        }else{
            mThermalThrottle= new ThermalThrottleFromService(app);
        }

        boolean isForceDisableBGService =
                SystemProperties.getInt("vendor.debug.camera.bgservice.mode", 0) == 2
                        ? true : false;
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(
                mActivity.getApplicationContext()).getDeviceDescriptionMap().get("0");
        if (deviceDescription != null) {
            mIsBGServiceEnabled = deviceDescription.isBGServiceSupport()
                    && !isForceDisableBGService;
        }
        if (mIsBGServiceEnabled) {
            mBGServiceKeeper = ((CameraApplication) mActivity.getApplication())
                    .getBGServiceKeeper(this);
        }

        if (CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_SEVEN) {
            //start camerapostalgo service in background thread since the service start slowly

        }

    }

    @Override
    public void resume() {
        mStorageService.resume();
        mLocationManager.recordLocation(true);
        mThermalThrottle.resume();
    }

    @Override
    public void pause() {
        mStorageService.pause();
        mLocationManager.recordLocation(false);
        mSoundPlayback.pause();
        mThermalThrottle.pause();
    }

    @Override
    public void destroy() {
        if (CameraUtil.getAppVersionLevel() == MTK_CAMERA_APP_VERSION_SEVEN) {

        }
        mSoundPlayback.release();
        mThermalThrottle.destroy();

    }

    @Override
    public DataStore getDataStore() {
        return mDataStore;
    }

    @Override
    public IStorageService getStorageService() {
        return mStorageService;
    }

    @Override
    public CameraDeviceManager getDeviceManager(@Nonnull CameraApi api) {
        return mCameraDeviceManagerFactory.getCameraDeviceManager(mActivity.getApplicationContext(), api);
    }

    @Override
    public SettingManagerFactory getSettingManagerFactory() {
        return mSettingManagerFactory;
    }

    @Override
    public MediaSaver getMediaSaver() {
        return mMediaSaver;
    }

    @Override
    public FeatureProvider getFeatureProvider() {
        return mFeatureProvider;
    }

    @Override
    public StatusMonitor getStatusMonitor(String cameraId) {
        return mStatusMonitorFactory.getStatusMonitor(cameraId);
    }

    @Override
    public ISoundPlayback getSoundPlayback() {
        return mSoundPlayback;
    }

    @Override
    public Location getLocation() {
        return mLocationManager.getCurrentLocation();
    }

    @Override
    public BGServiceKeeper getBGServiceKeeper() {
        return mBGServiceKeeper;
    }



    /**
     *if /proc/driver/cl_cam_status file exist will get thermal from it
     * else use thermal service get thermal status
     * @return
     */
    private boolean isThermalFileExist(){
        File file=new File(THERMAL_THROTTLE_PATH);
        return file.exists();
    }


}
